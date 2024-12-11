package com.st.neai_anomaly_detection.composable

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.st.blue_sdk.features.extended.neai_anomaly_detection.PhaseType
import com.st.blue_sdk.features.extended.neai_anomaly_detection.StateType
import com.st.blue_sdk.features.extended.neai_anomaly_detection.StatusType
import com.st.neai_anomaly_detection.NeaiAnomalyDetectionViewModel
import com.st.neai_anomaly_detection.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey5
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.WarningText


@Composable
fun NeaiAnomalyDetectionDemoContent(
    modifier: Modifier = Modifier,
    viewModel: NeaiAnomalyDetectionViewModel,
    nodeId: String
) {

    var commandExpanded by remember { mutableStateOf(true) }

    val context = LocalContext.current

    val anomalyDetectionData by viewModel.anomalyDetectionData.collectAsStateWithLifecycle()

    val phase by remember(key1 = anomalyDetectionData) {
        derivedStateOf { if (anomalyDetectionData != null) anomalyDetectionData!!.phase.value else PhaseType.Null }
    }

    var learningDetectingSwitch by remember(key1 = phase){ mutableStateOf(
        (phase == PhaseType.Idle_Trained) || (phase == PhaseType.Detection)) }

    var askIfForceStartCommand by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingMedium)
    ) {
        if ((phase != PhaseType.Learning) && (phase != PhaseType.Detection)) {
            //Static NEAI Logo
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconMedium),
                painter = painterResource(R.drawable.neai_logo),
                tint = PrimaryBlue,
                contentDescription = null
            )
        } else {
            //Dynamic NEAI Logo
            GifImage()
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = "Anomaly Detection"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { commandExpanded = !commandExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconNormal)
                    .rotate(degrees = 90f),
                painter = painterResource(R.drawable.ic_gear_ai),
                tint = PrimaryBlue,
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LocalDimensions.current.paddingNormal)
                    .weight(2f),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                text = "NEAI Commands"
            )
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                painter = if (commandExpanded) {
                    painterResource(R.drawable.ic_arrow_up)
                } else {
                    painterResource(R.drawable.ic_arrow_down)
                },
                tint = PrimaryBlue,
                contentDescription = null
            )
        }

        //Commands Section
        AnimatedVisibility(
            visible = commandExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {

                if (phase == PhaseType.Busy) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningText,
                        text = "Resource busy"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Learning"
                    )

                    Switch(
                        modifier = Modifier.padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                        checked = learningDetectingSwitch, onCheckedChange = {
                            learningDetectingSwitch = !learningDetectingSwitch
                        },
                        enabled = phase != PhaseType.Null,
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedTrackColor = Grey6,
                            disabledUncheckedTrackColor = Grey3
                        )
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Detecting"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {

                    BlueMsButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .padding(start = LocalDimensions.current.paddingNormal),
                        text = "Reset Knowledge",
                        color = WarningText,
                        enabled = phase == PhaseType.Idle_Trained,
                        onClick = {
                            viewModel.writeResetLearningCommand(nodeId)
                            Toast.makeText(context, "Reset DONE.", Toast.LENGTH_SHORT).show()
                        })

                    BlueMsButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .padding(end = LocalDimensions.current.paddingNormal),
                        text = if ((phase != PhaseType.Learning) && (phase != PhaseType.Detection)) "Start" else "Stop",
                        onClick = {
                            if (phase != PhaseType.Busy) {
                                if (phase == PhaseType.Learning ||
                                    phase == PhaseType.Detection
                                ) {
                                    viewModel.writeStopCommand(nodeId)
                                } else {
                                    if (learningDetectingSwitch) {
                                        Toast.makeText(
                                            context,
                                            "Start Detection",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        viewModel.writeDetectionCommand(nodeId)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Start Learning",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        viewModel.writeLearningCommand(nodeId)
                                    }
                                }
                            } else {
                                //if the phase is == Busy... open dialog for asking what to do..
                                askIfForceStartCommand = true
                            }
                        })
                }
            }
        }


        anomalyDetectionData?.let { adData ->
            //AI Engine Section
            HorizontalDivider(
                modifier = Modifier.padding(top = LocalDimensions.current.paddingNormal),
                thickness = 2.dp,
                color = Grey5
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    color = SecondaryBlue,
                    text = "AI Engine"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = "Phase:"
                    )

                    Text(
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                        style = MaterialTheme.typography.bodyMedium,
                        text = fromPhaseToString(adData.phase.value)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = "State:"
                    )

                    Text(
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                        style = MaterialTheme.typography.bodyMedium,
                        text = fromStateToString(adData.state.value)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = "Progress:"
                    )

                    if (adData.phaseProgress.value.toInt() == 255) {
                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            style = MaterialTheme.typography.bodyMedium,
                            text = "---"
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            style = MaterialTheme.typography.bodyMedium,
                            text = "${adData.phaseProgress.value}%"
                        )

                        LinearProgressIndicator(
                            progress = { adData.phaseProgress.value / 100f },
                            modifier = Modifier
                                .weight(2f)
                                .padding(
                                    start = LocalDimensions.current.paddingNormal,
                                    end = LocalDimensions.current.paddingNormal
                                )
                                .height(8.dp),
                            drawStopIndicator = {}
                        )
                    }
                }
            }

            //Result Section
            AnimatedVisibility(
                visible = phase == PhaseType.Detection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                //if (phase == PhaseType.Detection) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = LocalDimensions.current.paddingNormal),
                        thickness = 2.dp,
                        color = Grey5
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryBlue,
                        text = "Results"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = LocalDimensions.current.paddingNormal),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Icon(
                            modifier = Modifier.size(size = LocalDimensions.current.iconSmall),
                            painter = painterResource(
                                if (adData.status.value == StatusType.Anomaly) {
                                    R.drawable.predictive_status_warnings
                                } else {
                                    R.drawable.predictive_status_good
                                }
                            ),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )

                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            style = MaterialTheme.typography.titleMedium,
                            text = "Status:"
                        )

                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            style = MaterialTheme.typography.bodyMedium,
                            text = fromStatusToString(adData.status.value)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = LocalDimensions.current.paddingNormal),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Spacer(modifier = Modifier.width(24.dp))

                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                            style = MaterialTheme.typography.titleMedium,
                            text = "Similarity"
                        )

                        if (adData.similarity.value.toInt() == 255) {
                            Text(
                                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                style = MaterialTheme.typography.bodyMedium,
                                text = "---"
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                style = MaterialTheme.typography.bodyMedium,
                                text = "${adData.similarity.value} %"
                            )

                            LinearProgressIndicator(
                                progress = { adData.similarity.value / 100f },
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(
                                        start = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    )
                                    .height(8.dp),
                                drawStopIndicator = {}
                            )
                        }
                    }
                }
            }
        }
    }

    if (askIfForceStartCommand) {
        AlertDialog(
            onDismissRequest = { askIfForceStartCommand = false },
            dismissButton = {
                BlueMsButton(
                    onClick = {
                        askIfForceStartCommand = false
                    },
                    text = "Cancel"
                )
            },
            confirmButton = {
                BlueMsButton(
                    onClick = {

                        if (learningDetectingSwitch) {

                            Toast.makeText(
                                context,
                                "Start Detection",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.writeDetectionCommand(nodeId)
                        } else {

                            Toast.makeText(
                                context,
                                "Start Learning",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.writeLearningCommand(nodeId)
                        }
                        askIfForceStartCommand = false
                    },
                    text = "OK"
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "WARNING!"
                )
            },
            text = {
                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = "Resources are busy with another process. Do you want to stop it and start NEAI-Anomaly Detection anyway?"
                )

            }
        )
    }
}


