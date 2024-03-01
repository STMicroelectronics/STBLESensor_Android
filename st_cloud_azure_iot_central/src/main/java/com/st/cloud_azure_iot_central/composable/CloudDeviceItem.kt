package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.st.cloud_azure_iot_central.model.AzureCloudDevice
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey5
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText

@Composable
fun CloudDeviceItem(
    boardUid: String,
    isSelected: Boolean,
    hasCredentials: Boolean,
    cloudDevice: AzureCloudDevice,
    onCloudDeviceSelection: () -> Unit = { /** NOOP**/ },
    onCloudDeviceDeleting: () -> Unit = { /** NOOP**/ }
) {
    val fontColorTitle =
        if (cloudDevice.id == boardUid) MaterialTheme.colorScheme.primary else Grey6

    val fontColorBody = if (cloudDevice.id == boardUid) Color.Unspecified else Grey5

    Surface(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(4.dp, SuccessText) else null,
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        enabled = cloudDevice.id == boardUid,
        onClick = { onCloudDeviceSelection() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = LocalDimensions.current.paddingMedium)
                    .fillMaxHeight()
                    .weight(0.8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = fontColorTitle,
                    text = "Name: "
                )

                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingLarge),
                    text = cloudDevice.displayName,
                    color = fontColorBody
                )

                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = fontColorTitle,
                    text = "Id: "
                )

                Text(
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingLarge),
                    text = cloudDevice.id,
                    color = fontColorBody
                )

            }

            if (cloudDevice.id == boardUid) {
                Column(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingMedium)
                        .fillMaxHeight()
                        .weight(0.2f)
                        .clickable { onCloudDeviceDeleting() },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        tint = if(hasCredentials) Grey6 else MaterialTheme.colorScheme.surface,
                        imageVector = Icons.Default.Token,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        modifier = Modifier.clickable { onCloudDeviceDeleting() },
                        tint = ErrorText,
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }
        }
    }
}