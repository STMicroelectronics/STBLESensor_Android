package com.st.smart_motor_control.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.st.smart_motor_control.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
@Composable
fun StopLoggingDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        StopLoggingDialogContent(
            modifier = modifier,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
fun StopLoggingDialogContent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = { /** NOOP **/ }
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal)
    ) {
        Column(
            modifier = Modifier
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = stringResource(id = R.string.st_motor_control_datalog_stopDialogTitle)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Grey6,
                text = stringResource(id = R.string.st_motor_control_datalog_stopDialogMessage)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

            BlueMsButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.st_motor_control_datalog_stopDialogClose),
                onClick = onDismissRequest
            )
        }
    }
}