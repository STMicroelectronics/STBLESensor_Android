package com.st.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.st.ui.composables.BlueMsButton


@Composable
fun BlueMSAlertDialog(
    title: String,
    message: String,
    onShowCancelButton: Boolean = true,
    onShowOkButton: Boolean = true,
    onDismiss: () -> Unit = { /** NOOP**/ },
    onConfirmation: () -> Unit = { /** NOOP**/ }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            if (onShowOkButton) {
                BlueMsButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = onConfirmation
                )
            } else null
        },
        dismissButton = {
            if (onShowCancelButton) {
                BlueMsButton(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = onDismiss
                )
            }else null
        }
    )
}