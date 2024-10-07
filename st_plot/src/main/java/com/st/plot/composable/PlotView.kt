package com.st.plot.composable

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.plot.PlotViewModel
import com.st.plot.utils.fieldsDesc
import com.st.plot.utils.toPlotEntry
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey10
import com.st.ui.theme.Grey2
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryPink
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import java.util.LinkedList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private val mColors: Array<Color> = arrayOf(
    InfoText,
    ErrorText,
    SuccessText,
    Grey10,
    SecondaryBlue,
    PrimaryPink
)

@Composable
fun PlotView(
    modifier: Modifier,
    feature: Feature<*>,
    viewModel: PlotViewModel,
    featureUpdate: FeatureUpdate<*>?,
    makeSnapShot: Boolean,
    onMakeSnapShotDone: () -> Unit = { /** NOOP **/ },
    showMaxMin: Boolean,
    onSaveSnapshot: (Bitmap) -> Unit = { /** NOOP **/ }
) {
    var mPlot by remember { mutableStateOf<LineChart?>(value = null) }
    val context = LocalContext.current

    var mFirstNotificationTimeStamp by remember {
        mutableLongStateOf(0)
    }

    var legend by remember {
        mutableStateOf<Array<String>>(arrayOf())
    }

    var unit by remember {
        mutableStateOf<Array<String>>(arrayOf())
    }

    val boundary by viewModel.boundary.collectAsStateWithLifecycle()

    var snap by remember { mutableStateOf<Bitmap?>(null) }

    var maxPlotEntry by remember { mutableStateOf(floatArrayOf()) }
    var minPlotEntry by remember { mutableStateOf(floatArrayOf()) }


    Box(
        modifier = modifier
    ) {
        AndroidView(factory = { ctx ->
            LineChart(ctx).also { chart ->
                chart.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mPlot = chart
                initializePlot(chart, context)

                val lineData = featureLegend(feature).mapIndexed { i, name ->
                    buildLineDataSet(name, mColors[i % mColors.size].toArgb())
                }
                mPlot!!.data = LineData(lineData)
            }
        }, update = {
            featureUpdate?.let { update ->
                val plotEntry =
                    update.toPlotEntry(feature = feature, xOffset = mFirstNotificationTimeStamp)

                plotEntry?.let { lastData ->

                    //if we have a number of data different from the number of legends
                    //rebuild the plot
                    if (lastData.y.size != legend.size) {
                        legend = featureLegend(feature)
                        unit = featureUnit(feature)

                        val lineData = legend.mapIndexed { i, name ->
                            buildLineDataSet(name, mColors[i % mColors.size].toArgb())
                        }

                        maxPlotEntry = FloatArray(lineData.size) { Float.MIN_VALUE }
                        minPlotEntry = FloatArray(lineData.size) { Float.MAX_VALUE }

                        mPlot!!.data = LineData(lineData)
                        mFirstNotificationTimeStamp = System.currentTimeMillis()
                    }

                    //Compute the Max
                    for (i in lastData.y.indices) {
                        if (lastData.y[i] > maxPlotEntry[i]) {
                            maxPlotEntry[i] = lastData.y[i]
                        }
                        if (lastData.y[i] < minPlotEntry[i]) {
                            minPlotEntry[i] = lastData.y[i]
                        }
                    }

                    mPlot!!.data?.let { lineData ->
                        lastData.y.forEachIndexed { index, value ->
                            lineData.addEntry(Entry(lastData.x.toFloat(), value), index)
                        }
                        lineData.removeEntryOlderThan(viewModel.secondsToPlot.seconds)
                        lineData.notifyDataChanged()
                        mPlot!!.notifyDataSetChanged()
                        mPlot!!.invalidate()
                    }
                }
            }
        })

        if (showMaxMin) {
            Box(modifier = Modifier
                .alpha(0.8f)
                .clip(Shapes.small)
                .background(Grey2)
                .align(Alignment.BottomEnd)
                .padding(LocalDimensions.current.paddingSmall)) {
                Column {
                    Text(
                        text = "Max:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                    for (i in maxPlotEntry.indices) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = "${legend[i]}: ${maxPlotEntry[i]} ${unit[i]}",
                            color = mColors[i % mColors.size],
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                    Text(
                        text = "Min:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                    for (i in minPlotEntry.indices) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = "${legend[i]}: ${minPlotEntry[i]} ${unit[i]}",
                            color = mColors[i % mColors.size],
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                )
            }
        }
    }

    if (snap != null) {
        AlertDialog(
            modifier = Modifier.alpha(0.9f),
            onDismissRequest = { snap = null },
            dismissButton = {
                BlueMsButton(
                    onClick = {
                        snap = null
                    },
                    text = "Cancel"
                )
            },
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        onSaveSnapshot(snap!!)
                        snap = null
                    },
                    text = "Save"
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = feature.name
                )
            },
            text = {
                Image(bitmap = snap!!.asImageBitmap(), contentDescription = "Snapshot")
            }
        )
    }


    LaunchedEffect(feature) {
        mPlot?.let { plot ->
            legend = featureLegend(feature)
            unit = featureUnit(feature)
            val lineData = legend.mapIndexed { i, name ->
                buildLineDataSet(name, mColors[i % mColors.size].toArgb())
            }

            maxPlotEntry = FloatArray(lineData.size) { Float.MIN_VALUE }
            minPlotEntry = FloatArray(lineData.size) { Float.MAX_VALUE }

            plot.data = LineData(lineData)
            plot.invalidate()
            mFirstNotificationTimeStamp = System.currentTimeMillis()
        }

        mPlot?.let { plot ->
            plot.legend.isEnabled = !showMaxMin
        }
    }

    LaunchedEffect(key1 = showMaxMin) {
        mPlot?.let { plot ->
            plot.legend.isEnabled = !showMaxMin
        }
    }

    LaunchedEffect(key1 = makeSnapShot) {
        if(makeSnapShot) {
            mPlot?.let { plot ->
                snap = plot.getChartBitmap()
            }
            onMakeSnapShotDone()
        }
    }

    LaunchedEffect(boundary) {
            mPlot?.let { plot ->
                val yAxis = plot.axisLeft
                boundary.nLabels?.let {
                    yAxis.labelCount = it
                }

                if (boundary.enableAutoScale) {
                    plot.isAutoScaleMinMaxEnabled = true
                    yAxis.apply {
                        resetAxisMaximum()
                        resetAxisMinimum()
                    }
                    plot.setScaleEnabled(true)
                } else {
                    plot.isAutoScaleMinMaxEnabled = false
                    yAxis.apply {
                        if (boundary.max != null)
                            axisMaximum = boundary.max!!
                        if (boundary.min != null)
                            axisMinimum = boundary.min!!
                    }
                    plot.setScaleEnabled(false)
                }
                plot.invalidate()
            }
    }

}

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

