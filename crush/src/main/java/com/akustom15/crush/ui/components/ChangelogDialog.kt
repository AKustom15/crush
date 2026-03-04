package com.akustom15.crush.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akustom15.crush.R
import com.akustom15.crush.config.ChangelogConfig

/**
 * Diálogo de changelog con efecto de fondo difuminado fuerte.
 * Muestra versión de la app, entradas del changelog y contadores.
 */
@Composable
fun ChangelogDialog(
    changelog: ChangelogConfig,
    appVersion: String,
    iconCount: Int = 0,
    widgetCount: Int = 0,
    wallpaperCount: Int = 0,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Fondo con efecto blur fuerte y gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(20.dp)
                    } else {
                        Modifier
                    }
                )
        )

        // Capa de contenido sobre el blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Tarjeta de contenido
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Título
                    Text(
                        text = stringResource(R.string.changelog_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Versión auto-detectada
                    Text(
                        text = appVersion,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Entradas del changelog
                    changelog.entries.forEach { entry ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = if (entry.resourceId != 0) {
                                    stringResource(entry.resourceId)
                                } else {
                                    entry.text
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Contador de iconos
                    if (iconCount > 0) {
                        ChangelogCountRow(stringResource(R.string.changelog_icons, iconCount))
                    }

                    // Contador de widgets
                    if (widgetCount > 0) {
                        ChangelogCountRow(stringResource(R.string.changelog_widgets, widgetCount))
                    }

                    // Contador de wallpapers
                    if (wallpaperCount > 0) {
                        ChangelogCountRow(stringResource(R.string.changelog_wallpapers, wallpaperCount))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón aceptar alineado a la derecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.changelog_accept),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogCountRow(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "•",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
    }
}
