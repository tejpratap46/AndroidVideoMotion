import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

const FakeAudioChartView: React.FC<{ frame: number }> = ({ frame }) => {
  const bars = 8;
  const barWidth = 10;
  const barSpacing = 5;
  const animationSpeed = 0.3;

  return (
    <div style={{ display: 'flex', gap: `${barSpacing}px`, alignItems: 'center', height: '30px' }}>
      {Array.from({ length: bars }).map((_, i) => {
        const base = 0.5 + 0.5 * Math.sin((frame * animationSpeed + i) * 2.0);
        const finalAmp = Math.max(0.1, Math.min(1, base));
        const height = finalAmp * 100;

        return (
          <div
            key={i}
            style={{
              width: `${barWidth}px`,
              height: `${height}%`,
              backgroundColor: 'white',
              borderRadius: `${barWidth / 2}px`,
              transition: 'height 0.1s linear'
            }}
          />
        );
      })}
    </div>
  );
};

export const MultiLyricsContainer: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'black',
        overflow: 'hidden',
        ...style,
      }}
    >
      {/* Album Art Background */}
      {props.image && (
        <img
          src={props.image}
          crossOrigin="anonymous"
          referrerPolicy="no-referrer"
          alt="Album Art"
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            opacity: 0.3,
          }}
        />
      )}

      {/* Audio Chart View */}
      <div
        style={{
          position: 'absolute',
          bottom: '15%',
          left: 0,
          right: 0,
          display: 'flex',
          justifyContent: 'center'
        }}
      >
        <FakeAudioChartView frame={currentFrame} />
      </div>
    </div>
  );
};
