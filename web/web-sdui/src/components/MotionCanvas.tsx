import React from 'react';
import type { MotionSDUI } from '../infra/types';
import { MotionViewRenderer } from '../views/ViewRegistry';

interface MotionCanvasProps {
  sdui: MotionSDUI;
  currentFrame: number;
  previewRef?: React.RefObject<HTMLDivElement | null>;
}

export const MotionCanvas: React.FC<MotionCanvasProps> = ({ sdui, currentFrame, previewRef }) => {
  const config = sdui.config || {
    aspectRatio: { width: 1080, height: 1920, label: "9:16 Full HD" },
    fps: 24,
    outputQuality: 100
  };

  const { width, height } = config.aspectRatio;

  return (
    <div
      style={{
        position: 'relative',
        width: '100%',
        paddingBottom: `${(height / width) * 100}%`,
        background: '#000',
        overflow: 'hidden',
        boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
        borderRadius: '8px',
      }}
    >
      <div
        ref={previewRef}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
        }}
      >
        {sdui.views.map((view, index) => (
          <MotionViewRenderer key={index} props={view} currentFrame={currentFrame} />
        ))}
      </div>
    </div>
  );
};
