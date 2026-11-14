# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keep class * implements com.tejpratapsingh.motionlib.core.MotionView { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionEffect { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionAsset { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionPlugin { *; }
-keep class * implements com.tejpratapsingh.motionlib.core.MotionTransition { *; }
-keep class com.tejpratapsingh.motionlib.core.MotionAudio { *; }
-keep class com.tejpratapsingh.motion.sdui.** { *; }
