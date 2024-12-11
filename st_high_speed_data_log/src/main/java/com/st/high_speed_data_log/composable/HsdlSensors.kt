package com.st.high_speed_data_log.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.st.high_speed_data_log.ComponentWithInterface
import com.st.pnpl.composable.Component
import com.st.ui.composables.CommandRequest
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.json.JsonObject

@Composable
fun HsdlSensors(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sensors: List<ComponentWithInterface> = emptyList(),
    status: List<JsonObject>,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onAfterUcf: () -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    state: LazyListState
) {
    var isOpen by rememberSaveable(sensors) { mutableStateOf(value = "") }

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = LocalDimensions.current.paddingNormal,
            end = LocalDimensions.current.paddingNormal,
            top = LocalDimensions.current.paddingNormal)
    ) {
        itemsIndexed(sensors) { _, componentWithInterface ->
            val name = componentWithInterface.first.name
            val data = (status.find { it.containsKey(name) })?.get(name)
            Component(
                modifier = modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                name = name,
                data = data,
                enabled = isLoading.not(),
                enableCollapse = true,
                isOpen = isOpen == name,
                showNotMounted = false,
                componentModel = componentWithInterface.first,
                interfaceModel = componentWithInterface.second,
                onValueChange = { onValueChange(name, it) },
                onSendCommand = { onSendCommand(name, it) },
                onBeforeUcf = onBeforeUcf,
                onAfterUcf = onAfterUcf,
                onOpenComponent = {
                    isOpen = if (it == isOpen) "" else it
                }
            )
        }

        item {
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBars
                )
            )
        }
    }
}