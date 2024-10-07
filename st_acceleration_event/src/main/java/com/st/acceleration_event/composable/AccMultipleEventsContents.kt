package com.st.acceleration_event.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.st.acceleration_event.model.getDefaultIconResource
import com.st.acceleration_event.model.getEventIconResource
import com.st.acceleration_event.model.isOrientationEvent
import com.st.blue_sdk.features.acceleration_event.AccelerationEventInfo
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.ui.theme.LocalDimensions

@Composable
fun AccMultipleEventsContents(
    modifier: Modifier = Modifier,
    accEventData: Pair<AccelerationEventInfo, Long?> = Pair(
        AccelerationEventInfo.emptyAccelerationEventInfo(),
        null
    )
) {

    val localOrientation by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first.accEvent.firstOrNull { isOrientationEvent(it.value) } }
    }

    val localSingleTap by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first.accEvent.firstOrNull { it.value == AccelerationType.SingleTap } }
    }

    val localDoubleTap by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first.accEvent.firstOrNull { it.value == AccelerationType.DoubleTap } }
    }

    val localFreeFall by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first.accEvent.firstOrNull { it.value == AccelerationType.FreeFall } }
    }

    val localTilt by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first.accEvent.firstOrNull { it.value == AccelerationType.Tilt } }
    }


    val numSteps by remember(key1 = accEventData.second) {
        derivedStateOf {
            accEventData.first.numSteps.value.toInt()
        }
    }

    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            if (localOrientation == null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.3f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.Orientation)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(
                        getEventIconResource(eventType = localOrientation!!.value)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }

            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "Orientation"
            )

            if (localSingleTap == null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.3f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.SingleTap)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(
                        getEventIconResource(eventType = localSingleTap!!.value)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

            }

            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "Tap"
            )
            if (localDoubleTap == null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.3f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.DoubleTap)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(
                        getEventIconResource(eventType = localDoubleTap!!.value)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }


            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "Double Tap"
            )

        }
        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {

            if (numSteps == 0) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.3f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.Pedometer)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                    Icon(
                        modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                        painter = painterResource(
                            getDefaultIconResource(eventType = DetectableEventType.Pedometer)
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = "Num Steps:"
                )
                AnimatedContent(
                    targetState = numSteps, label = "",

                    transitionSpec = {
                        // Compare the incoming number with the previous number.
                        if (targetState > initialState) {
                            // If the target number is larger, it slides up and fades in
                            // while the initial (smaller) number slides up and fades out.
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            // If the target number is smaller, it slides down and fades in
                            // while the initial number slides down and fades out.
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> height } + fadeOut())
                        }.using(
                            // Disable clipping since the faded slide-in/out should
                            // be displayed out of bounds.
                            SizeTransform(clip = false)
                        )
                    }
                ) { steps ->

                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = "$steps"
                    )
                }
            }

            if (localFreeFall == null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.3f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.FreeFall)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.FreeFall)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

            }

            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "Free Fall"
            )

            if (localTilt == null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .graphicsLayer(alpha = 0.5f),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.Tilt)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(
                        getDefaultIconResource(eventType = DetectableEventType.Tilt)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }

            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "Tilt"
            )
        }
    }
}