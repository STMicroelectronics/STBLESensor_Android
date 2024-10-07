package com.st.neai_extrapolation.composable

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import com.st.blue_sdk.features.extended.neai_extrapolation.model.PhaseType
import com.st.blue_sdk.features.extended.neai_extrapolation.model.StateType
import com.st.neai_extrapolation.NeaiExtrapolationViewModel
import com.st.neai_extrapolation.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey5
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.WarningText


@Composable
fun NeaiExtrapolationDemoContent(
    modifier: Modifier = Modifier,
    viewModel: NeaiExtrapolationViewModel,
    nodeId: String
) {
    val context = LocalContext.current

    val extrapolationData by viewModel.extrapolationData.collectAsStateWithLifecycle()

    var commandExpanded by remember { mutableStateOf(true) }

    var askIfForceStartCommand by remember { mutableStateOf(false) }

    var showDialogForRemovingStub by remember { mutableStateOf(false) }

    val phase by remember(key1 = extrapolationData) {
        derivedStateOf { if (extrapolationData.extrapolation != null) extrapolationData.extrapolation!!.phase else PhaseType.Null }
    }

    var enableStart by remember {
        mutableStateOf(true)
    }

    var enableStop by remember {
        mutableStateOf(false)
    }

    when (phase) {
        PhaseType.Idle -> {
            enableStop = false
            enableStart = true
        }
        PhaseType.Extrapolation -> {
            enableStop = true
            enableStart = false
        }
        PhaseType.Busy -> {
            enableStop = false
            enableStart = true
        }
        PhaseType.Null -> {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingMedium)
    ) {
        if (phase != PhaseType.Extrapolation) {
            //Static NEAI Logo
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconMedium),
                painter = painterResource(R.drawable.neai_icon),
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
            text = "NEAI Extrapolation"
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {

                    BlueMsButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .padding(start = LocalDimensions.current.paddingNormal),
                        text = "Start",
                        enabled = enableStart,
                        onClick = {
                            if (phase != PhaseType.Busy) {
                                viewModel.writeStartCommand(nodeId)
                                Toast.makeText(
                                    context,
                                    "Start Extrapolation",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                //if the phase is == Busy... open dialog for asking what to do..
                                askIfForceStartCommand = true
                            }
                        })

                    BlueMsButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .padding(end = LocalDimensions.current.paddingNormal),
                        text = "Stop",
                        enabled = enableStop,
                        onClick = {
                            viewModel.writeStopCommand(nodeId)
                            Toast.makeText(
                                context,
                                "Stop Extrapolation",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }

                if (extrapolationData.extrapolation?.stub == true) {
                    BlueMsButton(
                        text = "DEMO MODE",
                        color = WarningText,
                        iconPainter = painterResource(R.drawable.ic_info),
                        onClick = { showDialogForRemovingStub = true })
                }
            }
        }

        extrapolationData.extrapolation?.let { extrapolation ->
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
                        text = fromPhaseToString(extrapolation.phase)
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
                        text = fromStateToString(extrapolation.state)
                    )
                }
            }

            //Result Section
            AnimatedVisibility(
                visible = phase == PhaseType.Extrapolation,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
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

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = LocalDimensions.current.paddingNormal),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        text = "Target:"
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        text = setTargetUI(
                            target = extrapolation.target,
                            unit = extrapolation.unit
                        )
                    )
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

                        viewModel.writeStartCommand(nodeId)
                        enableStop = true
                        enableStart = false
                        Toast.makeText(
                            context,
                            "Start Extrapolation",
                            Toast.LENGTH_SHORT
                        ).show()

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
                    text = "Resources are busy with another process. Do you want to stop it and start NEAI-Extrapolation anyway?"
                )
            }
        )
    }

    if (showDialogForRemovingStub) {
        AlertDialog(
            onDismissRequest = { showDialogForRemovingStub = false },
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        showDialogForRemovingStub = false
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
                    text = "Demo Mode"
                )
            },
            text = {
                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = "This is a demo extrapolation library, its results have no sense. To easily develop your own real AI libraries, use the free ST tool:\n NanoEdge AI Studio."
                )
            }
        )
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

private fun fromStateToString(state: StateType?): String {
    return if (state == null) {
        "---"
    } else {
        when (state) {
            StateType.Ok -> "OK"
            StateType.Init_Not_Called -> "INIT NOT CALLED"
            StateType.Board_Error -> "BOARD ERROR"
            StateType.Knowledge_Error -> "KNOWLEDGE ERROR"
            StateType.Not_Enough_Learning -> "NOT ENOUGH LEARNING"
            StateType.Minimal_Learning_done -> "MINIMAL LEARNING DONE"
            StateType.Unknown_Error -> ">UNKNOWN ERROR"
            StateType.Null -> "---"
        }
    }
}

private fun fromPhaseToString(phase: PhaseType): String {
    return when (phase) {
        PhaseType.Idle -> "IDLE"
        PhaseType.Extrapolation -> "EXTRAPOLATION"
        PhaseType.Busy -> "BUSY"
        PhaseType.Null -> "---"
    }
}

private fun setTargetUI(target: Float?, unit: String?): String {
    return if (unit != null) {
        "${target ?: ""} [$unit]"
    } else {
        target?.toString() ?: ""
    }
}