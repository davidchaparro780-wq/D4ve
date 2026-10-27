package com.auraplayer.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.auraplayer.util.getSafeClipboardText
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.auraplayer.data.repository.FacebookDownloader
import com.auraplayer.data.repository.InstagramDownloader
import com.auraplayer.data.repository.TikTokDownloader
import com.auraplayer.ui.components.DaveRadarDialog
import com.auraplayer.ui.components.FacebookDownloaderDialog
import com.auraplayer.ui.components.InstagramDownloaderDialog
import com.auraplayer.ui.components.RytDownloadBottomSheet
import com.auraplayer.ui.components.TikTokDownloaderDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.OnlineTrack
import com.auraplayer.data.repository.DownloadEngine
import com.auraplayer.data.repository.DownloadStatus
import com.auraplayer.data.repository.GlobalSearchService
import com.auraplayer.ui.components.ShimmerCardPlaceholder
import com.auraplayer.ui.components.ShimmerSongRow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    searchService: GlobalSearchService,
    downloadEngine: DownloadEngine,
    onOpenDownloads: () -> Unit = {},
    pendingSearchQuery: String? = null,
    onPendingSearchQueryConsumed: () -> Unit = {},
    onPreviewTrack: (OnlineTrack) -> Unit,
    onDownloadComplete: () -> Unit,
    currentPlayingTitle: String? = null,
    isPlaying: Boolean = false,
    currentAccent: String = "MYSTIC_OBSIDIAN",
    onAddToQueue: ((OnlineTrack) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSource by rememberSaveable { mutableStateOf("Todas") } // "Todas", "Jamendo", "Deezer", "Archive"
    var selectedGenre by rememberSaveable { mutableStateOf("Trending") }
    var trackList by remember { mutableStateOf<List<OnlineTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Batch download state
    var isDownloadingBatch by remember { mutableStateOf(false) }
    var batchTotal by remember { mutableIntStateOf(0) }
    var batchCompleted by remember { mutableIntStateOf(0) }
    var batchCurrentTitle by remember { mutableStateOf("") }
    var showBatchConfirmDialog by remember { mutableStateOf(false) }

    val appPrefs = remember { context.getSharedPreferences("dave_app_prefs", Context.MODE_PRIVATE) }
    var downloadQuality by remember { mutableStateOf(appPrefs.getString("download_quality", "320") ?: "320") }
    var trackForDownloadChoice by remember { mutableStateOf<OnlineTrack?>(null) }
    var showTikTokDialog by remember { mutableStateOf(false) }
    var tiktokInitialUrl by remember { mutableStateOf("") }
    var showInstagramDialog by remember { mutableStateOf(false) }
    var instagramInitialUrl by remember { mutableStateOf("") }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var facebookInitialUrl by remember { mutableStateOf("") }
    var showRadarDialog by remember { mutableStateOf(false) }
    var showSocialDownloaderSheet by remember { mutableStateOf(false) }

    // When searching or viewing specific genre, BackHandler resets to Trending
    BackHandler(enabled = searchQuery.isNotBlank() || selectedGenre != "Trending") {
        if (searchQuery.isNotBlank()) {
            searchQuery = ""
        }
        selectedGenre = "Trending"
        scope.launch {
            searchError = null
            isLoading = true
            trackList = searchService.getTrending("Trending", selectedSource)
            isLoading = false
        }
    }

    val downloadStates by downloadEngine.downloadStates.collectAsState()

    val sourceFilters = listOf(
        "Todas" to androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_source_all),
        "YouTube" to androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_source_youtube),
        "Jamendo" to androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_source_jamendo)
    )

    val genres = listOf(
        "TikTok" to "🎵 Top TikTok",
        "Trending" to "🔥 Tendencias",
        "Pop" to "⚡ Pop",
        "Rock" to "🎸 Rock",
        "Electronic" to "🎧 Dance / EDM",
        "Lofi" to "☕ Lo-Fi",
        "HipHop" to "🎤 Hip-Hop",
        "Jazz" to "🎷 Jazz",
        "Acoustic" to "🌿 Acústico"
    )

    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun runSearch() {
        searchJob?.cancel()
        searchJob = scope.launch {
            isLoading = true
            try {
                trackList = searchService.searchOrExtract(searchQuery, selectedSource)
                searchError = null
            } catch (e: SocketTimeoutException) {
                searchError = "El servidor tardó demasiado. Comprueba tu internet y reintenta."
            } catch (e: UnknownHostException) {
                searchError = "No se pudo conectar. Comprueba tu internet y reintenta."
            } catch (e: IOException) {
                searchError = "No se pudo conectar. Comprueba tu internet y reintenta."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                searchError = "Hubo un problema al procesar los resultados"
            } finally {
                isLoading = false
            }
        }
    }

    fun executeSearch() {
        if (searchQuery.isBlank()) return
        focusManager.clearFocus()
        searchError = null
        if (TikTokDownloader.isTikTokUrl(searchQuery)) {
            tiktokInitialUrl = searchQuery.trim()
            showTikTokDialog = true
            return
        }
        if (InstagramDownloader.isInstagramUrl(searchQuery)) {
            instagramInitialUrl = searchQuery.trim()
            showInstagramDialog = true
            return
        }
        if (FacebookDownloader.isFacebookUrl(searchQuery)) {
            facebookInitialUrl = searchQuery.trim()
            showFacebookDialog = true
            return
        }
        runSearch()
    }

    LaunchedEffect(pendingSearchQuery) {
        val q = pendingSearchQuery
        if (!q.isNullOrBlank()) {
            searchQuery = q
            executeSearch()
            onPendingSearchQueryConsumed()
        }
    }

    // Load initial trending tracks
    LaunchedEffect(selectedGenre, selectedSource) {
        if (searchQuery.isBlank()) {
            searchError = null
            isLoading = true
            try {
                trackList = searchService.getTrending(selectedGenre, selectedSource)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    searchError = "No se pudieron cargar las tendencias. Revisa tu conexión."
                }
            } finally {
                isLoading = false
            }
        }
    }

    val isMysterious = com.auraplayer.ui.theme.isMysteriousAccent(currentAccent)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isMysterious) {
                        // Void esotérico para la Suite Misteriosa
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1B0D33).copy(alpha = 0.6f),
                                Color(0xFF070310),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    }
                )
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Compact Header Row with Integrated Quality Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_subtitle),
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unified Social Tools Button
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFFE2C55).copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFE2C55), Color(0xFF833AB4))
                                )
                            )
                            .clickable {
                                AuraHaptic.click(view)
                                showSocialDownloaderSheet = true
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_social),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Compact Glassmorphic Quality Toggle Pill
                    val is320 = downloadQuality == "320"
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = if (is320) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (is320) listOf(Color(0xFF1E2642), Color(0xFF0F1524))
                                    else listOf(Color(0xFF261D18), Color(0xFF150F0D))
                                )
                            )
                            .clickable {
                                AuraHaptic.tick(view)
                                val next = if (is320) "160" else "320"
                                downloadQuality = next
                                appPrefs.edit().putString("download_quality", next).apply()
                                val msg = if (next == "320") "💎 Calidad: 320 kbps (Hi-Fi)" else "⚡ Calidad: 160 kbps (Rápido)"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (is320) "💎 320k" else "⚡ 160k",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (is320) Color(0xFF38BDF8) else Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // Nebulosa de constelación arcano reactiva al audio (Suite Misteriosa)
            if (isMysterious) {
                val liveBass by com.auraplayer.audio.RealtimeVisualizerManager.instance.bassEnergyState.collectAsState()
                com.auraplayer.ui.components.StellarConstellationBanner(
                    bassEnergy = if (isPlaying) liveBass else 0.05f,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Omnibar Global Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1C243B), Color(0xFF0E1322))
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            androidx.compose.ui.res.stringResource(com.auraplayer.R.string.discover_search_hint),
                            color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = {
                            AuraHaptic.click(view)
                            executeSearch()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    AuraHaptic.click(view)
                                    searchQuery = ""
                                    scope.launch {
                                        searchError = null
                                        isLoading = true
                                        trackList = searchService.getTrending(selectedGenre, selectedSource)
                                        isLoading = false
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                // Quick Paste Button from Clipboard
                                IconButton(onClick = {
                                    AuraHaptic.click(view)
                                    val clip = context.getSafeClipboardText()
                                    if (!clip.isNullOrBlank()) {
                                        searchQuery = clip
                                        executeSearch()
                                    } else {
                                        Toast.makeText(context, "Portapapeles vacío", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Pegar enlace",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            val activeDownloadCount = downloadStates.values.count { status ->
                                status !is DownloadStatus.Completed && status !is DownloadStatus.Idle
                            }
                            IconButton(onClick = {
                                AuraHaptic.click(view)
                                onOpenDownloads()
                            }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Descargas",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    if (activeDownloadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                                .align(Alignment.TopEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (activeDownloadCount > 9) "9+" else "$activeDownloadCount",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        AuraHaptic.click(view)
                        executeSearch()
                    })
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Category & Genre Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { (key, label) ->
                    val isSelected = selectedGenre == key
                    Box(
                        modifier = Modifier
                            .shadow(
                                if (isSelected) 5.dp else 2.5.dp,
                                RoundedCornerShape(16.dp),
                                spotColor = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.5f) else Color.Black
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF4338CA))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E2840), Color(0xFF101626))
                                    )
                                }
                            )
                            .clickable {
                                AuraHaptic.tick(view)
                                selectedGenre = key
                                if (searchQuery.isNotBlank()) {
                                    searchQuery = ""
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Compact Results & Batch Download Action Bar (Replaces bulky 80dp box)
            if (!isLoading && trackList.isNotEmpty()) {
                val downloadableTracks = remember(trackList) { trackList.filter { it.isDownloadable } }
                val downloadableCount = downloadableTracks.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "Resultados" else "Tendencias ($selectedGenre)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${trackList.size} canciones",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        if (!isDownloadingBatch) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                        )
                                    )
                                    .clickable {
                                        AuraHaptic.click(view)
                                        if (downloadableCount > 0) {
                                            showBatchConfirmDialog = true
                                        } else {
                                            Toast.makeText(context, "No hay canciones descargables en esta lista", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Descargar Todo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$batchCompleted/$batchTotal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    // Live progress bar when batch download is active
                    if (isDownloadingBatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = if (batchTotal > 0) batchCompleted.toFloat() / batchTotal.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Descargando: ${batchCurrentTitle.ifBlank { "audio..." }}",
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // Empty / Initial state counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Resultados unificados" else "Tendencias ($selectedGenre)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${trackList.size} canciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showBatchConfirmDialog) {
                val downloadableTracks = trackList.filter { it.isDownloadable }
                AlertDialog(
                    onDismissRequest = { showBatchConfirmDialog = false },
                    title = {
                        Text(
                            text = "Descargar Álbum o Lista",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "Se descargarán ${downloadableTracks.size} canciones completas con carátula oficial y metadatos en segundo plano directamente a tu música local.\n\n¿Deseas continuar?",
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showBatchConfirmDialog = false
                                isDownloadingBatch = true
                                batchTotal = downloadableTracks.size
                                batchCompleted = 0
                                batchCurrentTitle = ""
                                Toast.makeText(context, "Iniciando descarga por lotes (${downloadableTracks.size} canciones)...", Toast.LENGTH_SHORT).show()

                                scope.launch {
                                    downloadEngine.downloadBatch(
                                        tracks = downloadableTracks,
                                        onProgress = { completed, total, currentTitle ->
                                            batchCompleted = completed
                                            batchTotal = total
                                            batchCurrentTitle = currentTitle
                                        },
                                        onAllCompleted = {
                                            isDownloadingBatch = false
                                            Toast.makeText(context, "✓ ¡Descarga completa! $batchTotal canciones guardadas en tu biblioteca.", Toast.LENGTH_LONG).show()
                                            onDownloadComplete()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Descargar (${downloadableTracks.size})")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showBatchConfirmDialog = false }
                        ) {
                            Text("Cancelar", color = Color(0xFF94A3B8))
                        }
                    },
                    containerColor = Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(18.dp)
                )
            }

            // Results List / Loading State
            if (isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp)
                ) {
                    item {
                        Text(
                            text = "Buscando en catálogo global...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            items(4) {
                                ShimmerCardPlaceholder()
                            }
                        }
                    }
                    items(3) {
                        ShimmerSongRow()
                    }
                }
            } else if (trackList.isEmpty() && searchError != null) {
                val errorMessage = searchError ?: ""
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFEF4444).copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF261A28), Color(0xFF140D17))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin resultados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { runSearch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Reintentar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else if (trackList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No se encontraron canciones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Prueba con otro nombre, artista o pega un enlace de YouTube/Spotify arriba.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        item(key = "bento_categories_hub") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✨ Explorar por Categorías",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                // Asymmetrical Bento Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoCard(
                                        title = "🔥 Top TikTok",
                                        subtitle = "Audios virales en tendencia",
                                        gradient = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE)),
                                        modifier = Modifier.weight(1.2f).height(94.dp),
                                        onClick = {
                                            AuraHaptic.click(view)
                                            selectedGenre = "TikTok"
                                            executeSearch()
                                        }
                                    )
                                    BentoCard(
                                        title = "🌴 Urbano & Flow",
                                        subtitle = "Trap & Reggaetón",
                                        gradient = listOf(Color(0xFFF12711), Color(0xFFF5AF19)),
                                        modifier = Modifier.weight(1f).height(94.dp),
                                        onClick = {
                                            AuraHaptic.click(view)
                                            selectedGenre = "Reggaeton"
                                            executeSearch()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoCard(
                                        title = "🎧 Lo-Fi Chill",
                                        subtitle = "Relax y concentración",
                                        gradient = listOf(Color(0xFF654EA3), Color(0xFFEAAFC8)),
                                        modifier = Modifier.weight(1f).height(90.dp),
                                        onClick = {
                                            AuraHaptic.click(view)
                                            selectedGenre = "Lo-Fi"
                                            executeSearch()
                                        }
                                    )
                                    BentoCard(
                                        title = "⚡ EDM & Hi-Fi",
                                        subtitle = "Electrónica 320 kbps",
                                        gradient = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                                        modifier = Modifier.weight(1f).height(90.dp),
                                        onClick = {
                                            AuraHaptic.click(view)
                                            selectedGenre = "Electronic"
                                            executeSearch()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "🎵 Pistas Destacadas ($selectedGenre)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    items(trackList, key = { it.id }, contentType = { "online_track" }) { track ->
                        val status = downloadStates[track.id] ?: DownloadStatus.Idle
                        val isPlayingThis = isPlaying && !currentPlayingTitle.isNullOrBlank() &&
                                (currentPlayingTitle.equals(track.title, ignoreCase = true) ||
                                 currentPlayingTitle.contains(track.title, ignoreCase = true) ||
                                 track.title.contains(currentPlayingTitle, ignoreCase = true))

                        OnlineTrackCard(
                            track = track,
                            status = status,
                            isPlayingThis = isPlayingThis,
                            onPreview = {
                                onPreviewTrack(track)
                            },
                            onDownload = {
                                if (!track.isDownloadable) {
                                    Toast.makeText(context, "⚠️ Esta pista es una muestra de 30s. Filtra por Jamendo o YouTube para canciones completas.", Toast.LENGTH_LONG).show()
                                } else {
                                    trackForDownloadChoice = track
                                }
                            },
                            onAddToQueue = {
                                onAddToQueue?.invoke(track) ?: run {
                                    Toast.makeText(context, "Añadida: ${track.title}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal de Descarga Estilo RYT Music: Música (HQ 320k / Ligero 128k) o Video (MP4 HD 720p / SD 360p)
    if (trackForDownloadChoice != null) {
        val targetTrack = trackForDownloadChoice!!
        RytDownloadBottomSheet(
            track = targetTrack,
            downloadEngine = downloadEngine,
            onDismiss = { trackForDownloadChoice = null },
            onDownloadCompleted = {
                onDownloadComplete()
            },
            onPlayTrackNow = { completedTrack ->
                onPreviewTrack(completedTrack)
            }
        )
    }

    if (showSocialDownloaderSheet) {
        AlertDialog(
            onDismissRequest = { showSocialDownloaderSheet = false },
            title = {
                Text(
                    text = "📥 Descargar de Redes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona una red para descargar videos o audio directamente:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    // TikTok
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF00F2FE).copy(alpha = 0.15f), Color(0xFFFE2C55).copy(alpha = 0.25f))))
                            .border(1.dp, Color(0xFFFE2C55).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                AuraHaptic.click(view)
                                showSocialDownloaderSheet = false
                                tiktokInitialUrl = if (TikTokDownloader.isTikTokUrl(searchQuery)) searchQuery.trim() else ""
                                showTikTokDialog = true
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "TikTok Downloader", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Sin marca de agua en MP4 o MP3", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    // Instagram
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF833AB4).copy(alpha = 0.2f), Color(0xFFFD1D1D).copy(alpha = 0.2f))))
                            .border(1.dp, Color(0xFFFD1D1D).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                AuraHaptic.click(view)
                                showSocialDownloaderSheet = false
                                instagramInitialUrl = if (InstagramDownloader.isInstagramUrl(searchQuery)) searchQuery.trim() else ""
                                showInstagramDialog = true
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Instagram Reels", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Reels, audios y publicaciones HD", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    // Facebook
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1877F2).copy(alpha = 0.18f))
                            .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                AuraHaptic.click(view)
                                showSocialDownloaderSheet = false
                                facebookInitialUrl = if (FacebookDownloader.isFacebookUrl(searchQuery)) searchQuery.trim() else ""
                                showFacebookDialog = true
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📘", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Facebook Watch", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Videos y reels públicos en HD", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    // Radar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF00F0FF).copy(alpha = 0.18f), Color(0xFF8B5CF6).copy(alpha = 0.18f))))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                AuraHaptic.click(view)
                                showSocialDownloaderSheet = false
                                showRadarDialog = true
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔍", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "DaVE Radar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Buscador global multi-plataforma", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSocialDownloaderSheet = false }) {
                    Text("Cerrar", color = Color(0xFF38BDF8))
                }
            },
            containerColor = Color(0xFF101626),
            tonalElevation = 8.dp
        )
    }

    if (showTikTokDialog) {
        TikTokDownloaderDialog(
            initialUrl = tiktokInitialUrl,
            onDismiss = { showTikTokDialog = false },
            onDownloadCompleted = { _, _ -> }
        )
    }

    if (showInstagramDialog) {
        InstagramDownloaderDialog(
            initialUrl = instagramInitialUrl,
            onDismiss = { showInstagramDialog = false },
            onDownloadCompleted = { _ -> }
        )
    }

    if (showFacebookDialog) {
        FacebookDownloaderDialog(
            initialUrl = facebookInitialUrl,
            onDismiss = { showFacebookDialog = false },
            onDownloadCompleted = { _ -> }
        )
    }

    if (showRadarDialog) {
        DaveRadarDialog(
            onDismiss = { showRadarDialog = false },
            onSearchTrackInApp = { query ->
                searchQuery = query
                executeSearch()
            }
        )
    }
}

