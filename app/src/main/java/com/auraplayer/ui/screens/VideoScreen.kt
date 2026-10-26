package com.auraplayer.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.net.Uri
import com.auraplayer.ui.components.TikTokDownloaderDialog
import com.auraplayer.ui.components.InstagramDownloaderDialog
import com.auraplayer.ui.components.FacebookDownloaderDialog
import com.auraplayer.ui.components.WhatsAppStatusDialog
import com.auraplayer.data.repository.WhatsAppStatusItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.data.model.MediaModel
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "VideoScreen"

object VideoThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // 1/8th of app memory for thumbnails

    private val memoryCache = object : LruCache<Long, Bitmap>(cacheSize) {
        override fun sizeOf(key: Long, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun get(id: Long): Bitmap? = memoryCache.get(id)
    fun put(id: Long, bitmap: Bitmap) {
        memoryCache.put(id, bitmap)
    }
}

private suspend fun loadVideoThumbnail(context: Context, video: MediaModel): Bitmap? = withContext(Dispatchers.IO) {
    // 1. Try ContentResolver.loadThumbnail (Android 10+ / API 29+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val thumb = context.contentResolver.loadThumbnail(video.uri, Size(512, 384), null)
            if (thumb != null) return@withContext thumb
        } catch (e: Exception) {
            AppLog.d(TAG, "ContentResolver.loadThumbnail no disponible para el vídeo", e)
        }
    }

    // 2. Try MediaMetadataRetriever
    val retriever = MediaMetadataRetriever()
    try {
        if (video.path.isNotBlank() && File(video.path).exists()) {
            retriever.setDataSource(video.path)
        } else {
            retriever.setDataSource(context, video.uri)
        }
        val rawFrame: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                retriever.getScaledFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 512, 384)
            } catch (e: Exception) {
                null
            } ?: retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: retriever.frameAtTime
        } else {
            retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: retriever.frameAtTime
        }

        if (rawFrame != null) {
            val maxDimension = 512
            if (rawFrame.width > maxDimension || rawFrame.height > maxDimension) {
                val ratio = rawFrame.width.toFloat() / rawFrame.height.toFloat()
                val targetW: Int
                val targetH: Int
                if (ratio > 1f) {
                    targetW = maxDimension
                    targetH = (maxDimension / ratio).toInt().coerceAtLeast(1)
                } else {
                    targetH = maxDimension
                    targetW = (maxDimension * ratio).toInt().coerceAtLeast(1)
                }
                val scaled = Bitmap.createScaledBitmap(rawFrame, targetW, targetH, true)
                if (scaled !== rawFrame) {
                    rawFrame.recycle()
                }
                return@withContext scaled
            }
            return@withContext rawFrame
        }
    } catch (e: Exception) {
        AppLog.d(TAG, "MediaMetadataRetriever no pudo extraer un fotograma", e)
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {}
    }

    // 3. Fallback: ThumbnailUtils
    try {
        if (video.path.isNotBlank() && File(video.path).exists()) {
            @Suppress("DEPRECATION")
            val thumb = ThumbnailUtils.createVideoThumbnail(video.path, MediaStore.Images.Thumbnails.MINI_KIND)
            if (thumb != null) return@withContext thumb
        }
    } catch (e: Exception) {
        AppLog.d(TAG, "ThumbnailUtils no pudo crear la miniatura del vídeo", e)
    }

    null
}

