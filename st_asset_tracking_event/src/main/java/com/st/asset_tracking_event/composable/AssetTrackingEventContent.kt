package com.st.asset_tracking_event.composable


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.asset_tracking_event.AssetTrackingEventFViewModel
import com.st.asset_tracking_event.R
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey10
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.NotActiveColor
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningPressed
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun SegmentedMeter(
    modifier: Modifier = Modifier,
    segmentColors: List<Color>,
    currentLevel: Short,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (i in 1..10) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    //.width(15.dp)
                    .height(15.dp)
                    .background(
                        if (i <= currentLevel) segmentColors[when (i) {
                            in 1..3 -> 0
                            in 4..6 -> 1
                            in 7..8 -> 2
                            else -> 3
                        }] else Color.Transparent, shape = Shapes.extraSmall
                    )
                    .border(1.dp, Grey6, shape = Shapes.extraSmall)
            )
        }
    }
}

@Preview
@Composable
private fun SegmentedMeterPreview1() {
    PreviewBlueMSTheme {
        SegmentedMeter(
            segmentColors = listOf(SuccessText, PrimaryYellow, WarningPressed, ErrorText),
            currentLevel = 1
        )
    }
}

@Preview
@Composable
private fun SegmentedMeterPreview4() {
    PreviewBlueMSTheme {
        SegmentedMeter(
            segmentColors = listOf(SuccessText, PrimaryYellow, WarningPressed, ErrorText),
            currentLevel = 4
        )
    }
}

@Preview
@Composable
private fun SegmentedMeterPreview10() {
    PreviewBlueMSTheme {
        SegmentedMeter(
            segmentColors = listOf(SuccessText, PrimaryYellow, WarningPressed, ErrorText),
            currentLevel = 10
        )
    }
}

@Composable
fun AssetTrackingEventContent(
    modifier: Modifier,
    viewModel: AssetTrackingEventFViewModel
) {

    val assetTrackingStatus by viewModel.assetTrackingStatus.collectAsStateWithLifecycle()

    val trackingEventsData by viewModel.trackingEventsData.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val numberOfFallEvents by remember {
        derivedStateOf {
            trackingEventsData.filter { it.second.type == AssetTrackingEventType.Fall }.size
        }
    }

    val numberOfShockEvents by remember {
        derivedStateOf {
            trackingEventsData.filter { it.second.type == AssetTrackingEventType.Shock }.size
        }
    }

    var showShockInfoDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Pair<Long, AssetTrackingEventData>?>(null) }

    var openFilter by remember { mutableStateOf(false) }

    var showFallEvent by rememberSaveable { mutableStateOf(true) }
    var showShockEvent by rememberSaveable { mutableStateOf(true) }

    val trackingEventsDataFiltered by remember (showFallEvent, showShockEvent, trackingEventsData) {
        derivedStateOf {
            trackingEventsData.filter {
                (showFallEvent && it.second.type == AssetTrackingEventType.Fall) ||
                        (showShockEvent && it.second.type == AssetTrackingEventType.Shock)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
//                modifier = Modifier.padding(
//                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
//                ),
                containerColor = SecondaryBlue,
                onClick = { openFilter = true }) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .consumeWindowInsets(paddingValues),
            //.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                text = "Summary"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                AssetTrackingEventSummaryEvent(
                    description = "Fall Detected",
                    numEvents = numberOfFallEvents,
                    painter = painterResource(R.drawable.event_fall)
                )

                AssetTrackingEventSummaryEvent(
                    description = "Shock Detected",
                    numEvents = numberOfShockEvents,
                    painter = painterResource(R.drawable.event_shock)
                )
            }

