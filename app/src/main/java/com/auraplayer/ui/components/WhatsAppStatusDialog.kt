package com.auraplayer.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.auraplayer.data.repository.WhatsAppStatusItem
import com.auraplayer.data.repository.WhatsAppStatusManager
import com.auraplayer.util.AppLog
import com.auraplayer.util.findActivity
import kotlinx.coroutines.launch

@Composable
fun WhatsAppStatusDialog(
    onDismiss: () -> Unit,
    onPlayVideo: ((WhatsAppStatusItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() as? ComponentActivity }

    var statuses by remember { mutableStateOf<List<WhatsAppStatusItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    // 0: Recientes 24h, 1: Todos, 2: Videos, 3: Fotos
    var selectedFilter by remember { mutableIntStateOf(0) }
    var savedSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var previewItem by remember { mutableStateOf<WhatsAppStatusItem?>(null) }

    val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    var hasStoragePermission by remember {
        mutableStateOf(
            if (isAndroid11Plus) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var hasSaf by remember { mutableStateOf(WhatsAppStatusManager.hasSafAccess(context)) }

    var isCurrentlyLoading by remember { mutableStateOf(false) }

    fun load() {
        if (isCurrentlyLoading) return
        isCurrentlyLoading = true
        isLoading = true
        scope.launch {
            try {
                val loaded = WhatsAppStatusManager.getStatuses(context)
                statuses = loaded
                if (loaded.isNotEmpty() && !loaded.any { it.isRecent24h } && selectedFilter == 0) {
                    selectedFilter = 1
                }
            } catch (e: Throwable) {
                AppLog.e("WhatsAppStatusDialog", "Error cargando estados", e)
            } finally {
                isLoading = false
                isCurrentlyLoading = false
            }
        }
    }

    // Observador de Ciclo de Vida: al regresar de Ajustes o de WhatsApp, recargar automáticamente
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasStoragePermission = if (isAndroid11Plus) {
                    Environment.isExternalStorageManager()
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
                hasSaf = WhatsAppStatusManager.hasSafAccess(context)
                load()
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            activity?.lifecycle?.removeObserver(observer)
        }
    }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (_: SecurityException) {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                WhatsAppStatusManager.saveTreeUri(context, uri)
                hasSaf = true
                Toast.makeText(context, "✅ Carpeta de WhatsApp vinculada con éxito", Toast.LENGTH_SHORT).show()
                load()
            } catch (e: Exception) {
                Toast.makeText(context, "Error vinculando carpeta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        if (granted) load()
    }

    LaunchedEffect(Unit) {
        load()
    }

    val recentStatuses = remember(statuses) { statuses.filter { it.isRecent24h } }
    val filteredList = remember(statuses, selectedFilter) {
        when (selectedFilter) {
            0 -> statuses.filter { it.isRecent24h }
            1 -> statuses
            2 -> statuses.filter { it.isVideo }
            3 -> statuses.filter { !it.isVideo }
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
                            Text(
                                if (selectedFilter == 0) "⚡ Estados recientes de las últimas 24h" else "Guardar fotos y videos de tus contactos",
                                color = Color(0xFF25D366),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
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

                // Banner de Permiso de Archivos (solo si no tiene acceso ni por sistema ni por SAF)
                if (!hasStoragePermission && !hasSaf) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A1B12))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permiso de Archivos Requerido",
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAndroid11Plus) {
                                "En Android 11+ WhatsApp guarda los estados en carpetas protegidas. Activa el permiso para poder detectarlos automáticamente."
                            } else {
                                "Se requiere permiso de lectura de almacenamiento para mostrar los estados de WhatsApp."
                            },
                            color = Color(0xFFD1D5DB),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (isAndroid11Plus) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        try {
                                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Abre Ajustes > Permisos para conceder acceso", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    legacyPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text(
                                if (isAndroid11Plus) "Activar Acceso a Todos los Archivos" else "Conceder Permiso de Almacenamiento",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Banner de Vinculación SAF para Android 11+ (si no tiene SAF vinculado o no hay estados recientes)
                if (isAndroid11Plus && (!hasSaf || recentStatuses.isEmpty())) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF132A1C), Color(0xFF183822))))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FolderShared, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vincular Carpeta de WhatsApp (SAF)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Abre la carpeta Media en el explorador. Solo toca 'USAR ESTA CARPETA' abajo y 'Permitir'.", color = Color(0xFF86EFAC), fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    try {
                                        val initialUri = WhatsAppStatusManager.getStatusesFolderInitialUri(isBusiness = false)
                                        safLauncher.launch(initialUri)
                                    } catch (_: Exception) {
                                        safLauncher.launch(null)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Conectar WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val initialUri = WhatsAppStatusManager.getStatusesFolderInitialUri(isBusiness = true)
                                        safLauncher.launch(initialUri)
                                    } catch (_: Exception) {
                                        safLauncher.launch(null)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC)),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("WhatsApp Business", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Chips de filtro con scroll horizontal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = {
                            Text(
                                text = "⚡ Recientes 24h (${recentStatuses.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedFilter == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF25D366).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF25D366)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text("📁 Todos (${statuses.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text("🎬 Videos (${statuses.count { it.isVideo }})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        label = { Text("🖼️ Fotos (${statuses.count { !it.isVideo }})", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF25D366))
                    }
                } else if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            if (selectedFilter == 0 && statuses.isNotEmpty()) {
                                Text("⚡ Sin estados en las últimas 24h", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Tienes ${statuses.size} estados en tu historial. Puedes verlos todos en la pestaña 'Todos'.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { selectedFilter = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Ver Historial Completo (${statuses.size})", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(30.dp))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("👀 No hay estados descargados aún", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "WhatsApp solo guarda los estados en la memoria de tu teléfono cuando los abres y los miras completos en la app de WhatsApp.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fila de diagnóstico de conexión
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (hasSaf) Color(0xFF14532D) else Color(0xFF334155))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (hasSaf) "📁 SAF Conectado" else "📁 SAF Pendiente",
                                            color = if (hasSaf) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (hasStoragePermission) Color(0xFF14532D) else Color(0xFF334155))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (hasStoragePermission) "🛡️ Permiso Otorgado" else "🛡️ Permiso Básico",
                                            color = if (hasStoragePermission) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { WhatsAppStatusManager.openWhatsApp(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Abrir WhatsApp para Mirar Estados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                if (isAndroid11Plus && !hasSaf) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val initialUri = WhatsAppStatusManager.getStatusesFolderInitialUri(isBusiness = false)
                                                safLauncher.launch(initialUri)
                                            } catch (_: Exception) {
                                                safLauncher.launch(null)
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC)),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Vincular Carpeta de WhatsApp (SAF)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { load() },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recargar Estados", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
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
                        items(filteredList, key = { it.path }) { item ->
                            val isSaved = savedSet.contains(item.path)
                            WhatsAppStatusCard(
                                item = item,
                                isSaved = isSaved,
                                onSave = {
                                    scope.launch {
                                        val res = WhatsAppStatusManager.saveStatusToGallery(context, item)
                                        if (res.isSuccess) {
                                            savedSet = savedSet + item.path
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
        val isItemSaved = savedSet.contains(pItem.path)

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
                    initialValue = WhatsAppStatusManager.getCachedThumbnail(pItem.path),
                    key1 = pItem.path
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
                            model = pItem.uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(56.dp)
                            )
                        }
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
                                    } else {
                                        try {
                                            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(pItem.uri, "video/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(playIntent, "Reproducir video"))
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "No hay reproductor de video disponible", Toast.LENGTH_SHORT).show()
                                        }
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
                                    savedSet = savedSet + pItem.path
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
        initialValue = WhatsAppStatusManager.getCachedThumbnail(item.path),
        key1 = item.path
    ) {
        if (value == null) {
            value = WhatsAppStatusManager.loadThumbnail(context, item)
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .shadow(8.dp, RoundedCornerShape(16.dp))
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
                model = item.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Video",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Video",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                    imageVector = if (item.isVideo) Icons.Default.PlayCircle else Icons.Default.Image,
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

        // Indicador de tiempo / frescura en esquina superior derecha
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (item.isRecent24h) Color(0xFF059669).copy(alpha = 0.9f)
                    else Color.Black.copy(alpha = 0.65f)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.getRelativeTimeSpan(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
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
