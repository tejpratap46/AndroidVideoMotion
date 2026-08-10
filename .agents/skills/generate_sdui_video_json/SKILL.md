---
name: generate_sdui_video_json
description: Generates valid SDUI JSON for the video rendering engine based on MotionView, MotionEffect, and MotionTransition.
---

# Generate SDUI Video JSON

This skill provides the structure and parameters required to generate valid SDUI JSON that can be parsed and rendered by `VideoProducerAdapter`.

## Root Structure

The root JSON object defines the video project.

```json
{
  "config": {
    "aspectRatio": {
      "width": 16,
      "height": 9
    },
    "fps": 24,
    "outputQuality": 100
  },
  "audios": [],
  "plugins": [],
  "views": [],
  "sequence": []
}
```

- `config`: (Optional) Global configuration.
- `audios`: (Optional) List of `MotionAudio` objects.
- `plugins`: (Optional) List of `MotionPlugin` objects.
- `views`: (Optional) List of `MotionView` objects.
- `sequence`: (Optional) Ordered list of `MotionView` and `MotionTransition` objects. If present, it defines the timeline sequence.

---

## 1. MotionAsset

Assets are used by views and audio components to represent media or resources. They are polymorphic objects.

- `type`: (Required) String name of the asset type.
- `uri`: (Required) String URI of the resource.
- `metadata`: (Optional) Object containing custom implementation-specific metadata.

### Available MotionAsset Types

| Type | Additional Parameters |
| :--- | :--- |
| `SimpleMotionAsset` | - |
| `ImageAsset` | - |
| `VideoAsset` | - |
| `FontAsset` | `fontName` (Optional String) |

Example `FontAsset` with metadata:
```json
{
  "type": "FontAsset",
  "uri": "https://example.com/font.ttf",
  "fontName": "CustomFont",
  "metadata": {
    "weight": "bold",
    "isItalic": false
  }
}
```

---

## 2. MotionView

All `MotionView` objects share these base properties:

- `type`: (Required) String name of the view type.
- `startFrame`: (Optional) Default `0`.
- `endFrame`: (Optional) Default `0`.
- `loop`: (Optional) Object `{ "start": Int, "end": Int }`.
- `layout`: (Optional) Object:
  - `width`: `"match_parent"`, `"wrap_content"`, or Integer value.
  - `height`: `"match_parent"`, `"wrap_content"`, or Integer value.
  - `padding`: `{ "left": Int, "top": Int, "right": Int, "bottom": Int }`.
  - `margin`: `{ "left": Int, "top": Int, "right": Int, "bottom": Int }`.
  - `gravity`: Pipe-separated string (e.g., `"center|top"`, `"left|bottom"`).
- `assets`: (Optional) Array of `MotionAsset` objects. These are the resources required by the view.
- `effects`: (Optional) Array of `MotionEffect` objects.

### Asset Management in MotionView

Each `MotionView` implementation manages its required resources through `MotionAsset` objects. In SDUI, these are typically provided via:
- `asset`: For main media resources (e.g., in `MotionImageView`, `VideoFrameView`).
- `fontAsset`: For custom typography (e.g., in `TransparentTextView`).
- `assets`: A generic array for additional required resources.

The engine uses these to ensure all assets are prepared and cached before rendering.

### Available MotionView Types and Specific Parameters

| Type | Parameters |
| :--- | :--- |
| `TransparentTextView` | `text` (String), `fontAsset` (`MotionAsset`), `textSizeVariant` (`H1`-`H6`, `P`), `textColor` (Hex String) |
| `TypeWriterTextView` | `text`, `fontAsset`, `writingSpeed` (Float), `unwrittenTextAlpha` (Float), `cursorChar` (String, default `\|`), `blinkFrameRate` (Int), `textSizeVariant`, `textColor` |
| `WordWriterTextView` | `text`, `fontAsset`, `writingSpeed`, `unwrittenTextAlpha`, `textSizeVariant`, `textColor`, `highlightColor` (Hex String) |
| `WordBlinkTextView` | `text`, `fontAsset`, `writingSpeed`, `textSizeVariant`, `textColor` |
| `PopUpTextView` | `text`, `fontAsset`, `writingSpeed`, `unwrittenTextAlpha`, `maxTranslationY` (Float), `textSizeVariant`, `textColor`, `highlightColor` |
| `RainbowPopUpTextView` | `text`, `fontAsset`, `writingSpeed`, `unwrittenTextAlpha`, `maxTranslationY`, `textSizeVariant`, `textColor`, `highlightColor` |
| `AccentMiddlePopUpTextView` | `text`, `fontAsset`, `writingSpeed`, `unwrittenTextAlpha`, `maxTranslationY`, `accentColor` (Int Color), `textSizeVariant`, `textColor`, `highlightColor` |
| `CircularMotionImageView` | `asset` (`MotionAsset`) |
| `MotionImageView` | `asset` (`MotionAsset`) |
| `VideoFrameView` | `asset` (`MotionAsset`) |
| `GradientView` | `orientation` (`HORIZONTAL`, `VERTICAL`), `colors` (Array of Hex Strings) |
| `TranslucentMotionView` | `color` (Hex String), `alpha` (Float 0.0-1.0) |
| `CircularAudioWaveformView` | `amplitudes` (Array of Floats) |
| `RadialAudioWaveformView` | `amplitudes` (Array of Floats) |
| `CoilVideoPlayer` | `asset` (`MotionAsset`), `plugins` (Array of Coil Plugins) |
| `MultiLyricsContainer` | `songName` (String), `asset` (`MotionAsset`) |

