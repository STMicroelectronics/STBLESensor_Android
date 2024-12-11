package com.st.proximity_gesture_recognition.composable

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.proximity_gesture.ProximityGestureType
import com.st.proximity_gesture_recognition.ProximityGestureRecognitionViewModel
import com.st.proximity_gesture_recognition.R
import com.st.ui.theme.LocalDimensions


@Composable
fun ProximityGestureRecognitionDemoContent(
    modifier: Modifier,
    viewModel: ProximityGestureRecognitionViewModel
) {

    val gestureData by viewModel.gestureData.collectAsStateWithLifecycle()

    val gesture = remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                ProximityGestureType.Unknown
            } else {
                gestureData.first.gesture.value
            }
        }
    }

    val tapEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                false
            } else {
                if (gesture.value != ProximityGestureType.Unknown) {
                    when (gesture.value) {
                        ProximityGestureType.Tap -> true
                        else -> false
                    }
                } else {
                    false
                }
            }
        }
    }

    val tapAlpha: Float by animateFloatAsState(
        if (tapEnable) 1f else 0f, label = "",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val leftEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                false
            } else {
                if (gesture.value != ProximityGestureType.Unknown) {
                    when (gesture.value) {
                        ProximityGestureType.Right -> true
                        else -> false
                    }
                } else {
                    false
                }
            }
        }
    }

    val leftAlpha: Float by animateFloatAsState(
        if (leftEnable) 1f else 0f, label = "",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val rightEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                false
            } else {
                if (gesture.value != ProximityGestureType.Unknown) {
                    when (gesture.value) {
                        ProximityGestureType.Left -> true
                        else -> false
                    }
                } else {
                    false
                }
            }
        }
    }

    val rightAlpha: Float by animateFloatAsState(
        if (rightEnable) 1f else 0f, label = "",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .padding(top = LocalDimensions.current.paddingLarge)
                .size(size = LocalDimensions.current.imageLarge)
                .graphicsLayer(alpha = tapAlpha),
            painter = painterResource(R.drawable.tap_icon),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {

            Icon(
                modifier = Modifier
                    .padding(top = LocalDimensions.current.paddingLarge)
                    .size(size = LocalDimensions.current.imageLarge)
                    .rotate(-90f)
                    .graphicsLayer(alpha = leftAlpha),
                painter = painterResource(R.drawable.ic_arrow_rounded),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Icon(
                modifier = Modifier
                    .padding(top = LocalDimensions.current.paddingLarge)
                    .size(size = LocalDimensions.current.imageLarge)
                    .rotate(90f)
                    .graphicsLayer(alpha = rightAlpha),
                painter = painterResource(R.drawable.ic_arrow_rounded),
                tint = Color.Unspecified,
                contentDescription = null
            )

        }
    }
}