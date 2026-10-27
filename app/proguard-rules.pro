# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\...\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt

# ---- Codigo de la app (data models usan reflecion de Compose/serializacion) ----
-keep class com.auraplayer.** { *; }

# ---- NewPipeExtractor & Rhino JS Engine (refleccion intensiva) ----
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**
-dontwarn javax.annotation.**
-keep class org.schabi.newpipe.extractor.** { *; }
-keep class org.schabi.newpipe.extractor.services.** { *; }
-dontwarn org.schabi.newpipe.**

# Rhino referencia APIs de JVM desktop que no existen en Android y nunca se invocan en el dispositivo
-dontwarn javax.script.**
-dontwarn java.beans.**
-dontwarn jdk.dynalink.**

# ---- OkHttp / Okio ----
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ---- Media3 / ExoPlayer ----
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ---- Coil (carga de imagenes) ----
-dontwarn coil.**
-keep class coil.** { *; }

# ---- ZXing (QR) ----
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ---- Coroutines (evita crash de UnitTesting/debugprobes si se activa R8) ----
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---- Para activar R8: en app/build.gradle.kts cambia isMinifyEnabled = true
# en el buildType release y prueba en un dispositivo real:
#   1. Busqueda de YouTube (NewPipeExtractor)  2. Caratulas (Coil)
#   3. QR share (ZXing)  4. Servidor WiFi (org.json via JSONObject)
