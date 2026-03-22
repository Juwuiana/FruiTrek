# 🌿 FruiTrek — Interactive Edge-AI Botanical Explorer for Children

> **CS1240 CIS302 — Graphics and Visual Computing**
> Mapúa Malayan College Laguna | March 2026

**Espiritu, Julliana Clarise O. · Sabandal, Charles Louie C. · Tidalgo, Jerinel S. · Tingson, Kristian Carl N.**

---

## What is FruiTrek?

FruiTrek is an Android app that uses on-device AI to help children (ages 4–8) identify Philippine tropical fruits in real time. Point your camera at a fruit → the app detects it, draws a bounding box, speaks its name aloud, and unlocks it in a gamified **Fruit Passport** with its Filipino name, Baybayin script, and a fun cultural fact.

---

## Features

- **The Lens** — Live camera + TFLite object detection draws child-friendly bounding boxes
- **The Voice** — Android TTS speaks the fruit name in English and Filipino on confirmed detection
- **The Passport** — Gamified sticker collection that persists across sessions (DataStore)
- **Baybayin Script** — Every fruit entry shows its traditional Filipino script translation
- **Child-first UI** — Large buttons, big text, bouncy animations, designed for ages 4–8

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.2.10 |
| UI | Jetpack Compose | BOM 2024.09.00 |
| Navigation | Navigation Compose | 2.9.7 |
| Camera | CameraX | 1.5.3 |
| Edge AI | TensorFlow Lite | 2.17.0 |
| ML Model | YOLOv8n → TFLite | 320×320 INT8 |
| Persistence | DataStore Preferences | 1.2.1 |
| Audio | Android TextToSpeech | SDK built-in |
| Build | AGP | 9.1.0 |
| Min SDK | Android 8.0 | API 26 |

---

## Project Structure

```
app/src/main/
├── assets/
│   ├── fruitrek_model.tflite    ← ⚠️ YOU MUST ADD THIS (see Training section)
│   └── labels.txt               ← fruit class names, one per line
│
├── java/com/example/fruitrek/
│   ├── MainActivity.kt          ← entry point, navigation graph
│   │
│   ├── data/
│   │   ├── model/
│   │   │   ├── Fruit.kt         ← data class + FruitCatalog (8 fruits with Filipino names, Baybayin, fun facts)
│   │   │   └── DetectionResult.kt ← label + confidence + bounding box
│   │   └── repository/
│   │       └── PassportRepository.kt ← saves unlocked fruits using DataStore
│   │
│   ├── ml/
│   │   └── FruitDetector.kt     ← TFLite wrapper, YOLO output parsing, NMS
│   │
│   └── ui/
│       ├── FruiTrekViewModel.kt  ← sustained detection (8 frames), TTS, unlock logic
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Type.kt
│       │   └── Theme.kt
│       ├── components/
│       │   ├── DetectionOverlay.kt   ← Canvas bounding boxes drawn over camera
│       │   └── CelebrationOverlay.kt ← bouncy popup on new fruit discovery
│       └── screens/
│           ├── LandingScreen.kt      ← splash/home with bouncing emojis + team info
│           ├── PermissionScreen.kt   ← camera permission request
│           ├── CameraScreen.kt       ← main scanner screen
│           └── PassportScreen.kt     ← fruit collection grid + detail sheet
```

---

## Fruits Supported

| English | Filipino | Baybayin |
|---|---|---|
| Mango | Mangga | ᜋᜅ᜔ᜄ |
| Banana | Saging | ᜐᜄᜒᜅ᜔ |
| Pineapple | Pinya | ᜉᜒᜈ᜔ᜌ |
| Papaya | Papaya | ᜉᜉᜌ |
| Coconut | Niyog | ᜈᜒᜌᜓᜄ᜔ |
| Jackfruit | Langka | ᜎᜅ᜔ᜃ |
| Guava | Bayabas | ᜊᜌᜊᜐ᜔ |
| Starfruit | Balimbing | ᜊᜎᜒᜋ᜔ᜊᜒᜅ᜔ |

---

## Setup

### Prerequisites
- Android Studio Meerkat (2024.3) or newer
- Android phone with USB debugging enabled (or emulator API 26+)
- Internet connection for first Gradle sync

### Steps
1. Clone this repo and open the folder in Android Studio
2. Wait for Gradle sync to finish (~3–5 min first time)
3. Add `fruitrek_model.tflite` to `app/src/main/assets/` (see Training below)
4. Connect your phone via USB → press **Run ▶**
5. Grant camera permission on the app's first launch