@Composable
fun BentoCard(
    title: String,
    subtitle: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = gradient.first().copy(alpha = 0.45f))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(gradient))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedEqualizerBars(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF38BDF8)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar1)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar3)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar2)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
    }
}

@Composable
fun OnlineTrackCard(
    track: OnlineTrack,
    status: DownloadStatus,
    isPlayingThis: Boolean,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }

    val sourceBadgeColor = when (track.source) {
        "YouTube" -> Color(0xFFEF4444)
        "Jamendo" -> Color(0xFF8B5CF6)
        "Deezer" -> Color(0xFF3B82F6)
        "Archive" -> Color(0xFFF59E0B)
        "TikTok" -> Color(0xFFEC4899)
        else -> Color(0xFF10B981)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                if (isPlayingThis) 6.dp else 3.dp,
                RoundedCornerShape(14.dp),
                spotColor = if (isPlayingThis) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color.Black
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    if (isPlayingThis) listOf(Color(0xFF231E4D), Color(0xFF13102C))
                    else listOf(Color(0xFF1C243B), Color(0xFF0F1422))
                )
            )
            .clickable {
                AuraHaptic.click(view)
                onPreview()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Cover Art with Playing Overlay / Animated Bars
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(3.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF251F42), Color(0xFF131122))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFF8B5CF6).copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            if (track.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track.coverUrl)
                        .size(180, 180)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // If playing: Translucent dark scrim + Animated Equalizer Bars
            if (isPlayingThis) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedEqualizerBars(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Info: Title, Artist, Badges
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlayingThis) Color(0xFF38BDF8) else Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${track.artist} • ${track.durationFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Source Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(sourceBadgeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.source,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = sourceBadgeColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Bitrate / Quality Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.format,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Audio Waveform Preview Indicator
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.22f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        AnimatedEqualizerBars(modifier = Modifier.size(10.dp), color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▶ Preview",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right Actions: Liquid Download Action + More Options Menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (status) {
                is DownloadStatus.Idle -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(
                                3.dp,
                                CircleShape,
                                spotColor = if (track.isDownloadable) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.Black
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    if (track.isDownloadable) listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    else listOf(Color(0xFF151824), Color(0xFF0D0F17))
                                )
                            )
                            .clickable {
                                AuraHaptic.click(view)
                                onDownload()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.isDownloadable) Icons.Default.CloudDownload else Icons.Default.Info,
                            contentDescription = if (track.isDownloadable) "Descargar Canción Completa" else "Muestra 30s",
                            tint = if (track.isDownloadable) Color(0xFF38BDF8) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                is DownloadStatus.Downloading -> {
                    // Liquid Download Pulse Indicator
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFF38BDF8).copy(alpha = 0.55f))
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { status.progress / 100f },
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "${status.progress}%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
                is DownloadStatus.Tagging -> {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xFFEC4899).copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFEC4899),
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                is DownloadStatus.Completed -> {
                    // Liquid Completion Shockwave Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFF10B981).copy(alpha = 0.75f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF059669), Color(0xFF10B981))
                                )
                            )
                            .border(1.2.dp, Color(0xFF34D399), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Descargado",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                is DownloadStatus.Error -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, spotColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF450A0A), Color(0xFF290505))
                                )
                            )
                            .clickable {
                                AuraHaptic.click(view)
                                onDownload()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Reintentar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // More Options Menu (⋮)
            Box {
                IconButton(
                    onClick = {
                        AuraHaptic.tick(view)
                        showMenu = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1F1B3E), Color(0xFF121025))
                            )
                        )
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PlaylistAdd,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Añadir a la cola", color = Color.White, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            AuraHaptic.click(view)
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar título", color = Color.White, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            AuraHaptic.tick(view)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("song_title", "${track.title} - ${track.artist}")
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado: ${track.title}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
