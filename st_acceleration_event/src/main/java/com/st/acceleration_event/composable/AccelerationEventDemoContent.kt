package com.st.acceleration_event.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.acceleration_event.AccelerationEventViewModel
import com.st.acceleration_event.model.getDefaultEvent
import com.st.acceleration_event.model.getDetectableEvent
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.LocalDimensions

@Composable
fun AccelerationEventDemoContent(
    modifier: Modifier,
    viewModel: AccelerationEventViewModel,
    nodeId: String
) {

    val nodeType = viewModel.getBoardType(nodeId)

    var mCurrentEvent by remember { mutableStateOf(value = getDefaultEvent(nodeType)) }

    val accEventData by viewModel.accEventData.collectAsStateWithLifecycle()

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                //Set Default Event Detectable
                val defaultEvent = getDefaultEvent(nodeType)
//                val detectableEvents = getDetectableEvent(nodeType)
//                if (detectableEvents.contains(DetectableEventType.Multiple)) {
//                        viewModel.setDetectableEventCommand(
//                            nodeId,
//                            DetectableEventType.Multiple,
//                            false
//                        )
//                    }  else {
                viewModel.setDetectableEventCommand(
                    nodeId,
                    DetectableEventType.Multiple,
                    false
                )
//                }
                viewModel.setDetectableEventCommand(nodeId, defaultEvent, true)
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = LocalDimensions.current.paddingNormal,
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {


        AccEventDropDownMenu(
            title = "Event:",
            initialValue = mCurrentEvent.name,
            values = getDetectableEvent(nodeType).map { it.name },
            onValueSelected = { eventSelected ->

                //disable the current one
                viewModel.setDetectableEventCommand(nodeId, mCurrentEvent, false)

                mCurrentEvent = getDetectableEvent(nodeType).first { it.name == eventSelected }

                //enable the new one
                viewModel.setDetectableEventCommand(nodeId, mCurrentEvent, true)
            })

        if (mCurrentEvent == DetectableEventType.Multiple) {
            AccMultipleEventsContents(
                accEventData = accEventData
            )
        } else {
            AccSingleEventContent(
                mCurrentEvent = mCurrentEvent,
                accEventData = accEventData
            )
        }
    }
}