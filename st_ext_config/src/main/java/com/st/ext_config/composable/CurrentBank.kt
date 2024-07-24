/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.ext_config.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6

@Composable
fun CurrentBank(
    modifier: Modifier = Modifier,
    currentBank: Int?,
    currentFwDetail: String?
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(
                id = R.string.st_extConfig_fwDownload_currentBankLabel, currentBank ?: 0
            )
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = Grey6,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(id = R.string.st_extConfig_fwDownload_currentFwLabel)
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = SecondaryBlue,
            style = MaterialTheme.typography.bodyLarge,
            text = currentFwDetail ?: ""
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
        HorizontalDivider()
    }
}

@Composable
fun OtherBank(
    modifier: Modifier = Modifier,
    otherFwDetail: String?,
    onSwap: () -> Unit = { /**NOOP**/ }
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(id = R.string.st_extConfig_fwDownload_otherBankLabel)
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = Grey6,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(id = R.string.st_extConfig_fwDownload_otherFwLabel)
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        if (otherFwDetail == null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Grey6,
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(id = R.string.st_extConfig_fwDownload_noFwLabel)
            )
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodyLarge,
                text = otherFwDetail
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(weight = 1f))

                BlueMsButton(
                    onClick = onSwap,
                    text = stringResource(id = R.string.st_extConfig_fwDownload_swapBtn)
                )
            }
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
        HorizontalDivider()
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun CurrentBankPreview() {
    PreviewBlueMSTheme {
        CurrentBank(
            currentBank = 1,
            currentFwDetail = "test"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherBankPreview() {
    PreviewBlueMSTheme {
        OtherBank(
            otherFwDetail = "test"
        )
    }
}
