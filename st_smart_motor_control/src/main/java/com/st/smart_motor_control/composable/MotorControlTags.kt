package com.st.smart_motor_control.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.pnpl.composable.Component
import com.st.ui.composables.CommandRequest
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.json.JsonObject

@Composable
fun MotorControlTags(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    tags: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> = emptyList(),
    status: List<JsonObject>,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    lazyState: LazyListState
) {
    /** NOOP **/
    LazyColumn(
        state = lazyState,
        modifier = modifier.fillMaxSize(),
        contentPadding =  PaddingValues(start = LocalDimensions.current.paddingNormal,
            end = LocalDimensions.current.paddingNormal,
            top = LocalDimensions.current.paddingNormal)
    ) {
        itemsIndexed(tags) { _, componentWithInterface ->
            val name = componentWithInterface.first.name
            val data = (status.find { it.containsKey(name) })?.get(name)
            /** NOOP **/
            /** NOOP **/
            Component(
                modifier = modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                enabled = isLoading.not(),
                name = name,
                data = data,
                enableCollapse = false,
                isOpen = true,
                showNotMounted = false,
                componentModel = componentWithInterface.first,
                interfaceModel = componentWithInterface.second,
                onValueChange = { onValueChange(name, it) },
                onSendCommand = { onSendCommand(name, it) },
                onBeforeUcf = {},
                onAfterUcf = {},
                onOpenComponent = { /** NOOP **/ }
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