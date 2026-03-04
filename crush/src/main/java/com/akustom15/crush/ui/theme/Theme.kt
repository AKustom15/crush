package com.akustom15.crush.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.akustom15.crush.R
import com.akustom15.crush.data.CrushPreferences
import com.akustom15.crush.data.AccentColor
import com.akustom15.crush.data.ThemeMode

private fun getDarkColorScheme(
    accentColor: Color,
    backgroundColor: Color,
    surfaceColor: Color,
    cardColor: Color
) = darkColorScheme(
    primary = accentColor,
    primaryContainer = accentColor.copy(alpha = 0.7f),
    secondary = CrushColors.Secondary,
    background = backgroundColor,
    surface = surfaceColor,
    surfaceVariant = cardColor,
    surfaceContainerHigh = surfaceColor,
    surfaceContainer = backgroundColor,
    surfaceContainerHighest = cardColor,
    surfaceBright = cardColor,
    surfaceDim = backgroundColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CrushColors.OnBackground,
    onSurface = CrushColors.OnSurface,
    onSurfaceVariant = CrushColors.OnSurfaceVariant,
    error = CrushColors.Error
)

private fun getLightColorScheme(
    accentColor: Color,
    backgroundColor: Color,
    surfaceColor: Color,
    cardColor: Color
) = lightColorScheme(
    primary = accentColor,
    primaryContainer = accentColor.copy(alpha = 0.3f),
    secondary = CrushColors.Secondary,
    background = backgroundColor,
    surface = surfaceColor,
    surfaceVariant = cardColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    error = CrushColors.Error
)

@Composable
fun CrushTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { CrushPreferences.getInstance(context) }

    val themeMode = preferences.themeMode.collectAsState().value
    val accentColorPref = preferences.accentColor.collectAsState().value

    val accentColor = if (accentColorPref == AccentColor.DEFAULT) {
        Color(ContextCompat.getColor(context, R.color.crush_accent_color))
    } else {
        Color(accentColorPref.colorValue)
    }

    val systemDarkTheme = isSystemInDarkTheme()

    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDarkTheme
    }

    val backgroundColor = if (useDarkTheme) {
        Color(ContextCompat.getColor(context, R.color.crush_background_color_dark))
    } else {
        Color(ContextCompat.getColor(context, R.color.crush_background_color_light))
    }
    val surfaceColor = if (useDarkTheme) {
        Color(ContextCompat.getColor(context, R.color.crush_surface_color_dark))
    } else {
        Color(ContextCompat.getColor(context, R.color.crush_surface_color_light))
    }
    val cardColor = if (useDarkTheme) {
        Color(ContextCompat.getColor(context, R.color.crush_card_color_dark))
    } else {
        Color(ContextCompat.getColor(context, R.color.crush_card_color_light))
    }

    val colorScheme = if (useDarkTheme) {
        getDarkColorScheme(accentColor, backgroundColor, surfaceColor, cardColor)
    } else {
        getLightColorScheme(accentColor, backgroundColor, surfaceColor, cardColor)
    }

    CrushColors.updatePrimary(accentColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !useDarkTheme
                    isAppearanceLightNavigationBars = !useDarkTheme
                }
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = CrushTypography, content = content)
}

private fun android.content.Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
