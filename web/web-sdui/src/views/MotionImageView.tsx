import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { getAssetUri } from '../infra/assetUtils';

export const MotionImageView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const imageUri = getAssetUri(props.asset) || props.imageUri || "";
  const isCircular = props.type === 'CircularMotionImageView';

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        overflow: 'hidden',
        ...style,
        clipPath: isCircular ? 'circle(50% at 50% 50%)' : 'none',
      }}
    >
      {imageUri && (
        <img
          src={imageUri}
          crossOrigin="anonymous"
          referrerPolicy="no-referrer"
          alt="Motion Image"
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
          }}
        />
      )}
    </div>
  );
};
