---
name: create-new-motion-view
description: create a new MotionView, either for Template or otherwise, make sure to map tio SDUI.
---

# How to Create a New MotionView

This guide describes the process of creating a new `MotionView`, adding it to the Template DSL, and registering it for Server-Driven UI (SDUI).

---

## 1. Create the MotionView Class

Create your new view class in the `motionlib` module. It should typically extend `BaseContourMotionView` or a more specific abstract class like `AbstractMotionTextView`.

**Example: `RainbowPopUpTextView.kt`**
Location: `modules/motionlib/src/main/java/com/tejpratapsingh/motionlib/ui/custom/text/`

```kotlin
class RainbowPopUpTextView(
    context: Context,
    text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    // ... other properties
) : AbstractMotionTextView(...) {

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        // 1. Calculate progress based on frame
        // 2. Update view state (e.g., apply Spans to TextView)
        // 3. Invalidate view if necessary
        return this
    }
}
```

## 2. Add Template DSL Extension

To make the new view easy to use in templates, add an extension function to `ContentScope` in the `templates` module.

**Example: `TextExtensions.kt`**
Location: `modules/templates/src/main/java/com/tejpratapsingh/motionlib/templates/extensions/`

```kotlin
fun ContentScope.rainbowPopUpTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    // ... other params
) = RainbowPopUpTextView(context, text, startFrame, endFrame, ...)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
```

## 3. Register for SDUI

To enable JSON serialization/deserialization, register the view in `MotionSduiInitializer`.

**Example: `MotionSduiInitializer.kt`**
Location: `modules/sdui/src/main/java/com/tejpratapsingh/motion/sdui/infra/`

### Deserializer (JSON -> Object)
```kotlin
MotionSdui.registerView(RainbowPopUpTextView::class.java.simpleName) { context, json ->
    val props = json.parseMotionViewProps()
    val text = json.get("text")?.asString ?: ""
    // ... parse other custom fields from json
    RainbowPopUpTextView(
        context = context,
        text = text,
        startFrame = props.startFrame,
        endFrame = props.endFrame,
        // ...
    )
}
```

### Serializer (Object -> JSON)
```kotlin
MotionSdui.registerViewSerializer(RainbowPopUpTextView::class.java) { view, json ->
    json.addProperty("type", view.javaClass.simpleName)
    json.addProperty("text", view.text)
    // ... add other custom properties to json
}
```

---

## Summary Checklist
1.  [ ] Define view in `motionlib`.
2.  [ ] Add DSL extension in `templates`.
3.  [ ] Register deserializer in `MotionSduiInitializer`.
4.  [ ] Register serializer in `MotionSduiInitializer`.
