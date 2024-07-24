/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.ext_config.R
import com.st.ext_config.model.WifiCredentials
import com.st.ext_config.ui.ext_config.ExtConfigViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.EnumProperty
import com.st.ui.composables.Header
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.Shapes

@Composable
fun BoardSettingsCard(
    modifier: Modifier = Modifier,
    deviceName: String = "",
    showSetName: Boolean = true,
    onSetName: (String) -> Unit = { /** NOOP **/ },
    showReadCustomCommand: Boolean = true,
    onReadCustomCommand: () -> Unit = { /** NOOP **/ },
    showSetTime: Boolean = true,
    onSetTime: () -> Unit = { /** NOOP **/ },
    showSetDate: Boolean = true,
    onSetDate: () -> Unit = { /** NOOP **/ },
    showSensorConfiguration: Boolean = true,
    onSensorConfiguration: () -> Unit = { /** NOOP **/ },
    showSetWiFiCredentials: Boolean = true,
    onSetWiFiCredentials: (WifiCredentials) -> Unit = { /** NOOP **/ }
) {
    var isOpen by rememberSaveable(
        showSetName,
        showReadCustomCommand,
        showSetTime,
        showSetDate,
        showSensorConfiguration,
        showSetWiFiCredentials
    ) {
        mutableStateOf(
            value = showSetName || showReadCustomCommand || showSetTime ||
                    showSetDate || showSensorConfiguration || showSetWiFiCredentials
        )
    }
    var name by rememberSaveable { mutableStateOf(value = deviceName) }
    var openSetNameDialog by rememberSaveable { mutableStateOf(value = false) }
    var openSetWiFiDialog by rememberSaveable { mutableStateOf(value = false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = { isOpen = !isOpen }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Header(
                isOpen = isOpen,
                icon = Icons.Default.Settings,
                title = stringResource(id = R.string.st_extConfig_boardSettings_cardTitle)
            )

            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BoardSettingsContentCard(
                    showSetName = showSetName,
                    onSetName = {
                        if (showSetName) {
                            openSetNameDialog = true
                        }
                    },
                    showReadCustomCommand = showReadCustomCommand,
                    onReadCustomCommand = onReadCustomCommand,
                    showSetTime = showSetTime,
                    onSetTime = onSetTime,
                    showSetDate = showSetDate,
                    onSetDate = onSetDate,
                    showSensorConfiguration = showSensorConfiguration,
                    onSensorConfiguration = onSensorConfiguration,
                    showSetWiFiCredentials = showSetWiFiCredentials,
                    onSetWiFiCredentials = {
                        if (showSetWiFiCredentials) {
                            openSetWiFiDialog = true
                        }
                    }
                )
            }
        }
    }

    if (openSetNameDialog) {
        AlertDialog(
            onDismissRequest = { openSetNameDialog = false },
            title = {
                Text(text = stringResource(id = R.string.st_extConfig_boardSettings_setNameTitle))
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    supportingText = {
                        Text(
                            text = "${name.length} / ${ExtConfigViewModel.MAX_NAME_LEN}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    onValueChange = {
                        if (it.length <= ExtConfigViewModel.MAX_NAME_LEN) name = it
                    }
                )
            },
            dismissButton = {
                BlueMsButtonOutlined(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = { openSetNameDialog = false }
                )
            },
            confirmButton = {
                BlueMsButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        onSetName(name)
                        openSetNameDialog = false
                    }
                )
            }
        )
    }

    if (openSetWiFiDialog) {
        val securityTypeList = listOf("OPEN", "WEP", "WPA", "WPA2", "WPA/WPA2")
        var securityType by remember {
            mutableStateOf(value = securityTypeList.first())
        }
        var ssid by remember {
            mutableStateOf(value = "")
        }
        var psw by remember {
            mutableStateOf(value = "")
        }
        AlertDialog(
            onDismissRequest = { openSetWiFiDialog = false },
            title = {
                Text(text = stringResource(id = R.string.st_extConfig_boardSettings_setWiFiCredentials))
            },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ssid,
                        placeholder = {
                            Text(text = stringResource(id = R.string.st_extConfig_boardSettings_wifiSsid))
                        },
                        onValueChange = {
                            ssid = it
                        }
                    )
                    var showPassword by rememberSaveable {
                        mutableStateOf(value = false)
                    }

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = psw,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            autoCorrect = false
                        ),
                        trailingIcon = {
                            if (showPassword) {
                                IconButton(onClick = { showPassword = false }) {
                                    Icon(
                                        tint = MaterialTheme.colorScheme.primary,
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                IconButton(onClick = { showPassword = true }) {
                                    Icon(
                                        tint = MaterialTheme.colorScheme.primary,
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        visualTransformation =
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        placeholder = {
                            Text(text = stringResource(id = R.string.st_extConfig_boardSettings_wifiPassword))
                        },
                        onValueChange = {
                            psw = it
                        }
                    )

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    EnumProperty(
                        data = null,
                        label = stringResource(id = R.string.st_extConfig_boardSettings_wifiSecurityType),
                        initialValue = securityTypeList.first(),
                        values = securityTypeList.map { it to it },
                        onValueChange = {
                            securityType = it
                        }
                    )
                }
            },
            dismissButton = {
                BlueMsButtonOutlined(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = { openSetWiFiDialog = false }
                )
            },
            confirmButton = {
                BlueMsButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        onSetWiFiCredentials(
                            WifiCredentials(
                                ssid = ssid,
                                password = psw,
                                securityType = securityType
                            )
                        )
                        openSetWiFiDialog = false
                    }
                )
            }
        )
    }
}