//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        start = LocalDimensions.current.paddingMedium,
//                        end = LocalDimensions.current.paddingMedium
//                    ),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    style = MaterialTheme.typography.titleSmall,
//                    color = Grey6,
//                    text = "Total Events"
//                )
//                Text(
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary,
//                    text = "${trackingEventsDataFiltered.size}"
//                )
//            }
//
//            HorizontalDivider(
//                Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        start = LocalDimensions.current.paddingMedium,
//                        end = LocalDimensions.current.paddingMedium
//                    ),
//                thickness = 1.dp, color = Grey3
//            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LocalDimensions.current.paddingMedium,
                        end = LocalDimensions.current.paddingMedium
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = MaterialTheme.typography.titleSmall,
                    color = Grey6,
                    text = "Last event time"
                )
                if (trackingEventsDataFiltered.isEmpty()) {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        text = "-"
                    )
                } else {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        text = trackingEventsDataFiltered.last().first.convertLongToTime()
                    )
                }
            }

            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LocalDimensions.current.paddingMedium,
                        end = LocalDimensions.current.paddingMedium
                    ),
                thickness = 1.dp, color = Grey3
            )

            if (assetTrackingStatus != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LocalDimensions.current.paddingMedium,
                            end = LocalDimensions.current.paddingMedium
                        ),
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        color = Grey6,
                        textAlign = TextAlign.Start,
                        text = "Status:"
                    )
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryBlue,
                        text = assetTrackingStatus!!.first.name
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LocalDimensions.current.paddingMedium,
                            end = LocalDimensions.current.paddingMedium
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                    ) {
                        Text(
                            style = MaterialTheme.typography.titleSmall,
                            color = Grey6,
                            text = "Amps:"
                        )
                        Text(
                            style = MaterialTheme.typography.titleSmall,
                            color = PrimaryBlue,
                            text = "${assetTrackingStatus!!.second.current} [µA]"
                        )
                    }

                    SegmentedMeter(
                        modifier = Modifier.weight(1f),
                        segmentColors = listOf(
                            SuccessText,
                            PrimaryYellow,
                            WarningPressed,
                            ErrorText
                        ),
                        currentLevel = assetTrackingStatus!!.second.powerIndex
                    )
                }


            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LocalDimensions.current.paddingMedium,
                            end = LocalDimensions.current.paddingMedium
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        color = Grey6,
                        text = "Total Events"
                    )
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        text = "${trackingEventsDataFiltered.size}"
                    )
                }
            }
            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LocalDimensions.current.paddingMedium,
                        end = LocalDimensions.current.paddingMedium
                    ),
                thickness = 1.dp, color = Grey3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Events:"
                )

                BlueMsButton(
                    text = "Clear", onClick = { viewModel.clearEventList() }
                    //iconPainter = painterResource(R.drawable.event_reset)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                state = lazyListState,
                contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
            ) {
                item {
                    Spacer(
//                        Modifier.windowInsetsBottomHeight(
//                            WindowInsets.systemBars
//                        )
                        modifier = Modifier.height(
                            WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                        )
                    )
                }

                itemsIndexed(items = trackingEventsDataFiltered) { index, item ->
                    AssetTrackingEventItem(
                        modifier = Modifier.animateItem(),
                        timestamp = item.first,
                        event = item.second,
                        onShockInfoClick = {
                            selectedEvent = item
                            showShockInfoDialog = true
                        },
//                        isTheLastElement = index == trackingEventsDataFiltered.size - 1
                    )
                }
            }
        }

        LaunchedEffect(trackingEventsDataFiltered.size) {
            if (trackingEventsDataFiltered.isNotEmpty()) {
                coroutineScope.launch {
                    if (trackingEventsDataFiltered.size > 1) {
                        lazyListState.animateScrollToItem(trackingEventsDataFiltered.size - 1)
                    }
                }
            }
        }
    }

    if (showShockInfoDialog) {
        selectedEvent?.let { event ->
            ShockEventInfoDetails(event.second, onDismissRequest = { showShockInfoDialog = false })
        }
    }

    if (openFilter) {
        FilterSelectionDialog(
            onDismissRequest = { openFilter = false },
            showFallEvent = showFallEvent,
            onFallCheckChanged = { showFallEvent = it },
            showShockEvent = showShockEvent,
            onShockCheckChanged = { showShockEvent = it })
    }
}

@Composable
private fun FilterSelectionDialog(
    onDismissRequest: () -> Unit,
    showFallEvent: Boolean,
    onFallCheckChanged: (Boolean) -> Unit,
    showShockEvent: Boolean,
    onShockCheckChanged: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    text = "Filter Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                HorizontalDivider(thickness = 1.dp, color = Grey3)

                Text(
                    modifier = Modifier
                        .padding(
                            top = LocalDimensions.current.paddingMedium,
                            bottom = LocalDimensions.current.paddingMedium
                        )
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue,
                    text = "Select which events you want to see:"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EventTypeChip(
                        eventName = "Shock",
                        checked = showShockEvent,
                        onCheckedChange = { onShockCheckChanged(it) })

                    EventTypeChip(
                        eventName = "Fall",
                        checked = showFallEvent,
                        onCheckedChange = { onFallCheckChanged(it) })
                }

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    BlueMsButton(
                        text = "Ok",
                        onClick = onDismissRequest
                    )
                }
            }
        }
    }
}


@Composable
fun EventTypeChip(
    modifier: Modifier = Modifier,
    eventName: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = { /** NOOP **/ }
) {
    Surface(
        //modifier = modifier.padding(horizontal = LocalDimensions.current.paddingNormal),
        modifier = modifier,
        onClick = { onCheckedChange(checked.not()) },
        shape = Shapes.medium,
        color = if (checked) PrimaryBlue else NotActiveColor,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            modifier = Modifier.padding(
                top = LocalDimensions.current.paddingNormal,
                bottom = LocalDimensions.current.paddingNormal,
                start = LocalDimensions.current.paddingMedium,
                end = LocalDimensions.current.paddingMedium
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) Grey0 else Grey10,
            text = eventName
        )
    }
}

fun Long.convertLongToTime(): String {
    val localDate = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val time = LocalTime.now()
    return LocalDateTime.of(localDate, time).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
}