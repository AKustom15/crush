package com.akustom15.crush.ui.screens.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.akustom15.crush.model.IconItem
import com.akustom15.crush.ui.components.AppHeader

/**
 * Pantalla de cuadrícula de iconos que muestra todos los iconos del pack.
 * Soporta búsqueda, categorías y selección de iconos.
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

    // Cargar iconos del pack desde assets
    val allIcons = remember(config.packageName) {
        try {
            AppFilterParser.parseDrawableXml(context)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Obtener categorías únicas
    val categories = remember(allIcons) {
        listOf("All") + allIcons.map { it.category }.filter { it.isNotEmpty() }.distinct()
    }

    var selectedCategory by remember { mutableStateOf("All") }

    // Filtrar iconos por búsqueda y categoría
    val filteredIcons = remember(allIcons, searchQuery, selectedCategory) {
        allIcons.filter { icon ->
            val matchesSearch = searchQuery.isEmpty() ||
                icon.formattedName.contains(searchQuery, ignoreCase = true) ||
                icon.drawableName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" ||
                icon.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp, end = 12.dp,
            top = 0.dp, bottom = bottomContentPadding + 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header con icono de la app (ocupa todo el ancho)
        if (showHeader) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AppHeader(
                    appIcon = config.appIcon,
                    appName = config.appName,
                    appSubtitle = config.appSubtitle
                )
            }
        }

        // Contador de iconos
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.icons_total, filteredIcons.size),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }

        // Filtro de categorías (si hay más de una)
        if (categories.size > 2) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
        }

        // Cuadrícula de iconos
        if (filteredIcons.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.icons_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(filteredIcons, key = { it.drawableName }) { icon ->
                IconGridItem(
                    icon = icon,
                    onClick = { onIconSelected?.invoke(icon) }
                )
            }
        }
    }
}

/** Fila de filtro de categorías con chips horizontales */
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = if (category == "All") stringResource(R.string.icons_all) else category,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/** Item individual de la cuadrícula de iconos */
@Composable
private fun IconGridItem(icon: IconItem, onClick: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Renderizar el icono desde el resource ID
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
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder si no se puede cargar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = icon.formattedName,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Pantalla de iconos standalone para usar desde IconPickerActivity.
 * Incluye su propia barra de búsqueda.
 */
@Composable
fun StandaloneIconsScreen(
    activity: android.app.Activity,
    onIconSelected: (IconItem) -> Unit,
    onCloseScreen: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Cargar todos los iconos
    val allIcons = remember {
        try {
            AppFilterParser.parseDrawableXml(context)
        } catch (e: Exception) {
            AppFilterParser.parseAppFilter(context)
        }
    }

    // Filtrar por búsqueda
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
        // Barra de búsqueda simple
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

        // Cuadrícula de iconos
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
