package com.st.smart_motor_control.composable

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.smart_motor_control.R
import com.st.smart_motor_control.SmartMotorControlViewModel.Companion.MOTOR_CONTROLLER_JSON_KEY
import com.st.smart_motor_control.model.MotorControlFault
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.CommandRequest
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningPressed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorControl(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    dataRawPnpLFeature: String? = null,
    isRunning: Boolean = false,
    isLogging: Boolean = false,
    motorSpeed: Int = 1024,
    motorSpeedControl: DtmiContent.DtmiPropertyContent.DtmiIntegerPropertyContent? = null,
    faultStatus: MotorControlFault = MotorControlFault.None,
    temperature: Int? = null,
    speedRef: Int? = null,
    speedMeas: Int? = null,
    busVoltage: Int? = null,
    temperatureUnit: String,
    speedRefUnit: String,
    speedMeasUnit: String,
    busVoltageUnit: String,
    onSendCommand: (String, CommandRequest?) -> Unit = { _, _ -> /** NOOP**/ },
    onValueChange: (String, Pair<String, Any>) -> Unit = { _, _ -> /** NOOP**/ }
) {

    var openSettingMotorSpeedDialog by rememberSaveable { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
            .verticalScroll(
                rememberScrollState()
            ),
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall)
    ) {

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall)
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconMedium)
                        .padding(
                            start = LocalDimensions.current.paddingNormal,
                            top = LocalDimensions.current.paddingNormal
                        ),
                    painter = painterResource(R.drawable.smart_motor_control_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )

                Text(
                    modifier = Modifier
                        .padding(
                            top = LocalDimensions.current.paddingSmall,
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    text = "Motor Information"
                )

                if (!isRunning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = LocalDimensions.current.paddingSmall,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val modId = "modIcon"
                        val textButton = buildAnnotatedString {
                            appendInlineContent(modId, "[icon]")
                            append(" STOPPED")

                        }
                        val inlineContent = mapOf(
                            Pair(
                                // This tells the [CoreText] to replace the placeholder string "[icon]" by
                                // the composable given in the [InlineTextContent] object.
                                modId,
                                InlineTextContent(
                                    // Placeholder tells text layout the expected size and vertical alignment of
                                    // children composable.
                                    Placeholder(
                                        width = 20.sp,
                                        height = 20.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                    )
                                ) {
                                    // This Icon will fill maximum size, which is specified by the [Placeholder]
                                    // above. Notice the width and height in [Placeholder] are specified in TextUnit,
                                    // and are converted into pixel by text layout.

                                    Icon(
                                        modifier = Modifier
                                            .size(size = LocalDimensions.current.iconSmall),
                                        painter = painterResource(R.drawable.ic_close),
                                        tint = ErrorText,
                                        contentDescription = null
                                    )
                                }
                            )
                        )

                        Text(
                            modifier = Modifier
                                .padding(
                                    start = LocalDimensions.current.paddingNormal
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            text = textButton,
                            color = ErrorText,
                            inlineContent = inlineContent
                        )

                        BlueMsButton(
                            text = "START",
                            color = SuccessText,
                            onClick = {
                                val command =
                                    CommandRequest(commandType = "", commandName = "start_motor")

                                onSendCommand(MOTOR_CONTROLLER_JSON_KEY, command)
                            }
                        )
                    }
                } else {
                    when (faultStatus) {
                        MotorControlFault.None -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = LocalDimensions.current.paddingSmall,
                                        start = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                val modId = "modIcon"
                                val textButton = buildAnnotatedString {
                                    appendInlineContent(modId, "[icon]")
                                    append(" RUNNING")

                                }
                                val inlineContent = mapOf(
                                    Pair(
                                        // This tells the [CoreText] to replace the placeholder string "[icon]" by
                                        // the composable given in the [InlineTextContent] object.
                                        modId,
                                        InlineTextContent(
                                            // Placeholder tells text layout the expected size and vertical alignment of
                                            // children composable.
                                            Placeholder(
                                                width = 20.sp,
                                                height = 20.sp,
                                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                            )
                                        ) {
                                            // This Icon will fill maximum size, which is specified by the [Placeholder]
                                            // above. Notice the width and height in [Placeholder] are specified in TextUnit,
                                            // and are converted into pixel by text layout.

                                            Icon(
                                                modifier = Modifier
                                                    .size(size = LocalDimensions.current.iconSmall),
                                                painter = painterResource(R.drawable.ic_no_fault_running),
                                                tint = SuccessText,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                )

                                Text(
                                    modifier = Modifier
                                        .padding(
                                            start = LocalDimensions.current.paddingNormal
                                        ),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = textButton,
                                    color = SuccessText,
                                    inlineContent = inlineContent
                                )

                                BlueMsButton(
                                    text = "STOP",
                                    color = ErrorText,
                                    onClick = {
                                        val command = CommandRequest(
                                            commandType = "",
                                            commandName = "stop_motor"
                                        )
                                        onSendCommand(MOTOR_CONTROLLER_JSON_KEY, command)
                                    }
                                )
                            }

                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                text = "No fault message"
                            )
                        }

                        else -> {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = LocalDimensions.current.paddingSmall,
                                        start = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                val modId = "modIcon"
                                val textButton = buildAnnotatedString {
                                    appendInlineContent(modId, "[icon]")
                                    append(" RUNNING")

                                }
                                val inlineContent = mapOf(
                                    Pair(
                                        // This tells the [CoreText] to replace the placeholder string "[icon]" by
                                        // the composable given in the [InlineTextContent] object.
                                        modId,
                                        InlineTextContent(
                                            // Placeholder tells text layout the expected size and vertical alignment of
                                            // children composable.
                                            Placeholder(
                                                width = 20.sp,
                                                height = 20.sp,
                                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                            )
                                        ) {
                                            // This Icon will fill maximum size, which is specified by the [Placeholder]
                                            // above. Notice the width and height in [Placeholder] are specified in TextUnit,
                                            // and are converted into pixel by text layout.

                                            Icon(
                                                modifier = Modifier
                                                    .size(size = LocalDimensions.current.iconSmall),
                                                painter = painterResource(R.drawable.ic_fault_running),
                                                tint = WarningPressed,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                )

                                Text(
                                    modifier = Modifier
                                        .padding(
                                            start = LocalDimensions.current.paddingNormal
                                        ),
                                    style = MaterialTheme.typography.bodySmall,
                                    text = textButton,
                                    color = WarningPressed,
                                    inlineContent = inlineContent
                                )

                                BlueMsButton(
                                    text = "FAULT ACK",
                                    color = PrimaryYellow,
                                    onClick = {
                                        val command = CommandRequest(
                                            commandType = "",
                                            commandName = "ack_fault"
                                        )
                                        onSendCommand(MOTOR_CONTROLLER_JSON_KEY, command)
                                    }
                                )
                            }

                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                text = buildAnnotatedString {
                                    //append("FAULT ")
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("FAULT: '")
                                        append(faultStatus.getErrorStringFromCode())
                                        append("'")
                                    }
                                    append("\nA problem has been detected, click on '")
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("FAULT ACK")
                                    }
                                    append("' to restart the motor.")
                                },
                                color = WarningPressed
                            )
                        }
                    }
                }


                if ((isRunning) && (faultStatus == MotorControlFault.None)) {
                    Divider(modifier = Modifier.fillMaxWidth())

                    Text(
                        modifier = Modifier
                            .padding(
                                top = LocalDimensions.current.paddingSmall,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        text = "Motor Speed"
                    )

                    var sliderPosition by remember(key1 = motorSpeed) {
                        derivedStateOf {
                            motorSpeed.toFloat()
                        }
                        mutableFloatStateOf(value = motorSpeed.toFloat())
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = LocalDimensions.current.paddingSmall,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            modifier = modifier.fillMaxWidth(),
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            colors = SliderDefaults.colors(
                                thumbColor = SecondaryBlue,
                                activeTrackColor = SecondaryBlue,
                                inactiveTrackColor = Grey6,
                            ),
                            valueRange = if (motorSpeedControl != null) {

                                (motorSpeedControl.minValue?.toFloat()
                                    ?: -4000f)..(motorSpeedControl.maxValue?.toFloat() ?: 4000f)
                            } else {
                                -4000f..4000f
                            },
                            thumb = {
                                SliderLabel(
                                    label = sliderPosition.toInt().toString(),
                                    minWidth = 50.dp
                                )
                            },
                            onValueChangeFinished = {
                                // launch some business logic update with the state you hold
                                openSettingMotorSpeedDialog = true

                                val value: Pair<String, Int> =
                                    Pair("motor_speed", sliderPosition.toInt())
                                onValueChange(MOTOR_CONTROLLER_JSON_KEY, value)
                            }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall)
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = LocalDimensions.current.paddingSmall,
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    text = "Slow Motor Telemetries"
                )


                if (!isRunning && !isLogging) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = LocalDimensions.current.paddingNormal,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        text = buildAnnotatedString {
                            append("To view the data given by the Motor, you must start the acquisition with the ")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Play")
                            }
                            append(" button and enable the motor via the '")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("START")
                            }
                            append("' button")
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = LocalDimensions.current.paddingNormal,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        text = buildAnnotatedString {
                            append("To stop the acquisition you must stop before the motor via the '")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("STOP")
                            }
                            append("' button and stop the acquisition with the ")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Stop")
                            }
                            append(" button")
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (isLogging) {
                    temperature?.let {
                        SlowTelemetry(
                            id = R.drawable.ic_temperature_telemetry,
                            label = "Temperature",
                            value = it,
                            unit = temperatureUnit
                        )
                    }
                    speedRef?.let {
                        SlowTelemetry(
                            id = R.drawable.ic_speed_telemetry,
                            label = "Speed Ref.",
                            value = it,
                            unit = speedRefUnit
                        )
                    }
                    speedMeas?.let {
                        SlowTelemetry(
                            id = R.drawable.ic_speed_telemetry,
                            label = "Speed Meas.",
                            value = it,
                            unit = speedMeasUnit
                        )
                    }
                    busVoltage?.let {
                        SlowTelemetry(
                            id = R.drawable.ic_bus_voltage_telemetry,
                            label = "Bus Voltage",
                            value = it,
                            unit = busVoltageUnit
                        )
                    }

                    if (dataRawPnpLFeature != null) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = LocalDimensions.current.paddingSmall,
                                    start = LocalDimensions.current.paddingNormal,
                                    end = LocalDimensions.current.paddingNormal
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            text = dataRawPnpLFeature
                        )
                    }
                }
            }
        }
    }

    if (openSettingMotorSpeedDialog) {
        SettingMotorSpeedDialog(
            isLoading = isLoading,
            onDismiss = { openSettingMotorSpeedDialog = false }
        )
    }
}

