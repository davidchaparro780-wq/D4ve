package com.auraplayer.ui.components

import android.content.Context
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.auraplayer.data.repository.InstagramDownloader
import com.auraplayer.data.repository.InstagramMediaDetails
import com.auraplayer.data.repository.TikTokDownloader
import kotlinx.coroutines.launch

@Composable
fun InstagramDownloaderDialog(
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
    var resolvedDetails by remember { mutableStateOf<InstagramMediaDetails?>(null) }
    var resolveError by remember { mutableStateOf<String?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var completedUri by remember { mutableStateOf<Uri?>(null) }

    val instagramGradient = Brush.linearGradient(
        listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
    )

    fun downloadDetails(details: InstagramMediaDetails) {
        if (details.videoUrl.isBlank()) {
            Toast.makeText(context, "Enlace de video no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        isDownloading = true
        downloadProgress = 0

        scope.launch {
            val result = InstagramDownloader.downloadMedia(
                context = context,
                details = details,
                onProgress = { pct -> downloadProgress = pct }
            )

            isDownloading = false
            result.onSuccess { uri ->
                completedUri = uri
                Toast.makeText(context, "✅ Reel de Instagram guardado en Galería", Toast.LENGTH_LONG).show()
                onDownloadCompleted(uri)
            }.onFailure { err ->
                Toast.makeText(context, "Error al descargar: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun resolveLink(targetUrl: String) {
        val clean = targetUrl.trim()
        if (clean.isBlank()) {
            Toast.makeText(context, "Pega un enlace de Instagram primero", Toast.LENGTH_SHORT).show()
            return
        }
        keyboardController?.hide()
        isResolving = true
        resolveError = null
        resolvedDetails = null
        completedUri = null

        scope.launch {
            val result = InstagramDownloader.resolveInstagram(clean)
            isResolving = false
            result.onSuccess { details ->
                resolvedDetails = details
                if (autoDownload) {
                    downloadDetails(details)
                }
            }.onFailure { err ->
                resolveError = err.message ?: "No se pudo extraer el video de Instagram."
            }
        }
    }

    LaunchedEffect(Unit) {
        if (initialUrl.isNotBlank()) {
            resolveLink(initialUrl)
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        containerColor = Color(0xFF140D22),
        shape = RoundedCornerShape(26.dp),
        title = {
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
                            .background(instagramGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (autoDownload) "⚡ Descarga Automática" else "Instagram Reels & Videos",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (autoDownload) "Compartido desde Instagram • Calidad HD" else "Descargador HD Libre de Cuenta",
                            color = Color(0xFFE1306C),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onDismiss, enabled = !isDownloading) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("Pega un enlace de Reel o Video...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE1306C),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE1306C)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { resolveLink(urlInput) })
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (resolvedDetails == null && !isResolving) {
                    Button(
                        onClick = { resolveLink(urlInput) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                        enabled = urlInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizar Video", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (isResolving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFFE1306C), strokeWidth = 3.dp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Obteniendo video de Instagram...", fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                if (resolveError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = resolveError ?: "", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(
                    visible = resolvedDetails != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    resolvedDetails?.let { details ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E1530))
                                .border(1.dp, Color(0xFFE1306C).copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (details.coverUrl.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black)
                                    ) {
                                        AsyncImage(
                                            model = details.coverUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(72.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE1306C).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("📸 INSTAGRAM REEL", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF43F5E))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = details.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (isDownloading) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Descargando video...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                        Text("$downloadProgress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1306C))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress / 100f },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFFE1306C),
                                        trackColor = Color(0xFF2E1065)
                                    )
                                }
                            } else if (completedUri != null) {
                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("¡Guardado en Galería (DCIM)!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            completedUri?.let { uri ->
                                                TikTokDownloader.shareMedia(context, uri, true, "com.whatsapp")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enviar a WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }

                                    if (onPlayDownloadedVideo != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                completedUri?.let { uri ->
                                                    onPlayDownloadedVideo(uri)
                                                    onDismiss()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reproducir Video Ahora", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { downloadDetails(details) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))
                                ) {
                                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Descargar Video HD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
