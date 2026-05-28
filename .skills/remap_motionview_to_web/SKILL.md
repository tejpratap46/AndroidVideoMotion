---
name: map-view-and-effects-to-web-ui
description: map motionview or motioneffect to support rendering in web-sdui
---

# How to Remap MotionView and Effects from SDUI to Web (ReactJS)

This guide describes the process of mapping existing Android `MotionView` and `MotionEffect` components to their web equivalents in the `web-sdui` ReactJS implementation.

---

## 1. Identify the Android Serialization

Find how the Android view or effect is serialized to JSON. This is usually located in `MotionSduiInitializer.kt`.

**Location:** `modules/sdui/src/main/java/com/tejpratapsingh/motion/sdui/infra/`

### For Views:
Look for `registerViewSerializer`:
```kotlin
MotionSdui.registerViewSerializer(RainbowPopUpTextView::class.java) { view, json ->
    json.addProperty("type", view.javaClass.simpleName)
    json.addProperty("text", view.text)
    // Note these properties for the Web implementation
}
```

### For Effects:
Look for `registerEffectSerializer`:
```kotlin
MotionSdui.registerEffectSerializer(ZoomInEffect::class.java) { effect, json ->
    json.addProperty("type", effect.javaClass.simpleName)
    json.addProperty("startScale", effect.startScale)
    json.addProperty("endScale", effect.endScale)
}
```

---

## 2. Create/Update the Web Implementation

### Mapping a View
Create a new `.tsx` file in `web/web-sdui/src/views/`.

```tsx
import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';

export const MyNewView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  // Apply common effects and layout
  const style = useApplyEffects(props, currentFrame, config);

  return (
    <div style={{ position: 'absolute', ...style }}>
      {/* View Content */}
    </div>
  );
};
```

### Mapping an Effect
If you are adding a new effect, you must add it to the `useApplyEffects` hook.

**Location:** `web/web-sdui/src/effects/useApplyEffects.ts`

```tsx
// Inside useApplyEffects loop:
if (effect.type === 'MyNewEffect') {
  const intensity = effect.intensity ?? 1;
  // Calculate style based on progress (0 to 1)
  style.filter = `${style.filter || ''} sepia(${progress * intensity})`;
}
```

---

## 3. Register the Component/Effect

### For Views:
Register in `web/web-sdui/src/views/ViewRegistry.tsx`.

```tsx
export const ViewRegistry: Record<string, React.FC<...>> = {
  MyNewView: MyNewView, // Key must match Android "type"
};
```

### For Effects:
Effects are automatically handled if added to `useApplyEffects.ts`. Ensure the `type` check matches the Android serializer.

---

## 4. Helper: Interpolation

Use `interpolateForRange` for frame-based animations.

```tsx
import { interpolateForRange, Easing } from '../infra/interpolation';

const progress = interpolateForRange(
  Easing.LINEAR,
  currentFrame,
  startFrame,
  endFrame,
  0, // Start value
  1  // End value
);
```

---

## Summary Checklist
1.  [ ] Check Android serializer for property keys (View or Effect).
2.  [ ] For **Views**: Create `.tsx` and register in `ViewRegistry.tsx`.
3.  [ ] For **Effects**: Add logic to `useApplyEffects.ts`.
4.  [ ] Use `useApplyEffects(props, ...)` in the View's root element.
