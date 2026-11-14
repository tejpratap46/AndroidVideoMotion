# Preserve Motion SDUI types from R8/ProGuard obfuscation so polymorphic JSON serialization/deserialization functions correctly.
-keep class * implements com.tejpratapsingh.motionlib.core.MotionView { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionEffect { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionAsset { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionPlugin { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionTransition { *; }
-keep class com.tejpratapsingh.motionlib.core.MotionAudio { *; }
-keep class com.tejpratapsingh.motion.sdui.** { *; }
