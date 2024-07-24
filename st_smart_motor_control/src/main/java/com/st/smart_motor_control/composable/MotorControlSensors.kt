package com.st.smart_motor_control.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.ui.composables.CommandRequest
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.json.JsonObject
import com.st.pnpl.composable.Component

@Composable
fun MotorControlSensors(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sensorsActuators: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> = emptyList(),
    status: List<JsonObject>,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit
) {
    var isOpen by rememberSaveable(sensorsActuators) { mutableStateOf(value = "") }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
        //verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        itemsIndexed(sensorsActuators) { _, componentWithInterface ->
            val name = componentWithInterface.first.name
            val data = (status.find { it.containsKey(name) })?.get(name)
            Component(
                modifier = modifier.padding(bottom = LocalDimensions.current.paddingMedium),
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
                onOpenComponent = {
                    isOpen = if (it == isOpen) "" else it
                }
            )

//            if (sensorsActuators.lastIndex != index) {
//                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
//            }
        }
    }
}