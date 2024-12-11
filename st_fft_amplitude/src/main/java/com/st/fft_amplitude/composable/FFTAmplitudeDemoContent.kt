package com.st.fft_amplitude.composable

import android.view.ViewGroup
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.fft_amplitude.FFTAmplitudeViewModel
import com.st.fft_amplitude.utilites.LineConf
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.Grey2
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import java.util.Date
import java.util.Locale

@Composable
fun FFTAmplitudeDemoContent(
    modifier: Modifier,
    viewModel: FFTAmplitudeViewModel
) {

    var showDetails by remember { mutableStateOf(false) }

    val loadingStatus by viewModel.loadingStatus.collectAsStateWithLifecycle()

    var mFFTChart by remember { mutableStateOf<LineChart?>(value = null) }

    val context = LocalContext.current

    val mFftData by viewModel.mFftData.collectAsStateWithLifecycle()

    val mFftMax by viewModel.mFftMax.collectAsStateWithLifecycle()

    val mXStats by viewModel.mXStats.collectAsStateWithLifecycle()
    val mYStats by viewModel.mYStats.collectAsStateWithLifecycle()
    val mZStats by viewModel.mZStats.collectAsStateWithLifecycle()

    var snap by remember { mutableStateOf<Bitmap?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("image/png")
    ) { fileUri ->
        fileUri?.let {
            val result = viewModel.saveImage(context, fileUri)
            if (result) {
                Toast.makeText(context, "File Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error Saving File", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AndroidView(factory = { ctx ->
                    LineChart(ctx).also { chart ->
                        chart.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        mFFTChart = chart
                        setUpChart(chart, context)
                    }
                }, update = {
                    mFFTChart?.let { plot ->
                        if (mFftData.isNotEmpty()) {
                            val nComponents = mFftData.size.coerceAtMost(LineConf.LINES.size)
                            val dataSets: MutableList<ILineDataSet> = ArrayList(nComponents)
                            for (i in 0 until nComponents) {
                                val line: LineDataSet = buildDataSet(
                                    LineConf.LINES[i],
                                    mFftData[i], viewModel.mFreqStep
                                )
                                dataSets.add(line)
                            }
                            val lineData = LineData(dataSets)

                            plot.data = lineData
                            plot.invalidate()
                        }
                    }
                })

                if (showDetails) {
                    Box(
                        modifier = Modifier
                            .alpha(0.8f)
                            .clip(Shapes.small)
                            .background(Grey2)
                            .align(Alignment.TopEnd)
                            .animateContentSize()
                            .padding(LocalDimensions.current.paddingSmall)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(all = LocalDimensions.current.paddingNormal),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Frequency Detail info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                lineHeight = 12.sp
                            )
                            if (mFftMax.isNotEmpty()) {
                                if (mFftMax.size > 1) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "X: Max: %.4f @ %.2f Hz",
                                            mFftMax[0].amplitude,
                                            mFftMax[0].frequency
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                                if (mFftMax.size > 2) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "Y: Max: %.4f @ %.2f Hz",
                                            mFftMax[1].amplitude,
                                            mFftMax[1].frequency
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }

                                if (mFftMax.size == 3) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "Z: Max: %.4f @ %.2f Hz",
                                            mFftMax[1].amplitude,
                                            mFftMax[1].frequency
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "Not Available",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }

                            Text(
                                text = "Time Data Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                lineHeight = 12.sp
                            )
                            if ((mXStats == null) && (mYStats == null) && (mZStats == null)) {
                                Text(
                                    text = "Not Available",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            } else {
                                if (mXStats != null) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "X: Acc Peak: %.2f m/s^2\n\t\tRMS Speed %.2f mm/s",
                                            mXStats!!.accPeak,
                                            mXStats!!.rmsSpeed
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }

                                if (mYStats != null) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "Y: Acc Peak: %.2f m/s^2\n\t\tRMS Speed %.2f mm/s",
                                            mYStats!!.accPeak,
                                            mYStats!!.rmsSpeed
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }

                                if (mZStats != null) {
                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "Y: Acc Peak: %.2f m/s^2\n\t\tRMS Speed %.2f mm/s",
                                            mZStats!!.accPeak,
                                            mZStats!!.rmsSpeed
                                        ),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                        )
                    }
                }
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = "Loading Progress:", style = MaterialTheme.typography.titleSmall
        )

        LinearProgressIndicator(modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal
            )
            .height(6.dp),
            progress = { loadingStatus / 100f })

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BlueMsButtonOutlined(text = "Snapshot", onClick = {
                mFFTChart?.let { plot ->
                    snap = plot.getChartBitmap()
                }
            })

            BlueMsButtonOutlined(text = "Details", onClick = { showDetails = !showDetails
            mFFTChart?.let { plot -> changeLegendPosition(plot,showDetails)}})
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
                        viewModel.snap = snap
                        val fileName =
                            "SnapShot_FFT_${Date()}.png".replace(' ', '-')
                        pickFileLauncher.launch(fileName)
                        snap = null
                    },
                    text = "Save"
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "FFT"
                )
            },
            text = {
                Image(bitmap = snap!!.asImageBitmap(), contentDescription = "Snapshot")
            }
        )
    }
}

private fun setUpChart(chart: LineChart, context: Context) {
//hide right axis
    chart.axisRight.isEnabled = false
    //move x axis on the bottom
    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

    //hide plot description
    chart.description.isEnabled = false
    chart.setNoDataText("Data acquisition ongoing…")
    chart.setNoDataTextColor(ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast))
    chart.setTouchEnabled(false)
    val legend = chart.legend
    legend.setDrawInside(true)
    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    legend.orientation = Legend.LegendOrientation.VERTICAL
    chart.axisLeft.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)
    chart.xAxis.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)
    chart.legend.textColor = ContextCompat.getColor(context, com.st.ui.R.color.labelPlotContrast)
}

private fun changeLegendPosition(chart: LineChart, showDetails: Boolean) {
    val legend = chart.legend
    if(showDetails) {
        //legend.setDrawInside(true)
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.yOffset = 10f
    } else {
        //legend.setDrawInside(true)
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.yOffset = 5f
    }
    chart.notifyDataSetChanged()
    chart.invalidate()
}

private fun buildDataSet(
    conf: LineConf,
    yData: FloatArray,
    deltaX: Float
): LineDataSet {
    val data: MutableList<Entry> = java.util.ArrayList(yData.size)
    var x = 0f
    for (y in yData) {
        data.add(Entry(x, y))
        x += deltaX
    }
    val dataSet = LineDataSet(data, conf.name)
    dataSet.setDrawCircles(false)
    dataSet.color = conf.color
    dataSet.setDrawValues(false)
    return dataSet
}

