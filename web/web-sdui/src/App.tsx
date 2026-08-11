import { useState, useEffect, useRef } from 'react';
import html2canvas from 'html2canvas';
import { MotionCanvas } from './components/MotionCanvas';
import JsonFinder from './components/JsonFinder';
import { UserProfileMenu } from './components/UserProfileMenu';
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
  const [isEncoding, setIsEncoding] = useState(false);
  const [encodeStatus, setEncodeStatus] = useState('');
  const previewRef = useRef<HTMLDivElement | null>(null);
  const frameWaiterRef = useRef<((value: void | PromiseLike<void>) => void) | null>(null);
  const [isMobile, setIsMobile] = useState(() => typeof window !== 'undefined' && window.innerWidth <= 768);
  const [isSheetExpanded, setIsSheetExpanded] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth <= 768;
      setIsMobile(mobile);
      if (!mobile) {
        setIsSheetExpanded(false);
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Pause playback when bottomsheet is minimised (collapsed)
  useEffect(() => {
    if (!isSheetExpanded) {
      setIsPlaying(false);
    }
  }, [isSheetExpanded]);

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
    if (isEncoding) return;
    setCurrentFrame(prev => Math.max(0, Math.min(maxFrames, prev + delta)));
  };

  useEffect(() => {
    if (frameWaiterRef.current) {
      frameWaiterRef.current();
      frameWaiterRef.current = null;
    }
  }, [currentFrame]);

  const setFrameAndWaitForRender = (frame: number) =>
    new Promise<void>((resolve) => {
      if (frame === currentFrame) {
        resolve();
        return;
      }
      frameWaiterRef.current = resolve;
      setCurrentFrame(frame);
    });

  const TRANSPARENT_PIXEL =
    'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==';

  const imageCache = useRef<Map<string, string>>(new Map()).current;

  const imageUrlToDataUrl = async (url: string): Promise<string> => {
    if (!url || url.startsWith('data:')) return url;
    if (imageCache.has(url)) return imageCache.get(url)!;

    try {
      const response = await fetch(url, { mode: 'cors' });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const blob = await response.blob();
      const dataUrl = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          if (typeof reader.result === 'string') resolve(reader.result);
          else reject(new Error('FileReader result is not string'));
        };
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(blob);
      });
      imageCache.set(url, dataUrl);
      return dataUrl;
    } catch (fetchErr) {
      console.warn('[VideoEncoder] CORS fetch failed for image, trying canvas fallback:', url, fetchErr);
      try {
        const dataUrl = await new Promise<string>((resolve, reject) => {
          const img = new Image();
          img.crossOrigin = 'anonymous';
          img.onload = () => {
            try {
              const offCanvas = document.createElement('canvas');
              offCanvas.width = img.naturalWidth || img.width || 100;
              offCanvas.height = img.naturalHeight || img.height || 100;
              const offCtx = offCanvas.getContext('2d');
              if (offCtx) {
                offCtx.drawImage(img, 0, 0);
                resolve(offCanvas.toDataURL('image/png'));
                return;
              }
            } catch (e) {
              reject(e);
            }
            resolve(TRANSPARENT_PIXEL);
          };
          img.onerror = () => resolve(TRANSPARENT_PIXEL);
          img.src = url;
        });
        imageCache.set(url, dataUrl);
        return dataUrl;
      } catch {
        imageCache.set(url, TRANSPARENT_PIXEL);
        return TRANSPARENT_PIXEL;
      }
    }
  };

  const convertCssUrls = async (cssValue: string): Promise<string> => {
    if (!cssValue || !cssValue.includes('url(')) return cssValue;
    const regex = /url\((['"]?)(.*?)\1\)/g;
    let match: RegExpExecArray | null;
    let result = cssValue;
    while ((match = regex.exec(cssValue)) !== null) {
      const fullMatch = match[0];
      const url = match[2];
      if (url && !url.startsWith('data:')) {
        const dataUrl = await imageUrlToDataUrl(url);
        result = result.replace(fullMatch, `url("${dataUrl}")`);
      }
    }
    return result;
  };

  const convertElementStyleUrls = async (element: HTMLElement) => {
    const styleProps = ['backgroundImage', 'maskImage', 'webkitMaskImage'] as const;
    for (const prop of styleProps) {
      const val = element.style[prop];
      if (val && val.includes('url(')) {
        element.style[prop] = await convertCssUrls(val);
      }
    }
  };

  const renderNodeToCanvas = async (node: HTMLElement, canvas: HTMLCanvasElement, width: number, height: number) => {
    const clonedNode = node.cloneNode(true) as HTMLElement;
    
    // Position cloned node off-screen with export dimensions for accurate rendering
    clonedNode.style.position = 'fixed';
    clonedNode.style.top = '-10000px';
    clonedNode.style.left = '-10000px';
    clonedNode.style.width = `${width}px`;
    clonedNode.style.height = `${height}px`;
    clonedNode.style.zIndex = '-9999';
    clonedNode.style.transform = 'none';
    document.body.appendChild(clonedNode);

    try {
      const clonedElements = Array.from(clonedNode.querySelectorAll<HTMLElement>('*'));
      
      // Convert background and mask image URLs on cloned elements
      await convertElementStyleUrls(clonedNode);
      for (const element of clonedElements) {
        await convertElementStyleUrls(element);
      }

      // Convert img src attributes to Data URLs
      const clonedImages = Array.from(clonedNode.querySelectorAll<HTMLImageElement>('img'));
      await Promise.all(clonedImages.map(async (image) => {
        const src = image.getAttribute('src');
        if (src && !src.startsWith('data:')) {
          const dataUrl = await imageUrlToDataUrl(src);
          image.setAttribute('src', dataUrl);
        }
        image.removeAttribute('crossorigin');
      }));

      // Wait for all images to complete loading
      await Promise.all(clonedImages.map(img => {
        return new Promise((resolve) => {
          if (img.complete && img.naturalWidth > 0) {
            resolve(null);
          } else {
            img.onload = () => resolve(null);
            img.onerror = () => resolve(null);
          }
        });
      }));

      const renderedCanvas = await html2canvas(clonedNode, {
        width,
        height,
        scale: 1,
        useCORS: true,
        allowTaint: false,
        backgroundColor: null,
        logging: false,
      });

      const ctx = canvas.getContext('2d');
      if (!ctx) {
        throw new Error('Could not create canvas context.');
      }
      ctx.clearRect(0, 0, width, height);
      ctx.drawImage(renderedCanvas, 0, 0, width, height);
    } finally {
      if (document.body.contains(clonedNode)) {
        document.body.removeChild(clonedNode);
      }
    }
  };

  const downloadIvf = (chunks: EncodedVideoChunk[], width: number, height: number, fps: number) => {
    const fourcc = 'VP80';
    const frameCount = chunks.length;
    const header = new ArrayBuffer(32);
    const dv = new DataView(header);
    dv.setUint8(0, 'D'.charCodeAt(0)); dv.setUint8(1, 'K'.charCodeAt(0)); dv.setUint8(2, 'I'.charCodeAt(0)); dv.setUint8(3, 'F'.charCodeAt(0));
    dv.setUint16(4, 0, true); dv.setUint16(6, 32, true);
    for (let i = 0; i < 4; i += 1) dv.setUint8(8 + i, fourcc.charCodeAt(i));
    dv.setUint16(12, width, true); dv.setUint16(14, height, true);
    dv.setUint32(16, fps, true); dv.setUint32(20, 1, true);
    dv.setUint32(24, frameCount, true); dv.setUint32(28, 0, true);

    const parts: BlobPart[] = [header];
    for (const chunk of chunks) {
      const frameHeader = new ArrayBuffer(12);
      const fh = new DataView(frameHeader);
      fh.setUint32(0, chunk.byteLength, true);
      const timestamp = BigInt(Math.round(chunk.timestamp / 1000));
      fh.setUint32(4, Number(timestamp & BigInt(0xffffffff)), true);
      fh.setUint32(8, Number((timestamp >> BigInt(32)) & BigInt(0xffffffff)), true);
      const chunkData = new Uint8Array(chunk.byteLength);
      chunk.copyTo(chunkData);
      parts.push(frameHeader, chunkData);
    }

    const blob = new Blob(parts, { type: 'video/x-ivf' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `motion-${Date.now()}.ivf`;
    a.click();
    URL.revokeObjectURL(a.href);
  };

  const handleGenerateVideo = async () => {
    console.info('[VideoEncoder] Export requested', {
      currentFrame,
      maxFrames,
      fps: sdui?.config?.fps || 24
    });
    if (!('VideoEncoder' in window)) {
      setEncodeStatus('VideoEncoder is not supported in this browser.');
      console.warn('[VideoEncoder] Browser does not support VideoEncoder API');
      return;
    }
    if (!previewRef.current) {
      setEncodeStatus('Preview element is not available.');
      console.warn('[VideoEncoder] Preview ref is unavailable');
      return;
    }
    setIsEncoding(true);
    setIsPlaying(false);
    setEncodeStatus('Starting encoding...');
    const fps = sdui?.config?.fps || 24;
    const width = sdui?.config?.aspectRatio?.width || 480;
    const height = sdui?.config?.aspectRatio?.height || 854;
    const exportCanvas = document.createElement('canvas');
    exportCanvas.width = width;
    exportCanvas.height = height;
    const chunks: EncodedVideoChunk[] = [];
    const encoder = new VideoEncoder({
      output: (chunk) => {
        chunks.push(chunk);
        if (chunks.length % Math.max(1, Math.floor(fps / 2)) === 0) {
          console.debug('[VideoEncoder] Chunk emitted', {
            chunkCount: chunks.length,
            timestampUs: chunk.timestamp,
            byteLength: chunk.byteLength,
            type: chunk.type
          });
        }
      },
      error: (error) => {
        console.error('[VideoEncoder] Encoder callback error', error);
        setEncodeStatus(`Encoding error: ${error.message}`);
      }
    });
    try {
      console.info('[VideoEncoder] Configuring encoder', { codec: 'vp8', width, height, fps });
      encoder.configure({ codec: 'vp8', width, height, bitrate: 4_000_000, framerate: fps });
      for (let frame = 0; frame <= maxFrames; frame += 1) {
        await setFrameAndWaitForRender(frame);
        setEncodeStatus(`Encoding frame ${frame + 1}/${maxFrames + 1}...`);
        console.debug('[VideoEncoder] Frame render synchronized', {
          frame,
          previewReady: Boolean(previewRef.current),
          pendingQueue: encoder.encodeQueueSize
        });
        await renderNodeToCanvas(previewRef.current, exportCanvas, width, height);
        console.debug('[VideoEncoder] Frame rendered to canvas', {
          frame,
          canvasWidth: exportCanvas.width,
          canvasHeight: exportCanvas.height
        });
        const videoFrame = new VideoFrame(exportCanvas, { timestamp: Math.round((frame / fps) * 1_000_000) });
        encoder.encode(videoFrame, { keyFrame: frame % fps === 0 });
        videoFrame.close();
      }
      console.info('[VideoEncoder] Waiting for flush', { encodeQueueSize: encoder.encodeQueueSize });
      await encoder.flush();
      downloadIvf(chunks, width, height, fps);
      setEncodeStatus(`Done. Downloaded ${chunks.length} encoded frames as IVF.`);
      console.info('[VideoEncoder] Export complete', { chunkCount: chunks.length, width, height, fps });
    } catch (error: any) {
      console.error('[VideoEncoder] Export failed', error);
      setEncodeStatus(`Failed: ${error?.message || 'Unknown error'}`);
    } finally {
      encoder.close();
      console.info('[VideoEncoder] Encoder closed');
      setIsEncoding(false);
    }
  };

  const renderPlayerControls = () => (
    <div style={{
      width: '100%',
      backgroundColor: '#0e0e11',
      padding: '20px',
      borderRadius: '16px',
      border: '1px solid rgba(255,255,255,0.08)',
      boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
      boxSizing: 'border-box'
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
          disabled={isEncoding}
          style={{ width: '100%', cursor: 'pointer', margin: 0 }}
        />
      </div>

      {/* Action Buttons */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '16px' }}>
        {/* Step Back */}
        <button className="player-btn" onClick={() => skipFrames(-1)} title="Previous Frame (Left Arrow)" disabled={isEncoding}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
        </button>

        {/* Play/Pause */}
        <button
          className="player-btn primary"
          onClick={() => setIsPlaying(!isPlaying)}
          disabled={isEncoding}
          style={{ width: '48px', height: '48px' }}
        >
          {isPlaying ? (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
          ) : (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" style={{ marginLeft: '2px' }}><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
          )}
        </button>

        {/* Step Forward */}
        <button className="player-btn" onClick={() => skipFrames(1)} title="Next Frame (Right Arrow)" disabled={isEncoding}>
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
      <div style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
        <button
          onClick={handleGenerateVideo}
          disabled={isEncoding}
          style={{
            width: '100%',
            border: 0,
            borderRadius: '12px',
            padding: '12px 14px',
            fontWeight: 700,
            fontSize: '13px',
            letterSpacing: '0.2px',
            color: '#fff',
            cursor: isEncoding ? 'not-allowed' : 'pointer',
            background: isEncoding
              ? 'linear-gradient(90deg, #4b5563 0%, #374151 100%)'
              : 'linear-gradient(90deg, #7c3aed 0%, #2563eb 100%)',
            boxShadow: isEncoding ? 'none' : '0 8px 24px rgba(59,130,246,0.35)',
            transition: 'all 180ms ease'
          }}
        >
          {isEncoding ? `Generating… ${currentFrame + 1}/${maxFrames + 1}` : 'Generate Video'}
        </button>
        {encodeStatus && <div style={{ fontSize: '12px', color: '#9898b0', textAlign: 'center' }}>{encodeStatus}</div>}
      </div>
    </div>
  );

  return (
    <div className="app-layout">
      {/* Editor Side */}
      <div className="editor-pane">
        <JsonFinder
          initialJson={JSON.stringify(defaultSDUI, null, 2)}
          onUpdate={handleJsonUpdate}
          headerActions={<UserProfileMenu />}
        />
      </div>

      {/* Preview Side */}
      {isMobile ? (
        <>
          <div
            className={`bottom-sheet-backdrop ${isSheetExpanded ? 'visible' : ''}`}
            onClick={() => setIsSheetExpanded(false)}
          />
          <div className={`preview-pane mobile-bottom-sheet ${isSheetExpanded ? 'expanded' : 'collapsed'}`}>
            <div className="bottom-sheet-header" onClick={() => setIsSheetExpanded(!isSheetExpanded)}>
              <div className="bottom-sheet-drag-handle" />
              <div className="bottom-sheet-header-content">
                <div className="bottom-sheet-title-area">
                  <span style={{ fontSize: '15px' }}>🎬</span>
                  <span className="bottom-sheet-title">Video Preview & Controls</span>
                  <span style={{ fontSize: '11px', color: '#6ba4ff', background: 'rgba(107,164,255,0.12)', padding: '2px 8px', borderRadius: '10px', marginLeft: '4px', fontFamily: 'monospace' }}>
                    {currentFrame}F / {maxFrames}F
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <button
                    className="player-btn primary"
                    onClick={(e) => {
                      e.stopPropagation();
                      setIsPlaying(!isPlaying);
                    }}
                    style={{ width: '32px', height: '32px', padding: 0 }}
                  >
                    {isPlaying ? (
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
                    ) : (
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style={{ marginLeft: '1px' }}><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                    )}
                  </button>
                  <button className="player-btn" style={{ width: '28px', height: '28px', color: '#9898b0' }}>
                    {isSheetExpanded ? (
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    ) : (
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="18 15 12 9 6 15"></polyline></svg>
                    )}
                  </button>
                </div>
              </div>
            </div>

            <div className="bottom-sheet-body">
              <div style={{ width: '100%', maxWidth: '280px', marginBottom: '20px' }}>
                {sdui && <MotionCanvas sdui={sdui} currentFrame={currentFrame} previewRef={previewRef} />}
              </div>

              {renderPlayerControls()}
            </div>
          </div>
        </>
      ) : (
        <div className="preview-pane">
          <div style={{ width: '100%', maxWidth: '320px', marginBottom: '32px', marginTop: '24px' }}>
            {sdui && <MotionCanvas sdui={sdui} currentFrame={currentFrame} previewRef={previewRef} />}
          </div>

          {renderPlayerControls()}
        </div>
      )}
    </div>
  );
}

export default App;
