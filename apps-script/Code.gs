/**
 * ComicCraft backend on Google Sheets + Google Drive (no Firebase needed).
 *
 * Setup: see SETUP_SHEETS_BN.md. In short:
 *  1. Create a Google Sheet. Extensions > Apps Script. Paste this file.
 *  2. Run the function  setup  once (it asks for permission, then creates the tabs and the image folder).
 *  3. Deploy > New deployment > Web app > Execute as: Me, Who has access: Anyone. Copy the URL.
 *  4. Put the URL in firebase-config.js (COMICCRAFT_SHEETS.url).
 *
 * Moderation: new feed posts and comics stay hidden from everyone except their author until you tick
 * the "approved" box in the sheet. Tick "hidden" to take something down at any time.
 */
var CONFIG = {
  REQUIRE_APPROVAL: true,     // true = feed posts and comics appear only after you tick "approved"
  MAX_WRITES_PER_10_MIN: 40,  // per person
  MAX_DOC_CHARS: 45000,       // a sheet cell holds 50000 characters
  MAX_IMAGE_CHARS: 600000,    // one picture (as text) must be smaller than this
  IMAGE_FOLDER: 'ComicCraft images'
};

var HEAD = ['col', 'id', 'updatedAt', 'approved', 'hidden', 'title', 'author', 'ownerHash', 'data'];
var OWNER_FIELD = { feed: 'authorId', comics: 'authorId', pages: 'authorId', collaborations: 'ownerId', reports: 'reporterId', bookmarks: 'userId' };
var MODERATED = { feed: true, comics: true };
var COUNTERS = { likesCount: 1, viewCount: 1, remixCount: 1 };
var IMAGE_KEYS = ['img', 'imageUrl', 'coverUrl'];

function setup() {
  Object.keys(OWNER_FIELD).forEach(function (tab) { sheetFor(tab); });
  imageFolder();
  return 'ready';
}

function doGet() { return out({ ok: true, ping: true, sheets: true }); }

function doPost(e) {
  var res;
  try {
    var body = JSON.parse((e && e.postData && e.postData.contents) || '{}');
    res = handle(body);
  } catch (err) {
    res = { error: err && err.code ? err.code : 'server' };
    if (!err || !err.code) console.error(err && err.stack || err);
  }
  return out(res);
}

function out(o) {
  return ContentService.createTextOutput(JSON.stringify(o)).setMimeType(ContentService.MimeType.JSON);
}
function fail(code) { var e = new Error(code); e.code = code; throw e; }

function tabFor(col) {
  if (typeof col !== 'string') return null;
  var p = col.split('/');
  if (p.length === 1 && OWNER_FIELD[p[0]] && p[0] !== 'pages' && p[0] !== 'bookmarks') return p[0];
  if (p.length === 3 && p[0] === 'comics' && p[2] === 'pages') return 'pages';
  if (p.length === 3 && p[0] === 'users' && p[2] === 'bookmarks') return 'bookmarks';
  return null;
}

function handle(b) {
  if (b.action === 'ping') return { ok: true };
  var tab = tabFor(b.col);
  if (!tab) fail('col');
  var who = identify(b);
  if (b.action === 'list') return list(tab, b, who);
  if (!who.hash) fail('auth');
  throttle(who.id);
  var lock = LockService.getScriptLock();
  lock.waitLock(20000);
  try {
    if (b.action === 'put') return put(tab, b, who);
    if (b.action === 'del') return del(tab, b, who);
    if (b.action === 'inc') return inc(tab, b, who);
    fail('action');
  } finally { lock.releaseLock(); }
}

/* The browser sends a public id (uid) and a private random secret. Only the secret proves ownership. */
function identify(b) {
  var id = String(b.uid || '').replace(/[^A-Za-z0-9_-]/g, '').slice(0, 64);
  var secret = String(b.secret || '');
  var hash = secret.length >= 20 ? sha(secret) : '';
  return { id: id, hash: hash };
}
function sha(s) {
  var d = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, s, Utilities.Charset.UTF_8);
  return d.map(function (x) { return ('0' + (x & 0xff).toString(16)).slice(-2); }).join('');
}
function throttle(id) {
  var cache = CacheService.getScriptCache(), k = 'w_' + id, n = Number(cache.get(k) || 0) + 1;
  if (n > CONFIG.MAX_WRITES_PER_10_MIN) fail('rate');
  cache.put(k, String(n), 600);
}

function sheetFor(tab) {
  var ss = SpreadsheetApp.getActiveSpreadsheet(), sh = ss.getSheetByName(tab);
  if (!sh) {
    sh = ss.insertSheet(tab);
    sh.getRange(1, 1, 1, HEAD.length).setValues([HEAD]);
    sh.setFrozenRows(1);
  }
  return sh;
}
function truthy(v) { return v === true || String(v).toUpperCase() === 'TRUE'; }
function rows(tab) {
  var sh = sheetFor(tab), n = sh.getLastRow();
  if (n < 2) return { sh: sh, list: [] };
  var vals = sh.getRange(2, 1, n - 1, HEAD.length).getValues();
  return { sh: sh, list: vals.map(function (r, i) { return { row: i + 2, v: r }; }) };
}
function findRow(tab, col, id) {
  var r = rows(tab), hit = null;
  r.list.forEach(function (x) { if (x.v[0] === col && x.v[1] === id) hit = x; });
  return { sh: r.sh, hit: hit };
}

