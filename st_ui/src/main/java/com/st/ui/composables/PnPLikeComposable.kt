/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.ui.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.st.ui.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.toLocalDateTime
import com.st.ui.theme.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val LocalLastStatusUpdatedAt = compositionLocalOf { 0L }

@Composable
fun BooleanProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    description: String = "",
    color: String = "",
    comment: String = "",
    trueLabel: String = "",
    falseLabel: String = "",
    unit: String = "",
    value: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (Boolean) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(
        value,
        lastStatusUpdatedAt
    ) { mutableStateOf(value = value) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(text = text, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            enabled = enabled,
            checked = internalState,
            colors = SwitchDefaults.colors(uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary),
            onCheckedChange = {
                internalState = it
                onValueChange(internalState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringProperty(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    unit: String = "",
    description: String = "",
    color: String = "",
    comment: String = "",
    minLength: Int? = null,
    maxLength: Int? = null,
    trimWhitespace: Boolean = false,
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Any, Boolean) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(
        value,
        lastStatusUpdatedAt
    ) { mutableStateOf(value = value) }

    val isValid by rememberIsValid(
        value = internalState.length,
        maxValue = minLength,
        minValue = maxLength
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(text = text, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            value = internalState,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send
            ),
            supportingText = {
                maxLength?.let { max ->
                    Text(
                        text = "${value.length} / $max",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (isValid.not()) ErrorText else Color.Unspecified
                    )
                }
            },
            trailingIcon = {
                if (isValid.not()) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorText
                    )
                }
            },
            onValueChange = {
                internalState = it

                if (commandBehavior) {
                    onValueChange(internalState, isValid)
                }
            },
            keyboardActions = KeyboardActions(onSend = {
                onValueChange(internalState, isValid)
            })
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeProperty(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    unit: String = "",
    description: String = "",
    color: String = "",
    comment: String = "",
    hideTime: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (Any) -> Unit
) {
    // 2023-02-08T03:00:00.000Z
    val formatter by remember { mutableStateOf(value = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")) }
    val printDateFormatter by remember { mutableStateOf(value = DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
    val printTimeFormatter by remember { mutableStateOf(value = DateTimeFormatter.ofPattern("HH:mm:ss")) }
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalStateDateTime by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(value = value.toLocalDateTime(formatter = formatter))
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }
    var showTimePicker by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(text = text, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        Column {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.date))
                },
                enabled = false,
                value = internalStateDateTime?.format(printDateFormatter) ?: "",
                trailingIcon = {
                    IconButton(enabled = enabled, onClick = {
                        showDatePicker = true
                    }) {
                        Icon(
                            tint = MaterialTheme.colorScheme.primary,
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                    }
                },
                onValueChange = { /** NOOP **/ }
            )

            if (hideTime.not()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = stringResource(id = R.string.time))
                    },
                    enabled = false,
                    value = internalStateDateTime?.format(printTimeFormatter) ?: "",
                    trailingIcon = {
                        IconButton(
                            enabled = enabled,
                            onClick = {
                                showTimePicker = true
                            }
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                imageVector = Icons.Default.Watch,
                                contentDescription = null
                            )
                        }
                    },
                    onValueChange = { /** NOOP **/ }
                )
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = internalStateDateTime?.toInstant(ZoneOffset.UTC)
                    ?.toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                dismissButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = { showDatePicker = false },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { ts ->

                                val localDate = Instant.ofEpochMilli(ts)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()

                                val time = internalStateDateTime?.toLocalTime() ?: LocalTime.now()

                                internalStateDateTime = LocalDateTime.of(localDate, time)

                                onValueChange(internalStateDateTime?.format(formatter) ?: "")
                            }
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                )
            }
        }

        if (showTimePicker) {
            val timePicker = rememberTimePickerState(
                initialHour = internalStateDateTime?.toLocalTime()?.hour ?: 12,
                initialMinute = internalStateDateTime?.toLocalTime()?.minute ?: 0,
                is24Hour = true
            )

            DatePickerDialog(
                onDismissRequest = { showTimePicker = false },
                dismissButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = { showTimePicker = false },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            showTimePicker = false

                            val time = LocalTime.of(timePicker.hour, timePicker.minute, 0)

                            val date = internalStateDateTime?.toLocalDate() ?: LocalDate.now()
                            internalStateDateTime = LocalDateTime.of(date, time)

                            onValueChange(internalStateDateTime?.format(formatter) ?: "")
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            ) {
                TimePicker(
                    state = timePicker,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeProperty(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    unit: String = "",
    description: String = "",
    color: String = "",
    comment: String = "",
    enabled: Boolean = true,
    onValueChange: (Any) -> Unit
) {
    // 03:00:00
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    val formatter by remember {
        mutableStateOf(value = DateTimeFormatter.ofPattern("HH:mm:ss"))
    }
    var showTimePicker by remember {
        mutableStateOf(false)
    }
    var internalStateDateTime by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(
            value =
            value.toLocalTime(formatter = formatter)
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(text = text, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(id = R.string.time))
            },
            enabled = false,
            value = internalStateDateTime?.format(formatter) ?: "",
            trailingIcon = {
                IconButton(
                    enabled = enabled,
                    onClick = {
                        showTimePicker = true
                    }
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = Icons.Default.Watch,
                        contentDescription = null
                    )
                }
            },
            onValueChange = { /** NOOP **/ }
        )


        if (showTimePicker) {
            val timePicker = rememberTimePickerState(
                initialHour = internalStateDateTime?.hour ?: LocalTime.now().hour,
                initialMinute = internalStateDateTime?.minute ?: LocalTime.now().minute,
                is24Hour = true
            )

            DatePickerDialog(
                onDismissRequest = { showTimePicker = false },
                dismissButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = { showTimePicker = false },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            showTimePicker = false

                            internalStateDateTime =
                                LocalTime.of(timePicker.hour, timePicker.minute, 0)
                            onValueChange(internalStateDateTime?.format(formatter) ?: "")
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            ) {
                TimePicker(
                    state = timePicker,
                )
            }
        }
    }
}

