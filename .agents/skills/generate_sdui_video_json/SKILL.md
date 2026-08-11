---
name: generate_sdui_video_json
description: Generates valid SDUI JSON for the video rendering engine based on MotionView, MotionEffect, and MotionTransition.
---

# Generate SDUI Video JSON

This skill provides a comprehensive reference for generating valid SDUI JSON for the video rendering engine. The engine uses a frame-based timeline where all timings are defined in frames relative to the global FPS.

## 1. Root Structure

The root object defines the entire video project.

```json
{
  "config": {
    "aspectRatio": { "width": 1080, "height": 1920 },
    "fps": 30,
    "outputQuality": 100
  },
  "audios": [],
  "plugins": [],
  "views": [],
  "sequence": []
}
```

- **`config`**: (Optional) Global settings.
- **`audios`**: (Optional) List of background audio tracks.
- **`plugins`**: (Optional) Global rendering plugins (e.g., `SubjectSegmentationPlugin`).
- **`views`**: (Optional) Parallel layers of views. Higher index = higher Z-order (rendered on top).
- **`sequence`**: (Optional) Ordered list of `MotionView` and `MotionTransition` objects. If present, the engine automatically calculates `startFrame`/`endFrame` for items in the sequence based on their duration and the preceding transitions.

---

## 2. MotionAsset

Assets are polymorphic resources used by views and audio components.

| Type | URI Protocol | Additional Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- | :--- |
| `SimpleMotionAsset` | `http(s)://`, `file://`, `content://` | - | `{"type": "SimpleMotionAsset", "uri": "https://server.com/audio.mp3", "metadata": {"key": "value"}}` |
| `ImageAsset` | `http(s)://`, `file://` | - | `{"type": "ImageAsset", "uri": "https://server.com/image.png", "metadata": {"width": 1080}}` |
| `VideoAsset` | `http(s)://`, `file://` | - | `{"type": "VideoAsset", "uri": "https://server.com/video.mp4", "metadata": {"duration_ms": 5000}}` |
| `FontAsset` | `http(s)://`, `file://` | `fontName` (String) | `{"type": "FontAsset", "uri": "file:///android_asset/myfont.ttf", "fontName": "CustomFont", "metadata": {}}` |
| `TTSAudioAsset` | `content://tts_<hash>` | `metadata` containing `text` (String) | `{"type": "TTSAudioAsset", "metadata": {"text": "This is a voiceover text", "engine": "google", "voice": "en-us-x-sfg#male_1-local"}}` |

---

## 3. MotionView

### Available MotionView Types

#### Text Views (Modern Typography)
All text views support `textSizeVariant` (H1-H6, P) and `fontAsset`.

| Type | Specific Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `TransparentTextView` | `text`, `textColor` (Hex) | `{"type": "TransparentTextView", "text": "Hello World", "textColor": "#FF0000", "highlightColor": "#FFFF00", "textSizeVariant": "H1", "fontAsset": {"type": "FontAsset", "uri": "...", "fontName": "Bold"}}` |
| `TypeWriterTextView` | `text`, `writingSpeed` (Float), `unwrittenTextAlpha` (Float 0-1), `cursorChar` (String), `blinkFrameRate` (Int) | `{"type": "TypeWriterTextView", "text": "Typing...", "writingSpeed": 0.1, "unwrittenTextAlpha": 0.2, "cursorChar": "_", "blinkFrameRate": 15, "textColor": "#FFFFFF"}` |
| `WordWriterTextView` | `text`, `writingSpeed`, `textColor`, `highlightColor` | `{"type": "WordWriterTextView", "text": "Dynamic word reveal", "writingSpeed": 0.5, "unwrittenTextAlpha": 0.0, "textColor": "#FFFFFF", "highlightColor": "#FF5722"}` |
| `WordBlinkTextView` | `text`, `writingSpeed`, `textColor`, `highlightColor` | `{"type": "WordBlinkTextView", "text": "Blinking words", "writingSpeed": 0.8, "textColor": "#00FF00", "highlightColor": "#FFFFFF"}` |
| `PopUpTextView` | `text`, `writingSpeed`, `maxTranslationY` (Float) | `{"type": "PopUpTextView", "text": "Jump!", "writingSpeed": 0.4, "maxTranslationY": 100, "unwrittenTextAlpha": 0.1, "textColor": "#BBDEFB"}` |
| `RainbowPopUpTextView` | Same as `PopUpTextView` with multi-color animation. | `{"type": "RainbowPopUpTextView", "text": "Rainbow", "writingSpeed": 0.3, "maxTranslationY": 50, "textColor": "#FFFFFF"}` |
| `AccentMiddlePopUpTextView` | Same as `PopUpTextView` + `accentColor` (Hex Color). | `{"type": "AccentMiddlePopUpTextView", "text": "Accent View", "accentColor": "#FFFF00", "writingSpeed": 0.5, "maxTranslationY": 80}` |

