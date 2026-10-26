package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.R
import kotlinx.coroutines.delay

@Composable
fun DaveSplashIntro(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1300)
        isVisible = false
        delay(300) // allow fade out animation to finish
        onFinish()
    }

    // Scale animation with smooth spring
    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    // Alpha animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "LogoAlpha"
    )

    // Infinite breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "AuraBreathing")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    // Círculo de invocación: anillo rúnico rotatorio alrededor del logo
    val invokeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InvokeRotation"
    )

    val lunarPhase = remember { com.auraplayer.audio.LunarPhaseManager.getCurrentPhase() }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(300)) + scaleOut(targetScale = 1.08f, animationSpec = tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030712), // Jet Black
                            Color(0xFF090D1A), // Deep Midnight Navy
                            Color(0xFF020408)  // Abyss
                        )
                    )
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onFinish()
                },
            contentAlignment = Alignment.Center
        ) {
            // Ambient Neon Glow Aura behind logo
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(auraScale)
                    .alpha(auraAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.45f), // Cyan glow
                                Color(0xFF8B5CF6).copy(alpha = 0.30f), // Neon purple
                                Color.Transparent
                            )
                        )
                    )
            )

            // Círculo de invocación arcano: geometría esotérica rotando tras el badge
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(230.dp)
                    .graphicsLayer { rotationZ = invokeRotation }
            ) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.46f
                val ringColor = Color(0xFF818CF8).copy(alpha = 0.5f)

                // Anillo punteado exterior
                drawCircle(
                    color = ringColor,
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(16f, 22f), 0f)
                    )
                )

                // 6 nodos rúnicos (circulitos) sobre el anillo
                for (k in 0 until 6) {
                    val rad = Math.toRadians((k * 60).toDouble()).toFloat()
                    val nx = center.x + radius * kotlin.math.cos(rad)
                    val ny = center.y + radius * kotlin.math.sin(rad)
                    drawCircle(
                        color = Color(0xFFC084FC).copy(alpha = 0.8f),
                        radius = 3f,
                        center = androidx.compose.ui.geometry.Offset(nx, ny)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(nx, ny),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }

                // Triángulo de invocación interior
                val triPath = androidx.compose.ui.graphics.Path().apply {
                    for (k in 0 until 3) {
                        val rad = Math.toRadians((k * 120 - 90).toDouble()).toFloat()
                        val px = center.x + radius * 0.72f * kotlin.math.cos(rad)
                        val py = center.y + radius * 0.72f * kotlin.math.sin(rad)
                        if (k == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(
                    triPath,
                    color = Color(0xFF8B5CF6).copy(alpha = 0.30f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // Outer Neon Ring Badge
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF818CF8),
                                    Color(0xFFC084FC)
                                )
                            )
                        )
                        .padding(3.dp), // Border thickness
                    contentAlignment = Alignment.Center
                ) {
                    // Dark Inner Badge
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0A0F1D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dave_foreground),
                            contentDescription = "DaVE Logo",
                            modifier = Modifier
                                .size(95.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Name with Gradient
                Text(
                    text = "DaVE",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8), // Electric Cyan
                                Color(0xFFA78BFA), // Lavender Neon
                                Color(0xFFF472B6)  // Pink Rose
                            )
                        )
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "S T U D I O   S O U N D",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fase lunar real del día: el cielo acompaña la invocación
                Text(
                    text = "${lunarPhase.phaseEmoji} ${lunarPhase.illuminationPercent}% · ᛟ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = Color(0xFF818CF8)
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Animated Equalizer Frequency Bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barHeights = listOf(14.dp, 24.dp, 34.dp, 20.dp, 12.dp)
                    val barColors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF818CF8),
                        Color(0xFFA855F7),
                        Color(0xFF818CF8),
                        Color(0xFF38BDF8)
                    )

                    barHeights.forEachIndexed { index, defaultHeight ->
                        val animBarHeight by infiniteTransition.animateFloat(
                            initialValue = 0.35f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 350 + (index * 110),
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "BarHeight$index"
                        )

                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(defaultHeight * animBarHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColors[index])
                        )
                    }
                }
            }
        }
    }
}
