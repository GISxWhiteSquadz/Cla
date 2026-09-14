package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    language: String = "DE",
    onFinish: () -> Unit
) {
    val isEn = language.uppercase() == "EN"

    // Animation visibility states for fade-in transitions
    var isLogoVisible by remember { mutableStateOf(false) }
    var isTextVisible by remember { mutableStateOf(false) }
    var statusStage by remember { mutableIntStateOf(0) }

    // Subtle breathing pulse for outer glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        // Trigger smooth staggered entrance
        delay(100)
        isLogoVisible = true
        delay(350)
        isTextVisible = true
        delay(800)
        statusStage = 1
        delay(1200)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.16f),
                        DarkBackground,
                        Color(0xFF030712)
                    ),
                    radius = 1200f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap anywhere to skip instantly
                onFinish()
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animated Logo with AnimatedVisibility Fade-in & Scale-in
            AnimatedVisibility(
                visible = isLogoVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.82f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(154.dp)
                        .testTag("splash_logo")
                ) {
                    // Outer glowing ambient ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        PrimaryBlue.copy(alpha = pulseAlpha),
                                        SecondaryTeal.copy(alpha = pulseAlpha * 0.7f),
                                        PrimaryBlue.copy(alpha = pulseAlpha)
                                    )
                                )
                            )
                    )

                    // Inner logo container
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF090D16))
                            .border(
                                width = 1.5.dp,
                                color = PrimaryBlue.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Repair-Agent by CoreSystems Logo",
                            modifier = Modifier
                                .size(118.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Typography with AnimatedVisibility Fade-in
            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("splash_title")
                ) {
                    Text(
                        text = "Repair-Agent",
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "by CoreSystems",
                        color = PrimaryBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Badge: CoreRepair AI
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface)
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CoreRepair AI • Hardware Diagnostics",
                                color = SecondaryTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Crossfade status transition
                    Crossfade(
                        targetState = statusStage,
                        animationSpec = tween(durationMillis = 400),
                        label = "statusCrossfade"
                    ) { stage ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statusText = when (stage) {
                                0 -> if (isEn) "Initializing local models & diagnostics..." else "Lade KI-Modelle & Diagnose-Profile..."
                                else -> if (isEn) "Diagnostic suite ready" else "Reparatur-Agent bereit"
                            }
                            Text(
                                text = statusText,
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Tap to skip hint at the bottom
        AnimatedVisibility(
            visible = isTextVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = if (isEn) "Tap anywhere to continue" else "Tippen zum Fortfahren",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
