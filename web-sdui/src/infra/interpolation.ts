export const Easing = {
  LINEAR: 'LINEAR',
  EASE_IN: 'EASE_IN',
  EASE_OUT: 'EASE_OUT',
  EASE_IN_OUT: 'EASE_IN_OUT',
} as const;

export type EasingType = typeof Easing[keyof typeof Easing];

export const getInterpolation = (easing: EasingType, t: number): number => {
  switch (easing) {
    case Easing.EASE_IN:
      return t * t;
    case Easing.EASE_OUT:
      return t * (2 - t);
    case Easing.EASE_IN_OUT:
      return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    case Easing.LINEAR:
    default:
      return t;
  }
};

export const interpolateForRange = (
  easing: EasingType,
  currentFrame: number,
  startFrame: number,
  endFrame: number,
  startValue: number,
  endValue: number
): number => {
  if (currentFrame <= startFrame) return startValue;
  if (currentFrame >= endFrame) return endValue;

  const framePercent = (currentFrame - startFrame) / (endFrame - startFrame);
  const interpolatedFramePercent = getInterpolation(easing, framePercent);

  return startValue + interpolatedFramePercent * (endValue - startValue);
};

export const interpolateColorForRange = (
  easing: EasingType,
  currentFrame: number,
  startFrame: number,
  endFrame: number,
  startColor: string,
  endColor: string
): string => {
  if (currentFrame <= startFrame) return startColor;
  if (currentFrame >= endFrame) return endColor;

  const framePercent = (currentFrame - startFrame) / (endFrame - startFrame);
  const interpolatedFramePercent = getInterpolation(easing, framePercent);

  // Simple HEX to RGB and back for color interpolation
  const hexToRgb = (hex: string) => {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : { r: 0, g: 0, b: 0 };
  };

  const rgbToHex = (r: number, g: number, b: number) => {
    return "#" + ((1 << 24) + (Math.round(r) << 16) + (Math.round(g) << 8) + Math.round(b)).toString(16).slice(1);
  };

  const c1 = hexToRgb(startColor);
  const c2 = hexToRgb(endColor);

  const r = c1.r + (c2.r - c1.r) * interpolatedFramePercent;
  const g = c1.g + (c2.g - c1.g) * interpolatedFramePercent;
  const b = c1.b + (c2.b - c1.b) * interpolatedFramePercent;

  return rgbToHex(r, g, b);
};
