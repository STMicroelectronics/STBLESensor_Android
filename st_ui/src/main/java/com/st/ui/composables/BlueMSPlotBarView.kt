package com.st.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryPink
import com.st.ui.theme.WarningText
import kotlin.collections.any
import kotlin.collections.map
import androidx.compose.foundation.shape.RoundedCornerShape
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.common.Fill

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(2, suffix= "%")

@Composable
fun BlueMSPlotBarView(
    modifier: Modifier = Modifier,
    label: String,
    historyValues: List<Float>,
    currentValue: Float,
    minValue: Float,
    maxValue: Float,
    averageValue: Float
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val positiveColumn =
        rememberLineComponent(
            fill = Fill(PrimaryPink),
            thickness = 8.dp,
            shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
        )

    LaunchedEffect(key1 = historyValues) {
        modelProducer.runTransaction {
            columnModel {
                series(historyValues)
                extras {
                    it[BottomAxisLabelKey] =
                        historyValues.indices.map { index -> "-${index + 1} ${if (index == 0) "Day" else "Days"}" }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall)
    ) {
        CartesianChartHost(
            modifier = modifier
                .weight(2f)
                .padding(LocalDimensions.current.paddingNormal),
            chart = rememberCartesianChart(
                layers = arrayOf(
                    rememberColumnCartesianLayer(
                        rangeProvider = CartesianLayerRangeProvider.fixed(
                            minY = minValue.toDouble(),
                            maxY = maxValue.toDouble()
                        ),
                        columnProvider = remember(positiveColumn) {
                            ColumnCartesianLayer.ColumnProvider.series(positiveColumn)
                        }
                        //dataLabel =  rememberTextComponent()
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberTextComponent(
                        style = MaterialTheme.typography.bodySmall
                    ),
                    guideline = null
                ),
                marker = rememberVicoMarker(valueFormatter = MarkerValueFormatter),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned() },
                    label = rememberAxisLabelComponent(
                        style = MaterialTheme.typography.bodySmall
                    ),
                    valueFormatter = BottomAxisValueFormatter,
                    labelRotationDegrees = -90f,
                    guideline = null
                ),
                decorations = listOf(
                    rememberAverageValueVicoHorizontalLine(
                        averageValue = averageValue,
                    )
                )
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ), thickness = 1.dp, color = Grey3
        )

        AnimatedContent(targetState = historyValues.any { it != 0f }) { targetState ->
            if (targetState) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = LocalDimensions.current.paddingSmall,
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Text(
                        text = "$currentValue",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = LocalDimensions.current.paddingSmall,
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                    textAlign = TextAlign.Center,
                    text = "No Data in this period",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = WarningText
                )
            }
        }
    }
}

@Composable
fun BlueMSSimplePlotBarView(
    modifier: Modifier = Modifier,
    historyValues: List<Float>,
    minValue: Float,
    maxValue: Float,
    showXAxis: Boolean = true,
    showYAxis: Boolean = true,
    animateIn: Boolean = true
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val positiveColumn =
        rememberLineComponent(
            fill =  Fill(PrimaryPink),
            thickness = 8.dp,
            shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
        )

    LaunchedEffect(key1 = historyValues) {
        modelProducer.runTransaction {
            columnModel { series(historyValues) }
        }
    }

    val  animationSpec: AnimationSpec<Float> =  remember {tween(durationMillis = 500)}

    CartesianChartHost(
        modifier = modifier.padding(LocalDimensions.current.paddingNormal),
        chart = rememberCartesianChart(
            layers = arrayOf(
                rememberColumnCartesianLayer(
                    rangeProvider = CartesianLayerRangeProvider.fixed(
                        minY = minValue.toDouble(),
                        maxY = maxValue.toDouble()
                    ),
                    columnProvider = remember(positiveColumn) {
                        ColumnCartesianLayer.ColumnProvider.series(positiveColumn)
                    }
                    //dataLabel =  rememberTextComponent()
                )
            ),
            startAxis = if(showYAxis) VerticalAxis.rememberStart(
                label = rememberTextComponent(
                    style = MaterialTheme.typography.bodySmall
                ),
                guideline = null
            ) else null,
            //marker = rememberVicoMarker(valueFormatter = MarkerValueFormatter),
            bottomAxis = if(showXAxis) HorizontalAxis.rememberBottom(
                itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned() },
                label = rememberAxisLabelComponent(
                    style = MaterialTheme.typography.bodySmall
                ),
                labelRotationDegrees = -90f,
                guideline = null
            ) else null
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        animationSpec = animationSpec,
        initialAnimationSpec = if (animateIn) animationSpec else null,
    )
}