package com.st.heart_rate_demo.composable

import android.content.Context
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.heart_rate_demo.HeartRateDemoViewModel
import com.st.heart_rate_demo.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey5
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import java.util.LinkedList
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private val SECONDS_TO_PLOT_DEFAULT = 20.seconds

@OptIn(ExperimentalTime::class)
@Composable
fun HeartRateFragmentDemoContent(
    modifier: Modifier = Modifier,
    viewModel: HeartRateDemoViewModel
) {

    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val heartData by viewModel.heartData.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var heartPlot by remember { mutableStateOf<LineChart?>(value = null) }
    var heartLineData by remember {
        mutableStateOf<LineData?>(null)
    }

    var energyPlot by remember { mutableStateOf<LineChart?>(value = null) }
    var energyLineData by remember {
        mutableStateOf<LineData?>(null)
    }
    var mFirstNotificationTimeStamp by remember { mutableLongStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {
        //Heart Rate Position & Parameter
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    LocalDimensions.current.paddingSmall
                )
            ) {
                BodySensorLocationView(location = locationData.bodySensorLocation.value)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.paddingSmall
                    )
                ) {
                    Text(
                        text = "Skin Contact",
                        style = MaterialTheme.typography.titleSmall
                    )

                    heartData?.let { data ->
                        Row(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (data.skinContactDetected.value) {
                                Icon(
                                    modifier = Modifier
                                        .size(
                                            LocalDimensions.current.iconSmall
                                        ),
                                    painter = painterResource(id = R.drawable.ic_baseline_check_24),
                                    tint = SuccessText,
                                    contentDescription = "Skin Contact"
                                )
                                Text(
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = "Yes"
                                )
                            } else {
                                Icon(
                                    modifier = Modifier
                                        .size(
                                            LocalDimensions.current.iconSmall
                                        ),
                                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                                    tint = ErrorText,
                                    contentDescription = "Skin Contact"
                                )
                                Text(
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = "No"
                                )
                            }
                        }
                    }

                    Text(
                        text = "Position",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                        text = locationData.bodySensorLocation.value.name,
                        style = MaterialTheme.typography.bodySmall
                    )


                    Text(
                        text = "RR Interval",
                        style = MaterialTheme.typography.titleSmall
                    )

                    heartData?.let { data ->
                        Row(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!data.rrInterval.value.isNaN()) {
                                Icon(
                                    modifier = Modifier
                                        .size(
                                            LocalDimensions.current.iconSmall
                                        ),
                                    painter = painterResource(id = R.drawable.ic_baseline_check_24),
                                    tint = SuccessText,
                                    contentDescription = "RR interval"
                                )
                                Text(
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%1.2f %s",
                                        data.rrInterval.value,
                                        data.rrInterval.unit
                                    )
                                )
                            } else {
                                Icon(
                                    modifier = Modifier
                                        .size(
                                            LocalDimensions.current.iconSmall
                                        ),
                                    painter = painterResource(id = R.drawable.ic_baseline_block_24),
                                    tint = ErrorText,
                                    contentDescription = "RR interval"
                                )
                                Text(
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = "Not Supported"
                                )
                            }
                        }
                    }
                }
            }
        }

        //Heart Rate Plot
        heartData?.let { data ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.paddingNormal
                    )
                ) {
                    AnimatedContent(
                        targetState = data.heartRate.value,
                        label = "heart rate Animation"
                    ) { value ->
                        Column(
                            modifier = Modifier.width(LocalDimensions.current.imageNormal),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(LocalDimensions.current.imageNormal),
                                painter = painterResource(id = R.drawable.ic_heart_rate),
                                tint = if (value < 0) {
                                    Grey5
                                } else {
                                    ErrorText
                                },
                                contentDescription = "Heart"
                            )
                            Text(
                                text = if (value < 0) {
                                    ""
                                } else {
                                    String.format(
                                        Locale.getDefault(),
                                        "%d %s",
                                        value,
                                        data.heartRate.unit
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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
                                heartPlot = chart
                                heartLineData = initializePlot(
                                    chart = chart,
                                    context = context,
                                    name = "bpm",
                                    color = ContextCompat.getColor(
                                        context,
                                        com.st.ui.R.color.ErrorText
                                    )
                                )
                                mFirstNotificationTimeStamp = System.currentTimeMillis()
                            }
                        }, update = {
                            if(data.heartRate.value>0) {
                                heartPlot?.let { plot ->
                                    heartLineData?.let { lineData ->
                                        val actualTimeStamp = System.currentTimeMillis()
                                        val yData = data.heartRate.value.toFloat()
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
                            }
                        })
                    }
                }
            }


            //Energy Plot
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.paddingNormal
                    )
                ) {
                    AnimatedContent(
                        targetState = data.energyExpended.value,
                        label = "Energy Animation"
                    ) { value ->
                        Column(
                            modifier = Modifier.width(LocalDimensions.current.imageNormal),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(LocalDimensions.current.imageNormal),
                                painter = painterResource(id = R.drawable.ic_calories),
                                tint = if (value < 0) {
                                    Grey5
                                } else {
                                    InfoText
                                },
                                contentDescription = "Energy"
                            )
                            Text(
                                text = if (value < 0) {
                                    ""
                                } else {
                                    String.format(
                                        Locale.getDefault(),
                                        "%d %s",
                                        value,
                                        data.energyExpended.unit
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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
                                energyPlot = chart
                                energyLineData = initializePlot(
                                    chart = chart,
                                    context = context,
                                    name = "kJ",
                                    color = ContextCompat.getColor(
                                        context,
                                        com.st.ui.R.color.InfoText
                                    )
                                )
                                mFirstNotificationTimeStamp = System.currentTimeMillis()
                            }
                        }, update = {
                            if(data.energyExpended.value>0) {
                                energyPlot?.let { plot ->
                                    energyLineData?.let { lineData ->
                                        val actualTimeStamp = System.currentTimeMillis()
                                        val yData = data.energyExpended.value.toFloat()
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
                            }
                        })
                    }
                }
            }
        }
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
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    chart.setDrawGridBackground(false)
    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false)
    // set an alternative background color
    //chart.setBackgroundColor(Color.LTGRAY)
    val dataSet = LineDataSet(LinkedList(), name)
    //Enable the Circles
    dataSet.setDrawCircles(true)
    dataSet.circleColors = listOf(color)
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
    //chart.legend.isEnabled = false

    // remove the label for x-Axis
    chart.xAxis.setDrawLabels(false)

    //Remove the Y Axis on the Right
    chart.axisRight.isEnabled = false
    //Set the Color for the Y Axis on the Left
    chart.axisLeft.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)

    //Disable grid lines for x/y Axis
    chart.xAxis.setDrawGridLines(false)
    chart.axisLeft.setDrawGridLines(false)

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
