package com.olens.launcher.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olens.launcher.core.NativeEngine
import kotlin.math.roundToInt

val ALPHABET_ITEMS = listOf(
    "★", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
    "U", "V", "W", "X", "Y", "Z", "#"
)

@Composable
fun AlphabetScrubber(
    onLetterSelected: (String) -> Unit,
    onScrubStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    enableHaptics: Boolean = true,
    isRightAligned: Boolean = true
) {
    val view = LocalView.current
    var isTouching by remember { mutableStateOf(false) }
    var currentTouchY by remember { mutableFloatStateOf(-1f) }
    var scrubberHeight by remember { mutableFloatStateOf(0f) }
    var lastSelectedIndex by remember { mutableIntStateOf(-1) }

    val totalLetters = ALPHABET_ITEMS.size
    val selectedIndexFloat = if (scrubberHeight > 0 && isTouching && currentTouchY >= 0) {
        ((currentTouchY / scrubberHeight) * totalLetters).coerceIn(0f, (totalLetters - 1).toFloat())
    } else {
        -100f
    }

    // Calculate wave protrusion offsets using Native Rust Engine
    val waveOffsets = remember(selectedIndexFloat, isTouching, scrubberHeight) {
        if (isTouching && selectedIndexFloat >= 0) {
            NativeEngine.calculateWaveOffsets(
                selectedIndex = selectedIndexFloat,
                totalCount = totalLetters,
                waveRadius = 3.8f,
                maxOffsetPx = 52f
            )
        } else {
            FloatArray(totalLetters) { 0f }
        }
    }

    fun handleTouchPosition(y: Float) {
        currentTouchY = y
        if (scrubberHeight > 0) {
            val idx = ((y / scrubberHeight) * totalLetters).toInt().coerceIn(0, totalLetters - 1)
            if (idx != lastSelectedIndex) {
                lastSelectedIndex = idx
                if (enableHaptics) {
                    if (ALPHABET_ITEMS[idx] == "★") {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                onLetterSelected(ALPHABET_ITEMS[idx])
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(54.dp)
            .padding(vertical = 40.dp)
            .onGloballyPositioned { coordinates ->
                scrubberHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isTouching = true
                        onScrubStateChange(true)
                        handleTouchPosition(offset.y)
                        tryAwaitRelease()
                        isTouching = false
                        onScrubStateChange(false)
                        lastSelectedIndex = -1
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isTouching = true
                        onScrubStateChange(true)
                        handleTouchPosition(offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        handleTouchPosition(change.position.y)
                    },
                    onDragEnd = {
                        isTouching = false
                        onScrubStateChange(false)
                        lastSelectedIndex = -1
                    },
                    onDragCancel = {
                        isTouching = false
                        onScrubStateChange(false)
                        lastSelectedIndex = -1
                    }
                )
            },
        contentAlignment = if (isRightAligned) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start
        ) {
            ALPHABET_ITEMS.forEachIndexed { index, letter ->
                val waveOffset = if (waveOffsets.isNotEmpty() && index < waveOffsets.size) {
                    waveOffsets[index]
                } else 0f

                val isClosest = (selectedIndexFloat.roundToInt() == index) && isTouching
                val xOffset = if (isRightAligned) -waveOffset else waveOffset
                val fontSize = if (isClosest) 17.sp else (11.5 + (waveOffset / 10f)).sp
                val fontWeight = if (isClosest) FontWeight.ExtraBold else FontWeight.Medium
                val alpha = if (isTouching) {
                    if (isClosest) 1.0f else (0.45f + (waveOffset / 100f)).coerceAtMost(0.9f)
                } else {
                    0.50f
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .offset { IntOffset(xOffset.roundToInt(), 0) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = if (isClosest) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
                        }
                    )
                }
            }
        }

        // Floating thumb magnifier bubble
        if (isTouching && lastSelectedIndex in ALPHABET_ITEMS.indices) {
            val selectedLetter = ALPHABET_ITEMS[lastSelectedIndex]
            val bubbleX = if (isRightAligned) -72 else 72
            val bubbleY = (currentTouchY - 28f).coerceIn(0f, (scrubberHeight - 56f).coerceAtLeast(0f))

            Box(
                modifier = Modifier
                    .offset { IntOffset(bubbleX.dp.roundToPx(), bubbleY.roundToInt()) }
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

