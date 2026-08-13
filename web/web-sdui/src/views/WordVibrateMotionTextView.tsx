import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const WordVibrateMotionTextView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const text = props.text || "";
  const amplitude = props.amplitude || 5;
  const frequency = props.frequency || 0.5;
  const phaseShiftPerWord = props.phaseShiftPerWord || 1.0;

  const words = text.split(" ");

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
        fontSize: '3rem',
        fontWeight: 'bold',
        color: props.textColor || 'white',
        textAlign: 'center',
        whiteSpace: 'pre-wrap',
        ...style,
      }}
    >
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
        {words.map((word, index) => {
          const phase = index * phaseShiftPerWord;
          // Matching the Android implementation:
          // val offsetX = sin(frame.toDouble() * frequency + phase).toFloat() * amplitude
          // val offsetY = sin(frame.toDouble() * frequency + phase + 0.5).toFloat() * (amplitude / 2f)
          const offsetX = Math.sin(currentFrame * frequency + phase) * amplitude;
          const offsetY = Math.sin(currentFrame * frequency + phase + 0.5) * (amplitude / 2);

          return (
            <span
              key={index}
              style={{
                display: 'inline-block',
                transform: `translate(${offsetX}px, ${offsetY}px)`,
                marginRight: '0.25em',
              }}
            >
              {word}
            </span>
          );
        })}
      </div>
    </div>
  );
};
