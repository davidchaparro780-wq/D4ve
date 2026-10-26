package com.auraplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Cyberpunk, VIP Luxury & Mysterious Enigmatic Palettes
fun getAuraDarkPalette(accent: String): androidx.compose.material3.ColorScheme {
    val upper = accent.uppercase()

    val (primary, secondary, tertiary, bg, surf, surfVar, onSurf) = when (upper) {
        // --- 🔮 COLORES MISTERIOSOS & ENIGMÁTICOS ---
        "MYSTIC_OBSIDIAN", "MYSTIC", "OBSIDIAN" -> PaletteDefinition(
            primary = Color(0xFFB388FF),      // Electric Amethyst
            secondary = Color(0xFF00E5FF),    // Ethereal Phantom Cyan
            tertiary = Color(0xFF7C4DFF),     // Astral Deep Violet
            background = Color(0xFF05030B),   // Obsidian Void
            surface = Color(0xFF0D0818),      // Dark Amethyst Glass
            surfaceVariant = Color(0xFF19112E), // Mystic Shadow
            onSurface = Color(0xFFF3EEFF)
        )
        "MIDNIGHT_ECLIPSE", "ECLIPSE" -> PaletteDefinition(
            primary = Color(0xFFFF2A55),      // Blood Moon Rose
            secondary = Color(0xFF6366F1),    // Astral Midnight Indigo
            tertiary = Color(0xFF38BDF8),     // Starlight Ice
            background = Color(0xFF04060C),   // Eclipse Void
            surface = Color(0xFF0A0F1D),      // Midnight Slate
            surfaceVariant = Color(0xFF131B32), // Twilight Blue
            onSurface = Color(0xFFF1F5F9)
        )
        "PHANTOM_SPECTER", "SPECTER", "PHANTOM" -> PaletteDefinition(
            primary = Color(0xFF00FF9D),      // Toxic Bioluminescent Green
            secondary = Color(0xFF00E5FF),    // Spectral Ghost Cyan
            tertiary = Color(0xFF7C3AED),     // Phantom Purple
            background = Color(0xFF020706),   // Abyssal Deep Swamp
            surface = Color(0xFF061512),      // Dark Jade
            surfaceVariant = Color(0xFF0C241E), // Phosphorescent Murk
            onSurface = Color(0xFFEEFFF7)
        )
        "NEBULA_ENIGMA", "NEBULA", "ENIGMA" -> PaletteDefinition(
            primary = Color(0xFFE040FB),      // Cosmic Ultraviolet Magenta
            secondary = Color(0xFF00F0FF),    // Deep Nebula Cyan
            tertiary = Color(0xFFFFB300),     // Stardust Amber
            background = Color(0xFF06030F),   // Cosmic Deep Void
            surface = Color(0xFF100724),      // Starlight Dust
            surfaceVariant = Color(0xFF1C0E3D), // Cosmic Dust Violet
            onSurface = Color(0xFFFDF4FF)
        )

        // --- ⚡ NEÓN & CLÁSICOS ---
        "AMOLED", "AMOLED_BLACK" -> PaletteDefinition(
            primary = Color(0xFF38BDF8),
            secondary = Color(0xFFA855F7),
            tertiary = Color(0xFF22C55E),
            background = Color(0xFF000000),
            surface = Color(0xFF090909),
            surfaceVariant = Color(0xFF141414),
            onSurface = Color(0xFFF8FAFC)
        )
        "CYBERPUNK" -> PaletteDefinition(
            primary = Color(0xFF00F0FF),
            secondary = Color(0xFFFF0055),
            tertiary = Color(0xFFFFE500),
            background = Color(0xFF040711),
            surface = Color(0xFF080F21),
            surfaceVariant = Color(0xFF101E3D),
            onSurface = Color(0xFFF0FDF4)
        )
        "SUNSET_GOLD", "GOLD" -> PaletteDefinition(
            primary = Color(0xFFFFD700),
            secondary = Color(0xFFFF9100),
            tertiary = Color(0xFFF59E0B),
            background = Color(0xFF080602),
            surface = Color(0xFF140F05),
            surfaceVariant = Color(0xFF241C0A),
            onSurface = Color(0xFFFFFBEB)
        )
        "CYAN" -> PaletteDefinition(
            primary = Color(0xFF06B6D4),
            secondary = Color(0xFF3B82F6),
            tertiary = Color(0xFFA855F7),
            background = Color(0xFF02070B),
            surface = Color(0xFF05131C),
            surfaceVariant = Color(0xFF0B2130),
            onSurface = Color(0xFFECFEFF)
        )
        "MAGENTA" -> PaletteDefinition(
            primary = Color(0xFFF43F5E),
            secondary = Color(0xFFA855F7),
            tertiary = Color(0xFF06B6D4),
            background = Color(0xFF0A0306),
            surface = Color(0xFF16080E),
            surfaceVariant = Color(0xFF270F1A),
            onSurface = Color(0xFFFFF1F2)
        )
        "GREEN" -> PaletteDefinition(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF06B6D4),
            tertiary = Color(0xFFF59E0B),
            background = Color(0xFF020905),
            surface = Color(0xFF05160D),
            surfaceVariant = Color(0xFF0B2717),
            onSurface = Color(0xFFECFDF5)
        )
        else -> PaletteDefinition( // Velvet Purple default
            primary = Color(0xFFA855F7),
            secondary = Color(0xFFEC4899),
            tertiary = Color(0xFF06B6D4),
            background = Color(0xFF06040C),
            surface = Color(0xFF100B20),
            surfaceVariant = Color(0xFF1B1333),
            onSurface = Color(0xFFFAF5FF)
        )
    }

    return darkColorScheme(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary,
        background = bg,
        surface = surf,
        surfaceVariant = surfVar,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = onSurf,
        onSurface = onSurf,
        onSurfaceVariant = Color(0xFF94A3B8)
    )
}

private data class PaletteDefinition(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color
)

// Acentos canónicos de la Suite Misteriosa: activan la ambientación arcano en toda la app
val MysteriousAccents = setOf(
    "MYSTIC_OBSIDIAN", "MIDNIGHT_ECLIPSE", "PHANTOM_SPECTER", "NEBULA_ENIGMA"
)

fun isMysteriousAccent(accent: String): Boolean = accent.uppercase() in MysteriousAccents

@Composable
fun AuraTheme(
    accent: String = "MYSTIC_OBSIDIAN",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && accent.equals("DYNAMIC", ignoreCase = true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getAuraDarkPalette(accent)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
