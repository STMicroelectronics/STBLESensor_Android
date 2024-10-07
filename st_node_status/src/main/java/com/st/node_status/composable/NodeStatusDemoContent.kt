package com.st.node_status.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.math.MathUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.battery.BatteryStatus
import com.st.node_status.NodeStatusViewModel
import com.st.node_status.R
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.utils.getBlueStBoardImages
import java.util.Locale


private val BATTERY_CHARGING_IMAGES = intArrayOf(
    R.drawable.battery_00c,
    R.drawable.battery_20c,
    R.drawable.battery_40c,
    R.drawable.battery_60c,
    R.drawable.battery_80c,
    R.drawable.battery_100c
)

private val BATTERY_DISCHARGE_IMAGES = intArrayOf(
    R.drawable.battery_00,
    R.drawable.battery_20,
    R.drawable.battery_40,
    R.drawable.battery_60,
    R.drawable.battery_80,
    R.drawable.battery_100
)

@Composable
fun NodeStatusDemoContent(
    modifier: Modifier,
    viewModel: NodeStatusViewModel
) {

    val batteryData by viewModel.batteryData.collectAsStateWithLifecycle()
    val rssiData by viewModel.rssiData.collectAsStateWithLifecycle()
    val batteryCapacity by viewModel.batteryCapacity.collectAsStateWithLifecycle()

    val batteryStatus by remember(key1 = batteryData) {
        derivedStateOf {
            if (batteryData != null) {
                "Status: ${batteryData!!.status.value}"
            } else {
                null
            }
        }
    }
    val batteryPercentage by remember(key1 = batteryData) {
        derivedStateOf {
            if (batteryData != null) String.format(
                Locale.getDefault(),
                "Charge: %.1f %s",
                batteryData!!.percentage.value,
                batteryData!!.percentage.unit
            ) else null
        }
    }
    val batteryVoltage by remember(key1 = batteryData) {
        derivedStateOf {
            if (batteryData != null) String.format(
                Locale.getDefault(),
                "Voltage: %.3f %s",
                batteryData!!.voltage.value,
                batteryData!!.voltage.unit
            ) else null
        }
    }
    val batteryCurrent by remember(key1 = batteryData) {
        derivedStateOf {
            if (batteryData != null) {
                if (batteryData!!.current.value.isNaN() or (batteryData!!.current.value == 0f)) {
                    "Current: not available"
                } else {
                    String.format(
                        Locale.getDefault(),
                        "Current: %.2f %s",
                        batteryData!!.current.value,
                        batteryData!!.current.unit
                    )
                }
            } else {
                null
            }
        }
    }

    val remainingTime by remember(key1 = batteryData, key2 = batteryCapacity) {
        derivedStateOf {
            if (batteryData != null) {
                if (batteryData!!.current.value.isNaN() or (batteryData!!.current.value == 0f) or (batteryData!!.status.value == BatteryStatus.Charging)) {
                    null
                } else {
                    val remainingBattery =
                        batteryCapacity * (batteryData!!.percentage.value / 100.0f)
                    val remainingTime: Float =
                        getRemainingTimeMinutes(remainingBattery, batteryData!!.current.value)
                    String.format(Locale.getDefault(), "Autonomy: %.1f m", remainingTime)
                }
            } else {
                null
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.25f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(getBlueStBoardImages(viewModel.boardType)),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .padding(start = LocalDimensions.current.paddingLarge),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = viewModel.nodeName
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = viewModel.nodeAddress
                    )
                }
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.20f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(R.drawable.signal_strenght),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .padding(start = LocalDimensions.current.paddingLarge),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = if (rssiData != null) {
                            "Rssi: ${rssiData!!.rssi} [dBm]"
                        } else {
                            "Rssi: --- [dBm]"
                        }
                    )
                }
            }
        }

        batteryData?.let {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.imageMedium),
                        painter = painterResource(
                            getBatteryIcon(
                                percentage = batteryData!!.percentage.value,
                                status = batteryData!!.status.value
                            )
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f)
                            .padding(start = LocalDimensions.current.paddingLarge),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.Start
                    ) {
                        batteryPercentage?.let { percentage ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                text = percentage
                            )
                        }

                        batteryStatus?.let { status ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                text = status
                            )
                        }

                        batteryVoltage?.let { voltage ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                text = voltage
                            )
                        }

                        batteryCurrent?.let { current ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                text = current
                            )
                        }

                        remainingTime?.let { time ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                text = time
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun getIconIndex(percentage: Float, nIcons: Int): Int {
    val iconIndex = percentage.toInt() * nIcons / 100
    return MathUtils.clamp(iconIndex, 0, nIcons - 1)
}

@DrawableRes
private fun getBatteryIcon(percentage: Float, status: BatteryStatus): Int {
    val index: Int
    return when (status) {
        BatteryStatus.LowBattery, BatteryStatus.Discharging, BatteryStatus.PluggedNotCharging -> {
            index = getIconIndex(percentage, BATTERY_DISCHARGE_IMAGES.size)
            BATTERY_DISCHARGE_IMAGES[index]
        }

        BatteryStatus.Charging -> {
            index = getIconIndex(percentage, BATTERY_CHARGING_IMAGES.size)
            BATTERY_CHARGING_IMAGES[index]
        }

        BatteryStatus.Unknown, BatteryStatus.Error -> R.drawable.battery_missing
    }
}

private fun getRemainingTimeMinutes(batteryCapacity: Float, current: Float): Float {
    return if (current < 0)
        batteryCapacity / -current * 60
    else
        Float.NaN
}