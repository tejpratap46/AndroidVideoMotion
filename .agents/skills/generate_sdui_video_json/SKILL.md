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
  "views": []
}
```

- **`config`**: (Optional) Global settings.
- **`audios`**: (Optional) List of background audio tracks.
- **`plugins`**: (Optional) Global rendering plugins (e.g., `SubjectSegmentationPlugin`).
- **`views`**: (Optional) Parallel layers of views. Higher index = higher Z-order (rendered on top).

---

## 2. MotionAsset

Assets are polymorphic resources used by views and audio components.

| Type | URI Protocol | Description |
| :--- | :--- | :--- |
| `SimpleMotionAsset` | `http(s)://`, `file://` | Generic file reference. |
| `ImageAsset` | `http(s)://`, `file://` | Specifically for images. |
| `VideoAsset` | `http(s)://`, `file://` | Specifically for video files. |
| `FontAsset` | `http(s)://`, `file://` | Custom web fonts (requires `fontName`). |
| `TTSAudioAsset` | `content://tts_...` | Text-to-Speech audio (metadata contains `text`). |

---

## 3. MotionView

### 3.1 Text Views
All text views support `textSizeVariant` (H1-H6, P) and `fontAsset`.

| Type | Specific Parameters | JSON Sample |
| :--- | :--- | :--- |
| `TransparentTextView` | `text`, `textColor` | `{"type": "TransparentTextView", "text": "Hello"}` |
| `SyncedLyricsMotionTextView` | `lyrics` (Array) | `{"type": "SyncedLyricsMotionTextView", "lyrics": [{"frame": 0, "text": "Lyric Line"}]}` |
| `TypeWriterTextView` | `writingSpeed` | `{"type": "TypeWriterTextView", "text": "Typing...", "writingSpeed": 0.1}` |
| `PopUpTextView` | `maxTranslationY` | `{"type": "PopUpTextView", "text": "Pop!", "maxTranslationY": 50.0}` |
| `WordVibrateMotionTextView` | `amplitude`, `frequency` | `{"type": "WordVibrateMotionTextView", "text": "Vibrating", "amplitude": 5.0, "frequency": 0.5}` |

### 3.2 Media & Backgrounds
| Type | Specific Parameters | JSON Sample |
| :--- | :--- | :--- |
| `MotionImageView` | `asset` | `{"type": "MotionImageView", "asset": {"type": "ImageAsset", "uri": "..."}}` |
| `VideoFrameView` | `asset` | `{"type": "VideoFrameView", "asset": {"type": "VideoAsset", "uri": "..."}}` |
| `GradientView` | `colors` (Hex Array) | `{"type": "GradientView", "colors": ["#FF0000", "#0000FF"]}` |
| `TranslucentMotionView` | `color`, `alpha` | `{"type": "TranslucentMotionView", "color": "#000000", "alpha": 0.5}` |

### 3.3 UI Components
| Type | Description | JSON Sample |
| :--- | :--- | :--- |
| `MotionProgressBar` | Animated video progress indicator. | `{"type": "MotionProgressBar", "startFrame": 0, "endFrame": 300}` |
| `CircularAudioWaveformView`| Circular music visualizer. | `{"type": "CircularAudioWaveformView", "amplitudes": [0.1, 0.5, 0.3]}` |

### 3.4 Container Views
Manage nested views using percentage-based sizing.

| Type | Specific Parameters | JSON Sample |
| :--- | :--- | :--- |
| `VerticalStackMotionView` | `sections` (Array) | See below. |
| `HorizontalStackMotionView` | `sections` (Array) | See below. |

**`StackSection` Structure:**
- `percentage`: Float (0-100) share of the container's height (Vertical) or width (Horizontal).
- `view`: A nested `MotionView` object.

**Example: Side-by-Side (Horizontal Stack)**
```json
{
  "type": "HorizontalStackMotionView",
  "startFrame": 0,
  "endFrame": 300,
  "sections": [
    {
      "percentage": 50.0,
      "view": {
        "type": "MotionImageView",
        "asset": { "type": "ImageAsset", "uri": "https://server.com/left.jpg" }
      }
    },
    {
      "percentage": 50.0,
      "view": {
        "type": "MotionImageView",
        "asset": { "type": "ImageAsset", "uri": "https://server.com/right.jpg" }
      }
    }
  ]
}
```

