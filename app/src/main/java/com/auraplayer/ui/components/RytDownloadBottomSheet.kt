package com.auraplayer.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.OnlineTrack
import com.auraplayer.data.repository.DownloadEngine
import com.auraplayer.data.repository.DownloadStatus
import kotlinx.coroutines.launch

enum class RytDownloadFormat {
    AUDIO_HQ_320,
    AUDIO_LITE_128,
    VIDEO_HD_720,
    VIDEO_SD_360
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RytDownloadBottomSheet(
    track: OnlineTrack,
    downloadEngine: DownloadEngine,
    onDismiss: () -> Unit,
    onDownloadCompleted: (OnlineTrack) -> Unit = {},
    onPlayTrackNow: ((OnlineTrack) -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedFormat by remember { mutableStateOf(RytDownloadFormat.AUDIO_HQ_320) }
    val downloadStates by downloadEngine.downloadStates.collectAsState()

    val isVideoSelected = selectedFormat == RytDownloadFormat.VIDEO_HD_720 || selectedFormat == RytDownloadFormat.VIDEO_SD_360
    val currentDownloadKey = if (isVideoSelected) "vid_${track.id}" else track.id
    val currentStatus = downloadStates[currentDownloadKey] ?: DownloadStatus.Idle

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334155))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header: RYT Music title bar with close icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RYT Descargador",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                IconButton(
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Track Presentation Card (Artwork, Title, Artist, Duration badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(track.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val durationMin = track.durationSec / 60
                        val durationSecRem = track.durationSec % 60
                        val durFormatted = String.format("%d:%02d", durationMin, durationSecRem)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = durFormatted,
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = track.source,
                                color = Color(0xFFA855F7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Section Title: Formatos de Audio
            Text(
                text = "🎵 AUDIO (Música para escuchar)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 1: Audio HQ 320k
            val dur = track.durationSec.coerceAtLeast(60)
            val sizeHq = String.format("%.1f MB", ((dur * 320) / 8000.0).coerceAtLeast(3.2))
            FormatSelectionTile(
                title = "Audio Alta Fidelidad (HQ)",
                subtitle = "MP3/M4A 320 kbps • Sonido nítido para auriculares",
                badgeText = "💎 HQ 320k",
                sizeText = sizeHq,
                badgeColor = Color(0xFF38BDF8),
                isSelected = selectedFormat == RytDownloadFormat.AUDIO_HQ_320,
                onClick = {
                    AuraHaptic.tick(view)
                    selectedFormat = RytDownloadFormat.AUDIO_HQ_320
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Audio Ligero 128k
            val sizeLite = String.format("%.1f MB", ((dur * 128) / 8000.0).coerceAtLeast(1.4))
            FormatSelectionTile(
                title = "Audio Ligero (Ahorro de Datos)",
                subtitle = "AAC 128 kbps • Descarga instantánea y poco peso",
                badgeText = "⚡ 128k",
                sizeText = sizeLite,
                badgeColor = Color(0xFF10B981),
                isSelected = selectedFormat == RytDownloadFormat.AUDIO_LITE_128,
                onClick = {
                    AuraHaptic.tick(view)
                    selectedFormat = RytDownloadFormat.AUDIO_LITE_128
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Section Title: Formatos de Video
            Text(
                text = "🎬 VIDEO (Películas y Videos Musicales)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 3: Video HD 720p
            val sizeHd = String.format("%.1f MB", ((dur * 1800) / 8000.0).coerceAtLeast(16.0))
            FormatSelectionTile(
                title = "Video Alta Definición (HD)",
                subtitle = "MP4 720p Muxed • Audio + video HD completo",
                badgeText = "🎬 HD 720p",
                sizeText = sizeHd,
                badgeColor = Color(0xFF8B5CF6),
                isSelected = selectedFormat == RytDownloadFormat.VIDEO_HD_720,
                onClick = {
                    AuraHaptic.tick(view)
                    selectedFormat = RytDownloadFormat.VIDEO_HD_720
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 4: Video SD 360p
            val sizeSd = String.format("%.1f MB", ((dur * 650) / 8000.0).coerceAtLeast(6.5))
            FormatSelectionTile(
                title = "Video Rápido / Reducido (SD)",
                subtitle = "MP4 360p • Tamaño compacto, ideal para compartir",
                badgeText = "📱 SD 360p",
                sizeText = sizeSd,
                badgeColor = Color(0xFFF59E0B),
                isSelected = selectedFormat == RytDownloadFormat.VIDEO_SD_360,
                onClick = {
                    AuraHaptic.tick(view)
                    selectedFormat = RytDownloadFormat.VIDEO_SD_360
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Live Download Status & Dynamic Action Button
            when (currentStatus) {
                is DownloadStatus.Downloading -> {
                    val progress = currentStatus.progress
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isVideoSelected) "Descargando video MP4..." else "Descargando música HQ...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "$progress%",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF0F172A)
                        )
                    }
                }
                is DownloadStatus.Tagging -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFA855F7)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Guardando metadatos y carátula HQ...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                is DownloadStatus.Completed -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.7f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Descargado con éxito!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (onPlayTrackNow != null && !isVideoSelected) {
                            Button(
                                onClick = {
                                    AuraHaptic.click(view)
                                    onPlayTrackNow(track)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escuchar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                is DownloadStatus.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.6f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentStatus.message,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                AuraHaptic.click(view)
                                scope.launch {
                                    if (isVideoSelected) {
                                        downloadEngine.downloadVideo(track)
                                    } else {
                                        downloadEngine.downloadTrack(track)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reintentar Descarga", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    // Idle state: Vibrant download trigger button
                    Button(
                        onClick = {
                            AuraHaptic.click(view)
                            val formatLabel = when (selectedFormat) {
                                RytDownloadFormat.AUDIO_HQ_320 -> "Audio HQ (320 kbps)"
                                RytDownloadFormat.AUDIO_LITE_128 -> "Audio Ligero (128 kbps)"
                                RytDownloadFormat.VIDEO_HD_720 -> "Video HD (720p)"
                                RytDownloadFormat.VIDEO_SD_360 -> "Video SD (360p)"
                            }
                            Toast.makeText(context, "Iniciando descarga: ${track.title} ($formatLabel)", Toast.LENGTH_SHORT).show()

                            scope.launch {
                                try {
                                    if (isVideoSelected) {
                                        downloadEngine.downloadVideo(
                                            track = track,
                                            onComplete = {
                                                Toast.makeText(context, "✓ Video guardado en Movies/DaVEPlayer: ${track.title}", Toast.LENGTH_LONG).show()
                                                onDownloadCompleted(track)
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "Error en video: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        downloadEngine.downloadTrack(
                                            track = track,
                                            onComplete = {
                                                Toast.makeText(context, "✓ Música guardada en Music/DaVEPlayer: ${track.title}", Toast.LENGTH_LONG).show()
                                                onDownloadCompleted(track)
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "Error en audio: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Fallo al iniciar descarga: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isVideoSelected) {
                                            listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                        } else {
                                            listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isVideoSelected) Icons.Default.Videocam else Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isVideoSelected) "Descargar Video MP4" else "Descargar Música Ahora",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatSelectionTile(
    title: String,
    subtitle: String,
    badgeText: String,
    sizeText: String,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131D2E))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) badgeColor else Color(0xFF334155).copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio / Check Circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) badgeColor else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isSelected) badgeColor else Color(0xFF64748B),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = sizeText,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
