package com.st.medical_signal.composable

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.blue_sdk.features.extended.medical_signal.MedicalInfo
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey10
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
import kotlin.time.ExperimentalTime


private val mLineColors: IntArray = intArrayOf(
    InfoText.toArgb(),
    ErrorText.toArgb(),
    SuccessText.toArgb(),
    Grey10.toArgb(),
    SecondaryBlue.toArgb(),
    PrimaryPink.toArgb()
)

@OptIn(ExperimentalTime::class)
@Composable
fun MedicalSignalPlotView(
    modifier: Modifier = Modifier,
    featureUpdate: MedicalInfo?,
    resetZoomTime: Long,
    type: String
) {

    val featureDescription by remember(key1 = featureUpdate) {
        derivedStateOf {
            if (featureUpdate != null) {
                var string = featureUpdate.sigType.value.description
                if (featureUpdate.sigType.value.yMeasurementUnit != null) {
                    string += " [" + featureUpdate.sigType.value.yMeasurementUnit + "]"
                }
                string
            } else {
                ""
            }
        }
    }

    var mPlot by remember { mutableStateOf<LineChart?>(value = null) }
    val context = LocalContext.current

    var prevMedInfo by remember { mutableStateOf<MedicalInfo?>(null) }

    var firstInternalTimeStamp by remember {
        mutableStateOf<Int?>(null)
    }

    var mMedLineData by remember {
        mutableStateOf<LineData?>(null)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {
            Text(text = featureDescription, style = MaterialTheme.typography.titleSmall)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                AndroidView(factory = { ctx ->
                    LineChart(ctx).also { chart ->
                        chart.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        mPlot = chart
                        initializePlot(chart, context)
                        chart.clear()
                    }
                }, update = {
                    featureUpdate?.let { update ->
                        mPlot?.let { plot ->
                            val yAxis = plot.axisLeft
                            if (update.sigType.value.nLabels != 0) {
                                yAxis.labelCount = update.sigType.value.nLabels
                            }

                            plot.setScaleEnabled(update.sigType.value.isAutoscale)
                            if (!update.sigType.value.isAutoscale) {
                                yAxis.apply {
                                    axisMaximum = update.sigType.value.maxGraphValue.toFloat()
                                    axisMinimum = update.sigType.value.minGraphValue.toFloat()
                                }
                            }

                            if (prevMedInfo == null) {
                                //We don't draw nothing... only save data
                                prevMedInfo = update.copy()
                                firstInternalTimeStamp = update.internalTimeStamp.value

                                if (update.sigType.value.numberOfSignals > 1) {
                                    plot.legend.isEnabled = true
                                    val dataSets = ArrayList<ILineDataSet>()
                                    for (i in 0 until update.sigType.value.numberOfSignals) {
                                        val mMedDataSet = LineDataSet(
                                            LinkedList(),
                                            update.sigType.value.signalLabels.getOrNull(i)
                                                ?: "${type}_${i}"
                                        )
                                        //Remove the Circles
                                        mMedDataSet.setDrawCircles(false)

                                        if (update.sigType.value.cubicInterpolation) {
                                            mMedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                                            mMedDataSet.setCubicIntensity(0.2f)
                                        }

                                        mMedDataSet.color = mLineColors[i % mLineColors.size]
                                        dataSets.add(mMedDataSet)

                                    }
                                    mMedLineData = LineData(dataSets)
                                } else {
                                    val mMedDataSet = LineDataSet(LinkedList(), type)
                                    //Remove the Circles
                                    mMedDataSet.setDrawCircles(false)

                                    if (update.sigType.value.cubicInterpolation) {
                                        mMedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                                        mMedDataSet.setCubicIntensity(0.2f)
                                    }

                                    mMedDataSet.color = mLineColors[0]
                                    mMedLineData = LineData(mMedDataSet)
                                }
                                //Show/hide legend
                                plot.legend.isEnabled = update.sigType.value.showLegend
                                plot.legend.horizontalAlignment =
                                    Legend.LegendHorizontalAlignment.RIGHT

                                //Disable the Text Values
                                mMedLineData!!.setDrawValues(false)
                                // add empty data
                                plot.data = mMedLineData
                            } else {
                                //Compute the delta Time respect the previous Sample
                                val timeDiff =
                                    update.internalTimeStamp.value - prevMedInfo!!.internalTimeStamp.value
                                if (timeDiff != 0) { ///????/////
                                    //This is the delta time between Samples
                                    val deltaBetweenSample =
                                        timeDiff * prevMedInfo!!.sigType.value.numberOfSignals / prevMedInfo!!.values.value.size

                                    //Fill the data..
                                    if (prevMedInfo!!.sigType.value.numberOfSignals > 1) {
                                        val dataSets =
                                            prevMedInfo!!.values.value.chunked(prevMedInfo!!.sigType.value.numberOfSignals)
                                        dataSets.forEachIndexed { indexSet, dataSet ->
                                            dataSet.forEachIndexed { index, data ->
                                                mMedLineData!!.addEntry(
                                                    Entry(
                                                        (prevMedInfo!!.internalTimeStamp.value + deltaBetweenSample * indexSet - firstInternalTimeStamp!!).toFloat(),
                                                        data.toFloat()
                                                    ), index
                                                )
                                            }
                                        }
                                    } else {
                                        prevMedInfo!!.values.value.forEachIndexed { index, data ->
                                            mMedLineData!!.addEntry(
                                                Entry(
                                                    (prevMedInfo!!.internalTimeStamp.value + deltaBetweenSample * index - firstInternalTimeStamp!!).toFloat(),
                                                    data.toFloat()
                                                ), 0
                                            )
                                        }
                                    }

                                    mMedLineData!!.removeEntryOlderThan(prevMedInfo!!.sigType.value.displayWindowTimeSecond.seconds)

                                    mMedLineData!!.notifyDataChanged()
                                    plot.notifyDataSetChanged()
                                    plot.invalidate()

                                    prevMedInfo = update.copy()
                                }
                            }
                        }
                    }
                })

                LaunchedEffect(key1 = resetZoomTime) {
                    if (resetZoomTime != 0L) {
                        mPlot?.let { plot ->
                            plot.fitScreen()
                            plot.invalidate()
                            plot.clear()
                        }
                    }
                    prevMedInfo = null
                    firstInternalTimeStamp = null
                    mMedLineData = null
                }
            }
        }
    }
}

private fun initializePlot(chart: LineChart, context: Context) {
    //hide chart description
    chart.description.isEnabled = false
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

    chart.setNoDataText("waiting sample")
    chart.setNoDataTextColor(ContextCompat.getColor(context, com.st.ui.R.color.colorAccent))
    chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    chart.legend.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)

    chart.description.textColor =
        ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)
    chart.description.textSize = 14f
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