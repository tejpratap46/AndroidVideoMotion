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

### 2) Add the modules you need

Every library module is consumable as a separate JitPack artifact, so you can keep your app dependency graph as small as the feature set you use. Replace `TAG` with a release/tag from JitPack.

```gradle
dependencies {
  // Shared primitives: config, MotionView contracts, layout metadata, audio, effects.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:core:TAG'

  // Main Android View-based motion composer, custom views, transitions, and export flow.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:motionlib:TAG'

  // Ready-made composition templates and SDUI-backed template helpers.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:templates:TAG'

  // JSON-driven motion and generic server-driven UI rendering.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:sdui:TAG'

  // Persist, catalog, or retrieve reusable motion definitions/assets.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:motion-store:TAG'

  // Extract media metadata for timeline and asset decisions.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:metadata-extractor:TAG'

  // Playback helpers for generated motion videos.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:motion-video-player:TAG'

  // Video processing/export extension backed by FFmpeg.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:ffmpeg-motion-ext:TAG'

  // Video processing/export extension backed by JCodec.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:jcodec-motion-ext:TAG'

  // Offscreen 3D rendering integrations.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:3d-opengl-renderer:TAG'
  implementation 'com.github.tejpratap46.AndroidVideoMotion:3d-filament-renderer:TAG'

  // ML-assisted motion/image/video extensions.
  implementation 'com.github.tejpratap46.AndroidVideoMotion:tensorflow-motion-ext:TAG'
  implementation 'com.github.tejpratap46.AndroidVideoMotion:pytorch-motion-ext:TAG'
}
```

> Demo/application modules such as `app`, `ivi-demo`, and `lyrics-maker` are intended as examples and entry points rather than reusable library dependencies.

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

### Create and customize a motion view

Custom motion views usually extend one of the base motion containers, set a frame window, optionally expose `layoutInfo`, and update their child views inside `forFrame(frame)`. Always call `super.forFrame(frame)` so visibility, nested `MotionView` children, and attached effects continue to work.

```kotlin
class CaptionMotionView(
    context: Context,
    private val caption: String,
    private val fromFrame: Int,
    private val toFrame: Int,
) : BaseLinearMotionView(context) {

    private val captionView = AppCompatTextView(context).apply {
        text = caption
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
    }

    override var layoutInfo = MotionLayoutInfo(
        width = MotionLayoutInfo.MATCH_PARENT,
        height = MotionLayoutInfo.WRAP_CONTENT,
        margin = MotionLayoutInfo.Margin(bottom = 48),
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
    )

    init {
        startFrame = fromFrame
        endFrame = toFrame
        addView(captionView)
        addEffect(FadeInEffect(startFrame = fromFrame, endFrame = fromFrame + 12))
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val scale = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.CUBIC_OUT),
            currentFrame = frame.coerceIn(startFrame, endFrame),
            frameRange = startFrame to endFrame,
            valueRange = 0.92f to 1.08f,
        )

        captionView.scaleX = scale
        captionView.scaleY = scale
        return this
    }
}
```

Use it in the producer exactly like a built-in view:

```kotlin
val caption = CaptionMotionView(
    context = applicationContext,
    caption = "Generated with AndroidVideoMotion",
    fromFrame = 1,
    toFrame = motionConfig.fps * 3,
)

val motionProducer = MotionVideoProducer
    .with(context = applicationContext)
    .addMotionViewToSequence(caption)
```

---

## Where to look next

- **Core implementation:** `modules/motionlib/src/main/java/.../core/motion`
- **UI motion views/effects:** `modules/motionlib/src/main/java/.../ui`
- **Sample composition:** `modules/app/src/main/java/.../presentation/SampleMotionVideo.kt`
- **JSON-driven rendering:** `modules/sdui`

If you’re new to the repo, start with `motionlib` and then open `app` or `ivi-demo` to see end-to-end usage.
