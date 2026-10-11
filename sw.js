// ComicCraft service worker: makes the app installable and usable offline.
// Bump V when you publish a new version so everyone gets the update.
const V='cc-v2';
const SHELL=['./','index.html','manifest.webmanifest','assets/banner.jpg','assets/icon.jpg','icons/icon-192.png','icons/icon-512.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(V).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==V).map(x=>caches.delete(x)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{
  const r=e.request,u=new URL(r.url);
  if(r.method!=='GET'||u.origin!==location.origin)return;
  const put=res=>{if(res&&res.ok){const cp=res.clone();caches.open(V).then(c=>c.put(r,cp))}return res};
  if(r.mode==='navigate'||u.pathname.endsWith('/index.html')){
    e.respondWith(fetch(r).then(put).catch(()=>caches.match(r).then(m=>m||caches.match('index.html'))));return}
  e.respondWith(caches.match(r).then(m=>m||fetch(r).then(put)));
});
