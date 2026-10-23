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

                val items = sections?.optJSONObject(0)
                    ?.optJSONObject("itemSectionRenderer")
                    ?.optJSONArray("contents")

                if (items != null) {
                    for (i in 0 until items.length()) {
                        val video = items.getJSONObject(i).optJSONObject("videoRenderer") ?: continue
                        val videoId = video.optString("videoId")
                        if (videoId.isBlank()) continue

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
        } catch (e: Exception) {
            AppLog.e(TAG, "Fallo en la petición de búsqueda de YouTube Music", e)
        }
        list
    }

    private var cobaltMirrors = listOf(
        "https://cobalt-api.kwiatekm.pl",
        "https://api.cobalt.tools",
        "https://cobalt.api.sciter.io"
    )

    private var invidiousMirrors = listOf(
        "https://invidious.nerdvpn.de",
        "https://inv.nadeko.net",
        "https://invidious.tiekoetter.com",
        "https://invidious.f5.si",
        "https://yt.chocolatemoo53.com"
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

        // 1. Primary: InnerTube ANDROID player (Instant ~200ms, direct ratebypass URL, no cipher)
        try {
            val androidUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID",
                clientVersion = "20.10.38",
                userAgent = "com.google.android.youtube/20.10.38 (Linux; U; Android 14) gzip",
                isVideo = false
            )
            if (!androidUrl.isNullOrBlank()) {
                return@withContext androidUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube ANDROID para videoId=$videoId", e)
        }

        // 2. Secondary: InnerTube IOS player
        try {
            val iosUrl = queryInnerTube(
                videoId = videoId,
                clientName = "IOS",
                clientVersion = "20.10.1",
                userAgent = "com.google.ios.youtube/20.10.1 (iPhone16,2; U; CPU iOS 18_2 like Mac OS X; en_US)",
                deviceModel = "iPhone16,2",
                isVideo = false
            )
            if (!iosUrl.isNullOrBlank()) {
                return@withContext iosUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en endpoint InnerTube IOS para videoId=$videoId", e)
        }

        // 3. Fallback: Fast Invidious dynamic mirror (max 2 mirrors, 2.5s timeout)
        for (mirror in invidiousMirrors.take(2)) {
            try {
                val invUrl = "$mirror/api/v1/videos/$videoId"
                val conn = (URL(invUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2500
                    readTimeout = 2500
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0")
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

        // 4. Fallback: NewPipeExtractor
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

        // 1. Primary: InnerTube ANDROID player (Instant ~200ms, direct muxed MP4 720p/360p with ratebypass=yes)
        try {
            val androidUrl = queryInnerTube(
                videoId = videoId,
                clientName = "ANDROID",
                clientVersion = "20.10.38",
                userAgent = "com.google.android.youtube/20.10.38 (Linux; U; Android 14) gzip",
                isVideo = true
            )
            if (!androidUrl.isNullOrBlank()) {
                return@withContext androidUrl
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo en InnerTube ANDROID de vídeo para videoId=$videoId", e)
        }

        // 2. Secondary: Fast Invidious formatStreams (muxed MP4 video+audio)
        for (mirror in invidiousMirrors.take(2)) {
            try {
                val invUrl = "$mirror/api/v1/videos/$videoId"
                val conn = (URL(invUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2500
                    readTimeout = 2500
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0")
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

        // 3. Fallback: NewPipeExtractor
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

    private fun queryInnerTube(
        videoId: String,
        clientName: String = "ANDROID",
        clientVersion: String = "20.10.38",
        userAgent: String = "com.google.android.youtube/20.10.38 (Linux; U; Android 14) gzip",
        deviceModel: String? = null,
        isVideo: Boolean = false
    ): String? {
        val url = URL("https://www.youtube.com/youtubei/v1/player")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", userAgent)
            doOutput = true
        }

        val clientObj = JSONObject().apply {
            put("clientName", clientName)
            put("clientVersion", clientVersion)
            put("hl", "es")
            put("gl", "US")
            if (deviceModel != null) {
                put("deviceModel", deviceModel)
            }
        }

        val payload = JSONObject().apply {
            put("context", JSONObject().apply {
                put("client", clientObj)
            })
            put("videoId", videoId)
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

            // 1. Check muxed formats (formats contains ratebypass=yes streams that download at maximum speed)
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
                    AppLog.d(TAG, "Stream muxed encontrado (qualityScore=$bestQualityScore) para videoId=$videoId")
                    return bestMuxedUrl
                }
            }

            // 2. If video was not requested (audio only) and no muxed format was found, check adaptiveFormats
            if (!isVideo) {
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
                        return chosen
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
     * Extracts metadata for a single YouTube video using official oEmbed.
     */
    suspend fun extractSingleVideo(videoId: String): OnlineTrack? = withContext(Dispatchers.IO) {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return@withContext null

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

        // Fallback without oEmbed
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