@Composable
fun BoardSettingsContentCard(
    modifier: Modifier = Modifier,
    showSetName: Boolean = true,
    onSetName: () -> Unit = { /** NOOP **/ },
    showReadCustomCommand: Boolean = true,
    onReadCustomCommand: () -> Unit = { /** NOOP **/ },
    showSetTime: Boolean = true,
    onSetTime: () -> Unit = { /** NOOP **/ },
    showSetDate: Boolean = true,
    onSetDate: () -> Unit = { /** NOOP **/ },
    showSensorConfiguration: Boolean = true,
    onSensorConfiguration: () -> Unit = { /** NOOP **/ },
    showSetWiFiCredentials: Boolean = true,
    onSetWiFiCredentials: () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {
        val nameTextColor = if (showSetName) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable { onSetName() },
            color = nameTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSettings_setName)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val readCustomTextColor =
            if (showReadCustomCommand) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showReadCustomCommand) {
                    onReadCustomCommand()
                }
            },
            color = readCustomTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSettings_readCustomCommands)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val timeTextColor = if (showSetTime) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showSetTime) {
                    onSetTime()
                }
            },
            color = timeTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSettings_setTime)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val dateTextColor = if (showSetDate) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showSetDate) {
                    onSetDate()
                }
            },
            color = dateTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSettings_setDate)
        )

//        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
//
//        val sensTextColor = if (showSensorConfiguration) Grey6 else Grey6.copy(alpha = 0.3f)
//        Text(
//            modifier = Modifier.clickable {
//                if (showSensorConfiguration) {
//                    onSensorConfiguration()
//                }
//            },
//            color = sensTextColor,
//            style = MaterialTheme.typography.bodyMedium,
//            text = stringResource(id = R.string.st_extConfig_boardSettings_sensorsConfiguration)
//        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val wifiTextColor = if (showSetWiFiCredentials) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable { onSetWiFiCredentials() },
            color = wifiTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSettings_setWiFiCredentials)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardSettingsCardPreview() {
    PreviewBlueMSTheme {
        BoardSettingsCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardSettingsContentCardPreview() {
    PreviewBlueMSTheme {
        BoardSettingsContentCard()
    }
}
