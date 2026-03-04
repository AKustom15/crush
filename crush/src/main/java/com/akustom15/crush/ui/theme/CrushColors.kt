package com.akustom15.crush.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object CrushColors {
    // Default colors (same as PUM dark theme base)
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val Card = Color(0xFF2C2C2C)
    val OnBackground = Color(0xFFE0E0E0)
    val OnSurface = Color(0xFFE0E0E0)
    val OnSurfaceVariant = Color(0xFFA0A0A0)
    val Secondary = Color(0xFF03DAC6)
    val Error = Color(0xFFCF6679)

    // Dynamic primary (accent) color — updated at runtime
    var Primary by mutableStateOf(Color(0xFFBE1452))
        private set

    fun updatePrimary(color: Color) {
        Primary = color
    }
}
