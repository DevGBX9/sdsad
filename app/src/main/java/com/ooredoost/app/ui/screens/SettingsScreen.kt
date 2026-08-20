package com.ooredoost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ooredoost.app.ui.MainViewModel
import com.ooredoost.app.ui.theme.CyberBlue
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkBackground
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.DarkSurfaceElevated
import com.ooredoost.app.ui.theme.GlassBorder
import com.ooredoost.app.ui.theme.GoldenYellow
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextPrimary
import com.ooredoost.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()

    var airplaneDuration by remember { mutableIntStateOf(1000) }
    var monitoringSensitivity by remember { mutableIntStateOf(100) }
    var dataTimeout by remember { mutableIntStateOf(3000) }

    LaunchedEffect(Unit) {
        airplaneDuration = viewModel.preferencesManager.airplaneOnDuration.first()
        monitoringSensitivity = viewModel.preferencesManager.monitoringSensitivity.first()
        dataTimeout = viewModel.preferencesManager.dataTimeout.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = OoredooRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "الإعدادات",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ═══════════════════════════════════════
        // Timing Settings
        // ═══════════════════════════════════════
        SectionHeader(icon = Icons.Rounded.Timer, title = "إعدادات التوقيت")

        Spacer(modifier = Modifier.height(12.dp))

        // Airplane mode ON duration
        SettingSlider(
            title = "مدة وضع الطيران",
            description = "كم يبقى وضع الطيران مفعلاً",
            value = airplaneDuration.toFloat(),
            valueRange = 500f..3000f,
            steps = 24,
            valueLabel = "${airplaneDuration}ms",
            color = OoredooRed,
            onValueChange = { airplaneDuration = it.toInt() },
            onValueChangeFinished = {
                scope.launch {
                    viewModel.preferencesManager.setAirplaneOnDuration(airplaneDuration)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data timeout
        SettingSlider(
            title = "مهلة انتهاء البيانات",
            description = "كم ينتظر قبل اعتبار الدفعة انتهت",
            value = dataTimeout.toFloat(),
            valueRange = 1000f..8000f,
            steps = 69,
            valueLabel = "${dataTimeout}ms",
            color = GoldenYellow,
            onValueChange = { dataTimeout = it.toInt() },
            onValueChangeFinished = {
                scope.launch {
                    viewModel.preferencesManager.setDataTimeout(dataTimeout)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ═══════════════════════════════════════
        // Monitoring Settings
        // ═══════════════════════════════════════
        SectionHeader(icon = Icons.Rounded.Speed, title = "إعدادات المراقبة")

        Spacer(modifier = Modifier.height(12.dp))

        SettingSlider(
            title = "دقة المراقبة",
            description = "كل كم ميلي ثانية يتم فحص البيانات",
            value = monitoringSensitivity.toFloat(),
            valueRange = 50f..500f,
            steps = 44,
            valueLabel = "${monitoringSensitivity}ms",
            color = CyberGreen,
            onValueChange = { monitoringSensitivity = it.toInt() },
            onValueChangeFinished = {
                scope.launch {
                    viewModel.preferencesManager.setMonitoringSensitivity(monitoringSensitivity)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ═══════════════════════════════════════
        // App Info
        // ═══════════════════════════════════════
        SectionHeader(icon = Icons.Rounded.Info, title = "حول التطبيق")

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                InfoRow("الإصدار", "1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("المطور", "OoreDoost Team")
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("الشبكة المدعومة", "Ooredoo Algeria")
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("الجهاز المحسن", "Infinix Hot 50i")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingSlider(
    title: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    color: androidx.compose.ui.graphics.Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = DarkSurface
                )
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
