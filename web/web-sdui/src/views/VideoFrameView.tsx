import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { getAssetUri } from '../infra/assetUtils';

export const VideoFrameView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const videoUri = getAssetUri(props.asset) || props.videoUri || "";

  // In a real implementation, we would seek the video to the current frame.
  // For SDUI preview, we might just show a placeholder or the first frame.
  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: '#333',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white',
        ...style,
      }}
    >
      Video: {videoUri}
    </div>
  );
};
