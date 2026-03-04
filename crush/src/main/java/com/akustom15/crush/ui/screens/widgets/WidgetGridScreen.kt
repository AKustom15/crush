package com.akustom15.crush.ui.screens.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.config.WidgetType
import com.akustom15.crush.data.CrushPreferences
import com.akustom15.crush.model.WidgetItem
import com.akustom15.crush.model.WallpaperItem
import com.akustom15.crush.ui.components.ItemCard
import com.akustom15.crush.utils.AssetsReader
import com.akustom15.crush.utils.KustomIntegration

@Composable
fun WidgetGridScreen(
    config: CrushConfig,
    bottomContentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { CrushPreferences.getInstance(context) }
    val gridColumns by preferences.gridColumns.collectAsState()

    if (config.widgetType == WidgetType.KWGT) {
        KwgtWidgetGrid(
            config = config,
            gridColumnsCount = gridColumns.count,
            bottomContentPadding = bottomContentPadding,
            modifier = modifier
        )
    } else {
        KlwpWallpaperGrid(
            config = config,
            gridColumnsCount = gridColumns.count,
            bottomContentPadding = bottomContentPadding,
            modifier = modifier
        )
    }
}

@Composable
private fun KwgtWidgetGrid(
    config: CrushConfig,
    gridColumnsCount: Int,
    bottomContentPadding: Dp,
    modifier: Modifier
) {
    val context = LocalContext.current
    val widgets = remember { mutableStateOf<List<WidgetItem>>(emptyList()) }

    LaunchedEffect(Unit) { widgets.value = AssetsReader.getWidgetsFromAssets(context) }

    if (widgets.value.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No widgets found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumnsCount),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp + bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                AppHeaderSection(
                    appName = config.appName,
                    appSubtitle = config.appSubtitle,
                    appIcon = config.appIcon
                )
            }

            items(widgets.value) { widget ->
                ItemCard(
                    name = widget.name,
                    description = widget.description,
                    previewUrl = widget.previewUrl,
                    appIcon = config.appIcon,
                    appName = config.appName,
                    isCompactMode = gridColumnsCount > 1,
                    onApplyClick = {
                        KustomIntegration.applyWidget(
                            context = context,
                            widgetFileName = widget.fileName,
                            packageName = config.packageName
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun KlwpWallpaperGrid(
    config: CrushConfig,
    gridColumnsCount: Int,
    bottomContentPadding: Dp,
    modifier: Modifier
) {
    val context = LocalContext.current
    val wallpapers = remember { mutableStateOf<List<WallpaperItem>>(emptyList()) }

    LaunchedEffect(Unit) { wallpapers.value = AssetsReader.getWallpapersFromAssets(context) }

    if (wallpapers.value.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No wallpapers found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumnsCount),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp + bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AppHeaderSection(
                    appName = config.appName,
                    appSubtitle = config.appSubtitle,
                    appIcon = config.appIcon
                )
            }

            items(wallpapers.value) { wallpaper ->
                ItemCard(
                    name = wallpaper.name,
                    description = wallpaper.description,
                    previewUrl = wallpaper.previewUrl,
                    appIcon = config.appIcon,
                    appName = config.appName,
                    isCompactMode = gridColumnsCount > 1,
                    onApplyClick = {
                        KustomIntegration.applyWallpaper(
                            context = context,
                            wallpaperFileName = wallpaper.fileName,
                            packageName = config.packageName
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AppHeaderSection(
    appName: String,
    appSubtitle: String,
    appIcon: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (appIcon != null) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = appIcon),
                contentDescription = appName,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (appSubtitle.isNotEmpty()) {
            Text(
                text = appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
