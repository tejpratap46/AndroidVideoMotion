---
name: generate_sdui_video_json
description: Generates valid SDUI JSON for the video rendering engine based on MotionView, MotionEffect, MotionTransition, MotionAudio, and MotionAsset from the SDUI module.
---

# Generate SDUI Video JSON

This skill provides the comprehensive reference and schema instructions for generating valid Server-Driven UI (SDUI) JSON to produce video projects using the `modules/sdui` engine.

The rendering engine operates on a **frame-based timeline**, where all durations, delays, and animation keyframes are specified in integer frames relative to the global project frame rate (`fps`). For example, at 30 FPS, 30 frames represent 1 second of video.

---

## 1. Root Project Structure

The root JSON object defines the configuration, audio tracks, rendering plugins, and visual components of the video.

```json
{
  "config": {
    "aspectRatio": {
      "width": 1080,
      "height": 1920,
      "label": "9:16 Full HD"
    },
    "fps": 30,
    "outputQuality": 100
  },
  "audios": [],
  "plugins": [],
  "views": [],
  "sequence": []
}
```

### Top-Level Fields

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| **`config`** | `Object` | No | Global rendering configuration (resolution, FPS, quality). Defaults to 24 FPS, 100 quality. |
| **`views`** | `Array<MotionView>` | Conditional | Parallel composition layer mode: all views run concurrently according to their frame ranges. Rendered in array order (higher index = higher Z-order, rendered on top). |
| **`sequence`** | `Array<MotionView \| MotionTransition>` | Conditional | Sequential composition mode: views and transitions play sequentially in the timeline. |
| **`audios`** | `Array<MotionAudio>` | No | Background music tracks, voiceovers, and sound effects. |
| **`plugins`** | `Array<MotionPlugin>` | No | Global canvas-level video filters or AI segmenters applied across frames. |

> [!NOTE]
> If `"sequence"` is provided, the producer processes elements sequentially, applying `MotionTransition` between sequential views. If `"sequence"` is omitted, the engine uses the `"views"` list as parallel overlay layers.

---

## 2. Video Configuration (`config`)

The `config` object specifies video dimensions, frame rate, and output encoding quality.

```json
{
  "aspectRatio": {
    "width": 1080,
    "height": 1920
  },
  "fps": 30,
  "outputQuality": 100
}
```

### Predefined Aspect Ratios

The engine recognizes predefined aspect ratio dimensions:

| Aspect Ratio | Preset Dimensions (`width` x `height`) | Common Use Case |
| :--- | :--- | :--- |
| **9:16 (Portrait)** | `1080 x 1920` (Full HD), `720 x 1280` (HD), `480 x 854` (SD) | TikTok, Instagram Reels, YouTube Shorts |
| **16:9 (Landscape)** | `1920 x 1080` (Full HD), `1280 x 720` (HD), `2560 x 1440` (2K), `3840 x 2160` (4K) | Standard YouTube, TV, Desktop |
| **1:1 (Square)** | `1080 x 1080` (Full HD), `720 x 720` (HD), `480 x 480` (SD) | Instagram Feed, Square ads |
| **4:3 (Classic)** | `960 x 720` (HD), `768 x 576` (PAL SD), `640 x 480` (SD) | Retro / Classic TV formats |
| **21:9 (Cinema)** | `2520 x 1080` (Full HD), `5120 x 2160` (5K) | Ultrawide cinematic widescreen |

*Custom dimensions:* Any custom positive integer `width` and `height` are supported.

---

## 3. MotionAsset Architecture

Assets are polymorphic resources referenced by views and audio components. Every asset contains a `type`, a `uri` string (`https://`, `http://`, `file://`, or `content://`), and optional `metadata`.

### Asset Types

| Type | URI Protocol | Parameters | Description |
| :--- | :--- | :--- | :--- |
| **`SimpleMotionAsset`** | `https://`, `file://` | `uri`, `metadata` (optional) | Generic media file reference. |
| **`ImageAsset`** | `https://`, `file://` | `uri`, `metadata` (optional) | Bitmap images (JPEG, PNG, WebP). |
| **`VideoAsset`** | `https://`, `file://` | `uri`, `metadata` (optional) | Video files (MP4, WebM, etc.). |
| **`FontAsset`** | `https://`, `file://` | `uri`, `fontName` (optional), `metadata` (optional) | Custom TrueType/OpenType web font. |
| **`TTSAudioAsset`** | `content://tts_...` | `uri`, `metadata: { "text": "..." }` | Synthetic text-to-speech audio asset. |

### JSON Asset Samples

```json
// Image Asset
{
  "type": "ImageAsset",
  "uri": "https://picsum.photos/1080/1920"
}

// Font Asset
{
  "type": "FontAsset",
  "uri": "https://fonts.gstatic.com/s/roboto/v30/KFOmCnqEu92Fr1Mu4mxK.ttf",
  "fontName": "Roboto"
}

// TTS Audio Asset
{
  "type": "TTSAudioAsset",
  "uri": "content://tts_audio",
  "metadata": {
    "text": "Welcome to our newly generated video."
  }
}
```

---

## 4. Common MotionView Properties & Layout

All views inherit common timeline and layout properties.

