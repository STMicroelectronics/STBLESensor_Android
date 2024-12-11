package com.st.neai_classification.composable

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.neai_class_classification.ModeType
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification
import com.st.blue_sdk.features.extended.neai_class_classification.PhaseType
import com.st.blue_sdk.features.extended.neai_class_classification.StateType
import com.st.neai_classification.NeaiClassificationViewModel
import com.st.neai_classification.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey5
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.WarningText

@Composable
fun NeaiClassificationDemoContent(
    modifier: Modifier = Modifier,
    viewModel: NeaiClassificationViewModel,
    nodeId: String
) {

    val context = LocalContext.current

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                viewModel.readCustomNames(context)
            }

            else -> Unit
        }
    }

    var commandExpanded by remember { mutableStateOf(true) }

    var askIfForceStartCommand by remember { mutableStateOf(false) }

    val classificationData by viewModel.classificationData.collectAsStateWithLifecycle()

    val phase by remember(key1 = classificationData) {
        derivedStateOf { if (classificationData != null) classificationData!!.phase.value else PhaseType.Null }
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
        PhaseType.Classification -> {
            enableStop = true
            enableStart = false
        }
        PhaseType.Busy -> {
            enableStop = false
            enableStart = true
        }
        PhaseType.Null -> {}
    }

    var showAllClassesSwitch by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingMedium)
    ) {
        if (phase != PhaseType.Classification) {
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

        if (classificationData != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = fromModeToTitle(classificationData!!.mode.value)
            )
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = "NEAI Classification"
            )
        }

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
                                viewModel.writeStartClassificationCommand(nodeId)
                                Toast.makeText(
                                    context,
                                    "Start Classification",
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
                            viewModel.writeStopClassificationCommand(nodeId)
                            Toast.makeText(
                                context,
                                "Stop Classification",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
            }
        }

        classificationData?.let { classData ->

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
                        text = fromPhaseToString(classData.phase.value)
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
                        text = fromStateToString(classData.state)
                    )
                }
            }


            //Result Section
            AnimatedVisibility(
                visible = phase == PhaseType.Classification,
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

                    if (classData.mode.value == ModeType.One_Class) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = LocalDimensions.current.paddingNormal),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            text = "Outlier:"
                        )

                        AnimatedContent(
                            targetState = setOneClassOutlier(
                                classData.classProb?.get(0)?.value?.toInt() ?: 0
                            ),
                            label = "animate outlier"
                        ) { value ->
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = value
                            )
                        }

                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Switch(
                                modifier = Modifier.padding(
                                    end = LocalDimensions.current.paddingNormal
                                ),
                                checked = showAllClassesSwitch, onCheckedChange = {
                                    showAllClassesSwitch = !showAllClassesSwitch
                                },
                                colors = SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    uncheckedTrackColor = Grey6,
                                    disabledUncheckedTrackColor = Grey3
                                )
                            )

                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = "Show all classes"
                            )
                        }

                        if (showAllClassesSwitch) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = LocalDimensions.current.paddingNormal),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "Probabilities:"
                            )
                            if (classData.classProb != null) {
                                val mostProb = classData.classMajorProb?.value?.toInt() ?: 0
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                                    verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall)
                                ) {
                                    itemsIndexed(classData.classProb!!) { index, item ->
                                        if (item.value.toInt() != NeaiClassClassification.CLASS_PROB_ESCAPE_CODE) {
                                            NeaiClassElementView(
                                                name = setClassName(
                                                    index + 1,
                                                    viewModel
                                                ),
                                                value = item.value,
                                                isTheMostProbable = index + 1 == mostProb
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = LocalDimensions.current.paddingNormal),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "Most probable Class:"
                            )

                            AnimatedContent(
                                targetState = classData.classMajorProb?.value ?: 0,
                                label = "animate most pb class"
                            ) { value ->
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    text = setMostProbClass(value, viewModel)
                                )
                            }
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

                        viewModel.writeStartClassificationCommand(nodeId)
                        enableStop = true
                        enableStart = false
                        Toast.makeText(
                            context,
                            "Start Classification",
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
                    text = "Resources are busy with another process. Do you want to stop it and start NEAI-Classification anyway?"
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

private fun fromStateToString(state: FeatureField<StateType>?): String {
    return if (state == null) {
        "---"
    } else {
        when (state.value) {
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
        PhaseType.Classification -> "CLASSIFICATION"
        PhaseType.Busy -> "BUSY"
        PhaseType.Null -> "---"
    }
}


private fun fromModeToTitle(mode: ModeType): String {
    return when (mode) {
        ModeType.One_Class -> "1-Class"
        ModeType.N_Class -> "N-Classes"
        else -> "Something Wrong"
    }
}

private fun setMostProbClass(
    mostProbClass: Short?,
    viewModel: NeaiClassificationViewModel
): String {
    val mostProb = (mostProbClass ?: 0).toInt()
    return if (mostProb != 0) {
        if (viewModel.useDefaultNames) {
            "$mostProbClass"
        } else {
            viewModel.customNames[mostProb - 1]
        }
    } else {
        "UNKNOWN"
    }
}

private fun setClassName(classNumber: Int, viewModel: NeaiClassificationViewModel): String {
    return if (viewModel.useDefaultNames) {
        "CL_${classNumber}"
    } else {
        viewModel.customNames[classNumber - 1]
    }
}

private fun setOneClassOutlier(classProb: Int): String {
    return if (classProb == 0) {
        "NO"
    } else {
        if (classProb != NeaiClassClassification.CLASS_PROB_ESCAPE_CODE) {
            "YES"
        } else {
            "---"
        }
    }
}