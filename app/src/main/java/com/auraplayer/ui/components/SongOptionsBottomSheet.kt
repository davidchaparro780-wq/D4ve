package com.auraplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: MediaModel,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditTags: () -> Unit,
    onFetchCover: () -> Unit,
    onCutAudio: () -> Unit,
    onShowDetails: () -> Unit,
    onShareSong: () -> Unit,
    onShareQr: () -> Unit,
    onSetAsRingtone: () -> Unit,
    onDeleteSong: () -> Unit
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val extension = try {
        val ext = File(song.path).extension.uppercase()
        if (ext.isNotEmpty()) ext else "AUDIO"
    } catch (_: Exception) {
        "AUDIO"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B0F19),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF38BDF8).copy(alpha = 0.55f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header: Portada + Info de la pista
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF131B2E), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, Color(0xFF1E293B)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.artworkUri != null) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artist} • ${song.formattedDuration}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Badge de formato de archivo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = extension,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Barra de 4 Acciones Rápidas (Siguiente, Cola, Playlist, Favorito)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SkipNext,
                        iconTint = Color(0xFF38BDF8),
                        label = "Siguiente",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onPlayNext()
                        }
                    )

                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PlaylistAdd,
                        iconTint = Color(0xFF06B6D4),
                        label = "A la Cola",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onAddToQueue()
                        }
                    )

                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.QueueMusic,
                        iconTint = Color(0xFFA855F7),
                        label = "Playlist",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onAddToPlaylist()
                        }
                    )

                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        iconTint = if (isFavorite) Color(0xFFEC4899) else Color(0xFF94A3B8),
                        label = if (isFavorite) "Favorito" else "Añadir Fav",
                        onClick = {
                            AuraHaptic.heavy(view)
                            onToggleFavorite()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Sección 1: Edición & Multimedia
            item {
                SectionLabel(title = "EDICIÓN & MULTIMEDIA")
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131B2E).copy(alpha = 0.7f))
                        .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
                ) {
                    GroupedOptionItem(
                        icon = Icons.Default.Edit,
                        iconTint = Color(0xFFF59E0B),
                        title = "Editar Etiquetas (ID3)",
                        subtitle = "Modificar título, artista, álbum, año y género",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onEditTags()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.Image,
                        iconTint = Color(0xFF06B6D4),
                        title = "Descargar / Actualizar Portada",
                        subtitle = "Obtener carátula oficial de alta resolución en línea",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onFetchCover()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.ContentCut,
                        iconTint = Color(0xFF10B981),
                        title = "Cortar Audio / Creador de Tonos",
                        subtitle = "Recortar fragmento y guardarlo como tono o alarma",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onCutAudio()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección 2: Archivo & Sistema
            item {
                SectionLabel(title = "ARCHIVO & OPCIONES")
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131B2E).copy(alpha = 0.7f))
                        .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
                ) {
                    GroupedOptionItem(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF38BDF8),
                        title = "Detalles del Archivo",
                        subtitle = "Ruta local, peso en MB, frecuencia de muestreo",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onShowDetails()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.Share,
                        iconTint = Color(0xFF38BDF8),
                        title = "Compartir Canción",
                        subtitle = "Enviar el archivo de audio a través de WhatsApp, Bluetooth, etc.",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onShareSong()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.QrCode2,
                        iconTint = Color(0xFF06B6D4),
                        title = "Compartir con Código QR",
                        subtitle = "Transferencia offline directa a otro dispositivo",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onShareQr()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.NotificationsActive,
                        iconTint = Color(0xFFA855F7),
                        title = "Establecer como Tono Directo",
                        subtitle = "Asignar directamente como tono de llamada del sistema",
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                            onSetAsRingtone()
                        }
                    )

                    OptionDivider()

                    GroupedOptionItem(
                        icon = Icons.Default.Delete,
                        iconTint = Color(0xFFEF4444),
                        title = "Eliminar del Teléfono",
                        subtitle = "Borrar el archivo de forma permanente de tu almacenamiento",
                        titleColor = Color(0xFFEF4444),
                        onClick = {
                            AuraHaptic.heavy(view)
                            onDismiss()
                            onDeleteSong()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2338), Color(0xFF111726))
                )
            )
            .border(
                BorderStroke(1.dp, Color(0xFF26354D)),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun GroupedOptionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OptionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
    )
}
