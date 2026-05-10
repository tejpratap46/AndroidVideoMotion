# SDUI Module

The SDUI (Server-Driven UI) module provides frameworks for defining and rendering user interfaces and motion compositions using JSON. It contains two primary systems: **Motion SDUI** and **Generic SDUI**.

## 1. Motion SDUI

The Motion SDUI system is designed for polymorphic serialization and deserialization of `motionlib` components. This allows you to define complex video/motion compositions in JSON.

### Core Components

- **`MotionSdui`**: A central registry for factories and serializers.
- **`MotionView`**: Supports nested views, effects, and plugins.
- **`MotionEffect`**: Serializes effects applied to views.
- **`MotionPlugin`**: Serializes plugins for composer views.
- **`MotionAudio`**: Serializes audio components.

### Usage

#### Registering Custom Components
To support custom `MotionView` types, you must register them with the `MotionSdui` registry. It is recommended to do this during application startup.

```kotlin
MotionSdui.registerView("CustomText") { context, json ->
    // Use helper extension to parse common properties (startFrame, endFrame, etc.)
    val props = json.parseMotionViewProps()
    
    // Parse custom properties specific to your view
    val text = json.get("text")?.asString ?: ""

    CustomTextView(context!!).apply {
        this.text = text
        this.startFrame = props.startFrame
        this.endFrame = props.endFrame
    }
}

MotionSdui.registerViewSerializer(CustomTextView::class.java) { view, json ->
    json.addProperty("type", "CustomText")
    json.addProperty("text", view.text.toString())
    json.addProperty("startFrame", view.startFrame)
    json.addProperty("endFrame", view.endFrame)
}
```

#### Serialization & Deserialization
You can convert `MotionView` objects to/from JSON using the provided extension functions:

```kotlin
// To JSON
val jsonObject = motionView.toJson()

// From JSON
val motionView = jsonObject.toMotionView(context)
```

## 2. Generic SDUI

The Generic SDUI system provides a simple way to render standard Android `View` hierarchies from JSON.

### Core Components

- **`SduiRenderer`**: The main class responsible for rendering JSON into a `ViewGroup`.
- **`ViewFactory`**: Interface for creating views from JSON.

### Default Factories
The following types are supported out of the box:
- `text`: Renders a `TextView` (`TextFactory`).
- `image`: Renders an `ImageView` (`ImageFactory`).
- `container`: Renders a `LinearLayout` or similar container (`ContainerFactory`).

### Usage

#### Basic Rendering
```kotlin
val renderer = SduiRenderer(imageLoader = MyImageLoader())
renderer.renderInto(rootContainer, jsonString)
```

#### Registering Custom View Factories
```kotlin
renderer.register("my_custom_button") { context, json, renderer ->
    Button(context).apply {
        text = json.get("label").asString
    }
}
```

## JSON Structure Examples

### Motion SDUI Example
```json
{
  "type": "VideoView",
  "startFrame": 0,
  "endFrame": 100,
  "loop": { "start": 0, "end": 100 },
  "effects": [
    {
      "type": "FadeInEffect",
      "duration": 10
    }
  ]
}
```

### Generic SDUI Example
```json
{
  "type": "container",
  "children": [
    {
      "type": "text",
      "text": "Hello, SDUI!",
      "textSize": 20
    }
  ]
}
```

## Dependencies
- `modules:motionlib`: Core motion functionality.
- `com.google.code.gson:gson`: JSON parsing.
