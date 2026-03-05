package com.akustom15.crush.ui.screens.icons

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.iconpack.AppFilterParser
import com.akustom15.crush.iconpack.FavoriteIconsManager
import com.akustom15.crush.iconpack.IconPackManager
import com.akustom15.crush.iconpack.LauncherInfo
import com.akustom15.crush.model.IconItem

/**
 * Icon grid screen with tab-based categories, favorites, search, and icon detail dialog.
 * Design matches Zyra/Glasswave icon preview style.
 */
@Composable
fun IconGridScreen(
    config: CrushConfig,
    searchQuery: String = "",
    showHeader: Boolean = true,
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onIconSelected: ((IconItem) -> Unit)? = null
) {
    val context = LocalContext.current

    // Load icons from assets
    val allIcons = remember(config.packageName) {
        try {
            AppFilterParser.parseDrawableXml(context)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get unique categories
    val categories = remember(allIcons) {
        allIcons.map { it.category }.filter { it.isNotEmpty() }.distinct()
    }

    // Tab state: 0=All, 1=Favorites, 2+=categories
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Favorites state
    var favoriteIcons by remember { mutableStateOf(FavoriteIconsManager.loadFavoriteIcons(context)) }
    val favoriteIconItems = remember(allIcons, favoriteIcons) {
        allIcons.filter { favoriteIcons.contains(it.drawableName) }
    }

    // Icon detail dialog state
    var showIconDetail by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf<IconItem?>(null) }

    // Launcher dialog state
    var showLauncherDialog by remember { mutableStateOf(false) }
    val compatibleLaunchers = remember(context) { IconPackManager.getCompatibleLaunchers(context) }

    // Filter icons based on tab and search
    val filteredIcons = remember(allIcons, searchQuery, selectedTabIndex, categories, favoriteIconItems) {
        when {
            selectedTabIndex == 0 && searchQuery.isBlank() -> allIcons
            selectedTabIndex == 0 -> allIcons.filter {
                it.formattedName.contains(searchQuery, ignoreCase = true) ||
                    it.drawableName.contains(searchQuery, ignoreCase = true)
            }
            selectedTabIndex == 1 -> {
                if (searchQuery.isBlank()) favoriteIconItems
                else favoriteIconItems.filter {
                    it.formattedName.contains(searchQuery, ignoreCase = true) ||
                        it.drawableName.contains(searchQuery, ignoreCase = true)
                }
            }
            else -> {
                val catIndex = selectedTabIndex - 2
                if (catIndex < categories.size) {
                    val selectedCategory = categories[catIndex]
                    allIcons.filter { icon ->
                        icon.category == selectedCategory && (searchQuery.isBlank() ||
                            icon.formattedName.contains(searchQuery, ignoreCase = true) ||
                            icon.drawableName.contains(searchQuery, ignoreCase = true))
                    }
                } else allIcons
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable tab row (All, Favorites, categories...)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            divider = {},
            indicator = {},
            edgePadding = 0.dp
        ) {
            // Tab All
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                text = {
                    Text(
                        text = stringResource(R.string.icons_all),
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 0)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onBackground
                    )
                }
            )

            // Tab Favorites
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                text = {
                    Text(
                        text = stringResource(R.string.icons_favorites),
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 1)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onBackground
                    )
                }
            )

            // Category tabs
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedTabIndex == index + 2,
                    onClick = { selectedTabIndex = index + 2 },
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selectedTabIndex == index + 2) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                    text = {
                        Text(
                            text = category,
                            fontWeight = if (selectedTabIndex == index + 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index + 2)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }

        // Icon count
        Text(
            text = stringResource(R.string.icons_total, filteredIcons.size),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Icon grid
        if (filteredIcons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomContentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.icons_no_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 8.dp, end = 8.dp,
                    top = 4.dp, bottom = bottomContentPadding + 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIcons, key = { it.drawableName }) { icon ->
                    IconGridItem(
                        icon = icon,
                        onClick = {
                            selectedIcon = icon
                            showIconDetail = true
                        }
                    )
                }
            }
        }
    }

    // Icon detail dialog
    if (showIconDetail && selectedIcon != null) {
        IconDetailDialog(
            iconItem = selectedIcon!!,
            isFavorite = favoriteIcons.contains(selectedIcon!!.drawableName),
            onDismiss = { showIconDetail = false },
            onToggleFavorite = {
                favoriteIcons = FavoriteIconsManager.toggleFavoriteIcon(context, selectedIcon!!.drawableName)
            },
            onApplyClick = {
                showIconDetail = false
                val launchers = compatibleLaunchers
                when {
                    launchers.size == 1 -> IconPackManager.applyIconPack(context, launchers.first())
                    launchers.size > 1 -> showLauncherDialog = true
                    else -> Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Launcher selection dialog
    if (showLauncherDialog) {
        AlertDialog(
            onDismissRequest = { showLauncherDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    stringResource(R.string.launcher_selection_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    compatibleLaunchers.forEach { launcher ->
                        Button(
                            onClick = {
                                IconPackManager.applyIconPack(context, launcher)
                                showLauncherDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(text = launcher.name, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLauncherDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

/** Icon grid item with circular background like Zyra/Glasswave */
@Composable
fun IconGridItem(icon: IconItem, onClick: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val drawable = remember(icon.resourceId) {
                    try {
                        context.getDrawable(icon.resourceId)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (drawable != null) {
                    val bitmap = remember(drawable) {
                        drawable.toBitmap(width = 128, height = 128)
                    }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = icon.formattedName,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Icon name
            Text(
                text = icon.formattedName,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Standalone icons screen for use from IconPickerActivity.
 * Includes its own search bar.
 */
@Composable
fun StandaloneIconsScreen(
    activity: android.app.Activity,
    onIconSelected: (IconItem) -> Unit,
    onCloseScreen: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val allIcons = remember {
        try {
            AppFilterParser.parseDrawableXml(context)
        } catch (e: Exception) {
            AppFilterParser.parseAppFilter(context)
        }
    }

    val filteredIcons = remember(allIcons, searchQuery) {
        if (searchQuery.isEmpty()) allIcons
        else allIcons.filter {
            it.formattedName.contains(searchQuery, ignoreCase = true) ||
                it.drawableName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(50)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredIcons, key = { it.drawableName }) { icon ->
                IconGridItem(icon = icon, onClick = { onIconSelected(icon) })
            }
        }
    }
}
