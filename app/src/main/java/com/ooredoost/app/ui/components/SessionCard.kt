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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ooredoost.app.data.SessionEntity
import com.ooredoost.app.ui.theme.CyberBlue
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.DarkSurfaceElevated
import com.ooredoost.app.ui.theme.GlassBorder
import com.ooredoost.app.ui.theme.NeonPurple
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.StatusError
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextPrimary
import com.ooredoost.app.ui.theme.TextSecondary
import com.ooredoost.app.util.DataFormatter

/**
 * Card displaying a single session's details in the history.
 */
@Composable
fun SessionCard(
    session: SessionEntity,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSmartMode = session.mode == "smart"
    val modeColor = if (isSmartMode) NeonPurple else CyberBlue
    val modeIcon = if (isSmartMode) Icons.Rounded.SmartToy else Icons.Rounded.Timer
    val modeLabel = if (isSmartMode) "ذكي" else "يدوي"
    val duration = session.endTime - session.startTime

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkSurfaceElevated,
                        DarkSurface
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header: mode + date + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(modeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = modeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = modeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = modeColor
                        )
                        Text(
                            text = DataFormatter.formatTimestamp(session.startTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "حذف",
                            tint = StatusError.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Data consumed - main highlight
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.DataUsage,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = DataFormatter.formatBytesPrecise(session.dataBytes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailChip(
                    icon = Icons.Rounded.Autorenew,
                    label = "${session.cycleCount} دورات",
                    color = CyberBlue
                )
                DetailChip(
                    icon = Icons.Rounded.CellTower,
                    label = "${session.burstCount} دفعات",
                    color = CyberGreen
                )
                DetailChip(
                    icon = Icons.Rounded.AccessTime,
                    label = DataFormatter.formatDuration(duration),
                    color = OoredooRed
                )
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
