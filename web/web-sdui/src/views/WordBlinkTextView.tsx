import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { interpolateForRange, Easing } from '../infra/interpolation';

export const WordBlinkTextView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const text = props.text || "";
  const startFrame = props.startFrame;
  const endFrame = props.endFrame;

  const words = text.split(" ");
  const wordCount = words.length;

  const progress = interpolateForRange(
    Easing.LINEAR,
    currentFrame,
    startFrame,
    endFrame,
    0,
    wordCount
  );

  const visibleWordIndex = Math.max(0, Math.floor(progress - 0.00001));
  const currentWord = words[visibleWordIndex] || "";

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
        color: 'white',
        textAlign: 'center',
        ...style,
      }}
    >
      {currentWord}
    </div>
  );
};
