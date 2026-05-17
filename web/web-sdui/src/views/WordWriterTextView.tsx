import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const WordWriterTextView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const text = props.text || "";
  const writingSpeed = props.writingSpeed || 1;
  const startFrame = props.startFrame;

  // Calculate how many characters to show
  const elapsedFrames = currentFrame - startFrame;
  const charsToShow = Math.floor(elapsedFrames * writingSpeed);

  const visibleText = text.substring(0, charsToShow);
  const hiddenText = text.substring(charsToShow);

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
        ...style,
      }}
    >
      <span>
        {visibleText}
        <span style={{ opacity: props.unwrittenTextAlpha || 0 }}>{hiddenText}</span>
      </span>
    </div>
  );
};
