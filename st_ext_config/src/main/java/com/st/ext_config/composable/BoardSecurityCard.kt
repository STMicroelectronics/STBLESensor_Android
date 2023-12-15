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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.ext_config.R
import com.st.ext_config.ui.ext_config.ExtConfigViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.Header
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSecurityCard(
    modifier: Modifier = Modifier,
    showChangePin: Boolean = true,
    onChangePin: (Int) -> Unit = { /** NOOP **/ },
    showClearDB: Boolean = true,
    onClearDB: () -> Unit = { /** NOOP **/ },
    showCertRegistration: Boolean = true,
    onCertRegistration: () -> Unit = { /** NOOP **/ },
    showCertRequest: Boolean = true,
    onCertRequest: () -> Unit = { /** NOOP **/ }
) {
    var isOpen by rememberSaveable { mutableStateOf(value = true) }
    var pin by rememberSaveable { mutableStateOf(value = "123456") }
    var openChangePINDialog by rememberSaveable { mutableStateOf(value = false) }

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
                icon = Icons.Default.Security,
                title = stringResource(id = R.string.st_extConfig_boardSecurity_cardTitle)
            )

            AnimatedVisibility(
                visible = isOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BoardSecurityContentCard(
                    showChangePin = showChangePin,
                    onChangePin = { openChangePINDialog = true },
                    showClearDB = showClearDB,
                    onClearDB = onClearDB,
                    showCertRegistration = showCertRegistration,
                    onCertRegistration = onCertRegistration,
                    showCertRequest = showCertRequest,
                    onCertRequest = onCertRequest
                )
            }
        }
    }


    if (openChangePINDialog) {
        AlertDialog(
            onDismissRequest = { openChangePINDialog = false },
            title = {
                Text(text = stringResource(id = R.string.st_extConfig_boardSettings_setWiFiCredentials))
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = pin,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    supportingText = {
                        Text(
                            text = "${pin.length} / ${ExtConfigViewModel.MAX_PIN_LEN}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    onValueChange = {
                        if (it.length <= ExtConfigViewModel.MAX_PIN_LEN) pin = it
                    }
                )
            },
            dismissButton = {
                BlueMsButtonOutlined(
                    onClick = { openChangePINDialog = false },
                    text = stringResource(id = android.R.string.cancel)
                )
            },
            confirmButton = {
                BlueMsButton(
                    enabled = pin.length in 1..ExtConfigViewModel.MAX_PIN_LEN,
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        onChangePin(pin.toInt())
                        openChangePINDialog = false
                    }
                )
            }
        )
    }
}

@Composable
fun BoardSecurityContentCard(
    modifier: Modifier = Modifier,
    showChangePin: Boolean = true,
    onChangePin: () -> Unit = { /** NOOP **/ },
    showClearDB: Boolean = true,
    onClearDB: () -> Unit = { /** NOOP **/ },
    showCertRegistration: Boolean = true,
    onCertRegistration: () -> Unit = { /** NOOP **/ },
    showCertRequest: Boolean = true,
    onCertRequest: () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
    ) {
        val pinTextColor = if (showChangePin) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showChangePin) {
                    onChangePin()
                }
            },
            color = pinTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSecurity_changePin)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val clearTextColor = if (showClearDB) Grey6 else Grey6.copy(alpha = 0.3f)
        val context = LocalContext.current
        Text(
            modifier = Modifier.clickable {
                if (showClearDB) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.st_extConfig_boardSecurity_clearDbToast),
                        Toast.LENGTH_SHORT
                    ).show()
                    onClearDB()
                }
            },
            color = clearTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSecurity_clearDb)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val certRegTextColor = if (showCertRegistration) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showCertRegistration) {
                    onCertRegistration()
                }
            },
            color = certRegTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSecurity_certRegistration)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        val certReqTextColor = if (showCertRequest) Grey6 else Grey6.copy(alpha = 0.3f)
        Text(
            modifier = Modifier.clickable {
                if (showCertRequest) {
                    onCertRequest()
                }
            },
            color = certReqTextColor,
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(id = R.string.st_extConfig_boardSecurity_certRequest)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardSecurityCardPreview() {
    PreviewBlueMSTheme {
        BoardSecurityCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardSecurityContentCardPreview() {
    PreviewBlueMSTheme {
        BoardSecurityContentCard()
    }
}
