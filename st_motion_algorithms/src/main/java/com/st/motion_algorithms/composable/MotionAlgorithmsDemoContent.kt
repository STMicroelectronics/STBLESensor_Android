package com.st.motion_algorithms.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.motion_algorithm.AlgorithmType
import com.st.blue_sdk.features.extended.motion_algorithm.DesktopType
import com.st.blue_sdk.features.extended.motion_algorithm.PoseType
import com.st.blue_sdk.features.extended.motion_algorithm.VerticalContextType
import com.st.motion_algorithms.MotionAlgorithmsViewModel
import com.st.motion_algorithms.R
import com.st.ui.theme.LocalDimensions

@Composable
fun MotionAlgorithmsDemoContent(
    modifier: Modifier,
    viewModel: MotionAlgorithmsViewModel,
    nodeId: String
) {

    val motAlgData by viewModel.motAlgData.collectAsStateWithLifecycle()

    val drawResource by remember(key1 = motAlgData) {
        derivedStateOf {
            val algorithmType = motAlgData.algorithmType.value
            val statusType = motAlgData.statusType.value

            when (algorithmType) {
                AlgorithmType.PoseEstimation -> {
                    poseContextIcon(PoseType.getPoseType(statusType))
                }

                AlgorithmType.DesktopTypeDetection -> {
                    desktopIcon(DesktopType.getDesktopType(statusType))
                }

                AlgorithmType.VerticalContext -> {
                    verticalContextIcon(
                        VerticalContextType.getVerticalContextType(
                            statusType
                        )
                    )
                }
                //AlgorithmType.Unknown
                else -> {
                    R.drawable.motion_algo_unknown
                }
            }
        }
    }

    val stringResource by remember(key1 = motAlgData) {
        derivedStateOf {
            val algorithmType = motAlgData.algorithmType.value
            val statusType = motAlgData.statusType.value

            when (algorithmType) {
                AlgorithmType.PoseEstimation -> {
                    poseContextString(PoseType.getPoseType(statusType))
                }

                AlgorithmType.DesktopTypeDetection -> {
                    desktopString(DesktopType.getDesktopType(statusType))
                }

                AlgorithmType.VerticalContext -> {
                    verticalContextString(
                        VerticalContextType.getVerticalContextType(
                            statusType
                        )
                    )
                }
                //AlgorithmType.Unknown
                else -> {
                    R.string.motionAlgo_unknown
                }
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
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {
        MotionAlgorithmsDropDownMenu(
            title = "Algorithm:",
            initialValue = motAlgData.algorithmType.value.toString(),
            values = AlgorithmType.entries.map { it.toString() },
            onValueSelected = { eventSelected ->
                viewModel.setAlgorithmTypeCommand(nodeId, AlgorithmType.fromString(eventSelected))
            })

        AnimatedContent(targetState = drawResource, label = "") { drawRes ->
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.imageExtraLarge),
                painter = painterResource(drawRes),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

        AnimatedContent(targetState = stringResource, label = "") { stringRes ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingLarge),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = stringRes)
            )
        }
    }
}

@DrawableRes
private fun poseContextIcon(type: PoseType): Int {
    return when (type) {
        PoseType.Unknown -> R.drawable.motion_algo_unknown
        PoseType.Sitting -> R.drawable.motion_algo_pose_sitting
        PoseType.Standing -> R.drawable.motion_algo_pose_standing
        PoseType.LyingDown -> R.drawable.motion_algo_pose_lying_down
    }
}

@StringRes
private fun poseContextString(type: PoseType): Int {
    return when (type) {
        PoseType.Unknown -> R.string.motionAlgo_unknown
        PoseType.Sitting -> R.string.motionAlgo_pose_sitting
        PoseType.Standing -> R.string.motionAlgo_pose_standing
        PoseType.LyingDown -> R.string.motionAlgo_pose_layingDown
    }
}

@DrawableRes
private fun desktopIcon(type: DesktopType): Int {
    return when (type) {
        DesktopType.Unknown -> R.drawable.motion_algo_unknown
        DesktopType.Sitting -> R.drawable.desktop_type_sitting
        DesktopType.Standing -> R.drawable.desktop_type_standing
    }
}

@StringRes
private fun desktopString(type: DesktopType): Int {
    return when (type) {
        DesktopType.Unknown -> R.string.motionAlgo_unknown
        DesktopType.Sitting -> R.string.motionAlgo_desktop_sitting
        DesktopType.Standing -> R.string.motionAlgo_desktop_standing
    }
}

@DrawableRes
private fun verticalContextIcon(type: VerticalContextType): Int {
    return when (type) {
        VerticalContextType.Unknown -> R.drawable.motion_algo_unknown
        VerticalContextType.Floor -> R.drawable.motion_algo_vertical_floor
        VerticalContextType.UpDown -> R.drawable.motion_algo_vertical_updown
        VerticalContextType.Stairs -> R.drawable.motion_algo_vertical_stairs
        VerticalContextType.Elevator -> R.drawable.motion_algo_vertical_elevator
        VerticalContextType.Escalator -> R.drawable.motion_algo_vertical_escalator
    }
}

@StringRes
private fun verticalContextString(type: VerticalContextType): Int {
    return when (type) {
        VerticalContextType.Unknown -> R.string.motionAlgo_unknown
        VerticalContextType.Floor -> R.string.motionAlgo_vertical_floor
        VerticalContextType.UpDown -> R.string.motionAlgo_vertical_upDown
        VerticalContextType.Stairs -> R.string.motionAlgo_vertical_stairs
        VerticalContextType.Elevator -> R.string.motionAlgo_vertical_elevator
        VerticalContextType.Escalator -> R.string.motionAlgo_vertical_escalator
    }
}