/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.st.demo_showcase.R
import com.st.demo_showcase.ui.log_settings.LogType
import com.st.ui.composables.ActionItem
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@Composable
fun LogSettingsScreen(
    isLogging: Boolean,
    logType: LogType,
    onStartLog: () -> Unit = { /** NOOP **/ },
    onStopLog: () -> Unit = { /** NOOP **/ },
    onClearLog: () -> Unit = { /** NOOP **/ },
    onShareLog: () -> Unit = { /** NOOP **/ },
    onLogTypeChanged: (LogType) -> Unit = { /** NOOP **/ },
    numberLogs: Int
) {
    val context = LocalContext.current
    var openChangeTypeDialog by rememberSaveable { mutableStateOf(value = false) }
    var internalLogType by remember(key1 = logType) { mutableStateOf(value = logType) }
    val internalNumberLogs by remember(key1 = numberLogs) { mutableStateOf(value = numberLogs) }

    val actions by remember(key1 = isLogging, key2= internalLogType, key3 = internalNumberLogs) {
        mutableStateOf(value =
        if (isLogging) {
            listOf(
                ActionItem(
                    label = context.getString(R.string.st_demoShowcase_logSettings_stopTitle),
                    description = context.getString(R.string.st_demoShowcase_logSettings_stopDesc,internalLogType.name),
                    action = onStopLog
                )
            )
        } else {
            listOf(
                ActionItem(
                    label = context.getString(R.string.st_demoShowcase_logSettings_startTitle),
                    description = context.getString(R.string.st_demoShowcase_logSettings_startDesc),
                    action = onStartLog
                ),
                ActionItem(
                    label = context.getString(R.string.st_demoShowcase_logSettings_logType),
                    description = context.getString(R.string.st_demoShowcase_logSettings_logTypeDesc,internalLogType.name),
                    action = { openChangeTypeDialog = true }
                ),
                if(internalNumberLogs!=0) {
                    ActionItem(
                        label = context.getString(R.string.st_demoShowcase_logSettings_clearTitle),
                        description = context.getString(R.string.st_demoShowcase_logSettings_clearDesc,internalNumberLogs),
                        action = onClearLog
                    )
                } else {
                    ActionItem(
                        label = context.getString(R.string.st_demoShowcase_logSettings_clearTitle),
                        description = context.getString(R.string.st_demoShowcase_logSettings_clearEmptyDesc),
                        action = onClearLog
                    )
                }
                ,ActionItem(
                    label = context.getString(R.string.st_demoShowcase_logSettings_exportTitle),
                    description = context.getString(R.string.st_demoShowcase_logSettings_exportDesc),
                    action = onShareLog
                )
            )
        }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        items(actions) { action ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { action.action() }
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = action.label
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey6,
                    text = action.description
                )

                Divider()
            }
        }
    }

    if (openChangeTypeDialog) {
        Dialog(onDismissRequest = { openChangeTypeDialog = false }) {
            LogStorageDialog(
                logType = logType,
            ) {
                onLogTypeChanged(it)
                internalLogType = it
                openChangeTypeDialog = false
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun LogSettingsScreenPreview() {
    PreviewBlueMSTheme {
        LogSettingsScreen(isLogging = false, logType = LogType.CSV, numberLogs = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun LogSettingsScreenNoFilePreview() {
    PreviewBlueMSTheme {
        LogSettingsScreen(isLogging = false, logType = LogType.DB, numberLogs = 0)
    }
}

@Preview(showBackground = true)
@Composable
private fun LogSettingsScreenLoggingPreview() {
    PreviewBlueMSTheme {
        LogSettingsScreen(isLogging = true, logType = LogType.LOG_CAT, numberLogs = 1)
    }
}