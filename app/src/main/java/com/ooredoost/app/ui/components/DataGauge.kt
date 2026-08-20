package com.ooredoost.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkSurfaceVariant
import com.ooredoost.app.ui.theme.GradientRedEnd
import com.ooredoost.app.ui.theme.GradientRedStart
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextSecondary
import com.ooredoost.app.util.DataFormatter

/**
 * Animated circular gauge displaying data usage with gradient arc.
 */
@Composable
fun DataGauge(
    currentBytes: Long,
    isActive: Boolean,
    isDataFlowing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp
) {
    val (valueText, unitText) = DataFormatter.formatBytesComponents(currentBytes)

    // Animate the gauge fill based on data flowing
    val targetSweep = when {
        isDataFlowing -> 300f
        isActive -> 120f
        else -> 0f
    }
    val sweepAngle by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    // Pulsing glow when data is flowing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Rotation animation when active
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(
                canvasSize.width - strokePx,
                canvasSize.height - strokePx
            )
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background track
            drawArc(
                color = DarkSurfaceVariant,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (isActive) {
                // Active gradient arc
                val arcColor = if (isDataFlowing) {
                    Brush.sweepGradient(
                        colors = listOf(
                            CyberGreen.copy(alpha = pulseAlpha * 0.5f),
                            CyberGreen.copy(alpha = pulseAlpha),
                            CyberGreen.copy(alpha = pulseAlpha * 0.7f)
                        )
                    )
                } else {
                    Brush.sweepGradient(
                        colors = listOf(
                            GradientRedStart.copy(alpha = 0.5f),
                            GradientRedEnd,
                            OoredooRed
                        )
                    )
                }

                drawArc(
                    brush = arcColor,
                    startAngle = 135f,
                    sweepAngle = sweepAngle * 270f / 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glow effect when data is flowing
                if (isDataFlowing) {
                    drawArc(
                        color = CyberGreen.copy(alpha = pulseAlpha * 0.15f),
                        startAngle = 135f,
                        sweepAngle = sweepAngle * 270f / 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 2.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                ),
                color = if (isDataFlowing) CyberGreen else if (isActive) Color.White else TextMuted
            )
            Text(
                text = unitText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDataFlowing) CyberGreen.copy(alpha = 0.7f) else TextSecondary
            )
        }
    }
}