@Composable
fun VideoThumbnail(
    video: MediaModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = VideoThumbnailCache.get(video.id), key1 = video.id) {
        if (value == null) {
            val loaded = loadVideoThumbnail(context, video)
            if (loaded != null) {
                VideoThumbnailCache.put(video.id, loaded)
                value = loaded
            }
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color(0xFF6366F1).copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun VideoScreen(
    videos: List<MediaModel>,
    isLoading: Boolean,
    onVideoClick: (MediaModel) -> Unit,
    onOpenVault: () -> Unit = {},
    onHideVideo: (MediaModel) -> Unit = {},
    onRefreshVideos: () -> Unit = {},
    onPlayDownloadedUri: (Uri) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoForMenu by remember { mutableStateOf<MediaModel?>(null) }
    var showWhatsAppStatusDialog by remember { mutableStateOf(false) }
    var showInstagramDownloader by remember { mutableStateOf(false) }
    var showFacebookDownloader by remember { mutableStateOf(false) }
    var showTikTokDownloader by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Top Header Bar with Vault Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Videos Locales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${videos.size} videos encontrados",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Glowing Vault Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .clickable { onOpenVault() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Bóveda",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "🔒 Bóveda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

        // Quick Social & Hub Action Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // WhatsApp Status Chip
            Box(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF25D366).copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF184228), Color(0xFF0C2215))
                        )
                    )
                    .clickable { showWhatsAppStatusDialog = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Estados WhatsApp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ADE80)
                    )
                }
            }

            // Instagram Reels Chip
            Box(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFE1306C).copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF3D1532), Color(0xFF200B1A))
                        )
                    )
                    .clickable { showInstagramDownloader = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📸", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Instagram Reels",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF472B6)
                    )
                }
            }

            // TikTok Chip
            Box(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00F2FE).copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF143040), Color(0xFF0A1922))
                        )
                    )
                    .clickable { showTikTokDownloader = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎵", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "TikTok HD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            // Facebook Reels Chip
            Box(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1877F2).copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF132A4A), Color(0xFF091626))
                        )
                    )
                    .clickable { showFacebookDownloader = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📘", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Facebook Reels",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin videos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(18.dp),
                                spotColor = Color.Black.copy(alpha = 0.5f)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onVideoClick(video) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13182C))
                    ) {
                        Column {
                            // Thumbnail with duration badge and play icon
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                VideoThumbnail(
                                    video = video,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Scrim Gradient overlay for high contrast
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                )

                                // Glowing Center Play Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .shadow(4.dp, CircleShape, spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f))
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF223452),
                                                    Color(0xFF10192A)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Reproducir",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // 3-dots Menu for Hiding
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable { videoForMenu = video },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opciones",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = videoForMenu == video,
                                        onDismissRequest = { videoForMenu = null },
                                        modifier = Modifier.background(Color(0xFF0F172A))
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Send,
                                                        contentDescription = null,
                                                        tint = Color(0xFF25D366),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Enviar a WhatsApp", color = Color.White, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                val v = videoForMenu
                                                videoForMenu = null
                                                if (v != null) {
                                                    com.auraplayer.data.repository.TikTokDownloader.shareMedia(context, v.uri, true, "com.whatsapp")
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = Color(0xFFA855F7),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("🎵 Extraer Audio (MP3)", color = Color.White, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                val v = videoForMenu
                                                videoForMenu = null
                                                if (v != null) {
                                                    android.widget.Toast.makeText(context, "Extrayendo pista de audio en HD...", android.widget.Toast.LENGTH_SHORT).show()
                                                    scope.launch {
                                                        val res = com.auraplayer.audio.VideoToAudioConverter.extractAudio(context, v.uri, v.title)
                                                        res.onSuccess {
                                                            android.widget.Toast.makeText(context, "¡Audio guardado en Música/DaVEPlayer!", android.widget.Toast.LENGTH_LONG).show()
                                                            onRefreshVideos()
                                                        }.onFailure { err ->
                                                            android.widget.Toast.makeText(context, "Error: ${err.message}", android.widget.Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = null,
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Compartir Video", color = Color.White, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                val v = videoForMenu
                                                videoForMenu = null
                                                if (v != null) {
                                                    com.auraplayer.data.repository.TikTokDownloader.shareMedia(context, v.uri, true, null)
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("🔒 Ocultar en Bóveda", color = Color.White, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                val v = videoForMenu
                                                videoForMenu = null
                                                if (v != null) onHideVideo(v)
                                            }
                                        )
                                    }
                                }

                                // Duration badge (Tactile 3D pill)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .shadow(3.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF1E2640), Color(0xFF0F1422))
                                            )
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = video.formattedDuration,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            // Video Title & Folder Info
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = Color(0xFFA855F7).copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = video.folderName,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // WhatsApp Status Saver Dialog
    if (showWhatsAppStatusDialog) {
        WhatsAppStatusDialog(
            onDismiss = { showWhatsAppStatusDialog = false },
            onPlayVideo = { item ->
                onPlayDownloadedUri(item.uri)
            }
        )
    }

    // Instagram Downloader Dialog
    if (showInstagramDownloader) {
        InstagramDownloaderDialog(
            initialUrl = "",
            onDismiss = { showInstagramDownloader = false },
            onDownloadCompleted = { uri ->
                onRefreshVideos()
            },
            onPlayDownloadedVideo = { uri ->
                onPlayDownloadedUri(uri)
            }
        )
    }

    // TikTok Downloader Dialog
    if (showTikTokDownloader) {
        TikTokDownloaderDialog(
            initialUrl = "",
            onDismiss = { showTikTokDownloader = false },
            onDownloadCompleted = { uri, isVideo ->
                if (isVideo) {
                    onRefreshVideos()
                    onPlayDownloadedUri(uri)
                }
            }
        )
    }

    // Facebook Downloader Dialog
    if (showFacebookDownloader) {
        FacebookDownloaderDialog(
            initialUrl = "",
            onDismiss = { showFacebookDownloader = false },
            onDownloadCompleted = { uri ->
                onRefreshVideos()
            },
            onPlayDownloadedVideo = { uri ->
                onPlayDownloadedUri(uri)
            }
        )
    }
}

