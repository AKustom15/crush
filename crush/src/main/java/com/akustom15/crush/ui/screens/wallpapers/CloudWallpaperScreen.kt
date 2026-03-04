package com.akustom15.crush.ui.screens.wallpapers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.model.CloudWallpaperItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            wallpapers.isEmpty() -> {
                Text(
                    text = "No wallpapers available",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                // Group by collection
                val grouped = remember(wallpapers) {
                    wallpapers.groupBy { it.collections.ifEmpty { "All" } }
                }
                val collections = remember(grouped) { grouped.keys.toList() }
                var selectedCollection by remember { mutableStateOf(collections.firstOrNull() ?: "All") }

                val filteredWallpapers = remember(selectedCollection, grouped) {
                    grouped[selectedCollection] ?: wallpapers
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Collection tabs
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

                    // Wallpaper grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = 8.dp + bottomContentPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredWallpapers) { wallpaper ->
                            CloudWallpaperCard(
                                wallpaper = wallpaper,
                                onClick = { selectedWallpaper = wallpaper }
                            )
                        }
                    }
                }
            }
        }

        // Full screen wallpaper viewer
        if (selectedWallpaper != null) {
            WallpaperViewerDialog(
                wallpaper = selectedWallpaper!!,
                onDismiss = { selectedWallpaper = null },
                onApply = { wallpaper ->
                    coroutineScope.launch {
                        applyWallpaper(context, wallpaper)
                    }
                }
            )
        }
    }
}

@Composable
private fun CloudWallpaperCard(
    wallpaper: CloudWallpaperItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column {
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.65f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = wallpaper.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.author,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WallpaperViewerDialog(
    wallpaper: CloudWallpaperItem,
    onDismiss: () -> Unit,
    onApply: (CloudWallpaperItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bottom action bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = wallpaper.author,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (wallpaper.downloadable) {
                    FloatingActionButton(
                        onClick = { onApply(wallpaper) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Apply wallpaper"
                        )
                    }
                }
            }
        }
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

private suspend fun applyWallpaper(context: Context, wallpaper: CloudWallpaperItem) {
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(wallpaper.url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    wallpaperManager.setBitmap(bitmap)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Wallpaper applied!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CloudWallpaper", "Error applying wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
