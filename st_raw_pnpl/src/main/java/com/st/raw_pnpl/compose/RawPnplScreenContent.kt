package com.st.raw_pnpl.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlled.Companion.HIDE_PROPERTIES_NAME
import com.st.raw_pnpl.RawPnplViewModel
import com.st.pnpl.composable.Component
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun RawPnplScreenContent(
    modifier: Modifier,
    viewModel: RawPnplViewModel,
    nodeId: String
) {
    val lastStatusUpdatedAt = viewModel.lastStatusUpdatedAt.value
    val contents = viewModel.modelUpdates.value
    val status = viewModel.componentStatusUpdates.value
    val isLoading = viewModel.isLoading.value
    val enableCollapse = viewModel.enableCollapse.value

    val dataFeature = viewModel.dataFeature.collectAsState(initial = "")

    var isOpen by rememberSaveable(contents) { mutableStateOf(value = "") }

    CompositionLocalProvider(
        LocalLastStatusUpdatedAt provides lastStatusUpdatedAt
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            itemsIndexed(contents) { index, componentWithInterface ->
                val name = componentWithInterface.first.name
                val data = (status.find { it.containsKey(name) })?.get(name)
                Component(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingNormal),
                    hideProperties = HIDE_PROPERTIES_NAME,
                    name = name,
                    data = data,
                    enabled = isLoading.not(),
                    enableCollapse = enableCollapse,
                    isOpen = isOpen == name,
                    componentModel = componentWithInterface.first,
                    interfaceModel = componentWithInterface.second,
                    onValueChange = { value ->
                        viewModel.sendChange(
                            nodeId = nodeId,
                            name = name,
                            value = value
                        )
                    },
                    onSendCommand = { value ->
                        viewModel.sendCommand(
                            nodeId = nodeId,
                            name = name,
                            value = value
                        )
                    },
                    onOpenComponent = {
                        isOpen = if (it == isOpen) "" else it
                    }
                )

                if (contents.lastIndex != index) {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
                }
            }

            item {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = Shapes.small,
                    shadowElevation = LocalDimensions.current.elevationNormal
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        style = MaterialTheme.typography.bodyMedium,
                        text = dataFeature.value
                    )
                }
            }
        }
    }
}