---

## Training the TFLite Model

The app will show **"Loading fruit detector..."** forever until you add the model file. Use Google Colab (free) to train it.

### 1. Collect images
- 80–150 photos per fruit class (8 classes = ~640–1200 images total)
- Use different backgrounds, angles, and lighting
- Sources: your own photos, [Roboflow Universe](https://universe.roboflow.com), [Kaggle](https://www.kaggle.com/datasets)

### 2. Label with Roboflow
1. Create a free account at [roboflow.com](https://roboflow.com)
2. New project → **Object Detection**
3. Upload images → draw bounding boxes → assign labels (use exact names from `labels.txt`)
4. Generate dataset: 70/20/10 split, enable Flip + Rotation + Brightness augmentations
5. Export → **YOLOv8 format** → Download ZIP

### 3. Train on Google Colab
```python
# Cell 1
!pip install ultralytics roboflow

# Cell 2 — download your labeled dataset
from roboflow import Roboflow
rf = Roboflow(api_key="YOUR_API_KEY")
dataset = rf.workspace("YOUR_WORKSPACE").project("YOUR_PROJECT").version(1).download("yolov8")

# Cell 3 — train YOLOv8n (nano = smallest, fastest on mobile)
from ultralytics import YOLO
model = YOLO('yolov8n.pt')
model.train(data=f'{dataset.location}/data.yaml', epochs=60, imgsz=320, batch=16)

# Cell 4 — export to TFLite
model.export(format='tflite', imgsz=320, int8=True)

# Cell 5 — download
from google.colab import files
files.download('runs/detect/train/weights/best_saved_model/best_float16.tflite')
```

### 4. Add to project
- Rename the file to `fruitrek_model.tflite`
- Copy it to `app/src/main/assets/`
- Rebuild the app

> Training takes ~20–35 minutes on Colab's free GPU tier.

---

## How Detection Works

```
Camera frame (CameraX)
    ↓ resize to 320×320
    ↓ normalize RGB to 0.0–1.0 floats
    ↓ TFLite inference → raw tensor [1, 4+classes, 2100]
    ↓ parse cx,cy,w,h + class scores per anchor
    ↓ filter by confidence ≥ 0.45
    ↓ Non-Maximum Suppression (IoU threshold 0.45)
    ↓ DetectionOverlay draws boxes on screen
    ↓ ViewModel counts consecutive frames
    ↓ 8 frames same fruit → confirmed detection
    ↓ PassportRepository.unlockFruit() + TTS speaks name + celebration popup
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| "Loading fruit detector..." stuck | `fruitrek_model.tflite` is missing from `assets/` |
| `Cannot add extension 'kotlin'` | Remove `id("org.jetbrains.kotlin.android")` from plugins — keep only `kotlin.plugin.compose` |
| `Unresolved reference 'camera'` | Gradle sync incomplete → **File → Sync Project with Gradle Files** |
| Camera shows black screen | Phone Settings → Apps → FruiTrek → Permissions → enable Camera |
| Gradle sync failed | **File → Invalidate Caches → Restart**, then re-sync |
| TTS not speaking | Check device volume. Use a real device — emulators often lack TTS engines |
| Duplicate `.so` / namespace conflict | Add `packaging { jniLibs { pickFirsts += "**/*.so" } }` to `android {}` block |

---

## Important Gradle Notes

These settings in `build.gradle.kts` are critical — do not change them:

```kotlin
// ✅ Only 2 plugins — do NOT add kotlin.android (causes duplicate extension crash with AGP 9.x)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ✅ kotlin block must be OUTSIDE android { }
kotlin { jvmToolchain(11) }

android {
    // ✅ Required — prevents .tflite from being compressed (model won't load without this)
    androidResources { noCompress += "tflite" }

    // ✅ Required — prevents .so library conflicts from TFLite
    packaging { jniLibs { pickFirsts += "**/*.so" } }
}
```

---

## Academic Context

This project transitions prior computer vision work (OpenCV static image processing — thresholding, contour detection, calibration) into modern **Edge AI deployment**. It demonstrates:

- Custom dataset collection and annotation
- YOLOv8 model training and INT8 quantization
- TFLite on-device inference (no internet required)
- Real-time object detection on Android
- Child-accessible UI/UX for early childhood education

---

## License

Academic project — Mapúa Malayan College Laguna, March 2026.
