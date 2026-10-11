// Optional settings. Leave everything empty to keep the app fully local (no accounts, nothing uploaded).
//
// 1) Shared Community feed, published comics and live Collab (Firebase):
//    paste your Firebase WEB app config here. See SETUP_BN.md for the steps.
window.COMICCRAFT_FIREBASE = {
  apiKey: "",
  authDomain: "",
  projectId: "",
  appId: "",
  firestoreDatabaseId: "ai-studio-android-comiccra-d6b98e48-7901-4d3d-b738-2f60b3636c26"
};
// 2) AI Comic Magic: only if this website is NOT hosted on the same Vercel project as /api/ai-ink
//    (for example the site is on GitHub Pages). Put the full Vercel URL, e.g. "https://your-app.vercel.app/api/ai-ink"
window.COMICCRAFT_AI_URL = "";
