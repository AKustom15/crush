package com.akustom15.crush.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akustom15.crush.R

/**
 * Barra de búsqueda animada en la parte superior.
 * Animación profesional al hacer clic en el icono de búsqueda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedSearchTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    showMenuDropdown: Boolean = false,
    onMenuDismiss: () -> Unit = {},
    onChangelogClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // Solicitar foco cuando la búsqueda se activa
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                        slideInHorizontally(initialOffsetX = { it / 2 }) togetherWith
                    fadeOut(animationSpec = tween(300)) +
                        slideOutHorizontally(targetOffsetX = { -it / 2 })
                },
                label = "SearchAnimation"
            ) { searchActive ->
                if (searchActive) {
                    // Campo de búsqueda redondeado
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .clip(RoundedCornerShape(50)),
                        placeholder = {
                            Text(
                                stringResource(R.string.search_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                } else {
                    // Espacio vacío cuando no se busca
                    Box {}
                }
            }
        },
        actions = {
            // Botones de búsqueda y menú (visibles cuando NO se busca)
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row {
                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Botón de menú con dropdown
                    Box {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Menú desplegable
                        DropdownMenu(
                            expanded = showMenuDropdown,
                            onDismissRequest = onMenuDismiss,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.menu_changelog),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onMenuDismiss()
                                    onChangelogClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.menu_about),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onMenuDismiss()
                                    onAboutClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.menu_settings),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onMenuDismiss()
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            }

            // Botón de cerrar cuando se está buscando
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = {
                    onSearchQueryChange("")
                    onSearchActiveChange(false)
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
    )
}