@Composable
fun <T : Any> EnumProperty(
    modifier: Modifier = Modifier,
    data: T?,
    label: String = "",
    unit: String = "",
    enabled: Boolean = true,
    color: String = "",
    description: String = "",
    comment: String = "",
    initialValue: T,
    values: List<Pair<String, T>> = emptyList(),
    colors: List<Pair<T, String>> = emptyList(),
    onValueChange: (T) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(initialValue, lastStatusUpdatedAt) {
        mutableStateOf(value = data ?: initialValue)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val propName = if (unit.isEmpty()) label else "$label [$unit]"
        Text(text = propName, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        var expanded by remember { mutableStateOf(value = false) }
        var fieldSize by remember { mutableStateOf(Size.Zero) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Grey3))
                .padding(LocalDimensions.current.paddingNormal)
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                    }
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        fieldSize = coordinates.size.toSize()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = values.first { it.second == internalState }.first
                Text(text = text)

                Spacer(modifier = Modifier.weight(1f))

                Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = null)

                DropdownMenu(
                    modifier = Modifier.width(with(LocalDensity.current) { fieldSize.width.toDp() }),
                    expanded = expanded, onDismissRequest = {
                        expanded = false
                    }) {
                    values.forEach {
                        DropdownMenuItem(
                            onClick = {
                                internalState = it.second
                                if (enabled) {
                                    onValueChange(internalState)
                                }
                                expanded = false
                            },
                            text = {
                                Text(it.first)
                            }
                        )
                    }
                }
            }
        }
    }
}

const val ENABLE_PROPERTY_NAME = "enable"

@Composable
fun HeaderEnabledProperty(
    modifier: Modifier = Modifier,
    name: String,
    value: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (Pair<String, Boolean>) -> Unit
) {
    BooleanProperty(
        modifier = modifier,
        value = value,
        enabled = enabled,
        onValueChange = { onValueChange(name to it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFileResultProperty(
    modifier: Modifier = Modifier,
    result: Boolean
) {
    Box(modifier = modifier.fillMaxWidth()) {
        AssistChip(
            modifier = Modifier.align(alignment = Alignment.CenterEnd),
            enabled = false,
            onClick = { /** NOOP **/ },
            label = {
                if (result) {
                    Text(text = stringResource(id = R.string.file_loaded))
                } else {
                    Text(text = stringResource(id = R.string.file_notLoaded))
                }
            }
        )
    }
}

private fun isCommentLine(line: String): Boolean {
    return !line.startsWith("--")
}

private fun compressUCFString(ucfContent: String): String {
    val isSpace = "\\s+".toRegex()
    return ucfContent.lineSequence().filter { isCommentLine(it) }
        .map { it.replace(isSpace, "").drop(2) }.joinToString("")
}

const val LOAD_FILE_COMMAND_NAME = "load_file"
const val LOAD_FILE_RESPONSE_PROPERTY_NAME = "ucf_status"
const val BIN_FILE_TYPE = "application/octet-stream"
const val JSON_FILE_TYPE = "application/json"
const val FILE_UCF = "ucf"
const val FILE_DATA = "data"
const val FILE_SIZE = "size"
const val FILE_REQUEST = "ucf_data"

data class CommandRequest(
    val commandType: String,
    val commandName: String,
    val request: Map<String, Any> = emptyMap()
)

@Composable
fun UCF(
    modifier: Modifier = Modifier,
    label: String,
    commandType: String,
    commandName: String,
    onSendCommand: (CommandRequest?) -> Unit
) {
    val context = LocalContext.current
    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { fileUri ->
            if (fileUri != null) {
                context.contentResolver.openInputStream(fileUri)?.let { stream ->
                    val fileContent = stream.readBytes().toString(Charsets.UTF_8)
                    stream.close()

//                    val fileName = context.contentResolver.query(fileUri, null, null, null, null)
//                        ?.use { cursor ->
//                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                            cursor.moveToFirst()
//
//                            cursor.getString(nameIndex)
//                        } ?: ""
//                    val fileExt = fileName.split('.').last()
                    val fileExt = fileContent.split('.').last()

                    var postProcFile = fileContent
                    if (fileExt == FILE_UCF) {
                        postProcFile = compressUCFString(fileContent)
                    }
                    onSendCommand(
                        CommandRequest(
                            commandType = commandType,
                            commandName = commandName,
                            request = mapOf(
                                FILE_REQUEST to mapOf(
                                    FILE_SIZE to postProcFile.length,
                                    FILE_DATA to postProcFile
                                )
                            )
                        )
                    )
                }
            }
        }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        BlueMsButton(
            modifier = Modifier.align(alignment = Alignment.End),
            onClick = {
                pickFileLauncher.launch(arrayOf(BIN_FILE_TYPE))
            },
            text = stringResource(id = R.string.command_uploadBtn)
        )
    }
}
