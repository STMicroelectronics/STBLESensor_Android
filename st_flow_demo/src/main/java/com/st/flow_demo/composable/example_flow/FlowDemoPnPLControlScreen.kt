package com.st.flow_demo.composable.example_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.ui.composables.ComposableLifecycle
import com.st.pnpl.composable.Component
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoPnPLControlScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {

    val nodeId = viewModel.getNodeId()
    val pnplComponentName =
        (viewModel.getRunningFlowFromOptionBytes() ?: "control").lowercase().replace(' ', '_')
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                nodeId?.let {
                    viewModel.getModel(nodeId = nodeId, compName = pnplComponentName)
                }
            }

            else -> Unit
        }
    }

    val lastStatusUpdatedAt = viewModel.lastStatusUpdatedAt.value
    val contents = viewModel.modelUpdates.value
    val status = viewModel.componentStatusUpdates.value
    val isLoading = viewModel.isLoading.value
    val enableCollapse = viewModel.enableCollapse.value

    nodeId?.let {
        var isOpen by rememberSaveable(contents) { mutableStateOf(value = "") }

        CompositionLocalProvider(
            LocalLastStatusUpdatedAt provides lastStatusUpdatedAt
        ) {
            Box(modifier = Modifier.padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = LocalDimensions.current.paddingNormal,
                        start = LocalDimensions.current.paddingNormal,
                        end = LocalDimensions.current.paddingNormal
                    ),
                    verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                ) {
                    itemsIndexed(contents) { _, componentWithInterface ->
                        val name = componentWithInterface.first.name
                        val data = (status.find { it.containsKey(name) })?.get(name)
                        Component(
                            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
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
                            onBeforeUcf = {},
                            onAfterUcf = {},
                            onOpenComponent = {
                                isOpen = if (it == isOpen) "" else it
                            }
                        )
                    }

                    if(contents.isEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.fillMaxWidth().padding(top = LocalDimensions.current.paddingLarge,bottom = LocalDimensions.current.paddingLarge),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                text = "No Available Control Components"
                            )
                        }
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
        }
    }
}