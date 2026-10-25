package com.auraplayer.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.auraplayer.data.repository.FacebookDownloader
import com.auraplayer.data.repository.FacebookMediaDetails
import com.auraplayer.util.getSafeClipboardText
import kotlinx.coroutines.launch

@Composable
fun FacebookDownloaderDialog(
    initialUrl: String = "",
    autoDownload: Boolean = false,
    onDismiss: () -> Unit,
    onDownloadCompleted: (Uri) -> Unit = {},
    onPlayDownloadedVideo: ((Uri) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var urlInput by remember { mutableStateOf(initialUrl) }
    var isResolving by remember { mutableStateOf(false) }
    var resolvedDetails by remember { mutableStateOf<FacebookMediaDetails?>(null) }
    var resolveError by remember { mutableStateOf<String?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var completedUri by remember { mutableStateOf<Uri?>(null) }
    var completedIsAudio by remember { mutableStateOf(false) }

    val facebookGradient = Brush.linearGradient(
        listOf(Color(0xFF1877F2), Color(0xFF0D55BE), Color(0xFF0A3C85))
    )

    fun startDownload(extractAudio: Boolean) {
        val details = resolvedDetails ?: return
        isDownloading = true
        downloadProgress = 0
        completedIsAudio = extractAudio

        scope.launch {
            val result = FacebookDownloader.downloadMedia(
                context = context,
                details = details,
                extractAudio = extractAudio,
                onProgress = { pct -> downloadProgress = pct }
            )

            isDownloading = false
            result.onSuccess { uri ->
                completedUri = uri
                val msg = if (extractAudio) "✅ Audio extraído en Música" else "✅ Video guardado en Galería"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onDownloadCompleted(uri)
            }.onFailure { err ->
                Toast.makeText(context, "Error al descargar: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun resolveUrl(targetUrl: String) {
        val clean = targetUrl.trim()
        if (clean.isBlank()) return
        if (!FacebookDownloader.isFacebookUrl(clean)) {
            resolveError = "Pega un enlace válido de Facebook (Reels, Watch o Video)"
            return
        }

        keyboardController?.hide()
        isResolving = true
        resolveError = null
        resolvedDetails = null
        completedUri = null

        scope.launch {
            val res = FacebookDownloader.resolveFacebook(clean)
            isResolving = false
            res.onSuccess { details ->
                resolvedDetails = details
                if (autoDownload) {
                    startDownload(extractAudio = false)
                }
            }.onFailure { err ->
                resolveError = err.message ?: "No se pudo obtener el video"
            }
        }
    }

    LaunchedEffect(Unit) {
        if (initialUrl.isNotBlank() && FacebookDownloader.isFacebookUrl(initialUrl)) {
            resolveUrl(initialUrl)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clickable(enabled = false) {}
                    .shadow(24.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header con badge de Facebook
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(facebookGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "f",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Descargador de Facebook",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reels, Videos & Audio MP3 en HD",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input de Enlace y botón Pegar
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            resolveError = null
                        },
                        placeholder = {
                            Text("Pega el enlace de Facebook aquí...", color = Color(0xFF64748B), fontSize = 12.sp)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1877F2),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF1877F2)
                        ),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(onClick = { urlInput = ""; resolvedDetails = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color(0xFF94A3B8))
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        val clip = context.getSafeClipboardText()
                                        if (!clip.isNullOrBlank()) {
                                            urlInput = clip
                                            resolveUrl(clip)
                                        } else {
                                            Toast.makeText(context, "Portapapeles vacío", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Pegar", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { resolveUrl(urlInput) })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Analizar / Resolver
                    Button(
                        onClick = { resolveUrl(urlInput) },
                        enabled = urlInput.isNotBlank() && !isResolving && !isDownloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                    ) {
                        if (isResolving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analizando video...", color = Color.White, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analizar Enlace", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Mensaje de Error
                    AnimatedVisibility(visible = resolveError != null, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.35f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(resolveError ?: "", color = Color(0xFFFCA5A5), fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }

                    // Vista Previa y Opciones de Descarga
                    resolvedDetails?.let { details ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (details.coverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = details.coverUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = details.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = details.author,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                        if (details.videoHdUrl != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF1877F2).copy(alpha = 0.25f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("HD", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progreso de Descarga
                        if (isDownloading) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Guardando en el dispositivo...", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                    Text("$downloadProgress%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF1877F2),
                                    trackColor = Color(0xFF334155)
                                )
                            }
                        } else if (completedUri != null) {
                            // Descarga Exitosa
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF14532D).copy(alpha = 0.4f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF86EFAC), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("¡Descarga completada con éxito!", color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Listo para reproducir o compartir", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                }
                                if (onPlayDownloadedVideo != null) {
                                    IconButton(onClick = { onPlayDownloadedVideo(completedUri!!) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = Color(0xFF86EFAC))
                                    }
                                }
                                IconButton(onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = if (completedIsAudio) "audio/*" else "video/*"
                                        putExtra(Intent.EXTRA_STREAM, completedUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir medio"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White)
                                }
                            }
                        } else {
                            // Botones de Descarga: Video MP4 y Extraer Audio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { startDownload(extractAudio = false) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                                ) {
                                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video MP4", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { startDownload(extractAudio = true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Audio MP3", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
