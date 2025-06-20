package com.st.audio_classification_demo.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
fun AudioClassificationBabyCryingContent(
    modifier: Modifier,
    audioClassificationData: Pair<AudioClassificationInfo, Long?>
) {

    val babyIsCryingImage by remember(key1 = audioClassificationData.second) {
        derivedStateOf { audioClassificationData.first.classification.value == AudioClassType.BabyIsCrying }
    }

    val babyIsNotCryingImage by remember(key1 = audioClassificationData.second) {
        derivedStateOf { audioClassificationData.first.classification.value == AudioClassType.Unknown }
    }

    val animatedColorBabyIsCryingImage by animateColorAsState(
        if (babyIsCryingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val animatedColorBabyIsNotCryingImage by animateColorAsState(
        if (babyIsNotCryingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (babyIsCryingImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorBabyIsCryingImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.audio_scene_babycrying
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (babyIsNotCryingImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorBabyIsNotCryingImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.audio_scene_babynotcrying
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )
    }
}