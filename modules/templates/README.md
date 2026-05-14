# Motion Template System

The `templates` module provides a way to define `MotionView` structures using JSON templates with placeholders. This allows for decoupling the design of a motion view from the dynamic data it displays.

## Core Components

- **`MotionTemplate`**: Holds the SDUI JSON definition with `{{placeholder}}` markers.
- **`MotionTemplateApplier`**: Replaces placeholders in the JSON with actual values from a `Map<String, Any>`.
- **`MotionTemplateViewGenerator`**: Combines the applier and SDUI parsers to create `MotionView` instances.

## Usage Example

### 1. Define a Template

You can define your SDUI JSON with placeholders for any value (strings, numbers, etc.).

```json
{
  "type": "TransparentTextView",
  "text": "Welcome, {{username}}!",
  "startFrame": 0,
  "endFrame": "{{duration}}",
  "layout": {
    "width": "match_parent",
    "height": "wrap_content",
    "gravity": "center"
  }
}
```

### 2. Load and Apply Data

Use the `MotionTemplateViewGenerator` to create a `MotionView`.

```kotlin
// 1. Prepare the template
val templateJson = """{ ... }""" // The JSON above
val template = MotionTemplate(JsonParser.parseString(templateJson).asJsonObject)

// 2. Define the data
val data = mapOf(
    "username" to "John Doe",
    "duration" to 150
)

// 3. Generate the MotionView
val motionView = MotionTemplateViewGenerator.generate(context, template, data)

// 4. Add to your composer or layout
motionComposer.addView(motionView as View)
```

## Advanced Usage

### List Replication
You can generate a dynamic list of views (like lyrics or a gallery) using the `{{REPLICATE}}` marker.

**Template:**
```json
{
  "views": [
    {
      "{{REPLICATE}}": "items",
      "template": {
        "type": "PopUpTextView",
        "text": "{{text}}",
        "startFrame": "{{frame}}"
      }
    }
  ]
}
```

**Data:**
```kotlin
val data = mapOf(
    "items" to listOf(
        mapOf("text" to "Line 1", "frame" to 0),
        mapOf("text" to "Line 2", "frame" to 100)
    )
)
```

**Result:**
The system will repeat the `template` object for each item in the `items` list, injecting the item's data into the placeholders.

## Real World Example: Lyrics Video

Instead of manually creating views for each lyric line in code, you can use a template:

```kotlin
val lyricsData = project.lyrics.map { 
    mapOf("text" to it.text, "frame" to it.frame, "nextFrame" to it.nextFrame) 
}

val data = mapOf(
    "songName" to project.name,
    "lyrics" to lyricsData
)

val motionView = MotionTemplateViewGenerator.generate(context, lyricsTemplate, data)
```

The system uses `Gson`'s `JsonObject` and `JsonArray` to traverse the template tree. It performs string replacement on any `JsonPrimitive` that is a string.

> [!NOTE]
> All data values are converted to strings using `.toString()` before replacement. Ensure your SDUI factories can handle the resulting string types (e.g., parsing "150" back to an Int if needed, though most SDUI parsers in this project handle stringified numbers well).
