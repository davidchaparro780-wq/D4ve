package com.auraplayer.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlphabetFastScroller(
    letters: List<Char> = listOf('#') + ('A'..'Z').toList(),
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var columnHeight by remember { mutableFloatStateOf(0f) }

    fun updateLetterAt(y: Float) {
        if (columnHeight <= 0f || letters.isEmpty()) return
        val clampedY = y.coerceIn(0f, columnHeight)
        val index = ((clampedY / columnHeight) * letters.size).toInt().coerceIn(0, letters.size - 1)
        val letter = letters[index]
        if (selectedLetter != letter) {
            selectedLetter = letter
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onLetterSelected(letter)
        }
    }

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Holographic Floating Letter Preview Bubble
        AnimatedVisibility(
            visible = selectedLetter != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.padding(end = 40.dp)
        ) {
            selectedLetter?.let { letter ->
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .androidx.compose.ui.draw.shadow(
                            elevation = 14.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFF38BDF8).copy(alpha = 0.55f),
                            ambientColor = Color.Black
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFF7C3AED))
                            )
                        )
                        .androidx.compose.foundation.border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
            }
        }

        // Holographic Slim Alphabet Rail
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF090E1A).copy(alpha = 0.75f))
                .androidx.compose.foundation.border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                )
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(letters) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            updateLetterAt(offset.y)
                        },
                        onDragEnd = {
                            selectedLetter = null
                        },
                        onDragCancel = {
                            selectedLetter = null
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            updateLetterAt(change.position.y)
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            letters.forEach { char ->
                val isCurrent = selectedLetter == char
                Text(
                    text = char.toString(),
                    fontSize = 9.5.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isCurrent) Color(0xFF38BDF8) else Color(0xFF94A3B8).copy(alpha = 0.75f)
                )
            }
        }
    }
}
