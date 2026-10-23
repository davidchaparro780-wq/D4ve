package com.auraplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.auraplayer.data.model.OnlineTrack
import com.auraplayer.data.repository.DownloadEngine
import com.auraplayer.data.repository.DownloadStatus
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    downloadEngine: DownloadEngine,
    onClose: () -> Unit,
    onPlayTrack: (OnlineTrack) -> Unit = {},
    onPlayVideo: (OnlineTrack) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val states by downloadEngine.downloadStates.collectAsState()
    val tracks by downloadEngine.trackRegistry.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Todas, 1: Música, 2: Videos

    val registryEntries = tracks.entries.toList()
    val activeEntries = registryEntries.filter { (key, _) ->
        when (states[key]) {
            is DownloadStatus.Downloading, is DownloadStatus.Tagging, is DownloadStatus.Error -> true
            else -> false
        }
    }
    val completedEntries = registryEntries.filter { (key, _) ->
        states[key] is DownloadStatus.Completed
    }
    val visibleEntries = activeEntries + completedEntries

    val musicEntries = visibleEntries.filter { !it.key.startsWith("vid_") }
    val videoEntries = visibleEntries.filter { it.key.startsWith("vid_") }

    val currentFilteredEntries = when (selectedTab) {
        1 -> musicEntries
        2 -> videoEntries
        else -> visibleEntries
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Descargas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (completedEntries.isNotEmpty()) {
                        TextButton(onClick = { downloadEngine.clearCompleted() }) {
                            Text(
                                text = "Limpiar",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Filter Tabs (Todas, Música, Videos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadCategoryChip(
                    label = "Todas (${visibleEntries.size})",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                DownloadCategoryChip(
                    label = "🎵 Música (${musicEntries.size})",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                DownloadCategoryChip(
                    label = "🎬 Videos (${videoEntries.size})",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }

            if (currentFilteredEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (selectedTab) {
                            2 -> Icons.Default.Videocam
                            else -> Icons.Default.CloudDownload
                        },
                        contentDescription = null,
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = when (selectedTab) {
                            1 -> "No hay canciones descargadas"
                            2 -> "No hay videos descargados"
                            else -> "No hay descargas aún"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Descarga música y videos desde Explorar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    items(currentFilteredEntries, key = { it.key }) { (downloadKey, track) ->
                        val isVideo = downloadKey.startsWith("vid_")
                        DownloadRow(
                            track = track,
                            isVideo = isVideo,
                            status = states[downloadKey] ?: DownloadStatus.Idle,
                            onClick = {
                                if (states[downloadKey] is DownloadStatus.Completed) {
                                    if (isVideo) {
                                        onPlayVideo(track)
                                    } else {
                                        onPlayTrack(track)
                                    }
                                }
                            },
                            onRemove = {
                                downloadEngine.removeEntry(downloadKey)
                            },
                            onRetry = {
                                scope.launch {
                                    if (isVideo) {
                                        downloadEngine.downloadVideo(track = track)
                                    } else {
                                        downloadEngine.downloadTrack(track = track)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF0B0E1A) else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun QualityBadge(
    text: String,
    bg: Color,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DownloadRow(
    track: OnlineTrack,
    isVideo: Boolean,
    status: DownloadStatus,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit
) {
    val isCompleted = status is DownloadStatus.Completed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF13182E))
            .clickable(enabled = isCompleted, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.coverUrl)
                        .size(112, 112)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = if (isVideo) Color(0xFF38BDF8) else Color(0xFF8B5CF6).copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (isVideo) {
                    QualityBadge(
                        text = "🎬 MP4 HD",
                        bg = Color(0xFF0284C7).copy(alpha = 0.2f),
                        color = Color(0xFF38BDF8)
                    )
                } else {
                    val fmt = if (track.format.isNotBlank()) track.format.uppercase() else "MP3"
                    val rate = if (track.bitrateKbps > 0) "${track.bitrateKbps}k" else "HQ"
                    QualityBadge(
                        text = "🎵 $fmt • $rate",
                        bg = Color(0xFF7C3AED).copy(alpha = 0.2f),
                        color = Color(0xFFA78BFA)
                    )
                }
                if (track.durationSec > 0) {
                    val m = track.durationSec / 60
                    val s = track.durationSec % 60
                    QualityBadge(
                        text = String.format("%d:%02d", m, s),
                        bg = Color(0xFF334155).copy(alpha = 0.4f),
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        when (status) {
            is DownloadStatus.Downloading -> {
                Column(
                    modifier = Modifier.width(78.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    LinearProgressIndicator(
                        progress = { status.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${status.progress}%",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is DownloadStatus.Tagging -> {
                Text(
                    text = "Aplicando etiquetas…",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            is DownloadStatus.Completed -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Descargada",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVideo) "Video listo" else "Listo",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Descartar",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            is DownloadStatus.Error -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = status.message,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(96.dp)
                        )
                        TextButton(onClick = onRetry) {
                            Text(
                                text = "Reintentar",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Descartar",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            DownloadStatus.Idle -> {}
        }
    }
}
