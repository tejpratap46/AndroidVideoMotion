import React from 'react';
import type { MotionViewProps } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const GradientView: React.FC<{ props: MotionViewProps; currentFrame: number }> = ({ props, currentFrame }) => {
  const style = useApplyEffects(props, currentFrame);

  const orientation = props.orientation || 'VERTICAL';
  const colors = props.colors || ['#000000', '#FFFFFF'];
  const colorsStr = colors.join(', ');

  let background = '';
  if (orientation === 'VERTICAL') {
    background = `linear-gradient(to bottom, ${colorsStr})`;
  } else if (orientation === 'HORIZONTAL') {
    background = `linear-gradient(to right, ${colorsStr})`;
  } else if (orientation === 'CIRCULAR') {
    background = `radial-gradient(circle at center, ${colorsStr})`;
  }

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        background,
        ...style,
      }}
    />
  );
};
