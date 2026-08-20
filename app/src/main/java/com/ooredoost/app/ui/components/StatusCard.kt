package com.ooredoost.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ooredoost.app.data.ServiceStatus
import com.ooredoost.app.ui.theme.CyberBlue
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkCardBorder
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.DarkSurfaceElevated
import com.ooredoost.app.ui.theme.GlassBorder
import com.ooredoost.app.ui.theme.GlassWhite
import com.ooredoost.app.ui.theme.GoldenYellow
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.StatusError
import com.ooredoost.app.ui.theme.StatusInactive
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextPrimary
import com.ooredoost.app.ui.theme.TextSecondary

/**
 * Card showing current service status with icon and color coding.
 */
@Composable
fun StatusCard(
    status: ServiceStatus,
    statusMessage: String,
    cycleCount: Int,
    burstCount: Int,
    modifier: Modifier = Modifier
) {
    val (icon, color, statusLabel) = getStatusVisuals(status)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkSurfaceElevated,
                            DarkSurface
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                // Status header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "الدورات",
                        value = "$cycleCount",
                        icon = Icons.Rounded.Autorenew,
                        color = CyberBlue
                    )
                    StatItem(
                        label = "الدفعات",
                        value = "$burstCount",
                        icon = Icons.Rounded.CellTower,
                        color = CyberGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

private data class StatusVisuals(
    val icon: ImageVector,
    val color: Color,
    val label: String
)

private fun getStatusVisuals(status: ServiceStatus): StatusVisuals {
    return when (status) {
        ServiceStatus.IDLE -> StatusVisuals(
            Icons.Rounded.Pause, StatusInactive, "متوقف"
        )
        ServiceStatus.ACTIVATING_AIRPLANE -> StatusVisuals(
            Icons.Rounded.AirplanemodeActive, OoredooRed, "تفعيل وضع الطيران"
        )
        ServiceStatus.AIRPLANE_ON -> StatusVisuals(
            Icons.Rounded.AirplanemodeActive, GoldenYellow, "وضع الطيران مفعل"
        )
        ServiceStatus.DEACTIVATING_AIRPLANE -> StatusVisuals(
            Icons.Rounded.AirplanemodeActive, CyberBlue, "إلغاء وضع الطيران"
        )
        ServiceStatus.WAITING_RECONNECT -> StatusVisuals(
            Icons.Rounded.SignalCellularAlt, CyberBlue, "انتظار الاتصال"
        )
        ServiceStatus.MONITORING -> StatusVisuals(
            Icons.Rounded.NetworkCheck, GoldenYellow, "مراقبة البيانات"
        )
        ServiceStatus.DATA_FLOWING -> StatusVisuals(
            Icons.Rounded.DataUsage, CyberGreen, "بيانات متدفقة!"
        )
        ServiceStatus.DATA_STOPPED -> StatusVisuals(
            Icons.Rounded.HourglassTop, OoredooRed, "توقف التدفق"
        )
        ServiceStatus.WAITING_INTERVAL -> StatusVisuals(
            Icons.Rounded.HourglassTop, CyberBlue, "انتظار الفاصل الزمني"
        )
        ServiceStatus.PERMISSION_ERROR -> StatusVisuals(
            Icons.Rounded.ErrorOutline, StatusError, "خطأ في الصلاحيات"
        )
    }
}
