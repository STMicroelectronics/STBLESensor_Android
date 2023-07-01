/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.ext_config.R
import com.st.ui.composables.Header
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardReportCard(
    modifier: Modifier = Modifier,
    showGetUID: Boolean = true,
    onGetUID: () -> Unit = { /** NOOP **/ },
    showGetVersionFirmware: Boolean = true,
    onGetVersionFirmware: () -> Unit = { /** NOOP **/ },
    showGetInfo: Boolean = true,
    onGetInfo: () -> Unit = { /** NOOP **/ },
    showGetPowerStatus: Boolean = true,
    onGetPowerStatus: () -> Unit = { /** NOOP **/ },
    showGetHelp: Boolean = true,
    onGetHelp: () -> Unit = { /** NOOP **/ }
) {
    var isOpen by rememberSaveable { mutableStateOf(value = true) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = { isOpen = !isOpen }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
        ) {
            Header(
                isOpen = isOpen,
                icon = Icons.Default.Info,
                title = stringResource(id = R.string.st_extConfig_boardReport_cardTitle)
            )

            AnimatedVisibility(
                visible = isOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BoardReportContentCard(
                    showGetUID = showGetUID,
                    onGetUID = onGetUID,
                    showGetVersionFirmware = showGetVersionFirmware,
                    onGetVersionFirmware = onGetVersionFirmware,
                    showGetInfo = showGetInfo,
                    onGetInfo = onGetInfo,
                    showGetPowerStatus = showGetPowerStatus,
                    onGetPowerStatus = onGetPowerStatus,
                    showGetHelp = showGetHelp,
                    onGetHelp = onGetHelp
                )
            }
        }
    }
}

@Composable
fun BoardReportContentCard(
    modifier: Modifier = Modifier,
    showGetUID: Boolean = true,
    onGetUID: () -> Unit = { /** NOOP **/ },
    showGetVersionFirmware: Boolean = true,
    onGetVersionFirmware: () -> Unit = { /** NOOP **/ },
    showGetInfo: Boolean = true,
    onGetInfo: () -> Unit = { /** NOOP **/ },
    showGetPowerStatus: Boolean = true,
    onGetPowerStatus: () -> Unit = { /** NOOP **/ },
    showGetHelp: Boolean = true,
    onGetHelp: () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
    ) {
        val uidTextColor = if (showGetUID) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showGetUID) {
                    onGetUID()
                }
            },
            color = uidTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardReport_uid)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val versionFwTextColor =
            if (showGetVersionFirmware) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showGetVersionFirmware) {
                    onGetVersionFirmware()
                }
            },
            color = versionFwTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardReport_versionFirmware)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val infoTextColor = if (showGetInfo) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showGetInfo) {
                    onGetInfo()
                }
            },
            color = infoTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardReport_info)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val helpTextColor = if (showGetHelp) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showGetHelp) {
                    onGetHelp()
                }
            },
            color = helpTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardReport_help)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val powerTextColor = if (showGetPowerStatus) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showGetPowerStatus) {
                    onGetPowerStatus()
                }
            },
            color = powerTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardReport_powerStatus)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardReportCardPreview() {
    PreviewBlueMSTheme {
        BoardReportCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardReportContentCardPreview() {
    PreviewBlueMSTheme {
        BoardReportContentCard()
    }
}
