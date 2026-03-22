FruiTrek
Interactive Edge-AI Botanical Explorer for Children

 
1. Project Overview
FruiTrek is a mobile Android application that uses on-device (Edge) AI to help children aged 4-8 identify Philippine tropical fruits in real time. When a child points their phone camera at a fruit, the app detects it, draws a friendly bounding box, speaks its name aloud, and unlocks it in a gamified Fruit Passport — complete with its Filipino name, Baybayin script, and a fun cultural fact.

The project transitions our earlier OpenCV-based fruit analysis (static image processing) into a modern deep learning mobile deployment, demonstrating Edge AI competency required for CS1240.

Project Objectives
•	Real-time fruit detection using a lightweight TFLite model running entirely on-device
•	Auditory feedback via Text-to-Speech to support language development in early childhood
•	Cultural literacy through Filipino fruit names, Baybayin script translations, and fun facts
•	Gamified learning via the Fruit Passport — a persistent sticker collection that rewards discovery

Target Audience
Early childhood education, ages 4-8. The UI is designed for low-dexterity users: large buttons (minimum 72dp), big text, bright colors, and simple navigation.

 
2. Technology Stack
Layer	Technology	Version / Details
Language	Kotlin	2.2.10
UI Framework	Jetpack Compose	BOM 2024.09.00
Navigation	Navigation Compose	2.9.7
Camera	CameraX	1.5.3 - lifecycle-aware
Edge AI / ML	TensorFlow Lite	2.17.0 - on-device inference
ML Model	YOLOv8n (exported to TFLite)	320x320 input, INT8 quantized
Data Persistence	DataStore Preferences	1.2.1 - saves passport progress
Audio	Android TextToSpeech API	Built-in Android SDK
Build System	Gradle (AGP 9.1.0)	Kotlin DSL (.kts)
Min SDK	Android 8.0 (API 26)	Target SDK 36

 
3. Project File Structure
Every file in the project and what it does:

app/src/main/java/com/example/fruitrek/
Root
•	MainActivity.kt — App entry point. Sets up Jetpack Compose, handles navigation graph. Routes: landing → permission → camera → passport.

data/model/
•	Fruit.kt — Data class for a single fruit. Contains the FruitCatalog object with all 8 fruits hardcoded (English name, Filipino name, Baybayin script, emoji, fun fact, color).
•	DetectionResult.kt — Data class representing one ML detection: label, confidence score (0-1), and normalized bounding box (RectF with values 0.0 to 1.0).

data/repository/
•	PassportRepository.kt — Saves and reads the set of unlocked fruit IDs using Android DataStore. Data persists across app restarts. Call unlockFruit(id) to unlock a fruit. Use the unlockedFruits Flow to observe changes.

ml/
•	FruitDetector.kt — Core ML inference class. Loads fruitrek_model.tflite from the assets folder. Resizes camera frames to 320x320, runs TFLite inference, parses the raw YOLO output tensor, applies Non-Maximum Suppression (NMS), and returns a list of DetectionResult objects. Uses Java reflection to load the TFLite Interpreter to avoid build-time dependency issues.

ui/
•	FruiTrekViewModel.kt — The brain of the app. Connects FruitDetector, PassportRepository, and TextToSpeech. Implements sustained detection (a fruit must appear in 8 consecutive frames before being confirmed). Triggers TTS announcement and celebration overlay on new fruit discovery.

ui/theme/
•	Color.kt — Brand colors: TropicalGreen, MangoOrange, SunshineYellow, etc.
•	Type.kt — Typography scale.
•	Theme.kt — Material3 theme applying FruiTrek color scheme.

ui/components/
•	DetectionOverlay.kt — Transparent Canvas drawn on top of the camera preview. Draws child-friendly bounding boxes with corner brackets and a label pill showing fruit name + confidence percentage.
•	CelebrationOverlay.kt — Full-screen popup shown when a new fruit is discovered. Features a bouncing emoji animation, fruit name in large text, Filipino name, Baybayin script, fun fact, and a passport confirmation message.

ui/screens/
•	LandingScreen.kt — First screen the user sees. Shows the FruiTrek logo, bouncing fruit emojis, app description, team credits, school name, and a large Start button.
•	PermissionScreen.kt — Shown if camera permission has not been granted. Has a large Let's Go button that triggers the Android permission dialog.
•	CameraScreen.kt — Main scanner screen. Runs the CameraX preview, overlays DetectionOverlay, shows a HUD with fruit count and passport button, and displays scanning status at the bottom.
•	PassportScreen.kt — Fruit Passport collection screen. Shows a 2-column grid of all 8 fruits. Unlocked fruits show their emoji, name, Filipino name, and Baybayin script. Locked fruits show question marks. Tap an unlocked fruit to see its full detail sheet with fun fact.

