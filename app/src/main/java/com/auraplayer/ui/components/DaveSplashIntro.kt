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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * DaveSplashIntro: Introducción cinemática 120 FPS ultra-fluida.
 * Arquitectura de CERO recomposiciones: todos los estados de animación se evalúan
 * exclusivamente en RenderNode / graphicsLayer y DrawScope de hardware.
 */
@Composable
fun DaveSplashIntro(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val view = LocalView.current

    // Animators for cinematic sequences (evaluated purely in graphicsLayer / DrawScope)
    val vortexProgress = remember { Animatable(0f) }
    val shockwaveProgress = remember { Animatable(0f) }
    val logoScaleAnim = remember { Animatable(0.35f) }
    val logoAlphaAnim = remember { Animatable(0f) }
    val logoRotationYAnim = remember { Animatable(60f) }
    val textAlphaAnim = remember { Animatable(0f) }
    val shimmerAnim = remember { Animatable(0f) }

    val lunarPhase = remember { LunarPhaseManager.getCurrentPhase() }

    // Seeded background stars (24 estrellas ligeras, 0 asignaciones por frame)
    val stars = remember {
        val rng = kotlin.random.Random(1337)
        List(24) {
            Triple(
                rng.nextFloat(),
                rng.nextFloat(),
                rng.nextFloat() * 2.2f + 1f
            )
        }
    }

    val cachedPlasmaPath = remember { Path() }
    val sweepBrushColors = remember {
        listOf(
            Color(0xFF00E5FF),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
            Color(0xFF00E5FF)
        )
    }
    val plasmaSweepBrush = remember(sweepBrushColors) {
        Brush.sweepGradient(sweepBrushColors)
    }
    val auraBreathingBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF00E5FF).copy(alpha = 0.40f),
                Color(0xFF8B5CF6).copy(alpha = 0.30f),
                Color(0xFFE040FB).copy(alpha = 0.15f),
                Color.Transparent
            )
        )
    }

    // Continuous infinite transitions (kept as State<Float> to avoid composition phase invalidation)
    val infiniteTransition = rememberInfiniteTransition(label = "IntroInfinite")

    val starTwinkle = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    val outerRingRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    val plasmaRotation = infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PlasmaRotation"
    )

    val auraBreathingScale = infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraBreathingScale"
    )

    val auraBreathingAlpha = infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraBreathingAlpha"
    )

    val eqPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EqPhase"
    )

    // Master cinematic timeline (~900ms snappy and responsive)
    LaunchedEffect(Unit) {
        // Phase 1: Inflow Quantum Vortex (200ms)
        vortexProgress.animateTo(1f, tween(200, easing = FastOutSlowInEasing))

        // Phase 2: Supernova Detonation & Haptic Blast
        AuraHaptic.click(view)

        launch {
            shockwaveProgress.animateTo(1f, tween(360, easing = LinearOutSlowInEasing))
        }
        launch {
            logoScaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            logoAlphaAnim.animateTo(1f, tween(180))
        }
        launch {
            logoRotationYAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
        }
        launch {
            delay(70)
            textAlphaAnim.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
        }
        launch {
            delay(150)
            shimmerAnim.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
        }

        // Phase 3: Hold and smooth portal transition
        delay(550)
        isVisible = false
        delay(180)
        onFinish()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 1.15f, animationSpec = tween(250, easing = FastOutSlowInEasing)),
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
            // Unified Background Canvas: Stardust Field + Inflow Vortex + Supernova Shockwave (0 recompositions)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val twinkle = starTwinkle.value
                stars.forEachIndexed { idx, (xr, yr, sz) ->
                    val alpha = if (idx % 2 == 0) twinkle else (1.35f - twinkle).coerceIn(0.2f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.75f),
                        radius = sz.dp.toPx(),
                        center = Offset(size.width * xr, size.height * yr)
                    )
                }

                val vortexProg = vortexProgress.value
                if (vortexProg < 0.98f) {
                    val invProg = 1f - vortexProg
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxDist = size.minDimension * 0.65f
                    for (i in 0 until 12) {
                        val baseAngle = (i * (360f / 12f) * (PI / 180f)).toFloat()
                        val angle = baseAngle + (vortexProg * 3.8f)
                        val currentDist = maxDist * invProg * (0.6f + (i % 5) * 0.1f)
                        val px = center.x + currentDist * cos(angle)
                        val py = center.y + currentDist * sin(angle)
                        val particleAlpha = (vortexProg * 1.5f).coerceIn(0f, 1f) * (1f - invProg * 0.3f)
                        val pColor = if (i % 2 == 0) Color(0xFF00E5FF) else Color(0xFFE040FB)

                        drawCircle(
                            color = pColor.copy(alpha = particleAlpha.coerceIn(0f, 0.95f)),
                            radius = (2.2f + (i % 3) * 1.3f).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                val shockProg = shockwaveProgress.value
                if (shockProg in 0.01f..0.99f) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension * 0.95f
                    val r1 = maxR * shockProg
                    val alpha1 = (1f - shockProg).coerceIn(0f, 1f)

                    // Wave 1: Celestial Cyan
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = alpha1 * 0.65f),
                        radius = r1,
                        center = center,
                        style = Stroke(width = (5.dp.toPx() * (1f - shockProg)).coerceAtLeast(1.5f))
                    )
                    // Wave 2: Radiant Amethyst
                    if (shockProg > 0.15f) {
                        val prog2 = (shockProg - 0.15f) / 0.85f
                        val r2 = maxR * prog2
                        val alpha2 = (1f - prog2).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFFE040FB).copy(alpha = alpha2 * 0.5f),
                            radius = r2,
                            center = center,
                            style = Stroke(width = (3.5.dp.toPx() * (1f - prog2)).coerceAtLeast(1.2f))
                        )
                    }
                    // Central luminous burst flash
                    if (shockProg < 0.5f) {
                        val flashAlpha = (1f - (shockProg * 2f)).coerceIn(0f, 1f)
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

            // Ambient Radiant Breathing Nebula Glow behind Logo (RenderNode GPU-accelerated)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        scaleX = auraBreathingScale.value
                        scaleY = auraBreathingScale.value
                        alpha = auraBreathingAlpha.value
                    }
                    .clip(CircleShape)
                    .background(auraBreathingBrush)
            )

            // Outer Sacred Astrolabe & Runic Ring (250dp, Hardware Layer Rotation)
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .graphicsLayer { rotationZ = outerRingRotation.value }
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

                // 8 Arcane Nordic Runes around circumference (Static cached layout)
                val runes = remember { listOf("ᛟ", "ᚱ", "ᚦ", "ᛉ", "ᚲ", "ᛊ", "ᛗ", "ᛚ") }
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
                    .graphicsLayer {
                        scaleX = auraBreathingScale.value
                        scaleY = auraBreathingScale.value
                        rotationZ = plasmaRotation.value
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseR = size.minDimension * 0.44f
                val rot = plasmaRotation.value
                cachedPlasmaPath.reset()
                val numPoints = 14
                for (i in 0 until numPoints) {
                    val angle = (i * (360f / numPoints) * (PI / 180f)).toFloat()
                    val waveOffset = sin(i * 1.8f + rot * 0.05f) * 4.5.dp.toPx()
                    val r = baseR + waveOffset
                    val px = center.x + r * cos(angle)
                    val py = center.y + r * sin(angle)
                    if (i == 0) cachedPlasmaPath.moveTo(px, py) else cachedPlasmaPath.lineTo(px, py)
                }
                cachedPlasmaPath.close()

                drawPath(
                    path = cachedPlasmaPath,
                    brush = plasmaSweepBrush,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central Branding Column with 3D Flip & Zoom Spring (0 recompositions)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScaleAnim.value
                    scaleY = logoScaleAnim.value
                    alpha = logoAlphaAnim.value
                }
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

                        // Shimmer Light Sweep across Logo (Hardware DrawScope, 0 recomposition)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val shim = shimmerAnim.value
                            if (shim in 0.01f..0.99f) {
                                val shimmerX = shim * 220.dp.toPx() - 60.dp.toPx()
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

                Spacer(modifier = Modifier.height(20.dp))

                // Brand Name with High-End Chromatic Gradient
                Box(
                    modifier = Modifier.graphicsLayer { alpha = textAlphaAnim.value },
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
                    modifier = Modifier.graphicsLayer { alpha = textAlphaAnim.value }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Sacred Lunar Resonance Indicator
                Text(
                    text = "✨ ${lunarPhase.phaseEmoji} ${lunarPhase.illuminationPercent}% · 432 Hz RESONANCIA ᛟ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.graphicsLayer { alpha = textAlphaAnim.value }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 9-Band Harmonic Dancing Spectrum Equalizer (GPU zero-layout Canvas a 120 FPS, 0 recomposiciones)
                Canvas(
                    modifier = Modifier
                        .size(width = 68.dp, height = 44.dp)
                        .graphicsLayer { alpha = textAlphaAnim.value }
                ) {
                    val phase = eqPhase.value
                    val count = 9
                    val barW = 3.5.dp.toPx()
                    val gap = (size.width - (count * barW)) / (count - 1).coerceAtLeast(1)
                    val baseHeights = floatArrayOf(
                        12.dp.toPx(), 20.dp.toPx(), 28.dp.toPx(), 38.dp.toPx(), 44.dp.toPx(),
                        38.dp.toPx(), 28.dp.toPx(), 20.dp.toPx(), 12.dp.toPx()
                    )
                    val barColors = arrayOf(
                        Color(0xFF00E5FF), Color(0xFF38BDF8), Color(0xFF818CF8),
                        Color(0xFFA855F7), Color(0xFFE040FB), Color(0xFFA855F7),
                        Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFF00E5FF)
                    )
                    val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                    for (i in 0 until count) {
                        val harmonic = abs(sin(phase * 1.6f + i * 0.72f)).toFloat()
                        val barH = (baseHeights[i] * (0.30f + 0.70f * harmonic)).coerceIn(5.dp.toPx(), size.height)
                        val left = i * (barW + gap)
                        val top = (size.height - barH) / 2f
                        drawRoundRect(
                            color = barColors[i],
                            topLeft = Offset(left, top),
                            size = Size(barW, barH),
                            cornerRadius = corner
                        )
                    }
                }
            }
        }
    }
}
