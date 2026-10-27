package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import com.auraplayer.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.AuraHaptic

data class HubToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String,
    val color: Color,
    val isActive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaveMagicHubSheet(
    onDismiss: () -> Unit,
    // Social / Downloaders
    onOpenTikTok: () -> Unit,
    onOpenInstagram: () -> Unit,
    onOpenFacebook: () -> Unit,
    onOpenWhatsAppStatus: () -> Unit,
    onOpenVideoExtractor: () -> Unit,
    // Audio Lab
    onOpenStemMixer: () -> Unit,
    onOpenViralStudio: () -> Unit,
    onOpenZap: () -> Unit,
    onOpenBinaural: () -> Unit,
    onOpenRelaxAmbience: () -> Unit,
    onOpenHeadphones: () -> Unit,
    onOpenBpmWorkout: () -> Unit = {},
    // Fiesta & Juegos
    onOpenPartyLink: () -> Unit,
    onOpenKaraoke: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenRoulette: () -> Unit,
    onOpenTournament: () -> Unit,
    onOpenMoodWheel: () -> Unit = {},
    isFlashlightActive: Boolean,
    onToggleFlashlight: () -> Unit,
    isVirtualDjActive: Boolean,
    onToggleVirtualDj: () -> Unit,
    onOpenJukebox: () -> Unit,
    onOpenOracle: () -> Unit,
    // Utilidades
    onOpenRadar: () -> Unit,
    onOpenInsights: () -> Unit = {},
    onOpenTimeCapsule: () -> Unit = {},
    onOpenAlarm: () -> Unit = {},
    onOpenBatchCovers: () -> Unit,
    onOpenDiagnostic: () -> Unit,
    isWifiServerRunning: Boolean,
    onToggleWifiServer: () -> Unit,
    onOpenStoryMaker: () -> Unit,
    onOpenFolderShield: () -> Unit,
    isBubbleActive: Boolean = false,
    onToggleBubble: () -> Unit = {},
    isAirGesturesActive: Boolean = false,
    onToggleAirGestures: () -> Unit = {},
    isShakeActive: Boolean = false,
    onToggleShake: () -> Unit = {},
    isSleepSensorActive: Boolean = false,
    onToggleSleepSensor: () -> Unit = {},
    onOpenThemes: () -> Unit,
    onOpenOledSaver: () -> Unit,
    dailySong: com.auraplayer.data.model.MediaModel? = null,
    onPlayDailySong: (com.auraplayer.data.model.MediaModel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("🔥 Todo", "📥 Descargas", "🎛️ Audio Lab", "🎉 Fiesta & Juegos", "🧰 Utilidades")

    val allTools = listOf(
        listOf(
            // 1. Descargas & Redes
            HubToolItem(
                id = "tiktok",
                title = stringResource(R.string.hub_tiktok_title),
                subtitle = stringResource(R.string.hub_tiktok_subtitle),
                icon = Icons.Default.MusicNote,
                category = "📥 Descargas",
                color = Color(0xFF00F2FE),
                onClick = onOpenTikTok
            ),
            HubToolItem(
                id = "instagram",
                title = stringResource(R.string.hub_instagram_title),
                subtitle = stringResource(R.string.hub_instagram_subtitle),
                icon = Icons.Default.CameraAlt,
                category = "📥 Descargas",
                color = Color(0xFFE1306C),
                onClick = onOpenInstagram
            ),
            HubToolItem(
                id = "facebook",
                title = stringResource(R.string.hub_facebook_title),
                subtitle = stringResource(R.string.hub_facebook_subtitle),
                icon = Icons.Default.VideoLibrary,
                category = "📥 Descargas",
                color = Color(0xFF1877F2),
                onClick = onOpenFacebook
            ),
            HubToolItem(
                id = "whatsapp",
                title = stringResource(R.string.hub_whatsapp_title),
                subtitle = stringResource(R.string.hub_whatsapp_subtitle),
                icon = Icons.Default.Chat,
                category = "📥 Descargas",
                color = Color(0xFF25D366),
                onClick = onOpenWhatsAppStatus
            ),
            HubToolItem(
                id = "extractor",
                title = stringResource(R.string.hub_extractor_title),
                subtitle = stringResource(R.string.hub_extractor_subtitle),
                icon = Icons.Default.Movie,
                category = "📥 Descargas",
                color = Color(0xFF06B6D4),
                onClick = onOpenVideoExtractor
            ),

            // 2. Audio Lab
            HubToolItem(
                id = "stem_mixer",
                title = stringResource(R.string.hub_stem_title),
                subtitle = stringResource(R.string.hub_stem_subtitle),
                icon = Icons.Default.Tune,
                category = "🎛️ Audio Lab",
                color = Color(0xFFA78BFA),
                onClick = onOpenStemMixer
            ),
            HubToolItem(
                id = "viral_studio",
                title = stringResource(R.string.hub_viral_title),
                subtitle = stringResource(R.string.hub_viral_subtitle),
                icon = Icons.Default.GraphicEq,
                category = "🎛️ Audio Lab",
                color = Color(0xFFEC4899),
                onClick = onOpenViralStudio
            ),
            HubToolItem(
                id = "zap_dj",
                title = stringResource(R.string.hub_zap_title),
                subtitle = stringResource(R.string.hub_zap_subtitle),
                icon = Icons.Default.Bolt,
                category = "🎛️ Audio Lab",
                color = Color(0xFFF59E0B),
                onClick = onOpenZap
            ),
            HubToolItem(
                id = "binaural",
                title = stringResource(R.string.hub_binaural_title),
                subtitle = stringResource(R.string.hub_binaural_subtitle),
                icon = Icons.Default.Psychology,
                category = "🎛️ Audio Lab",
                color = Color(0xFF38BDF8),
                onClick = onOpenBinaural
            ),
            HubToolItem(
                id = "ambience",
                title = stringResource(R.string.hub_ambience_title),
                subtitle = stringResource(R.string.hub_ambience_subtitle),
                icon = Icons.Default.WaterDrop,
                category = "🎛️ Audio Lab",
                color = Color(0xFF00E5FF),
                onClick = onOpenRelaxAmbience
            ),
            HubToolItem(
                id = "headphones",
                title = stringResource(R.string.hub_headphones_title),
                subtitle = stringResource(R.string.hub_headphones_subtitle),
                icon = Icons.Default.Headphones,
                category = "🎛️ Audio Lab",
                color = Color(0xFF38BDF8),
                onClick = onOpenHeadphones
            ),
            HubToolItem(
                id = "bpm_workout",
                title = stringResource(R.string.hub_bpm_title),
                subtitle = stringResource(R.string.hub_bpm_subtitle),
                icon = Icons.Default.Timer,
                category = "🎛️ Audio Lab",
                color = Color(0xFF00F0FF),
                onClick = onOpenBpmWorkout
            ),

            // 3. Fiesta & Juegos
            HubToolItem(
                id = "party_link",
                title = stringResource(R.string.hub_party_title),
                subtitle = stringResource(R.string.hub_party_subtitle),
                icon = Icons.Default.Radio,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFF8B5CF6),
                onClick = onOpenPartyLink
            ),
            HubToolItem(
                id = "karaoke",
                title = stringResource(R.string.hub_karaoke_title),
                subtitle = stringResource(R.string.hub_karaoke_subtitle),
                icon = Icons.Default.Mic,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFE040FB),
                onClick = onOpenKaraoke
            ),
            HubToolItem(
                id = "quiz",
                title = stringResource(R.string.hub_quiz_title),
                subtitle = stringResource(R.string.hub_quiz_subtitle),
                icon = Icons.Default.SportsEsports,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFF43F5E),
                onClick = onOpenQuiz
            ),
            HubToolItem(
                id = "roulette",
                title = stringResource(R.string.hub_roulette_title),
                subtitle = stringResource(R.string.hub_roulette_subtitle),
                icon = Icons.Default.Casino,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFFFD700),
                onClick = onOpenRoulette
            ),
            HubToolItem(
                id = "tournament",
                title = stringResource(R.string.hub_tournament_title),
                subtitle = stringResource(R.string.hub_tournament_subtitle),
                icon = Icons.Default.Casino,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFFF9800),
                onClick = onOpenTournament
            ),
            HubToolItem(
                id = "mood_wheel",
                title = stringResource(R.string.hub_mood_title),
                subtitle = stringResource(R.string.hub_mood_subtitle),
                icon = Icons.Default.AutoAwesome,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFA855F7),
                onClick = onOpenMoodWheel
            ),
            HubToolItem(
                id = "flashlight",
                title = stringResource(R.string.hub_flashlight_title),
                subtitle = stringResource(R.string.hub_flashlight_subtitle),
                icon = Icons.Default.FlashlightOn,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFEAB308),
                isActive = isFlashlightActive,
                onClick = onToggleFlashlight
            ),
            HubToolItem(
                id = "virtual_dj",
                title = stringResource(R.string.hub_dj_title),
                subtitle = stringResource(R.string.hub_dj_subtitle),
                icon = Icons.Default.RecordVoiceOver,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFF10B981),
                isActive = isVirtualDjActive,
                onClick = onToggleVirtualDj
            ),
            HubToolItem(
                id = "jukebox",
                title = stringResource(R.string.hub_jukebox_title),
                subtitle = stringResource(R.string.hub_jukebox_subtitle),
                icon = Icons.Default.AutoAwesome,
                category = "🎉 Fiesta & Juegos",
                color = Color(0xFFEC4899),
                onClick = onOpenJukebox
            ),

            // 4. Utilidades
            HubToolItem(
                id = "radar",
                title = stringResource(R.string.hub_radar_title),
                subtitle = stringResource(R.string.hub_radar_subtitle),
                icon = Icons.Default.Radar,
                category = "🧰 Utilidades",
                color = Color(0xFF00F0FF),
                onClick = onOpenRadar
            ),
            HubToolItem(
                id = "insights",
                title = stringResource(R.string.hub_insights_title),
                subtitle = stringResource(R.string.hub_insights_subtitle),
                icon = Icons.Default.GraphicEq,
                category = "🧰 Utilidades",
                color = Color(0xFF6366F1),
                onClick = onOpenInsights
            ),
            HubToolItem(
                id = "time_capsule",
                title = stringResource(R.string.hub_capsule_title),
                subtitle = stringResource(R.string.hub_capsule_subtitle),
                icon = Icons.Default.AutoAwesome,
                category = "🧰 Utilidades",
                color = Color(0xFFFFD700),
                onClick = onOpenTimeCapsule
            ),
            HubToolItem(
                id = "alarm",
                title = stringResource(R.string.hub_alarm_title),
                subtitle = stringResource(R.string.hub_alarm_subtitle),
                icon = Icons.Default.NotificationsActive,
                category = "🧰 Utilidades",
                color = Color(0xFF10B981),
                onClick = onOpenAlarm
            ),
            HubToolItem(
                id = "covers",
                title = stringResource(R.string.hub_covers_title),
                subtitle = stringResource(R.string.hub_covers_subtitle),
                icon = Icons.Default.Image,
                category = "🧰 Utilidades",
                color = Color(0xFF06B6D4),
                onClick = onOpenBatchCovers
            ),
            HubToolItem(
                id = "diagnostic",
                title = stringResource(R.string.hub_diagnostic_title),
                subtitle = stringResource(R.string.hub_diagnostic_subtitle),
                icon = Icons.Default.Handyman,
                category = "🧰 Utilidades",
                color = Color(0xFF3B82F6),
                onClick = onOpenDiagnostic
            ),
            HubToolItem(
                id = "wifi_server",
                title = stringResource(R.string.hub_wifi_title),
                subtitle = stringResource(R.string.hub_wifi_subtitle),
                icon = Icons.Default.Wifi,
                category = "🧰 Utilidades",
                color = Color(0xFF10B981),
                isActive = isWifiServerRunning,
                onClick = onToggleWifiServer
            ),
            HubToolItem(
                id = "story_maker",
                title = stringResource(R.string.hub_story_title),
                subtitle = stringResource(R.string.hub_story_subtitle),
                icon = Icons.Default.ContentCut,
                category = "🧰 Utilidades",
                color = Color(0xFFF472B6),
                onClick = onOpenStoryMaker
            ),
            HubToolItem(
                id = "folder_shield",
                title = stringResource(R.string.hub_shield_title),
                subtitle = stringResource(R.string.hub_shield_subtitle),
                icon = Icons.Default.Security,
                category = "🧰 Utilidades",
                color = Color(0xFF10B981),
                onClick = onOpenFolderShield
            ),
            HubToolItem(
                id = "bubble",
                title = stringResource(R.string.hub_bubble_title),
                subtitle = stringResource(R.string.hub_bubble_subtitle),
                icon = Icons.Default.MusicNote,
                category = "🧰 Utilidades",
                color = Color(0xFF06B6D4),
                isActive = isBubbleActive,
                onClick = onToggleBubble
            ),
            HubToolItem(
                id = "air_gestures",
                title = stringResource(R.string.hub_air_title),
                subtitle = stringResource(R.string.hub_air_subtitle),
                icon = Icons.Default.Tune,
                category = "🧰 Utilidades",
                color = Color(0xFFE040FB),
                isActive = isAirGesturesActive,
                onClick = onToggleAirGestures
            ),
            HubToolItem(
                id = "shake",
                title = stringResource(R.string.hub_shake_title),
                subtitle = stringResource(R.string.hub_shake_subtitle),
                icon = Icons.Default.Shuffle,
                category = "🧰 Utilidades",
                color = Color(0xFFF59E0B),
                isActive = isShakeActive,
                onClick = onToggleShake
            ),
            HubToolItem(
                id = "sleep_sensor",
                title = stringResource(R.string.hub_sleep_title),
                subtitle = stringResource(R.string.hub_sleep_subtitle),
                icon = Icons.Default.Timer,
                category = "🧰 Utilidades",
                color = Color(0xFF38BDF8),
                isActive = isSleepSensorActive,
                onClick = onToggleSleepSensor
            ),
            HubToolItem(
                id = "themes",
                title = stringResource(R.string.hub_themes_title),
                subtitle = stringResource(R.string.hub_themes_subtitle),
                icon = Icons.Default.Palette,
                category = "🧰 Utilidades",
                color = Color(0xFFB388FF),
                onClick = onOpenThemes
            ),
            HubToolItem(
                id = "oled_saver",
                title = stringResource(R.string.hub_oled_title),
                subtitle = stringResource(R.string.hub_oled_subtitle),
                icon = Icons.Default.BatteryChargingFull,
                category = "🧰 Utilidades",
                color = Color(0xFF22C55E),
                onClick = onOpenOledSaver
            )
        )
    
    val filteredTools = remember(allTools, selectedCategoryIndex, searchQuery) {
        allTools.filter { tool ->
            val matchesCategory = when (selectedCategoryIndex) {
                0 -> true
                else -> tool.category == categories[selectedCategoryIndex]
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                tool.title.contains(searchQuery, ignoreCase = true) ||
                        tool.subtitle.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0C101D),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Carta del Día: fase lunar + canción destinada por el azar cósmico (Suite Misteriosa)
            val daily = dailySong
            if (daily != null) {
                val lunar = androidx.compose.runtime.remember { com.auraplayer.audio.LunarPhaseManager.getCurrentPhase() }
                DailyArcaneCard(
                    song = daily,
                    lunarText = "${lunar.phaseEmoji} ${lunar.phaseName} · ${lunar.illuminationPercent}%",
                    rune = lunar.runeSymbol,
                    onPlay = {
                        AuraHaptic.heavy(view)
                        onPlayDailySong(daily)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Header: Icon + Title + Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF5722), Color(0xFFFF9800))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DaVE Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Herramientas & Experiencias",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF182236))
                    .border(BorderStroke(1.dp, Color(0x26FFFFFF)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(Color(0xFF00F0FF)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar herramienta (TikTok, 8D, Quiz, Limpieza...)",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.indices.toList()) { index ->
                    val isSelected = selectedCategoryIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00F0FF), Color(0xFF0072FF))
                                    )
                                } else {
                                    SolidColor(Color(0xFF182236))
                                }
                            )
                            .clickable {
                                AuraHaptic.click(view)
                                selectedCategoryIndex = index
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = categories[index],
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tools Grid (2 columns)
            if (filteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No se encontraron herramientas con '$searchQuery'",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTools, key = { it.id }) { tool ->
                        HubToolCard(
                            tool = tool,
                            onItemClick = {
                                AuraHaptic.click(view)
                                onDismiss()
                                tool.onClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubToolCard(
    tool: HubToolItem,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141A29))
            .border(
                BorderStroke(
                    1.dp,
                    if (tool.isActive) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0x1AFFFFFF)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onItemClick)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tool.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = tool.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (tool.isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF064E3B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVO",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = tool.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.subtitle,
                    fontSize = 9.5.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyArcaneCard(
    song: com.auraplayer.data.model.MediaModel,
    lunarText: String,
    rune: String,
    onPlay: () -> Unit
) {
    val glowPulse by rememberInfiniteTransition(label = "daily_arcane").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFB388FF).copy(alpha = glowPulse))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF221438), Color(0xFF0F081E))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFB388FF).copy(alpha = 0.7f),
                        Color(0xFF00E5FF).copy(alpha = 0.35f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onPlay() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sello rúnico
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF31194F))
                .border(1.2.dp, Color(0xFFB388FF).copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rune,
                color = Color(0xFFE2D4FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CARTA DEL DÍA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFB388FF)
            )
            Text(
                text = song.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} · $lunarText",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Invocar canción del día",
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(26.dp)
        )
    }
}
