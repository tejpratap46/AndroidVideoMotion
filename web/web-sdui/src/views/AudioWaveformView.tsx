import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const AudioWaveformView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const amplitudes: number[] = props.amplitudes || [];

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        ...style,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-end', gap: '2px', height: '100%' }}>
        {amplitudes.slice(0, 50).map((amp, i) => (
          <div
            key={i}
            style={{
              width: '4px',
              height: `${amp * 200}%`,
              backgroundColor: '#2ed573',
              borderRadius: '2px',
            }}
          />
        ))}
      </div>
    </div>
  );
};
