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

# Sunmi Peripheral SDK (AIDL interfaces must be kept)
-keep class com.sunmi.nfc.** { *; }
-keep class com.sunmi.peripheralsdk.** { *; }
-keep class com.sunmi.card.** { *; }
-keep class com.sunmi.statuslightmanager.** { *; }
-keep class com.sunmi.usbscreen.** { *; }
-keep class com.sunmi.docker.** { *; }
-dontwarn com.sunmi.**
