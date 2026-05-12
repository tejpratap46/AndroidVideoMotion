import { useState, useEffect } from 'react';
import { MotionCanvas } from './components/MotionCanvas';
import JsonFinder from './components/JsonFinder';
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
  const [sdui, setSdui] = useState<MotionSDUI>(defaultSDUI);
  const [currentFrame, setCurrentFrame] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [maxFrames, setMaxFrames] = useState(100);

  useEffect(() => {
    if (sdui && sdui.views) {
      let max = 0;
      sdui.views.forEach((v: any) => {
        if (v.endFrame > max) max = v.endFrame;
      });
      setMaxFrames(max || 100);
    }
  }, [sdui]);

  useEffect(() => {
    let interval: any;
    if (isPlaying) {
      interval = setInterval(() => {
        setCurrentFrame((prev) => (prev + 1) % (maxFrames + 1));
      }, 1000 / (sdui?.config?.fps || 24));
    }
    return () => clearInterval(interval);
  }, [isPlaying, maxFrames, sdui?.config?.fps]);

  const handleJsonUpdate = (data: any) => {
    if (data && typeof data === 'object') {
      setSdui(data);
    }
  };

  const skipFrames = (delta: number) => {
    setCurrentFrame(prev => Math.max(0, Math.min(maxFrames, prev + delta)));
  };

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif', backgroundColor: '#111114', color: '#fff', overflow: 'hidden' }}>
      {/* Editor Side */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', borderRight: '1px solid rgba(255,255,255,0.1)' }}>
        <JsonFinder
          initialJson={JSON.stringify(defaultSDUI, null, 2)}
          onUpdate={handleJsonUpdate}
        />
      </div>

      {/* Preview Side */}
      <div style={{ width: '450px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '24px', backgroundColor: '#161619' }}>
        <div style={{ width: '100%', maxWidth: '320px', marginBottom: '32px' }}>
          {sdui && <MotionCanvas sdui={sdui} currentFrame={currentFrame} />}
        </div>

        {/* Professional Player Controls */}
        <div style={{
          width: '100%',
          backgroundColor: '#0e0e11',
          padding: '24px',
          borderRadius: '16px',
          border: '1px solid rgba(255,255,255,0.08)',
          boxShadow: '0 8px 32px rgba(0,0,0,0.4)'
        }}>
          {/* Seek Bar */}
          <div style={{ marginBottom: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: '#58586a', marginBottom: '8px', fontFamily: 'monospace' }}>
              <span>{currentFrame}F</span>
              <span>{maxFrames}F</span>
            </div>
            <input
              type="range"
              min="0"
              max={maxFrames}
              value={currentFrame}
              onChange={(e) => setCurrentFrame(parseInt(e.target.value))}
              style={{ width: '100%', cursor: 'pointer', margin: 0 }}
            />
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '16px' }}>
            {/* Step Back */}
            <button className="player-btn" onClick={() => skipFrames(-1)} title="Previous Frame (Left Arrow)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
            </button>

            {/* Play/Pause */}
            <button
              className="player-btn primary"
              onClick={() => setIsPlaying(!isPlaying)}
              style={{ width: '48px', height: '48px' }}
            >
              {isPlaying ? (
                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
              ) : (
                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" style={{ marginLeft: '2px' }}><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
              )}
            </button>

            {/* Step Forward */}
            <button className="player-btn" onClick={() => skipFrames(1)} title="Next Frame (Right Arrow)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
            </button>
          </div>

          {/* Meta Info */}
          <div style={{ marginTop: '20px', borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: '16px', display: 'flex', justifyContent: 'center', gap: '24px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '10px', color: '#58586a', fontWeight: 'bold', marginBottom: '4px' }}>FPS</div>
              <div style={{ fontSize: '13px', color: '#9898b0' }}>{sdui?.config?.fps || 24}</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '10px', color: '#58586a', fontWeight: 'bold', marginBottom: '4px' }}>DURATION</div>
              <div style={{ fontSize: '13px', color: '#9898b0' }}>{((maxFrames) / (sdui?.config?.fps || 24)).toFixed(1)}s</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
