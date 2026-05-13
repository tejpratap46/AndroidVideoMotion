import React from 'react';
import type { MotionViewProps } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const TransparentTextView: React.FC<{ props: MotionViewProps; currentFrame: number }> = ({ props, currentFrame }) => {
  const style = useApplyEffects(props, currentFrame);

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
        ...style,
      }}
    >
      {props.text}
    </div>
  );
};
