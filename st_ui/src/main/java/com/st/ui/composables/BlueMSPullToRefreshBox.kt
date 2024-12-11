package com.st.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.st.ui.theme.LocalDimensions

@Composable
@ExperimentalMaterial3Api
fun BlueMSPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicatorAlignment: Alignment = Alignment.TopCenter,
    isBetaRelease: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier.pullToRefresh(state = state, isRefreshing = isRefreshing, onRefresh = onRefresh),
        contentAlignment = contentAlignment
    ) {
        content()
        if (isBetaRelease) {
            BlueMSPullToRefreshIndicator(
                modifier = Modifier.align(indicatorAlignment),
                isRefreshing = isRefreshing,
                state = state
            )
        } else {
            Indicator(
                modifier = Modifier.align(indicatorAlignment),
                isRefreshing = isRefreshing,
                state = state
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueMSPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    //containerColor: Color = PullToRefreshDefaults.containerColor,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    threshold: Dp = PositionalThreshold,
) {

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ), label = ""
    )

    Box(
        modifier =
        modifier.pullToRefreshIndicator(
            state = state,
            isRefreshing = isRefreshing,
            containerColor = containerColor,
            threshold = threshold,
        ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = 100), label = "Test"
        ) { refreshing ->
            if (refreshing) {
                Icon(
                    modifier = Modifier
                        .size(LocalDimensions.current.iconNormal)
                        .rotate(angle),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(LocalDimensions.current.iconNormal),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        }
    }
}