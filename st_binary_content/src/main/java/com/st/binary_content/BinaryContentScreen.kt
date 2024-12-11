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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.st.ui.composables.BlueMSDialogCircularProgressIndicator
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.toInt

@OptIn(ExperimentalMaterial3Api::class)
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
    val chunkProgress = viewModel.chunkProgress.value
    val isSendingOperationOnGoing = viewModel.isSendingOperationOnGoing.value
    val enableCollapse = viewModel.enableCollapse.value
    val binaryContentReceived = viewModel.binaryContentReceived.value
    val binaryContentReadyForSending = viewModel.binaryContentReadyForSending.value
    val fileOperationResultVisible = viewModel.fileOperationResultVisible.value

    val bytesRec = viewModel.bytesRec.value
    val numberPackets = viewModel.numberPackets.value
    val maxBinaryContentWriteSize = viewModel.maxBinaryContentWriteSize.value

    val pullRefreshState = rememberPullToRefreshState()

    var isOpen by rememberSaveable(contents) { mutableStateOf(value = "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
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

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {

            Column {

                Text(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingMedium,
                        bottom = LocalDimensions.current.paddingNormal,
                        top = LocalDimensions.current.paddingNormal,
                        end = LocalDimensions.current.paddingNormal
                    ),
                    text = "File Management",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isSendingOperationOnGoing) {
                    Text(
                        text = "..operation on going..",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
                    )
                } else {

                    MyExtendedFloatingActionButton(
                        operation = {
                            viewModel.hideFileOperation()
                            pickBinaryFileForBoardLauncher.launch(arrayOf("*/*"))
                        },
                        idIcon = R.drawable.ic_file_upload,
                        idString = R.string.st_loadFromFile,
                        modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
                    )

                    if (numberPackets != 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(LocalDimensions.current.paddingNormal),
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

                    if (binaryContentReadyForSending) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(LocalDimensions.current.paddingSmall),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            var text by remember { mutableStateOf(maxBinaryContentWriteSize.toString()) }

                            OutlinedTextField(
                                value = text,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                onValueChange = {
                                    text = it
                                    viewModel.setMaxBinaryContentWriteSize(it.toInt())
                                },
                                label = { Text("BLE Write Size") },
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .padding(end = LocalDimensions.current.paddingNormal)
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
                    modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

                FileOperationResult(fileOperationResultVisible, viewModel.fileOperationResult)
            }
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        CompositionLocalProvider(
            LocalLastStatusUpdatedAt provides lastStatusUpdatedAt
        ) {
            BlueMSPullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = isLoading,
                isBetaRelease = viewModel.isBeta,
                onRefresh = { viewModel.getModel(nodeId = nodeId, compName = "control") }) {
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
                            onBeforeUcf = {},
                            onAfterUcf = {},
                            onOpenComponent = {
                                isOpen = if (it == isOpen) "" else it
                            }
                        )

                        if (contents.lastIndex != index) {
                            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
                        }
                    }
                }
            }
        }
    }

    if (isSendingOperationOnGoing) {
        BlueMSDialogCircularProgressIndicator(
            percentage = if (chunkProgress != null) {
                chunkProgress.current * 100f / chunkProgress.total
            } else {
                null
            },
            message = "Sending", colorFill = null
        )
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
                .padding(LocalDimensions.current.paddingNormal),
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
            containerColor = MaterialTheme.colorScheme.primary,
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
