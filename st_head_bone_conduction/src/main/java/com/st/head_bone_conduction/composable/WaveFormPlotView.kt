package com.st.head_bone_conduction.composable


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.st.head_bone_conduction.utility.BlueMSPlotEntry
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlin.collections.forEach
import kotlin.collections.toMutableList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private val SECONDS_TO_PLOT_DEFAULT = 10.seconds
private const val NUMBER_LINES = 6

@OptIn(ExperimentalTime::class)
@Composable
fun WaveFormPlotView(
    modifier: Modifier = Modifier,
    sample: Short
) {

    var waveData: List<BlueMSPlotEntry> by remember {
        mutableStateOf(
            listOf()
        )
    }

    var maxY by remember { mutableFloatStateOf(-Float.MAX_VALUE) }
    var minY by remember { mutableFloatStateOf(Float.MAX_VALUE) }

    var mFirstNotificationTimeStamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(key1 = sample) {
        val actualTimeStamp = System.currentTimeMillis()
        val localWaveData = waveData.toMutableList()
        localWaveData.add(
            BlueMSPlotEntry(
                actualTimeStamp - mFirstNotificationTimeStamp,
                sample.toFloat()
            )
        )

        waveData = removeEntryOlderThan(
            localWaveData.toList(),
            SECONDS_TO_PLOT_DEFAULT
        )

        //Compute the Max and the Min
        maxY = -Float.MAX_VALUE
        minY = Float.MAX_VALUE
        waveData.forEach { data ->
            val y = data.y
            if (y < minY)
                minY = y
            if (y > maxY)
                maxY = y
        }

        if (maxY == minY) {
            maxY += 1
            minY -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {

        if (waveData.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(2.dp, PrimaryBlue),
                        Shapes.small
                    )
                    .padding(start = 40.dp, top = 20.dp, bottom = 20.dp, end = 10.dp)
            ) {
                val width = size.width
                val height = size.height
                val minValue = if (waveData.size < 2) {
                    2
                } else {
                    waveData.size
                }
                val spacing = width / (minValue - 1)
                if (waveData.isNotEmpty()) {

                    val labelLineSpacing = height / (NUMBER_LINES)

                    // Draw Horizontal Grid Lines
                    for (i in 0..NUMBER_LINES) {

                        val y = height - labelLineSpacing * i

                        val labelValue =
                            minY + ((maxY - minY) / NUMBER_LINES) * i

                        // Draw Grid Lines
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )


                        // Draw Y-Axis Label (Text)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = labelValue.toInt().toString(),
                            topLeft = Offset(-35.dp.toPx(), y - 10.dp.toPx()),
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        )
                    }

                    val path = Path().apply {
                        waveData.forEachIndexed { index, value ->
                            // Calculate X and Y coordinates
                            // We flip Y because (0,0) is the top-left in Canvas
                            val x = index * spacing
                            val y =
                                height - (((value.y - minY) / (maxY - minY)) * height)

                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = SecondaryBlue,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun removeEntryOlderThan(
    list: List<BlueMSPlotEntry>,
    timeRange: Duration?
): List<BlueMSPlotEntry> {
    if (timeRange == null)
        return list
    var xMax = Long.MIN_VALUE
    var xMin = Long.MAX_VALUE

    list.forEach { data ->
        if (data.x > xMax) {
            xMax = data.x
        }
        if (data.x < xMin) {
            xMin = data.x
        }
    }

    val plotRangeMs = (xMax - xMin).toDouble().milliseconds
    if (plotRangeMs > timeRange) {
        val minValidX = (xMax - timeRange.toDouble(DurationUnit.MILLISECONDS)).toFloat()
        val newList = list.filter { it.x >= minValidX }
        return newList
    } else {
        return list
    }
}