package com.ooredoost.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ooredoost.app.data.CycleMode
import com.ooredoost.app.data.ServiceStatus
import com.ooredoost.app.ui.MainViewModel
import com.ooredoost.app.ui.components.AnimatedButton
import com.ooredoost.app.ui.components.DataGauge
import com.ooredoost.app.ui.components.StatusCard
import com.ooredoost.app.ui.theme.CyberBlue
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkBackground
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.DarkSurfaceElevated
import com.ooredoost.app.ui.theme.GlassBorder
import com.ooredoost.app.ui.theme.NeonPurple
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextPrimary
import com.ooredoost.app.ui.theme.TextSecondary
import com.ooredoost.app.util.DataFormatter

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSetup: () -> Unit
) {
    val context = LocalContext.current
    val serviceState by viewModel.serviceState.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val manualInterval by viewModel.manualInterval.collectAsStateWithLifecycle()
    val totalDataBytes by viewModel.totalDataBytes.collectAsStateWithLifecycle()

    var selectedMode by remember { mutableStateOf(CycleMode.SMART) }
    var sliderValue by remember { mutableFloatStateOf(manualInterval.toFloat()) }
    var visible by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.checkPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(manualInterval) {
        sliderValue = manualInterval.toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -40 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OoreDoost",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = OoredooRed
                )
                Text(
                    text = "أوريدوست",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Permission warning
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        OoredooRed.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, OoredooRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onNavigateToSetup() }
                    .padding(16.dp)
            ) {
                Text(
                    text = "⚠️ صلاحية ADB غير ممنوحة - اضغط هنا للإعداد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OoredooRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Data Gauge
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 40 }
        ) {
            DataGauge(
                currentBytes = serviceState.totalSessionBytes,
                isActive = serviceState.isRunning,
                isDataFlowing = serviceState.isDataFlowing,
                size = 220.dp,
                strokeWidth = 14.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total data (all sessions)
        if (totalDataBytes > 0) {
            Text(
                text = "الإجمالي: ${DataFormatter.formatBytes(totalDataBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Selector
        AnimatedVisibility(
            visible = visible && !serviceState.isRunning,
            enter = fadeIn()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Smart Mode
                    ModeCard(
                        icon = Icons.Rounded.SmartToy,
                        title = "ذكي",
                        subtitle = "تلقائي بالكامل",
                        isSelected = selectedMode == CycleMode.SMART,
                        color = NeonPurple,
                        onClick = { selectedMode = CycleMode.SMART },
                        modifier = Modifier.weight(1f)
                    )

                    // Manual Mode
                    ModeCard(
                        icon = Icons.Rounded.Timer,
                        title = "يدوي",
                        subtitle = "تحكم بالفاصل",
                        isSelected = selectedMode == CycleMode.MANUAL,
                        color = CyberBlue,
                        onClick = { selectedMode = CycleMode.MANUAL },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Manual interval slider
                if (selectedMode == CycleMode.MANUAL) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                DarkSurfaceElevated,
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الفاصل الزمني",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${sliderValue.toInt()} ثانية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = sliderValue,
                                onValueChange = { sliderValue = it },
                                onValueChangeFinished = {
                                    viewModel.setManualInterval(sliderValue.toInt())
                                },
                                valueRange = 3f..120f,
                                steps = 116,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberBlue,
                                    activeTrackColor = CyberBlue,
                                    inactiveTrackColor = DarkSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start/Stop Button
        AnimatedButton(
            isRunning = serviceState.isRunning,
            isDataFlowing = serviceState.isDataFlowing,
            onClick = {
                if (serviceState.isRunning) {
                    viewModel.stopService(context)
                } else {
                    if (!hasPermission) {
                        onNavigateToSetup()
                        return@AnimatedButton
                    }
                    when (selectedMode) {
                        CycleMode.SMART -> viewModel.startSmartMode(context)
                        CycleMode.MANUAL -> viewModel.startManualMode(context, sliderValue.toInt())
                    }
                }
            },
            size = 80.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Card (when running)
        if (serviceState.isRunning || serviceState.status != ServiceStatus.IDLE) {
            StatusCard(
                status = serviceState.status,
                statusMessage = serviceState.statusMessage,
                cycleCount = serviceState.cycleCount,
                burstCount = serviceState.burstCount
            )

            // Current burst info
            if (serviceState.isDataFlowing) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CyberGreen.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📡 الدفعة الحالية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberGreen
                        )
                        Text(
                            text = DataFormatter.formatBytesPrecise(serviceState.currentBurstBytes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) color.copy(alpha = 0.12f) else DarkSurfaceElevated
    val borderColor = if (isSelected) color.copy(alpha = 0.4f) else GlassBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (isSelected) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) color else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