function list(tab, b, who) {
  var order = ['updatedAt', 'index', 'createdAt', 'likesCount'].indexOf(b.order) >= 0 ? b.order : 'updatedAt';
  var dir = b.dir === 'asc' ? 1 : -1, limit = Math.min(Number(b.limit) || 60, 200), docs = [];
  rows(tab).list.forEach(function (x) {
    var v = x.v;
    if (v[0] !== b.col || truthy(v[4])) return;
    var mine = who.hash && v[7] === who.hash, ok = truthy(v[3]);
    if (!ok && !mine) return;
    var d;
    try { d = JSON.parse(v[8]); } catch (e) { return; }
    d.id = v[1];
    if (!ok) d.pending = true;
    docs.push(d);
  });
  docs.sort(function (a, c) { var x = a[order], y = c[order]; return (x > y ? 1 : x < y ? -1 : 0) * dir; });
  return { docs: docs.slice(0, limit) };
}

function saveImage(url) {
  var m = /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+\/=]+)$/.exec(url);
  if (!m || url.length > CONFIG.MAX_IMAGE_CHARS) fail('image');
  var blob = Utilities.newBlob(Utilities.base64Decode(m[2]), m[1], 'cc_' + Date.now() + '.jpg');
  var f = imageFolder().createFile(blob);
  f.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  return 'https://lh3.googleusercontent.com/d/' + f.getId();
}
function imageFolder() {
  var it = DriveApp.getFoldersByName(CONFIG.IMAGE_FOLDER);
  return it.hasNext() ? it.next() : DriveApp.createFolder(CONFIG.IMAGE_FOLDER);
}

function put(tab, b, who) {
  var doc = b.doc;
  if (!doc || !/^[A-Za-z0-9_-]{1,64}$/.test(String(doc.id || ''))) fail('id');
  var id = doc.id; doc = JSON.parse(JSON.stringify(doc)); delete doc.id; delete doc.pending;
  IMAGE_KEYS.forEach(function (k) { if (typeof doc[k] === 'string' && doc[k].indexOf('data:image/') === 0) doc[k] = saveImage(doc[k]); });
  var found = findRow(tab, b.col, id), now = new Date().toISOString();
  var title = String(doc.title || '').slice(0, 80), author = String(doc.authorName || doc.ownerName || '').slice(0, 40);
  if (found.hit) {
    var v = found.hit.v, open = (tab === 'collaborations');   // collab rooms can be edited by anyone with the link
    if (!open && v[7] !== who.hash) fail('forbidden');
    if (!open) doc[OWNER_FIELD[tab]] = who.id;
    else if (v[7] !== who.hash) { var old = JSON.parse(v[8]); doc.ownerId = old.ownerId; doc.ownerName = old.ownerName; }
    var json = JSON.stringify(doc);
    if (json.length > CONFIG.MAX_DOC_CHARS) fail('big');
    found.sh.getRange(found.hit.row, 3).setValue(doc.updatedAt || now);
    found.sh.getRange(found.hit.row, 6, 1, 2).setValues([[title, author]]);
    found.sh.getRange(found.hit.row, 9).setValue(json);
    return { ok: true, doc: withId(doc, id) };
  }
  doc[OWNER_FIELD[tab]] = who.id;
  var json2 = JSON.stringify(doc);
  if (json2.length > CONFIG.MAX_DOC_CHARS) fail('big');
  var approved = !(CONFIG.REQUIRE_APPROVAL && MODERATED[tab]);
  var sh = sheetFor(tab), r = sh.getLastRow() + 1;
  sh.getRange(r, 1, 1, HEAD.length).setValues([[b.col, id, doc.updatedAt || now, approved, false, title, author, who.hash, json2]]);
  sh.getRange(r, 4, 1, 2).insertCheckboxes();
  sh.getRange(r, 4, 1, 2).setValues([[approved, false]]);
  return { ok: true, doc: withId(doc, id), pending: !approved };
}
function withId(d, id) { d.id = id; return d; }

function del(tab, b, who) {
  var f = findRow(tab, b.col, String(b.id || ''));
  if (!f.hit) return { ok: true };
  if (f.hit.v[7] !== who.hash) fail('forbidden');
  f.sh.deleteRow(f.hit.row);
  return { ok: true };
}

/* likes, views and remixes: anyone may change a counter by one */
function inc(tab, b, who) {
  if (!COUNTERS[b.field]) fail('field');
  var d = Math.max(-1, Math.min(1, Number(b.delta) || 0));
  var f = findRow(tab, b.col, String(b.id || ''));
  if (!f.hit) return { ok: false };
  var doc = JSON.parse(f.hit.v[8]);
  doc[b.field] = Math.max(0, (Number(doc[b.field]) || 0) + d);
  f.sh.getRange(f.hit.row, 9).setValue(JSON.stringify(doc));
  return { ok: true };
}
