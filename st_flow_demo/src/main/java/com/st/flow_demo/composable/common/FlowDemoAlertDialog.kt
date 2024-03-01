package com.st.flow_demo.composable.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.st.ui.composables.BlueMsButton

@Composable
fun FlowDemoAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit
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
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onConfirmation
            )
        },
        dismissButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}
