# ComicCraft Web: সেটআপ গাইড (বাংলা)

কিছু না করলেও অ্যাপ চলে। তখন সবকিছু ব্যবহারকারীর নিজের ব্রাউজারে সেভ হয়। নিচের ধাপগুলো দিয়ে তিনটা জিনিস চালু করবে:
**(ক)** সবার জন্য ভাগ করা ফিড ও কমিক, **(খ)** লাইভ কোল্যাব, **(গ)** এআই কমিক ম্যাজিক।

---

## ১. সাইট পাবলিশ করা

**Vercel (সবচেয়ে ভালো, কারণ এআই লাগলে এটাই লাগবে)**
1. vercel.com এ **Add New > Project**, তোমার GitHub রিপো (`Comics-`) ইমপোর্ট করো।
2. Framework Preset: **Other**। Build Command খালি রাখো। **Deploy** চাপো।

**GitHub Pages**
রিপো **Settings > Pages > Deploy from a branch > main / root**। (এখানে এআই চলবে না, ধাপ ৪ দেখো।)

রিপোর মূল জায়গায় এই সব ফাইল থাকতে হবে: `index.html`, `sw.js`, `manifest.webmanifest`, `firebase-config.js`, `vercel.json`, `api/ai-ink.js`, আর `assets/` ও `icons/` ফোল্ডার।

---

## ২. Firebase চালু (ভাগ করা ফিড, প্রকাশিত কমিক, কোল্যাব)

1. [Firebase console](https://console.firebase.google.com) এ তোমার অ্যান্ড্রয়েড অ্যাপের **একই প্রজেক্ট** খোলো।
2. **Project settings > Your apps > Add app > Web (`</>`)**। নাম দাও `ComicCraft Web`। যে `firebaseConfig` দেখাবে সেটা কপি করো।
3. `firebase-config.js` ফাইলে `apiKey`, `authDomain`, `projectId`, `appId` বসাও। `firestoreDatabaseId` আগে থেকেই বসানো আছে (তোমার অ্যান্ড্রয়েড অ্যাপের ডাটাবেস)। তুমি যদি `(default)` ডাটাবেস ব্যবহার করো, তাহলে ওটা খালি রাখো।
4. **Authentication > Sign-in method > Google** চালু করো।
5. **Authentication > Settings > Authorized domains** এ তোমার সাইটের ডোমেইন যোগ করো (যেমন `saimonsamiu-alt.github.io` আর তোমার `*.vercel.app` ডোমেইন)।
6. **Firestore Database > Rules** এ এই রিপোর `firestore.rules` ফাইলের লেখা বসিয়ে **Publish** চাপো। এই নতুন নিয়মে:
   - কমিক, পোস্ট ও কোল্যাব বানাতে সাইন ইন লাগে, আর নিজের নামেই বানাতে হয়।
   - যেকোনো সাইন-ইন করা মানুষ লাইক, ভিউ ও রিমিক্স সংখ্যা এক করে বাড়াতে পারে।
   - `reports` কালেকশনে রিপোর্ট জমা হয়, শুধু তুমি কনসোলে দেখতে পাবে।
7. **Google Cloud Console > APIs & Services > Credentials** এ ওয়েব API key-তে **HTTP referrer** সীমা দাও (শুধু তোমার সাইট)।

**পরীক্ষা:** দুইটা আলাদা ব্রাউজারে দুই অ্যাকাউন্টে সাইন ইন করে একটাতে কমিক প্রকাশ করো, অন্যটাতে পড়ে দেখো।
**মনে রাখো:** আমি এই ক্লাউড কোড আর নিয়ম আসল Firebase-এ চালিয়ে দেখিনি। রিপোর `firestore.test.js` দিয়ে Firebase emulator-এ নিয়মগুলো আগে পরীক্ষা করে নাও, বিশেষ করে তোমার অ্যান্ড্রয়েড অ্যাপ যেন ভেঙে না যায়।

---

## ৩. মডারেশন (ছোটদের জন্য জরুরি)

- পাঠকেরা 🚩 চাপলে Firestore-এর `reports` কালেকশনে জমা হয়।
- কোনো কমিক বা পোস্ট লুকাতে ওই ডকুমেন্টে `hidden` ফিল্ডে `true` লেখো। অ্যাপ তখন আর দেখাবে না। পুরোপুরি মুছতে ডকুমেন্ট ডিলিট করো।
- নিয়মিত `reports` দেখার একটা অভ্যাস করো।
- ১৩ বছরের কম বয়সীদের জন্য ভাগ করা ফিড চালু করার আগে তোমার দেশের শিশু-গোপনীয়তা নিয়ম (যেমন COPPA) আর অভিভাবকের সম্মতির কথা ভেবে নাও।

---

## ৪. এআই কমিক ম্যাজিক চালু (Vercel)

1. [Google AI Studio](https://aistudio.google.com) থেকে একটা **Gemini API key** নাও।
2. Vercel প্রজেক্টে **Settings > Environment Variables** এ `GEMINI_API_KEY` নামে বসাও। তারপর **Redeploy** করো।
3. সাইট খুলে স্টুডিওর **আঁকো** ট্যাবে **"এআই কমিক ম্যাজিক"** দেখা গেলে চালু হয়েছে। (key না থাকলে বোতামটা আপনা-আপনি লুকিয়ে থাকে।)
4. ছবির মডেলের নাম বদলাতে পারে। ডিফল্ট হলো `gemini-3.1-flash-image-preview`। ai.google.dev-এ বর্তমান নাম দেখে দরকার হলে `GEMINI_IMAGE_MODEL` ভ্যারিয়েবলে বসাও।
5. **খরচ:** প্রতিটা ছবির জন্য Google টাকা নেয়। Google Cloud-এ বাজেট সীমা আর সতর্কতা দাও। ফাংশনে প্রতি আইপি থেকে ১০ মিনিটে ৮টা সীমা আছে, কিন্তু এটা পুরোপুরি নিরাপদ নয়। বেশি ব্যবহার হলে সাইন-ইন বাধ্যতামূলক করা ভালো।

**সাইট GitHub Pages-এ থাকলে:** Vercel-এও একই রিপো ডিপ্লয় করো, তারপর
- Vercel-এ `ALLOWED_ORIGINS` = `https://saimonsamiu-alt.github.io`
- `firebase-config.js` এ `COMICCRAFT_AI_URL` = `https://তোমার-অ্যাপ.vercel.app/api/ai-ink`

**ছবির গোপনীয়তা:** এআই চালালে ছবি Google-এর Gemini-তে যায়। অ্যাপ প্রথমবার সম্মতি চায় আর মুখ বা ব্যক্তিগত তথ্য না দিতে বলে।

---

## ৫. নতুন সংস্করণ দিলে
`sw.js` এ `const V='cc-v2'` কে `cc-v3` করো, যাতে ফোনে পুরনো সংস্করণ আটকে না থাকে।
