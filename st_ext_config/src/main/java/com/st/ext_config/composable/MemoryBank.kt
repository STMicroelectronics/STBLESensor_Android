/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.ext_config.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes


@Composable
fun MemoryBank(
    modifier: Modifier = Modifier,
    installedFwDetail: String?,
    bankNum: Int,
    isRunningBank: Boolean = false,
    nextRunningBank: Int = 0,
    onSwap: () -> Unit = { /**NOOP**/ },
    onDownload: () -> Unit = { /**NOOP**/ },
    onInstall: () -> Unit = { /**NOOP**/ },
    isCompatibleFwListNotEmpty: Boolean = false,
    selectedFwName: String? = null,
    selectedFwDescription: String? = null
) {

    var switchToThisBank by remember { mutableStateOf(false) }

    val mustBeBordered = if (nextRunningBank==bankNum) {
        true
    } else {
        isRunningBank && (nextRunningBank == 0)
    }


    Surface(
        modifier = if (mustBeBordered) modifier
            .fillMaxWidth()
            .border(
                BorderStroke(2.dp, PrimaryBlue),
                Shapes.small
            ) else
            modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = if (mustBeBordered) LocalDimensions.current.elevationMedium else LocalDimensions.current.elevationSmall
    ) {

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (mustBeBordered) {
                    Checkbox(
                        checked = true,
                        enabled = false,
                        onCheckedChange = {})
                } else {
                    Checkbox(
                        checked = switchToThisBank,
                        enabled = (installedFwDetail != null) && (switchToThisBank != true),
                        onCheckedChange = {
                            switchToThisBank = it
                            onSwap()
                        })
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    text = "Memory Bank $bankNum"
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
//                color = if (isRunningBank) PrimaryBlue else Grey6,
//                textDecoration = if (isRunningBank) TextDecoration.Underline else null,
                style = MaterialTheme.typography.bodyLarge,
                text = if (isRunningBank) "Running Firmware:" else {
                    if(nextRunningBank==bankNum) {
                        "Next Running Firmware:"
                    } else {
                        "Firmware Present:"
                    }
                }
            )
            if (installedFwDetail == null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_extConfig_fwDownload_noFwLabel)
                )

                if (isCompatibleFwListNotEmpty && (isRunningBank == false) && (switchToThisBank==false)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        BlueMsButton(
                            onClick = onDownload,
                            enabled = switchToThisBank.not(),
                            text = "Download one Firmware",
                            imageVector = Icons.Default.CloudDownload
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    textAlign = TextAlign.Center,
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.bodyLarge,
                    text = installedFwDetail
                )

                if (isCompatibleFwListNotEmpty && (isRunningBank == false)&& (switchToThisBank==false)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        BlueMsButton(
                            onClick = onDownload,
                            enabled = switchToThisBank.not(),
                            text = "Try a new Firmware",
                            imageVector = Icons.Default.CloudDownload
                        )
                    }
                }
            }

            if ((selectedFwName != null) && (isRunningBank == false) && (switchToThisBank==false)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_extConfig_fwDownload_selectedFwLabel)
                )

                Text(
                    modifier = Modifier
                        .padding(top = LocalDimensions.current.paddingNormal)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.bodyLarge,
                    text = selectedFwName
                )

                if (selectedFwDescription != null) {

                    Text(
                        modifier = Modifier.padding(top = LocalDimensions.current.paddingNormal),
                        color = Grey6,
                        style = MaterialTheme.typography.bodyLarge,
                        text = stringResource(id = R.string.st_extConfig_fwDownload_descriptionLabel)
                    )
                    Text(
                        color = Grey6,
                        style = MaterialTheme.typography.bodyLarge,
                        text = selectedFwDescription
                    )
                }

                BlueMsButton(
                    onClick = onInstall,
                    text = "Install"
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun MemoryBankPreview() {
    PreviewBlueMSTheme {
        MemoryBank(
            installedFwDetail = "test",
            isRunningBank = false,
            bankNum = 1,
            isCompatibleFwListNotEmpty = true,
            selectedFwName = "Selected fw",
            selectedFwDescription = "Selected fw description"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoryBank2() {
    PreviewBlueMSTheme {
        MemoryBank(
            installedFwDetail = "test",
            isRunningBank = true,
            bankNum = 2,
            isCompatibleFwListNotEmpty = true,
            selectedFwName = "Selected fw",
            selectedFwDescription = "Selected fw description"
        )
    }
}
