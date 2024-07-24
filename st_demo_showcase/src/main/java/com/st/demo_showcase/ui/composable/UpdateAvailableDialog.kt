/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.demo_showcase.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6

@Composable
fun UpdateAvailableDialog(
    mandatory: Boolean = false,
    show: Boolean,
    currentFw: String,
    updateFw: String,
    changeLog: String,
    onInstall: () -> Unit = { /** NOOP **/ },
    dismissUpdateDialog: (Boolean) -> Unit = { /** NOOP **/ }
) {
    var showDialog by remember { mutableStateOf(value = show) }
    var checked by remember { mutableStateOf(value = false) }
    if (showDialog) {
        val onDismissRequest = {
            showDialog = false
            dismissUpdateDialog(checked)
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        showDialog = false
                        onInstall()
                    },
                    text = stringResource(id = R.string.st_demoShowcase_fwUpdate_installBtn)
                )
            },
            dismissButton = {
                BlueMsButtonOutlined(
                    onClick = onDismissRequest,
                    text = stringResource(id = R.string.st_demoShowcase_fwUpdate_cancelBtn)
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_demoShowcase_fwUpdate_newFwTitle)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                    Text(
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey6,
                        text = stringResource(
                            id = R.string.st_demoShowcase_fwUpdate_currentFwLabel,
                            currentFw
                        )
                    )

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    Text(
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey6,
                        text = stringResource(
                            id = R.string.st_demoShowcase_fwUpdate_availableFwLabel,
                            updateFw
                        )
                    )
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                    Text(
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryBlue,
                        text = stringResource(
                            id = R.string.st_demoShowcase_fwUpdate_changelogLabel,
                            changeLog
                        )
                    )

                    if (!mandatory) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Grey6,
                                text = stringResource(id = R.string.st_demoShowcase_fwUpdate_doNotAskAgain)
                            )
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun AvailableDialogUpdatePreview() {
    PreviewBlueMSTheme {
        UpdateAvailableDialog(
            show = true,
            currentFw = "current",
            updateFw = "update",
            changeLog = "changelog"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableDialogUpdateMandatoryPreview() {
    PreviewBlueMSTheme {
        UpdateAvailableDialog(
            mandatory = true,
            show = true,
            currentFw = "current",
            updateFw = "update",
            changeLog = "changelog"
        )
    }
}