package com.auraplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlin.math.abs

data class AlbumModel(
    val name: String,
    val artist: String,
    val artworkUri: String?,
    val songs: List<MediaModel>
)

/**
 * 🌌 3D Cover Flow Carousel para la Pestaña Álbumes.
 * Recrea la experiencia icónica de navegación en perspectiva 3D con reflejos en suelo de cristal,
 * inercia física de resorte y retroalimentación háptica.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverFlowAlbumsCarousel(
    songs: List<MediaModel>,
    onPlayAlbum: (List<MediaModel>, Int) -> Unit,
    onOpenAlbumDetail: (AlbumModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val density = LocalDensity.current.density

    val albums = remember(songs) {
        songs.groupBy { it.album.ifBlank { "Álbum Desconocido" } }
            .map { (albumName, albumSongs) ->
                AlbumModel(
                    name = albumName,
                    artist = albumSongs.firstOrNull()?.artist ?: "Varios Artistas",
                    artworkUri = albumSongs.firstOrNull { it.artworkUri != null }?.artworkUri,
                    songs = albumSongs
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron álbumes en tu biblioteca",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }
        return
    }

    val initialPage = (albums.size / 2).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { albums.size }

    // Haptic feedback en cada pase de disco
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            AuraHaptic.tick(view)
        }
    }

    val currentAlbum = albums.getOrNull(pagerState.currentPage) ?: albums.first()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ESCENARIO 3D COVER FLOW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF090D18),
                            Color(0xFF0D1424),
                            Color(0xFF070A12)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 90.dp),
                pageSpacing = (-25).dp,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val album = albums[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            cameraDistance = 18f * density
                            rotationY = (pageOffset * -38f).coerceIn(-68f, 68f)
                            val scale = (1f - (abs(pageOffset) * 0.16f)).coerceIn(0.65f, 1f)
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - (abs(pageOffset) * 0.28f)).coerceIn(0.18f, 1f)
                        }
                        .clickable {
                            if (pagerState.currentPage == page) {
                                onOpenAlbumDetail(album)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // CARÁTULA PRINCIPAL
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF00F0FF).copy(alpha = 0.40f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (album.artworkUri != null) {
                            AsyncImage(
                                model = album.artworkUri,
                                contentDescription = album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Album,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }

                    // REFLEJO EN SUELO DE CRISTAL ESPEJADO
                    Box(
                        modifier = Modifier
                            .size(width = 190.dp, height = 55.dp)
                            .graphicsLayer {
                                scaleY = -1f
                                alpha = 0.35f
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xFF070A12))
                                    )
                                )
                            }
                    ) {
                        if (album.artworkUri != null) {
                            AsyncImage(
                                model = album.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // INFORMACIÓN Y BOTONES DEL ÁLBUM SELECCIONADO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentAlbum.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${currentAlbum.artist} • ${currentAlbum.songs.size} ${if (currentAlbum.songs.size == 1) "canción" else "canciones"}",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de acción del álbum
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        AuraHaptic.click(view)
                        onPlayAlbum(currentAlbum.songs, 0)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reproducir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = {
                        AuraHaptic.click(view)
                        onOpenAlbumDetail(currentAlbum)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Temas", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // PARTE INFERIOR: CUADRÍCULA RÁPIDA DE TODOS LOS ÁLBUMES
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(bottom = 120.dp, top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albums, key = { it.name }) { album ->
                val isCurrent = album.name == currentAlbum.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A))
                        .border(
                            width = if (isCurrent) 1.dp else 0.5.dp,
                            color = if (isCurrent) Color(0xFF00F0FF) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            AuraHaptic.tick(view)
                            onOpenAlbumDetail(album)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (album.artworkUri != null) {
                            AsyncImage(
                                model = album.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Album, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color(0xFF00F0FF) else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.songs.size} pistas",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