### Common Base Properties

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| **`type`** | `String` | **Required** | The registered class name of the view (e.g., `"TransparentTextView"`). |
| **`startFrame`** | `Int` | `0` | Starting frame index on the timeline when the view becomes visible. |
| **`endFrame`** | `Int` | `0` | Ending frame index on the timeline when the view ceases to be visible. `-1` keeps the view visible until the end of the video. |
| **`loop`** | `Object` | `{ "start": 0, "end": 0 }` | Optional internal loop range: `{ "start": 0, "end": 60 }`. |
| **`layout`** | `Object` | Default layout | Sizing, alignment, margin, and padding (`MotionLayoutInfo`). |
| **`effects`** | `Array<MotionEffect>` | `[]` | List of visual effects applied to this view over time. |

---

### MotionLayoutInfo (`layout`)

Controls how a view is positioned and sized inside its parent canvas or container:

```json
{
  "width": "match_parent",
  "height": "wrap_content",
  "gravity": "bottom|center_horizontal",
  "margin": {
    "left": 40,
    "top": 0,
    "right": 40,
    "bottom": 120
  },
  "padding": {
    "left": 20,
    "top": 20,
    "right": 20,
    "bottom": 20
  }
}
```

#### Sizing (`width` & `height`)
- `"match_parent"`: Expands to the parent container's full dimension.
- `"wrap_content"`: Bounds to the internal content size.
- Exact integer: Explicit pixel dimensions (e.g., `1080`, `450`).

#### Alignment (`gravity`)
Combine horizontal and vertical flags using the pipe `|` character:
- **Vertical flags:** `top`, `bottom`, `center_vertical`
- **Horizontal flags:** `left`, `right`, `start`, `end`, `center_horizontal`
- **Combined centering:** `center` (centers both horizontally and vertically)
- *Default centering:* If horizontal gravity is omitted, the engine centers horizontally. If vertical gravity is omitted, the engine centers vertically.

#### Spacing (`margin` & `padding`)
- `margin`: `{ "left": 0, "top": 0, "right": 0, "bottom": 0 }` (offset from parent border)
- `padding`: `{ "left": 0, "top": 0, "right": 0, "bottom": 0 }` (offset within the view border)

---

## 5. Complete MotionView Catalog

The following `MotionView` components are registered and available in `modules/sdui`:

### 5.1 Text Views

All text views extend `AbstractMotionTextView` and share these styling properties:
- `textSizeVariant`: Typography scale enum: `"H1"`, `"H2"`, `"H3"`, `"H4"`, `"H5"`, `"H6"`, or `"P"`. These auto-scale proportionally with the video resolution.
- `textColor`: Hex color string (e.g., `"#FFFFFF"`, `"#00E5FF"`).
- `highlightColor`: Hex color string for emphasis (e.g., `"#FFEB3B"`).
- `fontAsset`: Polymorphic `FontAsset` object for custom web typography.

#### 1. `TransparentTextView`
Static text overlay with a transparent background.
- **Properties:**
  - `text` (`String`): The string to render.
- **Sample:**
  ```json
  {
    "type": "TransparentTextView",
    "startFrame": 0,
    "endFrame": 150,
    "text": "Breaking Headlines",
    "textSizeVariant": "H1",
    "textColor": "#FFFFFF",
    "layout": {
      "width": "match_parent",
      "height": "wrap_content",
      "gravity": "center"
    }
  }
  ```

#### 2. `TypeWriterTextView`
Reveals text character by character with an optional blinking cursor.
- **Properties:**
  - `text` (`String`): The text to type.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor. `0.0` calculates speed automatically to fit `startFrame` to `endFrame`. Values `> 1.0` speed up typing.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Opacity of untyped letters.
  - `cursorChar` (`String`, default `"|"`): Character used for typing cursor.
  - `blinkFrameRate` (`Int`, default `10`): Number of frames per cursor blink cycle.
- **Sample:**
  ```json
  {
    "type": "TypeWriterTextView",
    "startFrame": 0,
    "endFrame": 120,
    "text": "Generating intelligence across devices...",
    "writingSpeed": 1.0,
    "cursorChar": "|",
    "blinkFrameRate": 8,
    "textSizeVariant": "H3",
    "textColor": "#00FFCC",
    "layout": {
      "gravity": "center"
    }
  }
  ```

#### 3. `WordWriterTextView`
Reveals text word by word (rather than character by character), highlighting the current word.
- **Properties:**
  - `text` (`String`): The full sentence.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Opacity of words not yet revealed.
  - `highlightColor` (`String`): Hex color used to highlight the currently spoken/written word.
- **Sample:**
  ```json
  {
    "type": "WordWriterTextView",
    "startFrame": 0,
    "endFrame": 150,
    "text": "Every single word appears with precise timing",
    "writingSpeed": 1.0,
    "unwrittenTextAlpha": 0.2,
    "textSizeVariant": "H2",
    "textColor": "#FFFFFF",
    "highlightColor": "#FFD700",
    "layout": {
      "gravity": "center",
      "margin": { "left": 40, "right": 40 }
    }
  }
  ```

#### 4. `WordBlinkTextView`
Displays one word at a time in rapid succession (RSVP speed-reading style).
- **Properties:**
  - `text` (`String`): Sentence to cycle through.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor.
