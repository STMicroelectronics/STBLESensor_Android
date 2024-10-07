package com.st.blue_voice.composable

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.ui.R
import com.st.ui.theme.LocalDimensions
import java.util.LinkedList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private val SECONDS_TO_PLOT_DEFAULT = 20.seconds

@OptIn(ExperimentalTime::class)
@Composable
fun WaveFormPlotView(
    modifier: Modifier = Modifier,
    sample: Short,
    name: String=""
) {

    var wavePlot by remember { mutableStateOf<LineChart?>(value = null) }
    var waveLineData by remember {
        mutableStateOf<LineData?>(null)
    }

    var mFirstNotificationTimeStamp by remember { mutableLongStateOf(0) }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {
        AndroidView(factory = { ctx ->
            LineChart(ctx).also { chart ->
                chart.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                wavePlot = chart
                waveLineData = initializePlot(
                    chart = chart,
                    context = context,
                    name = name,
                    color = ContextCompat.getColor(
                        context,
                        R.color.SuccessText
                    )
                )
                mFirstNotificationTimeStamp = System.currentTimeMillis()
            }
        }, update = {
            wavePlot?.let { plot ->
                waveLineData?.let { lineData ->
                    val actualTimeStamp = System.currentTimeMillis()
                    val yData = sample.toFloat()
                    lineData.addEntry(
                        Entry(
                            (actualTimeStamp - mFirstNotificationTimeStamp).toFloat(),
                            yData
                        ), 0
                    )
                    lineData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                    lineData.notifyDataChanged()
                    plot.notifyDataSetChanged()
                    plot.invalidate()
                }
            }
        })
    }

}

private fun initializePlot(chart: LineChart, context: Context, name: String, color: Int): LineData {
    val lineData: LineData

    // Heart Rate Plot
    // enable description text
    //chart.description.isEnabled = true
    //chart.description.text = "HearRate"
    //chart.description.textColor = colorLabel
    //chart.description.textSize = 14f

    //disable description text
    chart.description.isEnabled = false

    // disable touch gestures
    chart.setTouchEnabled(false)
    // enable scaling and dragging
    chart.isDragEnabled = false
    chart.setScaleEnabled(true)
    chart.setDrawGridBackground(true)
    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false)
    // set an alternative background color
    //chart.setBackgroundColor(ContextCompat.getColor(context, com.st.ui.R.color.Grey10))
    val dataSet = LineDataSet(LinkedList(), name)
    //Disable the Circles
    dataSet.setDrawCircles(false)
    //dataSet.circleColors = listOf(color)
    //Size of the circles (default = 4f)
    //mHeartRateDataSet.circleRadius = 5f

    dataSet.color = color
    //Smooth plot
    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

    lineData = LineData(dataSet)
    //Color of the Text Values
    //mHeartRateData.setValueTextColor(Color.WHITE)
    //Disable the Text Values
    lineData.setDrawValues(false)
    // add empty data
    chart.data = lineData

    // remove the legend
    if(name.isEmpty()) {
        chart.legend.isEnabled = false
    }

    // remove the label for x-Axis
    chart.xAxis.setDrawLabels(false)

    //Remove the Y Axis on the Right
    chart.axisRight.isEnabled = false
    //Set the Color for the Y Axis on the Left
    chart.axisLeft.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)

    //Disable grid lines for x/y Axis
    chart.xAxis.setDrawGridLines(false)
    chart.axisLeft.setDrawGridLines(false)

    //Remove the label on Y axis
    chart.axisLeft.setDrawLabels(false)

    return lineData
}

@ExperimentalTime
private fun LineData.removeEntryOlderThan(timeRange: Duration?) {
    if (timeRange == null)
        return
    val plotRangeMs = (xMax - xMin).toDouble().milliseconds
    if (plotRangeMs > timeRange) {
        val minValidX = (xMax - timeRange.toDouble(DurationUnit.MILLISECONDS)).toFloat()
        dataSets.forEach {
            it.removeXLessThan(minValidX)
        }
    }
}

private fun ILineDataSet.removeXLessThan(value: Float) {
    while (getEntryForIndex(0).x < value) {
        removeFirst()
    }
}