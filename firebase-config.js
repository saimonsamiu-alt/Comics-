// Optional settings. Leave everything empty to keep the app fully local (nothing is uploaded).
//
// OPTION A: Google Sheets + Drive (no Firebase, no accounts). Easiest. Steps: SETUP_SHEETS_BN.md
//   Paste the Web app URL you get from Apps Script ("https://script.google.com/macros/s/.../exec").
window.COMICCRAFT_SHEETS = {
  url: "https://script.google.com/macros/s/AKfycbxVSM-SSzNFTkK75MGdEsZS-MrMjvvrIGtAFFRGOPkEBPlxgq9LS7bPhKzNV5LjV5Kj/exec"
};

// OPTION B: Firebase (real accounts with Google sign-in, live updates). Steps: SETUP_BN.md
//   Used only when COMICCRAFT_SHEETS.url above is empty.
window.COMICCRAFT_FIREBASE = {
  apiKey: "",
  authDomain: "",
  projectId: "",
  appId: "",
  firestoreDatabaseId: "ai-studio-android-comiccra-d6b98e48-7901-4d3d-b738-2f60b3636c26"
};

// AI Comic Magic: only if this website is NOT on the same Vercel project as /api/ai-ink
// (for example the site is on GitHub Pages). Put the full Vercel URL, e.g. "https://your-app.vercel.app/api/ai-ink"
window.COMICCRAFT_AI_URL = "";