- **Sample:**
  ```json
  {
    "type": "WordBlinkTextView",
    "startFrame": 0,
    "endFrame": 90,
    "text": "FOCUS SPEED PRECISION POWER",
    "textSizeVariant": "H1",
    "textColor": "#FF3366",
    "layout": { "gravity": "center" }
  }
  ```

#### 5. `PopUpTextView`
Each word smoothly animates popping up from the bottom with vertical translation and alpha fade.
- **Properties:**
  - `text` (`String`): Sentence to animate.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Alpha of pending words.
  - `maxTranslationY` (`Float`, default `50.0`): Pixel distance words translate upward from below.
- **Sample:**
  ```json
  {
    "type": "PopUpTextView",
    "startFrame": 0,
    "endFrame": 180,
    "text": "Words smoothly pop into place",
    "maxTranslationY": 60.0,
    "textSizeVariant": "H2",
    "textColor": "#FFFFFF",
    "layout": {
      "gravity": "center",
      "margin": { "left": 50, "right": 50 }
    }
  }
  ```

#### 6. `RainbowPopUpTextView`
Each word pops up from below while cycling through animated rainbow colors.
- **Properties:**
  - `text` (`String`): Words to pop up.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Alpha for unwritten words.
  - `maxTranslationY` (`Float`, default `50.0`): Upward translation distance.
- **Sample:**
  ```json
  {
    "type": "RainbowPopUpTextView",
    "startFrame": 0,
    "endFrame": 180,
    "text": "Unleash vibrant colorful motion graphics",
    "maxTranslationY": 50.0,
    "textSizeVariant": "H1",
    "layout": { "gravity": "center" }
  }
  ```

#### 7. `AccentMiddlePopUpTextView`
Pop-up words animation where the middle word in the phrase receives an accent color.
- **Properties:**
  - `text` (`String`): Input phrase.
  - `writingSpeed` (`Float`, default `0.0`): Speed factor.
  - `maxTranslationY` (`Float`, default `50.0`): Upward translation distance.
  - `accentColor` (`String`, default `"#FFFF00"`): Hex color applied exclusively to the middle word.
- **Sample:**
  ```json
  {
    "type": "AccentMiddlePopUpTextView",
    "startFrame": 0,
    "endFrame": 150,
    "text": "Create The Future",
    "accentColor": "#FF4081",
    "textSizeVariant": "H2",
    "textColor": "#FFFFFF",
    "layout": { "gravity": "center" }
  }
  ```

#### 8. `WordVibrateMotionTextView`
Each word oscillates independently with sinusoidal vibration.
- **Properties:**
  - `text` (`String`): Text string.
  - `amplitude` (`Float`, default `5.0`): Vibration displacement in pixels.
  - `frequency` (`Float`, default `0.5`): Oscillation cycles per frame.
  - `phaseShiftPerWord` (`Float`, default `1.0`): Phase lag between consecutive words.
- **Sample:**
  ```json
  {
    "type": "WordVibrateMotionTextView",
    "startFrame": 0,
    "endFrame": 200,
    "text": "HIGH ENERGY VIBRATION",
    "amplitude": 8.0,
    "frequency": 1.0,
    "phaseShiftPerWord": 0.8,
    "textSizeVariant": "H1",
    "textColor": "#FFEA00",
    "layout": { "gravity": "center" }
  }
  ```

---

### 5.2 Media & Background Views

#### 9. `MotionImageView`
Renders an image from an asset with `CENTER_CROP` scaling.
- **Properties:**
  - `asset` (`MotionAsset`): Polymorphic asset (or legacy `imageUri` string).
