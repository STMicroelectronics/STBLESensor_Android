package com.st.asset_tracking_event.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.asset_tracking_event.AssetTrackingEventFViewModel
import com.st.ui.theme.LocalDimensions
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AssetTrackingEventContentOld(
    modifier: Modifier,
    viewModel: AssetTrackingEventFViewModel
) {

    val lazyListState = rememberLazyListState()
    val trackingEventsData by viewModel.trackingEventsData.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal
            ),
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = LocalDimensions.current.paddingMedium),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = "Events"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            //reverseLayout = true,
            state = lazyListState,
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            itemsIndexed(items = trackingEventsData) { index, item ->
                AssetTrackingEventItemOld(modifier = Modifier.animateItem(), index = index, timestamp = item.first, event = item.second, isTheLastElement = index == trackingEventsData.size - 1)
            }
        }
    }

    LaunchedEffect(trackingEventsData.size) {
        if (trackingEventsData.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(trackingEventsData.size - 1)
            }
        }
    }
}