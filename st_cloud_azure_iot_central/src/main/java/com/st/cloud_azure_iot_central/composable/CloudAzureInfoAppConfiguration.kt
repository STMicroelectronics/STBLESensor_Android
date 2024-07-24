package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun CloudAzureInfoAppConfiguration(onDismissRequest: () -> Unit = { /** NOOP **/ }) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        shape = Shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {

            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = "Info"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Text(
                style = MaterialTheme.typography.bodySmall,
                text = "Follow the instructions below to configure Azure Iot Central application:"
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = "1. Send Sharable Link to your mail"
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = "2. Open the Link in a web browser on your PC"
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = "3. Sign in with your Microsoft account"
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = "4. Name the application you\'re replicating in your account and choose your plan"
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = buildAnnotatedString {
                    append("5. Create a new token:\n")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Administration -> Token API -> New")
                    }
                }
            )

            Text(
                modifier = Modifier.padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodySmall,
                text = buildAnnotatedString {
                    append("6. Scan the QR code through the ST BLE Sensor app and enter in the field ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("APP NAME")
                    }
                    append(" the name you chose before")
                }
            )


            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

            BlueMsButton(
                modifier = Modifier.align(Alignment.Start),
                text = stringResource(id = android.R.string.ok),
                onClick = onDismissRequest
            )
        }
    }
}