import type { MotionViewProps, MotionConfig } from '../infra/types';
import { interpolateForRange, Easing } from '../infra/interpolation';

export const useApplyEffects = (viewProps: MotionViewProps, currentFrame: number, config: MotionConfig): React.CSSProperties => {
  const style: React.CSSProperties = {
    transition: 'none', // Disable CSS transitions to ensure frame-perfect rendering
  };

  if (!viewProps.effects) return style;

  const width = config.aspectRatio.width;
  const height = config.aspectRatio.height;

  viewProps.effects.forEach(effect => {
    if (currentFrame < effect.startFrame || currentFrame > effect.endFrame) return;

    const progress = interpolateForRange(
      Easing.LINEAR,
      currentFrame,
      effect.startFrame,
      effect.endFrame,
      0,
      1
    );

    if (effect.type === 'SlideRightToLeftEffect') {
      style.transform = `${style.transform || ''} translateX(${-progress * 100}%)`;
    } else if (effect.type === 'SlideLeftToRightEffect') {
      style.transform = `${style.transform || ''} translateX(${(progress - 1) * 100}%)`;
    } else if (effect.type === 'SlideTopToBottomEffect') {
      style.transform = `${style.transform || ''} translateY(${(progress - 1) * 100}%)`;
    } else if (effect.type === 'SlideBottomToTopEffect') {
      style.transform = `${style.transform || ''} translateY(${(1 - progress) * 100}%)`;
    } else if (effect.type === 'ZoomInEffect') {
      const startScale = effect.startScale ?? 1;
      const endScale = effect.endScale ?? 2;
      const scale = startScale + (endScale - startScale) * progress;
      style.transform = `${style.transform || ''} scale(${scale})`;
    } else if (effect.type === 'ZoomOutEffect') {
      const startScale = effect.startScale ?? 2;
      const endScale = effect.endScale ?? 1;
      const scale = startScale + (endScale - startScale) * progress;
      style.transform = `${style.transform || ''} scale(${scale})`;
    } else if (effect.type === 'FadeInEffect') {
      style.opacity = progress;
    } else if (effect.type === 'FadeOutEffect') {
      style.opacity = 1 - progress;
    } else if (effect.type === 'BlurEffect') {
      const maxBlurRadius = effect.maxBlurRadius ?? 20;
      style.filter = `${style.filter || ''} blur(${progress * maxBlurRadius}px)`;
    } else if (effect.type === 'GlitchEffect') {
      const intensity = effect.intensity ?? 10;
      const jitterX = (Math.random() * 2 - 1) * intensity;
      const jitterY = (Math.random() * 2 - 1) * intensity;
      style.transform = `${style.transform || ''} translate(${jitterX}px, ${jitterY}px)`;
      if (Math.random() > 0.8) {
        style.opacity = Math.random() * 0.5 + 0.5;
      }
    } else if (effect.type === 'VibrateEffect') {
      const amplitude = effect.amplitude ?? 5;
      const frequency = effect.frequency ?? 1;
      const offset = Math.sin(currentFrame * frequency) * amplitude;
      style.transform = `${style.transform || ''} translate(${offset}px, ${offset / 2}px)`;
    } else if (effect.type === 'SlideEffect') {
      const fromX = effect.fromX;
      const toX = effect.toX;
      const fromY = effect.fromY;
      const toY = effect.toY;

      if (fromX !== undefined && toX !== undefined) {
        const tx = fromX + (toX - fromX) * progress;
        style.transform = `${style.transform || ''} translateX(${tx}px)`;
      }
      if (fromY !== undefined && toY !== undefined) {
        const ty = fromY + (toY - fromY) * progress;
        style.transform = `${style.transform || ''} translateY(${ty}px)`;
      }
    }
  });

  return style;
};
