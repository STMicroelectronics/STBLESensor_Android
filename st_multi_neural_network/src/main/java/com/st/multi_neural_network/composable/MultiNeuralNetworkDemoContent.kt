package com.st.multi_neural_network.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.activity.ActivityType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.multi_neural_network.MultiNeuralNetworkViewModel
import com.st.multi_neural_network.R
import com.st.multi_neural_network.extension.imageResource
import com.st.multi_neural_network.extension.stringResource
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun MultiNeuralNetworkDemoContent(
    modifier: Modifier = Modifier,
    viewModel: MultiNeuralNetworkViewModel,
    nodeId: String
) {

    val algorithmsList by viewModel.algorithmsList.collectAsStateWithLifecycle()

    val activityInfo by viewModel.activityInfo.collectAsStateWithLifecycle()

    val audioClassificationInfo by viewModel.audioClassificationInfo.collectAsStateWithLifecycle()

    var audioSceneStatus by remember { mutableStateOf("Running") }

    var lastVisibleAudioScene by remember { mutableStateOf<AudioClassType?>(null) }

    var lastVisibleAudioDateString by remember { mutableStateOf("") }

    audioClassificationInfo?.let { audioInfo ->
        if (audioInfo.classification.value == AudioClassType.AscOff) {
            audioSceneStatus = "Paused"
        } else if (audioInfo.classification.value == AudioClassType.AscOn) {
            audioSceneStatus = "Running"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Algorithm Selection
        if (algorithmsList.isNotEmpty()) {
            val names = algorithmsList.map { it.name }
            AlgorithmSelectionDropDownMenu(
                modifier = Modifier.fillMaxWidth(),
                values = names,
                initialValue = names[0],
                title = "Algorithm",
                onValueSelected = { algoName ->
                    viewModel.selectAlgorithm(nodeId, algorithmsList.first { it.name == algoName })
                })
        }

        //Human Activity Recognition
        AnimatedVisibility(visible = activityInfo != null) {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                val activity = activityInfo!!.activity.value
                val algorithm = activityInfo!!.algorithm.value
                val date = activityInfo!!.date.value

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.paddingNormal
                    )
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Human Activity Classification",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Running",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (algorithm.toInt() == 4) {
                        if (activity == ActivityType.AdultInCar) {
                            val activityString = stringResource(activity.stringResource())
                            Icon(
                                modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                painter = painterResource(activity.imageResource()),
                                tint = Color.Unspecified,
                                contentDescription = null
                            )

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = String.format(
                                    "%s: %s",
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date),
                                    activityString
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start
                            )
                        } else {
                            val activityString =
                                stringResource(R.string.activityRecognition_adultNotInCar)
                            Icon(
                                modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                painter = painterResource(R.drawable.activity_adult_not_in_car),
                                tint = Color.Unspecified,
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = String.format(
                                    "%s: %s",
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date),
                                    activityString
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        val activityString = stringResource(activity.stringResource())
                        Icon(
                            modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                            painter = painterResource(activity.imageResource()),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = String.format(
                                "%s: %s",
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date),
                                activityString
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }

        //Audio Scene Classification
        AnimatedVisibility(visible = audioClassificationInfo != null) {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                val scene = audioClassificationInfo!!.classification.value
                val algorithm = audioClassificationInfo!!.algorithm.value
                val dateString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                lastVisibleAudioDateString = dateString

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.paddingNormal
                    )
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Audio Scene Classification",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = audioSceneStatus,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (algorithm.toInt() == 1) {
                        when (scene) {
                            AudioClassType.AscOn, AudioClassType.AscOff -> {
                                //Visualize last valid
                                if (lastVisibleAudioScene != null) {
                                    val sceneString =
                                        stringResource(lastVisibleAudioScene!!.stringResource())
                                    Icon(
                                        modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                        painter = painterResource(lastVisibleAudioScene!!.imageResource()),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = String.format(
                                            "%s: %s",
                                            lastVisibleAudioDateString,
                                            sceneString
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start
                                    )
                                } else {
                                    //Visualize question mark
                                    val sceneString = "Analyzing Scene"
                                    Icon(
                                        modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                        painter = painterResource(R.drawable.audio_scene_unkown),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = String.format(
                                            "%s: %s",
                                            lastVisibleAudioDateString,
                                            sceneString
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }

                            AudioClassType.BabyIsCrying -> {
                                lastVisibleAudioScene = scene
                                val sceneString = stringResource(scene.stringResource())
                                Icon(
                                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                    painter = painterResource(scene.imageResource()),
                                    tint = Color.Unspecified,
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = String.format(
                                        "%s: %s",
                                        lastVisibleAudioDateString,
                                        sceneString
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Start
                                )
                            }

                            else -> {
                                lastVisibleAudioScene = scene
                                val sceneString = stringResource(R.string.audio_baby_not_crying)
                                Icon(
                                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                    painter = painterResource(R.drawable.audio_scene_babynotcrying),
                                    tint = Color.Unspecified,
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = String.format("%s: %s", dateString, sceneString),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    } else {
                        when (scene) {
                            AudioClassType.AscOn, AudioClassType.AscOff -> {
                                //Visualize last valid
                                if (lastVisibleAudioScene != null) {
                                    val sceneString =
                                        stringResource(lastVisibleAudioScene!!.stringResource())
                                    Icon(
                                        modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                        painter = painterResource(lastVisibleAudioScene!!.imageResource()),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = String.format(
                                            "%s: %s",
                                            lastVisibleAudioDateString,
                                            sceneString
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start
                                    )
                                } else {
                                    //Visualize question mark
                                    val sceneString = "Analyzing Scene"
                                    Icon(
                                        modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                        painter = painterResource(R.drawable.audio_scene_unkown),
                                        tint = Color.Unspecified,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = String.format(
                                            "%s: %s",
                                            lastVisibleAudioDateString,
                                            sceneString
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }

                            else -> {
                                lastVisibleAudioScene = scene
                                val sceneString = stringResource(scene.stringResource())
                                Icon(
                                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                    painter = painterResource(scene.imageResource()),
                                    tint = Color.Unspecified,
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = String.format("%s: %s", dateString, sceneString),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
        }

        //Multi Neural Network Classification
        if ((audioClassificationInfo != null) && (activityInfo != null)) {

            val activity = activityInfo!!.activity.value
            val algorithmActivity = activityInfo!!.algorithm.value

            val scene = audioClassificationInfo!!.classification.value
            val algorithmAudio = audioClassificationInfo!!.algorithm.value

            AnimatedVisibility(visible = ((algorithmAudio.toInt() == 1) && (algorithmActivity.toInt() == 4))) {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = Shapes.small,
                    shadowElevation = LocalDimensions.current.elevationNormal
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LocalDimensions.current.paddingNormal),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            LocalDimensions.current.paddingNormal
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "MultiNN Classification",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                text = "Running",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if ((activity != ActivityType.AdultInCar) && (scene == AudioClassType.BabyIsCrying)) {
                            val activityString = stringResource(R.string.multiNN_warning)
                            Icon(
                                modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                painter = painterResource(R.drawable.ic_error),
                                tint = Color.Unspecified,
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = String.format(
                                    "%s: %s",
                                    SimpleDateFormat(
                                        "HH:mm:ss",
                                        Locale.getDefault()
                                    ).format(Date()),
                                    activityString
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start
                            )
                        } else {
                            val activityString = stringResource(R.string.multiNN_quiet)
                            Icon(
                                modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                                painter = painterResource(R.drawable.ic_warning_light_grey_24dp),
                                tint = Color.Unspecified,
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = String.format(
                                    "%s: %s",
                                    SimpleDateFormat(
                                        "HH:mm:ss",
                                        Locale.getDefault()
                                    ).format(Date()),
                                    activityString
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}
