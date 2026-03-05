package com.akustom15.crush.ui.screens.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.iconpack.AppFilterParser
import com.akustom15.crush.iconpack.IconPackManager
import com.akustom15.crush.iconpack.IconRequestHelper
import com.akustom15.crush.iconpack.LauncherInfo
import com.akustom15.crush.ui.components.RotatingIconAnimation

@Composable
fun DashboardScreen(
    config: CrushConfig,
    bottomContentPadding: Dp = 0.dp,
    onAboutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onWallpapersClick: () -> Unit = {},
    onIconRequestClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Estado para el diálogo de selección de launcher
    var showLauncherDialog by remember { mutableStateOf(false) }
    val compatibleLaunchers = remember(context) { IconPackManager.getCompatibleLaunchers(context) }

    // Cargar nombres de iconos desde assets/appfilter.xml
    val iconResourceNames = remember(context) {
        try {
            AppFilterParser.parseAppFilter(context).map { it.drawableName }
        } catch (e: Exception) {
            Log.e("DashboardScreen", "Error loading icon names from assets", e)
            emptyList()
        }
    }

    // Contadores de apps temáticas vs total
    val totalApps = remember(context) { IconRequestHelper.getTotalAppCount(context) }
    val themedApps = remember(context) { IconRequestHelper.getThemedAppCount(context) }
    val missingApps = remember(totalApps, themedApps) { (totalApps - themedApps).coerceAtLeast(0) }

    var totalIcons by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        totalIcons = try {
            AppFilterParser.getIconCount(context)
        } catch (e: Exception) {
            Log.e("DashboardScreen", "Error getting icon count", e)
            0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomContentPadding)
        ) {
            // Icon animation grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                contentAlignment = Alignment.Center
            ) {
                if (iconResourceNames.isNotEmpty()) {
                    RotatingIconAnimation(
                        modifier = Modifier.fillMaxSize(),
                        iconResourceNames = iconResourceNames,
                        batchDisplayDurationMillis = 5500L,
                        iconAppearanceDurationMillis = 1000L,
                        staggerDelayMillis = 80L,
                        iconSize = 80
                    )
                } else {
                    Text(
                        text = stringResource(R.string.loading_icons),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App description / subtitle
            if (config.appSubtitle.isNotEmpty()) {
                Text(
                    text = config.appSubtitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 10.dp, bottom = 18.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Apply icons button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .width(264.dp)
                        .wrapContentHeight()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(43.dp)
                        )
                        .semantics { role = Role.Button }
                        .clickable {
                            val launchers = compatibleLaunchers
                            when {
                                launchers.size == 1 -> IconPackManager.applyIconPack(context, launchers.first())
                                launchers.size > 1 -> showLauncherDialog = true
                                else -> Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.apply_icons_button),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cards grid (2 columns)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Icons count card
                    DashboardCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = totalIcons.toString(),
                                fontSize = 48.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(R.string.icons_count_label),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Icon request card with progress
                    DashboardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onIconRequestClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.icon_request_label),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${totalApps} ${stringResource(R.string.total_apps_label)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${themedApps} ${stringResource(R.string.themed_apps_label)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Progress bar
                            val progress = if (totalApps > 0)
                                themedApps.toFloat() / totalApps else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${missingApps} ${stringResource(R.string.missing_apps_label)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wallpapers card
                    DashboardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onWallpapersClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.wallpapers_label),
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // About card
                    DashboardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAboutClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.about_label),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = config.developerName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Settings card
                    DashboardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSettingsClick
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_label),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de selección de launcher
    if (showLauncherDialog) {
        LauncherSelectionDialog(
            launchers = compatibleLaunchers,
            onLauncherSelected = { launcher ->
                IconPackManager.applyIconPack(context, launcher)
                showLauncherDialog = false
            },
            onDismiss = { showLauncherDialog = false }
        )
    }
}

@Composable
private fun LauncherSelectionDialog(
    launchers: List<LauncherInfo>,
    onLauncherSelected: (LauncherInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                stringResource(R.string.launcher_selection_title),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                launchers.forEach { launcher ->
                    Button(
                        onClick = { onLauncherSelected(launcher) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = launcher.name,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .semantics { role = Role.Button }
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

