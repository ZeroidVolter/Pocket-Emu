package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SpaceBlack

@Composable
fun BackgroundLaser(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    // Generate an infinite scroll animation for the matrix grid lines
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_grid_anim")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_offset"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gridSpacing = 60f

            // 1. Draw central glowing nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x1F02EAF4), // Glowing cyan
                        Color(0x0A9B5DE5), // Translucent violet
                        Color(0x00000000)
                    ),
                    center = Offset(width / 2f, height / 2f),
                    radius = width.coerceAtLeast(height) * 0.45f
                )
            )

            // 2. Vertical Gridlines
            var x = gridOffset
            while (x < width) {
                drawLine(
                    color = Color(0x1202EAF4),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 2f
                )
                x += gridSpacing
            }

            // 3. Horizontal Gridlines
            var y = gridOffset
            while (y < height) {
                drawLine(
                    color = Color(0x1202EAF4),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f
                )
                y += gridSpacing
            }

            // 4. Tech Corner Highlights & Crosshairs
            val borderGlow = Color(0xFF02EAF4).copy(alpha = pulseAlpha)
            val padding = 40f
            
            // Top-Left Tech L
            drawLine(borderGlow, Offset(padding, padding), Offset(padding + 50f, padding), 4f)
            drawLine(borderGlow, Offset(padding, padding), Offset(padding, padding + 50f), 4f)

            // Top-Right Tech L
            drawLine(borderGlow, Offset(width - padding, padding), Offset(width - padding - 50f, padding), 4f)
            drawLine(borderGlow, Offset(width - padding, padding), Offset(width - padding, padding + 50f), 4f)

            // Bottom-Left Tech L
            drawLine(borderGlow, Offset(padding, height - padding), Offset(padding + 50f, height - padding), 4f)
            drawLine(borderGlow, Offset(padding, height - padding), Offset(padding, height - padding - 50f), 4f)

            // Bottom-Right Tech L
            drawLine(borderGlow, Offset(width - padding, height - padding), Offset(width - padding - 50f, height - padding), 4f)
            drawLine(borderGlow, Offset(width - padding, height - padding), Offset(width - padding, height - padding - 50f), 4f)

            // Tech center crosshair (small decor)
            val cx = width / 2f
            val cy = height / 3f
            drawLine(Color(0x3302EAF4), Offset(cx - 20f, cy), Offset(cx + 20f, cy), 2f)
            drawLine(Color(0x3302EAF4), Offset(cx, cy - 20f), Offset(cx, cy + 20f), 2f)
            drawCircle(Color(0x2202EAF4), radius = 10f, center = Offset(cx, cy))
        }

        content()
    }
}
