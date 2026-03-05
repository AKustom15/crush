package com.akustom15.crush.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.akustom15.crush.R
import com.akustom15.crush.data.AccentColor
import com.akustom15.crush.data.AppLanguage
import com.akustom15.crush.data.GridColumns
import com.akustom15.crush.data.CrushPreferences
import com.akustom15.crush.data.ThemeMode
import com.akustom15.crush.notifications.CrushNotificationHelper
import com.akustom15.crush.ui.theme.CrushTheme

/**
 * Pantalla de ajustes con secciones para apariencia, idioma,
 * datos/caché, notificaciones e información.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    packageName: String,
    appVersion: String,
    updateJsonUrl: String = "",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { CrushPreferences.getInstance(context) }

    // Estados observados desde preferencias
    val themeMode by preferences.themeMode.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val accentColor by preferences.accentColor.collectAsState()
    val gridColumns by preferences.gridColumns.collectAsState()
    val downloadOnWifiOnly by preferences.downloadOnWifiOnly.collectAsState()
    val notificationsEnabled by preferences.notificationsEnabled.collectAsState()

    // Launcher para permiso de notificaciones (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        preferences.setNotificationsEnabled(isGranted)
    }

    // Estados para diálogos de selección
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccentColorDialog by remember { mutableStateOf(false) }
    var showGridColumnsDialog by remember { mutableStateOf(false) }

    // Manejar botón atrás del sistema
    BackHandler { onNavigateBack() }

    CrushTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
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
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección: Apariencia
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    // Tema
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.settings_theme),
                        subtitle = when (themeMode) {
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        },
                        onClick = { showThemeDialog = true }
                    )

                    // Color de acento
                    val displayColor = if (accentColor == AccentColor.DEFAULT) {
                        Color(ContextCompat.getColor(context, R.color.crush_accent_color))
                    } else {
                        Color(accentColor.colorValue)
                    }
                    val accentColorName = when (accentColor) {
                        AccentColor.DEFAULT -> stringResource(R.string.color_default)
                        AccentColor.BLUE -> stringResource(R.string.color_blue)
                        AccentColor.PURPLE -> stringResource(R.string.color_purple)
                        AccentColor.GREEN -> stringResource(R.string.color_green)
                        AccentColor.ORANGE -> stringResource(R.string.color_orange)
                        AccentColor.RED -> stringResource(R.string.color_red)
                        AccentColor.TEAL -> stringResource(R.string.color_teal)
                        AccentColor.PINK -> stringResource(R.string.color_pink)
                        AccentColor.CYAN -> stringResource(R.string.color_cyan)
                    }
                    SettingsItemWithColor(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_accent_color),
                        subtitle = accentColorName,
                        color = displayColor,
                        onClick = { showAccentColorDialog = true }
                    )

                    // Vista de cuadrícula
                    SettingsItem(
                        icon = Icons.Default.GridView,
                        title = stringResource(R.string.settings_grid_view),
                        subtitle = when (gridColumns) {
                            GridColumns.ONE -> stringResource(R.string.settings_one_column)
                            GridColumns.TWO -> stringResource(R.string.settings_two_columns)
                        },
                        onClick = { showGridColumnsDialog = true }
                    )
                }

                // Sección: Idioma
                SettingsSection(title = stringResource(R.string.settings_language)) {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_app_language),
                        subtitle = appLanguage.displayName,
                        onClick = { showLanguageDialog = true }
                    )
                }

                // Sección: Datos y Caché
                SettingsSection(title = stringResource(R.string.settings_data_cache)) {
                    val cacheCleared = stringResource(R.string.settings_cache_cleared)
                    val cacheError = stringResource(R.string.settings_cache_error)
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = stringResource(R.string.settings_clear_cache),
                        subtitle = stringResource(R.string.settings_clear_cache_desc),
                        onClick = {
                            val success = preferences.clearImageCache(context)
                            val message = if (success) cacheCleared else cacheError
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )

                    SettingsItemWithSwitch(
                        icon = Icons.Default.Wifi,
                        title = stringResource(R.string.settings_download_wifi),
                        subtitle = stringResource(R.string.settings_download_wifi_desc),
                        checked = downloadOnWifiOnly,
                        onCheckedChange = { preferences.setDownloadOnWifiOnly(it) }
                    )
                }

                // Sección: Notificaciones
                SettingsSection(title = stringResource(R.string.settings_notifications)) {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_notifications_desc),
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                && !CrushNotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                preferences.setNotificationsEnabled(enabled)
                            }
                        }
                    )
                }

                // Sección: Información
                SettingsSection(title = stringResource(R.string.settings_info)) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_version),
                        subtitle = appVersion,
                        onClick = { }
                    )

                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.settings_rate),
                        subtitle = stringResource(R.string.settings_rate_desc),
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                )
                            } catch (e: Exception) {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                )
                            }
                        }
                    )

                    val noEmailApp = stringResource(R.string.settings_no_email_app)
                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = stringResource(R.string.settings_report),
                        subtitle = stringResource(R.string.settings_report_desc),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:akustom15@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Bug Report - $packageName")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, noEmailApp, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Diálogos de selección
        if (showThemeDialog) {
            SelectionDialog(
                title = stringResource(R.string.settings_select_theme),
                options = listOf(
                    stringResource(R.string.settings_theme_light),
                    stringResource(R.string.settings_theme_dark),
                    stringResource(R.string.settings_theme_system)
                ),
                selectedIndex = ThemeMode.entries.indexOf(themeMode),
                onSelect = { index ->
                    preferences.setThemeMode(ThemeMode.entries[index])
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showLanguageDialog) {
            SelectionDialog(
                title = stringResource(R.string.settings_select_language),
                options = AppLanguage.entries.map { it.displayName },
                selectedIndex = AppLanguage.entries.indexOf(appLanguage),
                onSelect = { index ->
                    preferences.setAppLanguage(AppLanguage.entries[index])
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        if (showAccentColorDialog) {
            AccentColorDialog(
                selectedColor = accentColor,
                onSelect = { color ->
                    preferences.setAccentColor(color)
                    showAccentColorDialog = false
                },
                onDismiss = { showAccentColorDialog = false }
            )
        }

        if (showGridColumnsDialog) {
            SelectionDialog(
                title = stringResource(R.string.settings_grid_view),
                options = listOf(
                    stringResource(R.string.settings_one_column),
                    stringResource(R.string.settings_two_columns)
                ),
                selectedIndex = GridColumns.entries.indexOf(gridColumns),
                onSelect = { index ->
                    preferences.setGridColumns(GridColumns.entries[index])
                    showGridColumnsDialog = false
                },
                onDismiss = { showGridColumnsDialog = false }
            )
        }
    }
}

// ========== Componentes reutilizables de la pantalla de ajustes ==========

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsItemWithColor(
    icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color))
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(index) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex, onClick = { onSelect(index) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AccentColorDialog(
    selectedColor: AccentColor, onSelect: (AccentColor) -> Unit, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val defaultColorFromResources = Color(ContextCompat.getColor(context, R.color.crush_accent_color))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_select_accent_color), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                AccentColor.entries.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { color ->
                            val displayColor = if (color == AccentColor.DEFAULT) defaultColorFromResources else Color(color.colorValue)
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(displayColor).clickable { onSelect(color) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (color == selectedColor) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    )
}
