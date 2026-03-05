package com.akustom15.crush.ui.screens.request

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.data.IconRequestPreferences
import com.akustom15.crush.iconpack.IconRequestHelper
import com.akustom15.crush.iconpack.MissingApp
import com.akustom15.crush.ui.theme.CrushTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconRequestScreen(
    config: CrushConfig,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allApps by remember { mutableStateOf<List<MissingApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    var requestedIcons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var premiumAvailable by remember { mutableStateOf(IconRequestPreferences.getPremiumCount(context)) }

    val freeRequestLimit = config.freeIconRequestLimit
    val totalAlreadyRequested = requestedIcons.size
    val freeRequestsUsed = min(totalAlreadyRequested, freeRequestLimit)
    val freeRequestsRemaining = max(0, freeRequestLimit - freeRequestsUsed)
    val totalAvailableForNewSelection = freeRequestsRemaining + premiumAvailable

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                val icons = IconRequestPreferences.loadRequestedIcons(context)
                requestedIcons = icons
                premiumAvailable = IconRequestPreferences.getPremiumCount(context)
            } catch (e: Exception) {
                Log.e("IconRequest", "Error loading from Firestore", e)
            }

            withContext(Dispatchers.IO) {
                val apps = IconRequestHelper.getMissingIconApps(context)
                withContext(Dispatchers.Main) {
                    allApps = apps
                    isLoading = false
                }
            }
        }
    }

    CrushTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.icon_request_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.about_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                if (selectedApps.isNotEmpty() && selectedApps.size <= totalAvailableForNewSelection && !isLoading) {
                    ExtendedFloatingActionButton(
                        text = {
                            Text(
                                "${stringResource(R.string.icon_request_send)} (${selectedApps.size}/$totalAvailableForNewSelection)",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        onClick = {
                            isLoading = true
                            scope.launch {
                                var requestAttempted = false
                                try {
                                    val isPremiumBatch = premiumAvailable > 0
                                    withContext(Dispatchers.IO) {
                                        IconRequestHelper.shareIconRequests(
                                            context = context,
                                            selectedPackages = selectedApps,
                                            allApps = allApps,
                                            email = config.iconRequestEmail,
                                            appName = config.appName,
                                            isPremium = isPremiumBatch
                                        )
                                    }
                                    requestAttempted = true
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } finally {
                                    if (requestAttempted) {
                                        val newRequestedIcons = requestedIcons + selectedApps
                                        try {
                                            IconRequestPreferences.saveRequestedIcons(context, newRequestedIcons)

                                            val premiumConsumed = max(0, selectedApps.size - freeRequestsRemaining)
                                            if (premiumConsumed > 0) {
                                                IconRequestPreferences.consumePremiumRequests(context, premiumConsumed)
                                                premiumAvailable = IconRequestPreferences.getPremiumCount(context)
                                            }

                                            requestedIcons = newRequestedIcons
                                            selectedApps = emptySet()
                                            Toast.makeText(context, context.getString(R.string.icon_request_sent), Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Log.e("IconRequest", "Error saving to Firestore", e)
                                            selectedApps = emptySet()
                                            Toast.makeText(context, "Error saving request", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        expanded = true
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (allApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.icon_request_empty),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            RequestInfoSection(
                                totalRequests = freeRequestLimit + premiumAvailable,
                                usedRequests = selectedApps.size + totalAlreadyRequested,
                                availableRequests = max(0, totalAvailableForNewSelection - selectedApps.size)
                            )
                        }

                        if (config.enablePremiumRequest) {
                            item {
                                PremiumRequestSection(
                                    selectedCount = premiumAvailable,
                                    onDisablePremium = { }
                                )
                            }
                        }

                        items(allApps) { app ->
                            val isSelected = selectedApps.contains(app.packageName)
                            val alreadyRequested = requestedIcons.contains(app.packageName)
                            val canSelect = !alreadyRequested && (selectedApps.size < totalAvailableForNewSelection || isSelected)

                            RequestAppItem(
                                app = app,
                                isSelected = isSelected,
                                enabled = canSelect,
                                alreadyRequested = alreadyRequested,
                                onClick = {
                                    if (canSelect) {
                                        selectedApps = if (isSelected) {
                                            selectedApps - app.packageName
                                        } else {
                                            selectedApps + app.packageName
                                        }
                                    } else if (!alreadyRequested && selectedApps.size >= totalAvailableForNewSelection) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.icon_request_limit_reached, freeRequestLimit),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumRequestSection(
    selectedCount: Int,
    onDisablePremium: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.icon_request_premium),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.icon_request_premium_desc),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "${stringResource(R.string.icon_request_selected_label)}: $selectedCount",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDisablePremium) {
            Text(
                text = stringResource(R.string.icon_request_free),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RequestInfoSection(
    totalRequests: Int,
    usedRequests: Int,
    availableRequests: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.icon_request_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.icon_request_select_apps),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$totalRequests ${stringResource(R.string.icon_request_free_label)}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { usedRequests.toFloat() / totalRequests.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.background,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${stringResource(R.string.icon_request_available)}\n$availableRequests",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = "${stringResource(R.string.icon_request_selected_label)}\n$usedRequests",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun RequestAppItem(
    app: MissingApp,
    isSelected: Boolean,
    enabled: Boolean = true,
    alreadyRequested: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val alpha = if (alreadyRequested) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && !alreadyRequested, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = app.icon
            if (icon != null) {
                Image(
                    bitmap = drawableToBitmapSafe(icon).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = app.name,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (alreadyRequested) stringResource(R.string.icon_request_already_sent)
                           else app.packageName,
                    color = if (alreadyRequested) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (alreadyRequested) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            width = 2.dp,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

private fun drawableToBitmapSafe(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
