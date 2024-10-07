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
import com.st.acceleration_event.model.getDefaultIconResource
import com.st.acceleration_event.model.getEventIconResource
import com.st.acceleration_event.model.isOrientationEvent
import com.st.blue_sdk.features.acceleration_event.AccelerationEventInfo
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.ui.theme.LocalDimensions

@Composable
fun AccSingleEventContent(
    modifier: Modifier = Modifier,
    mCurrentEvent: DetectableEventType,
    accEventData: Pair<AccelerationEventInfo, Long?> = Pair(
        AccelerationEventInfo.emptyAccelerationEventInfo(),
        null
    )
) {

    val numSteps by remember(key1 = accEventData.second) {
        derivedStateOf {
            accEventData.first.numSteps.value.toInt()
        }
    }

    val localAccEvent by remember(key1 = accEventData.second) {
        derivedStateOf { accEventData.first }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (localAccEvent.accEvent.isNotEmpty()) {
            if (isOrientationEvent(localAccEvent.accEvent[0].value)) {
                Icon(
                    modifier = Modifier.size(size = LocalDimensions.current.imageExtraLarge),
                    painter = painterResource(
                        getEventIconResource(eventType = localAccEvent.accEvent[0].value)
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            } else {
                AnimatedContent(targetState = accEventData.second, label = "") { _ ->
                    Icon(
                        modifier = Modifier.size(size = LocalDimensions.current.imageExtraLarge),
                        painter = painterResource(
                            getEventIconResource(eventType = localAccEvent.accEvent[0].value)
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }

            accEventData.second?.let { ts ->
                Text(
                    modifier = Modifier.padding(
                        top = LocalDimensions.current.paddingNormal
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    text = "TS: $ts"
                )
            }

            if ((mCurrentEvent == DetectableEventType.Pedometer)) {
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
                            modifier = Modifier.padding(
                                top = LocalDimensions.current.paddingNormal
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            text = "$steps"
                        )
                    }
                }
            }

        } else {
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.imageExtraLarge),
                painter = painterResource(
                    getDefaultIconResource(eventType = mCurrentEvent)
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }
    }
}