package com.akustom15.crush.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.akustom15.crush.data.AppLanguage
import com.akustom15.crush.data.CrushPreferences
import java.util.Locale

/**
 * Wrapper composable que aplica el idioma seleccionado en las preferencias.
 * Envuelve el contenido con el contexto localizado correcto.
 */
@Composable
fun LocalizedContent(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { CrushPreferences.getInstance(context) }
    val appLanguage = preferences.appLanguage.collectAsState().value

    // Obtener el locale basado en la preferencia del usuario
    val targetLocale = when (appLanguage) {
        AppLanguage.SPANISH -> Locale("es")
        AppLanguage.ENGLISH -> Locale("en")
        AppLanguage.FRENCH -> Locale("fr")
        AppLanguage.GERMAN -> Locale("de")
        AppLanguage.PORTUGUESE -> Locale("pt", "BR")
        AppLanguage.ARABIC -> Locale("ar")
        AppLanguage.ITALIAN -> Locale("it")
        AppLanguage.HINDI -> Locale("hi")
        AppLanguage.INDONESIAN -> Locale("in")
        AppLanguage.CHINESE -> Locale("zh", "CN")
        AppLanguage.SYSTEM -> Locale.getDefault()
    }

    // Usar key() para forzar recomposición completa cuando cambia el idioma
    key(appLanguage) {
        // Crear contexto localizado
        val localizedContext = remember(targetLocale) {
            createLocalizedContext(context, targetLocale)
        }

        // Crear configuración localizada
        val localizedConfiguration = remember(targetLocale) {
            Configuration(context.resources.configuration).apply {
                setLocale(targetLocale)
            }
        }

        // Proporcionar el contexto y configuración localizados
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration
        ) {
            content()
        }
    }
}

/**
 * Crea un contexto con el idioma especificado
 */
private fun createLocalizedContext(context: Context, locale: Locale): Context {
    val configuration = Configuration(context.resources.configuration)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
    } else {
        @Suppress("DEPRECATION")
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.createConfigurationContext(configuration)
    } else {
        @Suppress("DEPRECATION")
        val resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        context
    }
}
