package com.auraplayer.audio

import kotlin.math.abs

data class LunarPhase(
    val phaseName: String,
    val phaseEmoji: String,
    val illuminationPercent: Int,
    val mysticAuraDescription: String,
    val recommendedFrequencyHz: Int,
    val runeSymbol: String
)

object LunarPhaseManager {

    // Referencia: luna nueva el 6 enero 2000 (UTC) en segundos Unix
    private const val KNOWN_NEW_MOON_EPOCH_S = 947182440L
    private const val SYNODIC_MONTH_S = 29.53058867 * 86400.0

    fun getCurrentPhase(): LunarPhase {
        val nowS = System.currentTimeMillis() / 1000L
        val elapsed = (nowS - KNOWN_NEW_MOON_EPOCH_S).toDouble()
        val cyclePos = ((elapsed % SYNODIC_MONTH_S) + SYNODIC_MONTH_S) % SYNODIC_MONTH_S
        val progress = cyclePos / SYNODIC_MONTH_S // 0.0 -> 1.0

        val illum = (50.0 * (1.0 - kotlin.math.cos(progress * 2.0 * Math.PI))).toInt()
            .coerceIn(0, 100)

        return when {
            progress < 0.0625 -> LunarPhase(
                phaseName = "Luna Nueva",
                phaseEmoji = "○",
                illuminationPercent = illum,
                mysticAuraDescription = "Vacio primordial y silencio acustico profundo",
                recommendedFrequencyHz = 396,
                runeSymbol = "ᚢ"
            )
            progress < 0.1875 -> LunarPhase(
                phaseName = "Luna Creciente",
                phaseEmoji = "◔",
                illuminationPercent = illum,
                mysticAuraDescription = "Ignicion estelar y frecuencias emergentes",
                recommendedFrequencyHz = 528,
                runeSymbol = "ᚱ"
            )
            progress < 0.3125 -> LunarPhase(
                phaseName = "Cuarto Creciente",
                phaseEmoji = "◑",
                illuminationPercent = illum,
                mysticAuraDescription = "Equilibrio ritmico y tension armonica",
                recommendedFrequencyHz = 639,
                runeSymbol = "ᚦ"
            )
            progress < 0.4375 -> LunarPhase(
                phaseName = "Gibosa Iluminada",
                phaseEmoji = "◕",
                illuminationPercent = illum,
                mysticAuraDescription = "Ascension de frecuencias y expansion de campo",
                recommendedFrequencyHz = 741,
                runeSymbol = "ᛏ"
            )
            progress < 0.5625 -> LunarPhase(
                phaseName = "Luna Llena",
                phaseEmoji = "●",
                illuminationPercent = illum,
                mysticAuraDescription = "Plenitud mistica, reverberacion cosmica total",
                recommendedFrequencyHz = 432,
                runeSymbol = "ᛟ"
            )
            progress < 0.6875 -> LunarPhase(
                phaseName = "Gibosa Menguante",
                phaseEmoji = "◕",
                illuminationPercent = illum,
                mysticAuraDescription = "Reflexion introspectiva y armonicos sutiles",
                recommendedFrequencyHz = 852,
                runeSymbol = "ᛈ"
            )
            progress < 0.8125 -> LunarPhase(
                phaseName = "Cuarto Menguante",
                phaseEmoji = "◑",
                illuminationPercent = illum,
                mysticAuraDescription = "Alineacion espectral y purificacion sonica",
                recommendedFrequencyHz = 528,
                runeSymbol = "ᚾ"
            )
            else -> LunarPhase(
                phaseName = "Luna Menguante",
                phaseEmoji = "◔",
                illuminationPercent = illum,
                mysticAuraDescription = "Susurros del eter y relajacion neuronal",
                recommendedFrequencyHz = 396,
                runeSymbol = "ᚨ"
            )
        }
    }
}
