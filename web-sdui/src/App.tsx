import { useState, useEffect } from 'react';
import { MotionCanvas } from './components/MotionCanvas';
import type { MotionSDUI } from './infra/types';

const defaultSDUI: MotionSDUI = {
                                    "views": [
                                        {
                                            "type": "MultiLyricsContainer",
                                            "startFrame": 0,
                                            "endFrame": 873,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "songName": "Closer (feat. Halsey) - The Chainsmokers",
                                            "image": "https://yt3.googleusercontent.com/jvgMIjgbvnqnwLwjtqNa0euo9WStdIxrJnpQURgbwuPazT2OpZUdYPZe1gss2fK39oC8ITofFmeGxKY"
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 0,
                                            "endFrame": 41,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "I know it breaks your heart",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 41,
                                            "endFrame": 124,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Moved to the city in a broke-down car, and",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 124,
                                            "endFrame": 162,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Four years, no calls",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 162,
                                            "endFrame": 228,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Now you're looking pretty in a hotel bar",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 228,
                                            "endFrame": 335,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "And I-I-I can't stop",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 335,
                                            "endFrame": 457,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "No, I-I-I can't stop",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 457,
                                            "endFrame": 510,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "So, baby, pull me closer",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 510,
                                            "endFrame": 570,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "In the backseat of your Rover",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 570,
                                            "endFrame": 630,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "That I know you can't afford",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 630,
                                            "endFrame": 693,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Bite that tattoo on your shoulder",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 693,
                                            "endFrame": 754,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Pull the sheets right off the corner",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 754,
                                            "endFrame": 815,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "Of that mattress that you stole",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        },
                                        {
                                            "type": "PopUpTextView",
                                            "startFrame": 815,
                                            "endFrame": 873,
                                            "loop": {
                                                "start": 0,
                                                "end": 0
                                            },
                                            "text": "From your roommate back in Boulder",
                                            "writingSpeed": 1.5,
                                            "unwrittenTextAlpha": 0,
                                            "maxTranslationY": 50
                                        }
                                    ],
                                    "audios": [],
                                    "plugins": [],
                                    "config": {
                                        "aspectRatio": {
                                            "height": 854,
                                            "label": "9:16 SD",
                                            "width": 480
                                        },
                                        "fps": 24,
                                        "outputQuality": 100
                                    }
                                };

function App() {
  const [jsonInput, setJsonInput] = useState(JSON.stringify(defaultSDUI, null, 2));
  const [sdui, setSdui] = useState<MotionSDUI>(defaultSDUI);
  const [currentFrame, setCurrentFrame] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [maxFrames, setMaxFrames] = useState(100);

  useEffect(() => {
    try {
      const parsed = JSON.parse(jsonInput);
      setSdui(parsed);

      // Calculate max frames
      let max = 0;
      parsed.views.forEach((v: any) => {
        if (v.endFrame > max) max = v.endFrame;
      });
      setMaxFrames(max || 100);
    } catch (e) {
      // Invalid JSON
    }
  }, [jsonInput]);

  useEffect(() => {
    let interval: any;
    if (isPlaying) {
      interval = setInterval(() => {
        setCurrentFrame((prev) => (prev + 1) % (maxFrames + 1));
      }, 1000 / (sdui.config?.fps || 24));
    }
    return () => clearInterval(interval);
  }, [isPlaying, maxFrames, sdui.config?.fps]);

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif', backgroundColor: '#1a1a1a', color: '#fff' }}>
      {/* Editor Side */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', borderRight: '1px solid #333' }}>
        <div style={{ padding: '10px', backgroundColor: '#333', fontWeight: 'bold' }}>SDUI JSON Editor</div>
        <textarea
          style={{
            flex: 1,
            backgroundColor: '#2b2b2b',
            color: '#a9b7c6',
            padding: '10px',
            border: 'none',
            outline: 'none',
            fontSize: '14px',
            fontFamily: 'monospace',
            resize: 'none'
          }}
          value={jsonInput}
          onChange={(e) => setJsonInput(e.target.value)}
        />
      </div>

      {/* Preview Side */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
        <div style={{ width: '320px', marginBottom: '20px' }}>
          <MotionCanvas sdui={sdui} currentFrame={currentFrame} />
        </div>

        {/* Controls */}
        <div style={{ width: '100%', maxWidth: '400px', backgroundColor: '#333', padding: '20px', borderRadius: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '15px' }}>
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              style={{
                backgroundColor: isPlaying ? '#ff4757' : '#2ed573',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '6px',
                cursor: 'pointer',
                fontWeight: 'bold',
                width: '80px'
              }}
            >
              {isPlaying ? 'Pause' : 'Play'}
            </button>
            <div style={{ flex: 1, fontSize: '14px' }}>
              Frame: {currentFrame} / {maxFrames}
            </div>
          </div>
          <input
            type="range"
            min="0"
            max={maxFrames}
            value={currentFrame}
            onChange={(e) => setCurrentFrame(parseInt(e.target.value))}
            style={{ width: '100%', cursor: 'pointer' }}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
