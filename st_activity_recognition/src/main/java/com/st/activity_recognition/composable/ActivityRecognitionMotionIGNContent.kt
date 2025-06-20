package com.st.activity_recognition.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.st.activity_recognition.R
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.activity.ActivityType
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.Shapes

@Composable
fun ActivityRecognitionMotionIGNContent(
    modifier: Modifier,
    activityData: Pair<ActivityInfo, Long?>
) {

    val stationaryImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Stationary }
    }

    val animatedColorStationaryImage by animateColorAsState(
        if (stationaryImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val walkingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Walking }
    }

    val animatedColorWalkingImage by animateColorAsState(
        if (walkingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val joggingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Jogging }
    }

    val animatedColorJoggingImage by animateColorAsState(
        if (joggingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val stairsImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Stairs }
    }

    val animatedColorStairsImage by animateColorAsState(
        if (stairsImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val configuration = LocalConfiguration.current

    val smallScreen by remember(key1 = configuration) {
        derivedStateOf {
            val screenHeight = configuration.screenHeightDp
            screenHeight < 800
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {
        Column(
            modifier = Modifier
                .weight(0.5f)
                .padding(end = LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (stationaryImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorStationaryImage),
                        Shapes.extraLarge
                    )
                    .padding(4.dp)
                    .clip(Shapes.extraLarge),
                painter = painterResource(
                    R.drawable.activity_stationary
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (joggingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorJoggingImage),
                        Shapes.extraLarge
                    )
                    .padding(4.dp)
                    .clip(Shapes.extraLarge),
                painter = painterResource(
                    R.drawable.activity_jogging
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )


        }

        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {


            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (walkingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorWalkingImage),
                        Shapes.extraLarge
                    )
                    .padding(4.dp)
                    .clip(Shapes.extraLarge),
                painter = painterResource(
                    R.drawable.activity_walking
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )


            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (stairsImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorStairsImage),
                        Shapes.extraLarge
                    )
                    .padding(4.dp)
                    .clip(Shapes.extraLarge),
                painter = painterResource(
                    R.drawable.activity_stairs
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }
    }
}