app/src/main/assets/
•	fruitrek_model.tflite — The trained TFLite object detection model. YOU MUST ADD THIS FILE. See Section 5 (Model Training) for how to create it.
•	labels.txt — One fruit class name per line, in the same order used during training: mango, banana, pineapple, papaya, coconut, jackfruit, guava, starfruit.

 
4. App Flow & How It Works
Navigation Flow
Launch App  →  Landing Screen  →  [tap Start]
If camera permission not granted  →  Permission Screen  →  [grant permission]  →  Camera Screen
If camera permission already granted  →  Camera Screen directly
From Camera Screen  →  [tap Passport icon]  →  Passport Screen  →  [tap Back]  →  Camera Screen

Detection Pipeline
Every camera frame goes through this pipeline:

#	Step	What Happens
1	CameraX Frame	ImageAnalysis captures a frame as a Bitmap on a background thread
2	Resize	Bitmap scaled to 320x320 pixels (model input size)
3	Normalize	RGB pixel values divided by 255 to get 0.0-1.0 floats, packed into a ByteBuffer
4	TFLite Inference	Model runs on ByteBuffer, outputs raw tensor [1, 4+classes, 2100]
5	Parse YOLO Output	Each of 2100 anchors: extract cx,cy,w,h + class scores. Keep detections above 0.45 confidence
6	NMS	Non-Maximum Suppression removes duplicate boxes (IoU threshold 0.45)
7	Draw Boxes	DetectionOverlay draws bounding boxes on screen using normalized coords x image size
8	Sustained Check	ViewModel counts consecutive frames. At 8 frames (approx 0.8s), fruit is confirmed
9	Unlock & Announce	Passport saved, TTS speaks the fruit name, celebration overlay shown for new discoveries

 
5. Training the TFLite Model (Required!)
The app will show "Loading fruit detector..." indefinitely until you add fruitrek_model.tflite to the assets folder. This section explains exactly how to create it for free using Google Colab.

Step 1 — Collect Images
You need 80-150 photos per fruit class (8 classes = ~640-1200 images total).
•	Take your own photos — best results. Different angles, lighting, backgrounds, distances.
•	Roboflow Universe — https://universe.roboflow.com — search for individual fruit names
•	Kaggle — https://www.kaggle.com/datasets — search "tropical fruit detection"

Tips for better model accuracy: shoot on different backgrounds (table, grass, hand-held), mix lighting (indoor/outdoor), take close-ups AND far shots, include ripe and unripe fruits.

Step 2 — Label Your Images
Use Roboflow (free) to draw bounding boxes:
1.	Go to roboflow.com and create a free account
2.	Create new project → select Object Detection
3.	Upload all your images
4.	For each image: draw a box around each fruit, type its label (e.g. "mango", "banana")
5.	Generate dataset: Train/Valid/Test = 70/20/10, enable Flip + Rotation + Brightness augmentations
6.	Export → Format: YOLOv8 → Download ZIP

Step 3 — Train on Google Colab (Free GPU)
Open https://colab.research.google.com and create a new notebook. Run these cells:

# Cell 1 — Install
!pip install ultralytics roboflow

# Cell 2 — Download dataset from Roboflow
from roboflow import Roboflow
rf = Roboflow(api_key="YOUR_API_KEY")
project = rf.workspace("YOUR_WORKSPACE").project("YOUR_PROJECT")
dataset = project.version(1).download("yolov8")

# Cell 3 — Train YOLOv8n (nano = fastest for mobile)
from ultralytics import YOLO
model = YOLO('yolov8n.pt')
model.train(data=f'{dataset.location}/data.yaml', epochs=60, imgsz=320, batch=16)

# Cell 4 — Export to TFLite
model.export(format='tflite', imgsz=320, int8=True)

# Cell 5 — Download the model file
from google.colab import files
files.download('runs/detect/train/weights/best_saved_model/best_float16.tflite')

Rename the downloaded file to fruitrek_model.tflite.
Training takes about 20-35 minutes on Colab's free GPU.

Step 4 — Add Files to Android Project
In Android Studio:
7.	Right-click app → New → Folder → Assets Folder → Finish
8.	Right-click the new assets/ folder → Show in Explorer/Finder
9.	Copy fruitrek_model.tflite and labels.txt into that folder
10.	Rebuild the app

 
6. Android Studio Setup Guide
Prerequisites
•	Android Studio Meerkat (2024.3) or newer
•	JDK 11 (bundled with Android Studio)
•	Android phone with USB debugging enabled OR Android emulator (API 26+)
•	Internet connection for first Gradle sync (~500 MB download)

