package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.AuraHaptic

data class ThemeOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val isMysterious: Boolean,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color
)

@Composable
fun MysteriousThemeDialog(
    currentAccent: String,
    onAccentChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current
    var selectedAccent by remember(currentAccent) { mutableStateOf(currentAccent) }

    val allThemes = listOf(
        // --- 🔮 TEMAS MISTERIOSOS & ENIGMÁTICOS ---
        ThemeOption(
            key = "MYSTIC_OBSIDIAN",
            title = "Misterio Arcano 🔮",
            subtitle = "Amatista eléctrica, cyan fantasmal y vacío de obsidiana",
            isMysterious = true,
            primaryColor = Color(0xFFB388FF),
            secondaryColor = Color(0xFF00E5FF),
            tertiaryColor = Color(0xFF7C4DFF)
        ),
        ThemeOption(
            key = "MIDNIGHT_ECLIPSE",
            title = "Eclipse Enigmático 🌑",
            subtitle = "Luna de sangre carmesí, índigo cósmico y hielo estelar",
            isMysterious = true,
            primaryColor = Color(0xFFFF2A55),
            secondaryColor = Color(0xFF6366F1),
            tertiaryColor = Color(0xFF38BDF8)
        ),
        ThemeOption(
            key = "PHANTOM_SPECTER",
            title = "Espectro Abisal 👁️",
            subtitle = "Verde bioluminiscente, jade oscuro y neblina fosforescente",
            isMysterious = true,
            primaryColor = Color(0xFF00FF9D),
            secondaryColor = Color(0xFF00E5FF),
            tertiaryColor = Color(0xFF7C3AED)
        ),
        ThemeOption(
            key = "NEBULA_ENIGMA",
            title = "Nebulosa Misteriosa 🌌",
            subtitle = "Magenta ultravioleta, polvo cósmico y resplandor estelar",
            isMysterious = true,
            primaryColor = Color(0xFFE040FB),
            secondaryColor = Color(0xFF00F0FF),
            tertiaryColor = Color(0xFFFFB300)
        ),

        // --- ⚡ NEÓN & CLÁSICOS ---
        ThemeOption(
            key = "AMOLED",
            title = "AMOLED Puro 🖤",
            subtitle = "Negro absoluto para pantallas OLED y acentos neón puros",
            isMysterious = false,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFFA855F7),
            tertiaryColor = Color(0xFF22C55E)
        ),
        ThemeOption(
            key = "CYBERPUNK",
            title = "Cyberpunk Neón ⚡",
            subtitle = "Cyan eléctrico, magenta cálido y atmósfera futurista",
            isMysterious = false,
            primaryColor = Color(0xFF00F0FF),
            secondaryColor = Color(0xFFFF0055),
            tertiaryColor = Color(0xFFFFE500)
        ),
        ThemeOption(
            key = "SUNSET_GOLD",
            title = "Sunset Gold 👑",
            subtitle = "Oro imperial, ámbar cálido y atmósfera de lujo",
            isMysterious = false,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFFF9100),
            tertiaryColor = Color(0xFFF59E0B)
        ),
        ThemeOption(
            key = "PURPLE",
            title = "Velvet Purple 💜",
            subtitle = "Púrpura terciopelo elegante y balanceado",
            isMysterious = false,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFFEC4899),
            tertiaryColor = Color(0xFF06B6D4)
        ),
        ThemeOption(
            key = "CYAN",
            title = "Matrix Cyan 💎",
            subtitle = "Cyan glacial y azul oceánico profundo",
            isMysterious = false,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF3B82F6),
            tertiaryColor = Color(0xFFA855F7)
        ),
        ThemeOption(
            key = "MAGENTA",
            title = "Magenta Rouge 🌸",
            subtitle = "Vibrante flor de neón y rosa profundo",
            isMysterious = false,
            primaryColor = Color(0xFFF43F5E),
            secondaryColor = Color(0xFFA855F7),
            tertiaryColor = Color(0xFF06B6D4)
        ),
        ThemeOption(
            key = "GREEN",
            title = "Emerald Jade 🌲",
            subtitle = "Verde esmeralda y menta energizante",
            isMysterious = false,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF06B6D4),
            tertiaryColor = Color(0xFFF59E0B)
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Atmósfera & Colores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paletas Misteriosas y Neón",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    Text(
                        text = "🔮 PALETAS MISTERIOSAS & ENIGMÁTICAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB388FF),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                items(allThemes.filter { it.isMysterious }) { theme ->
                    val isSelected = selectedAccent.equals(theme.key, ignoreCase = true)
                    ThemeCardItem(
                        theme = theme,
                        isSelected = isSelected,
                        onClick = {
                            selectedAccent = theme.key
                            AuraHaptic.heavy(view)
                            onAccentChange(theme.key)
                            android.widget.Toast.makeText(context, "🔮 Tema activado: ${theme.title}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⚡ NEÓN & CLÁSICOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                items(allThemes.filter { !it.isMysterious }) { theme ->
                    val isSelected = selectedAccent.equals(theme.key, ignoreCase = true)
                    ThemeCardItem(
                        theme = theme,
                        isSelected = isSelected,
                        onClick = {
                            selectedAccent = theme.key
                            AuraHaptic.heavy(view)
                            onAccentChange(theme.key)
                            android.widget.Toast.makeText(context, "⚡ Tema activado: ${theme.title}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Listo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}

@Composable
private fun ThemeCardItem(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(stiffness = 600f),
        label = "theme_card_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(theme.primaryColor, theme.secondaryColor)
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                },
                shape = RoundedCornerShape(16.dp)
            ),
        color = if (isSelected) {
            theme.primaryColor.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Triple Swatch Bubbles
            Row(
                modifier = Modifier
                    .shadow(elevation = if (isSelected) 8.dp else 0.dp, shape = RoundedCornerShape(14.dp), spotColor = theme.primaryColor)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor)
                )
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(theme.secondaryColor)
                )
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(theme.tertiaryColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) theme.primaryColor else Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = theme.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(theme.primaryColor, theme.secondaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seleccionado",
                        tint = Color.Black,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