private fun buildLineDataSet(name: String, color: Int): LineDataSet {
    return LineDataSet(LinkedList(), name).apply {
        axisDependency = YAxis.AxisDependency.LEFT
        setDrawCircles(false)
        setDrawValues(false)
        setColor(color)
        setDrawHighlightIndicators(false)
    }
}

private fun initializePlot(chart: LineChart, context: Context) {
    //hide chart description
    chart.description.isEnabled = true
    chart.description.text = ""

    // isEnable touch gestures
    chart.setTouchEnabled(true)

    // isEnable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(true)

    val xl = chart.xAxis
    xl.position = XAxis.XAxisPosition.BOTTOM
    xl.setDrawLabels(false)
    xl.setDrawGridLines(false)
    xl.setAvoidFirstLastClipping(true)

    chart.axisRight.isEnabled = false
    val leftAxis = chart.axisLeft
    leftAxis.setDrawGridLines(true)
    leftAxis.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)

    chart.setNoDataText("Select the feature to plot")
    chart.setNoDataTextColor(ContextCompat.getColor(context, com.st.ui.R.color.colorAccent))
    chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    chart.legend.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)
}

private fun featureLegend(feature: Feature<*>): Array<String> {
    val items = feature.fieldsDesc()
    return items.keys.toTypedArray()
}

private fun featureUnit(feature: Feature<*>): Array<String> {
    val items = feature.fieldsDesc()
    return items.values.toTypedArray()
}
