# ── Entidades Room ──────────────────────────────────────────────────────────
-keep class com.eninetworks.notas.data.** { *; }
-keep class com.eninetworks.notas.widget.** { *; }
-keepattributes *Annotation*

# ── Room ────────────────────────────────────────────────────────────────────
# La clase generada AppDatabase_Impl extiende RoomDatabase; sin esto R8 la elimina
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# ── Security-Crypto / Tink ──────────────────────────────────────────────────
# EncryptedSharedPreferences usa Tink internamente con ServiceLoader y reflexión.
# R8 elimina los registros de claves de Tink causando crash en cualquier phone
# que no sea debug. El try/catch en LockViewModel es la segunda línea de defensa.
-keep class com.google.crypto.tink.** { *; }
-keepnames class com.google.crypto.tink.** {}
-dontwarn com.google.crypto.tink.**

# ── ViewModels ───────────────────────────────────────────────────────────────
# AndroidViewModelFactory instancia ViewModels por reflexión buscando el constructor
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Advertencias suprimidas (análisis estático ausentes en Android) ──────────
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
