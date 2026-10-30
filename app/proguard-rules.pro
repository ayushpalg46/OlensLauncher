# Keep JNI native methods and engine class
-keepclassmembers class com.olens.launcher.core.NativeEngine {
    native <methods>;
}
-keep class com.olens.launcher.core.NativeEngine { *; }