#### Media & Backgrounds
| Type | Specific Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `MotionImageView` | `asset` (Required) | `{"type": "MotionImageView", "asset": {"type": "ImageAsset", "uri": "https://picsum.photos/500"}}` |
| `CircularMotionImageView` | `asset` (Required) | `{"type": "CircularMotionImageView", "asset": {"type": "ImageAsset", "uri": "file:///avatar.jpg"}}` |
| `VideoFrameView` | `asset` (Required) | `{"type": "VideoFrameView", "asset": {"type": "VideoAsset", "uri": "content://media/123"}}` |
| `CoilVideoPlayer` | `asset`, `plugins` (Array of Coil Plugins) | `{"type": "CoilVideoPlayer", "asset": {"type": "VideoAsset", "uri": "..."}, "plugins": [{"type": "CoilBlurPlugin", "radius": 15}]}` |
| `GradientView` | `orientation` (`HORIZONTAL`, `VERTICAL`, `CIRCULAR`), `colors` (Array of Hex) | `{"type": "GradientView", "orientation": "CIRCULAR", "colors": ["#FF0000", "#00FF00", "#0000FF"]}` |
| `TranslucentMotionView` | `color` (Hex), `alpha` (Float 0-1) | `{"type": "TranslucentMotionView", "color": "#80000000", "alpha": 0.7, "layout": {"width": "match_parent", "height": 300}}` |

#### Advanced Components
| Type | Specific Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `CircularAudioWaveformView`| `amplitudes` (Array of Float 0.0-1.0) | `{"type": "CircularAudioWaveformView", "amplitudes": [0.1, 0.4, 0.9, 0.2, 0.6], "startFrame": 0, "endFrame": 300}` |
| `RadialAudioWaveformView`  | `amplitudes` (Array of Float 0.0-1.0) | `{"type": "RadialAudioWaveformView", "amplitudes": [0.5, 0.5, 0.5], "startFrame": 0, "endFrame": 150}` |
| `MultiLyricsContainer`     | `songName` (String), `asset` (LRC/JSON File) | `{"type": "MultiLyricsContainer", "songName": "Midnight City", "asset": {"type": "SimpleMotionAsset", "uri": "https://lyrics.com/song.lrc"}}` |

---

## 4. MotionEffect

Effects are applied to views. **All effects MANDATORILY require `startFrame` and `endFrame`.**
- In the `views` array, these are absolute project frames.
- In the `sequence` array, these are relative to the start of the parent view (0 = start of view).

