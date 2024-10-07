package com.st.hight_speed_data_log.composable

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.st.high_speed_data_log.R
import com.st.hight_speed_data_log.model.ChartType
import com.st.hight_speed_data_log.model.StreamDataChannel
import java.util.LinkedList
import kotlin.math.sqrt

fun String.getChannelsName(channelNum: Int = 3) =
    when (this) {
        "Mag", "Acc" -> listOf("x", "y", "z")
        "Temp" -> listOf("Temp")
        "Press" -> listOf("Press")
        else -> List(channelNum) { "Channel $it" }
    }

fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    name: String,
    chartType: ChartType,
    label: String,
    maxSize: Int = 300,
    data: List<StreamDataChannel> = emptyList()
) {
    val context = LocalContext.current
    var lineChart: LineChart? by remember { mutableStateOf(value = null) }
    var prevChartType by remember { mutableStateOf(value = chartType) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
        ) {
            Text(
                modifier = Modifier
                    .vertical()
                    .rotate(-90f),
                text = label
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { ctx ->
                    LineChart(ctx).also { chart ->
                        Log.w("LineChart", "factory for $name $chartType")
                        chart.description.text = label
                        chart.description.isEnabled = false

                        chart.setTouchEnabled(true)
                        chart.isDragEnabled = true
                        chart.setScaleEnabled(true)
                        chart.setPinchZoom(true)

                        val xl = chart.xAxis
                        xl.position = XAxis.XAxisPosition.BOTTOM
                        xl.setDrawLabels(false)
                        xl.setDrawGridLines(false)
                        xl.setAvoidFirstLastClipping(true)

                        chart.axisRight.isEnabled = false
                        val leftAxis = chart.axisLeft
                        leftAxis.setDrawGridLines(true)
                        leftAxis.textColor =
                            ContextCompat.getColor(ctx, com.st.ui.R.color.labelPlotContrast)

//                chart.legend.setDrawInside(true)
                        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//                chart.legend.orientation = Legend.LegendOrientation.VERTICAL
                        chart.legend.textColor =
                            ContextCompat.getColor(ctx, com.st.ui.R.color.labelPlotContrast)

                        val mLineColors = ctx.resources.getIntArray(R.array.dataSetColor)

                        val channels = data.firstOrNull()?.data?.size ?: 0
                        chart.data = LineData(
                            List(channels) { i ->
                                val channelsName =
                                    if (i < name.getChannelsName(channels).size) {
                                        name.getChannelsName(channels)[i]
                                    } else {
                                        ""
                                    }

                                LineDataSet(LinkedList(), channelsName).apply {
                                    axisDependency = YAxis.AxisDependency.LEFT
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    setColor(mLineColors[i % mLineColors.size])
                                    setDrawHighlightIndicators(false)
                                }
                            }
                        )

                        chart.isAutoScaleMinMaxEnabled = true

                        lineChart = chart
                    }
                },
                update = {
                    Log.w("LineChart", "update for $name")
                    Log.w("LineChart", "data = ${data}")
                    lineChart?.let { chart ->
                        if (chart.description.text != label || chartType != prevChartType) {
                            prevChartType = chartType
                            chart.description.text = label
                            val mLineColors = context.resources.getIntArray(R.array.dataSetColor)

                            if (chartType == ChartType.SUM) {
                                chart.data = LineData(
                                    listOf(
                                        LineDataSet(LinkedList(), "Modulo").apply {
                                            axisDependency = YAxis.AxisDependency.LEFT
                                            setDrawCircles(false)
                                            setDrawValues(false)
                                            setColor(mLineColors.last())
                                            setDrawHighlightIndicators(false)
                                        }
                                    )
                                )
                            } else {
                                val channels = data.firstOrNull()?.data?.size ?: 0
                                chart.data = LineData(
                                    List(channels) { i ->

                                        val channelsName =
                                            if (i < name.getChannelsName(channels).size) {
                                                name.getChannelsName(channels)[i]
                                            } else {
                                                ""
                                            }

                                        LineDataSet(
                                            LinkedList(),
                                            channelsName
                                        ).apply {
                                            axisDependency = YAxis.AxisDependency.LEFT
                                            setDrawCircles(false)
                                            setDrawValues(false)
                                            setColor(mLineColors[i % mLineColors.size])
                                            setDrawHighlightIndicators(false)
                                        }
                                    }
                                )
                            }

                            chart.resetViewPortOffsets()
                        }

                        chart.data?.let { lineData ->
                            lineData.dataSets.forEachIndexed { channelIndex, channel ->
                                if (chartType == ChartType.SUM) {
                                    val size = channel.entryCount
                                    val xLastEntry =
                                        if (size > maxSize) {
                                            (channel as LineDataSet).values.lastOrNull()?.x?.plus(1)
                                                ?: 0f
                                        } else {
                                            size.toFloat()
                                        }

                                    data.forEachIndexed { i, value ->
                                        if (value.data.size == 3) {
                                            channel.addEntry(
                                                Entry(
                                                    xLastEntry + i.toFloat(),

                                                    sqrt(
                                                        ((value.data[0] * value.data[0]) +
                                                                (value.data[1] * value.data[1]) +
                                                                (value.data[2] * value.data[2])).toDouble()
                                                    ).toFloat()
                                                )
                                            )
                                        }

                                        if (size > maxSize) {
                                            channel.removeFirst()
                                        }
                                    }
                                } else {
                                    if (chartType == ChartType.ALL || chartType.channelIndex == channelIndex) {
                                        val size = channel.entryCount
                                        val xLastEntry =
                                            if (size > maxSize) {
                                                (channel as LineDataSet).values.lastOrNull()?.x?.plus(
                                                    1
                                                )
                                                    ?: 0f
                                            } else {
                                                size.toFloat()
                                            }

                                        data.forEachIndexed { i, value ->
                                            if (channelIndex < value.data.size) {
                                                channel.addEntry(
                                                    Entry(
                                                        xLastEntry + i.toFloat(),
                                                        value.data[channelIndex]
                                                    )
                                                )
                                            }

                                            if (size > maxSize) {
                                                channel.removeFirst()
                                            }
                                        }
                                    }
                                }
                            }

                            lineData.notifyDataChanged()
                            chart.notifyDataSetChanged()
                            chart.invalidate()

                            chart.setVisibleXRangeMaximum(maxSize.toFloat())

                            chart.data.dataSets?.map { it as LineDataSet }?.firstOrNull()?.let {
                                if (it.values.isNotEmpty()) {
                                    val lastIndex = it.entryCount - 1
                                    val lastEntry = it.values[lastIndex]
                                    chart.centerViewToAnimated(
                                        lastEntry.x,
                                        lastEntry.y,
                                        YAxis.AxisDependency.RIGHT,
                                        300L
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            text = "[s]"
        )
    }
}
