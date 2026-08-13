import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { MotionViewRenderer } from './ViewRegistry';

export const VerticalStackMotionView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const sections = props.sections || [];

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        ...style,
      }}
    >
      {sections.map((section: any, index: number) => (
        <div key={index} style={{ height: `${section.percentage}%`, width: '100%', position: 'relative' }}>
          <MotionViewRenderer props={section.view} currentFrame={currentFrame} config={config} />
        </div>
      ))}
    </div>
  );
};
