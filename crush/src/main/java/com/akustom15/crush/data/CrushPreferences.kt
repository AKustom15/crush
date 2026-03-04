package com.akustom15.crush.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AppLanguage(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    PORTUGUESE("pt-BR", "Português (Brasil)"),
    ARABIC("ar", "العربية"),
    ITALIAN("it", "Italiano"),
    HINDI("hi", "हिन्दी"),
    INDONESIAN("in", "Bahasa Indonesia"),
    CHINESE("zh-CN", "简体中文"),
    SYSTEM("system", "Automático / Auto")
}

enum class AccentColor(val colorValue: Long, val displayName: String) {
    DEFAULT(0xFFBE1452, "Predeterminado / Default"),
    BLUE(0xFF2196F3, "Azul"),
    PURPLE(0xFF9C27B0, "Púrpura"),
    GREEN(0xFF4CAF50, "Verde"),
    ORANGE(0xFFFF9800, "Naranja"),
    RED(0xFFF44336, "Rojo"),
    TEAL(0xFF009688, "Turquesa"),
    PINK(0xFFE91E63, "Rosa"),
    CYAN(0xFF00BCD4, "Cian")
}

enum class GridColumns(val count: Int) {
    ONE(1),
    TWO(2)
}

class CrushPreferences private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(getAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _accentColor = MutableStateFlow(getAccentColor())
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _gridColumns = MutableStateFlow(getGridColumns())
    val gridColumns: StateFlow<GridColumns> = _gridColumns.asStateFlow()

    fun getThemeMode(): ThemeMode {
        val value = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(value)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun getAppLanguage(): AppLanguage {
        val value = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name
        return try {
            AppLanguage.valueOf(value)
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
        _appLanguage.value = language
        applyLanguage(language)
    }

    fun applyStoredLanguage() {
        val language = getAppLanguage()
        applyLanguage(language)
    }

    private fun applyLanguage(language: AppLanguage) {
        val localeList = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language.code)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getAccentColor(): AccentColor {
        val value = prefs.getString(KEY_ACCENT_COLOR, AccentColor.DEFAULT.name) ?: AccentColor.DEFAULT.name
        return try {
            AccentColor.valueOf(value)
        } catch (e: Exception) {
            AccentColor.DEFAULT
        }
    }

    fun setAccentColor(color: AccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, color.name).apply()
        _accentColor.value = color
    }

    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
        try {
            com.akustom15.crush.notifications.CrushNotificationHelper.syncSubscription(appContext)
        } catch (_: Exception) { }
    }

    fun getGridColumns(): GridColumns {
        val value = prefs.getString(KEY_GRID_COLUMNS, GridColumns.ONE.name) ?: GridColumns.ONE.name
        return try {
            GridColumns.valueOf(value)
        } catch (e: Exception) {
            GridColumns.ONE
        }
    }

    fun setGridColumns(columns: GridColumns) {
        prefs.edit().putString(KEY_GRID_COLUMNS, columns.name).apply()
        _gridColumns.value = columns
    }

    fun clearImageCache(context: Context): Boolean {
        return try {
            val imageLoader = coil.ImageLoader(context)
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
            val previewCacheDir = java.io.File(context.filesDir, "previews")
            if (previewCacheDir.exists()) {
                previewCacheDir.deleteRecursively()
            }
            val coilCacheDir = java.io.File(context.cacheDir, "image_cache")
            if (coilCacheDir.exists()) {
                coilCacheDir.deleteRecursively()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "crush_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_GRID_COLUMNS = "grid_columns"

        @Volatile
        private var instance: CrushPreferences? = null

        fun getInstance(context: Context): CrushPreferences {
            return instance ?: synchronized(this) {
                instance ?: CrushPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
