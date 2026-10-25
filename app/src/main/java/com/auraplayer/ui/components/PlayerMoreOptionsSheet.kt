package com.auraplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.AuraHaptic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMoreOptionsSheet(
    isHeadphonesConnected: Boolean,
    isShakeEnabled: Boolean,
    onOpenHeadphones: () -> Unit,
    onOpenCarMode: () -> Unit,
    onOpenStandBy: () -> Unit,
    onToggleShake: () -> Unit,
    onPocketLock: () -> Unit,
    onDeleteSong: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B0F19),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF38BDF8).copy(alpha = 0.55f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Herramientas de Reproducción",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Modos de audio y utilidades de confort",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131B2E).copy(alpha = 0.7f))
                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            ) {
                // 1. Headphones Hub
                PlayerToolItem(
                    icon = Icons.Default.Headphones,
                    iconTint = if (isHeadphonesConnected) Color(0xFF10B981) else Color(0xFF38BDF8),
                    title = "Mis Audífonos & Audio",
                    subtitle = if (isHeadphonesConnected) "Conectado • Perfil de audio activo" else "Ecualizador, perfiles de sonido y efectos",
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                        onOpenHeadphones()
                    }
                )

                OptionDivider()

                // 2. Car Mode
                PlayerToolItem(
                    icon = Icons.Default.DirectionsCar,
                    iconTint = Color(0xFF38BDF8),
                    title = "Modo Conducción / Coche",
                    subtitle = "Interfaz simplificada con botones gigantes para el auto",
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                        onOpenCarMode()
                    }
                )

                OptionDivider()

                // 3. StandBy OLED
                PlayerToolItem(
                    icon = Icons.Default.Bedtime,
                    iconTint = Color(0xFFA855F7),
                    title = "Modo StandBy OLED (AOD)",
                    subtitle = "Reloj de noche minimalista y carátula para pantallas OLED",
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                        onOpenStandBy()
                    }
                )

                OptionDivider()

                // 4. Shake to Skip (con Switch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            AuraHaptic.click(view)
                            onToggleShake()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                (if (isShakeEnabled) Color(0xFF38BDF8) else Color(0xFF64748B)).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = if (isShakeEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agitar para Saltar Pista",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isShakeEnabled) "Activado • Agita el teléfono para cambiar" else "Desactivado • Toca para activar sensor",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isShakeEnabled,
                        onCheckedChange = {
                            AuraHaptic.click(view)
                            onToggleShake()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF38BDF8),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

                OptionDivider()

                // 5. Pocket Lock
                PlayerToolItem(
                    icon = Icons.Default.LockOpen,
                    iconTint = Color(0xFFF59E0B),
                    title = "Bloqueo de Pantalla en Bolsillo",
                    subtitle = "Protege contra pulsaciones accidentales al caminar",
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                        onPocketLock()
                    }
                )

                OptionDivider()

                // 6. Delete Song
                PlayerToolItem(
                    icon = Icons.Outlined.DeleteOutline,
                    iconTint = Color(0xFFEF4444),
                    title = "Eliminar del Teléfono",
                    subtitle = "Borrar permanentemente este archivo de audio",
                    titleColor = Color(0xFFEF4444),
                    onClick = {
                        AuraHaptic.heavy(view)
                        onDismiss()
                        onDeleteSong()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayerToolItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OptionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
    )
}
