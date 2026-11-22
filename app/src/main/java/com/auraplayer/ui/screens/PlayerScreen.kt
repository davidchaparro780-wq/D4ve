package com.auraplayer.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.auraplayer.util.findActivity
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import com.auraplayer.MainActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.FastOutSlowInEasing
import com.auraplayer.ui.components.AuraRippleRings
import com.auraplayer.ui.components.FloatingMusicParticles
import com.auraplayer.ui.components.PlayPauseMorphButton
import com.auraplayer.ui.components.LiquidShimmerProgressBar
import com.auraplayer.ui.components.AuraTouchSparks
import com.auraplayer.ui.components.NeonPlasmaCrown
import com.auraplayer.ui.components.SupernovaShockwave
import com.auraplayer.ui.components.EdgeAuroraGlow
import com.auraplayer.ui.components.HyperdriveWarpTunnel
import com.auraplayer.ui.components.StemMixerDialog
import com.auraplayer.ui.components.StealthCalculatorDialog
import com.auraplayer.ui.components.ClubPartyDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import com.auraplayer.ui.components.bounceClick
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import com.auraplayer.data.model.computeAudioFormatBadge
import androidx.compose.ui.platform.LocalView
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.DynamicPaletteManager
import com.auraplayer.audio.RelaxAmbienceManager
import com.auraplayer.ui.components.RelaxAmbienceDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.auraplayer.audio.SilenceTrimmerManager
import com.auraplayer.audio.LoudnessNormalizerManager
import com.auraplayer.audio.ZapPreviewManager
import com.auraplayer.audio.PartyLinkManager
import com.auraplayer.service.FloatingBubbleService
import com.auraplayer.ui.components.ZapDialog
import com.auraplayer.ui.components.PartyLinkDialog
import com.auraplayer.audio.EqualizerManager
import com.auraplayer.ui.components.AudioCutterDialog
import com.auraplayer.ui.components.EdgeLighting
import com.auraplayer.ui.components.FluidAmbilightGlow
import com.auraplayer.ui.components.SoundboardDialog
import com.auraplayer.ui.components.StoryShareHelper
import com.auraplayer.audio.ViralAudioEffectsManager
import com.auraplayer.audio.ViralAudioMode
import com.auraplayer.data.repository.SyncedLyricsManager
import com.auraplayer.ui.components.SyncedLyricsDialog
import com.auraplayer.ui.components.CarDriveModeDialog
import com.auraplayer.ui.components.SongHunterDialog
import com.auraplayer.ui.components.MoodWheelDialog
import com.auraplayer.ui.components.CassetteTapeSkin
import com.auraplayer.ui.components.IpodClassicSkin
import com.auraplayer.ui.components.SquareAlbumArtSkin
import com.auraplayer.ui.components.AuroraMeshBackground
import com.auraplayer.ui.components.VinylTurntableSkin
import com.auraplayer.ui.components.RetroDiscmanSkin
import com.auraplayer.ui.components.RetroIpodSkin
import com.auraplayer.ui.components.AnalogVuMetersSkin
import com.auraplayer.ui.components.DynamicWaveformSeekbar
import com.auraplayer.ui.components.ExplosiveHeartBurst
import com.auraplayer.ui.components.CanvasLoopsOverlay
import com.auraplayer.ui.components.CanvasLoopTheme
import com.auraplayer.ui.components.AudioQualityDialog
import com.auraplayer.ui.components.WaveformVisualizer
import com.auraplayer.ui.components.MysteriousThemeDialog
import com.auraplayer.ui.components.ArcaneRunicOrbit
import com.auraplayer.ui.components.ArcaneMonolithSkin
import com.auraplayer.ui.components.ArcaneFrequencyDialog
import com.auraplayer.ui.components.PhantomMistOverlay
import com.auraplayer.audio.LunarPhaseManager
import com.auraplayer.ui.components.StellarConstellationBanner
import com.auraplayer.ui.components.NecronomicScrollText
import com.auraplayer.ui.components.OracleCard
import com.auraplayer.ui.components.predictDailySong
import com.auraplayer.data.repository.PlaylistManager
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.auraplayer.service.PlaybackService
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.data.model.MediaModel
import com.auraplayer.data.model.RepeatMode
import com.auraplayer.data.repository.SongLyrics
import com.auraplayer.ui.components.AquaZenDialog
import com.auraplayer.ui.components.AuraSanctuaryDialog
import com.auraplayer.ui.components.HoloCockpitDialog
import com.auraplayer.ui.components.MinidiscRetroDialog
import com.auraplayer.ui.components.SupercarCockpitDialog
import com.auraplayer.ui.components.AuraGalaxyDialog
import com.auraplayer.ui.components.PixelArcadeDialog
import com.auraplayer.ui.components.SteampunkPhonographDialog
import com.auraplayer.ui.components.Boombox80sDialog
import com.auraplayer.ui.components.LofiCafeDialog
import com.auraplayer.ui.components.PlayerMoreOptionsSheet
import com.auraplayer.util.AppLog
import com.auraplayer.util.ArtworkPaletteExtractor
import com.auraplayer.util.DynamicArtworkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.math.sin

