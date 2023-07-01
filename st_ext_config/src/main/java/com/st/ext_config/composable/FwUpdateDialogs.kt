/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.services.ota.FwUploadError
import com.st.ext_config.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.PreviewBlueMSTheme
import kotlin.math.roundToInt

private const val FULL_PERCENT = 100

@Composable
fun FwUpdateProgressDialog(
    modifier: Modifier = Modifier,
    progress: Float? = 0f
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { /** NOOP **/ },
        confirmButton = { /** NOOP **/ },
        title = {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_extConfig_fwUpgrade_title)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = (progress ?: 0f) / FULL_PERCENT
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = stringResource(
                        id = R.string.st_extConfig_fwUpgrade_progressFormatter,
                        progress?.roundToInt() ?: 0
                    )
                )
            }
        }
    )
}


@Composable
fun FwUpgradeErrorDialog(
    modifier: Modifier = Modifier,
    fwUploadError: FwUploadError,
    onPositiveButtonPressed: () -> Unit = { /** NOOP **/ }
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { /** NOOP **/ },
        text = {
            Text(
                text = stringResource(
                    id = R.string.st_extConfig_fwUpgrade_errorFormatter,
                    fwUploadError.name
                )
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onPositiveButtonPressed
            )
        }
    )
}

@Composable
fun FwUpgradeSuccessDialog(
    modifier: Modifier = Modifier,
    seconds: Float = 0f,
    onPositiveButtonPressed: () -> Unit = { /** NOOP **/ }
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { /** NOOP **/ },
        title = {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_extConfig_fwUpgrade_successTitle)
            )
        },
        text = {
            Text(text = stringResource(id = R.string.st_extConfig_fwUpgrade_successText, seconds))
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onPositiveButtonPressed
            )
        }
    )
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun FwUpgradeErrorDialogPreview() {
    PreviewBlueMSTheme {
        FwUpgradeErrorDialog(fwUploadError = FwUploadError.ERROR_UNKNOWN)
    }
}

@Preview(showBackground = true)
@Composable
private fun FwUpdateProgressDialogPreview() {
    PreviewBlueMSTheme {
        FwUpdateProgressDialog()
    }
}

@Preview(showBackground = true)
@Composable
private fun FwUpgradeSuccessDialogPreview() {
    PreviewBlueMSTheme {
        FwUpgradeSuccessDialog()
    }
}
