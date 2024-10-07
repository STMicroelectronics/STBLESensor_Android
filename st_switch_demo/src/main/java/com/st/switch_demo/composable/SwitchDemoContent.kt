package com.st.switch_demo.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.switchfeature.SwitchStatusType
import com.st.blue_sdk.models.Boards
import com.st.switch_demo.R
import com.st.switch_demo.SwitchDemoViewModel
import com.st.ui.theme.LocalDimensions


@Composable
fun SwitchDemoContent(
    modifier: Modifier,
    viewModel: SwitchDemoViewModel,
    nodeId: String
) {
    val switchData by viewModel.switchData.collectAsStateWithLifecycle()

    val currentSwitchValue by remember(key1 = switchData) {
        derivedStateOf {
            if (switchData != null) {
                switchData!!.status.value
            } else {
                SwitchStatusType.Off
            }
        }
    }

    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingLarge)
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = if (viewModel.boardType == Boards.Model.SENSOR_TILE_BOX ||
                viewModel.boardType == Boards.Model.SENSOR_TILE_BOX_PRO ||
                viewModel.boardType == Boards.Model.SENSOR_TILE_BOX_PROB
            ) {
                "The led is switching on when an event is detected"
            } else {
                "Click on the image to change the led status"
            }
        )

        //For avoiding the  ripple effect when clicking on image
        val interactionSource = remember { MutableInteractionSource() }

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
                        viewModel.writeSwitchCommand(
                            nodeId,
                            currentSwitchValue
                        )
                    },
                painter = painterResource(
                    if (status == SwitchStatusType.On) {
                        R.drawable.switch_on
                    } else {
                        R.drawable.switch_off
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}