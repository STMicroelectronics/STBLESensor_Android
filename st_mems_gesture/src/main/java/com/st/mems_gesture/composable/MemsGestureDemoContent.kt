package com.st.mems_gesture.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.mems_gesture.MemsGestureType
import com.st.mems_gesture.MemsGestureViewModel
import com.st.mems_gesture.R
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow


@Composable
fun MemsGestureDemoContent(
    modifier: Modifier,
    viewModel: MemsGestureViewModel
) {
    val gestureData by viewModel.gestureData.collectAsStateWithLifecycle()


    val configuration = LocalConfiguration.current

    val smallScreen by remember(key1 = configuration) {
        derivedStateOf {
            val screenHeight = configuration.screenHeightDp
            screenHeight < 800
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val glanceImage by remember(key1 = gestureData.second) {
            derivedStateOf { gestureData.first.gesture.value == MemsGestureType.Glance }
        }

        val animatedColorGlanceImage by animateColorAsState(
            if (glanceImage) PrimaryYellow else Color.Unspecified,
            label = "color"
        )

        val pickUpImage by remember(key1 = gestureData.second) {
            derivedStateOf { gestureData.first.gesture.value == MemsGestureType.PickUp }
        }

        val animatedColorPickUpImage by animateColorAsState(
            if (pickUpImage) PrimaryYellow else Color.Unspecified,
            label = "color"
        )

        val wakeUpImage by remember(key1 = gestureData.second) {
            derivedStateOf { gestureData.first.gesture.value == MemsGestureType.WakeUp }
        }

        val animatedColorWakeUpImage by animateColorAsState(
            if (wakeUpImage) PrimaryYellow else Color.Unspecified,
            label = "color"
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {

            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (glanceImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorGlanceImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.mems_gesture_glance
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (pickUpImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorPickUpImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.mems_gesture_pick_up
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge)
                    .graphicsLayer(
                        alpha = if (wakeUpImage) {
                            1f
                        } else {
                            0.3f
                        }
                    )
                    .border(
                        BorderStroke(4.dp, animatedColorWakeUpImage),
                        RoundedCornerShape(size = 32.dp)
                    )
                    .padding(4.dp)
                    .clip(RoundedCornerShape(size = 32.dp)),
                painter = painterResource(
                    R.drawable.mems_gesture_wake_up
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

        if (gestureData.second == null) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Waiting data…"
            )
        }
    }
}