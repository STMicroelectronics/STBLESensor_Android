package com.st.gesture_navigation.composable

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationButton
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationGestureType
import com.st.gesture_navigation.GestureNavigationViewModel
import com.st.gesture_navigation.R
import com.st.ui.theme.LocalDimensions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun GestureNavigationDemoContent(
    modifier: Modifier,
    viewModel: GestureNavigationViewModel
) {
    val gestureData by viewModel.gestureData.collectAsStateWithLifecycle()


    //Understand Gesture/Button
    val gesture = remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                GestureNavigationGestureType.Undefined
            } else {
                gestureData.first.gesture.value
            }
        }
    }

    val button = remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                GestureNavigationButton.Undefined
            } else {
                gestureData.first.button.value
            }
        }
    }

    val buttonEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gesture.value.isPressEvent()) {
                System.currentTimeMillis()
            } else {
                null
            }
        }
    }

    val gestureDownToUp by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (gesture.value == GestureNavigationGestureType.SwipeDownToUp) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }

    val gestureUpToDown by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (gesture.value == GestureNavigationGestureType.SwipeUpToDown) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }


    val upEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (button.value == GestureNavigationButton.Up) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }


    val downEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (button.value == GestureNavigationButton.Down) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }

    val leftEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (button.value == GestureNavigationButton.Left) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }


    val rightEnable by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (button.value == GestureNavigationButton.Right) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }


    val gestureLeftToRight by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (gesture.value == GestureNavigationGestureType.SwipeLeftToRight) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }

    val gestureRightToLeft by remember(key1 = gestureData.second) {
        derivedStateOf {
            if (gestureData.second == null) {
                null
            } else {
                if (gesture.value == GestureNavigationGestureType.SwipeRightToLeft) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            }
        }
    }

    //Animations

    var alphaUp by remember { mutableFloatStateOf(0f) }
    var alphaDown by remember { mutableFloatStateOf(0f) }
    var alphaLeft by remember { mutableFloatStateOf(0f) }
    var alphaRight by remember { mutableFloatStateOf(0f) }
    var alphaButton by remember { mutableFloatStateOf(0f) }

    var moveUp by remember { mutableFloatStateOf(0f) }
    var moveDown by remember { mutableFloatStateOf(0f) }
    var moveLeft by remember { mutableFloatStateOf(0f) }
    var moveRight by remember { mutableFloatStateOf(0f) }

    var scaleUp by remember { mutableFloatStateOf(1f) }
    var scaleDown by remember { mutableFloatStateOf(1f) }
    var scaleLeft by remember { mutableFloatStateOf(1f) }
    var scaleRight by remember { mutableFloatStateOf(1f) }
    var scaleButton by remember { mutableFloatStateOf(1f) }


    val animSpec: AnimationSpec<Float> = tween(
        durationMillis = 400,
        easing = LinearEasing
    )
    val animSpecShort: AnimationSpec<Float> = tween(
        durationMillis = 100,
        easing = LinearEasing
    )

    LaunchedEffect(key1 = buttonEnable) {
        buttonEnable?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 0.3f, animationSpec = animSpec
                    ) { value, _ -> alphaButton = value }
                }
                launch {//scale up
                    animate(
                        1f, 1.2f, animationSpec = animSpec
                    ) { value, _ -> scaleButton = value }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        0.3f, 0f, animationSpec = animSpecShort
                    ) { value, _ -> alphaButton = value }
                }
                launch {//scale down
                    animate(
                        1.2f, 1f, animationSpec = animSpecShort
                    ) { value, _ -> scaleButton = value }
                }
            }
        }
    }

    LaunchedEffect(key1 = upEnable) {
        upEnable?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ -> alphaUp = value }
                }
                launch {//scale up
                    animate(
                        1f, 1.2f, animationSpec = animSpec
                    ) { value, _ -> scaleUp = value }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ -> alphaUp = value }
                }
                launch {//scale down
                    animate(
                        1.2f, 1f, animationSpec = animSpecShort
                    ) { value, _ -> scaleUp = value }
                }
            }
        }
    }

    LaunchedEffect(key1 = downEnable) {
        downEnable?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ -> alphaDown = value }
                }
                launch {//scale up
                    animate(
                        1f, 1.2f, animationSpec = animSpec
                    ) { value, _ -> scaleDown = value }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ -> alphaDown = value }
                }
                launch {//scale down
                    animate(
                        1.2f, 1f, animationSpec = animSpecShort
                    ) { value, _ -> scaleDown = value }
                }
            }
        }
    }

    LaunchedEffect(key1 = leftEnable) {
        leftEnable?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ -> alphaLeft = value }
                }
                launch {//scale up
                    animate(
                        1f, 1.2f, animationSpec = animSpec
                    ) { value, _ -> scaleLeft = value }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ -> alphaLeft = value }
                }
                launch {//scale down
                    animate(
                        1.2f, 1f, animationSpec = animSpecShort
                    ) { value, _ -> scaleLeft = value }
                }
            }
        }
    }


    LaunchedEffect(key1 = rightEnable) {
        rightEnable?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ -> alphaRight = value }
                }
                launch {//scale up
                    animate(
                        1f, 1.2f, animationSpec = animSpec
                    ) { value, _ -> scaleRight = value }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ -> alphaRight = value }
                }
                launch {//scale down
                    animate(
                        1.2f, 1f, animationSpec = animSpecShort
                    ) { value, _ -> scaleRight = value }
                }
            }
        }
    }


    LaunchedEffect(key1 = gestureDownToUp) {
        gestureDownToUp?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ ->
                        alphaDown = value
                        alphaUp = value
                    }
                }
                launch {//move up
                    animate(
                        0f, -100f, animationSpec = animSpec
                    ) { value, _ ->
                        moveDown = value
                        moveUp = value
                    }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        alphaDown = value
                        alphaUp = value
                    }
                }
                launch {//move down
                    animate(
                        -100f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        moveDown = value
                        moveUp = value
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = gestureUpToDown) {
        gestureUpToDown?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ ->
                        alphaDown = value
                        alphaUp = value
                    }
                }
                launch {//move down
                    animate(
                        0f, 100f, animationSpec = animSpec
                    ) { value, _ ->
                        moveDown = value
                        moveUp = value
                    }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        alphaDown = value
                        alphaUp = value
                    }
                }
                launch {//move up
                    animate(
                        100f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        moveDown = value
                        moveUp = value
                    }
                }
            }
        }
    }


    LaunchedEffect(key1 = gestureLeftToRight) {
        gestureLeftToRight?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ ->
                        alphaLeft = value
                        alphaRight = value
                    }
                }
                launch {//move Right
                    animate(
                        0f, 100f, animationSpec = animSpec
                    ) { value, _ ->
                        moveLeft = value
                        moveRight = value
                    }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        alphaLeft = value
                        alphaRight = value
                    }
                }
                launch {//move Left
                    animate(
                        100f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        moveLeft = value
                        moveRight = value
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = gestureRightToLeft) {
        gestureRightToLeft?.let {
            coroutineScope {
                launch { //fade in
                    animate(
                        0f, 1f, animationSpec = animSpec
                    ) { value, _ ->
                        alphaLeft = value
                        alphaRight = value
                    }
                }
                launch {//move Left
                    animate(
                        0f, -100f, animationSpec = animSpec
                    ) { value, _ ->
                        moveLeft = value
                        moveRight = value
                    }
                }
            }
            coroutineScope {
                launch { //fade out
                    animate(
                        1f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        alphaLeft = value
                        alphaRight = value
                    }
                }
                launch {//move Right
                    animate(
                        -100f, 0f, animationSpec = animSpecShort
                    ) { value, _ ->
                        moveLeft = value
                        moveRight = value
                    }
                }
            }
        }
    }


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (gestureData.second != null) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = LocalDimensions.current.paddingNormal,
                        end = LocalDimensions.current.paddingNormal,
                        top = LocalDimensions.current.paddingNormal,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
            ) {
                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = LocalDimensions.current.paddingLarge),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.imageMedium)
                            .graphicsLayer(
                                alpha = alphaUp,
                                scaleX = scaleUp,
                                scaleY = scaleUp,
                                translationY = moveUp
                            ),
                        painter = painterResource(R.drawable.ic_arrow_rounded),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.imageMedium)
                            .graphicsLayer(
                                alpha = alphaLeft,
                                scaleX = scaleLeft,
                                scaleY = scaleLeft,
                                translationX = moveLeft
                            )
                            .rotate(270f),
                        painter = painterResource(R.drawable.ic_arrow_rounded),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )

                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.imageMedium)
                                .graphicsLayer(
                                    alpha = alphaButton,
                                    scaleX = scaleButton,
                                    scaleY = scaleButton
                                ),
                            painter = painterResource(R.drawable.ic_touch_app),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(0.3f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            text = fromGestureToString(gesture.value)
                        )
                    }

                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.imageMedium)
                            .graphicsLayer(
                                alpha = alphaRight,
                                scaleX = scaleRight,
                                scaleY = scaleRight,
                                translationX = moveRight
                            )
                            .rotate(90f),
                        painter = painterResource(R.drawable.ic_arrow_rounded),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.imageMedium)
                            .graphicsLayer(
                                alpha = alphaDown,
                                scaleX = scaleDown,
                                scaleY = scaleDown,
                                translationY = moveDown
                            )
                            .rotate(180f),
                        painter = painterResource(R.drawable.ic_arrow_rounded),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        } else {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = "Waiting for gesture…"
            )
        }
    }
}

private fun fromGestureToString(gesture: GestureNavigationGestureType): String {
    return when(gesture) {
        GestureNavigationGestureType.Undefined -> "Undefined"
        GestureNavigationGestureType.SwipeLeftToRight -> "Swipe\nTo Right"
        GestureNavigationGestureType.SwipeRightToLeft -> "Swipe\nTo Left"
        GestureNavigationGestureType.SwipeUpToDown -> "Swipe\nTo Down"
        GestureNavigationGestureType.SwipeDownToUp -> "Swipe\nTo Up"
        GestureNavigationGestureType.SinglePress -> "Single\nPress"
        GestureNavigationGestureType.DoublePress -> "Double\nPress"
        GestureNavigationGestureType.TriplePress -> "Triple\nPress"
        GestureNavigationGestureType.LongPress -> "Long\nPress"
        GestureNavigationGestureType.Error -> "Error"
    }
}