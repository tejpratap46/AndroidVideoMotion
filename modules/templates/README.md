# Motion Template Module

The `templates` module provides a powerful, type-safe Kotlin DSL for defining motion video templates. It is designed to be extensible, allowing other modules to contribute custom view components to the DSL.

## Core Concepts

- **`MotionTemplate`**: A blueprint for a video sequence.
- **`TemplateParameter`**: Inputs required by the template (e.g., text, colors, durations).
- **`ContentScope`**: The context where you define the layout and behavior of the video using the DSL.

---

## Examples

### 1. Easy: Simple Text Overlay
A basic template that takes a title and displays it.

```kotlin
val simpleTitleTemplate = motionTemplate("Simple Title") {
    parameters {
        string("title", defaultValue = "Welcome")
        color("textColor", defaultValue = Color.WHITE)
    }

    content {
        val titleText = data.getString("title") ?: "Hello"
        val color = data.getInt("textColor") ?: Color.WHITE

        // Assuming a 'textView' extension exists in your project
        textView {
            text = titleText
            textColor = color
            startFrame = 0
            endFrame = 100
        }
    }
}
```

### 2. Medium: Animated Lower Third
A template with multiple parameters and basic animation logic.

```kotlin
val lowerThirdTemplate = motionTemplate("Lower Third") {
    parameters {
        string("name", description = "Person's Name")
        string("role", description = "Title/Role")
        int("duration", defaultValue = 150)
    }

    content {
        val name = data.getString("name") ?: ""
        val role = data.getString("role") ?: ""
        val duration = data.getInt("duration") ?: 150

        // Background Box
        boxView {
            color = Color.parseColor("#80000000") // Semi-transparent black
            startFrame = 0
            endFrame = duration
            // Slide in animation
            animateX(from = -500, to = 0, duration = 20)
        }

        // Name Text
        textView {
            text = name
            textSize = 24f
            startFrame = 10
            endFrame = duration
        }

        // Role Text
        textView {
            text = role
            textSize = 14f
            startFrame = 15
            endFrame = duration
        }
    }
}
```

### 3. Hard: Dynamic Lyrics Video with Background
A complex template using replication or list-based data to generate a sequence of synchronized views.

```kotlin
val lyricsVideoTemplate = motionTemplate("Lyrics Master") {
    parameters {
        string("songTitle")
        video("backgroundVideo")
        // Parameters can represent complex data structures passed via TemplateData
    }

    content {
        val bgVideo = data.getString("backgroundVideo")
        
        // Background Video Layer
        videoView {
            path = bgVideo
            startFrame = 0
            endFrame = 1000 // Total length
        }

        // Fetch list of lyrics from data
        val lyrics = data.get<List<LyricLine>>("lyricLines") ?: emptyList()

        lyrics.forEach { line ->
            textView {
                text = line.text
                startFrame = line.startFrame
                endFrame = line.endFrame
                
                // Complex effects
                applyEffect(FadeIn(10))
                applyEffect(PopUp(5))
                
                layout {
                    centerInParent()
                }
            }
        }
        
        // Overlay Song Title at the start
        textView {
            text = data.getString("songTitle")
            startFrame = 0
            endFrame = 60
            applyEffect(SlideOutToTop(20))
        }
    }
}
```

## JSON Templates

You can also define templates using JSON. This is useful for storing templates in a database or fetching them from a server. The JSON content follows the SDUI format and supports `{{placeholder}}` replacement.

### JSON Examples

#### 1. Easy: Simple Text Overlay
```json
{
  "name": "Simple Title JSON",
  "parameters": [
    { "name": "title", "type": "STRING", "defaultValue": "Welcome" },
    { "name": "duration", "type": "INTEGER", "defaultValue": 100 }
  ],
  "content": {
    "type": "TransparentTextView",
    "text": "{{title}}",
    "startFrame": 0,
    "endFrame": "{{duration}}"
  }
}
```

**Loading and Applying:**
```kotlin
val json = """{ ... }""" // JSON above
val template = TemplateSerialization.templateFromJson(JsonParser.parseString(json).asJsonObject)
val data = TemplateData(mapOf("title" to "Hello World", "duration" to 150))
template.buildContent(scope)
```

#### 2. Medium: Animated Lower Third
```json
{
  "name": "Lower Third JSON",
  "parameters": [
    { "name": "name", "type": "STRING" },
    { "name": "role", "type": "STRING" }
  ],
  "content": {
    "type": "PopUpTextView",
    "text": "{{name}} - {{role}}",
    "startFrame": 10,
    "endFrame": 150,
    "writingSpeed": 1.0,
    "layout": {
      "gravity": "bottom|center_horizontal",
      "margin": { "bottom": 40 }
    }
  }
}
```

**Loading and Applying:**
```kotlin
val json = """{ ... }""" // JSON above
val template = TemplateSerialization.templateFromJson(JsonParser.parseString(json).asJsonObject)
val data = TemplateData(mapOf("name" to "John Doe", "role" to "Software Engineer"))
template.buildContent(scope)
```

#### 3. Hard: Dynamic Multi-View Template with List Replication
A complex template using replication to generate a dynamic list of views (like lyrics or gallery items) based on provided data.

```json
{
  "name": "Lyrics Master JSON",
  "parameters": [
    { "name": "songTitle", "type": "STRING" },
    { "name": "lyrics", "type": "STRING" }
  ],
  "content": {
    "views": [
      {
        "type": "GradientView",
        "orientation": "VERTICAL",
        "colors": [ -16777216, -12303292 ],
        "startFrame": 0,
        "endFrame": 1000
      },
      {
        "{{REPLICATE}}": "lyricLines",
        "template": {
          "type": "PopUpTextView",
          "text": "{{text}}",
          "startFrame": "{{start}}",
          "endFrame": "{{end}}",
          "layout": { "gravity": "center" }
        }
      }
    ]
  }
}
```

**Loading and Applying:**
```kotlin
val json = """{ ... }""" // JSON above
val template = TemplateSerialization.templateFromJson(JsonParser.parseString(json).asJsonObject)

val lyricData = listOf(
    mapOf("text" to "Hello from the other side", "start" to 0, "end" to 100),
    mapOf("text" to "I must have called a thousand times", "start" to 101, "end" to 200)
)

val data = TemplateData(mapOf(
    "songTitle" to "Hello",
    "lyricLines" to lyricData
))
template.buildContent(scope)
```

### List Replication
You can generate a dynamic list of views using the `{{REPLICATE}}` marker inside any JSON array.

**Syntax:**
```json
{
  "{{REPLICATE}}": "data_list_key",
  "template": {
    "type": "ViewType",
    "property": "{{item_key}}"
  }
}
```

The system will repeat the `template` object for each item in the `data_list_key` provided via `TemplateData`. Each iteration uses the item's own map as the scope for placeholder replacement.

---

## Extensibility

To add your own components to the DSL, simply create an extension function on `ContentScope`:

```kotlin
fun ContentScope.myCustomComponent(block: MyComponentBuilder.() -> Unit) {
    val component = MyComponentBuilder(context).apply(block).build()
    producer.addMotionViewToSequence(component)
}
```

Now `myCustomComponent` is available inside any `content { ... }` block!
