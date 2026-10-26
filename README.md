# 🎵 DaVE Player — Reproductor Multimedia Místico, Potente y Sin Anuncios

[![Android Build](https://github.com/davidchaparro780-wq/D4ve/actions/workflows/build-apk.yml/badge.svg)](https://github.com/davidchaparro780-wq/D4ve/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Ads](https://img.shields.io/badge/Ads-0%20(Sin%20Anuncios)-brightgreen.svg)]()
[![Release](https://img.shields.io/badge/Versi%C3%B3n-4.9.x-8B5CF6.svg)](https://github.com/davidchaparro780-wq/D4ve/releases)

> **DaVE Player** es un reproductor de música y video para Android con una suite visual arcano-misteriosa completa, motor de audio de estudio y descargas online — 100% libre de anuncios.

---

## 📱 Cómo instalarlo

1. Entra a **[Releases](https://github.com/davidchaparro780-wq/D4ve/releases)** desde tu celular.
2. Descarga el **`app-release.apk`** (firmado) o el `app-debug.apk`.
3. Instálalo. La app se **auto-actualiza** sola cuando hay versión nueva (con tu permiso).

---

## ✨ Lo que hace DaVE Player

### 🎧 Audio de estudio
- **Ecualizador de 10 bandas** con presets, Refuerzo de Graves, Espacial 8D, Crossfeed, Normalización de Volumen (ReplayGain) y Realismo de Válvulas.
- **Loop A-B de estudio**, recorte de silencios, crossfade DJ inteligente, BPM workout y mezclador de stems con IA.
- **Modo ZAP DJ**, scrubbing de vinilo, speed/pitch independientes y Consola de Resonancia Arcana (frecuencias Solfeggio con fase lunar real).

### 🌌 Suite Visual Misteriosa
- 7 skins de reproductor: Tornamesa de Vinilo, Cassette, Discman, iPod Classic, Vúmetros Vintage, Monolito Arcano y más.
- Fondos reactivos al FFT real: Phantom Mist, constelaciones estelares, órbitas rúnicas y transiciones de velo.
- 4 temas misteriosos (Mystic Obsidian, Midnight Eclipse, Phantom Specter, Nebula Enigma) que transforman toda la app.

### 🌐 Descubre y Descarga
- Búsqueda y descarga desde YouTube (RYT), TikTok, Instagram, Facebook y estados de WhatsApp.
- Explorar con tendencias por género (Jamendo / Deezer / Archive).
- Extractor de audio desde videos locales.

### 🧰 Herramientas Pro
- **Caja Fuerte** con PIN y biometría, Escudo de Carpetas, limpia-duplicados, editor de tags y carátulas por lotes.
- **Servidor WiFi** con código de invocación de 4 dígitos: controla tu música desde la PC y descarga tu biblioteca de forma segura.
- Party Link (discoteca silenciosa), Karaoke DJ, quiz musical, ruleta, torneo de canciones, Wrapped e Insights.
- Widget de escritorio, Quick Tile, modos Auto y Sueño Etéreo con despedida de niebla.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Kotlin 2.0 + Jetpack Compose (Material 3)
* **Audio:** AndroidX Media3 (ExoPlayer + MediaSessionService) + Visualizer FFT en tiempo real
* **Extracción:** NewPipeExtractor + Cobalt + espejos Invidious (con auto-detección de cambios de API de YouTube)
* **Red:** OkHttp + servidor HTTP propio con token de emparejamiento
* **CI/CD:** GitHub Actions — build de APK firmado y release en cada versión

## 🚀 Publicar una versión nueva

```powershell
.\Publicar_DaVE.ps1 -NuevaVersion "4.9.67" -Changelog "Descripcion del release..."
```
El script hace el bump de versión, pushea, espera el CI, crea el release, lee el tamaño real del APK firmado y actualiza `version.json` (el manifiesto del auto-updater).
