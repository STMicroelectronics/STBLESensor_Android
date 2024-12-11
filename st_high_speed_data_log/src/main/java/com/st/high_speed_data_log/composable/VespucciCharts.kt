package com.st.high_speed_data_log.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.high_speed_data_log.R
import com.st.high_speed_data_log.ComponentWithInterface
import com.st.high_speed_data_log.model.ChartType
import com.st.high_speed_data_log.model.StreamData
import com.st.ui.composables.ENABLE_PROPERTY_NAME
import com.st.ui.composables.EnumProperty
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue2
import com.st.ui.theme.PrimaryPink
import com.st.ui.theme.SecondaryBlue2
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

@Composable
fun VespucciCharts(
    modifier: Modifier = Modifier,
    status: List<JsonObject>,
    vespucciTags: Map<String, Boolean>,
    streamData: StreamData? = null,
    showTagsEnabled: Boolean = false,
    currentSensorEnabled: String = "",
    sensors: List<ComponentWithInterface> = emptyList(),
    onSensorSelected: (String) -> Unit,
    extraContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    var chartType by remember {
        mutableStateOf(value = ChartType.ALL)
    }

    val channelNumb by remember(key1 = streamData) {
        derivedStateOf {
            streamData?.data?.firstOrNull()?.data?.size ?: 0
        }
    }

    val streamName by remember(key1 = streamData) {
        derivedStateOf {
            streamData?.name ?: ""
        }
    }

    val sensorsEnabled by remember(key1 = status, key2 = sensors) {
        derivedStateOf {
            sensors.filter { s ->
                val data = (status.find { it.containsKey(s.first.name) })?.get(s.first.name)
                var booleanData = false

                s.second.contents
                    .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
                    .find { it.name == ENABLE_PROPERTY_NAME }
                    ?.let { enableProperty ->
                        val defaultData = enableProperty.initValue
                        if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                            booleanData =
                                (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                                    ?: defaultData
                        }
                    }

                return@filter booleanData
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Grey3,
                    shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal)
                )
                .padding(all = LocalDimensions.current.paddingLarge),
        ) {
            extraContent?.let {
                it()

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
            }

            if (showTagsEnabled) {

                Text(
                    fontSize = 12.sp,
                    lineHeight = 17.37.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Current Label"
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal)
                        )
                        .padding(all = LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        color = PrimaryPink,
                        fontWeight = FontWeight.Bold,
                        text = vespucciTags.filter { it.value }.map { it.key }.joinToString(", ")
                    )
                }

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
            }

            EnumProperty(
                modifier = Modifier.background(color = Color.Transparent),
                dropDownColor = Color.White,
                label = "Current Sensor",
                selectedTextColor = PrimaryPink,
                selectedTextFontWeight = FontWeight.Bold,
                data = null,
                initialValue = currentSensorEnabled,
                values = sensorsEnabled.map { it.first.name to it.first.name },
                onValueChange = {
                    onSensorSelected(it)

                    chartType = ChartType.ALL
                }
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        if (channelNumb == 3) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconToggleButton(
                    checked = chartType == ChartType.ALL,
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(end = LocalDimensions.current.paddingSmall),
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContainerColor = PrimaryBlue2,
                        checkedContentColor = Grey0,
                        contentColor = Grey0,
                        containerColor = SecondaryBlue2
                    ),
                    onCheckedChange = {
                        chartType = ChartType.ALL
                    }
                ) {
                    Text(text = stringResource(id = R.string.st_hsdl_datalog_chartToggleAll))
                }
                IconToggleButton(
                    checked = chartType == ChartType.SUM,
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(end = LocalDimensions.current.paddingSmall),
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContainerColor = PrimaryBlue2,
                        checkedContentColor = Grey0,
                        contentColor = Grey0,
                        containerColor = SecondaryBlue2
                    ),
                    onCheckedChange = {
                        chartType = ChartType.SUM
                    }
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        text = stringResource(id = R.string.st_hsdl_datalog_chartToggleSum)
                    )
                }

                streamName.getChannelsName().getOrNull(ChartType.CHANNEL_1.channelIndex)
                    ?.uppercase()?.let {
                        IconToggleButton(
                            checked = chartType == ChartType.CHANNEL_1,
                            modifier = Modifier
                                .weight(weight = 1f)
                                .padding(end = LocalDimensions.current.paddingSmall),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContainerColor = PrimaryBlue2,
                                checkedContentColor = Grey0,
                                contentColor = Grey0,
                                containerColor = SecondaryBlue2
                            ),
                            onCheckedChange = {
                                chartType = ChartType.CHANNEL_1
                            }
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp,
                                text = it
                            )
                        }
                    }
                streamName.getChannelsName().getOrNull(ChartType.CHANNEL_2.channelIndex)
                    ?.uppercase()?.let {
                        IconToggleButton(
                            checked = chartType == ChartType.CHANNEL_2,
                            modifier = Modifier
                                .weight(weight = 1f)
                                .padding(end = LocalDimensions.current.paddingSmall),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContainerColor = PrimaryBlue2,
                                checkedContentColor = Grey0,
                                contentColor = Grey0,
                                containerColor = SecondaryBlue2
                            ),
                            onCheckedChange = {
                                chartType = ChartType.CHANNEL_2
                            }
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp,
                                text = it
                            )
                        }
                    }
                streamName.getChannelsName().getOrNull(ChartType.CHANNEL_3.channelIndex)
                    ?.uppercase()?.let {
                        IconToggleButton(
                            checked = chartType == ChartType.CHANNEL_3,
                            modifier = Modifier.weight(weight = 1f),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContainerColor = PrimaryBlue2,
                                checkedContentColor = Grey0,
                                contentColor = Grey0,
                                containerColor = SecondaryBlue2
                            ),
                            onCheckedChange = {
                                chartType = ChartType.CHANNEL_3
                            }
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp,
                                text = it
                            )
                        }
                    }
            }
        }

        if (streamData == null) {
            if (currentSensorEnabled.isEmpty()) {
                NoStreamData(label = stringResource(id = R.string.st_hsdl_logging_vespucci))
            } else {
                NoStreamData(label = stringResource(id = R.string.st_hsdl_waiting_vespucci))
            }
        } else {
            LineChart(
                name = streamData.name,
                data = streamData.data,
                chartType = chartType,
                label = streamData.uom
            )
        }
    }
}

@Composable
fun NoStreamData(
    modifier: Modifier = Modifier,
    label: String
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center
        )
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}
