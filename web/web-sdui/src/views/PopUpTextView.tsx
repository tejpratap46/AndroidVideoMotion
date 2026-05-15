import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { interpolateForRange, Easing } from '../infra/interpolation';

export const PopUpTextView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const text = props.text || "";
  const writingSpeed = props.writingSpeed || 1;
  const startFrame = props.startFrame;
  const endFrame = props.endFrame;
  const unwrittenTextAlpha = props.unwrittenTextAlpha ?? 0;
  const maxTranslationY = props.maxTranslationY ?? 50;

  const words = text.split(" ");
  const wordCount = words.length;

  const inferredEndFrame = endFrame !== -1 && writingSpeed > 0
    ? startFrame + (endFrame - startFrame) / writingSpeed
    : endFrame;

  const progress = interpolateForRange(
    Easing.LINEAR,
    currentFrame,
    startFrame,
    inferredEndFrame,
    0,
    wordCount
  );

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
        fontSize: '2rem',
        color: 'white',
        textAlign: 'center',
        padding: '0 20px',
        boxSizing: 'border-box',
        ...style,
      }}
    >
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
        {words.map((word: string, index: number) => {
          const wordProgress = Math.max(0, Math.min(1, progress - index));
          const alpha = unwrittenTextAlpha + (1 - unwrittenTextAlpha) * wordProgress;
          const translateY = maxTranslationY * (1 - wordProgress);

          return (
            <span
              key={index}
              style={{
                display: 'inline-block',
                marginRight: '0.25em',
                opacity: alpha,
                transform: `translateY(${translateY}px)`,
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