---

## 3. MotionEffect

All `MotionEffect` objects share these base properties:

- `type`: (Required) String name of the effect type.
- `startFrame`: (Optional) Relative to the parent view.
- `endFrame`: (Optional) Relative to the parent view.

### Available MotionEffect Types and Specific Parameters

| Type | Parameters |
| :--- | :--- |
| `SlideRightToLeftEffect` | - |
| `SlideLeftToRightEffect` | - |
| `SlideTopToBottomEffect` | - |
| `SlideBottomToTopEffect` | - |
| `ZoomInEffect` | `startScale` (Float, default `1.0`), `endScale` (Float, default `2.0`) |
| `ZoomOutEffect` | `startScale` (Float, default `2.0`), `endScale` (Float, default `1.0`) |
| `FadeInEffect` | - |
| `FadeOutEffect` | - |
| `BlurEffect` | `maxBlurRadius` (Float, default `20.0`) |
| `GlitchEffect` | `intensity` (Float, default `10.0`) |
| `VibrateEffect` | `amplitude` (Float), `frequency` (Float) |
| `VintageEffect` | `fromIntensity` (Float), `toIntensity` (Float) |
| `SlideEffect` | `fromX`, `toX`, `fromY`, `toY` (Floats) |
| `CoilBlurEffect` | `radius` (Float), `sampling` (Float) |
| `CoilGrayscaleEffect` | - |
| `CoilColorFilterEffect` | `color` (Int Color) |
| `CoilRoundedCornersEffect` | `radius` (Float) |
| `CoilCropEffect` | `cropType` (`CENTER`, `TOP`, `BOTTOM`, etc.) |
| `CoilMaskEffect` | `maskId` (Int Drawable Resource ID) |
| `CoilBrightnessEffect` | `brightness` (Float -1.0 to 1.0) |
| `CoilContrastEffect` | `contrast` (Float) |
| `CoilInvertEffect` | - |
| `CoilKuwaharaEffect` | `radius` (Int) |
| `CoilPixelationEffect` | `pixel` (Float) |
| `CoilSepiaEffect` | - |
| `CoilSketchEffect` | - |
| `CoilSwirlEffect` | `radius` (Float), `angle` (Float), `centerX` (Float), `centerY` (Float) |
| `CoilToonEffect` | `threshold` (Float), `quantizationLevels` (Float) |
| `CoilVignetteEffect` | `centerX`, `centerY`, `start`, `end` (Floats), `color` (Array of 3 Floats) |
| `CoilCenterOnFaceEffect` | `zoom` (Int percentage) |

---

## 4. MotionTransition

Transitions are used in the `sequence` array between two views.

- `type`: (Required) String name of the transition type.
- `duration`: (Required) Number of frames for the transition.

### Available MotionTransition Types and Specific Parameters

| Type | Parameters |
| :--- | :--- |
| `CrossFadeTransition` | - |
| `BlurTransition` | `maxBlurRadius` (Float) |
| `SlideTransition` | `direction` (`LEFT_TO_RIGHT`, `RIGHT_TO_LEFT`, `TOP_TO_BOTTOM`, `BOTTOM_TO_TOP`) |

---

## Helper Objects

### MotionAudio

Used in the `audios` root array.

- `type`: `"MotionAudio"`
- `asset`: `MotionAsset` (Object)
- `startFrame`: Int
- `endFrame`: Int
- `delayFrame`: Int

### MotionConfig

Used in the `config` root object.

- `aspectRatio`: Object `{ "width": Int, "height": Int }`
- `fps`: Int
- `outputQuality`: Int (0-100)

### MotionLayoutInfo (Layout)

Used in `layout` property of `MotionView`.

- `width`: `"match_parent"`, `"wrap_content"`, or Integer.
- `height`: `"match_parent"`, `"wrap_content"`, or Integer.
- `padding`: `{ "left", "top", "right", "bottom" }` (Int)
- `margin`: `{ "left", "top", "right", "bottom" }` (Int)
- `gravity`: String (e.g., `"center"`, `"top|left"`, `"bottom|right"`)

### Gravity Values

Combined using `|`: `top`, `bottom`, `left`, `right`, `start`, `end`, `center`, `center_horizontal`, `center_vertical`.

### Color Values

Most colors are Hex Strings (e.g., `"#FF0000"`). Some specific parameters (like `accentColor` in `AccentMiddlePopUpTextView`) use raw `Int` color values.
