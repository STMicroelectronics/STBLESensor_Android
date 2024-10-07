package com.st.led_control.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.external.stm32.P2PConfiguration
import com.st.led_control.LedControlViewModel
import com.st.led_control.R
import com.st.ui.theme.Grey6
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedControlDemoContent(
    modifier: Modifier,
    viewModel: LedControlViewModel,
    nodeId: String
) {

    val rssiData by viewModel.rssiData.collectAsStateWithLifecycle()

    val switchData by viewModel.switchData.collectAsStateWithLifecycle()

    val textAlarm by remember(key1 = switchData) {
        derivedStateOf {
            if (switchData.second == null) {
                "No alarm received"
            } else {
                val eventDate = Date().timeToString()
                if (switchData.first.isSwitchPressed.value) {
                    String.format(
                        Locale.getDefault(),
                        "Button pressed: %s { 1 }",
                        eventDate
                    )
                } else {
                    String.format(
                        Locale.getDefault(),
                        "Button pressed: %s { 0 }",
                        eventDate
                    )
                }
            }
        }
    }

    val haptic = LocalHapticFeedback.current


    val deviceId by remember(key1 = switchData) {
        derivedStateOf {
            if (switchData.second == null) {
                null
            } else {
                P2PConfiguration.getDeviceIdById(switchData.first.deviceId.value)
            }
        }
    }

    var currentSwitchValue by remember { mutableStateOf(value = false) }

    val targetColor by remember(key1 = switchData) {
        derivedStateOf {
            if (switchData.second == null) {
                Grey6
            } else {
                if (switchData.first.isSwitchPressed.value) {
                    PrimaryYellow
                } else {
                    InfoText
                }
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageNormal),
                painter = painterResource(R.drawable.stm32wb_signal_strength),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = if (rssiData != null) {
                    "Rssi: ${rssiData!!.rssi} [dBm]"
                } else {
                    "Rssi: --- [dBm]"
                }
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            AnimatedContent(targetState = targetColor, label = "", transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }) { color ->
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageNormal),
                    painter = painterResource(R.drawable.bell_ring),
                    tint = color,
                    contentDescription = null
                )
            }

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = textAlarm
            )

        }

        //For avoiding the  ripple effect when clicking on image
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (deviceId != null) {
                AnimatedContent(targetState = currentSwitchValue, label = "", transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }) { status ->
                    Image(
                        modifier = Modifier
                            .padding(top = LocalDimensions.current.paddingLarge)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentSwitchValue = currentSwitchValue == false
                                viewModel.writeSwitchCommand(
                                    nodeId = nodeId,
                                    mCurrentDevice = deviceId!!,
                                    currentSwitchValue
                                )
                            },
                        painter = painterResource(
                            if (status) {
                                R.drawable.stm32wb_led_on
                            } else {
                                R.drawable.stm32wb_led_off
                            }
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )
                }
            } else {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    text = "Press SW1 button to identify the board"
                )
            }
        }
    }
}

private fun Date.timeToString(): String {
    val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(this)
    return String.format(Locale.getDefault(), "%s", timeStr)
}