private const val TAG = "PlayerScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    playbackSpeed: Float,
    playbackPitch: Float,
    isShakeEnabled: Boolean,
    isWaveEnabled: Boolean = false,
    lyrics: SongLyrics?,
    isLoadingLyrics: Boolean,
    queueSongs: List<MediaModel> = emptyList(),
    onQueueSongClick: (MediaModel, Int) -> Unit = { _, _ -> },
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShake: () -> Unit,
    onToggleWave: () -> Unit = {},
    onOpenSoundboard: () -> Unit = {},
    onOpenWrapped: () -> Unit = {},
    onAudioFxChange: (speed: Float, pitch: Float) -> Unit,
    onOpenSleepTimer: () -> Unit,
    sleepTimerManager: com.auraplayer.audio.SleepTimerManager? = null,
    onOpenCarMode: () -> Unit = {},
    onOpenStandBy: () -> Unit = {},
    onDeleteSong: (MediaModel) -> Unit,
    vibeMode: com.auraplayer.audio.AudioVibe = com.auraplayer.audio.AudioVibe.NORMAL,
    onCycleVibe: () -> com.auraplayer.audio.AudioVibe = { com.auraplayer.audio.AudioVibe.NORMAL },
    isHapticBass: Boolean = false,
    onToggleHapticBass: () -> Unit = {},
    isFlashlightActive: Boolean = false,
    onToggleFlashlight: () -> Unit = {},
    onOpenHeadphones: () -> Unit = {},
    isHeadphonesConnected: Boolean = false,
    onSearchOnline: (String) -> Unit = {},
    onAccentChange: (String) -> Unit = {},
    onOpenFullScreenVideo: (MediaModel) -> Unit = {},
    onDownloadVideoForSong: (MediaModel) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentMedia == null) return

    val effectiveDurationMs = remember(durationMs, currentMedia.duration) {
        if (durationMs > 0L) durationMs else currentMedia.duration
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val artworkPalette = DynamicArtworkTheme.current
    LaunchedEffect(currentMedia?.artworkUri) {
        val palette = ArtworkPaletteExtractor.extract(context, currentMedia?.artworkUri)
        DynamicArtworkTheme.update(palette)
    }

    var isEdgeLightingEnabled by remember { mutableStateOf(false) }
    var isSpatial8D by remember { mutableStateOf(EqualizerManager.instance.isSpatial8DEnabled) }
    val skinPrefs = remember { context.getSharedPreferences("aura_skin_prefs", Context.MODE_PRIVATE) }
    var centerVisualizerMode by rememberSaveable {
        mutableIntStateOf(skinPrefs.getInt("center_visualizer_mode", 0))
    }
    fun setVisualizerSkin(mode: Int) {
        centerVisualizerMode = mode
        skinPrefs.edit().putInt("center_visualizer_mode", mode).apply()
    }
    fun cycleVisualizerSkin() {
        val nextMode = (centerVisualizerMode + 1) % 8
        setVisualizerSkin(nextMode)
        val label = when (nextMode) {
            0 -> "🖼️ Carátula Cuadrada Completa HD"
            1 -> "💿 Vinilo Giratorio Hi-Fi"
            2 -> "📼 Cassette Tape Retro"
            3 -> "💿 CD Discman 2000s"
            4 -> "⚡ Espectro FFT en Vivo"
            5 -> "📱 iPod Classic"
            6 -> "🎛️ Vúmetros Analógicos Hi-Fi"
            else -> "🗿 Monolito Arcano"
        }
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
    }
    var isGyro3dEnabled by rememberSaveable { mutableStateOf(true) }
    val gyroTiltState = com.auraplayer.audio.rememberGyroscopeTilt(isEnabled = isGyro3dEnabled)
    val realWaveform by com.auraplayer.audio.rememberAudioWaveform(currentMedia)
    var isVideoMode by rememberSaveable {
        mutableStateOf(currentMedia?.isVideo == true ||
            currentMedia?.path?.endsWith(".mp4", ignoreCase = true) == true ||
            currentMedia?.path?.endsWith(".mkv", ignoreCase = true) == true)
    }

    LaunchedEffect(currentMedia?.id) {
        isVideoMode = currentMedia?.isVideo == true ||
            currentMedia?.path?.endsWith(".mp4", ignoreCase = true) == true ||
            currentMedia?.path?.endsWith(".mkv", ignoreCase = true) == true
    }
    var showSoundboardDialog by remember { mutableStateOf(false) }
    var showSyncedLyricsDialog by remember { mutableStateOf(false) }
    var showIpodClassicDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showQuickDspDialog by remember { mutableStateOf(false) }
    var showStemMixerDialog by remember { mutableStateOf(false) }
    var showStealthCalculator by remember { mutableStateOf(false) }
    var showLofiCafeDialog by remember { mutableStateOf(false) }
    var showGalaxyDialog by remember { mutableStateOf(false) }
    var showBoomboxDialog by remember { mutableStateOf(false) }
    var showPixelArcadeDialog by remember { mutableStateOf(false) }
    var showSteampunkDialog by remember { mutableStateOf(false) }
    var showAquaZenDialog by remember { mutableStateOf(false) }
    var showSupercarDialog by remember { mutableStateOf(false) }
    var showMinidiscDialog by remember { mutableStateOf(false) }
    var showHoloCockpitDialog by remember { mutableStateOf(false) }
    var showSanctuaryDialog by remember { mutableStateOf(false) }
    var showClubPartyDialog by remember { mutableStateOf(false) }
    var showCarDriveModeDialog by remember { mutableStateOf(false) }
    var showSongHunterDialog by remember { mutableStateOf(false) }
    var showMoodAuraDialog by remember { mutableStateOf(false) }
    var showVolumeBoosterDialog by remember { mutableStateOf(false) }
    var showMysteriousThemeDialog by remember { mutableStateOf(false) }
    val boostPercent by com.auraplayer.audio.VolumeBoosterManager.instance.boostPercent.collectAsState()
    val hapticIntensity by com.auraplayer.audio.BassHapticManager.instance.intensity.collectAsState()
    val abPointA by com.auraplayer.audio.ABRepeatManager.instance.pointA.collectAsState()
    val abPointB by com.auraplayer.audio.ABRepeatManager.instance.pointB.collectAsState()
    val isAbActive by com.auraplayer.audio.ABRepeatManager.instance.isActive.collectAsState()
    val comfortPrefs = remember { context.getSharedPreferences("dave_ui_comfort_prefs", Context.MODE_PRIVATE) }
    var isComfortView by remember { mutableStateOf(comfortPrefs.getBoolean("is_comfort_view", true)) }
    var eyeComfortMode by remember { mutableIntStateOf(comfortPrefs.getInt("eye_comfort_mode", 0)) } // 0: Dinámico, 1: Cálido Nocturno, 2: OLED Negro Puro

    fun setComfortView(enabled: Boolean) {
        isComfortView = enabled
        comfortPrefs.edit().putBoolean("is_comfort_view", enabled).apply()
        val msg = if (enabled) "🌿 Vista Cómoda activada" else "🎛️ Vista Pro Completa (Herramientas FX y VIP)"
        try {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    fun cycleEyeComfortMode() {
        val next = (eyeComfortMode + 1) % 3
        eyeComfortMode = next
        comfortPrefs.edit().putInt("eye_comfort_mode", next).apply()
        val msg = when (next) {
            0 -> "🎨 Visual: Dinámico Natural"
            1 -> "🌙 Visual: Luz Cálida Nocturna (Descanso Ocular)"
            else -> "⬛ Visual: OLED Negro Puro (0% Fatiga)"
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    val smartDjManager = remember { com.auraplayer.audio.SmartDjTransitionManager.getInstance(context) }
    var isSmartDj by remember { mutableStateOf(smartDjManager.isSmartDjEnabled) }
    var isKaraokeVocalRemoved by remember { mutableStateOf(EqualizerManager.instance.isKaraokeMode) }
    val stemManager = remember { com.auraplayer.audio.StemMixerManager.getInstance(context) }
    val syncedLyricsManager = remember { SyncedLyricsManager.getInstance(context) }
    val viralEffectsManager = remember { ViralAudioEffectsManager.getInstance(context) }
    var currentViralMode by remember { mutableStateOf(viralEffectsManager.currentMode) }
    var currentCanvasLoop by remember { mutableStateOf(CanvasLoopTheme.OFF) }

    val tubeAmpManager = remember { com.auraplayer.audio.TubeAmpManager.getInstance(context) }
    var isTubeAmpActive by remember { mutableStateOf(tubeAmpManager.isTubeAmpEnabled) }
    var isNightDrcActive by remember { mutableStateOf(tubeAmpManager.isNightDrcEnabled) }

    val playerPlaylistManager = remember { PlaylistManager(context) }
    var currentThemeAccentState by remember { mutableStateOf(playerPlaylistManager.getThemeAccent()) }
    val isMysteriousTheme = com.auraplayer.ui.theme.isMysteriousAccent(currentThemeAccentState)
    val lunarPhase = remember { LunarPhaseManager.getCurrentPhase() }
    var currentArcaneHz by remember { mutableIntStateOf(432) }
    var showArcaneFrequencyDialog by remember { mutableStateOf(false) }
    var showOracleCard by remember { mutableStateOf(false) }

    var showFxDialog by remember { mutableStateOf(false) }
    var showCutterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLyricsView by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showMoreToolsSheet by remember { mutableStateOf(false) }
    var showAudioSpecSheet by remember { mutableStateOf(false) }
    var visualizerMode by remember { mutableIntStateOf(0) } // 0: Spectrum bars, 1: Neon wave, 2: Radial pulse, 3: Starfield
    var lyricsFontSizeMultiplier by remember { mutableFloatStateOf(1.0f) }
    var isPlayerTranslationEnabled by remember { mutableStateOf(false) }
    var playerLyricsTranslations by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isPlayerTranslating by remember { mutableStateOf(false) }

    LaunchedEffect(isPlayerTranslationEnabled, lyrics) {
        val currentLyrics = lyrics
        if (isPlayerTranslationEnabled && currentLyrics?.lines?.isNotEmpty() == true && playerLyricsTranslations.isEmpty()) {
            isPlayerTranslating = true
            val uniqueLines = currentLyrics.lines.map { it.text }.distinct().take(40)
            playerLyricsTranslations = com.auraplayer.data.repository.LyricsTranslatorService.batchTranslate(uniqueLines)
            isPlayerTranslating = false
        }
    }

    var seekBadgeText by remember { mutableStateOf("") }
    var showSeekBadge by remember { mutableStateOf(false) }
    var seekBadgeIsForward by remember { mutableStateOf(true) }
    var showHeartBurst by remember { mutableStateOf(false) }

    // A-B Looper (Segment repeat studio)
    var abLoopA by remember { mutableStateOf<Long?>(null) }
    var abLoopB by remember { mutableStateOf<Long?>(null) }
    var isAbLoopActive by remember { mutableStateOf(false) }

    // Reset A-B looper whenever track changes so new song starts fresh
    LaunchedEffect(currentMedia?.id) {
        abLoopA = null
        abLoopB = null
        isAbLoopActive = false
        com.auraplayer.audio.ABRepeatManager.instance.clear()
    }

    LaunchedEffect(currentPositionMs, isAbLoopActive, abLoopA, abLoopB, isPlaying) {
        if (isAbLoopActive && abLoopA != null && abLoopB != null && isPlaying) {
            if (currentPositionMs >= abLoopB!!) {
                onSeek(abLoopA!!)
            }
        }
    }

    LaunchedEffect(showSeekBadge) {
        if (showSeekBadge) {
            delay(850)
            showSeekBadge = false
        }
    }

    LaunchedEffect(showHeartBurst) {
        if (showHeartBurst) {
            delay(950)
            showHeartBurst = false
        }
    }

    fun triggerPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val act = context.findActivity()
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(16, 9))
                    .build()
                act?.enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Toast.makeText(context, "Ventana flotante: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Ventana flotante requiere Android 8+", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareSong() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, currentMedia.uri)
                putExtra(Intent.EXTRA_TEXT, "Escuchando '${currentMedia.title}' de ${currentMedia.artist} en DaVE Player")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir música"))
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo compartir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    var showStickerDialog by remember { mutableStateOf(false) }
    var showPitchSpeedDialog by remember { mutableStateOf(false) }
    var showStoryCardDialog by remember { mutableStateOf(false) }
    var storyCardInitialQuote by remember { mutableStateOf("") }
    var showFullscreenVisualizer by remember { mutableStateOf(false) }
    val view = LocalView.current
    var showQuickEqDialog by remember { mutableStateOf(false) }
    var showRelaxAmbienceDialog by remember { mutableStateOf(false) }
    var isKaraokeActive by remember { mutableStateOf(com.auraplayer.audio.KaraokeVocalManager.instance.isKaraokeEnabled) }
    val looper = remember { com.auraplayer.audio.AbLooperManager.instance }
    val silenceTrimmerMgr = remember { SilenceTrimmerManager.getInstance(context) }
    var isSilenceTrimmerActive by remember { mutableStateOf(silenceTrimmerMgr.isEnabled) }
    val loudnessNormMgr = remember { LoudnessNormalizerManager.getInstance(context) }
    var isLoudnessNormalizerActive by remember { mutableStateOf(loudnessNormMgr.isEnabled) }
    val zapManager = remember { ZapPreviewManager.getInstance(context) }
    var showZapDialog by remember { mutableStateOf(false) }
    val partyLinkManager = remember { PartyLinkManager.getInstance(context) }
    var showPartyLinkDialog by remember { mutableStateOf(false) }
    var isBubbleActive by remember { mutableStateOf(FloatingBubbleService.isRunning) }

    var qualityInfo by remember(currentMedia.id, currentMedia.path) { mutableStateOf<com.auraplayer.data.repository.AudioQualityInfo?>(null) }
    LaunchedEffect(currentMedia.id, currentMedia.path) {
        qualityInfo = com.auraplayer.data.repository.AudioQualityAnalyzer.analyze(currentMedia, context)
    }

    var dynamicArtworkColor by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(currentMedia.artworkUri, currentMedia.id) {
        val uri = currentMedia.artworkUri
        if (uri != null) {
            withContext(Dispatchers.IO) {
                try {
                    val loader = coil.Coil.imageLoader(context)
                    val req = coil.request.ImageRequest.Builder(context)
                        .data(uri)
                        .allowHardware(false)
                        .build()
                    val result = (loader.execute(req) as? coil.request.SuccessResult)?.drawable
                    val bmp = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        ?: com.auraplayer.util.AudioArtworkResolver.resolveBitmap(context, uri, targetSize = 256)
                    if (bmp != null) {
                        val color = DynamicPaletteManager.extractAccentColor(bmp)
                        withContext(Dispatchers.Main) {
                            dynamicArtworkColor = color
                        }
                    }
                } catch (e: Throwable) {
                    AppLog.d(TAG, "No se pudo extraer el color de acento de la carátula", e)
                }
            }
        } else {
            dynamicArtworkColor = null
        }
    }

    BackHandler {
        if (showPartyLinkDialog) {
            showPartyLinkDialog = false
        } else if (showZapDialog) {
            showZapDialog = false
        } else if (showRelaxAmbienceDialog) {
            showRelaxAmbienceDialog = false
        } else if (showQuickEqDialog) {
            showQuickEqDialog = false
        } else if (showFullscreenVisualizer) {
            showFullscreenVisualizer = false
        } else if (showStoryCardDialog) {
            showStoryCardDialog = false
        } else if (showPitchSpeedDialog) {
            showPitchSpeedDialog = false
        } else if (showStickerDialog) {
            showStickerDialog = false
        } else if (showCutterDialog) {
            showCutterDialog = false
        } else if (showQueueSheet) {
            showQueueSheet = false
        } else if (showMoreToolsSheet) {
            showMoreToolsSheet = false
        } else if (showAudioSpecSheet) {
            showAudioSpecSheet = false
        } else if (showLyricsView) {
            showLyricsView = false
        } else if (showFxDialog) {
            showFxDialog = false
        } else if (showDeleteConfirmDialog) {
            showDeleteConfirmDialog = false
        } else {
            onDismiss()
        }
    }

    val activeAccent = dynamicArtworkColor ?: MaterialTheme.colorScheme.primary

    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    // Dynamic Atmospheric Gradient Background (100% Opaque, zero bleed-through)
    val dynamicBg = remember(currentMedia.id, dynamicArtworkColor, themePrimary, themeSecondary) {
        val baseColor = dynamicArtworkColor
        if (baseColor != null) {
            val topColor = Color(
                red = (baseColor.red * 0.45f + 0.05f).coerceIn(0f, 1f),
                green = (baseColor.green * 0.45f + 0.06f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 0.45f + 0.10f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
            listOf(
                topColor,
                Color(0xFF101524),
                Color(0xFF070A12)
            )
        } else {
            val topColor = Color(
                red = (themePrimary.red * 0.35f + 0.04f).coerceIn(0f, 1f),
                green = (themePrimary.green * 0.35f + 0.05f).coerceIn(0f, 1f),
                blue = (themePrimary.blue * 0.35f + 0.08f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
            val midColor = Color(
                red = (themeSecondary.red * 0.20f + 0.03f).coerceIn(0f, 1f),
                green = (themeSecondary.green * 0.20f + 0.04f).coerceIn(0f, 1f),
                blue = (themeSecondary.blue * 0.20f + 0.07f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
            listOf(topColor, midColor, Color(0xFF070A12))
        }
    }

    val rootBackgroundBrush = remember(artworkPalette, dynamicBg) {
        val palette = artworkPalette
        if (palette != null) {
            Brush.verticalGradient(
                listOf(
                    palette.seed.copy(alpha = 0.35f),
                    Color(0xFF0B0E1A),
                    palette.accent.copy(alpha = 0.18f)
                )
            )
        } else {
            Brush.verticalGradient(dynamicBg)
        }
    }

    // 3D Parallax Tilt state on Center Artwork
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    // On-Screen HUD for Gestures (Volume & Brightness)
    var hudText by remember { mutableStateOf("") }
    var hudIcon by remember { mutableStateOf(Icons.Default.VolumeUp) }
    var hudPercent by remember { mutableFloatStateOf(0f) }
    var isHudVisible by remember { mutableStateOf(false) }
    var isPocketLocked by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val shuffleRotation = remember { Animatable(0f) }
    val repeatBounce = remember { Animatable(1f) }
    val swipeOffsetX = remember { Animatable(0f) }
    val prevNudge = remember { Animatable(0f) }
    val nextNudge = remember { Animatable(0f) }
    val artPlayingScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.93f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "artPlayingScale"
    )

    var supernovaTrigger by remember { mutableStateOf(0L) }

    // Hardware-accelerated continuous vinyl rotation (RenderThread / GPU execution with zero Compose recomposition)
    val vinylRotation = remember { Animatable(0f) }

    LaunchedEffect(currentMedia.id) {
        supernovaTrigger = System.currentTimeMillis()
        launch {
            vinylRotation.animateTo(
                targetValue = vinylRotation.value + 720f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                vinylRotation.animateTo(
                    targetValue = vinylRotation.value + 360f,
                    animationSpec = tween(12000, easing = LinearEasing)
                )
            }
        }
    }

    // Anti-Gravity Levitation Oscillation (RenderThread zero-recomposition animation)
    val infiniteTransition = rememberInfiniteTransition(label = "player_infinite")
    val levitationPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3400 else 6000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "levitationPhase"
    )
    val levitationModifier = Modifier.graphicsLayer {
        translationY = kotlin.math.sin(levitationPhase.value) * 6.dp.toPx()
    }

    val audioBadgeText = remember(currentMedia.path) {
        val ext = File(currentMedia.path).extension.lowercase()
        when (ext) {
            "flac" -> "FLAC • 24-BIT / 96kHz LOSSLESS"
            "wav" -> "WAV • 1411 KBPS LOSSLESS"
            "ape" -> "APE • MONKEY'S AUDIO"
            "dsf", "dff" -> "DSD • DIRECT STREAM DIGITAL"
            "m4a", "alac" -> "ALAC / M4A • LOSSLESS"
            "aac" -> "AAC • 256 KBPS VBR"
            "ogg", "opus" -> "OPUS • HI-RES STEREO"
            else -> "MP3 • 320 KBPS HQ AUDIO"
        }
    }

    LaunchedEffect(isHudVisible) {
        if (isHudVisible) {
            delay(1200)
            isHudVisible = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isPocketLocked) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (isPocketLocked) return@detectVerticalDragGestures
                        val isLeftSide = change.position.x < size.width / 2f
                        if (isLeftSide) {
                            // Brightness Gesture (Left side)
                            val activity = context.findActivity()
                            activity?.let { act ->
                                val window = act.window
                                val params = window.attributes
                                val currentBrightness = if (params.screenBrightness < 0) {
                                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f
                                } else params.screenBrightness

                                val delta = -dragAmount / 400f
                                val newBrightness = (currentBrightness + delta).coerceIn(0.05f, 1.0f)
                                params.screenBrightness = newBrightness
                                window.attributes = params

                                hudIcon = Icons.Default.Brightness6
                                hudPercent = newBrightness
                                hudText = "Brillo: ${(newBrightness * 100).toInt()}%"
                                AuraHaptic.tick(view)
                                isHudVisible = true
                            }
                        } else {
                            // Volume Gesture (Right side)
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val delta = if (dragAmount < -12) 1 else if (dragAmount > 12) -1 else 0
                            if (delta != 0) {
                                val newVol = (currentVol + delta).coerceIn(0, maxVol)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                val pct = (newVol.toFloat() / maxVol.toFloat())
                                hudIcon = Icons.Default.VolumeUp
                                hudPercent = pct
                                hudText = "Volumen: ${(pct * 100).toInt()}%"
                                AuraHaptic.tick(view)
                                isHudVisible = true
                            }
                        }
                    }
                )
            },
        color = Color(0xFF070A12)
    ) {
        val comfortBackground = when (eyeComfortMode) {
            1 -> Brush.verticalGradient(listOf(Color(0xFF191108), Color(0xFF0D0804)))
            2 -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF000000)))
            else -> if (isMysteriousTheme || centerVisualizerMode == 7) {
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF180A2E),
                        Color(0xFF0C0418),
                        Color(0xFF040108)
                    ),
                    radius = 1600f
                )
            } else {
                rootBackgroundBrush
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(comfortBackground)
        ) {
            if (eyeComfortMode == 0) {
                // High-Performance Blurred Artwork Background (Cached Static RenderNode a 120 FPS)
                if (currentMedia.artworkUri != null) {
                    val blurredBgRequest = remember(currentMedia.artworkUri) {
                        coil.request.ImageRequest.Builder(context)
                            .data(currentMedia.artworkUri)
                            .size(96, 96)
                            .precision(coil.size.Precision.EXACT)
                            .allowHardware(true)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .crossfade(false)
                            .build()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.06f
                                scaleY = 1.06f
                            }
                    ) {
                        AsyncImage(
                            model = blurredBgRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier.blur(20.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                                .alpha(if (isComfortView) 0.38f else 0.55f)
                        )
                    }

                    // Gradient scrim for optimal contrast, readability, and glassmorphism depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.60f),
                                        Color(0xFF060913).copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                }

                // Active Visualizer Overlay (Single active engine to eliminate overdraw and battery drain)
                if (!isComfortView) {
                    if (isMysteriousTheme || centerVisualizerMode == 7) {
                        PhantomMistOverlay(
                            accentColor = activeAccent,
                            secondaryColor = MaterialTheme.colorScheme.secondary,
                            bassEnergy = com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(isPlaying),
                            isPlaying = isPlaying
                        )
                    } else {
                        FluidAmbilightGlow(
                            primaryColor = dynamicBg.firstOrNull() ?: MaterialTheme.colorScheme.primary,
                            secondaryColor = dynamicBg.getOrNull(1) ?: MaterialTheme.colorScheme.secondary,
                            isPlaying = isPlaying
                        )
                    }
                }
            } else if (eyeComfortMode == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x22F59E0B), Color(0x0C78350F), Color.Transparent),
                                radius = 1200f
                            )
                        )
                )
            }

            if (!isComfortView) {
                CanvasLoopsOverlay(theme = currentCanvasLoop)
                if (isEdgeLightingEnabled) {
                    EdgeLighting(isPlaying = isPlaying)
                }
            }
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val screenHeight = configuration.screenHeightDp.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Swipe-down dismiss handle
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.28f))
                )

                // Header bar (swipe down to dismiss)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount > 20f) {
                                    AuraHaptic.click(view)
                                    onDismiss()
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Minimize & PiP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp).bounceClick()) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimizar",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            AuraHaptic.click(view)
                            triggerPiP()
                        }, modifier = Modifier.size(36.dp).bounceClick()) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Modo PiP Cápsula Flotante",
                                modifier = Modifier.size(19.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Center: DaVE PLAYER & Clean Core Pills
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clickable { showAudioSpecSheet = true }
                                .bounceClick()
                        ) {
                            Text(
                                text = "DaVE PLAYER",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (eyeComfortMode) {
                                    1 -> Color(0xFFF59E0B)
                                    2 -> Color(0xFFE2E8F0)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                letterSpacing = 1.8.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (isPlaying) {
                                Spacer(modifier = Modifier.width(5.dp))
                                val bar1 by infiniteTransition.animateFloat(
                                    initialValue = 0.25f,
                                    targetValue = 0.95f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(420, easing = FastOutSlowInEasing),
                                        repeatMode = AnimRepeatMode.Reverse
                                    ),
                                    label = "eqBar1"
                                )
                                val bar2 by infiniteTransition.animateFloat(
                                    initialValue = 0.85f,
                                    targetValue = 0.30f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(310, easing = FastOutSlowInEasing),
                                        repeatMode = AnimRepeatMode.Reverse
                                    ),
                                    label = "eqBar2"
                                )
                                val bar3 by infiniteTransition.animateFloat(
                                    initialValue = 0.40f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(520, easing = FastOutSlowInEasing),
                                        repeatMode = AnimRepeatMode.Reverse
                                    ),
                                    label = "eqBar3"
                                )
                                val eqColor = when {
                                    isMysteriousTheme || centerVisualizerMode == 7 -> Color(0xFF00E5FF)
                                    eyeComfortMode == 1 -> Color(0xFFF59E0B)
                                    eyeComfortMode == 2 -> Color(0xFFE2E8F0)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Canvas(
                                    modifier = Modifier.size(width = 12.dp, height = 11.dp)
                                ) {
                                    val barW = 2.5.dp.toPx()
                                    val space = 2.dp.toPx()
                                    val r = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                                    val h1 = (bar1 * size.height).coerceAtLeast(2.dp.toPx())
                                    val h2 = (bar2 * size.height).coerceAtLeast(2.dp.toPx())
                                    val h3 = (bar3 * size.height).coerceAtLeast(2.dp.toPx())
                                    drawRoundRect(eqColor, Offset(0f, size.height - h1), Size(barW, h1), r)
                                    drawRoundRect(eqColor, Offset(barW + space, size.height - h2), Size(barW, h2), r)
                                    drawRoundRect(eqColor, Offset((barW + space) * 2f, size.height - h3), Size(barW, h3), r)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            // Pill 1: Vista Cómoda vs Pro Switcher
                            Surface(
                                onClick = {
                                    AuraHaptic.click(view)
                                    setComfortView(!isComfortView)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isComfortView) Color(0xFF10B981).copy(alpha = 0.20f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, if (isComfortView) Color(0xFF10B981).copy(alpha = 0.60f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text(
                                    text = if (isComfortView) "🌿 Cómoda" else "🎛️ Pro",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isComfortView) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            // Pill 2: Selector de Skin (Carátula / Vinilo / Cassette / CD...)
                            val currentSkinLabel = when (centerVisualizerMode) {
                                0 -> "🖼️ Carátula"
                                1 -> "💿 Vinilo"
                                2 -> "📼 Cassette"
                                3 -> "💿 CD"
                                4 -> "⚡ Espectro"
                                5 -> "📱 iPod"
                                6 -> "🎛️ Vúmetros"
                                else -> "🗿 Monolito"
                            }
                            Surface(
                                onClick = {
                                    AuraHaptic.click(view)
                                    cycleVisualizerSkin()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text(
                                    text = currentSkinLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            // Pill 3: Modo Arcano & Misterioso Enigma
                            Surface(
                                onClick = {
                                    AuraHaptic.click(view)
                                    showMysteriousThemeDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMysteriousTheme) Color(0xFF7C3AED).copy(alpha = 0.28f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isMysteriousTheme) Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF00E5FF)))
                                    else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.40f), Color(0xFFA855F7).copy(alpha = 0.30f)))
                                ),
                                modifier = Modifier.bounceClick()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isMysteriousTheme) "🔮 Arcano: ON" else "🔮 Enigma",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMysteriousTheme) Color(0xFFE9D5FF) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Right: Unified Action Buttons (Favorito, Letras, Cola, Más herramientas)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // 1. Favorito
                        IconButton(onClick = {
                            AuraHaptic.heavy(view)
                            onToggleFavorite()
                            showHeartBurst = true
                        }, modifier = Modifier.size(34.dp).bounceClick()) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. Letras / Visualizador Toggle
                        IconButton(onClick = { showLyricsView = !showLyricsView }, modifier = Modifier.size(34.dp).bounceClick()) {
                            Icon(
                                imageVector = if (showLyricsView) Icons.Default.Album else Icons.Default.Mic,
                                contentDescription = "Letras",
                                tint = if (showLyricsView) Color(0xFFEC4899) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3. Cola de Reproducción
                        IconButton(onClick = { showQueueSheet = true }, modifier = Modifier.size(34.dp).bounceClick()) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Cola de Reproducción",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 4. Más Herramientas (Audífonos, Temporizador, Confort Ocular, Aura, etc.)
                        IconButton(onClick = {
                            AuraHaptic.click(view)
                            showMoreToolsSheet = true
                        }, modifier = Modifier.size(34.dp).bounceClick()) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más herramientas de reproducción",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                // YouTube Music Style Switcher: [ 🎵 Canción | 🎬 Video ] (solo si la pista contiene video o está en modo video)
                val hasVideoContent = remember(currentMedia.id, currentMedia.path, isVideoMode) {
                    isVideoMode || currentMedia.isVideo ||
                        currentMedia.path.endsWith(".mp4", ignoreCase = true) ||
                        currentMedia.path.endsWith(".mkv", ignoreCase = true) ||
                        currentMedia.path.endsWith(".webm", ignoreCase = true)
                }
                if (hasVideoContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 1.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                    Row(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E2840), Color(0xFF0F1424))
                                )
                            )
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🎵 Canción Tab
                        Box(
                            modifier = Modifier
                                .shadow(if (!isVideoMode) 3.dp else 0.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (!isVideoMode) {
                                        Modifier.background(Brush.verticalGradient(listOf(activeAccent, activeAccent.copy(alpha = 0.7f))))
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable {
                                    AuraHaptic.click(view)
                                    isVideoMode = false
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (!isVideoMode) Color.White else Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Canción",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isVideoMode) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (!isVideoMode) Color.White else Color.Gray
                                )
                            }
                        }

                        // 🎬 Video Tab
                        Box(
                            modifier = Modifier
                                .shadow(if (isVideoMode) 3.dp else 0.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isVideoMode) {
                                        Modifier.background(Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))))
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable {
                                    AuraHaptic.click(view)
                                    isVideoMode = true
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isVideoMode) Color.White else Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Video",
                                    fontSize = 11.sp,
                                    fontWeight = if (isVideoMode) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isVideoMode) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                }
                }

                // Center Display: Hero Album Art / Vinyl / CD / Retro Skin (Physics Deck Swipe)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .graphicsLayer {
                            translationX = swipeOffsetX.value
                            rotationZ = (swipeOffsetX.value / 35f).coerceIn(-12f, 12f)
                            val factor = (kotlin.math.abs(swipeOffsetX.value) / 1000f).coerceAtMost(0.10f)
                            scaleX = (1f - factor) * artPlayingScale
                            scaleY = (1f - factor) * artPlayingScale
                        }
                        .pointerInput(isPocketLocked) {
                            if (isPocketLocked) return@pointerInput
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (isPocketLocked) return@detectHorizontalDragGestures
                                    val currentX = swipeOffsetX.value
                                    if (currentX < -80f) {
                                        AuraHaptic.heavy(view)
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(-600f, tween(160, easing = LinearEasing))
                                            onNextClick()
                                            swipeOffsetX.snapTo(600f)
                                            swipeOffsetX.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow))
                                        }
                                    } else if (currentX > 80f) {
                                        AuraHaptic.heavy(view)
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(600f, tween(160, easing = LinearEasing))
                                            onPreviousClick()
                                            swipeOffsetX.snapTo(-600f)
                                            swipeOffsetX.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow))
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(0f, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow))
                                        }
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    if (isPocketLocked) return@detectHorizontalDragGestures
                                    change.consume()
                                    coroutineScope.launch {
                                        swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val artDimension = minOf(maxWidth * 0.85f, maxHeight * 0.95f)
                    val maxArtSize = artDimension.coerceAtLeast(100.dp)
                    // Ancho real de la carátula en píxeles: las zonas de doble toque (retroceso/favorito/avance)
                    // deben calcularse sobre el tamaño medido, no sobre valores fijos ni sobre Dp crudos.
                    val artWidthPx = with(LocalDensity.current) { maxArtSize.toPx() }
                    if (isVideoMode) {
                        // Video Player Screen
                        val activeExo = PlaybackService.activePlayer
                        val isVideoSource = currentMedia.isVideo ||
                            currentMedia.path.endsWith(".mp4", ignoreCase = true) ||
                            currentMedia.path.endsWith(".mkv", ignoreCase = true) ||
                            currentMedia.path.endsWith(".webm", ignoreCase = true)
                        val hasVideoTrack = activeExo != null && (isVideoSource || activeExo.currentTracks.groups.any { it.type == C.TRACK_TYPE_VIDEO })

                        Box(
                            modifier = Modifier
                                .size(maxArtSize)
                                .then(levitationModifier)
                                .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = activeAccent.copy(alpha = 0.55f))
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeExo != null && hasVideoTrack) {
                                AndroidView(
                                    factory = { ctx ->
                                        PlayerView(ctx).apply {
                                            player = activeExo
                                            useController = false
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            keepScreenOn = true
                                        }
                                    },
                                    update = { pv ->
                                        pv.player = activeExo
                                        pv.keepScreenOn = true
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(26.dp))
                                )

                                // Floating Fullscreen Expand Button
                                IconButton(
                                    onClick = {
                                        AuraHaptic.click(view)
                                        onOpenFullScreenVideo(currentMedia)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Pantalla Completa",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = activeAccent,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Sin videoclip",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            AuraHaptic.click(view)
                                            onDownloadVideoForSong(currentMedia)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Descargar Video MP4", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else if (!showLyricsView) {
                        AnimatedContent(
                            targetState = currentMedia.id,
                            transitionSpec = {
                                (slideInHorizontally(
                                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 350f)
                                ) { width -> width / 10 } + fadeIn(tween(220)) + scaleIn(initialScale = 0.94f))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = spring(dampingRatio = 0.76f, stiffness = 350f)
                                    ) { width -> -width / 10 } + fadeOut(tween(180)) + scaleOut(targetScale = 0.94f)
                                )
                            },
                            label = "visualizer_song_transition"
                        ) { _ ->
                            // Audiophile Stage: Dynamic Ambient Bloom multi-layer halo
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlaying) {
                                    val bloomTransition = rememberInfiniteTransition(label = "ambient_stage_bloom")
                                    val bloomPulse = bloomTransition.animateFloat(
                                        initialValue = 0.94f,
                                        targetValue = 1.06f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                            repeatMode = AnimRepeatMode.Reverse
                                        ),
                                        label = "stage_bloom_pulse"
                                    )
                                    val outerBloomBrush = remember(activeAccent) {
                                        androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                activeAccent,
                                                Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        )
                                    }
                                    val coreBloomBrush = remember(activeAccent) {
                                        androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                activeAccent,
                                                Color(0xFF38BDF8).copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    }

                                    // Outer atmospheric diffusion halo (120 FPS GPU RenderNode - zero recomposition)
                                    Box(
                                        modifier = Modifier
                                            .size(maxArtSize)
                                            .graphicsLayer {
                                                val liveBass = com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true)
                                                val bloomScale = (0.95f + (liveBass * 0.22f)) * bloomPulse.value
                                                val bloomAlpha = (0.35f + (liveBass * 0.45f)).coerceIn(0.2f, 0.88f)
                                                scaleX = bloomScale * 1.15f
                                                scaleY = bloomScale * 1.15f
                                                alpha = (bloomAlpha * 0.45f).coerceIn(0f, 1f)
                                            }
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(outerBloomBrush)
                                    )
                                    // Core glowing aura (120 FPS GPU RenderNode - zero recomposition)
                                    Box(
                                        modifier = Modifier
                                            .size(maxArtSize)
                                            .graphicsLayer {
                                                val liveBass = com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true)
                                                val bloomScale = (0.95f + (liveBass * 0.22f)) * bloomPulse.value
                                                val bloomAlpha = (0.35f + (liveBass * 0.45f)).coerceIn(0.2f, 0.88f)
                                                scaleX = bloomScale
                                                scaleY = bloomScale
                                                alpha = (bloomAlpha * 0.65f).coerceIn(0f, 1f)
                                            }
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(coreBloomBrush)
                                    )
                                }

                                if (isMysteriousTheme || centerVisualizerMode == 7) {
                                    // Corona de Plasma Líquido 360° reactiva a bandas FFT en tiempo real
                                    NeonPlasmaCrown(
                                        isPlaying = isPlaying,
                                        modifier = Modifier.size(maxArtSize * 1.14f),
                                        primaryColor = Color(0xFFB388FF),
                                        secondaryColor = Color(0xFF00E5FF)
                                    )
                                } else if (isPlaying) {
                                    // Anillos de pulso acústico en modo estándar
                                    AuraRippleRings(
                                        isPlaying = isPlaying,
                                        modifier = Modifier.size(maxArtSize * 1.12f),
                                        accentColor = activeAccent
                                    )
                                }

                                if (centerVisualizerMode == 0) {
                                    // Skin Carátula Cuadrada Completa (Full Square Album Art Hi-Fi)
                                    Box(
                                        modifier = Modifier
                                            .size(maxArtSize)
                                            .graphicsLayer {
                                                val (gPitch, gRoll) = if (isGyro3dEnabled) gyroTiltState.value else Pair(0f, 0f)
                                                rotationX = (animatedTiltX + gPitch).coerceIn(-18f, 18f)
                                                rotationY = (animatedTiltY + gRoll).coerceIn(-18f, 18f)
                                                cameraDistance = 14f * density
                                            }
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = {},
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        tiltX = (tiltX - dragAmount.y * 0.08f).coerceIn(-14f, 14f)
                                                        tiltY = (tiltY + dragAmount.x * 0.08f).coerceIn(-14f, 14f)
                                                    },
                                                    onDragEnd = {
                                                        tiltX = 0f
                                                        tiltY = 0f
                                                    },
                                                    onDragCancel = {
                                                        tiltX = 0f
                                                        tiltY = 0f
                                                    }
                                                )
                                            }
                                            .then(levitationModifier)
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Halo ambiental cuadrado (120 FPS GPU RenderNode - zero recomposition)
                                        Box(
                                            modifier = Modifier
                                                .size(maxArtSize * 1.06f)
                                                .graphicsLayer {
                                                    val liveBass = if (isPlaying) com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true) else 0.05f
                                                    val glowAlpha = (0.35f + (liveBass * 0.55f)).coerceIn(0.2f, 0.95f)
                                                    alpha = glowAlpha * 0.70f
                                                }
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            artworkPalette?.accent ?: activeAccent,
                                                            (artworkPalette?.seed ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.40f),
                                                            Color.Transparent
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(28.dp)
                                                )
                                        )

                                        SupernovaShockwave(
                                            trigger = supernovaTrigger,
                                            accentColor = activeAccent,
                                            secondaryColor = MaterialTheme.colorScheme.secondary
                                        )

                                        SquareAlbumArtSkin(
                                            song = currentMedia,
                                            isPlaying = isPlaying,
                                            accentColor = activeAccent,
                                            onTap = {
                                                AuraHaptic.click(view)
                                                cycleVisualizerSkin()
                                            },
                                            onLongPress = {
                                                Toast.makeText(context, "🖼️ Carátula Cuadrada Completa HD", Toast.LENGTH_SHORT).show()
                                            },
                                            onDoubleTap = { offset ->
                                                val w = artWidthPx
                                                if (offset.x < w * 0.35f) {
                                                    onSeekRelative(-10000L)
                                                    seekBadgeText = "-10s"
                                                    seekBadgeIsForward = false
                                                    showSeekBadge = true
                                                    AuraHaptic.click(view)
                                                } else if (offset.x > w * 0.65f) {
                                                    onSeekRelative(10000L)
                                                    seekBadgeText = "+10s"
                                                    seekBadgeIsForward = true
                                                    showSeekBadge = true
                                                    AuraHaptic.click(view)
                                                } else {
                                                    onToggleFavorite()
                                                    showHeartBurst = true
                                                    AuraHaptic.heavy(view)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Capa Holográfica Iridiscente reactiva al giro 3D del teléfono
                                        if (isGyro3dEnabled) {
                                            Canvas(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(26.dp))
                                                    .graphicsLayer { alpha = 0.35f }
                                            ) {
                                                val (gPitch, gRoll) = gyroTiltState.value
                                                val cx = size.width * (0.5f + ((animatedTiltY + gRoll) / 32f))
                                                val cy = size.height * (0.5f - ((animatedTiltX + gPitch) / 32f))
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = 0.38f),
                                                            Color(0xFF00F0FF).copy(alpha = 0.28f),
                                                            Color(0xFFEC4899).copy(alpha = 0.22f),
                                                            Color(0xFFFFD700).copy(alpha = 0.18f),
                                                            Color.Transparent
                                                        ),
                                                        center = Offset(cx, cy),
                                                        radius = size.width * 0.65f
                                                    ),
                                                    radius = size.width * 0.5f
                                                )
                                            }
                                        }
                                    }
                                } else if (centerVisualizerMode == 1) {
                                    // Glowing Ambient Aura Disc with Vinyl Record Grooves (Dynamic Palette Glow)
                                     Box(
                                        modifier = Modifier
                                            .size(maxArtSize)
                                             .graphicsLayer {
                                                 val (gPitch, gRoll) = if (isGyro3dEnabled) gyroTiltState.value else Pair(0f, 0f)
                                                 rotationX = (animatedTiltX + gPitch).coerceIn(-18f, 18f)
                                                 rotationY = (animatedTiltY + gRoll).coerceIn(-18f, 18f)
                                                 cameraDistance = 14f * density
                                             }
                                             .pointerInput(Unit) {
                                                 detectDragGestures(
                                                     onDragStart = {},
                                                     onDrag = { change, dragAmount ->
                                                         change.consume()
                                                         tiltX = (tiltX - dragAmount.y * 0.08f).coerceIn(-14f, 14f)
                                                         tiltY = (tiltY + dragAmount.x * 0.08f).coerceIn(-14f, 14f)
                                                     },
                                                     onDragEnd = {
                                                         tiltX = 0f
                                                         tiltY = 0f
                                                     },
                                                     onDragCancel = {
                                                         tiltX = 0f
                                                         tiltY = 0f
                                                     }
                                                 )
                                             }
                                             .then(levitationModifier)
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Ambient dynamic aura glow (Optimizado a 120 FPS sin reasignacion de shaders)
                                        Box(
                                            modifier = Modifier
                                                .size(maxArtSize * 1.08f)
                                                .graphicsLayer {
                                                    val liveBass = if (isPlaying) com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true) else 0.05f
                                                    val glowAlpha = (0.35f + (liveBass * 0.55f)).coerceIn(0.2f, 0.95f)
                                                    alpha = glowAlpha * 0.70f
                                                }
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            artworkPalette?.accent ?: activeAccent,
                                                            (artworkPalette?.seed ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.40f),
                                                            Color.Transparent
                                                        )
                                                    ),
                                                    shape = CircleShape
                                                )
                                        )
                                        SupernovaShockwave(
                                            trigger = supernovaTrigger,
                                            accentColor = activeAccent,
                                            secondaryColor = MaterialTheme.colorScheme.secondary
                                        )

                                        VinylTurntableSkin(
                                        song = currentMedia,
                                        isPlaying = isPlaying,
                                        vinylRotationState = { vinylRotation.value },
                                        accentColor = activeAccent,
                                        onSeekRelative = onSeekRelative,
                                        onTap = {
                                            AuraHaptic.click(view)
                                            cycleVisualizerSkin()
                                        },
                                        onLongPress = {
                                            Toast.makeText(context, "🔮 Resonancia Arcana: Ecos del Vacío Estelar", Toast.LENGTH_SHORT).show()
                                            showMysteriousThemeDialog = true
                                        },
                                        onDoubleTap = { offset ->
                                            val w = artWidthPx
                                            if (offset.x < w * 0.35f) {
                                                onSeekRelative(-10000L)
                                                seekBadgeText = "-10s"
                                                seekBadgeIsForward = false
                                                showSeekBadge = true
                                                AuraHaptic.click(view)
                                            } else if (offset.x > w * 0.65f) {
                                                onSeekRelative(10000L)
                                                seekBadgeText = "+10s"
                                                seekBadgeIsForward = true
                                                showSeekBadge = true
                                                AuraHaptic.click(view)
                                            } else {
                                                onToggleFavorite()
                                                showHeartBurst = true
                                                AuraHaptic.heavy(view)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Capa Holográfica Iridiscente reactiva al giro 3D del teléfono
                                    if (isGyro3dEnabled) {
                                        Canvas(
                                            modifier = Modifier
                                                .size(maxArtSize * 0.86f)
                                                .clip(CircleShape)
                                                .graphicsLayer { alpha = 0.40f }
                                        ) {
                                            val (gPitch, gRoll) = gyroTiltState.value
                                            val cx = size.width * (0.5f + ((animatedTiltY + gRoll) / 32f))
                                            val cy = size.height * (0.5f - ((animatedTiltX + gPitch) / 32f))
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.38f),
                                                        Color(0xFF00F0FF).copy(alpha = 0.28f),
                                                        Color(0xFFEC4899).copy(alpha = 0.22f),
                                                        Color(0xFFFFD700).copy(alpha = 0.18f),
                                                        Color.Transparent
                                                    ),
                                                    center = Offset(cx, cy),
                                                    radius = size.width * 0.65f
                                                ),
                                                radius = size.width * 0.5f
                                            )
                                        }
                                    }
                                }
                            } else if (centerVisualizerMode == 2) {
                                // Skin Retro Vintage: Cassette Tape Interactivo
                                CassetteTapeSkin(
                                    song = currentMedia,
                                    isPlaying = isPlaying,
                                    currentPositionMs = currentPositionMs,
                                    durationMs = durationMs,
                                    onTogglePlay = onPlayPauseClick,
                                    modifier = Modifier
                                        .width(maxArtSize)
                                        .height(maxArtSize * 0.65f)
                                        .then(levitationModifier)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    AuraHaptic.click(view)
                                                    cycleVisualizerSkin()
                                                }
                                            )
                                        }
                                )
                            } else if (centerVisualizerMode == 3) {
                                // Skin Retro 2000s: Discman CD Player con Holograma y LCD
                                RetroDiscmanSkin(
                                    song = currentMedia,
                                    isPlaying = isPlaying,
                                    currentPositionMs = currentPositionMs,
                                    durationMs = durationMs,
                                    onTap = {
                                        AuraHaptic.click(view)
                                        cycleVisualizerSkin()
                                    },
                                    onDoubleTap = { offset ->
                                        val w = artWidthPx
                                        if (offset.x < w * 0.35f) {
                                            onSeekRelative(-10000L)
                                            seekBadgeText = "-10s"
                                            seekBadgeIsForward = false
                                            showSeekBadge = true
                                            AuraHaptic.click(view)
                                        } else if (offset.x > w * 0.65f) {
                                            onSeekRelative(10000L)
                                            seekBadgeText = "+10s"
                                            seekBadgeIsForward = true
                                            showSeekBadge = true
                                            AuraHaptic.click(view)
                                        } else {
                                            onToggleFavorite()
                                            showHeartBurst = true
                                            AuraHaptic.heavy(view)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(maxArtSize)
                                        .then(levitationModifier)
                                )
                            } else if (centerVisualizerMode == 4) {
                                // Live Cyber Spectrum Waveform Visualizer (Real-time FFT audio engine)
                                Box(
                                    modifier = Modifier
                                        .size(maxArtSize)
                                        .then(levitationModifier)
                                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF00F0FF).copy(alpha = 0.5f))
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF131A2E), Color(0xFF090D18))
                                            )
                                        )
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    AuraHaptic.click(view)
                                                    cycleVisualizerSkin()
                                                },
                                                onDoubleTap = { offset ->
                                                    val w = size.width
                                                    if (offset.x < w * 0.35f) {
                                                        onSeekRelative(-10000L)
                                                        seekBadgeText = "-10s"
                                                        seekBadgeIsForward = false
                                                        showSeekBadge = true
                                                        AuraHaptic.click(view)
                                                    } else if (offset.x > w * 0.65f) {
                                                        onSeekRelative(10000L)
                                                        seekBadgeText = "+10s"
                                                        seekBadgeIsForward = true
                                                        showSeekBadge = true
                                                        AuraHaptic.click(view)
                                                    } else {
                                                        onToggleFavorite()
                                                        showHeartBurst = true
                                                        AuraHaptic.heavy(view)
                                                    }
                                                }
                                            )
                                        }
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val numBars = 20
                                        val spacing = size.width / numBars
                                        val barWidth = spacing * 0.65f
                                        val maxHeight = size.height * 0.72f
                                        val midY = size.height / 2f

                                        for (i in 0 until numBars) {
                                            val wave = com.auraplayer.audio.RealtimeVisualizerManager.instance.getBand(i, numBars, isPlaying)
                                            val barHeight = (maxHeight * wave).coerceIn(6f, maxHeight)
                                            val x = i * spacing + (spacing - barWidth) / 2f
                                            val y = midY - (barHeight / 2f)

                                            drawRoundRect(
                                                brush = Brush.verticalGradient(
                                                    listOf(Color(0xFFFF0055), Color(0xFF8B5CF6), Color(0xFF00F0FF)),
                                                    startY = y,
                                                    endY = y + barHeight
                                                ),
                                                topLeft = Offset(x, y),
                                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                            )
                                        }
                                    }

                                    Text(
                                        text = "⚡ ESPECTRO CYBER EN VIVO (Toca para iPod Classic)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00F0FF).copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                                    )
                                }
                            } else if (centerVisualizerMode == 5) {
                                // Skin Retro iPod Classic con Click Wheel Háptica
                                RetroIpodSkin(
                                    song = currentMedia,
                                    isPlaying = isPlaying,
                                    currentPositionMs = currentPositionMs,
                                    durationMs = durationMs,
                                    onTap = {
                                        AuraHaptic.click(view)
                                        cycleVisualizerSkin()
                                    },
                                    onTogglePlay = onPlayPauseClick,
                                    onNext = onNextClick,
                                    onPrev = onPreviousClick,
                                    onSeekRelative = onSeekRelative,
                                    modifier = Modifier
                                        .size(maxArtSize)
                                        .then(levitationModifier)
                                )
                            } else if (centerVisualizerMode == 6) {
                                // Skin Vintage Hi-Fi: Vúmetros Analógicos de Aguja Mecánica (Canal L & R)
                                AnalogVuMetersSkin(
                                    song = currentMedia,
                                    isPlaying = isPlaying,
                                    bassEnergy = if (isPlaying) com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true) else 0.05f,
                                    onTap = {
                                        AuraHaptic.click(view)
                                        cycleVisualizerSkin()
                                    },
                                    onDoubleTap = { offset ->
                                        val w = artWidthPx
                                        if (offset.x < w * 0.35f) {
                                            onSeekRelative(-10000L)
                                            seekBadgeText = "-10s"
                                            seekBadgeIsForward = false
                                            showSeekBadge = true
                                            AuraHaptic.click(view)
                                        } else if (offset.x > w * 0.65f) {
                                            onSeekRelative(10000L)
                                            seekBadgeText = "+10s"
                                            seekBadgeIsForward = true
                                            showSeekBadge = true
                                            AuraHaptic.click(view)
                                        } else {
                                            onToggleFavorite()
                                            showHeartBurst = true
                                            AuraHaptic.heavy(view)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(maxArtSize)
                                        .then(levitationModifier)
                                )
                            } else {
                                // Skin Monolito Arcano: Obelisco de Obsidiana Cuántica & Runas
                                Box(
                                    modifier = Modifier
                                        .size(maxArtSize)
                                        .then(levitationModifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SupernovaShockwave(
                                        trigger = supernovaTrigger,
                                        accentColor = Color(0xFF00E5FF),
                                        secondaryColor = Color(0xFFA855F7),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    ArcaneMonolithSkin(
                                        song = currentMedia,
                                        isPlaying = isPlaying,
                                        bassEnergy = if (isPlaying) com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true) else 0.05f,
                                        accentColor = activeAccent,
                                        secondaryColor = MaterialTheme.colorScheme.secondary,
                                        onTap = {
                                            AuraHaptic.click(view)
                                            cycleVisualizerSkin()
                                        },
                                        onLongPress = {
                                            showArcaneFrequencyDialog = true
                                        },
                                        onDoubleTap = { offset ->
                                            val w = artWidthPx
                                            if (offset.x < w * 0.35f) {
                                                onSeekRelative(-10000L)
                                                seekBadgeText = "-10s"
                                                seekBadgeIsForward = false
                                                showSeekBadge = true
                                                AuraHaptic.click(view)
                                            } else if (offset.x > w * 0.65f) {
                                                onSeekRelative(10000L)
                                                seekBadgeText = "+10s"
                                                seekBadgeIsForward = true
                                                showSeekBadge = true
                                                AuraHaptic.click(view)
                                            } else {
                                                onToggleFavorite()
                                                showHeartBurst = true
                                                AuraHaptic.heavy(view)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                } else {
                        // Apple Music-Style Frosted Glass Synchronized & Plain Lyrics View
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = activeAccent.copy(alpha = 0.45f))
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF090E1A).copy(alpha = 0.65f),
                                            Color(0xFF04060C).copy(alpha = 0.78f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.2.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            activeAccent.copy(alpha = 0.50f),
                                            Color.White.copy(alpha = 0.12f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingLyrics) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = activeAccent, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Buscando letras en vivo...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else if (lyrics == null || (lyrics.lines.isEmpty() && lyrics.plainLyrics.isNullOrBlank())) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No se encontraron letras en vivo para esta pista.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Controls for lyrics
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (lyrics.lines.isNotEmpty()) "✨ Sincronizado en Vivo" else "📄 Texto Completo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFEC4899),
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { isPlayerTranslationEnabled = !isPlayerTranslationEnabled },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text(
                                                    text = if (isPlayerTranslationEnabled) "🌐 ES" else "🌐 TR",
                                                    color = if (isPlayerTranslationEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = { lyricsFontSizeMultiplier = (lyricsFontSizeMultiplier - 0.15f).coerceAtLeast(0.7f) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.TextDecrease, contentDescription = "Menor tamaño", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { lyricsFontSizeMultiplier = (lyricsFontSizeMultiplier + 0.15f).coerceAtMost(1.6f) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.TextIncrease, contentDescription = "Mayor tamaño", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    val textToCopy = lyrics.plainLyrics ?: lyrics.lines.joinToString("\n") { it.text }
                                                    clipboardManager.setText(AnnotatedString(textToCopy))
                                                    Toast.makeText(context, "Letras copiadas al portapapeles", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    AuraHaptic.click(view)
                                                    val activeLineText = if (lyrics.lines.isNotEmpty()) {
                                                        val idx = lyrics.lines.indexOfLast { currentPositionMs >= it.timeMs }.coerceAtLeast(0)
                                                        lyrics.lines.getOrNull(idx)?.text ?: ""
                                                    } else {
                                                        lyrics.plainLyrics?.lines()?.firstOrNull { it.isNotBlank() } ?: ""
                                                    }
                                                    storyCardInitialQuote = if (activeLineText.isNotBlank()) "“$activeLineText”" else ""
                                                    showStoryCardDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.FormatQuote,
                                                    contentDescription = "Compartir verso en Story / Estado",
                                                    tint = Color(0xFFEC4899),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (lyrics.lines.isNotEmpty()) {
                                        val listState = rememberLazyListState()
                                        val activeIndex = remember(currentPositionMs, lyrics.lines) {
                                            val idx = lyrics.lines.indexOfLast { currentPositionMs >= it.timeMs }
                                            if (idx >= 0) idx else 0
                                        }

                                        LaunchedEffect(activeIndex) {
                                            if (activeIndex >= 0) {
                                                listState.animateScrollToItem(activeIndex)
                                            }
                                        }

                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(top = 140.dp, bottom = 180.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            itemsIndexed(
                                                items = lyrics.lines,
                                                key = { index, line -> "${line.timeMs}_$index" },
                                                contentType = { _, _ -> "lyric_line" }
                                            ) { index, line ->
                                                val isActive = index == activeIndex
                                                val lyricScale by androidx.compose.animation.core.animateFloatAsState(
                                                    targetValue = if (isActive) 1.05f else 0.95f,
                                                    animationSpec = androidx.compose.animation.core.spring(
                                                        dampingRatio = 0.75f,
                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                                    ),
                                                    label = "lyric_scale"
                                                )
                                                val lyricAlpha by androidx.compose.animation.core.animateFloatAsState(
                                                    targetValue = if (isActive) 1f else 0.38f,
                                                    animationSpec = androidx.compose.animation.core.tween(250),
                                                    label = "lyric_alpha"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .then(
                                                            if (isActive) Modifier.shadow(
                                                                elevation = 12.dp,
                                                                shape = RoundedCornerShape(20.dp),
                                                                spotColor = activeAccent.copy(alpha = 0.55f),
                                                                ambientColor = Color.Black
                                                            ) else Modifier
                                                        )
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                            if (isActive) Brush.verticalGradient(
                                                                listOf(
                                                                    activeAccent.copy(alpha = 0.32f),
                                                                    Color(0x338B5CF6)
                                                                )
                                                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                                        )
                                                        .border(
                                                            width = if (isActive) 1.2.dp else 0.dp,
                                                            brush = if (isActive) Brush.horizontalGradient(
                                                                listOf(
                                                                    activeAccent.copy(alpha = 0.65f),
                                                                    Color(0xFF8B5CF6).copy(alpha = 0.4f)
                                                                )
                                                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                                            shape = RoundedCornerShape(20.dp)
                                                        )
                                                        .combinedClickable(
                                                            onClick = {
                                                                AuraHaptic.click(null)
                                                                onSeek(line.timeMs)
                                                            },
                                                            onLongClick = {
                                                                AuraHaptic.heavyClick(view)
                                                                storyCardInitialQuote = "“${line.text}”"
                                                                showStoryCardDialog = true
                                                            }
                                                        )
                                                        .padding(horizontal = 16.dp, vertical = if (isActive) 14.dp else 7.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        val nextLineTime = lyrics.lines.getOrNull(index + 1)?.timeMs ?: (line.timeMs + 4500L)
                                                        val lineDuration = (nextLineTime - line.timeMs).coerceIn(1200L, 12000L)
                                                        val lineElapsed = (currentPositionMs - line.timeMs).coerceIn(0L, lineDuration)
                                                        val lineProgress = (lineElapsed.toFloat() / lineDuration.toFloat()).coerceIn(0f, 1f)
                                                        val bassPulse = if (isPlaying) com.auraplayer.audio.RealtimeVisualizerManager.instance.getBassEnergy(true) else 0f

                                                        Text(
                                                            text = line.text,
                                                            fontSize = if (isActive) (24 * lyricsFontSizeMultiplier).sp else (15 * lyricsFontSizeMultiplier).sp,
                                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = if (isActive) (32 * lyricsFontSizeMultiplier).sp else (22 * lyricsFontSizeMultiplier).sp,
                                                            style = androidx.compose.ui.text.TextStyle(
                                                                brush = if (isActive) {
                                                                    Brush.horizontalGradient(
                                                                        colorStops = arrayOf(
                                                                            0.0f to Color.White,
                                                                            (lineProgress - 0.08f).coerceAtLeast(0f) to Color.White,
                                                                            lineProgress to Color(0xFFFFD700),
                                                                            (lineProgress + 0.08f).coerceAtMost(1f) to Color.White.copy(alpha = 0.40f),
                                                                            1.0f to Color.White.copy(alpha = 0.40f)
                                                                        )
                                                                    )
                                                                } else {
                                                                    Brush.horizontalGradient(
                                                                        listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.38f))
                                                                    )
                                                                },
                                                                shadow = if (isActive) androidx.compose.ui.graphics.Shadow(
                                                                    color = activeAccent.copy(alpha = 0.90f),
                                                                    blurRadius = 22f + (bassPulse * 16f)
                                                                ) else null
                                                            ),
                                                            modifier = Modifier.graphicsLayer {
                                                                scaleX = lyricScale
                                                                scaleY = lyricScale
                                                                alpha = lyricAlpha
                                                            }
                                                        )
                                                        if (isPlayerTranslationEnabled) {
                                                            val trans = playerLyricsTranslations[line.text]
                                                            if (!trans.isNullOrBlank()) {
                                                                Text(
                                                                    text = trans,
                                                                    fontSize = if (isActive) (15 * lyricsFontSizeMultiplier).sp else (12 * lyricsFontSizeMultiplier).sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = if (isActive) Color(0xFF10B981) else Color(0xFF64748B),
                                                                    textAlign = TextAlign.Center,
                                                                    lineHeight = (18 * lyricsFontSizeMultiplier).sp,
                                                                    modifier = Modifier.padding(top = 2.dp)
                                                                )
                                                            } else if (isPlayerTranslating) {
                                                                Text(
                                                                    text = "Traduciendo...",
                                                                    fontSize = 11.sp,
                                                                    color = Color(0xFF64748B),
                                                                    textAlign = TextAlign.Center,
                                                                    modifier = Modifier.padding(top = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            item {
                                                Text(
                                                    text = lyrics.plainLyrics ?: "",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontSize = (15 * lyricsFontSizeMultiplier).sp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {},
                                                            onLongClick = {
                                                                AuraHaptic.heavyClick(view)
                                                                val firstLine = lyrics.plainLyrics?.lines()?.firstOrNull { it.isNotBlank() } ?: ""
                                                                storyCardInitialQuote = if (firstLine.isNotBlank()) "“$firstLine”" else ""
                                                                showStoryCardDialog = true
                                                            }
                                                        )
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Animated Neon Seek Badge Overlay (+10s / -10s)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showSeekBadge,
                        enter = fadeIn() + scaleIn(initialScale = 0.65f),
                        exit = fadeOut() + scaleOut(targetScale = 1.35f),
                        modifier = Modifier
                            .align(if (seekBadgeIsForward) Alignment.CenterEnd else Alignment.CenterStart)
                            .padding(horizontal = 24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xE6080C16),
                            border = BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    if (seekBadgeIsForward) listOf(Color(0xFF00F0FF), Color(0xFF38BDF8))
                                    else listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                )
                            ),
                            shadowElevation = 16.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (seekBadgeIsForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                                    contentDescription = null,
                                    tint = if (seekBadgeIsForward) Color(0xFF00F0FF) else Color(0xFFEC4899),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = seekBadgeText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Animated Glowing Neon Heart Burst Overlay with Particles
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHeartBurst,
                        enter = fadeIn() + scaleIn(initialScale = 0.35f),
                        exit = fadeOut() + scaleOut(targetScale = 1.6f),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        ExplosiveHeartBurst()
                    }
                }

                // Song Title & Artist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = currentMedia.title,
                        transitionSpec = {
                            (slideInVertically { it / 2 } + fadeIn(tween(260)))
                                .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(180)))
                        },
                        label = "titleAnim"
                    ) { t ->
                        Text(
                            text = t,
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    AnimatedContent(
                        targetState = currentMedia.artist,
                        transitionSpec = {
                            (slideInVertically { it / 2 } + fadeIn(tween(260)))
                                .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(180)))
                        },
                        label = "artistAnim"
                    ) { a ->
                        val artistDisplay = if (isMysteriousTheme || centerVisualizerMode == 7) {
                            "$a • ${currentMedia.album.ifEmpty { "Aura Audio" }} ᛉ"
                        } else {
                            "$a • ${currentMedia.album.ifEmpty { "Aura Audio" }}"
                        }
                        Text(
                            text = artistDisplay,
                            fontSize = 13.sp,
                            color = if (isMysteriousTheme || centerVisualizerMode == 7) Color(0xFFD8B4FE) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    // Audiophile Hi-Fi Quality & Quick DSP Calibrated Acoustic Pills
                    val audioBadge = qualityInfo?.let { "${it.codec} • ${it.bitrateKbps} kbps • ${it.qualityLabel}" }
                        ?: computeAudioFormatBadge(currentMedia.path, currentMedia.size, currentMedia.duration).let { b ->
                            if (b.isNotBlank()) "$b • Hi-Fi Audio" else "Audio HD"
                        }
                    val badgeColor = qualityInfo?.let { Color(it.qualityColor) } ?: MaterialTheme.colorScheme.primary

                    val activePresetIdx = com.auraplayer.audio.EqualizerManager.instance.currentPresetIndex
                    val activeHeadphoneProfile = remember(activePresetIdx) {
                        when (activePresetIdx) {
                            13 -> "Beats Studio"
                            14 -> "Sennheiser HD"
                            15 -> "Party Speaker"
                            8 -> "AirPods Pro"
                            9 -> "Sony WH-1000"
                            10 -> "Galaxy Buds"
                            11 -> "JBL Pure Bass"
                            1 -> "Bass Boost"
                            7 -> "Vocal & Hi-Fi"
                            else -> "AutoEQ DSP"
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    AuraHaptic.click(view)
                                    showAudioQualityDialog = true
                                }
                                .bounceClick(),
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.50f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💎", fontSize = 10.sp)
                                Text(
                                    text = audioBadge,
                                    color = badgeColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        // Calibrated DSP Acoustic Profile Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    AuraHaptic.click(view)
                                    showQuickDspDialog = true
                                }
                                .bounceClick(),
                            color = activeAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(activeAccent.copy(alpha = 0.70f), activeAccent.copy(alpha = 0.25f))
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🎧", fontSize = 10.sp)
                                Text(
                                    text = activeHeadphoneProfile,
                                    color = activeAccent,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text("▾", color = activeAccent, fontSize = 9.sp)
                            }
                        }

                        // Camelot Wheel DJ Harmonic Key Pill
                        val camelotKey = remember(currentMedia.id) {
                            com.auraplayer.audio.CamelotHarmonicDjEngine.detectCamelotKey(currentMedia)
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    AuraHaptic.click(view)
                                    Toast.makeText(context, "🎛️ Tonalidad Armónica DJ: ${camelotKey.code} (${camelotKey.musicalKey})", Toast.LENGTH_SHORT).show()
                                },
                            color = Color(0xFF8B5CF6).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B5CF6).copy(alpha = 0.70f), Color(0xFF00F0FF).copy(alpha = 0.30f))
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🎛️", fontSize = 10.sp)
                                Text(
                                    text = "${camelotKey.code} • ${camelotKey.musicalKey.take(6)}",
                                    color = Color(0xFFC4B5FD),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        // Resonancia Arcana / Frecuencia Solfeggio Pill
                        if (isMysteriousTheme || centerVisualizerMode == 7 || currentArcaneHz != 440) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        AuraHaptic.click(view)
                                        showArcaneFrequencyDialog = true
                                    }
                                    .bounceClick(),
                                color = Color(0xFF7C3AED).copy(alpha = 0.20f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFA855F7).copy(alpha = 0.85f), Color(0xFF00E5FF).copy(alpha = 0.45f))
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔮", fontSize = 10.sp)
                                    Text(
                                        text = "$currentArcaneHz Hz • Armonía Sagrada",
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                }

                if (isMysteriousTheme || centerVisualizerMode == 7) {
                    Spacer(modifier = Modifier.height(2.dp))
                    NecronomicScrollText(
                        accentColor = activeAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AuraHaptic.click(view)
                                showArcaneFrequencyDialog = true
                            }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Seek Bar & Timeline
                Column(modifier = Modifier.fillMaxWidth()) {
                    val sliderValue = if (effectiveDurationMs > 0L) (currentPositionMs.toFloat() / effectiveDurationMs.toFloat()) else 0f

                    DynamicWaveformSeekbar(
                        modifier = Modifier.semantics {
                            contentDescription = "Barra de progreso de reproducción"
                            stateDescription = "${formatTime(currentPositionMs)} de ${formatTime(effectiveDurationMs)}"
                        },
                        progress = sliderValue,
                        isPlaying = isPlaying,
                        onSeek = { percent ->
                            val targetMs = (percent * effectiveDurationMs).toLong()
                            onSeek(targetMs)
                        },
                        activeColor = activeAccent,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        durationMs = effectiveDurationMs,
                        waveform = realWaveform
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dispA = abPointA ?: abLoopA
                        val dispB = abPointB ?: abLoopB
                        val isLoopOn = isAbActive || (isAbLoopActive && dispA != null && dispB != null)
                        if (isLoopOn && dispA != null && dispB != null) {
                            Text(
                                text = "🔁 ${formatTime(dispA)} ➔ ${formatTime(dispB)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        } else if (dispA != null) {
                            Text(
                                text = "🅰️ ${formatTime(dispA)} ➔ [B?]",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        val remainingMs = (effectiveDurationMs - currentPositionMs).coerceAtLeast(0L)
                        Text(
                            text = if (effectiveDurationMs > 0L) "-${formatTime(remainingMs)}" else "--:--",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isComfortView,
                    enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(250)),
                    exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(200))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    // FX Studio, A-B Looper & Audio Processing Controls Row
                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio FX Studio pill
                    val fxLabel = when {
                        playbackSpeed == 0.85f && playbackPitch == 0.85f -> "🌌 SLOWED"
                        playbackSpeed == 1.25f && playbackPitch == 1.25f -> "⚡ NIGHTCORE"
                        playbackSpeed == 0.75f && playbackPitch == 0.75f -> "📼 VAPORWAVE"
                        playbackSpeed == 1.35f && playbackPitch == 1.0f -> "🚀 SPED UP"
                        playbackSpeed == 1.0f && playbackPitch == 1.0f -> "🎵 ORIGINAL"
                        else -> "🎛️ ${playbackSpeed}x"
                    }

                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826))
                                )
                            )
                            .clickable { showFxDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "FX Studio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = fxLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // A-B Looper Pill
                    val isAbLoopSet = isAbLoopActive && abLoopA != null && abLoopB != null
                    val isASetOnly = abLoopA != null && abLoopB == null
                    Box(
                        modifier = Modifier
                            .shadow(if (isAbLoopSet || isASetOnly) 4.dp else 2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isAbLoopSet -> Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                    isASetOnly -> Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
                                    else -> Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826)))
                                }
                            )
                            .clickable {
                                AuraHaptic.click(view)
                                if (abLoopA == null) {
                                    abLoopA = currentPositionMs
                                    abLoopB = null
                                    isAbLoopActive = false
                                    Toast.makeText(context, "Punto [A] marcado en ${formatTime(abLoopA!!)}. Toca de nuevo para marcar [B]", Toast.LENGTH_SHORT).show()
                                } else if (abLoopB == null) {
                                    var targetB = currentPositionMs
                                    if (targetB <= abLoopA!! + 1500L) {
                                        targetB = if (effectiveDurationMs > abLoopA!! + 1500L) {
                                            (abLoopA!! + 3000L).coerceAtMost(effectiveDurationMs)
                                        } else {
                                            abLoopA!! + 3000L
                                        }
                                    }
                                    abLoopB = targetB
                                    isAbLoopActive = true
                                    Toast.makeText(context, "🔁 Bucle A-B activado: ${formatTime(abLoopA!!)} ➔ ${formatTime(abLoopB!!)}", Toast.LENGTH_SHORT).show()
                                } else {
                                    abLoopA = null
                                    abLoopB = null
                                    isAbLoopActive = false
                                    Toast.makeText(context, "Bucle A-B desactivado", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                isAbLoopSet -> "🔂 A-B [${formatTime(abLoopA!!)}-${formatTime(abLoopB!!)}]"
                                isASetOnly -> "🅰️ [${formatTime(abLoopA!!)}] ➔ [B?]"
                                else -> "🔁 BUCLE A-B"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isAbLoopSet || isASetOnly -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 11.sp
                        )
                    }

                    // Night DRC Compressor Pill
                    Box(
                        modifier = Modifier
                            .shadow(if (isNightDrcActive) 4.dp else 2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isNightDrcActive) Brush.verticalGradient(listOf(Color(0xFF818CF8), Color(0xFF4338CA)))
                                else Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826)))
                            )
                            .clickable {
                                isNightDrcActive = !isNightDrcActive
                                tubeAmpManager.isNightDrcEnabled = isNightDrcActive
                                EqualizerManager.instance.setReplayGainEnabled(isNightDrcActive)
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isNightDrcActive) "🌙 DRC NOCHE" else "🌙 DRC",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightDrcActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    // Karaoke Vocal Reducer Pill
                    val gaplessManager = remember { com.auraplayer.audio.GaplessManager.getInstance(context) }
                    var isKaraokeActive by remember { mutableStateOf(gaplessManager.isVocalReducerEnabled) }

                    Box(
                        modifier = Modifier
                            .shadow(if (isKaraokeActive) 4.dp else 2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isKaraokeActive) Brush.verticalGradient(listOf(Color(0xFFE040FB), Color(0xFF8E24AA)))
                                else Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826)))
                            )
                            .clickable {
                                AuraHaptic.click(null)
                                isKaraokeActive = !isKaraokeActive
                                gaplessManager.toggleVocalReducer(isKaraokeActive)
                                com.auraplayer.audio.EqualizerManager.instance.setKaraokeMode(isKaraokeActive)
                                Toast.makeText(context, if (isKaraokeActive) "🎤 Modo Karaoke ACTIVADO (Voz Atenuada)" else "🎤 Modo Karaoke DESACTIVADO", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isKaraokeActive) "🎤 KARAOKE" else "🎙️ VOCAL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isKaraokeActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    // Viral Audio Mode Pill (Slowed+Reverb / Nightcore)
                    Box(
                        modifier = Modifier
                            .shadow(if (currentViralMode != ViralAudioMode.NORMAL) 4.dp else 2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (currentViralMode != ViralAudioMode.NORMAL) Brush.verticalGradient(listOf(Color(0xFF00F5FF), Color(0xFF008B8B)))
                                else Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826)))
                            )
                            .clickable {
                                currentViralMode = viralEffectsManager.cycleMode(MainActivity.activeController)
                                Toast.makeText(context, "Efecto: ${currentViralMode.displayName} (${currentViralMode.tag})", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (currentViralMode) {
                                ViralAudioMode.NORMAL -> "⚡ VIRAL"
                                ViralAudioMode.SLOWED_REVERB -> "🌊 SLOWED"
                                ViralAudioMode.NIGHTCORE -> "⚡ NIGHTCORE"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentViralMode != ViralAudioMode.NORMAL) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    // Synced Lyrics LRC Button
                    Box(
                        modifier = Modifier
                            .shadow(2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826))))
                            .clickable { showSyncedLyricsDialog = true }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎤 LRC EN VIVO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }

                    // Retro iPod Classic Skin Button
                    Box(
                        modifier = Modifier
                            .shadow(2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826))))
                            .clickable { showIpodClassicDialog = true }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🕹️ iPOD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp
                        )
                    }

                    // Canvas Loop Ambient Background Pill
                    Box(
                        modifier = Modifier
                            .shadow(if (currentCanvasLoop != CanvasLoopTheme.OFF) 4.dp else 2.5.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (currentCanvasLoop != CanvasLoopTheme.OFF) Brush.verticalGradient(listOf(Color(0xFFE879F9), Color(0xFF9333EA)))
                                else Brush.verticalGradient(if (isMysteriousTheme) listOf(Color(0xFF190E30), Color(0xFF0A0414)) else listOf(Color(0xFF222B42), Color(0xFF131826)))
                            )
                            .clickable {
                                currentCanvasLoop = when (currentCanvasLoop) {
                                    CanvasLoopTheme.OFF -> CanvasLoopTheme.RAIN
                                    CanvasLoopTheme.RAIN -> CanvasLoopTheme.SYNTHWAVE
                                    CanvasLoopTheme.SYNTHWAVE -> CanvasLoopTheme.STARFIELD
                                    CanvasLoopTheme.STARFIELD -> CanvasLoopTheme.OFF
                                }
                                Toast.makeText(context, "Fondo: ${currentCanvasLoop.label}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (currentCanvasLoop != CanvasLoopTheme.OFF) "🌌 ${currentCanvasLoop.name}" else "🌌 CANVAS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentCanvasLoop != CanvasLoopTheme.OFF) Color(0xFFE879F9) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    // Ringtone Cutter Tool
                    IconButton(
                        onClick = { showCutterDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Cortar Audio / Ringtone",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Floating Lyrics / PiP Mode
                    IconButton(
                        onClick = { triggerPiP() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Letras Flotantes PiP",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Native Share
                    IconButton(
                        onClick = { shareSong() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Rewind 10s
                    IconButton(
                        onClick = { onSeekRelative(-10000L) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Retroceder 10s",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { onSeekRelative(10000L) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Avanzar 10s",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // VIP Pro Suite Quick Action Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frecuencias Arcanas Solfeggio Chip (432Hz / 528Hz)
                    FilterChip(
                        selected = currentArcaneHz != 440,
                        onClick = {
                            AuraHaptic.click(view)
                            showArcaneFrequencyDialog = true
                        },
                        label = {
                            Text(
                                text = if (currentArcaneHz != 440) "🔮 $currentArcaneHz Hz Solfeggio" else "🔮 Frecuencias 432Hz",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7).copy(alpha = 0.28f),
                            selectedLabelColor = Color(0xFFE9D5FF),
                            labelColor = Color(0xFFC084FC)
                        )
                    )

                    // Oráculo Arcano DaVE Chip
                    FilterChip(
                        selected = false,
                        onClick = {
                            AuraHaptic.click(view)
                            showOracleCard = true
                        },
                        label = {
                            Text(
                                text = "👁️ Oráculo Arcano",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF00E5FF)
                        )
                    )

                    // Omitir Silencios Muertos Pro Chip
                    FilterChip(
                        selected = isSilenceTrimmerActive,
                        onClick = {
                            AuraHaptic.click(view)
                            isSilenceTrimmerActive = silenceTrimmerMgr.toggle()
                            val msg = if (isSilenceTrimmerActive) "✂️ Omitir Silencios ACTIVADO (transición continua)" else "Recorte de silencios desactivado"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = if (isSilenceTrimmerActive) "✂️ Sin Silencios: ON" else "✂️ Omitir Silencios",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF06B6D4).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF06B6D4),
                            labelColor = Color(0xFF22D3EE)
                        )
                    )

                    // Normalizador de Volumen (-14 LUFS) Chip
                    FilterChip(
                        selected = isLoudnessNormalizerActive,
                        onClick = {
                            AuraHaptic.click(view)
                            isLoudnessNormalizerActive = loudnessNormMgr.toggle()
                            val msg = if (isLoudnessNormalizerActive) "⚖️ Normalizador ACTIVADO (-14 LUFS uniforme)" else "Normalizador desactivado"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = if (isLoudnessNormalizerActive) "⚖️ Normalizado (-14dB)" else "⚖️ Normalizador",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFA855F7),
                            labelColor = Color(0xFFC084FC)
                        )
                    )

                    // Burbuja Flotante Multitarea Chip
                    FilterChip(
                        selected = isBubbleActive,
                        onClick = {
                            AuraHaptic.click(view)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                Toast.makeText(context, "Concede el permiso para mostrar la burbuja flotante", Toast.LENGTH_LONG).show()
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                val bubbleIntent = Intent(context, FloatingBubbleService::class.java)
                                if (FloatingBubbleService.isRunning) {
                                    context.stopService(bubbleIntent)
                                    isBubbleActive = false
                                    Toast.makeText(context, "Burbuja flotante cerrada", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(bubbleIntent)
                                    } else {
                                        context.startService(bubbleIntent)
                                    }
                                    isBubbleActive = true
                                    Toast.makeText(context, "🏝️ Burbuja Flotante ACTIVA en pantalla", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        label = {
                            Text(
                                text = if (isBubbleActive) "🏝️ Burbuja: ON" else "🏝️ Burbuja Flotante",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF00F0FF),
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Modo ZAP Chip
                    FilterChip(
                        selected = zapManager.isZapActive,
                        onClick = {
                            AuraHaptic.click(view)
                            showZapDialog = true
                        },
                        label = {
                            Text(
                                text = if (zapManager.isZapActive) "⚡ ZAP Activo (${zapManager.previewDurationSeconds}s)" else "⚡ Modo ZAP (15s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEAB308).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFEAB308),
                            labelColor = Color(0xFFFDE047)
                        )
                    )

                    // Silent Disco / Fiesta Wi-Fi Chip
                    FilterChip(
                        selected = false,
                        onClick = {
                            AuraHaptic.click(view)
                            showPartyLinkDialog = true
                        },
                        label = {
                            Text(
                                text = "📻 Fiesta Wi-Fi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEC4899).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFEC4899),
                            labelColor = Color(0xFFF472B6)
                        )
                    )

                    // Bucle A-B Pro Chip
                    FilterChip(
                        selected = isAbActive || abPointA != null,
                        onClick = {
                            val rep = com.auraplayer.audio.ABRepeatManager.instance
                            if (abPointA == null) {
                                rep.setPointA(currentPositionMs)
                                Toast.makeText(context, "🅰️ Punto A marcado en ${formatTime(currentPositionMs)}", Toast.LENGTH_SHORT).show()
                            } else if (abPointB == null) {
                                rep.setPointB(currentPositionMs)
                                Toast.makeText(context, "🅱️ Punto B marcado en ${formatTime(currentPositionMs)} (Bucle Activo)", Toast.LENGTH_SHORT).show()
                            } else {
                                rep.clear()
                                Toast.makeText(context, "Bucle A-B reiniciado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = {
                            Text(
                                text = when {
                                    abPointA == null -> "🔁 Bucle A-B"
                                    abPointB == null -> "🔁 Marcar B (${formatTime(abPointA!!)})"
                                    isAbActive -> "🔁 [${com.auraplayer.audio.ABRepeatManager.instance.formatTime(abPointA)} - ${com.auraplayer.audio.ABRepeatManager.instance.formatTime(abPointB)}]"
                                    else -> "🔁 A-B Pausado"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF10B981),
                            labelColor = Color(0xFF34D399)
                        )
                    )

                    // Modo Conducción / Coche Chip
                    FilterChip(
                        selected = false,
                        onClick = { showCarDriveModeDialog = true },
                        label = { Text("🚗 Modo Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Quitar Voz (Karaoke Vocal Attenuation) Chip
                    FilterChip(
                        selected = isKaraokeVocalRemoved,
                        onClick = {
                            val newState = !isKaraokeVocalRemoved
                            isKaraokeVocalRemoved = newState
                            EqualizerManager.instance.setKaraokeMode(newState)
                            val msg = if (newState) "🎤 Modo Karaoke: Voz principal atenuada" else "Voz restaurada"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = if (isKaraokeVocalRemoved) "🎤 Voz: MUTE" else "🎤 Quitar Voz",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEC4899).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFEC4899),
                            labelColor = Color(0xFFEC4899)
                        )
                    )

                    // Cazador Musical (Song Hunter) Chip
                    FilterChip(
                        selected = false,
                        onClick = { showSongHunterDialog = true },
                        label = { Text("👂 Cazador Musical", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF00F0FF)
                        )
                    )

                    // Smart DJ Transition Chip
                    FilterChip(
                        selected = isSmartDj,
                        onClick = {
                            val newState = !isSmartDj
                            isSmartDj = newState
                            smartDjManager.isSmartDjEnabled = newState
                            val msg = if (newState) "🎛️ Transición DJ: ACTIVADA" else "Transición DJ: DESACTIVADA"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = if (isSmartDj) "🎛️ Smart DJ: ON" else "🎛️ Smart DJ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF10B981),
                            labelColor = Color(0xFF10B981)
                        )
                    )

                    // Mood Aura Chip
                    FilterChip(
                        selected = false,
                        onClick = { showMoodAuraDialog = true },
                        label = { Text("🌈 Mood Aura", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFFA855F7)
                        )
                    )

                    // AI Stem Mixer Chip
                    FilterChip(
                        selected = stemManager.isEnabled,
                        onClick = { showStemMixerDialog = true },
                        label = { Text("🎛️ AI Stem Mixer", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF38BDF8),
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Linterna Rítmica / Flashlight Beat Chip
                    FilterChip(
                        selected = isFlashlightActive,
                        onClick = {
                            AuraHaptic.click(view)
                            onToggleFlashlight()
                        },
                        label = {
                            Text(
                                text = if (isFlashlightActive) "⚡ Linterna Beat: ON" else "⚡ Linterna Beat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFBBF24).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFF59E0B),
                            labelColor = Color(0xFFFBBF24)
                        )
                    )

                    // Giroscopio 3D Holográfico Chip
                    FilterChip(
                        selected = isGyro3dEnabled,
                        onClick = {
                            AuraHaptic.click(view)
                            isGyro3dEnabled = !isGyro3dEnabled
                            val msg = if (isGyro3dEnabled) "🛸 Giroscopio 3D: ACTIVADO (inclina el teléfono)" else "Giroscopio 3D: Desactivado"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = if (isGyro3dEnabled) "🛸 Giroscopio 3D: ON" else "🛸 Giroscopio 3D",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF00F0FF),
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Club Party Strobe Chip
                    FilterChip(
                        selected = false,
                        onClick = {
                            AuraHaptic.click(view)
                            showClubPartyDialog = true
                        },
                        label = { Text("🔦 Modo Club RGB", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFFFBBF24)
                        )
                    )

                    // Stealth Panic Calculator Disguise Chip
                    FilterChip(
                        selected = false,
                        onClick = { showStealthCalculator = true },
                        label = { Text("🕵️ Calculadora Secreta", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF94A3B8)
                        )
                    )

                    // 8D Audio Chip
                    FilterChip(
                        selected = isSpatial8D,
                        onClick = {
                            val newState = !isSpatial8D
                            isSpatial8D = newState
                            EqualizerManager.instance.setSpatial8DEnabled(newState)
                            val msg = if (newState) "🎧 Sonido Espacial 8D: ACTIVADO" else "Sonido Espacial 8D: DESACTIVADO"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("🎧 8D Espacial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF00F0FF)
                        )
                    )

                    // Wave Control Chip
                    FilterChip(
                        selected = isWaveEnabled,
                        onClick = onToggleWave,
                        label = { Text("🌊 Wave Control", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF10B981)
                        )
                    )

                    // Neon Edge Lighting Chip
                    FilterChip(
                        selected = isEdgeLightingEnabled,
                        onClick = {
                            isEdgeLightingEnabled = !isEdgeLightingEnabled
                            val msg = if (isEdgeLightingEnabled) "🌈 Borde Neón: ACTIVADO" else "Borde Neón: DESACTIVADO"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("🌈 Borde Neón", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF0055).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFFF0055)
                        )
                    )

                    // Share Story Card Chip
                    FilterChip(
                        selected = false,
                        onClick = { StoryShareHelper.shareMusicStory(context, currentMedia) },
                        label = { Text("📸 Estado / Historia", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFFEC4899)
                        )
                    )

                    // DJ Soundboard Chip
                    FilterChip(
                        selected = false,
                        onClick = { showSoundboardDialog = true },
                        label = { Text("📢 DJ Soundboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFFFFD700)
                        )
                    )

                    // Wrapped Stats Chip
                    FilterChip(
                        selected = false,
                        onClick = onOpenWrapped,
                        label = { Text("🏆 Wrapped", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Audio Vibe Mode Chip (Slowed+Reverb / Nightcore / Normal)
                    FilterChip(
                        selected = vibeMode != com.auraplayer.audio.AudioVibe.NORMAL,
                        onClick = {
                            val next = onCycleVibe()
                            Toast.makeText(context, "${next.icon} Modo: ${next.label}", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("${vibeMode.icon} ${vibeMode.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFA855F7)
                        )
                    )

                    // Bass Haptics Chip
                    FilterChip(
                        selected = isHapticBass,
                        onClick = onToggleHapticBass,
                        label = { Text("📳 Bajos Hápticos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFF59E0B)
                        )
                    )

                    // WhatsApp Sticker Generator Chip
                    FilterChip(
                        selected = false,
                        onClick = { showStickerDialog = true },
                        label = { Text("💬 Crear Sticker", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF25D366)
                        )
                    )

                    // Karaoke Vocal Remover Chip
                    FilterChip(
                        selected = isKaraokeActive,
                        onClick = {
                            isKaraokeActive = com.auraplayer.audio.KaraokeVocalManager.instance.toggleKaraoke(context)
                        },
                        label = { Text(if (isKaraokeActive) "🎤 Karaoke: ACTIVO" else "🎤 Modo Karaoke", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE040FB).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFE040FB),
                            labelColor = Color(0xFFE040FB)
                        )
                    )

                    // Pitch & Speed Shifter Chip
                    FilterChip(
                        selected = false,
                        onClick = { showPitchSpeedDialog = true },
                        label = { Text("🎚️ Tono & Temp", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // 9:16 Story Lyric Card Chip
                    FilterChip(
                        selected = false,
                        onClick = { showStoryCardDialog = true },
                        label = { Text("📱 Historia 9:16", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFFEC4899)
                        )
                    )

                    // 3D Fullscreen Visualizer & OLED Saver Chip
                    FilterChip(
                        selected = false,
                        onClick = { showFullscreenVisualizer = true },
                        label = { Text("🌌 Visualizador 3D", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF00F0FF)
                        )
                    )

                    // A-B Looper Chip
                    FilterChip(
                        selected = looper.isEnabled,
                        onClick = {
                            if (looper.pointAMs == null) {
                                looper.setPointA(currentPositionMs)
                                Toast.makeText(context, "Punto A marcado en ${formatTime(currentPositionMs)}", Toast.LENGTH_SHORT).show()
                            } else if (looper.pointBMs == null) {
                                looper.setPointB(currentPositionMs)
                                Toast.makeText(context, "Punto B marcado en ${formatTime(currentPositionMs)} (Bucle activo)", Toast.LENGTH_SHORT).show()
                            } else {
                                looper.clear()
                                Toast.makeText(context, "Bucle A-B reiniciado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = {
                            Text(
                                when {
                                    looper.pointAMs == null -> "🔂 Marcar A-B"
                                    looper.pointBMs == null -> "🔂 Marcar Fin B"
                                    else -> "🔂 Bucle Activo [A-B]"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFFFD700),
                            labelColor = Color(0xFFFFD700)
                        )
                    )

                    // Quick EQ Chip
                    FilterChip(
                        selected = false,
                        onClick = {
                            AuraHaptic.click(view)
                            showQuickEqDialog = true
                        },
                        label = { Text("🎚️ EQ Rápido", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color(0xFF38BDF8)
                        )
                    )

                    // Relax Ambience Chip
                    val ambienceMgr = remember { RelaxAmbienceManager.instance }
                    FilterChip(
                        selected = ambienceMgr.currentSound != com.auraplayer.audio.AmbienceSound.NONE,
                        onClick = {
                            AuraHaptic.click(view)
                            showRelaxAmbienceDialog = true
                        },
                        label = {
                            Text(
                                if (ambienceMgr.currentSound != com.auraplayer.audio.AmbienceSound.NONE)
                                    "${ambienceMgr.currentSound.emoji} ${ambienceMgr.currentSound.title}"
                                else "🌧️ Relax & Ambiente",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF38BDF8),
                            labelColor = Color(0xFF38BDF8)
                        )
                    )
                }
                    }
                }

                // Primary Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            coroutineScope.launch {
                                shuffleRotation.snapTo(0f)
                                shuffleRotation.animateTo(
                                    360f,
                                    spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)
                                )
                            }
                            onShuffleToggle()
                        },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = shuffleRotation.value }
                            .clip(CircleShape)
                            .background(if (isShuffle) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = {
                            AuraHaptic.tick(view)
                            coroutineScope.launch {
                                prevNudge.snapTo(0f)
                                prevNudge.animateTo(-10f, tween(70))
                                prevNudge.animateTo(0f, spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow))
                            }
                            onPreviousClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { translationX = prevNudge.value }
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Aesthetic Glowing Play / Pause with Elastic Morphing & Glow Burst
                    PlayPauseMorphButton(
                        isPlaying = isPlaying,
                        onClick = {
                            AuraHaptic.heavy(view)
                            onPlayPauseClick()
                        },
                        size = 76.dp,
                        iconSize = 42.dp,
                        primaryColor = if (isMysteriousTheme || centerVisualizerMode == 7) Color(0xFFA855F7) else MaterialTheme.colorScheme.primary,
                        secondaryColor = if (isMysteriousTheme || centerVisualizerMode == 7) Color(0xFF00E5FF) else MaterialTheme.colorScheme.secondary
                    )

                    // Next
                    IconButton(
                        onClick = {
                            AuraHaptic.tick(view)
                            coroutineScope.launch {
                                nextNudge.snapTo(0f)
                                nextNudge.animateTo(10f, tween(70))
                                nextNudge.animateTo(0f, spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow))
                            }
                            onNextClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { translationX = nextNudge.value }
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Repeat with Elastic Pop Animation
                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            coroutineScope.launch {
                                repeatBounce.animateTo(1.35f, tween(100))
                                repeatBounce.animateTo(1f, spring(dampingRatio = 0.5f))
                            }
                            onRepeatToggle()
                        },
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = repeatBounce.value
                                scaleY = repeatBounce.value
                            }
                            .clip(CircleShape)
                            .background(if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repetir",
                            tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ergonomic Quick Controls & View Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Volume Booster
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (boostPercent > 100) Color(0xFFFF5722).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, if (boostPercent > 100) Color(0xFFFF5722).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .clickable {
                                AuraHaptic.click(view)
                                showVolumeBoosterDialog = true
                            }
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (boostPercent > 100) Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (boostPercent > 100) "Booster $boostPercent%" else "Booster",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (boostPercent > 100) Color(0xFFFF7043) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Next Track / Queue Quick-Peek Pill
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable {
                            AuraHaptic.click(view)
                            showQueueSheet = true
                        }
                        .bounceClick()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Ver Cola de Reproducción",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Floating Cyberpunk HUD (Volume / Brightness)
            AnimatedVisibility(
                visible = isHudVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.82f),
                exit = fadeOut() + scaleOut(targetScale = 0.82f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(28.dp, RoundedCornerShape(24.dp), spotColor = activeAccent.copy(alpha = 0.65f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xE610172A), Color(0xF2060913))
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(activeAccent.copy(alpha = 0.80f), Color(0xFF8B5CF6).copy(alpha = 0.40f))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 26.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = hudIcon, contentDescription = null, tint = activeAccent, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = hudText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        // Cyberpunk Neon Glow Bar
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(hudPercent.coerceIn(0f, 1f))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(activeAccent, Color(0xFF00F0FF))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Pocket Lock Overlay
            AnimatedVisibility(
                visible = isPocketLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    ambientColor = Color.Black
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF24324D),
                                            Color(0xFF101726)
                                        )
                                    )
                                )
                                .clickable {
                                    AuraHaptic.heavy(view)
                                    isPocketLocked = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Desbloquear pantalla",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Modo Bolsillo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toca para desbloquear",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Audiophile Technical Spec Sheet & Bit-Perfect DAC Studio Dialog
    if (showAudioSpecSheet) {
        com.auraplayer.ui.components.AudiophileDacHudDialog(
            song = currentMedia,
            durationMs = durationMs,
            onDismiss = { showAudioSpecSheet = false }
        )
    }

    // Aura Audio FX Studio Dialog (Slowed, Nightcore, Vaporwave, Custom Pitch & Speed)
    if (showFxDialog) {
        var tempSpeed by remember { mutableFloatStateOf(playbackSpeed) }
        var tempPitch by remember { mutableFloatStateOf(playbackPitch) }

        AlertDialog(
            onDismissRequest = { showFxDialog = false },
            containerColor = Color(0xFF101422),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DaVE Studio FX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Estilos y Efectos Predefinidos:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Buttons Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FxPresetButton("Original", "1.0x", tempSpeed == 1.0f && tempPitch == 1.0f, Modifier.weight(1f)) {
                            tempSpeed = 1.0f
                            tempPitch = 1.0f
                            onAudioFxChange(1.0f, 1.0f)
                        }
                        FxPresetButton("Slowed", "0.85x", tempSpeed == 0.85f && tempPitch == 0.85f, Modifier.weight(1f)) {
                            tempSpeed = 0.85f
                            tempPitch = 0.85f
                            onAudioFxChange(0.85f, 0.85f)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FxPresetButton("Nightcore", "1.25x", tempSpeed == 1.25f && tempPitch == 1.25f, Modifier.weight(1f)) {
                            tempSpeed = 1.25f
                            tempPitch = 1.25f
                            onAudioFxChange(1.25f, 1.25f)
                        }
                        FxPresetButton("Vaporwave", "0.75x", tempSpeed == 0.75f && tempPitch == 0.75f, Modifier.weight(1f)) {
                            tempSpeed = 0.75f
                            tempPitch = 0.75f
                            onAudioFxChange(0.75f, 0.75f)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Semitones Vocal Pitch Shift Chips (Karaoke Key Shifter)
                    Text(text = "Cambio de Tonalidad Vocal (Semitonos):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(-3 to "-3♭", -2 to "-2♭", -1 to "-1♭", 0 to "0", 1 to "+1♯", 2 to "+2♯", 3 to "+3♯").forEach { (semi, lbl) ->
                            val pitchVal = Math.pow(2.0, semi.toDouble() / 12.0).toFloat()
                            val isSelected = kotlin.math.abs(tempPitch - pitchVal) < 0.03f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable {
                                        tempPitch = pitchVal
                                        onAudioFxChange(tempSpeed, tempPitch)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Velocidad de Reproducción", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text(String.format("%.2fx", tempSpeed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tempSpeed,
                        onValueChange = {
                            tempSpeed = it
                            onAudioFxChange(tempSpeed, tempPitch)
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tono Musical (Pitch Shift)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text(String.format("%.2fx", tempPitch), style = MaterialTheme.typography.bodySmall, color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tempPitch,
                        onValueChange = {
                            tempPitch = it
                            onAudioFxChange(tempSpeed, tempPitch)
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEC4899), activeTrackColor = Color(0xFFEC4899))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFxDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Listo", color = Color.White)
                }
            }
        )
    }

    // Interactive Queue Sheet Modal
    if (showQueueSheet) {
        var queueDisplay by remember(queueSongs) { mutableStateOf(queueSongs) }
        val queueListState = rememberLazyListState()
        LaunchedEffect(currentMedia.id, queueDisplay.size) {
            val currentIdx = queueDisplay.indexOfFirst { it.id == currentMedia.id }
            if (currentIdx > 0) {
                queueListState.scrollToItem((currentIdx - 1).coerceAtLeast(0))
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF0D1222),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.6f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cola de Reproducción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${queueDisplay.size} canciones en cola",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                onShuffleToggle()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Mezclar cola",
                                tint = if (isShuffle) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { showQueueSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (queueDisplay.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("La cola de reproducción está vacía", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        state = queueListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = queueDisplay,
                            key = { idx, song -> "${song.id}_$idx" }
                        ) { index, song ->
                            val isPlayingThis = song.id == currentMedia.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (isPlayingThis) 5.dp else 2.5.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        spotColor = if (isPlayingThis) Color(0xFF00F0FF).copy(alpha = 0.35f) else Color.Black
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            if (isPlayingThis) listOf(Color(0xFF223050), Color(0xFF131B2E))
                                            else listOf(Color(0xFF181F30), Color(0xFF0F1420))
                                        )
                                    )
                                    .clickable {
                                        AuraHaptic.click(view)
                                        onQueueSongClick(song, index)
                                        showQueueSheet = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover or Note Icon
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (song.artworkUri != null) {
                                        AsyncImage(
                                            model = song.artworkUri,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (isPlayingThis && isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPlayingThis) Color(0xFF38BDF8) else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist} • ${song.formattedDuration}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = if (isPlayingThis) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isPlayingThis) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Sonando",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (index > 0) {
                                        IconButton(
                                            onClick = {
                                                AuraHaptic.click(view)
                                                MainActivity.activeController?.moveMediaItem(index, index - 1)
                                                queueDisplay = queueDisplay.toMutableList().apply {
                                                    val item = removeAt(index)
                                                    add(index - 1, item)
                                                }
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Subir en la cola",
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    if (index < queueDisplay.size - 1) {
                                        IconButton(
                                            onClick = {
                                                AuraHaptic.click(view)
                                                MainActivity.activeController?.moveMediaItem(index, index + 1)
                                                queueDisplay = queueDisplay.toMutableList().apply {
                                                    val item = removeAt(index)
                                                    add(index + 1, item)
                                                }
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Bajar en la cola",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            AuraHaptic.click(view)
                                            MainActivity.activeController?.removeMediaItem(index)
                                            queueDisplay = queueDisplay.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Quitar de la cola",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Player Secondary Tools BottomSheet
    if (showMoreToolsSheet) {
        PlayerMoreOptionsSheet(
            isHeadphonesConnected = isHeadphonesConnected,
            isShakeEnabled = isShakeEnabled,
            onOpenHeadphones = onOpenHeadphones,
            onOpenCarMode = onOpenCarMode,
            onOpenStandBy = onOpenStandBy,
            onOpenLofiCafe = { showLofiCafeDialog = true },
            onOpenAuraGalaxy = { showGalaxyDialog = true },
            onOpenBoombox80s = { showBoomboxDialog = true },
            onOpenPixelArcade = { showPixelArcadeDialog = true },
            onOpenSteampunk = { showSteampunkDialog = true },
            onOpenAquaZen = { showAquaZenDialog = true },
            onOpenSupercar = { showSupercarDialog = true },
            onOpenMinidisc = { showMinidiscDialog = true },
            onOpenHoloCockpit = { showHoloCockpitDialog = true },
            onOpenSanctuary = { showSanctuaryDialog = true },
            onOpenMysteriousThemes = { showMysteriousThemeDialog = true },
            onOpenArcaneFrequency = { showArcaneFrequencyDialog = true },
            onOpenOracle = { showOracleCard = true },
            onToggleShake = onToggleShake,
            onPocketLock = { isPocketLocked = true },
            onDeleteSong = { showDeleteConfirmDialog = true },
            onDismiss = { showMoreToolsSheet = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "¿Eliminar canción?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Se eliminará permanentemente '${currentMedia.title}' del almacenamiento de tu teléfono.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteSong(currentMedia)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = Color(0xFF101422),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Audio Cutter & Ringtone Maker Dialog
    if (showCutterDialog && currentMedia != null) {
        AudioCutterDialog(
            song = currentMedia,
            onDismiss = { showCutterDialog = false }
        )
    }

    // Party Soundboard Dialog
    if (showSoundboardDialog) {
        SoundboardDialog(
            onDismiss = { showSoundboardDialog = false }
        )
    }

    // Calibrated Acoustic DSP Profile Selector Dialog
    if (showQuickDspDialog) {
        AlertDialog(
            onDismissRequest = { showQuickDspDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuickDspDialog = false }) {
                    Text("Cerrar", color = activeAccent, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎧", fontSize = 20.sp)
                    Text(
                        text = "Calibración Acústica DSP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                val dspManager = com.auraplayer.audio.EqualizerManager.instance
                val currentIdx = dspManager.currentPresetIndex
                val presets = listOf(
                    Triple(13, "Beats Studio & Solo", "Graves profundos y pegada contundente"),
                    Triple(14, "Sennheiser Momentum / HD", "Escena amplia audiófila y agudos cristalinos"),
                    Triple(15, "Altavoz Bluetooth Party", "Volumen y dinámica para exteriores"),
                    Triple(8, "AirPods / AirPods Pro", "Bajos cálidos y firma balanceada"),
                    Triple(9, "Sony WH-1000XM / WF", "Sub-bajos y cancelación acústica"),
                    Triple(10, "Samsung Galaxy Buds", "Firma Harman Neutra y voces claras"),
                    Triple(11, "JBL Pure Bass Series", "Graves de club y alta pegada"),
                    Triple(1, "Super Bass Booster", "Realce masivo de frecuencias graves"),
                    Triple(7, "Vocal Pop & Acústico", "Claridad vocal y medios detallados"),
                    Triple(0, "Referencia Plana (Flat)", "Curva neutra de estudio sin coloración")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presets, key = { it.first }) { (presetIdx, title, subtitle) ->
                        val isSelected = currentIdx == presetIdx
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    AuraHaptic.click(view)
                                    dspManager.applyPreset(presetIdx)
                                    showQuickDspDialog = false
                                    Toast.makeText(context, "Perfil: $title", Toast.LENGTH_SHORT).show()
                                },
                            color = if (isSelected) activeAccent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) activeAccent.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                if (isSelected) {
                                    Text("✓", color = activeAccent, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF0F1523),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // WhatsApp Sticker Generator Dialog
    if (showStickerDialog) {
        com.auraplayer.ui.components.StickerGeneratorDialog(
            song = currentMedia,
            onDismiss = { showStickerDialog = false }
        )
    }

    // Pitch & Speed Shifter Dialog
    if (showPitchSpeedDialog) {
        com.auraplayer.ui.components.PitchSpeedDialog(
            initialSpeed = playbackSpeed,
            initialPitch = playbackPitch,
            onApply = onAudioFxChange,
            onDismiss = { showPitchSpeedDialog = false }
        )
    }

    // 9:16 Story Lyric Card Generator
    if (showStoryCardDialog) {
        com.auraplayer.ui.components.StoryCardDialog(
            song = currentMedia,
            initialQuote = if (storyCardInitialQuote.isNotBlank()) storyCardInitialQuote else "“La música expresa lo que no se puede decir y aquello que no puede callarse.”",
            onDismiss = {
                showStoryCardDialog = false
                storyCardInitialQuote = ""
            }
        )
    }

    // 3D Fullscreen Visualizer & OLED Pure Black
    if (showFullscreenVisualizer) {
        com.auraplayer.ui.components.VisualizerFullscreenDialog(
            song = currentMedia,
            isPlaying = isPlaying,
            onDismiss = { showFullscreenVisualizer = false }
        )
    }

    // Quick 3-Band Equalizer Dialog
    if (showQuickEqDialog) {
        com.auraplayer.ui.components.QuickEqDialog(
            onDismiss = { showQuickEqDialog = false }
        )
    }

    // Relax & Ambience Mixer Dialog
    if (showRelaxAmbienceDialog) {
        RelaxAmbienceDialog(
            onDismiss = { showRelaxAmbienceDialog = false }
        )
    }

    // Synced Lyrics LRC Dialog
    if (showSyncedLyricsDialog) {
        SyncedLyricsDialog(
            song = currentMedia,
            currentPositionMs = currentPositionMs,
            lyricsManager = syncedLyricsManager,
            onSeekTo = onSeek,
            onDismiss = { showSyncedLyricsDialog = false }
        )
    }

    // Retro iPod Classic Skin Dialog
    if (showIpodClassicDialog) {
        IpodClassicSkin(
            song = currentMedia,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onTogglePlay = onPlayPauseClick,
            onNext = onNextClick,
            onPrevious = onPreviousClick,
            onSeekRelative = onSeekRelative,
            onDismiss = { showIpodClassicDialog = false }
        )
    }

    // Audio Quality Inspector Dialog
    if (showAudioQualityDialog) {
        AudioQualityDialog(
            song = currentMedia,
            onDismiss = { showAudioQualityDialog = false }
        )
    }

    // AI Stem Mixer Studio Dialog
    if (showStemMixerDialog) {
        StemMixerDialog(
            isPlaying = isPlaying,
            stemManager = stemManager,
            onDismiss = { showStemMixerDialog = false }
        )
    }

    // Modo Lofi Chill Cafe Dialog
    if (showLofiCafeDialog) {
        LofiCafeDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showLofiCafeDialog = false }
        )
    }

    // Aura DNA: Galaxia Musical Dialog
    if (showGalaxyDialog) {
        AuraGalaxyDialog(
            songs = if (queueSongs.isNotEmpty()) queueSongs else (currentMedia?.let { listOf(it) } ?: emptyList()),
            currentMedia = currentMedia,
            onSongClick = { song ->
                val idx = queueSongs.indexOfFirst { it.id == song.id }
                if (idx != -1) {
                    onQueueSongClick(song, idx)
                }
            },
            onDismiss = { showGalaxyDialog = false }
        )
    }

    // Modo Boombox 80s Retro Stereo Dialog
    if (showBoomboxDialog) {
        Boombox80sDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            currentPosition = currentPositionMs,
            duration = durationMs,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showBoomboxDialog = false }
        )
    }

    // Modo Retro Arcade 16-Bit Dialog
    if (showPixelArcadeDialog) {
        PixelArcadeDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showPixelArcadeDialog = false }
        )
    }

    // Fonógrafo Steampunk 1890 Dialog
    if (showSteampunkDialog) {
        SteampunkPhonographDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showSteampunkDialog = false }
        )
    }

    // Acuario Zen Bioluminiscente Dialog
    if (showAquaZenDialog) {
        AquaZenDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showAquaZenDialog = false }
        )
    }

    // Modo Supercar Cockpit GT Dialog
    if (showSupercarDialog) {
        SupercarCockpitDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = effectiveDurationMs,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showSupercarDialog = false }
        )
    }

    // Modo Minidisc Hi-MD 2000s Dialog
    if (showMinidiscDialog) {
        MinidiscRetroDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = effectiveDurationMs,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showMinidiscDialog = false }
        )
    }

    // Aura Holo-Cockpit 2077 Dialog
    if (showHoloCockpitDialog) {
        HoloCockpitDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showHoloCockpitDialog = false }
        )
    }

    // Santuario Nocturno Vela Viva Dialog
    if (showSanctuaryDialog) {
        AuraSanctuaryDialog(
            currentMedia = currentMedia,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onDismiss = { showSanctuaryDialog = false }
        )
    }

    // Stealth Panic Calculator Disguise Dialog
    if (showStealthCalculator) {
        StealthCalculatorDialog(
            onUnlock = { showStealthCalculator = false }
        )
    }

    // Club Party Strobe Hub Dialog
    if (showClubPartyDialog) {
        ClubPartyDialog(
            isPlaying = isPlaying,
            onDismiss = { showClubPartyDialog = false }
        )
    }

    // Modo Conducción / Coche Inteligente Dialog
    if (showCarDriveModeDialog) {
        CarDriveModeDialog(
            song = currentMedia,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onPlayPause = onPlayPauseClick,
            onNext = onNextClick,
            onPrevious = onPreviousClick,
            onDismiss = { showCarDriveModeDialog = false }
        )
    }

    // Cazador Musical (Song Hunter) Dialog
    if (showSongHunterDialog) {
        SongHunterDialog(
            onPlayOrSearch = { query ->
                showSongHunterDialog = false
                onDismiss()
                onSearchOnline(query)
            },
            onDismiss = { showSongHunterDialog = false }
        )
    }

    // Mood Aura / Rueda de Emociones Dialog
    if (showMoodAuraDialog) {
        MoodWheelDialog(
            allSongs = emptyList(),
            onMoodSelected = { mood, _ ->
                showMoodAuraDialog = false
                Toast.makeText(context, "Sintonizando Mood Aura: ${mood.title}", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showMoodAuraDialog = false }
        )
    }

    // Super Booster de Volumen 200% Dialog
    if (showVolumeBoosterDialog) {
        com.auraplayer.ui.components.VolumeBoosterDialog(
            onDismiss = { showVolumeBoosterDialog = false }
        )
    }

    // Modo ZAP Dialog
    if (showZapDialog) {
        ZapDialog(
            zapManager = zapManager,
            isCurrentlyActive = zapManager.isZapActive,
            onStartZap = { seconds ->
                zapManager.previewDurationSeconds = seconds
                zapManager.startZap(com.auraplayer.MainActivity.activeController, coroutineScope)
                showZapDialog = false
                Toast.makeText(context, "⚡ Modo ZAP Activo: Explorando cada ${seconds}s", Toast.LENGTH_SHORT).show()
            },
            onStopZap = {
                zapManager.stopZap()
                showZapDialog = false
                Toast.makeText(context, "Modo ZAP Detenido", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showZapDialog = false }
        )
    }

    // Party Link / Silent Disco Dialog
    if (showPartyLinkDialog) {
        val currentPosState = remember(currentPositionMs) { androidx.compose.runtime.mutableStateOf(currentPositionMs) }
        PartyLinkDialog(
            partyManager = partyLinkManager,
            currentMedia = currentMedia,
            currentPosMs = currentPosState,
            isPlaying = isPlaying,
            onSyncPlay = { title, pos, playing ->
                Toast.makeText(context, "📻 Sincronizando con Party Link: $title", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPartyLinkDialog = false }
        )
    }

    // Mysterious Themes & Colors In-Player Dialog
    if (showMysteriousThemeDialog) {
        MysteriousThemeDialog(
            currentAccent = currentThemeAccentState,
            onAccentChange = { newAccent ->
                currentThemeAccentState = newAccent
                playerPlaylistManager.setThemeAccent(newAccent)
                onAccentChange(newAccent)
            },
            onDismiss = { showMysteriousThemeDialog = false }
        )
    }

    // Consola de Resonancia Arcana & Frecuencias Solfeggio
    if (showArcaneFrequencyDialog) {
        ArcaneFrequencyDialog(
            currentHz = currentArcaneHz,
            onApplyFrequency = { hz, pitchRatio ->
                currentArcaneHz = hz
                onAudioFxChange(1.0f, pitchRatio)
                showArcaneFrequencyDialog = false
            },
            onDismiss = { showArcaneFrequencyDialog = false }
        )
    }

    // El Oráculo de DaVE: Predicción Esotérica de la Canción del Día
    if (showOracleCard) {
        val candidateSongs = remember(queueSongs, currentMedia) {
            if (queueSongs.isNotEmpty()) queueSongs else listOf(currentMedia)
        }
        val predicted = remember(candidateSongs) {
            predictDailySong(candidateSongs, context.getSharedPreferences("dave_oracle", Context.MODE_PRIVATE))
                ?: Pair(currentMedia, 92)
        }
        OracleCard(
            predictedSong = predicted.first,
            confidencePercent = predicted.second,
            onPlay = {
                val idx = queueSongs.indexOfFirst { it.id == predicted.first.id }
                if (idx >= 0) {
                    onQueueSongClick(predicted.first, idx)
                } else {
                    onPlayPauseClick()
                }
                showOracleCard = false
            },
            onDismiss = { showOracleCard = false }
        )
    }

    // Sueño Etéreo: despedida esotérica — el velo se espesa mientras el volumen se desvanece (últimos 120 s)
    if (sleepTimerManager != null) {
        val sleepActive by sleepTimerManager.isTimerActive.collectAsState()
        val sleepRemaining by sleepTimerManager.remainingSeconds.collectAsState()
        val veilAlpha = if (sleepActive && sleepRemaining in 1..120) {
            ((120 - sleepRemaining) / 120f).coerceIn(0f, 1f)
        } else 0f
        if (veilAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF05030B).copy(alpha = veilAlpha * 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ᛉ  El velo te llama…",
                    color = Color(0xFFB388FF).copy(alpha = 0.4f + veilAlpha * 0.6f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FxPresetButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 5.dp else 2.5.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Black
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    if (isSelected) listOf(Color(0xFF263350), Color(0xFF131A2A))
                    else listOf(Color(0xFF1B2236), Color(0xFF0F1524))
                )
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
            )
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
