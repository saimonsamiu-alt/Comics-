// Vercel serverless function: turns a hand-drawn panel into a comic-style picture with Gemini.
// Needs the environment variable GEMINI_API_KEY (Vercel > Project > Settings > Environment Variables).
// Optional: GEMINI_IMAGE_MODEL (default below), ALLOWED_ORIGINS (comma separated, only needed if the website is hosted somewhere else, e.g. GitHub Pages).

const MODEL = process.env.GEMINI_IMAGE_MODEL || 'gemini-3.1-flash-image-preview';

const STYLES = {
  manga: 'Redraw this hand-drawn sketch as a clean black-and-white manga ink illustration: bold confident line art, crisp outlines, light screentone shading and a pure white background.',
  webtoon: 'Redraw this hand-drawn sketch as a vibrant full-color webtoon illustration with cel shading, clean outlines and bright friendly colors.',
  neon: 'Redraw this hand-drawn sketch as a neon noir comic panel: dark moody atmosphere, bold ink outlines, glowing cyan and pink highlights.'
};
const RULES = ' Keep the same characters, pose and composition as the drawing. Do not add any text, letters, logos or watermarks. Keep it friendly and suitable for all ages. If the picture shows a real person, draw a simple cartoon character instead of a realistic likeness.';

// very small best-effort limiter (resets when the serverless instance restarts)
const hits = new Map();
function limited(ip) {
  const now = Date.now(), win = 10 * 60 * 1000, max = 8;
  const list = (hits.get(ip) || []).filter(t => now - t < win);
  list.push(now);
  hits.set(ip, list);
  if (hits.size > 2000) hits.clear();
  return list.length > max;
}

module.exports = async (req, res) => {
  const origin = req.headers.origin || '';
  const allowed = (process.env.ALLOWED_ORIGINS || '').split(',').map(s => s.trim()).filter(Boolean);
  if (origin && allowed.includes(origin)) {
    res.setHeader('Access-Control-Allow-Origin', origin);
    res.setHeader('Vary', 'Origin');
    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  }
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method === 'GET') return res.status(200).json({ ai: !!process.env.GEMINI_API_KEY });
  if (req.method !== 'POST') return res.status(405).json({ error: 'method' });
  if (!process.env.GEMINI_API_KEY) return res.status(503).json({ error: 'off' });

  const ip = String(req.headers['x-forwarded-for'] || '').split(',')[0].trim() || 'unknown';
  if (limited(ip)) return res.status(429).json({ error: 'rate' });

  const body = typeof req.body === 'string' ? safeJson(req.body) : req.body || {};
  const m = /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/=]+)$/.exec(body.image || '');
  const prompt = STYLES[body.style];
  if (!m || !prompt) return res.status(400).json({ error: 'bad' });
  if (m[2].length > 2200000) return res.status(413).json({ error: 'big' });

  const ctl = new AbortController();
  const timer = setTimeout(() => ctl.abort(), 55000);
  try {
    const r = await fetch('https://generativelanguage.googleapis.com/v1beta/models/' + encodeURIComponent(MODEL) + ':generateContent', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'x-goog-api-key': process.env.GEMINI_API_KEY },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt + RULES }, { inlineData: { mimeType: m[1], data: m[2] } }] }],
        generationConfig: { responseModalities: ['TEXT', 'IMAGE'] }
      }),
      signal: ctl.signal
    });
    const j = await r.json();
    if (!r.ok) {
      console.error('Gemini error', r.status, JSON.stringify(j).slice(0, 400));
      return res.status(r.status === 429 ? 429 : 502).json({ error: r.status === 429 ? 'rate' : 'upstream' });
    }
    const parts = (j.candidates && j.candidates[0] && j.candidates[0].content && j.candidates[0].content.parts) || [];
    const img = parts.find(p => (p.inlineData || p.inline_data) && p.thought !== true);
    const d = img && (img.inlineData || img.inline_data);
    if (!d) return res.status(502).json({ error: 'noimage' });
    return res.status(200).json({ image: 'data:' + (d.mimeType || d.mime_type || 'image/png') + ';base64,' + d.data });
  } catch (e) {
    console.error('ai-ink failed', e && e.message);
    return res.status(502).json({ error: 'upstream' });
  } finally {
    clearTimeout(timer);
  }
};

function safeJson(s) { try { return JSON.parse(s); } catch (e) { return {}; } }
