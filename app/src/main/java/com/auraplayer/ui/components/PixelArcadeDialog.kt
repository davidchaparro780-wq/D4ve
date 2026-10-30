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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.RealtimeVisualizerManager
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay

/**
 * 🎮 Modo Retro Arcade 16-Bit (Pixel Boy)
 * Consola portátil retro nostálgica de los 90s con:
 * - Pantalla LCD matricial con scanlines CRT sutiles y 3 paletas de color retro intercambiables.
 * - Personaje animado en pixel art caminando y saltando reactivo al beat de la música.
 * - Fondo en scroll parallax infinito con siluetas de rascacielos retro.
 * - Controles físicos con D-Pad en cruz, botones A/B y teclas Start/Select con háptica de pulsador vintage.
 * - Optimizada para 120 FPS sin alocaciones en bucles de dibujo.
 */
@Composable
fun PixelArcadeDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var paletteIndex by remember { mutableIntStateOf(0) }

    // Paletas retro de 4 tonos: [Fondo LCD, Tono Medio Claro, Tono Medio Oscuro, Tono Negro Pixel]
    val palettes = remember {
        listOf(
            // 0: Game Boy clásico verde oliva
            listOf(Color(0xFF8BAC0F), Color(0xFF9BBC0F), Color(0xFF306230), Color(0xFF0F380F)),
            // 1: Cyberpunk Neón
            listOf(Color(0xFF130924), Color(0xFF2E134D), Color(0xFF00F0FF), Color(0xFFFF007F)),
            // 2: Monocromo Ámbar OLED
            listOf(Color(0xFF140E05), Color(0xFF291B08), Color(0xFFD97706), Color(0xFFFDE68A))
        )
    }
    val currentPalette = palettes[paletteIndex % palettes.size]
    val lcdBgColor = currentPalette[0]
    val pixelColor = currentPalette[3]
    val highlightColor = currentPalette[2]

    // Energía de graves para el salto del personaje
    var bassEnergy by remember { mutableFloatStateOf(0.1f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            bassEnergy = (bassEnergy * 0.7f) + (target * 0.3f)
            delay(16)
        }
    }

    // Animación continua para scroll parallax y pasos del personaje
    val infiniteTransition = rememberInfiniteTransition(label = "pixel_arcade_anim")
    val scrollPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scrollPhase"
    )
    val walkCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walkCycle"
    )

    // Pre-cache de pinceles de chasis para regla de 120 FPS
    val consoleBodyBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF818CF8), Color(0xFF4F46E5), Color(0xFF3730A3)) // Morado atómico translúcido 90s
        )
    }
    val dpadBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF2B2E36), Color(0xFF17191E))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE05060A)),
            contentAlignment = Alignment.Center
        ) {
            // CHASIS DE LA CONSOLA
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(consoleBodyBrush)
                    .border(2.dp, Color(0x66FFFFFF), RoundedCornerShape(28.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CABECERA: INDICADOR LED Y BOTÓN CERRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFFEF4444) else Color(0xFF450A0A))
                                .border(0.8.dp, Color(0xFFB91C1C), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BATTERY • 16-BIT RETRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE0E7FF)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                paletteIndex = (paletteIndex + 1) % palettes.size
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Paleta", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                onDismiss()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // PANTALLA LCD MATRICIAL BEZEL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E24))
                        .border(3.dp, Color(0xFF121217), RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    // CRISTAL LCD INTERIOR
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(lcdBgColor)
                            .border(1.5.dp, pixelColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        // LIENZO PROCEDURAL PIXEL-ART
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val groundY = h * 0.76f

                            // 1. Siluetas de edificios en parallax de fondo
                            val buildingWidth = 36.dp.toPx()
                            val numBuildings = (w / buildingWidth).toInt() + 3
                            for (i in 0 until numBuildings) {
                                val bX = (i * buildingWidth) - (scrollPhase * buildingWidth)
                                val bHeight = ((i * 37) % 55 + 30).dp.toPx()
                                drawRect(
                                    color = highlightColor.copy(alpha = 0.35f),
                                    topLeft = Offset(bX, groundY - bHeight),
                                    size = Size(buildingWidth - 4.dp.toPx(), bHeight)
                                )
                            }

                            // 2. Línea de suelo cuadriculada
                            drawLine(
                                color = pixelColor,
                                start = Offset(0f, groundY),
                                end = Offset(w, groundY),
                                strokeWidth = 2.5.dp.toPx()
                            )
                            for (step in 0..15) {
                                val lineX = ((step * 24.dp.toPx()) - (scrollPhase * 24.dp.toPx())) % w
                                drawLine(
                                    color = pixelColor.copy(alpha = 0.6f),
                                    start = Offset(lineX, groundY),
                                    end = Offset(lineX - 12.dp.toPx(), h),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }

                            // 3. Personaje animado en pixel art (8x8 cuadrícula)
                            val charX = w * 0.28f
                            // Salto con graves
                            val jumpOffset = if (isPlaying && bassEnergy > 0.45f) {
                                ((bassEnergy - 0.45f) * 70.dp.toPx()).coerceAtMost(38.dp.toPx())
                            } else 0f
                            val charY = groundY - 26.dp.toPx() - jumpOffset
                            val pSize = 3.dp.toPx()

                            // Matriz de píxeles del personaje (cabeza, torso y piernas animadas)
                            val stepFrame = (walkCycle.toInt()) % 4
                            val isLegOpen = isPlaying && (stepFrame == 1 || stepFrame == 3)

                            // Cabeza
                            drawRect(pixelColor, Offset(charX + 2 * pSize, charY), Size(4 * pSize, 3 * pSize))
                            // Ojo
                            drawRect(lcdBgColor, Offset(charX + 4 * pSize, charY + pSize), Size(pSize, pSize))
                            // Auriculares retro del personaje
                            drawRect(highlightColor, Offset(charX + pSize, charY + pSize), Size(pSize, 2 * pSize))
                            drawRect(highlightColor, Offset(charX + 2 * pSize, charY - pSize * 0.5f), Size(3 * pSize, pSize * 0.7f))

                            // Torso
                            drawRect(pixelColor, Offset(charX + 2 * pSize, charY + 3 * pSize), Size(4 * pSize, 3 * pSize))

                            // Piernas según frame de caminata
                            if (isLegOpen) {
                                drawRect(pixelColor, Offset(charX + pSize, charY + 6 * pSize), Size(2 * pSize, 3 * pSize))
                                drawRect(pixelColor, Offset(charX + 5 * pSize, charY + 6 * pSize), Size(2 * pSize, 3 * pSize))
                            } else {
                                drawRect(pixelColor, Offset(charX + 2 * pSize, charY + 6 * pSize), Size(2 * pSize, 3 * pSize))
                                drawRect(pixelColor, Offset(charX + 4 * pSize, charY + 6 * pSize), Size(2 * pSize, 3 * pSize))
                            }

                            // 4. Scanlines CRT sutiles
                            val scanlineStep = 4.dp.toPx()
                            val scanlineCount = (h / scanlineStep).toInt()
                            for (line in 0..scanlineCount) {
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.08f),
                                    start = Offset(0f, line * scanlineStep),
                                    end = Offset(w, line * scanlineStep),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        // INFORMACIÓN DE CANCIÓN RETRO EN PANTALLA SUPERIOR
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = (currentMedia?.title ?: "STAGE 1: AURA PLAYER").uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = pixelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "BASS: ${(bassEnergy * 100).toInt()}% • ${if (isPlaying) "PLAYING" else "PAUSED"}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = highlightColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CONTROLES FÍSICOS RETRO: D-PAD & BOTONES A/B
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // D-PAD EN CRUZ
                    Box(
                        modifier = Modifier.size(108.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Brazo Horizontal (PREV / NEXT)
                        Row(
                            modifier = Modifier
                                .width(108.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(dpadBrush)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Izquierda = Prev
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        AuraHaptic.heavy(view)
                                        onPreviousClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("◀", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }

                            // Derecha = Next
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        AuraHaptic.heavy(view)
                                        onNextClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        // Brazo Vertical
                        Column(
                            modifier = Modifier
                                .width(36.dp)
                                .height(108.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(dpadBrush)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp)),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Text("▲", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Text("▼", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        // Centro del D-Pad
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D2027))
                        )
                    }

                    // BOTONES A & B DIAGONALES
                    Row(
                        modifier = Modifier
                            .rotate(-24f)
                            .padding(end = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // BOTÓN B: CAMBIAR PALETA
                        ArcadeRoundButton(
                            label = "B",
                            color = Color(0xFFE11D48),
                            onClick = {
                                AuraHaptic.click(view)
                                paletteIndex = (paletteIndex + 1) % palettes.size
                            }
                        )

                        // BOTÓN A: PLAY / PAUSE
                        ArcadeRoundButton(
                            label = "A",
                            color = Color(0xFFE11D48),
                            isPrimary = true,
                            onClick = {
                                AuraHaptic.heavy(view)
                                onPlayPauseClick()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTONES SELECT & START INCLINADOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PillOptionButton(label = "SELECT") {
                        AuraHaptic.tick(view)
                        paletteIndex = (paletteIndex + 1) % palettes.size
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    PillOptionButton(label = "START") {
                        AuraHaptic.heavy(view)
                        onPlayPauseClick()
                    }
                }
            }
        }
    }
}

@Composable
private fun ArcadeRoundButton(
    label: String,
    color: Color,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    translationY = if (isPressed) 2.5.dp.toPx() else 0f
                }
                .shadow(if (isPressed) 1.dp else 5.dp, CircleShape, spotColor = color)
                .clip(CircleShape)
                .background(if (isPressed) color.copy(alpha = 0.8f) else color)
                .border(1.5.dp, Color(0x55FFFFFF), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PillOptionButton(
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .rotate(-26f)
                .width(42.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF333844))
                .border(0.8.dp, Color(0x44FFFFFF), RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFC7D2FE)
        )
    }
}
