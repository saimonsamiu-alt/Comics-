# ComicCraft Web

The web version of the ComicCraft Android app. Works on phones, tablets and computers, and can be installed like an app (PWA).

## What is inside
- **Explore**: search and genre filter, 6 sample comics, "keep reading" with bookmarks
- **Reader**: panel-by-panel reading, rating, likes, bookmarks, comments
- **Studio**: live camera scanner (framing guide, torch, front/back, AI Lineart Boost, retake), sketch gallery in Profile (open, rename, delete), 5 page layouts (single, 3-strip, 4-grid, 2-split, widescreen), pen and eraser, camera / photo to comic ink, 5 backgrounds, speech / thought / shout / whisper bubbles, sound effects, 32 stickers, 5 color styles, camera motion (and WebM video export), undo / redo, PNG download and share
- **Publish comic**: publish pages as a comic (up to 100 pages, with chapters) that everyone can read in Explore
- **Free comic filters** (on the device, no server, no key): comic ink, color toon, manga screentone
- **Safety filter**: blocks emails, phone numbers, links and rude words in posts, comics, comments, collabs and nicknames (edit `BAD_WORDS` to add more)
- **AI Comic Magic** (optional, paid/quota API): redraw a sketch in manga / webtoon / neon style (needs Vercel + Gemini key)
- **Feed**: community posts, shared likes, remix any picture, report button
- **Collab**: shared story scripts in rooms with an invite link
- **Profile & Library**: nickname, avatar, saved comics, bookmarks, your posts
- English and Bangla, dark and light theme

## Files (upload all of them to the repo root, keep the folders)
```
index.html  manifest.webmanifest  sw.js  firebase-config.js  vercel.json
firestore.rules  SETUP_BN.md  SETUP_SHEETS_BN.md
api/ai-ink.js  apps-script/Code.gs
assets/banner.jpg  assets/icon.jpg
icons/icon-192.png  icons/icon-512.png  icons/apple-touch-icon.png
```
**Step-by-step setup for Firebase, moderation and AI: see `SETUP_BN.md` (Bangla).**

## Publish
- **GitHub Pages**: Settings > Pages > Deploy from a branch > `main` / root
- **Vercel**: Import the repo, Framework Preset **Other**, empty Build Command, Deploy

Open it from the published https address (not by double-clicking the file) so photos, remix and install work.
When you publish a new version, change `V='cc-v1'` in `sw.js` to `cc-v2` so phones pick up the update.

## Local mode (default)
Everything is saved in the visitor's browser. The Feed and Collab work on one device (collab even syncs between tabs). Nothing is uploaded.

## Shared Feed, comics and Collab: pick ONE backend (optional)
- **Google Sheets + Drive** (no Firebase, no accounts, you approve every post in the sheet): `SETUP_SHEETS_BN.md`. Good for small and medium communities.
- **Firebase** (Google sign-in, live updates, scales further): see below and `SETUP_BN.md`.

### Firebase
Paste your Firebase Web config into `firebase-config.js`, enable Google sign-in, publish `firestore.rules`. Details in `SETUP_BN.md`. This cloud code and the rules were written but not tested against a real Firebase project: run `firestore.test.js` in the emulator first.

## AI Comic Magic (optional)
`api/ai-ink.js` is a Vercel serverless function that calls Gemini. Set `GEMINI_API_KEY` in Vercel. The button stays hidden until the function answers. Untested against the real Gemini API (tested with a fake response).

## Kids and safety
The app asks for a nickname only, and tells kids not to share personal details. Google sign-in is for 13+ or with a parent. Before turning on the public Feed for under-13s, check COPPA / local child-privacy rules and plan moderation (reports, removal).

## Moderation
Readers can report posts and comics (collection `reports`). Set `hidden: true` on a document in the Firebase console to hide it.
