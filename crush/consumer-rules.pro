# Consumer ProGuard rules for crush-library
# These rules are applied to apps that depend on this library

# ==========================================
# NOTIFICATIONS — FCM Service + Helper
# ==========================================
-keep class com.akustom15.crush.notifications.** { *; }

# ==========================================
# DATA — Preferences and models
# ==========================================
-keep class com.akustom15.crush.data.** { *; }
-keep class com.akustom15.crush.config.** { *; }

# ==========================================
# ICON PACK — Core functionality
# ==========================================
-keep class com.akustom15.crush.iconpack.** { *; }

# ==========================================
# FIREBASE — Keep all Firebase-related classes
# ==========================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
