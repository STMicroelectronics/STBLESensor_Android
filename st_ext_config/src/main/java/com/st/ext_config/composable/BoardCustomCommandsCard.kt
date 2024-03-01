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
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.features.extended.ext_configuration.CustomCommand
import com.st.ext_config.R
import com.st.ext_config.model.CustomCommandType
import com.st.ext_config.util.customCommandType
import com.st.ui.composables.*
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.Shapes
import com.st.ui.utils.fadedEdgeMarquee

@Composable
fun BoardCustomCommandsCard(
    modifier: Modifier = Modifier,
    commands: List<CustomCommand> = emptyList(),
    onSendCommand: (CustomCommand, Any?) -> Unit = { _, _ -> /** NOOP**/ }
) {
    var isOpen by rememberSaveable { mutableStateOf(value = true) }
    var openCommandDialog: CustomCommand? by rememberSaveable { mutableStateOf(value = null) }
    if (commands.isNotEmpty()) {
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
                    icon = Icons.Default.Build,
                    title = stringResource(id = R.string.st_extConfig_customCommands_cardTitle)
                )

                AnimatedVisibility(
                    visible = isOpen,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    BoardCustomCommandsContentCard(commands = commands, onSendCommand = { command ->
                        if (command.customCommandType != CustomCommandType.UNKNOWN) {
                            if (command.customCommandType == CustomCommandType.VOID) {
                                onSendCommand(command, null)
                            } else {
                                openCommandDialog = command
                            }
                        }
                    })
                }
            }
        }
    }

    if (openCommandDialog != null) {
        var args: Any? by remember { mutableStateOf(value = null) }
        var isValid: Boolean by remember { mutableStateOf(value = false) }
        AlertDialog(
            onDismissRequest = { openCommandDialog = null },
            title = {
                Text(text = openCommandDialog?.name ?: "")
            },
            text = {
                CustomCommandInput(
                    command = openCommandDialog,
                    onChangeValue = {arg, valid ->
                        args = arg
                        isValid = valid
                    }
                )
            },
            dismissButton = {
                BlueMsButtonOutlined(
                    onClick = { openCommandDialog = null },
                    text = stringResource(id = android.R.string.cancel)
                )
            },
            confirmButton = {
                BlueMsButton(
                    enabled = isValid,
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        openCommandDialog?.let {
                            onSendCommand(it, args)

                            openCommandDialog = null
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun CustomCommandInput(
    command: CustomCommand? = null,
    onChangeValue: (Any?, Boolean) -> Unit = { _, _ -> /** NOOP **/ }
) {
    when (command?.customCommandType) {
        CustomCommandType.INTEGER -> {
            val initialValue = command.default ?: 0

            LaunchedEffect(key1 = command) {
                onChangeValue(initialValue, false)
            }

            IntegerProperty(
                value = initialValue,
                minValue = command.min,
                maxValue = command.max,
                commandBehavior = true,
                onValueChange = { value, valid ->
                    onChangeValue(value, valid)
                }
            )
        }

        CustomCommandType.STRING -> {
            val initialValue = ""

            LaunchedEffect(key1 = command) {
                onChangeValue(initialValue, false)
            }

            StringProperty(
                value = initialValue,
                minLength = command.min,
                maxLength = command.max,
                commandBehavior = true,
                onValueChange = { value, valid ->
                    onChangeValue(value, valid)
                }
            )
        }

        CustomCommandType.BOOLEAN -> {
            val initialValue = command.default != 0

            LaunchedEffect(key1 = command) {
                onChangeValue(if (initialValue) 1 else 0, true)
            }

            BooleanProperty(value = initialValue, onValueChange = {
                onChangeValue(if (it) 1 else 0, true)
            })
        }

        CustomCommandType.ENUMSTRING -> {
            if (command.stringValues.isNullOrEmpty().not()) {
                val initialValue = if (command.default != null) {
                    command.stringValues?.get(command.default!!)
                } else {
                    command.stringValues?.first()
                }
                LaunchedEffect(key1 = command) {
                    onChangeValue(initialValue, true)
                }

                EnumProperty(
                    data = null,
                    label = command.name ?: "",
                    initialValue = initialValue!!,
                    values = command.stringValues?.map { it to it } ?: emptyList(),
                    onValueChange = {
                        onChangeValue(it, true)
                    }
                )
            }
        }

        CustomCommandType.ENUMINTEGER -> {
            if (command.integerValues.isNullOrEmpty().not()) {
                val initialValue = if (command.default != null) {
                    command.integerValues?.get(command.default!!)
                } else {
                    command.integerValues?.first()
                }
                LaunchedEffect(key1 = command) {
                    onChangeValue(initialValue, true)
                }

                EnumProperty(
                    data = null,
                    label = command.name ?: "",
                    initialValue = initialValue!!,
                    values = command.integerValues?.map { "$it" to it } ?: emptyList(),
                    onValueChange = {
                        onChangeValue(it, true)
                    }
                )
            }
        }

        CustomCommandType.VOID -> Unit
        CustomCommandType.UNKNOWN -> Unit
        else -> Unit
    }
}

@Composable
fun BoardCustomCommandsContentCard(
    modifier: Modifier = Modifier,
    commands: List<CustomCommand> = emptyList(),
    onSendCommand: (CustomCommand) -> Unit = { /** NOOP**/ }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {
        commands.forEachIndexed { index, command ->
            Column(modifier = Modifier.clickable {
                onSendCommand(command)
            }) {
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyMedium,
                    text = command.name ?: ""
                )
                Text(
                    modifier = Modifier.fadedEdgeMarquee(),
                    color = Grey6,
                    style = MaterialTheme.typography.bodySmall,
                    text = command.description ?: ""
                )

                if (index != commands.lastIndex) {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardCustomCommandsCardPreview() {
    PreviewBlueMSTheme {
        BoardCustomCommandsCard(
            commands = listOf(
                CustomCommand(name = "Test1", type = "Integer", description = "desc1"),
                CustomCommand(name = "Test2", type = "Boolean", description = "desc1")
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardCustomCommandsContentCardPreview() {
    PreviewBlueMSTheme {
        BoardCustomCommandsContentCard(
            commands = listOf(
                CustomCommand(name = "Test1", type = "Integer", description = "desc1"),
                CustomCommand(name = "Test2", type = "Boolean", description = "desc1")
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCommandInputIntegerPreview() {
    PreviewBlueMSTheme {
        CustomCommandInput(
            CustomCommand(
                name = "Test1",
                type = "Integer",
                description = "desc1",
                max = 10,
                min = 1
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCommandInputBooleanPreview() {
    PreviewBlueMSTheme {
        CustomCommandInput(
            CustomCommand(name = "Test1", type = "Boolean", description = "desc1")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCommandInputStringPreview() {
    PreviewBlueMSTheme {
        CustomCommandInput(
            CustomCommand(name = "Test1", type = "String", description = "desc1", max = 10)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCommandInputEnumStringPreview() {
    PreviewBlueMSTheme {
        CustomCommandInput(
            CustomCommand(
                name = "Test1",
                type = "ENUMSTRING",
                description = "desc1",
                stringValues = listOf("a", "b")
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCommandInputEnumIntegerPreview() {
    PreviewBlueMSTheme {
        CustomCommandInput(
            CustomCommand(
                name = "Test1",
                type = "ENUMINTEGER",
                description = "desc1",
                integerValues = listOf(1, 2)
            )
        )
    }
}
