package com.st.audio_classification_demo.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.st.audio_classification_demo.R
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.Shapes


@Composable
fun AudioClassificationContent(
    modifier: Modifier,
    audioClassificationData: Pair<AudioClassificationInfo, Long?>
) {

    val indoorImage by remember(key1 = audioClassificationData.second) {
        derivedStateOf { audioClassificationData.first.classification.value == AudioClassType.Indoor }
    }

    val outdoorImage by remember(key1 = audioClassificationData.second) {
        derivedStateOf { audioClassificationData.first.classification.value == AudioClassType.Outdoor }
    }

    val inVehicleImage by remember(key1 = audioClassificationData.second) {
        derivedStateOf { audioClassificationData.first.classification.value == AudioClassType.InVehicle }
    }

    val animatedColorIndoorImage by animateColorAsState(
        if (indoorImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val animatedColorOutdoorImage by animateColorAsState(
        if (outdoorImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val animatedColorInVehicleImage by animateColorAsState(
        if (inVehicleImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (indoorImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorIndoorImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.audio_scene_inside
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (outdoorImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorOutdoorImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.audio_scene_outside
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (inVehicleImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorInVehicleImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.audio_scene_in_vehicle
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )
    }
}