- **Sample:**
  ```json
  {
    "type": "MotionImageView",
    "startFrame": 0,
    "endFrame": 300,
    "asset": {
      "type": "ImageAsset",
      "uri": "https://images.unsplash.com/photo-1518791841217-8f162f1e1131"
    },
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

#### 10. `CircularMotionImageView`
Clips and displays an image within a circular/oval shape.
- **Properties:**
  - `asset` (`MotionAsset`): Image asset (or legacy `imageUri` string).
- **Sample:**
  ```json
  {
    "type": "CircularMotionImageView",
    "startFrame": 0,
    "endFrame": 300,
    "asset": {
      "type": "ImageAsset",
      "uri": "https://i.pravatar.cc/500"
    },
    "layout": {
      "width": 360,
      "height": 360,
      "gravity": "center"
    }
  }
  ```

#### 11. `VideoFrameView`
Decodes and renders sequential video frames in sync with the project timeline.
- **Properties:**
  - `asset` (`MotionAsset`): Video asset (or legacy `videoUri` string).
- **Sample:**
  ```json
  {
    "type": "VideoFrameView",
    "startFrame": 0,
    "endFrame": 300,
    "asset": {
      "type": "VideoAsset",
      "uri": "file:///android_asset/sample_clip.mp4"
    },
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

#### 12. `CoilVideoPlayer`
Hardware-accelerated video decoding and frame rendering using the Coil media pipeline (`modules/coil-motion-ext`). Supports all Coil-based filters, effects, and plugins.
- **Properties:**
  - `asset` (`MotionAsset`): Video asset (`VideoAsset` or `SimpleMotionAsset`).
- **Sample:**
  ```json
  {
    "type": "CoilVideoPlayer",
    "startFrame": 0,
    "endFrame": 300,
    "asset": {
      "type": "VideoAsset",
      "uri": "file:///android_asset/sample_clip.mp4"
    },
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

#### 13. `GradientView`
Smooth multi-stop gradient background.
- **Properties:**
  - `orientation` (`String`): `"VERTICAL"`, `"HORIZONTAL"`, or `"CIRCULAR"` (default `"VERTICAL"`).
  - `colors` (`Array<String>`): Array of hex color strings (at least 2 colors).
- **Sample:**
  ```json
  {
    "type": "GradientView",
    "startFrame": 0,
    "endFrame": 300,
    "orientation": "VERTICAL",
    "colors": ["#0F2027", "#203A43", "#2C5364"],
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

#### 14. `TranslucentMotionView`
A solid or semi-transparent color tint overlay. Useful for darkening video backgrounds behind readable text.
- **Properties:**
  - `color` (`String`, default `"#00000000"`): Hex color string.
  - `alpha` (`Float`, default `1.0`): Opacity (0.0 to 1.0).
- **Sample:**
  ```json
  {
    "type": "TranslucentMotionView",
    "startFrame": 0,
    "endFrame": 300,
    "color": "#000000",
    "alpha": 0.5,
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

---

### 5.3 Progress & Audio Waveforms

#### 15. `MotionProgressBar`
A real-time progress bar synchronized with video progression.
- **Properties:**
  - `style` (`String`): `"HORIZONTAL"` or `"CIRCULAR"` (default `"HORIZONTAL"`).
  - `color` (`String`): Hex color string (default `"#FFFFFF"`).
- **Sample:**
  ```json
  {
    "type": "MotionProgressBar",
    "startFrame": 0,
    "endFrame": 300,
    "style": "HORIZONTAL",
    "color": "#E91E63",
    "layout": {
      "width": "match_parent",
      "height": 16,
      "gravity": "bottom"
    }
  }
  ```

#### 16. `CircularAudioWaveformView`
Circular music audio visualizer spikes.
- **Properties:**
  - `amplitudes` (`Array<Float>`): List of audio amplitude values per frame.
- **Sample:**
  ```json
  {
    "type": "CircularAudioWaveformView",
    "startFrame": 0,
    "endFrame": 300,
    "amplitudes": [0.1, 0.35, 0.7, 0.45, 0.2, 0.8, 0.6],
    "layout": {
      "width": 500,
      "height": 500,
      "gravity": "center"
    }
  }
  ```

#### 17. `RadialAudioWaveformView`
Dynamic radial spikes pulsing outwards from the center.
- **Properties:**
  - `amplitudes` (`Array<Float>`): List of audio amplitude values per frame.
- **Sample:**
  ```json
  {
    "type": "RadialAudioWaveformView",
    "startFrame": 0,
    "endFrame": 300,
    "amplitudes": [0.2, 0.5, 0.9, 0.3, 0.7, 0.4],
    "layout": {
      "width": 600,
      "height": 600,
      "gravity": "center"
    }
  }
  ```

---

### 5.4 Container Views

#### 18. `VerticalStackMotionView` & 19. `HorizontalStackMotionView`
Partitions the canvas into proportional stacked slices using percentage shares (sum of section percentages should equal 100.0).

- **`VerticalStackMotionView`**: Stacks children vertically (top-to-bottom).
- **`HorizontalStackMotionView`**: Stacks children horizontally (side-by-side).

**Section Structure (`StackSection`):**
- `percentage` (`Float`): Percent share of the container dimension (0.0 to 100.0).
- `view` (`MotionView`): The nested `MotionView` inside this section.

**Split-Screen Sample:**
```json
{
  "type": "VerticalStackMotionView",
  "startFrame": 0,
  "endFrame": 300,
  "sections": [
    {
      "percentage": 50.0,
      "view": {
        "type": "MotionImageView",
        "startFrame": 0,
        "endFrame": 300,
        "asset": {
          "type": "ImageAsset",
          "uri": "https://picsum.photos/1080/960?1"
        }
      }
    },
    {
      "percentage": 50.0,
      "view": {
        "type": "MotionImageView",
        "startFrame": 0,
        "endFrame": 300,
        "asset": {
          "type": "ImageAsset",
          "uri": "https://picsum.photos/1080/960?2"
        }
      }
    }
  ]
}
```

---

### 5.5 Chat & Lyrics Views

#### 20. `IMessageBubbleTextView`
Renders a standalone speech bubble in Apple iMessage style with rounded corners and a directional tail, revealing text word-by-word.
- **Properties:**
  - `text` (`String`): The text content inside the bubble.
  - `bubbleColor` (`String`, default `"#007AFF"`): Background color of the bubble (hex string).
  - `textColor` (`String`, default `"#FFFFFF"`): Text font color (hex string).
  - `isOutgoing` (`Boolean`, default `true`): If `true`, renders as an outgoing message with a tail on the bottom-right; if `false`, renders as an incoming message with a tail on the bottom-left.
  - `writingSpeed` (`Float`, default `0.0`): Word reveal speed factor. If `0.0`, reveals words evenly across the frame range.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Alpha opacity for words not yet written/revealed.
  - `textSizeVariant` (`String`, default `"H4"`): Typography scale enum (`"H1"` to `"H6"`, or `"P"`).
  - `fontAsset` (`FontAsset`, optional): Custom web typography font asset.
- **Sample:**
  ```json
  {
    "type": "IMessageBubbleTextView",
    "startFrame": 0,
    "endFrame": 120,
    "text": "Hey! Are you ready for this?",
    "bubbleColor": "#007AFF",
    "textColor": "#FFFFFF",
    "isOutgoing": true,
    "writingSpeed": 1.0,
    "unwrittenTextAlpha": 0.0,
    "textSizeVariant": "H4",
    "layout": {
      "width": "wrap_content",
      "height": "wrap_content",
      "gravity": "center"
    }
  }
  ```

#### 21. `IMessageChatMotionView`
Renders song lyrics flowing as an Apple iMessage conversation stream. Multiple chat bubbles appear sequentially on a single screen anchored to the bottom. Each line expands multi-frame word-by-word with alpha fade-in on newly added words, and older lyrics smoothly scroll upward out of the frame when the page fills up.
- **Key Features:**
  - **Bottom Anchored:** Messages begin anchored to the bottom safe area and push earlier lines upwards.
  - **Dynamic Animated Expansion:** Bubble dimensions expand smoothly across multiple video frames as each new word appears.
  - **Word Alpha Fade:** Newly added words fade in smoothly (`0.0 -> 1.0`) during expansion while earlier words remain fully opaque.
  - **Continuous Flow & Upward Glide:** When the screen is full, older lines glide upward above the visible page (`Y < 0`) with smooth fade-out, keeping active lyrics in the bottom safe area.
  - **Alternating Conversations:** Supports duet songs with incoming (left) and outgoing (right) bubbles.
- **Properties:**
  - `lyrics` (`Array<SyncedLyricFrame>`): List of synchronized lyric objects:
    - `frame` (`Int`): Keyframe index on the timeline when this line begins typing.
    - `text` (`String`): The lyric text for this line.
  - `bubbleColor` (`String`, default `"#007AFF"`): Outgoing bubble background color (hex string).
  - `textColor` (`String`, default `"#FFFFFF"`): Outgoing text color (hex string).
  - `incomingBubbleColor` (`String`, default `"#26252A"`): Incoming bubble background color (hex string).
  - `incomingTextColor` (`String`, default `"#FFFFFF"`): Incoming text color (hex string).
  - `isAlternating` (`Boolean`, default `false`): If `true`, alternates bubbles between outgoing and incoming for duet lyrics.
  - `writingSpeed` (`Float`, default `1.0`): Word reveal typing speed factor.
  - `unwrittenTextAlpha` (`Float`, default `0.0`): Opacity of unwritten words.
  - `textSizeVariant` (`String`, default `"H5"`): Typography variant. `"H5"` is recommended for authentic mobile chat bubble proportions.
  - `fontAsset` (`FontAsset`, optional): Custom web font.
- **Sample:**
  ```json
  {
    "type": "IMessageChatMotionView",
    "startFrame": 0,
    "endFrame": 360,
    "lyrics": [
      { "frame": 0, "text": "I found a love for me" },
      { "frame": 60, "text": "Darling, just dive right in" },
      { "frame": 120, "text": "And follow my lead" },
      { "frame": 180, "text": "Well, I found a girl, beautiful and sweet" },
      { "frame": 240, "text": "Oh, I never knew you were the someone" },
      { "frame": 300, "text": "Waiting for me" }
    ],
    "bubbleColor": "#007AFF",
    "textColor": "#FFFFFF",
    "incomingBubbleColor": "#26252A",
    "incomingTextColor": "#FFFFFF",
    "isAlternating": false,
    "writingSpeed": 1.0,
    "unwrittenTextAlpha": 0.0,
    "textSizeVariant": "H5",
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

#### 22. `SyncedLyricsMotionTextView`
Displays synchronized line-by-line lyrics synchronized to keyframes, showing the active line corresponding to the current playback frame.
- **Properties:**
  - `lyrics` (`Array<SyncedLyricFrame>`): Array of `{ "frame": Int, "text": String }`.
  - `textColor` (`String`, default `"#FFFFFF"`): Text font color.
  - `textSizeVariant` (`String`, default `"H2"`): Typography scale enum (`"H1"` to `"H6"`, or `"P"`).
  - `fontAsset` (`FontAsset`, optional): Custom web font.
- **Sample:**
  ```json
  {
    "type": "SyncedLyricsMotionTextView",
    "startFrame": 0,
    "endFrame": 300,
    "lyrics": [
      { "frame": 0, "text": "Is this the real life?" },
      { "frame": 90, "text": "Is this just fantasy?" },
      { "frame": 180, "text": "Caught in a landslide" },
      { "frame": 240, "text": "No escape from reality" }
    ],
    "textSizeVariant": "H2",
    "textColor": "#FFFFFF",
    "layout": {
      "width": "match_parent",
      "height": "wrap_content",
      "gravity": "center"
    }
  }
  ```

#### 23. `MultiLyricsContainer`
Container view managing multi-lyrics song playback and media assets.
- **Properties:**
  - `songName` (`String`): Title of the song.
  - `asset` (`MotionAsset`, optional): Media asset associated with the lyrics.
- **Sample:**
  ```json
  {
    "type": "MultiLyricsContainer",
    "startFrame": 0,
    "endFrame": 300,
    "songName": "Bohemian Rhapsody",
    "asset": {
      "type": "SimpleMotionAsset",
      "uri": "https://example.com/audio/song.mp3"
    },
    "layout": {
      "width": "match_parent",
      "height": "match_parent"
    }
  }
  ```

---

## 6. Complete MotionEffect Catalog

Effects are added to a view's `"effects"` array. Every effect **must specify `startFrame` and `endFrame`**.

| Effect Name | Parameters & Defaults | Description | Sample |
| :--- | :--- | :--- | :--- |
| **`FadeInEffect`** | None | Smoothly fades opacity from 0.0 to 1.0. | `{"type": "FadeInEffect", "startFrame": 0, "endFrame": 30}` |
| **`FadeOutEffect`** | None | Smoothly fades opacity from 1.0 to 0.0. | `{"type": "FadeOutEffect", "startFrame": 270, "endFrame": 300}` |
| **`ZoomInEffect`** | `startScale` (1.0), `endScale` (2.0) | Zooms view scale up between frames. | `{"type": "ZoomInEffect", "startFrame": 0, "endFrame": 60, "startScale": 1.0, "endScale": 1.25}` |
| **`ZoomOutEffect`** | `startScale` (2.0), `endScale` (1.0) | Zooms view scale down between frames. | `{"type": "ZoomOutEffect", "startFrame": 0, "endFrame": 60, "startScale": 1.3, "endScale": 1.0}` |
| **`BlurEffect`** | `maxBlurRadius` (20.0) | Blurs view from 0.1 to `maxBlurRadius`. | `{"type": "BlurEffect", "startFrame": 0, "endFrame": 30, "maxBlurRadius": 15.0}` |
| **`GlitchEffect`** | `intensity` (10.0) | Applies digital glitch displacement. | `{"type": "GlitchEffect", "startFrame": 45, "endFrame": 60, "intensity": 12.0}` |
| **`VibrateEffect`** | `amplitude` (5.0), `frequency` (1.0) | Applies rapid shake vibration to view. | `{"type": "VibrateEffect", "startFrame": 0, "endFrame": 90, "amplitude": 6.0, "frequency": 1.5}` |
| **`VintageEffect`** | `fromIntensity` (0.0), `toIntensity` (1.0) | Color wash / sepia vintage transition. | `{"type": "VintageEffect", "startFrame": 0, "endFrame": 100, "fromIntensity": 0.0, "toIntensity": 0.8}` |
| **`SlideLeftToRightEffect`** | None | Slides view in from the left edge. | `{"type": "SlideLeftToRightEffect", "startFrame": 0, "endFrame": 30}` |
| **`SlideRightToLeftEffect`** | None | Slides view in from the right edge. | `{"type": "SlideRightToLeftEffect", "startFrame": 0, "endFrame": 30}` |
| **`SlideTopToBottomEffect`** | None | Slides view in from the top edge. | `{"type": "SlideTopToBottomEffect", "startFrame": 0, "endFrame": 30}` |
| **`SlideBottomToTopEffect`** | None | Slides view in from the bottom edge. | `{"type": "SlideBottomToTopEffect", "startFrame": 0, "endFrame": 30}` |
| **`SlideEffect`** | `fromX`, `toX`, `fromY`, `toY` | Arbitrary custom coordinate sliding. | `{"type": "SlideEffect", "startFrame": 0, "endFrame": 25, "fromX": -1.0, "toX": 0.0}` |

---

## 7. MotionTransitions (Sequential Mode)

When using the root `"sequence"` array, views are sequenced one after another, separated by transition items.

| Transition Name | Parameters & Defaults | Description | Sample |
| :--- | :--- | :--- | :--- |
| **`CrossFadeTransition`** | `duration` (30 frames) | Smoothly cross-fades the preceding view into the following view. | `{"type": "CrossFadeTransition", "duration": 30}` |
| **`BlurTransition`** | `maxBlurRadius` (20.0), `duration` (30 frames) | Blurs the outgoing view and unblurs the incoming view. | `{"type": "BlurTransition", "duration": 25, "maxBlurRadius": 25.0}` |
| **`SlideTransition`** | `direction` (`"LEFT_TO_RIGHT"`, `"RIGHT_TO_LEFT"`, `"TOP_TO_BOTTOM"`, `"BOTTOM_TO_TOP"`), `duration` (30 frames) | Slides the next view into frame while sliding the current view out. | `{"type": "SlideTransition", "duration": 30, "direction": "RIGHT_TO_LEFT"}` |

### Sequence Example
```json
{
  "sequence": [
    {
      "type": "MotionImageView",
      "startFrame": 0,
      "endFrame": 90,
      "asset": { "type": "ImageAsset", "uri": "https://picsum.photos/1080/1920?1" }
    },
    {
      "type": "CrossFadeTransition",
      "duration": 30
    },
    {
      "type": "MotionImageView",
      "startFrame": 90,
      "endFrame": 180,
      "asset": { "type": "ImageAsset", "uri": "https://picsum.photos/1080/1920?2" }
    }
  ]
}
```

---

## 8. MotionAudio (Audio Tracks)

Audio tracks in the `"audios"` array configure background music, voiceovers, or effects.

```json
{
  "type": "MotionAudio",
  "asset": {
    "type": "SimpleMotionAsset",
    "uri": "https://example.com/audio/soundtrack.mp3"
  },
  "startFrame": 0,
  "endFrame": 300,
  "delayFrame": 15
}
```

### Parameters
- **`asset`**: Polymorphic `MotionAsset` (e.g., `SimpleMotionAsset` or `TTSAudioAsset`).
- **`startFrame`**: Audio trim start frame (offset within the source audio file).
- **`endFrame`**: Audio trim end frame (trim cut-off within the source audio file).
- **`delayFrame`**: Playback delay in frames from the beginning of the video timeline before playback starts.

---

## 9. Rules for LLMs Generating SDUI JSON

When generating SDUI JSON for videos, you MUST adhere to the following rules:

1. **Calculate Frames from FPS:**
   - Always check or specify `config.fps` (typically `30`).
   - `duration_in_frames = duration_in_seconds * fps`.
   - Ensure all `endFrame` values are greater than `startFrame`.
2. **Correct Layer Z-Ordering in `views`:**
   - Background views (`GradientView`, `MotionImageView`, `VideoFrameView`) MUST come first.
   - Tint overlays (`TranslucentMotionView`) come second.
   - Foreground text (`TypeWriterTextView`, `PopUpTextView`), visualizers, and `MotionProgressBar` come last so they remain visible on top.
3. **Effect Frame Boundaries:**
   - Any effect in `"effects"` MUST have `startFrame` and `endFrame` that lie strictly within the containing view's `[startFrame, endFrame]` range.
4. **Valid Aspect Ratios:**
   - For vertical reels (Shorts / TikTok / Reels), use `1080 x 1920` (`Ratio9x16_1080`).
   - For horizontal video, use `1920 x 1080` (`Ratio16x9_1080`).
5. **Exact Gravity Syntax:**
   - Gravity strings must be lowercase pipe-separated flags (e.g., `"top|center_horizontal"`, `"bottom|center_horizontal"`, `"center"`).
6. **Valid Hex Colors:**
   - Use standard 6-digit or 8-digit hex codes (e.g., `"#FFFFFF"`, `"#80000000"`, `"#FFD700"`).

---

## 10. Complete Production Recipes

### Recipe 1: Vertical Viral Reel with Pop-Up Text & Audio (9:16)
Features a background image with a Ken Burns zoom, a dark translucent overlay for contrast, pop-up animated text, a progress bar, and background audio.

```json
{
  "config": {
    "aspectRatio": {
      "width": 1080,
      "height": 1920,
      "label": "9:16 Full HD"
    },
    "fps": 30,
    "outputQuality": 100
  },
  "audios": [
    {
      "type": "MotionAudio",
      "asset": {
        "type": "SimpleMotionAsset",
        "uri": "https://assets.mixkit.co/music/preview/mixkit-tech-house-vibes-130.mp3"
      },
      "startFrame": 0,
      "endFrame": 180,
      "delayFrame": 0
    }
  ],
  "views": [
    {
      "type": "MotionImageView",
      "startFrame": 0,
      "endFrame": 180,
      "asset": {
        "type": "ImageAsset",
        "uri": "https://images.unsplash.com/photo-1518770660439-4636190af475"
      },
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      },
      "effects": [
        {
          "type": "ZoomInEffect",
          "startFrame": 0,
          "endFrame": 180,
          "startScale": 1.0,
          "endScale": 1.15
        }
      ]
    },
    {
      "type": "TranslucentMotionView",
      "startFrame": 0,
      "endFrame": 180,
      "color": "#000000",
      "alpha": 0.45,
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "AccentMiddlePopUpTextView",
      "startFrame": 15,
      "endFrame": 165,
      "text": "BUILD THE FUTURE",
      "accentColor": "#00E5FF",
      "textSizeVariant": "H1",
      "textColor": "#FFFFFF",
      "maxTranslationY": 60.0,
      "layout": {
        "width": "match_parent",
        "height": "wrap_content",
        "gravity": "center",
        "margin": {
          "left": 40,
          "right": 40
        }
      },
      "effects": [
        {
          "type": "FadeInEffect",
          "startFrame": 15,
          "endFrame": 35
        },
        {
          "type": "FadeOutEffect",
          "startFrame": 145,
          "endFrame": 165
        }
      ]
    },
    {
      "type": "MotionProgressBar",
      "startFrame": 0,
      "endFrame": 180,
      "style": "HORIZONTAL",
      "color": "#00E5FF",
      "layout": {
        "width": "match_parent",
        "height": 16,
        "gravity": "bottom"
      }
    }
  ]
}
```

---

### Recipe 2: Split-Screen Comparison (Stacked Layout)
Demonstrates vertical 50/50 split partitioning using `VerticalStackMotionView`.

```json
{
  "config": {
    "aspectRatio": {
      "width": 1080,
      "height": 1920,
      "label": "9:16 Full HD"
    },
    "fps": 30,
    "outputQuality": 100
  },
  "views": [
    {
      "type": "VerticalStackMotionView",
      "startFrame": 0,
      "endFrame": 180,
      "sections": [
        {
          "percentage": 50.0,
          "view": {
            "type": "MotionImageView",
            "startFrame": 0,
            "endFrame": 180,
            "asset": {
              "type": "ImageAsset",
              "uri": "https://picsum.photos/1080/960?image=10"
            }
          }
        },
        {
          "percentage": 50.0,
          "view": {
            "type": "MotionImageView",
            "startFrame": 0,
            "endFrame": 180,
            "asset": {
              "type": "ImageAsset",
              "uri": "https://picsum.photos/1080/960?image=20"
            }
          }
        }
      ],
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "WordWriterTextView",
      "startFrame": 10,
      "endFrame": 170,
      "text": "Before vs After Transformation",
      "writingSpeed": 1.0,
      "textSizeVariant": "H2",
      "textColor": "#FFFFFF",
      "highlightColor": "#FFD700",
      "layout": {
        "width": "match_parent",
        "height": "wrap_content",
        "gravity": "center",
        "padding": {
          "left": 30,
          "top": 15,
          "right": 30,
          "bottom": 15
        }
      }
    }
  ]
}
```

---

### Recipe 3: Multi-Scene Sequential Storyboard (`sequence`)
Demonstrates multi-scene progression where views transition seamlessly using `SlideTransition` and `CrossFadeTransition`.

```json
{
  "config": {
    "aspectRatio": {
      "width": 1920,
      "height": 1080,
      "label": "16:9 Full HD"
    },
    "fps": 30,
    "outputQuality": 100
  },
  "sequence": [
    {
      "type": "GradientView",
      "startFrame": 0,
      "endFrame": 90,
      "orientation": "VERTICAL",
      "colors": ["#141E30", "#243B55"],
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "SlideTransition",
      "duration": 30,
      "direction": "RIGHT_TO_LEFT"
    },
    {
      "type": "MotionImageView",
      "startFrame": 90,
      "endFrame": 180,
      "asset": {
        "type": "ImageAsset",
        "uri": "https://picsum.photos/1920/1080?image=100"
      },
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "CrossFadeTransition",
      "duration": 20
    },
    {
      "type": "GradientView",
      "startFrame": 180,
      "endFrame": 270,
      "orientation": "HORIZONTAL",
      "colors": ["#8A2387", "#E94057", "#F27121"],
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    }
  ]
}
```

---

### Recipe 4: Apple iMessage Chat Lyrics Video (9:16 Portrait)
Features an Apple iMessage flowing chat conversation where lyric lines expand word-by-word into chat bubbles, anchored to the bottom, pushing earlier lines upward as the screen fills. Includes background imagery with subtle zoom, dark contrast scrim, and an audio soundtrack.

```json
{
  "config": {
    "aspectRatio": {
      "width": 1080,
      "height": 1920,
      "label": "9:16 Full HD"
    },
    "fps": 30,
    "outputQuality": 100
  },
  "audios": [
    {
      "type": "MotionAudio",
      "asset": {
        "type": "SimpleMotionAsset",
        "uri": "https://assets.mixkit.co/music/preview/mixkit-serene-view-443.mp3"
      },
      "startFrame": 0,
      "endFrame": 360,
      "delayFrame": 0
    }
  ],
  "views": [
    {
      "type": "MotionImageView",
      "startFrame": 0,
      "endFrame": 360,
      "asset": {
        "type": "ImageAsset",
        "uri": "https://images.unsplash.com/photo-1518770660439-4636190af475"
      },
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      },
      "effects": [
        {
          "type": "ZoomInEffect",
          "startFrame": 0,
          "endFrame": 360,
          "startScale": 1.0,
          "endScale": 1.12
        }
      ]
    },
    {
      "type": "TranslucentMotionView",
      "startFrame": 0,
      "endFrame": 360,
      "color": "#000000",
      "alpha": 0.55,
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "IMessageChatMotionView",
      "startFrame": 0,
      "endFrame": 360,
      "lyrics": [
        { "frame": 0, "text": "I found a love for me" },
        { "frame": 60, "text": "Darling, just dive right in" },
        { "frame": 120, "text": "And follow my lead" },
        { "frame": 180, "text": "Well, I found a girl, beautiful and sweet" },
        { "frame": 240, "text": "Oh, I never knew you were the someone" },
        { "frame": 300, "text": "Waiting for me" }
      ],
      "bubbleColor": "#007AFF",
      "textColor": "#FFFFFF",
      "incomingBubbleColor": "#26252A",
      "incomingTextColor": "#FFFFFF",
      "isAlternating": false,
      "writingSpeed": 1.0,
      "unwrittenTextAlpha": 0.0,
      "textSizeVariant": "H5",
      "layout": {
        "width": "match_parent",
        "height": "match_parent"
      }
    },
    {
      "type": "MotionProgressBar",
      "startFrame": 0,
      "endFrame": 360,
      "style": "HORIZONTAL",
      "color": "#007AFF",
      "layout": {
        "width": "match_parent",
        "height": 12,
        "gravity": "bottom"
      }
    }
  ]
}
```
