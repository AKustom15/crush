package com.akustom15.crush.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

/**
 * Header reutilizable con icono de la app, nombre y subtítulo.
 * Se usa en la parte superior de las pantallas de contenido (iconos, widgets, wallpapers).
 */
@Composable
fun AppHeader(
    appIcon: Int?,
    appName: String,
    appSubtitle: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono de la app
        appIcon?.let { iconResId ->
            val drawable = remember(iconResId) {
                try { context.getDrawable(iconResId) } catch (e: Exception) { null }
            }
            drawable?.let { d ->
                val bitmap = remember(d) { d.toBitmap(width = 200, height = 200) }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre de la app
        Text(
            text = appName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Subtítulo
        if (appSubtitle.isNotEmpty()) {
            Text(
                text = appSubtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
