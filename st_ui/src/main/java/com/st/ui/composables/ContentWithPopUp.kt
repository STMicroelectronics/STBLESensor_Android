/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ui.composables

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.st.ui.theme.AppDimension
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.POPUP_DEFAULT_TIMEOUT
import com.st.ui.theme.POPUP_IN_TRANSITION_DURATION
import com.st.ui.theme.POPUP_OUT_TRANSITION_DURATION
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun rememberPaddingHorizontal(
    localCurrentDensity: Density,
    currentDimensions: AppDimension,
    anchor: Size,
    screenWidth: Dp,
    popupWidth: Dp,
    anchorPositionX: Dp
): State<Float> = remember {
    derivedStateOf {
        val anchorWidth = with(receiver = localCurrentDensity) { anchor.width.toDp() }
        val centerSideLowerLimit = popupWidth / 2
        val centerSideTopLimit =
            (screenWidth.value - ((popupWidth.value + currentDimensions.paddingNormal.value) / 2)).dp

        anchorPositionX.value - currentDimensions.paddingSmall.value + when {
            anchorPositionX <= centerSideLowerLimit -> ((anchorWidth.value / 2) - currentDimensions.paddingSmall.value)

            anchorPositionX > centerSideLowerLimit && anchorPositionX <= centerSideTopLimit
            -> ((popupWidth.value / 2) - anchorPositionX.value)

            else -> {
                popupWidth.value - screenWidth.value + currentDimensions.paddingSmall.value
                +(anchorWidth.value / 2)
            }
        }
    }
}

@Composable
fun rememberAlphaTransition(
    expandedStates: MutableTransitionState<Boolean>
): State<Float> =
    updateTransition(
        transitionState = expandedStates,
        label = "Tooltip"
    ).animateFloat(label = "alpha", transitionSpec = {
        if (false isTransitioningTo true) {
            // Dismissed to expanded
            tween(durationMillis = POPUP_IN_TRANSITION_DURATION)
        } else {
            // Expanded to dismissed.
            tween(durationMillis = POPUP_OUT_TRANSITION_DURATION)
        }
    }) {
        if (it) 1f else 0f
    }

@Composable
fun PopupContent(
    popupText: String,
    popupHeight: MutableState<Int>,
    currentDimensions: AppDimension,
    openDialog: MutableState<Boolean>,
    alpha: Float,
    paddingHorizontal: Float,
    localCurrentDensity: Density,
    popupWidth: MutableState<Dp>
) {
    Popup(
        popupPositionProvider = AlignmentOffsetPositionProvider(
            Alignment.BottomCenter,
            IntOffset(
                x = 0,
                y = popupHeight.value + currentDimensions.paddingNormal.value.roundToInt()
            )
        ),
        onDismissRequest = {
            openDialog.value = false
        },
        properties = PopupProperties()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = currentDimensions.paddingSmall)
                .alpha(alpha = alpha)
        ) {
            Surface(
                modifier = Modifier
                    .align(alignment = Alignment.Start)
                    .offset(x = paddingHorizontal.dp)
                    .height(height = currentDimensions.paddingSmall)
                    .width(width = currentDimensions.paddingNormal),
                shadowElevation = currentDimensions.elevationNormal,
                color = MaterialTheme.colorScheme.primary,
                shape = TriangleEdge()
            ) { /** NOOP **/ }

            Surface(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    popupWidth.value =
                        with(receiver = localCurrentDensity) { coordinates.size.width.toDp() }
                    popupHeight.value = coordinates.size.height
                },
                shadowElevation = currentDimensions.elevationNormal,
                shape = RoundedCornerShape(
                    corner = CornerSize(size = currentDimensions.cornerNormal)
                ),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    modifier = Modifier.padding(all = currentDimensions.paddingNormal),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    text = popupText
                )
            }
        }
    }
}

@Composable
fun BlueMsContentWithPopup(
    popupText: String = "",
    popupTimeout: Long = POPUP_DEFAULT_TIMEOUT,
    content: @Composable () -> Unit
) {
    val localCurrentDensity = LocalDensity.current
    val currentDimensions = LocalDimensions.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var popupWidth = remember { mutableStateOf(value = 0.dp) }
    var popupHeight = remember { mutableStateOf(value = 0) }
    var anchor by remember { mutableStateOf(value = Size.Unspecified) }
    var anchorPositionX by remember { mutableStateOf(value = 0.dp) }
    val openDialog = remember { mutableStateOf(value = false) }
    val expandedStates = remember { MutableTransitionState(initialState = false) }
    expandedStates.targetState = openDialog.value

    val paddingHorizontal by rememberPaddingHorizontal(
        localCurrentDensity = localCurrentDensity,
        currentDimensions = currentDimensions,
        screenWidth = screenWidth,
        popupWidth = popupWidth.value,
        anchor = anchor,
        anchorPositionX = anchorPositionX
    )

    val alpha by rememberAlphaTransition(
        expandedStates = expandedStates
    )

    Box {
        if (expandedStates.currentState || expandedStates.targetState) {
            if (expandedStates.isIdle) {
                LaunchedEffect(key1 = popupTimeout, key2 = openDialog) {
                    delay(popupTimeout)
                    openDialog.value = false
                }
            }

            PopupContent(
                popupText = popupText,
                popupHeight = popupHeight,
                currentDimensions = currentDimensions,
                openDialog = openDialog,
                alpha = alpha,
                paddingHorizontal = paddingHorizontal,
                localCurrentDensity = localCurrentDensity,
                popupWidth = popupWidth
            )
        }

        Box(
            modifier = Modifier
                .clickable {
                    openDialog.value = !openDialog.value
                }
                .onGloballyPositioned { coordinates ->
                    anchor = coordinates.size.toSize()
                    anchorPositionX =
                        with(receiver = localCurrentDensity) { coordinates.positionInRoot().x.toDp() }
                }
        ) {
            content()
        }
    }
}

internal class AlignmentOffsetPositionProvider(
    val alignment: Alignment,
    val offset: IntOffset
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var popupPosition = IntOffset(x = 0, y = 0)

        // Get the aligned point inside the parent
        val parentAlignmentPoint = alignment.align(
            IntSize.Zero, IntSize(anchorBounds.width, anchorBounds.height), layoutDirection
        )
        // Get the aligned point inside the child
        val relativePopupPos = alignment.align(
            IntSize.Zero, IntSize(popupContentSize.width, popupContentSize.height), layoutDirection
        )

        // Add the position of the parent
        popupPosition += IntOffset(x = anchorBounds.left, y = anchorBounds.top)

        // Add the distance between the parent's top left corner and the alignment point
        popupPosition += parentAlignmentPoint

        // Subtract the distance between the children's top left corner and the alignment point
        popupPosition -= IntOffset(relativePopupPos.x, relativePopupPos.y)

        // Add the user offset
        val scaleFactor = if (layoutDirection == LayoutDirection.Ltr) 1 else -1
        val resolvedOffset = IntOffset(x = offset.x * scaleFactor, y = offset.y)
        popupPosition += resolvedOffset

        return popupPosition
    }
}

class TriangleEdge : Shape {

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val trianglePath = Path()
        trianglePath.apply {
            moveTo(x = size.width / 2, y = 0f)
            lineTo(x = size.width, y = size.height)
            lineTo(x = 0f, y = size.height)
        }

        return Outline.Generic(path = trianglePath)
    }
}