### Common MotionEffect Types
| Type | Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `FadeInEffect` | - | `{"type": "FadeInEffect", "startFrame": 0, "endFrame": 15}` |
| `FadeOutEffect` | - | `{"type": "FadeOutEffect", "startFrame": 285, "endFrame": 300}` |
| `ZoomInEffect` | `startScale`, `endScale` | `{"type": "ZoomInEffect", "startFrame": 0, "endFrame": 60, "startScale": 1.0, "endScale": 1.5}` |
| `ZoomOutEffect` | `startScale`, `endScale` | `{"type": "ZoomOutEffect", "startFrame": 0, "endFrame": 60, "startScale": 1.5, "endScale": 1.0}` |
| `BlurEffect` | `maxBlurRadius` | `{"type": "BlurEffect", "startFrame": 0, "endFrame": 30, "maxBlurRadius": 50.0}` |
| `GlitchEffect` | `intensity` | `{"type": "GlitchEffect", "startFrame": 10, "endFrame": 50, "intensity": 25.0}` |
| `VibrateEffect` | `amplitude`, `frequency` | `{"type": "VibrateEffect", "startFrame": 0, "endFrame": 100, "amplitude": 10.0, "frequency": 5.0}` |
| `VintageEffect` | `fromIntensity`, `toIntensity` | `{"type": "VintageEffect", "startFrame": 0, "endFrame": 300, "fromIntensity": 0.0, "toIntensity": 1.0}` |
| `SlideEffect` | `fromX`, `toX`, `fromY`, `toY` | `{"type": "SlideEffect", "startFrame": 0, "endFrame": 45, "fromX": -1.0, "toX": 0.0, "fromY": 0.5, "toY": 0.5}` |
| `SlideRightToLeftEffect` | - | `{"type": "SlideRightToLeftEffect", "startFrame": 0, "endFrame": 30}` |
| `SlideLeftToRightEffect` | - | `{"type": "SlideLeftToRightEffect", "startFrame": 0, "endFrame": 30}` |
| `SlideTopToBottomEffect` | - | `{"type": "SlideTopToBottomEffect", "startFrame": 0, "endFrame": 30}` |
| `SlideBottomToTopEffect` | - | `{"type": "SlideBottomToTopEffect", "startFrame": 0, "endFrame": 30}` |
| `SubjectSegmentationEffect` | No params | `{"type": "SubjectSegmentationEffect", "startFrame": 0, "endFrame": 300}` |

### Specialized Effects (Coil/Media)
| Type | Description | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `CoilBlurEffect` | `radius`, `sampling` | `{"type": "CoilBlurEffect", "startFrame": 0, "endFrame": 30, "radius": 20.0, "sampling": 2.0}` |
| `CoilSwirlEffect` | `radius`, `angle`, `centerX`, `centerY` | `{"type": "CoilSwirlEffect", "startFrame": 0, "endFrame": 60, "radius": 0.7, "angle": 2.0, "centerX": 0.5, "centerY": 0.5}` |
| `CoilRoundedCornersEffect` | `radius` | `{"type": "CoilRoundedCornersEffect", "startFrame": 0, "endFrame": 30, "radius": 50.0}` |
| `CoilColorFilterEffect` | `color` (Hex Color) | `{"type": "CoilColorFilterEffect", "startFrame": 0, "endFrame": 30, "color": "#00FF00"}` |
| `CoilGrayscaleEffect` | - | `{"type": "CoilGrayscaleEffect", "startFrame": 0, "endFrame": 30}` |
| `CoilBrightnessEffect` | `brightness` | `{"type": "CoilBrightnessEffect", "startFrame": 0, "endFrame": 30, "brightness": 0.3}` |
| `CoilContrastEffect` | `contrast` | `{"type": "CoilContrastEffect", "startFrame": 0, "endFrame": 30, "contrast": 1.2}` |
| `CoilKuwaharaEffect` | `radius` (Int) | `{"type": "CoilKuwaharaEffect", "startFrame": 0, "endFrame": 30, "radius": 25}` |
| `CoilPixelationEffect` | `pixel` | `{"type": "CoilPixelationEffect", "startFrame": 0, "endFrame": 30, "pixel": 15.0}` |
| `CoilToonEffect` | `threshold`, `quantizationLevels` | `{"type": "CoilToonEffect", "startFrame": 0, "endFrame": 60, "threshold": 0.1, "quantizationLevels": 8.0}` |
| `CoilVignetteEffect` | `centerX`, `centerY`, `start`, `end`, `color` | `{"type": "CoilVignetteEffect", "startFrame": 0, "endFrame": 60, "centerX": 0.5, "centerY": 0.5, "start": 0.0, "end": 0.8, "color": [0.0, 0.0, 0.0]}` |
| `CoilCenterOnFaceEffect` | `zoom` (Int) | `{"type": "CoilCenterOnFaceEffect", "startFrame": 0, "endFrame": 150, "zoom": 120}` |

