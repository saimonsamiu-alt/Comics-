# ComicCraft - Hand-Drawn Animated Comic Maker 🎨📖

ComicCraft turns paper notebook drawings into dynamic, animated comics and webtoons using AI inking, cel-shading art styles, speech bubbles, child-friendly stickers, and interactive panel layouts.

---

## 🚀 How to Open and Run from GitHub in Android Studio

If you cloned or downloaded this repository from GitHub, follow these simple steps to open and run it:

### 1. Requirements
* **Android Studio** (Koala / Ladybug / Iguana or later recommended)
* **JDK 17 or JDK 21** (bundled directly with Android Studio)
* **Android Device or Emulator** (API 26+)

### 2. Opening the Project
1. Launch **Android Studio**.
2. Click **File > Open...** (or **Open** from the welcome screen).
3. Select the root folder of this project (`ComicCraft`) and click **OK**.
4. Android Studio will automatically detect the **Gradle Wrapper** (`gradlew`) and sync all dependencies.

### 3. Running the App
* Click the **Green Play button (▶ Run 'app')** in the top toolbar to build and install the app onto your emulator or connected Android device.

### 4. Command Line Build
To build the debug APK via terminal:
```bash
# On macOS / Linux:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```
The resulting APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

To run local unit tests:
```bash
./gradlew testDebugUnitTest
```

---

## 🌟 Features Included

1. **Comic Page Layout Selection Screen**: Choose between:
   * **Single Epic Panel** (1:1 Splash art & cover showcase)
   * **3-Panel Story Strip** (Setup • Conflict • Punchline webtoon strip)
   * **4-Panel Comic Grid** (2x2 classic Yonkoma / Newspaper gag strip)
   * **2-Panel Action Split** (Dominant action + reaction showdown)
   * **Cinematic Widescreen** (16:9 anime movie panoramic)
2. **AI Lineart & Sketch Inking**: Whitens notebook paper texture, sharpens pencil strokes into bold comic inks, and adds vibrant cel-shaded color grading.
3. **Speech Balloons & Pop-Art Stickers**: Fully draggable, resizable, and customizable dialogue balloons (Speech, Thought, Shout, Whisper) and child-friendly stickers.
4. **Undo / Redo Stack**: Step forward or backward through any changes to dialogue, stickers, sketches, and styling.
5. **High-Res Gallery Export & Sharing**: Export finished panels as 1200x1200px lossless PNGs directly to device gallery (`Pictures/ComicCraft`) with zero storage permissions (Android Scoped Storage) or share via system share sheet to Discord, Instagram, Messages, or WhatsApp.
6. **Firebase Firestore Cloud Persistence**: Sync bookmarks, user profiles, and save complete comic panel states with server timestamps.
7. **Offline-First Local Room Database**: Stores paper camera captures and drawings locally on device.
