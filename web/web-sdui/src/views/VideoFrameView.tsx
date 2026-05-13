import React, { useEffect, useRef } from 'react';
import type { MotionViewProps } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const VideoFrameView: React.FC<{ props: MotionViewProps; currentFrame: number }> = ({ props, currentFrame }) => {
  const style = useApplyEffects(props, currentFrame);
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    if (videoRef.current) {
      // Approximate frame to time
      // Assuming 24fps if not specified in config (though config should be available globally)
      const fps = 24;
      videoRef.current.currentTime = currentFrame / fps;
    }
  }, [currentFrame]);

  return (
    <video
      ref={videoRef}
      src={props.videoUri}
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        objectFit: 'cover',
        ...style,
      }}
      muted
      playsInline
    />
  );
};
