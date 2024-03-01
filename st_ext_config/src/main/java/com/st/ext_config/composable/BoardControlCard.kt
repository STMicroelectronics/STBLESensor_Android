/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.ext_config.R
import com.st.ui.composables.Header
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.Shapes

@Composable
fun BoardControlCard(
    modifier: Modifier = Modifier,
    showDFU: Boolean = true,
    onDFU: () -> Unit = { /** NOOP **/ },
    showOff: Boolean = true,
    onOff: () -> Unit = { /** NOOP **/ },
    showFwDownload: Boolean = true,
    onFwDownload: () -> Unit = { /** NOOP **/ },
    showSwap: Boolean = true,
    onSwap: () -> Unit = { /** NOOP **/ },
) {
    var isOpen by rememberSaveable(showDFU, showFwDownload, showSwap, showOff) {
        mutableStateOf(
            value = showDFU ||
                    showFwDownload || showSwap || showOff
        )
    }

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
                .padding(all = LocalDimensions.current.paddingNormal),
        ) {
            Header(
                isOpen = isOpen,
                icon = Icons.Default.PowerSettingsNew,
                title = stringResource(id = R.string.st_extConfig_boardControl_cardTitle)
            )

            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BoardControlContentCard(
                    showDFU = showDFU,
                    onDFU = onDFU,
                    showOff = showOff,
                    onOff = onOff,
                    showFwDownload = showFwDownload,
                    onFwDownload = onFwDownload,
                    showSwap = showSwap,
                    onSwap = onSwap
                )
            }
        }
    }
}

@Composable
fun BoardControlContentCard(
    modifier: Modifier = Modifier,
    showDFU: Boolean = true,
    onDFU: () -> Unit = { /** NOOP **/ },
    showOff: Boolean = true,
    onOff: () -> Unit = { /** NOOP **/ },
    showFwDownload: Boolean = true,
    onFwDownload: () -> Unit = { /** NOOP **/ },
    showSwap: Boolean = true,
    onSwap: () -> Unit = { /** NOOP **/ },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
    ) {
        val dfuTextColor = if (showDFU) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showDFU) {
                    onDFU()
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = dfuTextColor,
            text = stringResource(id = R.string.st_extConfig_boardControl_dfu)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val offTextColor = if (showOff) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showOff) {
                    onOff()
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = offTextColor,
            text = stringResource(id = R.string.st_extConfig_boardControl_off)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val fwDownloadTextColor = if (showFwDownload) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showFwDownload) {
                    onFwDownload()
                }
            },
            color = fwDownloadTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardControl_download)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val swapTextColor = if (showSwap) Grey6 else Grey6.copy(alpha = 0.3f)
        val context = LocalContext.current
        Text(
            modifier = Modifier.clickable {
                if (showSwap) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.st_extConfig_boardControl_swapToast),
                        Toast.LENGTH_SHORT
                    ).show()
                    onSwap()
                }
            },
            color = swapTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardControl_swap)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardControlCardPreview() {
    PreviewBlueMSTheme {
        BoardControlCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardControlContentCardPreview() {
    PreviewBlueMSTheme {
        BoardControlContentCard()
    }
}
