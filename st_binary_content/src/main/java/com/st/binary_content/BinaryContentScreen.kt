/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.binary_content

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.st.pnpl.composable.Component
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.json.JsonObject

val LocalLastStatus = compositionLocalOf { emptyList<JsonObject>() }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BinaryScreenScreen(
    modifier: Modifier = Modifier,
    viewModel: BinaryContentViewModel,
    nodeId: String
) {
    val lastStatusUpdatedAt = viewModel.lastStatusUpdatedAt.value
    val contents = viewModel.modelUpdates.value
    val status = viewModel.componentStatusUpdates.value
    val isLoading = viewModel.isLoading.value
    val enableCollapse = viewModel.enableCollapse.value
    val binaryContentReceived = viewModel.binaryContentReceived.value
    val binaryContentReadyForSending = viewModel.binaryContentReadyForSending.value
    val fileOperationResultVisible = viewModel.fileOperationResultVisible.value

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            viewModel.getModel(nodeId = nodeId, compName = "control")
        }
    )
    var isOpen by rememberSaveable(contents) { mutableStateOf(value = "") }

    Column(modifier = modifier.fillMaxWidth()) {

        val pickFileLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { fileUri ->
            if (fileUri != null) {
                viewModel.setDtmiModel(nodeId, fileUri)
            }
        }

        val pickBinaryFileFromBoardLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/*")
        ) { fileUri ->
            if (fileUri != null) {
                viewModel.saveBinaryContent(fileUri)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            val pickBinaryFileForBoardLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { fileUri ->
                if (fileUri != null) {
                    viewModel.readBinaryContent(fileUri)
                }
            }

            MyExtendedFloatingActionButton(
                operation = {
                    viewModel.hideFileOperation()
                    pickBinaryFileForBoardLauncher.launch(arrayOf("application/*"))
                },
                idIcon = R.drawable.ic_file_upload,
                idString = R.string.st_loadFromFile
            )

            Spacer(modifier = Modifier.weight(weight = 1.0f))

            MyExtendedFloatingActionButton(
                operation = {
                    viewModel.hideFileOperation()
                    viewModel.writeBinaryContentToNode(nodeId)
                },
                isVisible = binaryContentReadyForSending,
                idIcon = R.drawable.ic_bluetooth,
                idString = R.string.st_readyToSend
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        MyExtendedFloatingActionButton(
            operation = {
                viewModel.hideFileOperation()
                pickBinaryFileFromBoardLauncher.launch("Binary.bin")
            },
            isVisible = binaryContentReceived,
            idIcon = R.drawable.ic_file_download,
            idString = R.string.st_saveToFile
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
        FileOperationResult(fileOperationResultVisible, viewModel.fileOperationResult)
//        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            MyExtendedFloatingActionButton(
                operation = {
                    pickFileLauncher.launch(arrayOf(JSON_FILE_TYPE))
                },
                idIcon = R.drawable.ic_load_dtdl,
                idString = R.string.st_pnpl_uploadDtmiFile
            )
        }



        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))


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

@Composable
fun FileOperationResult(
    isVisible: Boolean,
    text: String?
) {
    if ((isVisible) && (text != null)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Result:")
            Spacer(modifier = Modifier.size(size = 24.dp))
            Text(text)
        }
    }
}

@Composable
fun MyExtendedFloatingActionButton(
    operation: () -> Unit,
    isVisible: Boolean = true,
    @DrawableRes idIcon: Int,
    @StringRes idString: Int
) {
    if (isVisible) {
        ExtendedFloatingActionButton(
            onClick = operation,
            icon = {
                Icon(
                    painter = painterResource(id = idIcon),
                    modifier = Modifier.size(size = 32.dp),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(id = idString)) }
        )
    }
}
