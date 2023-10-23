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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.st.pnpl.composable.Component
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.toInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BinaryScreenScreen(
    modifier: Modifier,
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

    val bytesRec = viewModel.bytesRec.value
    val numberPackets = viewModel.numberPackets.value

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
            ActivityResultContracts.CreateDocument("*/*")
        ) { fileUri ->
            if (fileUri != null) {
                viewModel.saveBinaryContent(fileUri)
            }
        }

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
                pickBinaryFileForBoardLauncher.launch(arrayOf("*/*"))
            },
            idIcon = R.drawable.ic_file_upload,
            idString = R.string.st_loadFromFile,
            modifier = Modifier.padding(LocalDimensions.current.paddingSmall)
        )

        if(numberPackets!=0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LocalDimensions.current.paddingSmall),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Packets = $numberPackets",
                    modifier = Modifier.weight(0.5f)
                )

                Text(
                    text = "Bytes = $bytesRec",
                    modifier = Modifier.weight(0.5f)
                )
            }

        }

        if(binaryContentReadyForSending) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LocalDimensions.current.paddingSmall),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                var text by remember { mutableStateOf("20") }

                OutlinedTextField(
                    value = text,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        text = it
                        viewModel.maxPayloadSize = it.toInt()
                    },
                    label = { Text("Write Size") },
                    modifier = Modifier.weight(0.5f)
                )

                MyExtendedFloatingActionButton(
                    operation = {
                        viewModel.hideFileOperation()
                        viewModel.writeBinaryContentToNode(nodeId)
                    },
                    idIcon = R.drawable.ic_bluetooth,
                    idString = R.string.st_sendToBoard,
                    modifier = Modifier.weight(0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        MyExtendedFloatingActionButton(
            operation = {
                viewModel.hideFileOperation()
                pickBinaryFileFromBoardLauncher.launch("Binary.bin")
            },
            isVisible = binaryContentReceived,
            idIcon = R.drawable.ic_file_download,
            idString = R.string.st_saveToFile,
            modifier = Modifier.padding(LocalDimensions.current.paddingSmall)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingSmall),
            horizontalArrangement = Arrangement.End
        ) {
            MyExtendedFloatingActionButton(
                operation = {
                    pickFileLauncher.launch(arrayOf(JSON_FILE_TYPE))
                },
                idIcon = R.drawable.ic_load_dtdl,
                idString = R.string.st_pnpl_uploadDtmiFile,
                modifier = Modifier.padding(LocalDimensions.current.paddingSmall)
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))


        CompositionLocalProvider(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingSmall),
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
    modifier: Modifier,
    operation: () -> Unit,
    isVisible: Boolean = true,
    @DrawableRes idIcon: Int,
    @StringRes idString: Int
) {
    if (isVisible) {
        ExtendedFloatingActionButton(
            modifier = modifier,
            onClick = operation,
            backgroundColor =  MaterialTheme.colorScheme.primary,
            icon = {
                Icon(
                    painter = painterResource(id = idIcon),
                    modifier = Modifier.size(size = 32.dp),
                    contentDescription = null,
                    tint = Grey0
                )
            },
            text = { Text(text = stringResource(id = idString), color = Grey0) }
        )
    }
}
