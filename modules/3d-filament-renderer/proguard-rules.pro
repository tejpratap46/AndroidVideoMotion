# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keep class com.google.android.filament.** { *; }
-keep interface com.google.android.filament.** { *; }
-keepclasseswithmembernames,includedescriptorclasses class com.google.android.filament.** {
    native <methods>;
}

-keep class com.tejpratapsingh.motionlib.filamentrenderer.** { *; }
