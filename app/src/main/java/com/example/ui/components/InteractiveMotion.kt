package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimaryBlue

/**
 * Interactive bounce modifier: Gently scales down to 0.95f when pressed
 * and springs back smoothly upon release, giving responsive, lively tactile feedback.
 */
fun Modifier.interactiveBounce(
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonBounceScale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Subtle pulsing glowing aura for primary action buttons, giving an alive, premium look.
 */
fun Modifier.pulsingGlow(
    glowColor: Color = PrimaryBlue,
    isPulsing: Boolean = true
): Modifier = composed {
    if (!isPulsing) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGlowTransition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val radiusSpread by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    this.drawBehind {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = alpha * 0.45f),
                    glowColor.copy(alpha = alpha * 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = size.maxDimension / 1.8f + radiusSpread
            )
        )
    }
}

/**
 * Calculates a smooth scroll progress (0.0f = at top, 1.0f = deeply scrolled)
 * from a LazyListState.
 */
@Composable
fun LazyListState.calculateScrollProgress(maxItemIndexThreshold: Int = 5): Float {
    val progress by remember {
        derivedStateOf {
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems <= 1) return@derivedStateOf 0f

            val currentIndex = firstVisibleItemIndex
            val offset = firstVisibleItemScrollOffset
            val firstItemSize = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 100

            val raw = (currentIndex + (offset.toFloat() / firstItemSize.coerceAtLeast(1))) / maxItemIndexThreshold.toFloat()
            raw.coerceIn(0f, 1f)
        }
    }
    return progress
}

/**
 * Atmospheric background that shifts hue & depth based on scrolling:
 * - Top (0.0): Deep Slate-Navy (#0A0E1A) with Cyan/Blue ambient glow
 * - Middle (0.5): Deep Electric Twilight Violet (#110D22)
 * - Deep (1.0): Warm Burnished Amber & Dark Obsidian (#160F06)
 * Scrolling back up reverses the transition smoothly!
 */
fun Modifier.scrollReactiveBackground(
    scrollProgress: Float,
    baseBackground: Color = DarkBackground
): Modifier = this.drawBehind {
    val progress = scrollProgress.coerceIn(0f, 1f)

    // Dynamic top ambient color
    val topAmbient = when {
        progress < 0.5f -> {
            val factor = progress * 2f
            lerp(Color(0xFF0D1E36), Color(0xFF1B1238), factor) // Cyan/Blue -> Electric Violet
        }
        else -> {
            val factor = (progress - 0.5f) * 2f
            lerp(Color(0xFF1B1238), Color(0xFF261608), factor) // Electric Violet -> Warm Amber/Bronze
        }
    }

    // Dynamic bottom ambient color
    val bottomAmbient = when {
        progress < 0.5f -> {
            val factor = progress * 2f
            lerp(Color(0xFF080C14), Color(0xFF120B1F), factor)
        }
        else -> {
            val factor = (progress - 0.5f) * 2f
            lerp(Color(0xFF120B1F), Color(0xFF1A1005), factor)
        }
    }

    val baseOverlay = lerp(baseBackground, Color(0xFF0F141F), progress * 0.3f)

    // Solid base
    drawRect(color = baseOverlay)

    // Dynamic directional gradient overlay
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                topAmbient.copy(alpha = 0.55f),
                topAmbient.copy(alpha = 0.20f),
                bottomAmbient.copy(alpha = 0.40f)
            ),
            startY = 0f,
            endY = size.height
        )
    )

    // Subtle ambient radial glow that follows scroll progress downward
    val glowCenterY = size.height * (0.15f + progress * 0.70f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                topAmbient.copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.85f, glowCenterY),
            radius = size.width * 0.95f
        )
    )
}
