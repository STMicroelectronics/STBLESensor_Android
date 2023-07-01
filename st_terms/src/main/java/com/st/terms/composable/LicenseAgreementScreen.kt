/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.terms.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.terms.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@Composable
fun LicenseAgreementScreen(
    modifier: Modifier = Modifier,
    onLicenseAgree: () -> Unit = { /** NOOP **/ }
) {
    val openDialog = remember { mutableStateOf(value = false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal)
            .verticalScroll(state = rememberScrollState())
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.st_terms_title),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.st_terms_description),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.st_terms_licenseAgreement),
            color = Grey6,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

        Row(modifier = Modifier.fillMaxWidth()) {
            BlueMsButtonOutlined(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(id = R.string.st_terms_doNotAgree),
                onClick = {
                    openDialog.value = true
                }
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

            BlueMsButton(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(id = R.string.st_terms_agree),
                onClick = onLicenseAgree
            )
        }

        if (openDialog.value) {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.st_terms_title))
                },
                text = {
                    Text(text = stringResource(id = R.string.st_terms_missingAgreement))
                },
                onDismissRequest = { /** NOOP **/ },
                confirmButton = { /** NOOP **/ },
                dismissButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            )
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun LicenseAgreementScreenPreview() {
    PreviewBlueMSTheme {
        LicenseAgreementScreen()
    }
}