---

## 5. MotionTransition

Transitions define how one view morphs into the next in a `sequence`. They are specialized "clubbed effects" that automatically calculate the overlap between adjacent views.

### Base Properties
- **`type`**: (Required) The `simpleName` of the Kotlin class.
- **`duration`**: (Required) The overlap duration in frames.

### Available MotionTransition Types
| Type | Parameters | JSON Sample (Detailed) |
| :--- | :--- | :--- |
| `CrossFadeTransition` | - | `{"type": "CrossFadeTransition", "duration": 45}` |
| `BlurTransition` | `maxBlurRadius` | `{"type": "BlurTransition", "duration": 30, "maxBlurRadius": 40.0}` |
| `SlideTransition` | `direction` | `{"type": "SlideTransition", "duration": 60, "direction": "RIGHT_TO_LEFT"}` |

---

## 6. Layout Reference

### MotionLayoutInfo
- **`width` / `height`**: `"match_parent"`, `"wrap_content"`, or Integer (Pixels).
- **`gravity`**: Pipe-separated string: `top`, `bottom`, `left`, `right`, `start`, `end`, `center`, `center_horizontal`, `center_vertical`.
- **`padding` / `margin`**: `{ "left": Int, "top": Int, "right": Int, "bottom": Int }`.

### Enums
- **`textSizeVariant`**: `H1`, `H2`, `H3`, `H4`, `H5`, `H6`, `P`.
- **`orientation`**: `HORIZONTAL`, `VERTICAL`, `CIRCULAR`.
- **`cropType`**: `CENTER`, `TOP`, `BOTTOM`, `FIT`.

---

## 7. Example Recipes

### Recipe 1: Reels Style Video with Typewriter Captions
```json
{
  "config": { "aspectRatio": { "width": 1080, "height": 1920 }, "fps": 30 },
  "audios": [
    {
      "type": "MotionAudio",
      "asset": { "type": "SimpleMotionAsset", "uri": "https://server.com/bgm.mp3" },
      "startFrame": 0, "endFrame": 300
    }
  ],
  "views": [
    {
      "type": "VideoFrameView",
      "startFrame": 0, "endFrame": 300,
      "layout": { "width": "match_parent", "height": "match_parent" },
      "asset": { "type": "VideoAsset", "uri": "https://server.com/video.mp4" }
    },
    {
      "type": "TypeWriterTextView",
      "startFrame": 30, "endFrame": 270,
      "text": "The Future of Video is SDUI",
      "writingSpeed": 0.5,
      "textSizeVariant": "H2",
      "textColor": "#FFFFFF",
      "layout": { "gravity": "center" },
      "effects": [
        { "type": "FadeInEffect", "startFrame": 30, "endFrame": 45 },
        { "type": "VibrateEffect", "startFrame": 30, "endFrame": 270, "amplitude": 5, "frequency": 2 }
      ]
    }
  ]
}
```

### Recipe 2: Sequence with Transitions
```json
{
  "sequence": [
    {
      "type": "MotionImageView",
      "duration": 60,
      "asset": { "type": "ImageAsset", "uri": "file:///img1.jpg" }
    },
    { "type": "CrossFadeTransition", "duration": 30 },
    {
      "type": "MotionImageView",
      "duration": 60,
      "asset": { "type": "ImageAsset", "uri": "file:///img2.jpg" }
    }
  ]
}
```

### Recipe 3: Automated TTS Voiceover
```json
{
  "config": { "aspectRatio": { "width": 1080, "height": 1080 }, "fps": 30 },
  "audios": [
    {
      "type": "MotionAudio",
      "asset": {
        "type": "TTSAudioAsset",
        "metadata": { "text": "This audio was generated using the Android TTS engine." }
      },
      "startFrame": 0, "endFrame": 150
    }
  ],
  "views": [
    {
      "type": "MotionImageView",
      "startFrame": 0, "endFrame": 150,
      "asset": { "type": "ImageAsset", "uri": "https://picsum.photos/1080" }
    }
  ]
}
```
