/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.st.blue_sdk.models.ConnectionStatus
import com.st.blue_sdk.models.NodeState
import com.st.bluems.R
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.Shapes
import com.st.ui.theme.WarningText

@Composable
fun ConnectionStatusDialog(
    isPairingRequest: Boolean = false,
    connectionStatus: ConnectionStatus,
    boardName: String
) {
    when (connectionStatus.current) {
        NodeState.Connecting -> NodeConnectingDialog(boardName, isPairingRequest)
        NodeState.Connected -> NodeConnectedDialog(boardName)
        NodeState.Disconnecting -> NodeDisconnectingDialog(boardName)
        NodeState.ServicesDiscovered -> Unit
        NodeState.Ready -> Unit
        NodeState.Disconnected -> Unit
    }
}

@Composable
fun NodeConnectingDialog(boardName: String, isPairingRequest: Boolean = false) {
    Dialog(onDismissRequest = { /** NOOP **/ }) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_home_connectionStatus_connectingTitle)
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                if (isPairingRequest) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = LocalDimensions.current.paddingMedium)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarningText,
                        text = "Default PIN 123456"
                    )
                }

                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(1f))

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = stringResource(
                        id = R.string.st_home_connectionStatus_connectingDescription,
                        boardName
                    )
                )
            }
        }
    }
}

@Composable
fun NodeConnectedDialog(boardName: String) {
    Dialog(onDismissRequest = { /** NOOP **/ }) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_home_connectionStatus_connectedTitle)
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(1f))

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = stringResource(
                        id = R.string.st_home_connectionStatus_connectedDescription,
                        boardName
                    )
                )
            }
        }
    }
}

@Composable
fun NodeDisconnectingDialog(boardName: String) {
    Dialog(onDismissRequest = { /** NOOP **/ }) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_home_connectionStatus_disconnectingTitle)
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(1f))

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = stringResource(
                        id = R.string.st_home_connectionStatus_disconnectingDescription,
                        boardName
                    )
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusDialogPreview() {
    PreviewBlueMSTheme {
        ConnectionStatusDialog(
            connectionStatus = ConnectionStatus(
                prev = NodeState.Disconnected,
                current = NodeState.Connecting
            ),
            boardName = "STMicro"
        )
    }
}