private fun fromPhaseToString(phase: PhaseType): String {
    return when (phase) {
        PhaseType.Idle -> "IDLE"
        PhaseType.Learning -> "LEARNING"
        PhaseType.Detection -> "DETECTION"
        PhaseType.Idle_Trained -> "IDLE TRAINED"
        PhaseType.Busy -> "BUSY"
        PhaseType.Null -> "---"
    }
}

private fun fromStateToString(state: StateType): String {
    return when (state) {
        StateType.Ok -> "OK"
        StateType.Init_Not_Called -> "INIT NOT CALLED"
        StateType.Board_Error -> "BOARD ERROR"
        StateType.Knowledge_Error -> "KNOWLEDGE ERROR"
        StateType.Not_Enough_Learning -> "NOT ENOUGH LEARNING"
        StateType.Minimal_Learning_done -> "MINIMAL LEARNING DONE"
        StateType.Unknown_Error -> "UNKNOWN ERROR"
        StateType.Null -> "---"
    }
}

private fun fromStatusToString(status: StatusType): String {
    return when (status) {
        StatusType.Null -> "---"
        StatusType.Anomaly -> "ANOMALY"
        StatusType.Normal -> "NORMAL"
    }
}

@Composable
private fun GifImage(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Icon(
        modifier = modifier.size(size = LocalDimensions.current.iconMedium),
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.neai_logo_white).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        tint = Color.Unspecified,
        contentDescription = null
    )
}

/** ----------------------- PREVIEW --------------------------------------- **/

//@Preview(showBackground = true)
//@Composable
//private fun NeaiAnomalyDetectionDemoContentPreview() {
//    BlueMSTheme {
//        NeaiAnomalyDetectionDemoContent(
//            nodeId = "Test"
//        )
//    }
//}