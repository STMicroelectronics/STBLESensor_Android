package com.st.nfc_writing.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.json_nfc.request.JsonCommand
import com.st.nfc_writing.NfcWritingViewModel
import com.st.ui.theme.LocalDimensions


@Composable
fun NfcWritingDemoContent(
    modifier: Modifier = Modifier,
    viewModel: NfcWritingViewModel,
    nodeId: String
) {

    val supportedModes by viewModel.supportedModes.collectAsStateWithLifecycle()

    var numberOfRecords by remember { mutableIntStateOf(0) }

    if (supportedModes.supportedModes.value != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingMedium)
        ) {
            if (supportedModes.supportedModes.value!!.Answer.toString()
                    .contains(JsonCommand.NFCText)
            ) {
                NfcTextView(viewModel = viewModel, nodeId = nodeId, expanded = true)
                numberOfRecords += 1
            }

            if (supportedModes.supportedModes.value!!.Answer.toString()
                    .contains(JsonCommand.NFCURL)
            ) {
                NfcUrlView(viewModel = viewModel, nodeId = nodeId, expanded = true)
                numberOfRecords += 1
            }

            if (supportedModes.supportedModes.value!!.Answer.toString()
                    .contains(JsonCommand.NFCWifi)
            ) {
                NfcWiFiView(viewModel = viewModel, nodeId = nodeId, expanded = numberOfRecords < 2)
                numberOfRecords += 1
            }

            if (supportedModes.supportedModes.value!!.Answer.toString()
                    .contains(JsonCommand.NFCVCard)
            ) {
                NfcVCardView(viewModel = viewModel, nodeId = nodeId, expanded = numberOfRecords < 2)
                //numberOfRecords += 1
            }

        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = "Waiting supported Modes from Node"
            )
        }
    }
}