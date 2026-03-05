package com.akustom15.crush.ui.screens.wallpapers

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.model.CloudWallpaperItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun CloudWallpaperScreen(
    config: CrushConfig,
    bottomContentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var wallpapers by remember { mutableStateOf<List<CloudWallpaperItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedWallpaper by remember { mutableStateOf<CloudWallpaperItem?>(null) }

    LaunchedEffect(config.cloudWallpapersUrl) {
        if (config.cloudWallpapersUrl.isNotEmpty()) {
            isLoading = true
            try {
                wallpapers = fetchWallpapers(config.cloudWallpapersUrl)
                errorMessage = null
            } catch (e: Exception) {
                Log.e("CloudWallpaper", "Error fetching wallpapers", e)
                errorMessage = e.message
            }
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.loading_wallpapers),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    try {
                                        wallpapers = fetchWallpapers(config.cloudWallpapersUrl)
                                        errorMessage = null
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                    isLoading = false
                                }
                            }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            wallpapers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_wallpapers_available),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            else -> {
                val grouped = remember(wallpapers) {
                    wallpapers.groupBy { it.collections.ifEmpty { "All" } }
                }
                val collections = remember(grouped) { grouped.keys.toList() }
                var selectedCollection by remember { mutableStateOf(collections.firstOrNull() ?: "All") }

                val filteredWallpapers = remember(selectedCollection, grouped) {
                    grouped[selectedCollection] ?: wallpapers
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (collections.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = collections.indexOf(selectedCollection).coerceAtLeast(0),
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 16.dp
                        ) {
                            collections.forEach { collection ->
                                Tab(
                                    selected = selectedCollection == collection,
                                    onClick = { selectedCollection = collection },
                                    text = {
                                        Text(
                                            text = collection,
                                            fontWeight = if (selectedCollection == collection) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    val gridState = rememberLazyGridState()

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp + bottomContentPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            filteredWallpapers,
                            key = { it.url }
                        ) { wallpaper ->
                            val index = filteredWallpapers.indexOf(wallpaper)
                            AnimatedWallpaperItem(
                                wallpaper = wallpaper,
                                index = index,
                                gridState = gridState,
                                onClick = { selectedWallpaper = wallpaper }
                            )
                        }
                    }
                }
            }
        }

        if (selectedWallpaper != null) {
            WallpaperDetailDialog(
                wallpaper = selectedWallpaper!!,
                onDismiss = { selectedWallpaper = null }
            )
        }
    }
}

/**
 * Item animado con efecto de escala y transparencia al hacer scroll (como Zyra)
 */
@Composable
private fun AnimatedWallpaperItem(
    wallpaper: CloudWallpaperItem,
    index: Int,
    gridState: LazyGridState,
    onClick: () -> Unit
) {
    val visibleItemsInfo = gridState.layoutInfo.visibleItemsInfo
    val isVisible = visibleItemsInfo.any { it.index == index }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wallpaper_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseOutBack
        ),
        label = "wallpaper_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.50f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(4.dp, RoundedCornerShape(30.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            SubcomposeAsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    }
}

/**
 * Diálogo de detalle de wallpaper con opciones de aplicar, descargar e info
 */
@Composable
private fun WallpaperDetailDialog(
    wallpaper: CloudWallpaperItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showApplyOptions by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Botón cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .statusBarsPadding()
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Controles inferiores
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = wallpaper.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(stringResource(R.string.wallpaper_by_author), wallpaper.author),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón info
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = stringResource(R.string.wallpaper_info),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Botón aplicar como fondo
                    IconButton(
                        onClick = { showApplyOptions = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.Wallpaper,
                            contentDescription = stringResource(R.string.apply_wallpaper),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Botón descargar
                    if (wallpaper.downloadable) {
                        IconButton(
                            onClick = { downloadWallpaper(context, wallpaper) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = stringResource(R.string.download),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de opciones para aplicar wallpaper
    if (showApplyOptions) {
        AlertDialog(
            onDismissRequest = { showApplyOptions = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.apply_wallpaper),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Button(
                        onClick = {
                            scope.launch {
                                applyWallpaperToScreen(context, wallpaper.url, WallpaperManager.FLAG_SYSTEM)
                            }
                            showApplyOptions = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.home_screen))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                applyWallpaperToScreen(context, wallpaper.url, WallpaperManager.FLAG_LOCK)
                            }
                            showApplyOptions = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.lock_screen))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                applyWallpaperToScreen(context, wallpaper.url, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                            }
                            showApplyOptions = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.both_screens))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showApplyOptions = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Diálogo de información del wallpaper
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.wallpaper_info),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    InfoRow(stringResource(R.string.wallpaper_info_name), wallpaper.name)
                    InfoRow(stringResource(R.string.wallpaper_info_author), wallpaper.author)
                    if (wallpaper.collections.isNotEmpty()) {
                        InfoRow(stringResource(R.string.wallpaper_info_collection), wallpaper.collections)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

private suspend fun fetchWallpapers(url: String): List<CloudWallpaperItem> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<CloudWallpaperItem>>() {}.type
        Gson().fromJson(body, type)
    }
}

private fun downloadWallpaper(context: Context, wallpaper: CloudWallpaperItem) {
    try {
        val fileName = "${wallpaper.name.replace(" ", "_")}.jpg"
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(wallpaper.url))
            .setTitle(String.format(context.getString(R.string.downloading_title), wallpaper.name))
            .setDescription(context.getString(R.string.downloading_description))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.download_started), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("CloudWallpaper", "Error downloading wallpaper", e)
        Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show()
    }
}

private suspend fun applyWallpaperToScreen(context: Context, url: String, flags: Int) {
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    wallpaperManager.setBitmap(bitmap, null, true, flags)
                    val msgRes = when (flags) {
                        WallpaperManager.FLAG_SYSTEM -> R.string.wallpaper_applied_home
                        WallpaperManager.FLAG_LOCK -> R.string.wallpaper_applied_lock
                        else -> R.string.wallpaper_applied_both
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CloudWallpaper", "Error applying wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.wallpaper_failed_apply), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
