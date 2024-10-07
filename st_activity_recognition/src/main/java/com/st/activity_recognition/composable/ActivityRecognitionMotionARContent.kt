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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.activity_recognition.R
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.activity.ActivityType
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow
import java.util.Date


@Composable
fun ActivityRecognitionMotionARContent(
    modifier: Modifier,
    showFastWalking: Boolean,
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

    val fastWalkingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.FastWalking }
    }

    val animatedColorFastWalkingImage by animateColorAsState(
        if (fastWalkingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val joggingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Jogging }
    }

    val animatedColorJoggingImage by animateColorAsState(
        if (joggingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val bikingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Biking }
    }

    val animatedColorBikingImage by animateColorAsState(
        if (bikingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val drivingImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.Driving }
    }

    val animatedColorDrivingImage by animateColorAsState(
        if (drivingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalArrangement = Arrangement.spacedBy(
            LocalDimensions.current.paddingNormal
        )
    ) {
        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (stationaryImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorStationaryImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_stationary
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (fastWalkingImage) {
                            1f
                        } else {
                            if (showFastWalking) {
                                0.3f
                            } else {
                                0f
                            }
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorFastWalkingImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_fastwalking
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (bikingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorBikingImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_biking
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
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (walkingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorWalkingImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_walking
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (joggingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorJoggingImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_jogging
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (drivingImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorDrivingImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.activity_driving
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShowStationary() {
    BlueMSTheme {
        ActivityRecognitionMotionARContent(
            modifier = Modifier,
            showFastWalking = true,
            activityData = Pair(
                ActivityInfo(
                    activity = FeatureField(
                        value = ActivityType.Stationary,
                        name = "Activity"
                    ),
                    algorithm = FeatureField(
                        value = ActivityInfo.ALGORITHM_NOT_DEFINED,
                        name = "Algorithm"
                    ),
                    date = FeatureField(
                        value = Date(),
                        name = "Date"
                    )
                ), 1L
            )
        )
    }
}
