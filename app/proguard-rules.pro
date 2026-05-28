# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.langxiancheng.kiosk.data.model.** { *; }
-keep class com.langxiancheng.kiosk.data.engine.** { *; }

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