@Composable
fun SettingMotorSpeedDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    if (!isLoading)
        onDismiss()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Column(
                modifier = Modifier
                    .padding(all = LocalDimensions.current.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Motor Speed",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )

                Divider()

                CircularProgressIndicator(
                    Modifier.padding(top = LocalDimensions.current.paddingNormal),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = SecondaryBlue
                )

                Text(
                    modifier = Modifier.padding(top = LocalDimensions.current.paddingNormal),
                    text = "We're currently setting motor speed"
                )
            }
        }
    }
}

@Composable
fun SlowTelemetry(@DrawableRes id: Int, label: String, value: Int, unit: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier
                    .weight(1f, true)
                    .size(size = LocalDimensions.current.iconSmall),
                painter = painterResource(id),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .weight(4f, true)
                    .padding(
                        start = LocalDimensions.current.paddingNormal
                    ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                text = label
            )

            Surface(
                modifier = Modifier
                    .weight(2f, true)
                    .padding(start = LocalDimensions.current.paddingNormal),
                shape = Shapes.small,
                color = Grey3
            ) {

                Text(
                    modifier = Modifier
                        .padding(
                            all = LocalDimensions.current.paddingNormal
                        ),
                    textAlign = TextAlign.Center,
                    text = value.toString()
                )
            }

            Text(
                modifier = Modifier
                    .weight(2f, true)
                    .padding(
                        start = LocalDimensions.current.paddingNormal
                    ),
                text = unit,
                textAlign = TextAlign.Start,
                fontStyle = FontStyle.Italic
            )
        }
        Divider(
            Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            thickness = 1.dp, color = PrimaryBlue
        )
    }
}


