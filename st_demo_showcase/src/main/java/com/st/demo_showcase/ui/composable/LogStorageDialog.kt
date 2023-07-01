/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.demo_showcase.R
import com.st.demo_showcase.ui.log_settings.LogType
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@Composable
fun LogStorageDialog(
    modifier: Modifier = Modifier,
    logType: LogType,
    onLogTypeChanged: (LogType) -> Unit = { /** NOOP **/ }
) {
    var internalState by remember(key1 = logType) { mutableStateOf(value = logType) }
    Surface(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_demoShowcase_logSettings_logType)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Divider()

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (internalState == LogType.LOG_CAT),
                        onClick = { internalState = LogType.LOG_CAT }
                    )
                    .padding(horizontal = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (internalState == LogType.LOG_CAT),
                    onClick = { internalState = LogType.LOG_CAT }
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_demoShowcase_logSettings_logcat),
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (internalState == LogType.CSV),
                        onClick = { internalState = LogType.CSV }
                    )
                    .padding(horizontal = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (internalState == LogType.CSV),
                    onClick = { internalState = LogType.CSV }
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_demoShowcase_logSettings_csv),
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (internalState == LogType.DB),
                        onClick = { internalState = LogType.DB }
                    )
                    .padding(horizontal = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (internalState == LogType.DB),
                    onClick = { internalState = LogType.DB }
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_demoShowcase_logSettings_db),
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                BlueMsButton(text = stringResource(id = android.R.string.ok),
                    onClick = { onLogTypeChanged(internalState) })
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun LogStorageDialogPreview() {
    PreviewBlueMSTheme {
        LogStorageDialog(
            logType = LogType.LOG_CAT
        )
    }
}