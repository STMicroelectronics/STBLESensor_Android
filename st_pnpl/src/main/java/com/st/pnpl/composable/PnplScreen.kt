/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.pnpl.PnplViewModel
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.json.JsonObject

val LocalLastStatus = compositionLocalOf { emptyList<JsonObject>() }

@Composable
fun StPnplScreen(
    modifier: Modifier = Modifier,
    viewModel: PnplViewModel,
    nodeId: String,
    demoName: String?
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId, demoName = demoName)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    PnplScreen(
        modifier = modifier,
        viewModel = viewModel,
        nodeId = nodeId,
        demoName = demoName
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PnplScreen(
    modifier: Modifier = Modifier,
    viewModel: PnplViewModel,
    nodeId: String,
    demoName: String?
) {
    val lastStatusUpdatedAt by viewModel.lastStatusUpdatedAt.collectAsStateWithLifecycle()
    val contents by viewModel.modelUpdates.collectAsStateWithLifecycle()
    val status by viewModel.componentStatusUpdates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val enableCollapse by viewModel.enableCollapse.collectAsStateWithLifecycle()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            viewModel.getModel(nodeId = nodeId, demoName = demoName)
        }
    )
    var isOpen by rememberSaveable(contents) { mutableStateOf(value = "") }

    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(
            LocalLastStatus provides status,
            LocalLastStatusUpdatedAt provides lastStatusUpdatedAt
        ) {
            Box(modifier = Modifier.pullRefresh(state = pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                ) {
                    itemsIndexed(contents) { index, componentWithInterface ->
                        val name = componentWithInterface.first.name
                        val data = (status.find { it.containsKey(name) })?.get(name)
                        Component(
                            name = name,
                            data = data,
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
                }

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    scale = true
                )
            }
        }
    }
}
