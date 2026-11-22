package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.R
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.LunarPhaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DaveSplashIntro(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val view = LocalView.current

    // Animators for cinematic sequences
    val vortexProgress = remember { Animatable(0f) }
    val shockwaveProgress = remember { Animatable(0f) }
    val logoScaleAnim = remember { Animatable(0.25f) }
    val logoAlphaAnim = remember { Animatable(0f) }
    val logoRotationYAnim = remember { Animatable(70f) }
    val textAlphaAnim = remember { Animatable(0f) }
    val shimmerAnim = remember { Animatable(0f) }

    val lunarPhase = remember { LunarPhaseManager.getCurrentPhase() }

    // Seeded background stars
    val stars = remember {
        val rng = kotlin.random.Random(1337)
        List(50) {
            Triple(
                rng.nextFloat(),
                rng.nextFloat(),
                rng.nextFloat() * 2.2f + 1f
            )
        }
    }

    // Continuous infinite transitions
    val infiniteTransition = rememberInfiniteTransition(label = "IntroInfinite")

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    val outerRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    val plasmaRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PlasmaRotation"
    )

    val auraBreathingScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraBreathingScale"
    )

    val auraBreathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraBreathingAlpha"
    )

    // Master cinematic timeline
    LaunchedEffect(Unit) {
        // Phase 1: Inflow Quantum Vortex (0 - 400ms)
        vortexProgress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))

        // Phase 2: Supernova Detonation & Haptic Blast (400ms)
        AuraHaptic.heavy(view)

        launch {
            shockwaveProgress.animateTo(1f, tween(650, easing = LinearOutSlowInEasing))
        }
        launch {
            logoScaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlphaAnim.animateTo(1f, tween(300))
        }
        launch {
            logoRotationYAnim.animateTo(0f, tween(550, easing = FastOutSlowInEasing))
        }
        launch {
            delay(150)
            textAlphaAnim.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            delay(350)
            shimmerAnim.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }

        // Phase 3: Hold the sacred resonance & prepare exit
        delay(1250)
        isVisible = false
        delay(360) // allow portal dissolve exit animation to conclude
        onFinish()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(350)) + scaleOut(targetScale = 1.30f, animationSpec = tween(350, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF13092B), // Deep Amethyst Nebula Core
                            Color(0xFF070314), // Midnight Void
                            Color(0xFF010005)  // Absolute Space Abyss
                        )
                    )
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    AuraHaptic.click(view)
                    onFinish()
                },
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas: Twinkling Stardust Field
            Canvas(modifier = Modifier.fillMaxSize()) {
                stars.forEachIndexed { idx, (xr, yr, sz) ->
                    val alpha = if (idx % 2 == 0) starTwinkle else (1.35f - starTwinkle).coerceIn(0.2f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.75f),
                        radius = sz.dp.toPx(),
                        center = Offset(size.width * xr, size.height * yr)
                    )
                }
            }

            // Phase 1: Inflow Quantum Sparks Vortex
            if (vortexProgress.value < 0.98f) {
                val invProg = 1f - vortexProgress.value
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxDist = size.minDimension * 0.65f
                    for (i in 0 until 24) {
                        val baseAngle = (i * (360f / 24f) * (PI / 180f)).toFloat()
                        val angle = baseAngle + (vortexProgress.value * 3.8f)
                        val currentDist = maxDist * invProg * (0.6f + (i % 5) * 0.1f)
                        val px = center.x + currentDist * cos(angle)
                        val py = center.y + currentDist * sin(angle)
                        val particleAlpha = (vortexProgress.value * 1.5f).coerceIn(0f, 1f) * (1f - invProg * 0.3f)
                        val pColor = if (i % 2 == 0) Color(0xFF00E5FF) else Color(0xFFE040FB)

                        drawCircle(
                            color = pColor.copy(alpha = particleAlpha.coerceIn(0f, 0.95f)),
                            radius = (2.2f + (i % 3) * 1.3f).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Phase 2: Expanding Supernova Shockwave Waves
            if (shockwaveProgress.value > 0.01f && shockwaveProgress.value < 0.99f) {
                val prog = shockwaveProgress.value
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension * 0.95f
                    val r1 = maxR * prog
                    val alpha1 = (1f - prog).coerceIn(0f, 1f)

                    // Wave 1: Celestial Cyan
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = alpha1 * 0.65f),
                        radius = r1,
                        center = center,
                        style = Stroke(width = (6.dp.toPx() * (1f - prog)).coerceAtLeast(1.5f))
                    )
                    // Wave 2: Radiant Amethyst
                    if (prog > 0.15f) {
                        val prog2 = (prog - 0.15f) / 0.85f
                        val r2 = maxR * prog2
                        val alpha2 = (1f - prog2).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFFE040FB).copy(alpha = alpha2 * 0.5f),
                            radius = r2,
                            center = center,
                            style = Stroke(width = (4.dp.toPx() * (1f - prog2)).coerceAtLeast(1.2f))
                        )
                    }
                    // Central luminous burst flash
                    if (prog < 0.5f) {
                        val flashAlpha = (1f - (prog * 2f)).coerceIn(0f, 1f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = flashAlpha * 0.55f),
                                    Color(0xFF00E5FF).copy(alpha = flashAlpha * 0.35f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = maxR * 0.45f
                            ),
                            radius = maxR * 0.45f,
                            center = center
                        )
                    }
                }
            }

            // Ambient Radiant Breathing Nebula Glow behind Logo
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(auraBreathingScale)
                    .alpha(auraBreathingAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.40f),
                                Color(0xFF8B5CF6).copy(alpha = 0.30f),
                                Color(0xFFE040FB).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer Sacred Astrolabe & Runic Ring (250dp)
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .graphicsLayer { rotationZ = outerRingRotation }
            ) {
                // Astrolabe geometry canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 6.dp.toPx()

                    // Perimeter Ring
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Inner Dotted Orbit
                    drawCircle(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.35f),
                        radius = radius * 0.88f,
                        center = center,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 16f), 0f)
                        )
                    )
                    // 32 Precision Clockwork / Astrolabe Ticks
                    for (step in 0 until 32) {
                        val angleRad = (step * (360f / 32f) * (PI / 180f)).toFloat()
                        val isMajor = step % 4 == 0
                        val tickLen = if (isMajor) 8.dp.toPx() else 3.5.dp.toPx()
                        val startR = radius - tickLen
                        val startX = center.x + startR * cos(angleRad)
                        val startY = center.y + startR * sin(angleRad)
                        val endX = center.x + radius * cos(angleRad)
                        val endY = center.y + radius * sin(angleRad)
                        drawLine(
                            color = if (isMajor) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color(0xFF818CF8).copy(alpha = 0.35f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 1.6f else 1f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 8 Arcane Nordic Runes around circumference
                val runes = listOf("ᛟ", "ᚱ", "ᚦ", "ᛉ", "ᚲ", "ᛊ", "ᛗ", "ᛚ")
                runes.forEachIndexed { i, rune ->
                    val angleDeg = i * 45f
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = angleDeg },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = rune,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE).copy(alpha = 0.90f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Inner Liquid Plasma Orbit (175dp) in counter-rotation
            Canvas(
                modifier = Modifier
                    .size(175.dp)
                    .scale(auraBreathingScale)
                    .graphicsLayer { rotationZ = plasmaRotation }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseR = size.minDimension * 0.44f
                val plasmaPath = Path()
                val numPoints = 16
                for (i in 0 until numPoints) {
                    val angle = (i * (360f / numPoints) * (PI / 180f)).toFloat()
                    val waveOffset = sin(i * 1.8f + plasmaRotation * 0.05f) * 5.dp.toPx()
                    val r = baseR + waveOffset
                    val px = center.x + r * cos(angle)
                    val py = center.y + r * sin(angle)
                    if (i == 0) plasmaPath.moveTo(px, py) else plasmaPath.lineTo(px, py)
                }
                plasmaPath.close()

                drawPath(
                    path = plasmaPath,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFF00E5FF)
                        )
                    ),
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central Branding Column with 3D Flip & Zoom Spring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(logoScaleAnim.value)
                    .alpha(logoAlphaAnim.value)
            ) {
                // Holographic 3D Logo Badge
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .graphicsLayer {
                            rotationY = logoRotationYAnim.value
                            cameraDistance = 16f * density
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF818CF8),
                                    Color(0xFFEC4899),
                                    Color(0xFF00E5FF)
                                )
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF070514)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dave_foreground),
                            contentDescription = "DaVE Logo",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )

                        // Shimmer Light Sweep across Logo
                        if (shimmerAnim.value > 0.01f && shimmerAnim.value < 0.99f) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val shimmerX = shimmerAnim.value * 220.dp.toPx() - 60.dp.toPx()
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.65f),
                                            Color.Transparent
                                        ),
                                        startX = shimmerX - 35f,
                                        endX = shimmerX + 35f
                                    ),
                                    start = Offset(shimmerX, 0f),
                                    end = Offset(shimmerX, size.height),
                                    strokeWidth = size.width
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Brand Name with High-End Chromatic Gradient
                Box(
                    modifier = Modifier.alpha(textAlphaAnim.value),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DaVE",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00F0FF), // Electric Cyan
                                    Color(0xFFC084FC), // Radiant Lavender
                                    Color(0xFFF43F5E)  // Electric Rose
                                )
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "S T U D I O   S O U N D",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.5.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.alpha(textAlphaAnim.value)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Sacred Lunar Resonance Indicator
                Text(
                    text = "✨ ${lunarPhase.phaseEmoji} ${lunarPhase.illuminationPercent}% · 432 Hz RESONANCIA ᛟ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.alpha(textAlphaAnim.value)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 9-Band Harmonic Dancing Spectrum Equalizer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(textAlphaAnim.value)
                ) {
                    val barBaseHeights = listOf(10.dp, 18.dp, 26.dp, 36.dp, 44.dp, 36.dp, 26.dp, 18.dp, 10.dp)
                    val barColors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF38BDF8),
                        Color(0xFF818CF8),
                        Color(0xFFA855F7),
                        Color(0xFFE040FB),
                        Color(0xFFA855F7),
                        Color(0xFF818CF8),
                        Color(0xFF38BDF8),
                        Color(0xFF00E5FF)
                    )

                    barBaseHeights.forEachIndexed { index, baseHeight ->
                        val animMultiplier by infiniteTransition.animateFloat(
                            initialValue = 0.25f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 320 + (index * 80),
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "EqualizerBar$index"
                        )

                        Box(
                            modifier = Modifier
                                .width(3.2.dp)
                                .height(baseHeight * animMultiplier)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColors[index])
                        )
                    }
                }
            }
        }
    }
}
