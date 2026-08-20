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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkSurfaceVariant
import com.ooredoost.app.ui.theme.GradientRedEnd
import com.ooredoost.app.ui.theme.GradientRedStart
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.OoredooRedDark

/**
 * Large animated power button for starting/stopping the service.
 * Features pulsing rings when active and gradient background.
 */
@Composable
fun AnimatedButton(
    isRunning: Boolean,
    isDataFlowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buttonPulse")

    // Pulsing scale when running
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Ring expansion
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    // Button scale on press
    val buttonScale by animateFloatAsState(
        targetValue = if (isRunning) pulseScale else 1f,
        animationSpec = tween(300),
        label = "buttonScale"
    )

    val activeColor = if (isDataFlowing) CyberGreen else OoredooRed

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size * 1.8f)
    ) {
        // Pulsing rings (only when running)
        if (isRunning) {
            Canvas(modifier = Modifier.size(size * 1.8f)) {
                val ringColor = if (isDataFlowing) CyberGreen else OoredooRed
                drawCircle(
                    color = ringColor.copy(alpha = ringAlpha * 0.5f),
                    radius = (this.size.minDimension / 2) * ringScale * 0.6f
                )
            }
            Canvas(modifier = Modifier.size(size * 1.8f)) {
                val ringColor = if (isDataFlowing) CyberGreen else OoredooRed
                drawCircle(
                    color = ringColor.copy(alpha = ringAlpha * 0.3f),
                    radius = (this.size.minDimension / 2) * ringScale * 0.45f
                )
            }
        }

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(
                    brush = if (isRunning) {
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor,
                                activeColor.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                GradientRedStart,
                                GradientRedEnd
                            )
                        )
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = if (isRunning) "إيقاف" else "تشغيل",
                tint = Color.White,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}
