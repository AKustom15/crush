package com.akustom15.crush.ui.screens.dashboard

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.Log
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
import com.akustom15.crush.iconpack.IconPackManager
import com.akustom15.crush.ui.components.RotatingIconAnimation
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@Composable
fun DashboardScreen(
    config: CrushConfig,
    bottomContentPadding: Dp = 0.dp
) {
    val context = LocalContext.current

    val iconResourceNames = remember(context, config.appFilterXmlRes) {
        if (config.appFilterXmlRes != 0) {
            loadIconNamesFromAppFilter(context, config.appFilterXmlRes)
        } else {
            emptyList()
        }
    }

    val counters = remember(context) {
        IconPackManager.getAppCounters(context)
    }

    var totalIcons by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        totalIcons = try {
            IconPackManager.getTotalIconsCount(context)
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
                            IconPackManager.showLauncherSelector(context)
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
                        modifier = Modifier.fillMaxWidth()
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
                                text = "${counters.totalApps} ${stringResource(R.string.total_apps_label)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${counters.themedApps} ${stringResource(R.string.themed_apps_label)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Progress bar
                            val progress = if (counters.totalApps > 0)
                                counters.themedApps.toFloat() / counters.totalApps else 0f
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
                                text = "${counters.missingApps} ${stringResource(R.string.missing_apps_label)}",
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
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

private fun loadIconNamesFromAppFilter(context: Context, xmlRes: Int): List<String> {
    val iconNames = mutableListOf<String>()
    var parser: XmlResourceParser? = null
    try {
        parser = context.resources.getXml(xmlRes)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                for (i in 0 until parser.attributeCount) {
                    if (parser.getAttributeName(i) == "drawable") {
                        parser.getAttributeValue(i)?.let { iconNames.add(it) }
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (e: XmlPullParserException) {
        Log.e("DashboardScreen", "Error parsing appfilter XML", e)
    } catch (e: IOException) {
        Log.e("DashboardScreen", "Error reading appfilter XML", e)
    } catch (e: Exception) {
        Log.e("DashboardScreen", "Unexpected error loading icons from appfilter", e)
    } finally {
        parser?.close()
    }
    return iconNames
}
