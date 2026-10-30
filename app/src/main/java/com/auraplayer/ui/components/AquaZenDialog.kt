package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.RealtimeVisualizerManager
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * 🌊 Acuario Zen Bioluminiscente (Abyssal Glow)
 * Experiencia inmersiva marina relajante con:
 * - Ondas de agua físicas al tacto en cualquier punto de la pantalla.
 * - Medusas bioluminiscentes procedurales pulsando al ritmo de los graves.
 * - Partículas flotantes de plancton oceánico luminiscente.
 * - Carátula flotante sumergida con halo acuático y controles en gotas de cristal líquido.
 * - 3 temas de inmersión: Fosa Abisal (Azul), Magma Volcánico (Ámbar) y Aurora Ártica (Esmeralda).
 */
@Composable
fun AquaZenDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var oceanThemeIndex by remember { mutableIntStateOf(0) }

    // Temas del océano: [Color Profundo, Color Medio, Color Acento Bioluminiscente]
    val oceanThemes = remember {
        listOf(
            // 0: Fosa de las Marianas (Azul Abisal)
            Triple(Color(0xFF020B17), Color(0xFF08223E), Color(0xFF00F0FF)),
            // 1: Magma Volcánico Submarino (Obsidiana y Fuego)
            Triple(Color(0xFF120505), Color(0xFF2E0D0D), Color(0xFFFF5722)),
            // 2: Aurora Ártica Glacial (Esmeralda y Turquesa)
            Triple(Color(0xFF021410), Color(0xFF063328), Color(0xFF10B981))
        )
    }
    val currentTheme = oceanThemes[oceanThemeIndex % oceanThemes.size]
    val oceanBgDark = currentTheme.first
    val oceanBgMid = currentTheme.second
    val biolumColor = currentTheme.third

    // Energía de graves para la respiración/pulsación de las medusas
    var bassPulse by remember { mutableFloatStateOf(0.05f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            bassPulse = (bassPulse * 0.70f) + (target * 0.30f)
            delay(16)
        }
    }

    // Ondas táctiles de agua (ripples) generadas al tocar
    data class RippleWave(val x: Float, val y: Float, val startTime: Long)
    val ripples = remember { mutableStateListOf<RippleWave>() }

    // Animación infinita para el movimiento de las medusas y plancton
    val infiniteTransition = rememberInfiniteTransition(label = "aqua_zen_drift")
    val waterPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waterPhase"
    )

    // Pre-cache de degradados para 120 FPS
    val oceanBackgroundBrush = remember(oceanBgDark, oceanBgMid) {
        Brush.verticalGradient(listOf(oceanBgDark, oceanBgMid, oceanBgDark))
    }
    val liquidGlassBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(oceanBackgroundBrush)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        AuraHaptic.click(view)
                        // Limitar a máximo 6 ondas simultáneas
                        if (ripples.size > 5) ripples.removeAt(0)
                        ripples.add(RippleWave(tapOffset.x, tapOffset.y, System.currentTimeMillis()))
                    }
                }
        ) {
            // LIENZO PROCEDURAL: MEDUSAS, ONDAS DE AGUA Y PLANCTON
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val now = System.currentTimeMillis()

                // 1. Partículas de plancton flotante bioluminiscente
                val numPlankton = 26
                for (i in 0 until numPlankton) {
                    val px = ((i * 73f) + sin(waterPhase + i * 0.6f) * 14.dp.toPx()) % w
                    val py = ((i * 113f) + (waterPhase * 24.dp.toPx())) % h
                    val pAlpha = (0.20f + ((i % 5) * 0.12f))
                    drawCircle(
                        color = biolumColor.copy(alpha = pAlpha),
                        radius = (1.5f + (i % 3) * 1.2f).dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // 2. Medusas bioluminiscentes flotantes (3 medusas en diferentes profundidades)
                for (j in 0..2) {
                    val jx = w * (0.22f + j * 0.28f) + (sin(waterPhase + j * 1.8f) * 16.dp.toPx())
                    val verticalDrift = ((waterPhase * 35.dp.toPx() * (1f + j * 0.3f)) + (j * 180.dp.toPx())) % h
                    val jy = h - verticalDrift

                    val jSize = (28.dp.toPx() + (j * 8.dp.toPx())) * (1f + bassPulse * 0.18f)

                    // Campana de la medusa (arco superior bioluminiscente)
                    drawArc(
                        color = biolumColor.copy(alpha = 0.40f + (j * 0.10f)),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(jx - jSize, jy - jSize * 0.6f),
                        size = androidx.compose.ui.geometry.Size(jSize * 2f, jSize * 1.2f)
                    )

                    // Tentáculos ondulantes (3 tentáculos por medusa)
                    for (t in -1..1) {
                        val tentacleStartX = jx + (t * jSize * 0.45f)
                        val tentaclePath = Path()
                        tentaclePath.moveTo(tentacleStartX, jy)
                        val tentacleLen = jSize * 1.8f
                        for (step in 1..10) {
                            val progress = step / 10f
                            val curY = jy + (progress * tentacleLen)
                            val sway = sin(waterPhase * 2f + j * 1.5f + progress * 4f + t).toFloat() * 6.dp.toPx()
                            tentaclePath.lineTo(tentacleStartX + sway, curY)
                        }
                        drawPath(
                            path = tentaclePath,
                            color = biolumColor.copy(alpha = 0.30f),
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 3. Ondas táctiles expansivas (Ripples)
                val rippleIterator = ripples.iterator()
                while (rippleIterator.hasNext()) {
                    val rip = rippleIterator.next()
                    val age = now - rip.startTime
                    if (age > 1200L) {
                        rippleIterator.remove()
                    } else {
                        val progress = age / 1200f
                        val rippleRadius = progress * 140.dp.toPx()
                        val rippleAlpha = (1f - progress) * 0.65f
                        drawCircle(
                            color = biolumColor.copy(alpha = rippleAlpha),
                            radius = rippleRadius,
                            center = Offset(rip.x, rip.y),
                            style = Stroke(width = (2.5f * (1f - progress)).dp.toPx())
                        )
                    }
                }
            }

            // CABECERA SUPERIOR: TÍTULO Y ACCIONES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AURA AQUA ZEN • ABYSS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = biolumColor,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Toca la pantalla para generar ondas de agua",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            oceanThemeIndex = (oceanThemeIndex + 1) % oceanThemes.size
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Tema", tint = biolumColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // CONTENIDO CENTRAL: PERLA SUMERGIDA CON CARÁTULA
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .shadow(24.dp, CircleShape, spotColor = biolumColor.copy(alpha = 0.6f))
                        .clip(CircleShape)
                        .background(Color(0xFF03101E))
                        .border(2.5.dp, biolumColor.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentMedia?.artworkUri != null) {
                        AsyncImage(
                            model = currentMedia.artworkUri,
                            contentDescription = currentMedia.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(biolumColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌊", fontSize = 48.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = currentMedia?.title ?: "Silencio Submarino",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currentMedia?.artist ?: "Aura Zen"} • Profundidad 10,928m",
                    fontSize = 12.sp,
                    color = biolumColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // CONTROLES DE REPRODUCCIÓN FLOTANTES EN GOTAS DE CRISTAL
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anterior pista
                IconButton(
                    onClick = {
                        AuraHaptic.tick(view)
                        onPreviousClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(liquidGlassBrush)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }

                // Play / Pause en gota luminosa
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(16.dp, CircleShape, spotColor = biolumColor)
                        .clip(CircleShape)
                        .background(biolumColor)
                        .clickable {
                            AuraHaptic.heavy(view)
                            onPlayPauseClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = oceanBgDark,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Siguiente pista
                IconButton(
                    onClick = {
                        AuraHaptic.tick(view)
                        onNextClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(liquidGlassBrush)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