Opening the Project
11.	Open Android Studio → click Open (not New Project)
12.	Navigate to the FruiTrek project folder → click OK
13.	Wait for Gradle sync to complete (3-5 minutes first time)
14.	Add fruitrek_model.tflite to app/src/main/assets/ (see Section 5)
15.	Connect your Android phone via USB (enable USB debugging first)
16.	Press the green Run button (Shift+F10)

Enabling USB Debugging on your phone
17.	Settings → About Phone → tap Build Number 7 times
18.	Settings → Developer Options → enable USB Debugging
19.	Connect via USB → accept Allow USB debugging? dialog on phone

Gradle Key Settings (build.gradle.kts)
These are the important settings in app/build.gradle.kts:
•	plugins — com.android.application + org.jetbrains.kotlin.plugin.compose only (NOT kotlin.android — causes duplicate extension error with AGP 9.x)
•	kotlin { jvmToolchain(11) } — must be OUTSIDE the android { } block
•	androidResources { noCompress += "tflite" } — prevents the model file from being compressed (CRITICAL — without this TFLite cannot load the model)
•	packaging { jniLibs { pickFirsts += "**/*.so" } } — prevents duplicate .so library conflicts

 
7. How to Add More Fruits
The app currently supports 8 fruits. Adding more is straightforward:

20.	Open Fruit.kt and add a new Fruit(...) entry to FruitCatalog.all
21.	Add the fruit's English name (lowercase) as a new line in assets/labels.txt
22.	Collect images of the new fruit, label them in Roboflow, retrain the model
23.	Replace fruitrek_model.tflite with the new model and rebuild

Suggested fruits to add next: Durian (Durian), Rambutan (Rambutan), Lanzones (Lanzones), Atis (Sugar Apple), Santol (Santol), Calamansi (Calamansi)

 
8. Troubleshooting
Error / Symptom	Fix
"Loading fruit detector..." stuck	fruitrek_model.tflite is missing from app/src/main/assets/. Also check labels.txt is there.
Cannot add extension 'kotlin'	Remove id("org.jetbrains.kotlin.android") from plugins block — only keep kotlin.plugin.compose
Unresolved reference 'camera'	Gradle sync didn't complete. File → Sync Project with Gradle Files. Check internet connection.
Duplicate namespace TFLite error	Add packaging { jniLibs { pickFirsts += "**/*.so" } } to android block in build.gradle.kts
Camera is black / no preview	Go to phone Settings → Apps → FruiTrek → Permissions → enable Camera
Gradle sync failed	File → Invalidate Caches → Invalidate and Restart. Check internet. Re-sync after restart.
TTS not speaking	Check device volume. TTS may not work on emulators. Test on real device.
Low detection accuracy	Collect more images per fruit class (target 150+). Enable more augmentations in Roboflow.

 
9. Academic & Technical Merit (CS1240 Alignment)
This project satisfies the CS1240 Graphics and Visual Computing requirements by demonstrating the following competencies:

CS1240 Competency	How FruiTrek Demonstrates It
Image acquisition & preprocessing	CameraX live frame capture → Bitmap conversion → 320x320 resize → RGB normalization
Feature extraction	YOLOv8n neural network extracts spatial features for fruit classification and localization
Object detection & localization	Bounding box prediction with cx,cy,w,h coordinates + Non-Maximum Suppression
Real-time visual feedback	DetectionOverlay draws child-friendly boxes at live camera frame rate
Deep learning deployment	Custom-trained TFLite model with INT8 quantization running entirely on-device (Edge AI)
Transition from classical CV	Extends prior OpenCV work (thresholding, contours, calibration) into modern DL inference
User-centric design	Child-safe UI: large targets, audio feedback, simple navigation, accessibility-first design
Cultural computing	Filipino language integration + Baybayin Unicode script for cultural literacy

10. Pre-Demo Checklist
Use this before your presentation:

•	[ ]  fruitrek_model.tflite added to app/src/main/assets/
•	[ ]  labels.txt present in app/src/main/assets/ (8 lines: mango, banana, etc.)
•	[ ]  App builds without errors (BUILD SUCCESSFUL)
•	[ ]  Camera permission granted on test device
•	[ ]  Device volume is on (for TTS audio)
•	[ ]  Test all 8 fruits detected correctly
•	[ ]  Passport unlocks and persists after app restart
•	[ ]  Landing screen shows team names correctly
•	[ ]  Baybayin script renders correctly on target device
•	[ ]  App does not crash on sustained detection

FruiTrek  |  Mapua Malayan College Laguna  |  CS1240 CIS302  |  March 2026
