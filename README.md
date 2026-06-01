# AndroidVideoMotion

AndroidVideoMotion is a modular toolkit for generating **frame-accurate motion videos on Android** using native Views, transitions, effects, audio tracks, and optional rendering extensions (OpenGL, Filament, SDUI, ML, and more).

[![](https://jitpack.io/v/tejpratap46/AndroidVideoMotion.svg)](https://jitpack.io/#tejpratap46/AndroidVideoMotion) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/tejpratap46/AndroidVideoMotion)

---

## What this project provides

- Build a video timeline from MotionView-based scenes.
- Compose transitions between scenes (cross-fade, slide, blur, etc.).
- Render frame-by-frame into video output via a producer adapter.
- Add synced audio over frame ranges.
- Extend rendering pipelines with dedicated modules (OpenGL, Filament, PyTorch, SDUI, etc.).

---

## Core architecture

At the heart of the project is a composition pipeline where views are scheduled by frame ranges and rendered into video output.

```text
Motion Views + Effects + Audio
           │
           ▼
   MotionVideoProducer
   (sequence + transitions)
           │
           ▼
    MotionComposerView
 (timeline composition root)
           │
           ▼
   VideoProducerAdapter
(AndroidVideoProducerAdapter by default)
           │
           ▼
       Output video file
```

### Main responsibilities

- **`MotionVideoProducer`**
    - Builder/facade for timeline authoring.
    - Tracks `totalFrames`, applies pending transitions, keeps motion audio list.
    - Delegates actual rendering/export to `VideoProducerAdapter`.

- **`MotionComposerView`**
    - Root container that aggregates all scheduled `MotionView` instances.
    - Receives layout and frame-window metadata from each view.

- **`VideoProducerAdapter`**
    - Rendering backend abstraction.
    - Default implementation is `AndroidVideoProducerAdapter`.

- **Motion Views / Transitions / Effects**
    - Views define what appears between `startFrame` and `endFrame`.
    - Transitions stitch adjacent views.
    - Effects provide visual modifiers (fade, slide, zoom, blur, etc.).

---

## Project structure

```text
AndroidVideoMotion/
├── modules/
│   ├── motionlib/                 # Core motion engine and UI components
│   │   ├── core/motion/           # Motion timeline, composer, producer, transitions
│   │   ├── core/adapter/          # Video producer adapters
│   │   ├── core/animation/        # Easings, springs, interpolators
│   │   └── ui/custom/             # Motion-aware custom views (text, image, audio, video)
│   ├── app/                       # Main sample app using motionlib
│   ├── ivi-demo/                  # IVI-specific demo compositions
│   ├── sdui/                      # JSON-driven Motion/Generic SDUI rendering
│   ├── 3d-opengl-renderer/        # OpenGL offscreen rendering integration
│   ├── 3d-filament-renderer/      # Filament-based 3D rendering integration
│   ├── pytorch-motion-ext/        # PyTorch-based image/video ML extensions
│   ├── tensorflow-motion-ext/     # TensorFlow extension module
│   ├── ffmpeg-motion-ext/         # FFmpeg-based processing extensions
│   ├── jcodec-motion-ext/         # JCodec-based processing extensions
│   ├── motion-video-player/       # Playback-focused module
│   └── ...                        # Additional feature modules
├── build.gradle.kts
├── settings.gradle
└── README.md
```

---

## Quick start

### 1) Add JitPack repository

```gradle
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
  }
}
```

### 2) Add dependency

```gradle
dependencies {
  implementation 'com.github.tejpratap46.AndroidVideoMotion:core:TAG'
}
```

> Replace `TAG` with a release/tag from JitPack.

---

## Code snippets (project overview)

### Build a motion timeline

```kotlin
val motionProducer = MotionVideoProducer
    .with(context = applicationContext, motionAudio = motionAudio)
    .addMotionViewToSequence(firstMotionView)
    .addTransition(CrossFadeTransition(), duration = 30)
    .addMotionViewToSequence(secondMotionView)
```

### Export the composed video

```kotlin
val file = motionProducer.produceVideo(
    context = applicationContext,
    outputFile = File(cacheDir, "output.mp4"),
) { progress, frameBitmap ->
    // Update progress UI / preview
}
```

### Typical config setup

```kotlin
val motionConfig = MotionConfig(
    aspectRatio = VideoAspectRatio.Ratio9x16_480,
    fps = 30,
)
setCurrentConfig(motionConfig)
```

---

## Where to look next

- **Core implementation:** `modules/motionlib/src/main/java/.../core/motion`
- **UI motion views/effects:** `modules/motionlib/src/main/java/.../ui`
- **Sample composition:** `modules/app/src/main/java/.../presentation/SampleMotionVideo.kt`
- **JSON-driven rendering:** `modules/sdui`

If you’re new to the repo, start with `motionlib` and then open `app` or `ivi-demo` to see end-to-end usage.
