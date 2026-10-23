package com.auraplayer.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.auraplayer.data.repository.WhatsAppStatusItem
import com.auraplayer.data.repository.WhatsAppStatusManager
import kotlinx.coroutines.launch

@Composable
fun WhatsAppStatusDialog(
    onDismiss: () -> Unit,
    onPlayVideo: ((WhatsAppStatusItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var statuses by remember { mutableStateOf<List<WhatsAppStatusItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Todos, 1: Videos, 2: Fotos
    var savedSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var previewItem by remember { mutableStateOf<WhatsAppStatusItem?>(null) }

    fun load() {
        isLoading = true
        scope.launch {
            statuses = WhatsAppStatusManager.getStatuses(context)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    val filteredList = remember(statuses, selectedFilter) {
        when (selectedFilter) {
            1 -> statuses.filter { it.isVideo }
            2 -> statuses.filter { !it.isVideo }
            else -> statuses
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B101E))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Estados de WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Guardar fotos y videos antes de que se borren", color = Color(0xFF25D366), fontSize = 11.sp)
                        }
                    }

                    Row {
                        IconButton(onClick = { load() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color(0xFF94A3B8))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chips de filtro
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text("Todos (${statuses.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text("🎬 Videos (${statuses.count { it.isVideo }})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text("🖼️ Fotos (${statuses.count { !it.isVideo }})", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                val hasStorageManager = if (isAndroid11Plus) Environment.isExternalStorageManager() else true
                val needsStoragePermission = isAndroid11Plus && !hasStorageManager

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF25D366))
                    }
                } else if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            if (needsStoragePermission) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Acceso a Archivos Necesario",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "En Android 11 y versiones superiores, los estados de WhatsApp se guardan en la carpeta protegida de medios. Para leerlos y guardarlos, activa el acceso a todos los archivos.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Abre Ajustes > Permisos para conceder acceso", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Conceder Permiso en Ajustes", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("👀 No hay estados vistos aún", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Abre WhatsApp y mira los estados de tus amigos. Al volver aquí aparecerán listos para guardarse.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.file.absolutePath }) { item ->
                            val isSaved = savedSet.contains(item.file.absolutePath)
                            WhatsAppStatusCard(
                                item = item,
                                isSaved = isSaved,
                                onSave = {
                                    scope.launch {
                                        val res = WhatsAppStatusManager.saveStatusToGallery(context, item)
                                        if (res.isSuccess) {
                                            savedSet = savedSet + item.file.absolutePath
                                            Toast.makeText(context, "✅ Guardado en tu Galería", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error al guardar: ${res.exceptionOrNull()?.message ?: ""}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onShare = {
                                    WhatsAppStatusManager.shareStatus(context, item)
                                },
                                onClick = {
                                    if (item.isVideo && onPlayVideo != null) {
                                        onPlayVideo(item)
                                    } else {
                                        previewItem = item
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Vista Previa en Pantalla Completa
    if (previewItem != null) {
        val pItem = previewItem!!
        val isItemSaved = savedSet.contains(pItem.file.absolutePath)

        Dialog(
            onDismissRequest = { previewItem = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                val previewThumb = produceState<Bitmap?>(
                    initialValue = WhatsAppStatusManager.getCachedThumbnail(pItem.file.absolutePath),
                    key1 = pItem.file.absolutePath
                ) {
                    if (value == null) {
                        value = WhatsAppStatusManager.loadThumbnail(context, pItem)
                    }
                }

                // Imagen / Miniatura centrada
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = previewThumb.value
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (!pItem.isVideo) {
                        AsyncImage(
                            model = pItem.file,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        CircularProgressIndicator(color = Color(0xFF25D366))
                    }

                    if (pItem.isVideo) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable {
                                    if (onPlayVideo != null) {
                                        previewItem = null
                                        onPlayVideo(pItem)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Reproducir",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                // Barra superior de cierre y título
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (pItem.isVideo) "🎬 Video de Estado" else "📷 Foto de Estado",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { previewItem = null },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Barra inferior de acciones (Compartir y Descargar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { WhatsAppStatusManager.shareStatus(context, pItem) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val res = WhatsAppStatusManager.saveStatusToGallery(context, pItem)
                                if (res.isSuccess) {
                                    savedSet = savedSet + pItem.file.absolutePath
                                    Toast.makeText(context, "✅ Guardado en tu Galería", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isItemSaved) Color(0xFF10B981) else Color(0xFF25D366)
                        )
                    ) {
                        Icon(
                            imageVector = if (isItemSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isItemSaved) "Guardado" else "Descargar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsAppStatusCard(
    item: WhatsAppStatusItem,
    isSaved: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val thumbState = produceState<Bitmap?>(
        initialValue = WhatsAppStatusManager.getCachedThumbnail(item.file.absolutePath),
        key1 = item.file.absolutePath
    ) {
        if (value == null) {
            value = WhatsAppStatusManager.loadThumbnail(context, item)
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF182032))
            .clickable(onClick = onClick)
    ) {
        val thumb = thumbState.value
        if (thumb != null) {
            Image(
                bitmap = thumb.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!item.isVideo) {
            AsyncImage(
                model = item.file,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF25D366),
                    strokeWidth = 2.dp
                )
            }
        }

        // Gradiente inferior para controles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Indicador de video o foto en esquina superior izquierda
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.isVideo) Icons.Default.PlayCircle else ImageIcon,
                    contentDescription = null,
                    tint = if (item.isVideo) Color(0xFF25D366) else Color(0xFF38BDF8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (item.isVideo) "VIDEO" else "FOTO",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botones inferiores: Compartir y Guardar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF10B981) else Color(0xFF25D366)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSaved) "Guardado" else "Guardar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