---

## 4. MotionLayoutInfo (Layout & Sizing)

The `layout` object is available on all `MotionView` types. It controls how the view is sized and positioned within its parent container.

### 4.1 Dimension Properties (`width` & `height`)
- **`"match_parent"`**: Stretch to fill the parent's full dimension.
- **`"wrap_content"`**: Size according to the view's content (e.g., text length or image aspect ratio).
- **`Int`**: Specify absolute size in pixels (e.g., `1080`).

### 4.2 Alignment (`gravity`)
The `gravity` property is a string that can combine multiple alignment flags using the pipe `|` character.

**Supported Values:**
- `top`, `bottom`: Vertical alignment.
- `left`, `right`, `start`, `end`: Horizontal alignment.
- `center`: Centers both horizontally and vertically.
- `center_horizontal`, `center_vertical`: Independent centering.

**Example:** `"top|center_horizontal"`

### 4.3 Spacing (`margin` & `padding`)
Both `margin` and `padding` are objects containing optional pixel offsets.
- **`margin`**: Outer spacing (distance from parent or siblings).
- **`padding`**: Inner spacing (distance between view border and content).

**Structure:**
```json
{
  "left": 20,
  "top": 0,
  "right": 20,
  "bottom": 50
}
```

### 4.4 Comprehensive Layout Example
```json
{
  "type": "TransparentTextView",
  "text": "Overlaid Caption",
  "layout": {
    "width": "match_parent",
    "height": 300,
    "gravity": "bottom|center_horizontal",
    "margin": { "bottom": 100, "left": 40, "right": 40 },
    "padding": { "top": 20, "bottom": 20 }
  }
}
```

---

## 5. MotionEffect

Applied to views via the `effects` array. **Requires `startFrame` and `endFrame`.**

| Type | Parameters | JSON Sample |
| :--- | :--- | :--- |
| `FadeInEffect` | - | `{"type": "FadeInEffect", "startFrame": 0, "endFrame": 15}` |
| `ZoomInEffect` | `startScale`, `endScale` | `{"type": "ZoomInEffect", "startFrame": 0, "endFrame": 60, "startScale": 1.0, "endScale": 1.2}` |
| `BlurEffect` | `maxBlurRadius` | `{"type": "BlurEffect", "startFrame": 0, "endFrame": 30, "maxBlurRadius": 20.0}` |
| `SlideEffect` | `fromX`, `toX`, `fromY`, `toY` | `{"type": "SlideEffect", "fromX": -1.0, "toX": 0.0}` |

---

## 6. MotionTransition

Used in the `sequence` array to morph between views.

| Type | Parameters | JSON Sample |
| :--- | :--- | :--- |
| `CrossFadeTransition` | `duration` | `{"type": "CrossFadeTransition", "duration": 30}` |
| `BlurTransition` | `duration`, `maxBlurRadius` | `{"type": "BlurTransition", "duration": 45, "maxBlurRadius": 40.0}` |

---

## 7. Comprehensive Recipe: Stacked Lyrics Template
This mimics the `StackedLyricsTemplate` using percentage-based stacking.

```json
{
  "views": [
    {
      "type": "VerticalStackMotionView",
      "startFrame": 0,
      "endFrame": 300,
      "sections": [
        {
          "percentage": 50.0,
          "view": {
            "type": "MotionImageView",
            "asset": { "type": "ImageAsset", "uri": "https://picsum.photos/1080" }
          }
        },
        {
          "percentage": 30.0,
          "view": {
            "type": "SyncedLyricsMotionTextView",
            "lyrics": [
              { "frame": 0, "text": "First line of the song" },
              { "frame": 60, "text": "Second line appears here" }
            ],
            "textSizeVariant": "H3",
            "textColor": "#FFFFFF"
          }
        },
        {
          "percentage": 20.0,
          "view": { "type": "MotionProgressBar" }
        }
      ]
    }
  ]
}
```
