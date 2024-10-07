package com.st.electric_charge_variation.composable

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.electric_charge_variation.ElectricChargeVariationViewModel
import com.st.electric_charge_variation.R
import com.st.ui.theme.Grey10
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryPink
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import java.util.LinkedList
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

//Color for Plot Label and axes
private val colorLabel = Grey10.toArgb()

//Color for Limit Lines
private val colorLimit = PrimaryPink.toArgb()

//Color for Lines
private val colorLine = SuccessText.toArgb()

private val SECONDS_TO_PLOT_DEFAULT = 5.seconds

@OptIn(ExperimentalTime::class)
@Composable
fun ElectricChargeVariationDemoContent(
    modifier: Modifier,
    viewModel: ElectricChargeVariationViewModel
) {

    val qvarData by viewModel.qvarData.collectAsStateWithLifecycle()

    var mQVARChart by remember { mutableStateOf<LineChart?>(value = null) }
    var mDQVARChart by remember { mutableStateOf<LineChart?>(value = null) }

    var mQVARData by remember { mutableStateOf<LineData?>(value = null) }
    var mDQVARData by remember { mutableStateOf<LineData?>(value = null) }

    var mFirstNotificationTimeStamp by remember {
        mutableLongStateOf(0)
    }

    var mQVARYMax by remember {
        mutableFloatStateOf(Float.MIN_VALUE)
    }

    var mQVARYMin by remember {
        mutableFloatStateOf(Float.MAX_VALUE)
    }

    var mDQVARYMax by remember {
        mutableFloatStateOf(Float.MIN_VALUE)
    }

    var mDQVARYMin by remember {
        mutableFloatStateOf(Float.MAX_VALUE)
    }

    var mQVARLimitLineYMax by remember {
        mutableStateOf(LimitLine(0f, "0"))
    }

    var mQVARLimitLineYMin by remember {
        mutableStateOf(LimitLine(0f, "0"))
    }

    var mDQVARLimitLineYMax by remember {
        mutableStateOf(LimitLine(0f, "0"))
    }

    var mDQVARLimitLineYMin by remember {
        mutableStateOf(LimitLine(0f, "0"))
    }

    val context = LocalContext.current

    qvarData?.let { qvar ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
                    .weight(3f),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                AndroidView(factory = { ctx ->
                    mFirstNotificationTimeStamp = System.currentTimeMillis()
                    LineChart(ctx).also { chart ->
                        chart.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        mQVARChart = chart
                        mQVARData = setUpChart(
                            chart,
                            context,
                            "QVAR",
                            mQVARLimitLineYMax,
                            mQVARLimitLineYMin
                        )
                    }
                }, update = {
                    mQVARChart?.let { chart ->
                        mQVARData?.let { data ->
                            data.addEntry(
                                Entry(
                                    (System.currentTimeMillis() - mFirstNotificationTimeStamp).toFloat(),
                                    qvar.qvar.value.toFloat()
                                ), 0
                            )
                            data.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                            //Because we have only one dataSet....
                            val dataSet = data.dataSets.first()

                            val yMax = dataSet.yMax
                            val yMin = dataSet.yMin

                            //Update the LimitLine for MaxY value
                            if (yMax != mQVARYMax) {
                                mQVARYMax = yMax
                                chart.axisLeft.removeLimitLine(mQVARLimitLineYMax)
                                mQVARLimitLineYMax = LimitLine(yMax, yMax.toString())
                                mQVARLimitLineYMax.lineColor = colorLimit
                                mQVARLimitLineYMax.textColor = colorLabel
                                mQVARLimitLineYMax.textSize = 14f
                                chart.axisLeft.addLimitLine(mQVARLimitLineYMax)
                            }

                            //Update the LimitLine for MinY value
                            if (yMin != mQVARYMin) {
                                mQVARYMin = yMin
                                chart.axisLeft.removeLimitLine(mQVARLimitLineYMin)
                                mQVARLimitLineYMin = LimitLine(yMin, yMin.toString())
                                mQVARLimitLineYMin.lineColor = colorLimit
                                mQVARLimitLineYMin.textColor = colorLabel
                                mQVARLimitLineYMin.textSize = 14f
                                mQVARLimitLineYMin.labelPosition =
                                    LimitLine.LimitLabelPosition.LEFT_BOTTOM
                                chart.axisLeft.addLimitLine(mQVARLimitLineYMin)
                            }

                            data.notifyDataChanged()
                            chart.notifyDataSetChanged()
                            chart.invalidate()
                        }
                    }
                })
            }

            qvar.flag.value?.let { flag ->
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .weight(1f),
                    shape = Shapes.small,
                    shadowElevation = LocalDimensions.current.elevationNormal
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                            painter = painterResource(
                                R.drawable.electric_charge_variation_icon
                            ),
                            tint = PrimaryBlue,
                            contentDescription = null
                        )

                        Text(
                            text = String.format(Locale.getDefault(), "Flag: 0x%X", flag),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            qvar.dqvar.value?.let { dqvar ->
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .weight(3f),
                    shape = Shapes.small,
                    shadowElevation = LocalDimensions.current.elevationNormal
                ) {
                    AndroidView(factory = { ctx ->
                        LineChart(ctx).also { chart ->
                            chart.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            mDQVARChart = chart
                            mDQVARData = setUpChart(
                                chart,
                                context,
                                "DQVAR",
                                mDQVARLimitLineYMax,
                                mDQVARLimitLineYMin
                            )
                        }
                    }, update = {
                        mDQVARChart?.let { chart ->
                            mDQVARData?.let { data ->
                                data.addEntry(
                                    Entry(
                                        (System.currentTimeMillis() - mFirstNotificationTimeStamp).toFloat(),
                                        qvar.qvar.value.toFloat()
                                    ), 0
                                )
                                data.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                                //Because we have only one dataSet....
                                val dataSet = data.dataSets.first()

                                val yMax = dataSet.yMax
                                val yMin = dataSet.yMin

                                //Update the LimitLine for MaxY value
                                if (yMax != mDQVARYMax) {
                                    mDQVARYMax = yMax
                                    chart.axisLeft.removeLimitLine(mDQVARLimitLineYMax)
                                    mDQVARLimitLineYMax = LimitLine(yMax, yMax.toString())
                                    mDQVARLimitLineYMax.lineColor = colorLimit
                                    mDQVARLimitLineYMax.textColor = colorLabel
                                    mDQVARLimitLineYMax.textSize = 14f
                                    chart.axisLeft.addLimitLine(mDQVARLimitLineYMax)
                                }

                                //Update the LimitLine for MinY value
                                if (yMin != mDQVARYMin) {
                                    mDQVARYMin = yMin
                                    chart.axisLeft.removeLimitLine(mDQVARLimitLineYMin)
                                    mDQVARLimitLineYMin = LimitLine(yMin, yMin.toString())
                                    mDQVARLimitLineYMin.lineColor = colorLimit
                                    mDQVARLimitLineYMin.textColor = colorLabel
                                    mDQVARLimitLineYMin.textSize = 14f
                                    mDQVARLimitLineYMin.labelPosition =
                                        LimitLine.LimitLabelPosition.LEFT_BOTTOM
                                    chart.axisLeft.addLimitLine(mDQVARLimitLineYMin)
                                }

                                data.notifyDataChanged()
                                chart.notifyDataSetChanged()
                                chart.invalidate()
                            }
                        }
                    })
                }
            }

            qvar.param.value?.let { param ->
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .weight(1f),
                    shape = Shapes.small,
                    shadowElevation = LocalDimensions.current.elevationNormal
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                            painter = painterResource(
                                R.drawable.electric_charge_variation_icon
                            ),
                            tint = PrimaryBlue,
                            contentDescription = null
                        )

                        Text(
                            text = String.format(Locale.getDefault(), "Parameter: 0x%X", param),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun setUpChart(
    chart: LineChart,
    context: Context,
    name: String,
    lineMax: LimitLine,
    lineMin: LimitLine
): LineData {

    // enable description text
    chart.description.isEnabled = true
    chart.description.text = name
    chart.description.textColor = colorLabel
    chart.description.textSize = 14f

    // disable touch gestures
    chart.setTouchEnabled(false)
    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    chart.setDrawGridBackground(false)
    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false)
    // set an alternative background color
    //chart.setBackgroundColor(Color.LTGRAY)

    val dataSet = LineDataSet(LinkedList(), name)
    //Remove the Circles
    dataSet.setDrawCircles(false)
    dataSet.color = colorLine
    val lineData = LineData(dataSet)
    //Color of the Text Values
    //lineData.setValueTextColor(Color.WHITE)
    //Disable the Text Values
    lineData.setDrawValues(false)
    // add empty data
    chart.data = lineData

    // remove the legend
    chart.legend.isEnabled = false
    // remove the label for x-Axe
    chart.xAxis.setDrawLabels(false)

    //Remove the Y Axe on the Right
    chart.axisRight.isEnabled = false
    //Set the Color for the Y Axe on the Left
    chart.axisLeft.textColor = colorLabel

    //Adding 2 LimitLines for following the Y Max and Min
    lineMax.lineColor = colorLimit
    lineMax.textColor = colorLabel
    lineMax.textSize = 14f
    chart.axisLeft.addLimitLine(lineMax)


    lineMin.lineColor = colorLimit
    lineMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
    lineMin.textColor = colorLabel
    lineMin.textSize = 14f
    chart.axisLeft.addLimitLine(lineMin)

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