@Composable
fun SliderLabel(label: String, minWidth: Dp, modifier: Modifier = Modifier) {
    Surface(
        shape = Shapes.small
    ) {
        Text(
            label,
            textAlign = TextAlign.Center,
            color = Grey0,
            modifier = modifier
                .background(
                    color = SecondaryBlue,
                    shape = Shapes.extraSmall
                )
                .padding(4.dp)
                .defaultMinSize(minWidth = minWidth)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview
@Composable
private fun SettingMotorSpeedDialogPreview() {
    SettingMotorSpeedDialog(
        isLoading = true,
        onDismiss = {}
    )
}

@Preview
@Composable
private fun MotorControlPreviewStopped() {
    PreviewBlueMSTheme {
        MotorControl(
            dataRawPnpLFeature = "Slow telemetries values...",
            isRunning = false,
            faultStatus = MotorControlFault.None,
            temperature = null,
            speedRef = null,
            speedMeas = null,
            busVoltage = null,
            temperatureUnit = "temp",
            speedRefUnit = "speed",
            speedMeasUnit = "speed",
            busVoltageUnit = "voltage"
        )
    }
}

@Preview
@Composable
private fun MotorControlPreviewRunningNoTel() {
    PreviewBlueMSTheme {
        MotorControl(
            dataRawPnpLFeature = null,
            isRunning = true,
            faultStatus = MotorControlFault.None,
            temperature = null,
            speedRef = null,
            speedMeas = null,
            busVoltage = null,
            temperatureUnit = "temp",
            speedRefUnit = "speed",
            speedMeasUnit = "speed",
            busVoltageUnit = "voltage"
        )
    }
}

@Preview
@Composable
private fun MotorControlPreviewRunningTel1() {
    PreviewBlueMSTheme {
        MotorControl(
            dataRawPnpLFeature = null,
            isRunning = true,
            faultStatus = MotorControlFault.None,
            temperature = 120,
            speedRef = 300,
            speedMeas = null,
            busVoltage = null,
            temperatureUnit = "temp",
            speedRefUnit = "speed",
            speedMeasUnit = "speed",
            busVoltageUnit = "voltage"
        )
    }
}

@Preview
@Composable
private fun MotorControlPreviewRunningTel2() {
    PreviewBlueMSTheme {
        MotorControl(
            dataRawPnpLFeature = null,
            isRunning = true,
            faultStatus = MotorControlFault.None,
            temperature = 120,
            speedRef = 300,
            speedMeas = 400,
            busVoltage = 500,
            temperatureUnit = "temp",
            speedRefUnit = "speed",
            speedMeasUnit = "speed",
            busVoltageUnit = "voltage"
        )
    }
}


@Preview
@Composable
private fun MotorControlPreviewRunningFault() {
    PreviewBlueMSTheme {
        MotorControl(
            dataRawPnpLFeature = null,
            isRunning = true,
            faultStatus = MotorControlFault.Duration,
            temperature = null,
            speedRef = null,
            speedMeas = 130,
            busVoltage = null,
            temperatureUnit = "temp",
            speedRefUnit = "speed",
            speedMeasUnit = "speed",
            busVoltageUnit = "voltage"
        )
    }
}
