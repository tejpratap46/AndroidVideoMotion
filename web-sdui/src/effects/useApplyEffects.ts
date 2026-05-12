import type { MotionViewProps } from '../infra/types';
import { interpolateForRange, Easing } from '../infra/interpolation';

export const useApplyEffects = (viewProps: MotionViewProps, currentFrame: number): React.CSSProperties => {
  const style: React.CSSProperties = {};

  if (!viewProps.effects) return style;

  viewProps.effects.forEach(effect => {
    if (currentFrame < effect.startFrame || currentFrame > effect.endFrame) return;

    if (effect.type === 'SlideRightToLeftEffect') {
      // We need aspect ratio width here. Let's assume a default or pass it.
      // For now, let's use 100% as a proxy if we don't have absolute pixels yet.
      // In the Android app, it uses aspectRatio.width.
      const progress = interpolateForRange(
        Easing.LINEAR,
        currentFrame,
        effect.startFrame,
        effect.endFrame,
        0,
        1
      );

      // If we are in a container, translateX should move it.
      style.transform = `${style.transform || ''} translateX(${-progress * 100}%)`;
    }

    // Add more effects here...
  });

  return style;
};
