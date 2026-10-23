package com.auraplayer.data.repository

import com.auraplayer.data.model.OnlineTrack
import com.auraplayer.data.network.OkHttpDownloader
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.VideoStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class YouTubeMusicRepository {

    companion object {
        private const val TAG = "YouTubeMusicRepository"
        private val isInitialized = AtomicBoolean(false)
        fun initNewPipe() {
            if (isInitialized.compareAndSet(false, true)) {
                try {
                    NewPipe.init(OkHttpDownloader.instance, Localization.DEFAULT, ContentCountry("US"))
                } catch (e: Exception) {
                    try {
                        NewPipe.init(OkHttpDownloader.instance)
                    } catch (e2: Exception) {
                        AppLog.e(TAG, "Fallo al inicializar NewPipe sin localización", e2)
                    }
                }
            }
        }
    }

    init {
        initNewPipe()
    }

    suspend fun searchYouTube(query: String): List<OnlineTrack> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val list = mutableListOf<OnlineTrack>()

        try {
            val url = URL("https://www.youtube.com/youtubei/v1/search")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20231201.00.00")
                        put("hl", "es")
                        put("gl", "US")
                    })
                })
                put("query", query.trim())
            }

            connection.outputStream.use { it.write(payload.toString().toByteArray()) }

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val sections = root.optJSONObject("contents")
                    ?.optJSONObject("twoColumnSearchResultsRenderer")
                    ?.optJSONObject("primaryContents")
                    ?.optJSONObject("sectionListRenderer")
                    ?.optJSONArray("contents")

                if (sections != null) {
                    for (s in 0 until sections.length()) {
                        val itemSection = sections.optJSONObject(s)?.optJSONObject("itemSectionRenderer") ?: continue
                        val items = itemSection.optJSONArray("contents") ?: continue

                        for (i in 0 until items.length()) {
                            val video = items.optJSONObject(i)?.optJSONObject("videoRenderer") ?: continue
                            val videoId = video.optString("videoId")
                            if (videoId.isBlank() || list.any { it.id == "yt_$videoId" }) continue

                            val title = video.optJSONObject("title")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optString("text") ?: "Canción"

                            val author = video.optJSONObject("ownerText")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optString("text") ?: "Artista"

                            val durationStr = video.optJSONObject("lengthText")
                                ?.optString("simpleText", "0:00") ?: "0:00"

                            val thumbArray = video.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                            val rawCover = if (thumbArray != null && thumbArray.length() > 0) {
                                thumbArray.optJSONObject(thumbArray.length() - 1)?.optString("url") ?: ""
                            } else {
                                ""
                            }
                            val coverUrl = when {
                                rawCover.startsWith("//") -> "https:$rawCover"
                                rawCover.startsWith("http") -> rawCover
                                else -> "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                            }

                            // Parse duration mm:ss or hh:mm:ss to seconds
                            val parts = durationStr.split(":")
                            val durationSec = when (parts.size) {
                                2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
                                3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
                                else -> 180
                            }

                            val audioUrl = "https://www.youtube.com/watch?v=$videoId"

                            list.add(
                                OnlineTrack(
                                    id = "yt_$videoId",
                                    title = title,
                                    artist = author,
                                    album = "YouTube Music",
                                    durationSec = durationSec,
                                    audioUrl = audioUrl,
                                    coverUrl = coverUrl,
                                    format = "HQ Audio",
                                    bitrateKbps = 320,
                                    license = "YouTube",
                                    source = "YouTube",
                                    isDownloadable = true
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Fallo en la petición de búsqueda de YouTube Music", e)
        }
        list
    }

    private var cobaltMirrors = listOf(
        "https://api.cobalt.tools",
        "https://cobalt-api.kwiatekm.pl",
        "https://cobalt.api.sciter.io"
    )

    private var invidiousMirrors = listOf(
        "https://inv.nadeko.net",
        "https://invidious.nerdvpn.de",
        "https://invidious.tiekoetter.com",
        "https://yt.chocolatemoo53.com",
        "https://invidious.f5.si"
    )

    var onYouTubeApiChangedDetected: (() -> Unit)? = null
    private var lastConfigFetchTime = 0L

    private fun refreshRemoteConfigIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastConfigFetchTime < 3600_000L) return
        lastConfigFetchTime = now

        try {
            val url = URL("https://raw.githubusercontent.com/davidchaparro780-wq/AuraPlayer/main/api-config.json")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val cList = json.optJSONArray("cobaltInstances")
                if (cList != null && cList.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until cList.length()) list.add(cList.getString(i))
                    cobaltMirrors = list
                }
                val iList = json.optJSONArray("invidiousInstances")
                if (iList != null && iList.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until iList.length()) list.add(iList.getString(i))
                    invidiousMirrors = list
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "No se pudo refrescar la config remota de instancias", e)
        }
    }

    suspend fun resolveAudioStream(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null
        refreshRemoteConfigIfNeeded()

        // 1. Primary: InnerTube ANDROID_VR player (Zero BotGuard, unthrottled adaptive audio & direct formats)
        try {
            val vrUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID_VR",
                clientVersion = "1.56.21",
                userAgent = "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36",
                isVideo = false
            )
            if (!vrUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Audio resuelto exitosamente vía InnerTube ANDROID_VR")
                return@withContext vrUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube ANDROID_VR para videoId=$videoId", e)
        }

        // 2. Secondary: InnerTube VISIONOS player (Vision Pro client, direct streams)
        try {
            val visionUrl = queryInnerTube(
                videoId = videoId,
                clientName = "VISIONOS",
                clientVersion = "1.02",
                userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3 Safari/605.1.15",
                isVideo = false
            )
            if (!visionUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Audio resuelto exitosamente vía InnerTube VISIONOS")
                return@withContext visionUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube VISIONOS para videoId=$videoId", e)
        }

        // 3. Tertiary: InnerTube ANDROID player (v21.26.364 with direct muxed MP4 stream for VEVO tracks)
        try {
            val androidUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID",
                clientVersion = "21.26.364",
                userAgent = "com.google.android.youtube/21.26.364 (Linux; U; Android 14)",
                isVideo = false
            )
            if (!androidUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Audio resuelto exitosamente vía InnerTube ANDROID")
                return@withContext androidUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube ANDROID para videoId=$videoId", e)
        }

        // 4. Quaternary: InnerTube IOS player (v21.26.4)
        try {
            val iosUrl = queryInnerTube(
                videoId = videoId,
                clientName = "IOS",
                clientVersion = "21.26.4",
                userAgent = "com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_1_1 like Mac OS X; en_US)",
                deviceModel = "iPhone16,2",
                isVideo = false
            )
            if (!iosUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Audio resuelto exitosamente vía InnerTube IOS")
                return@withContext iosUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube IOS para videoId=$videoId", e)
        }

        // 4. Quaternary: Cobalt High-Speed API Gateway (Dedicated anti-blocking server)
        try {
            val cobaltUrl = queryCobaltApi(videoId, isVideo = false)
            if (!cobaltUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Audio resuelto exitosamente vía Cobalt API")
                return@withContext cobaltUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en Cobalt API para videoId=$videoId", e)
        }

        // 5. Quinquenary: Active Invidious mirrors
        for (mirror in invidiousMirrors.take(3)) {
            try {
                val invUrl = "$mirror/api/v1/videos/$videoId"
                val conn = (URL(invUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                }
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(resp)
                    val formats = root.optJSONArray("adaptiveFormats") ?: root.optJSONArray("formatStreams")
                    if (formats != null) {
                        for (i in 0 until formats.length()) {
                            val fmt = formats.getJSONObject(i)
                            val type = fmt.optString("type", "")
                            val u = fmt.optString("url", "")
                            if (u.isNotBlank() && type.contains("audio", ignoreCase = true)) {
                                return@withContext u
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo en mirror Invidious $mirror para videoId=$videoId", e)
            }
        }

        // 6. Fallback: NewPipeExtractor
        try {
            initNewPipe()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val extractor = ServiceList.YouTube.getStreamExtractor(watchUrl)
            extractor.fetchPage()
            val audioStreams: List<AudioStream>? = extractor.audioStreams
            if (!audioStreams.isNullOrEmpty()) {
                val chosenStream = audioStreams.filter {
                    it.format?.name?.contains("M4A", ignoreCase = true) == true
                }.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                val directUrl = chosenStream.url
                if (!directUrl.isNullOrBlank()) {
                    return@withContext directUrl
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en NewPipeExtractor para videoId=$videoId", e)
        }

        // If all providers failed, YouTube likely updated its cipher/player JS — trigger self-updater check!
        try {
            onYouTubeApiChangedDetected?.invoke()
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo al invocar detector de cambio de API de YouTube", e)
        }

        null
    }

    /**
     * Resolves high quality video MP4 stream (audio + video muxed) for offline playback in Movies gallery.
     */
    suspend fun resolveVideoStream(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null

        // 1. Primary: InnerTube ANDROID_VR player
        try {
            val vrUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID_VR",
                clientVersion = "1.56.21",
                userAgent = "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36",
                isVideo = true
            )
            if (!vrUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Video resuelto exitosamente vía InnerTube ANDROID_VR")
                return@withContext vrUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en InnerTube ANDROID_VR de video para videoId=$videoId", e)
        }

        // 2. Secondary: InnerTube VISIONOS player
        try {
            val visionUrl = queryInnerTube(
                videoId = videoId,
                clientName = "VISIONOS",
                clientVersion = "1.02",
                userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3 Safari/605.1.15",
                isVideo = true
            )
            if (!visionUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Video resuelto exitosamente vía InnerTube VISIONOS")
                return@withContext visionUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en InnerTube VISIONOS de video para videoId=$videoId", e)
        }

        // 3. Tertiary: InnerTube ANDROID player (v21.26.364)
        try {
            val androidUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID",
                clientVersion = "21.26.364",
                userAgent = "com.google.android.youtube/21.26.364 (Linux; U; Android 14)",
                isVideo = true
            )
            if (!androidUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Video resuelto exitosamente vía InnerTube ANDROID")
                return@withContext androidUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en InnerTube ANDROID de video para videoId=$videoId", e)
        }

        // 4. Quaternary: InnerTube IOS player (v21.26.4)
        try {
            val iosUrl = queryInnerTube(
                videoId = videoId,
                clientName = "IOS",
                clientVersion = "21.26.4",
                userAgent = "com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_1_1 like Mac OS X; en_US)",
                deviceModel = "iPhone16,2",
                isVideo = true
            )
            if (!iosUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Video resuelto exitosamente vía InnerTube IOS")
                return@withContext iosUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en InnerTube IOS de video para videoId=$videoId", e)
        }

        // 4. Quaternary: Cobalt High-Speed API Gateway
        try {
            val cobaltUrl = queryCobaltApi(videoId, isVideo = true)
            if (!cobaltUrl.isNullOrBlank()) {
                AppLog.d(TAG, "Video resuelto exitosamente vía Cobalt API")
                return@withContext cobaltUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en Cobalt API de video para videoId=$videoId", e)
        }

        // 5. Quinquenary: Fast Invidious formatStreams (muxed MP4 video+audio)
        for (mirror in invidiousMirrors.take(3)) {
            try {
                val invUrl = "$mirror/api/v1/videos/$videoId"
                val conn = (URL(invUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                }
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(resp)
                    val formats = root.optJSONArray("formatStreams")
                    if (formats != null && formats.length() > 0) {
                        for (i in 0 until formats.length()) {
                            val fmt = formats.getJSONObject(i)
                            val u = fmt.optString("url", "")
                            val container = fmt.optString("container", "")
                            if (u.isNotBlank() && (container.contains("mp4", ignoreCase = true) || u.contains("googlevideo"))) {
                                return@withContext u
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo en mirror Invidious de vídeo $mirror para videoId=$videoId", e)
            }
        }

        // 6. Fallback: NewPipeExtractor
        try {
            initNewPipe()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val extractor = ServiceList.YouTube.getStreamExtractor(watchUrl)
            extractor.fetchPage()
            val videoStreams: List<VideoStream>? = extractor.videoStreams
            if (!videoStreams.isNullOrEmpty()) {
                val mp4Streams = videoStreams.filter { it.format?.name?.contains("MP4", ignoreCase = true) == true }
                val chosen = mp4Streams.firstOrNull { it.resolution?.contains("720") == true }
                    ?: mp4Streams.firstOrNull { it.resolution?.contains("480") == true }
                    ?: mp4Streams.firstOrNull { it.resolution?.contains("360") == true }
                    ?: mp4Streams.firstOrNull()
                val directUrl = chosen?.url
                if (!directUrl.isNullOrBlank()) {
                    return@withContext directUrl
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo al resolver vídeo MP4 en NewPipe para videoId=$videoId", e)
        }

        null
    }

    /**
     * Resolves stream URL via Cobalt distributed microservice API (Bypasses Google 403 blocks)
     */
    fun queryCobaltApi(videoId: String, isVideo: Boolean): String? {
        val targets = cobaltMirrors.ifEmpty {
            listOf(
                "https://api.cobalt.tools",
                "https://cobalt-api.kwiatekm.pl",
                "https://cobalt.api.sciter.io",
                "https://api.wuk.sh"
            )
        }
        val videoUrl = "https://www.youtube.com/watch?v=$videoId"

        for (host in targets) {
            try {
                val cleanHost = host.trimEnd('/')
                val url = URL(cleanHost)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 8000
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                    doOutput = true
                }

                val payload = JSONObject().apply {
                    put("url", videoUrl)
                    if (isVideo) {
                        put("downloadMode", "auto")
                        put("videoQuality", "720")
                    } else {
                        put("downloadMode", "audio")
                        put("audioFormat", "m4a")
                    }
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }

                if (conn.responseCode in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(resp)
                    val status = root.optString("status")
                    var resultUrl = root.optString("url")
                    if (resultUrl.isBlank() && status == "picker") {
                        val picker = root.optJSONArray("picker")
                        if (picker != null && picker.length() > 0) {
                            resultUrl = picker.getJSONObject(0).optString("url")
                        }
                    }
                    if (resultUrl.isNotBlank() && (status == "stream" || status == "tunnel" || status == "redirect" || status == "success" || status == "picker")) {
                        AppLog.d(TAG, "Cobalt API exitoso ($host) para videoId=$videoId: ${resultUrl.take(60)}...")
                        return resultUrl
                    }
                }
            } catch (e: Exception) {
                AppLog.d(TAG, "Cobalt mirror $host no disponible: ${e.message}")
            }
        }
        return null
    }

    private fun queryInnerTube(
        videoId: String,
        clientName: String = "ANDROID_VR",
        clientVersion: String = "1.56.21",
        userAgent: String = "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36",
        deviceModel: String? = null,
        isVideo: Boolean = false
    ): String? {
        val url = URL("https://www.youtube.com/youtubei/v1/player")
        val clientNumber = when (clientName) {
            "ANDROID_VR" -> "28"
            "VISIONOS" -> "101"
            "IOS" -> "5"
            "TVHTML5" -> "85"
            "ANDROID" -> "3"
            else -> "1"
        }
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 6000
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("X-YouTube-Client-Name", clientNumber)
            setRequestProperty("X-YouTube-Client-Version", clientVersion)
            setRequestProperty("Origin", "https://www.youtube.com")
            setRequestProperty("Accept", "*/*")
            doOutput = true
        }

        val clientObj = JSONObject().apply {
            put("clientName", clientName)
            put("clientVersion", clientVersion)
            put("hl", "es")
            put("gl", "US")
            when (clientName) {
                "ANDROID_VR" -> {
                    // ANDROID_VR is lightweight and unthrottled
                }
                "VISIONOS" -> {
                    put("deviceMake", "Apple")
                    put("deviceModel", "Vision Pro")
                    put("osName", "visionOS")
                    put("osVersion", "1.02")
                    put("platform", "DESKTOP")
                }
                "IOS" -> {
                    put("deviceMake", "Apple")
                    put("deviceModel", deviceModel ?: "iPhone16,2")
                    put("osName", "iOS")
                    put("osVersion", "18.1.1.22B91")
                    put("platform", "MOBILE")
                }
                "TVHTML5" -> {
                    put("theme", "CLASSIC")
                    put("platform", "TV")
                }
                "ANDROID" -> {
                    put("androidSdkVersion", 34)
                    put("osName", "Android")
                    put("osVersion", "14")
                    put("platform", "MOBILE")
                }
            }
        }

        val payload = JSONObject().apply {
            put("context", JSONObject().apply {
                put("client", clientObj)
                put("user", JSONObject().apply {
                    put("lockedSafetyMode", false)
                })
            })
            put("videoId", videoId)
            put("racyCheckOk", true)
            put("contentCheckOk", true)
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode == 200) {
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(resp)
            val playability = root.optJSONObject("playabilityStatus")
            val status = playability?.optString("status") ?: ""
            if (status != "OK") {
                AppLog.w(TAG, "InnerTube playabilityStatus=$status para videoId=$videoId")
                return null
            }
            val streamingData = root.optJSONObject("streamingData") ?: return null

            // 1. If Video requested, check muxed formats first (contains audio+video in single MP4)
            if (isVideo) {
                val muxedFormats = streamingData.optJSONArray("formats")
                if (muxedFormats != null && muxedFormats.length() > 0) {
                    var bestMuxedUrl: String? = null
                    var bestQualityScore = -1

                    for (i in 0 until muxedFormats.length()) {
                        val fmt = muxedFormats.getJSONObject(i)
                        val streamUrl = fmt.optString("url", "")
                        val quality = fmt.optString("qualityLabel", "")
                        if (streamUrl.isNotBlank()) {
                            val score = when {
                                quality.contains("1080") -> 1080
                                quality.contains("720") -> 720
                                quality.contains("480") -> 480
                                quality.contains("360") -> 360
                                else -> 240
                            }
                            if (score > bestQualityScore) {
                                bestQualityScore = score
                                bestMuxedUrl = streamUrl
                            }
                        }
                    }

                    if (!bestMuxedUrl.isNullOrBlank()) {
                        AppLog.d(TAG, "Stream muxed de video encontrado (qualityScore=$bestQualityScore) para videoId=$videoId")
                        return bestMuxedUrl
                    }
                }
            } else {
                // 2. Audio requested: check adaptiveFormats first for lightweight, fast HQ audio-only streams (M4A / Opus)
                val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats")
                if (adaptiveFormats != null) {
                    var bestAacUrl: String? = null
                    var bestAacBitrate = 0
                    var bestAnyUrl: String? = null
                    var highestBitrate = 0

                    for (i in 0 until adaptiveFormats.length()) {
                        val fmt = adaptiveFormats.getJSONObject(i)
                        val mime = fmt.optString("mimeType", "")
                        val streamUrl = fmt.optString("url", "")
                        val bitrate = fmt.optInt("bitrate", 0)

                        if (mime.contains("audio", ignoreCase = true) && streamUrl.isNotBlank()) {
                            if (mime.contains("mp4", ignoreCase = true) || mime.contains("m4a", ignoreCase = true)) {
                                if (bitrate >= bestAacBitrate) {
                                    bestAacBitrate = bitrate
                                    bestAacUrl = streamUrl
                                }
                            }
                            if (bitrate >= highestBitrate) {
                                highestBitrate = bitrate
                                bestAnyUrl = streamUrl
                            }
                        }
                    }
                    val chosen = bestAacUrl ?: bestAnyUrl
                    if (!chosen.isNullOrBlank()) {
                        AppLog.d(TAG, "Stream de audio puro adaptive encontrado (bitrate=$highestBitrate) para videoId=$videoId")
                        return chosen
                    }
                }

                // Fallback for audio: if no adaptive audio stream, fallback to muxed format
                val muxedFormats = streamingData.optJSONArray("formats")
                if (muxedFormats != null && muxedFormats.length() > 0) {
                    for (i in 0 until muxedFormats.length()) {
                        val fmt = muxedFormats.getJSONObject(i)
                        val streamUrl = fmt.optString("url", "")
                        if (streamUrl.isNotBlank()) {
                            return streamUrl
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Extracts an entire playlist or album of videos from YouTube by playlist ID.
     */
    suspend fun extractPlaylist(playlistId: String): List<OnlineTrack> = withContext(Dispatchers.IO) {
        val cleanId = playlistId.trim().removePrefix("VL")
        if (cleanId.isBlank()) return@withContext emptyList()
        val list = mutableListOf<OnlineTrack>()

        // 1. Primary: InnerTube Browse API
        try {
            val url = URL("https://www.youtube.com/youtubei/v1/browse")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20231201.00.00")
                        put("hl", "es")
                        put("gl", "US")
                    })
                })
                put("browseId", "VL$cleanId")
            }

            conn.outputStream.use { it.write(payload.toString().toByteArray()) }

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)

                fun parseVideoItem(item: JSONObject) {
                    val video = item.optJSONObject("playlistVideoRenderer")
                        ?: item.optJSONObject("musicResponsiveListItemRenderer")
                        ?: if (item.has("videoId")) item else null
                        ?: return

                    val videoId = video.optString("videoId")
                    if (videoId.isBlank()) return

                    val title = video.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: video.optJSONObject("title")?.optString("simpleText")
                        ?: video.optString("title", "Canción")

                    val author = video.optJSONObject("shortBylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: video.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: video.optJSONObject("subtitle")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: "Artista"

                    val lengthSec = video.optString("lengthSeconds").toIntOrNull()
                        ?: run {
                            val durText = video.optJSONObject("lengthText")?.optString("simpleText", "") ?: ""
                            val parts = durText.split(":")
                            when (parts.size) {
                                2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
                                3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
                                else -> 180
                            }
                        }

                    val thumbArray = video.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                    val rawCover = if (thumbArray != null && thumbArray.length() > 0) {
                        thumbArray.optJSONObject(thumbArray.length() - 1)?.optString("url") ?: ""
                    } else {
                        ""
                    }
                    val coverUrl = when {
                        rawCover.startsWith("//") -> "https:$rawCover"
                        rawCover.startsWith("http") -> rawCover
                        else -> "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                    }

                    list.add(
                        OnlineTrack(
                            id = "yt_$videoId",
                            title = title,
                            artist = author,
                            album = "Álbum Completo",
                            durationSec = lengthSec,
                            audioUrl = "https://www.youtube.com/watch?v=$videoId",
                            coverUrl = coverUrl,
                            format = "HQ Audio",
                            bitrateKbps = 320,
                            license = "YouTube",
                            source = "YouTube",
                            isDownloadable = true
                        )
                    )
                }

                fun findVideoRenderers(node: Any?) {
                    when (node) {
                        is JSONObject -> {
                            if (node.has("playlistVideoRenderer") || node.has("musicResponsiveListItemRenderer")) {
                                parseVideoItem(node)
                            } else {
                                val keys = node.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    findVideoRenderers(node.opt(key))
                                }
                            }
                        }
                        is org.json.JSONArray -> {
                            for (i in 0 until node.length()) {
                                findVideoRenderers(node.opt(i))
                            }
                        }
                    }
                }

                findVideoRenderers(root)
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Fallo al extraer playlist $playlistId vía InnerTube", e)
        }

        if (list.isNotEmpty()) {
            return@withContext list.distinctBy { it.id }
        }

        // 2. Fallback: Invidious dynamic mirror playlist endpoints
        for (mirror in invidiousMirrors) {
            try {
                val invUrl = "$mirror/api/v1/playlists/$cleanId"
                val conn = (URL(invUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(resp)
                    val videos = root.optJSONArray("videos")
                    if (videos != null && videos.length() > 0) {
                        for (i in 0 until videos.length()) {
                            val v = videos.getJSONObject(i)
                            val videoId = v.optString("videoId")
                            if (videoId.isNotBlank()) {
                                list.add(
                                    OnlineTrack(
                                        id = "yt_$videoId",
                                        title = v.optString("title", "Canción"),
                                        artist = v.optString("author", "Artista"),
                                        album = root.optString("title", "Álbum Completo"),
                                        durationSec = v.optInt("lengthSeconds", 180),
                                        audioUrl = "https://www.youtube.com/watch?v=$videoId",
                                        coverUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                                        format = "HQ Audio",
                                        bitrateKbps = 320,
                                        license = "YouTube",
                                        source = "YouTube",
                                        isDownloadable = true
                                    )
                                )
                            }
                        }
                        if (list.isNotEmpty()) {
                            return@withContext list.distinctBy { it.id }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo en playlist de mirror Invidious $mirror", e)
            }
        }

        list.distinctBy { it.id }
    }

    /**
     * Extracts metadata for a single YouTube video using InnerTube player API with oEmbed fallback.
     */
    suspend fun extractSingleVideo(videoId: String): OnlineTrack? = withContext(Dispatchers.IO) {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return@withContext null

        // 1. Try InnerTube player endpoint first for full metadata & exact duration
        try {
            val url = URL("https://www.youtube.com/youtubei/v1/player")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36")
                setRequestProperty("X-YouTube-Client-Name", "28")
                setRequestProperty("X-YouTube-Client-Version", "1.56.21")
                doOutput = true
            }
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "ANDROID_VR")
                        put("clientVersion", "1.56.21")
                        put("hl", "es")
                        put("gl", "US")
                    })
                })
                put("videoId", cleanId)
            }
            conn.outputStream.use { it.write(payload.toString().toByteArray()) }
            if (conn.responseCode == 200) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(resp)
                val videoDetails = root.optJSONObject("videoDetails")
                if (videoDetails != null) {
                    val title = videoDetails.optString("title", "Canción YouTube")
                    val author = videoDetails.optString("author", "YouTube")
                    val durationSec = videoDetails.optString("lengthSeconds", "0").toIntOrNull() ?: 0
                    val thumbs = videoDetails.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                    val rawCover = if (thumbs != null && thumbs.length() > 0) {
                        thumbs.optJSONObject(thumbs.length() - 1)?.optString("url") ?: ""
                    } else ""
                    val coverUrl = when {
                        rawCover.startsWith("//") -> "https:$rawCover"
                        rawCover.startsWith("http") -> rawCover
                        else -> "https://i.ytimg.com/vi/$cleanId/hqdefault.jpg"
                    }
                    return@withContext OnlineTrack(
                        id = "yt_$cleanId",
                        title = title,
                        artist = author,
                        album = "YouTube Track",
                        durationSec = durationSec,
                        audioUrl = "https://www.youtube.com/watch?v=$cleanId",
                        coverUrl = coverUrl,
                        format = "HQ Audio",
                        bitrateKbps = 320,
                        license = "YouTube",
                        source = "YouTube",
                        isDownloadable = true
                    )
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo al extraer metadata vía InnerTube para videoId=$cleanId", e)
        }

        // 2. Fallback to oEmbed
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$cleanId&format=json"
            val conn = (URL(oembedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val title = root.optString("title", "Canción YouTube")
                val author = root.optString("author_name", "YouTube")
                val thumb = root.optString("thumbnail_url", "https://i.ytimg.com/vi/$cleanId/hqdefault.jpg")

                return@withContext OnlineTrack(
                    id = "yt_$cleanId",
                    title = title,
                    artist = author,
                    album = "YouTube Track",
                    durationSec = 0,
                    audioUrl = "https://www.youtube.com/watch?v=$cleanId",
                    coverUrl = thumb,
                    format = "HQ Audio",
                    bitrateKbps = 320,
                    license = "YouTube",
                    source = "YouTube",
                    isDownloadable = true
                )
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en oEmbed de YouTube para videoId=$cleanId", e)
        }

        // 3. Fallback without oEmbed
        OnlineTrack(
            id = "yt_$cleanId",
            title = "YouTube Audio",
            artist = "YouTube",
            album = "YouTube Track",
            durationSec = 0,
            audioUrl = "https://www.youtube.com/watch?v=$cleanId",
            coverUrl = "https://i.ytimg.com/vi/$cleanId/hqdefault.jpg",
            format = "HQ Audio",
            bitrateKbps = 320,
            license = "YouTube",
            source = "YouTube",
            isDownloadable = true
        )
    }
}
