import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const GradientView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const colors = props.colors || ['#000000', '#ffffff'];
  const orientation = props.orientation || 'VERTICAL';

  const gradientAngle = orientation === 'VERTICAL' ? '180deg' : '90deg';
  const background = `linear-gradient(${gradientAngle}, ${colors.join(', ')})`;

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
