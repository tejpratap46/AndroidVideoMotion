# SDUI Web Preview

A React-based web previewer for the Android Video Motion SDUI JSON.

## Features
- **JSON Editor**: Paste your SDUI JSON to see it rendered instantly.
- **Playback Controls**: Play, pause, and scrub through frames.
- **View Support**:
    - `GradientView`
    - `TransparentTextView`
    - `WordWriterTextView` / `TypeWriterTextView`
    - `VideoFrameView`
    - `AudioWaveformView` (Placeholder)
- **Effect Support**:
    - `SlideRightToLeftEffect`

## Getting Started

1.  Navigate to `web-sdui` directory.
2.  Install dependencies: `npm install`
3.  Run development server: `npm run dev`
4.  Open the browser at the provided URL.

## How it works
The app maps the Android-specific `MotionView` types to React components. It calculates the state of each view for the current frame, applying interpolations and effects as defined in the JSON.
