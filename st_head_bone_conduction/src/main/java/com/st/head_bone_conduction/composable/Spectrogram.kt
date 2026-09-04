package com.st.head_bone_conduction.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SpectrogramPlotView(
    modifier: Modifier = Modifier,
    audioData: ShortArray
) {
    // Increase time steps to see a longer history (e.g., 100 or 150)
    val timeSteps = 120
    val spectrogramHistory = remember { mutableStateListOf<FloatArray>() }

    // Use LaunchedEffect to process audio data when it changes
    LaunchedEffect(audioData) {
        if (audioData.isNotEmpty()) {
            val magnitudes = computeSimpleFFT(audioData)
            
            // Add the new magnitudes at the beginning of the history
            spectrogramHistory.add(0, magnitudes)
            
            // Remove the oldest entry to keep the history size fixed
            if (spectrogramHistory.size > timeSteps) {
                spectrogramHistory.removeAt(spectrogramHistory.size - 1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Lighter background (using a very light gray/off-white)
            .background(Color(0xFFF5F5F5))
            .padding(start = 40.dp, top = 8.dp, end = 8.dp, bottom = 8.dp) // Leave space for Y axis
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Y Axis (Frequency Labels)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // Frequency markers for the zoomed view (0 to 2kHz)
            val labels = listOf("0", "0.5k", "1k", "1.5k", "2k")
            labels.forEachIndexed { index, label ->
                val yPos = canvasHeight - (index * (canvasHeight / (labels.size - 1)))
                drawIntoCanvas {
                    it.nativeCanvas.drawText(label, -10f, yPos + (paint.textSize / 3), paint)
                }
            }

            if (spectrogramHistory.isNotEmpty()) {
                // Zoom logic: show only up to 2kHz (1/4 of the 8kHz range)
                val totalBins = spectrogramHistory[0].size
                val visibleBins = totalBins / 4 
                
                val cellWidth = canvasWidth / timeSteps
                val cellHeight = canvasHeight / visibleBins

                // Find the global maximum value to normalize colors dynamically
                var globalMax = 1f
                spectrogramHistory.forEach { step ->
                    step.forEach { mag -> 
                        if (mag > globalMax) globalMax = mag 
                    }
                }

                spectrogramHistory.forEachIndexed { tIndex, magnitudes ->
                    // Draw from right (newest) to left (oldest)
                    val x = canvasWidth - (tIndex * cellWidth) - cellWidth
                    
                    magnitudes.forEachIndexed { fIndex, magnitude ->
                        // Draw only bins within the zoomed range (0 to 2kHz)
                        if (fIndex < visibleBins) {
                            // Low frequencies at the bottom, higher (up to 2k) at the top
                            val y = canvasHeight - (fIndex * cellHeight) - cellHeight
                            
                            val intensity = (magnitude / globalMax).coerceIn(0f, 1f)
                            
                            // "Inferno/Magma" style color mapping
                            val color = Color(
                                red = (intensity * 1.2f).coerceAtMost(1f),
                                green = (intensity * intensity), 
                                blue = 0.3f + (intensity * 0.4f),
                                alpha = 1f
                            )

                            drawRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(cellWidth + 1f, cellHeight + 1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simplified function to calculate FFT magnitude.
 */
private fun computeSimpleFFT(audio: ShortArray): FloatArray {
    val n = audio.size
    val outSize = n / 2
    val magnitudes = FloatArray(outSize)

    for (k in 0 until outSize) {
        var real = 0f
        var imag = 0f
        for (t in 0 until n) {
            val angle = 2.0 * Math.PI * k * t / n
            real += audio[t] * cos(angle).toFloat()
            imag -= audio[t] * sin(angle).toFloat()
        }
        magnitudes[k] = sqrt(real * real + imag * imag)
    }
    return magnitudes
}
