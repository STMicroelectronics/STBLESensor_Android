package com.st.high_speed_data_log.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.st.high_speed_data_log.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme

@Composable
fun ResetBoardDialog(
    modifier: Modifier = Modifier,
    onRestartRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ResetBoardDialogContent(
            modifier = modifier,
            onRestartRequest = onRestartRequest,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
fun ResetBoardDialogContent(
    modifier: Modifier = Modifier,
    onRestartRequest: () -> Unit = { /** NOOP **/ },
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
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = stringResource(id = R.string.st_hsdl_datalog_stopDialogTitle)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

            Text(
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
                color = Grey6,
                text = stringResource(id = R.string.st_hsdl_datalog_resetDialogMessage)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

            Row(modifier = Modifier.fillMaxWidth()) {
                BlueMsButtonOutlined(
                    modifier = Modifier.weight(weight = 0.5f),
                    text = stringResource(id = android.R.string.cancel),
                    onClick = onDismissRequest
                )

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                BlueMsButton(
                    modifier = Modifier.weight(weight = 0.5f),
                    text = stringResource(id = R.string.st_hsdl_datalog_resetBtn),
                    onClick = onRestartRequest
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun ResetBoardDialogContentPreview() {
    PreviewBlueMSTheme {
        ResetBoardDialogContent()
    }
}
