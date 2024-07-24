package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.st.cloud_azure_iot_central.model.AzureCloudDevice
import com.st.cloud_azure_iot_central.model.CloudTemplateRetrieved
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzureCloudAddDeviceDialog(
    cloudTemplates: List<CloudTemplateRetrieved>,
    boardUid: String,
    deviceTemplate: String,
    onDismiss: () -> Unit = { /** NOOP**/ },
    onConfirmation: (AzureCloudDevice) -> Unit = { /** NOOP**/ }
) {

    val deviceTemplatesList = cloudTemplates.map {
        it.displayName ?: "Default Name"
    }

    var deviceName: String by remember { mutableStateOf("") }

    var uniqueId: String by remember {
        mutableStateOf(boardUid)
    }

    var expanded by remember { mutableStateOf(value = false) }

    var selectedTemplate by remember(deviceTemplate) { mutableStateOf(value = deviceTemplate) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "To create a new device: select a name, deviceId and one device template"
                )

                Text(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Device Name"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    value = deviceName,
                    onValueChange = {
                        deviceName = it
                    }
                )

                Text(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Device Id"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    value = uniqueId,
                    onValueChange = {
                        uniqueId = it
                    }
                )

                Text(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Device Template"
                )

                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal),
                    expanded = expanded,
                    onExpandedChange = { newValue ->
                        expanded = newValue
                    }
                ) {
                    TextField(
                        value = selectedTemplate,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        deviceTemplatesList.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    selectedTemplate = it
                                    expanded = false
                                },
                                text = {
                                    Text(it)
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    BlueMsButton(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        text = stringResource(id = android.R.string.cancel),
                        onClick = {
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (deviceName.isNotEmpty()) {
                        BlueMsButton(
                            text = stringResource(id = android.R.string.ok),
                            enabled = true,
                            onClick = {
                                val device = AzureCloudDevice(
                                    id = uniqueId,
                                    template = cloudTemplates.first { it.displayName == selectedTemplate }.id,
                                    displayName = deviceName
                                )
                                onConfirmation(device)
                            }
                        )
                    }
                }
            }
        }
    }
}