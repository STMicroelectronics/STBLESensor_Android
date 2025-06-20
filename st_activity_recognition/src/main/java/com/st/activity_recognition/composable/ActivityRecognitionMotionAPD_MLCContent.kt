package com.st.activity_recognition.composable

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
fun ActivityRecognitionMotionAPD_MLCContent(
    modifier: Modifier,
    activityData: Pair<ActivityInfo, Long?>
) {

    val adultNotInCarImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.NoActivity }
    }

    val animatedColorAdultNotInCarImage by animateColorAsState(
        if (adultNotInCarImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val adultInCarImage by remember(key1 = activityData.second) {
        derivedStateOf { activityData.first.activity.value == ActivityType.AdultInCar }
    }

    val animatedColorAdultInCarImage by animateColorAsState(
        if (adultInCarImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val configuration = LocalConfiguration.current

    val smallScreen by remember(key1 = configuration) {
        derivedStateOf {
            val screenHeight = configuration.screenHeightDp
            screenHeight < 800
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {

        Icon(
            modifier = Modifier
                .size(size = if (smallScreen) LocalDimensions.current.imageLarge else LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (adultNotInCarImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorAdultNotInCarImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.activity_adult_not_in_car
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(size = if (smallScreen) LocalDimensions.current.imageLarge else LocalDimensions.current.imageExtraLarge)
                .graphicsLayer(
                    alpha = if (adultInCarImage) {
                        1f
                    } else {
                        0.3f
                    }
                )
                .border(
                    BorderStroke(4.dp, animatedColorAdultInCarImage),
                    Shapes.extraLarge
                )
                .padding(4.dp)
                .clip(Shapes.extraLarge),
            painter = painterResource(
                R.drawable.activity_adult_in_car
            ),
            tint = Color.Unspecified,
            contentDescription = null
        )
    }

}

