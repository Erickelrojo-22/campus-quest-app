# Reglas R8 para el build de release.
# proguard-android-optimize.txt (AGP) ya cubre Android, Kotlin y AndroidX.
# Room, Compose y CameraX incluyen sus propias reglas "consumer" en los AAR,
# así que aquí solo se refuerza lo específico de la app.

# Entidades Room / clases de datos usadas por reflexión al mapear cursores.
-keep class com.example.gamequest.data.local.entity.** { *; }

# Conserva metadatos de Kotlin (data classes, default args, sealed) para R8.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes InnerClasses,Signature,EnclosingMethod

# ZXing: el lector de QR se referencia por nombre en algunos formatos.
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Evita advertencias por APIs opcionales de CameraX no presentes en runtime.
-dontwarn androidx.camera.**
