package com.akustom15.crush.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.core.graphics.drawable.toBitmap
import com.akustom15.crush.R
import com.akustom15.crush.config.MoreApp
import com.akustom15.crush.config.SocialMediaConfig
import com.akustom15.crush.ui.theme.CrushTheme

/**
 * Pantalla de información "Acerca de" con logo del desarrollador,
 * redes sociales, más apps y política de privacidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appIcon: Int?,
    developerLogoUrl: String = "",
    developerName: String = "AKustom15",
    moreAppsUrl: String = "",
    moreApps: List<MoreApp> = emptyList(),
    privacyPolicyUrl: String = "",
    @DrawableRes xIcon: Int,
    @DrawableRes instagramIcon: Int,
    @DrawableRes youtubeIcon: Int,
    @DrawableRes facebookIcon: Int,
    @DrawableRes telegramIcon: Int,
    onNavigateBack: () -> Unit
) {
    // Manejar botón atrás del sistema
    BackHandler { onNavigateBack() }

    val context = LocalContext.current

    CrushTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.about_title),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.about_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo del desarrollador desde URL o icono de la app como fallback
                    if (developerLogoUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(developerLogoUrl)
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Developer Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        appIcon?.let { iconResId ->
                            val drawable = remember(iconResId) { context.getDrawable(iconResId) }
                            drawable?.let { d ->
                                val bitmap = remember(d) { d.toBitmap(width = 300, height = 300) }
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // Nombre del desarrollador
                    LimitedFontScaleText(
                        text = developerName,
                        baseSizeSp = 24f,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Descripción del desarrollador
                    LimitedFontScaleText(
                        text = stringResource(R.string.about_developer_desc),
                        baseSizeSp = 15f,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )

                    // Iconos de redes sociales
                    val socialMediaLinks = remember {
                        SocialMediaConfig.getSocialMediaLinks(
                            xIcon = xIcon,
                            instagramIcon = instagramIcon,
                            youtubeIcon = youtubeIcon,
                            facebookIcon = facebookIcon,
                            telegramIcon = telegramIcon
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        socialMediaLinks.forEach { socialMedia ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(socialMedia.url))
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.about_error_opening_link),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = socialMedia.iconRes),
                                    contentDescription = socialMedia.name,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Botón More Apps
                    if (moreAppsUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(moreAppsUrl))
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.about_error_opening_link),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.about_more_apps),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Enlace de Política de Privacidad
                    if (privacyPolicyUrl.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.about_error_opening_link),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.about_privacy_policy),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Sección de más apps con tarjetas horizontales
                    if (moreApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.about_more_apps_title),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.about_more_apps_desc),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Tarjetas de apps en scroll horizontal
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(moreApps) { app ->
                                        MoreAppCard(
                                            app = app,
                                            onClick = {
                                                if (app.playStoreUrl.isNotEmpty()) {
                                                    try {
                                                        context.startActivity(
                                                            Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(app.playStoreUrl)
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(R.string.about_error_opening_link),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/** Tarjeta individual de una app en la sección "Más apps" */
@Composable
private fun MoreAppCard(app: MoreApp, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono de la app
                if (app.iconUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(app.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.name,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.description.isNotEmpty()) {
                        Text(
                            text = app.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.about_install),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/** Texto con escala de fuente limitada para accesibilidad */
@Composable
fun LimitedFontScaleText(
    text: String,
    baseSizeSp: Float,
    color: androidx.compose.ui.graphics.Color,
    fontWeight: FontWeight,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale.coerceAtMost(1.0f)
    Text(
        text = text,
        fontSize = (baseSizeSp * fontScale).sp,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = modifier
    )
}
