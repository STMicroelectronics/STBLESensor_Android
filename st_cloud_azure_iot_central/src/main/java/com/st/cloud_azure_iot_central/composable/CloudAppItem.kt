package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.st.cloud_azure_iot_central.model.CloudAPIToken
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText

@Composable
fun CloudAppItem(
    apiToken: CloudAPIToken?,
    authorizationKey: String?,
    apiTokenExpired: Boolean,
    cloudAppName: String?,
    cloudAppUrl: String?,
    isSelected: Boolean,
    onCloudAppSelection: () -> Unit = { /** NOOP**/ },
    onCloudAppEditing: () -> Unit = { /** NOOP**/ }
) {
    val isConfigured =
        apiToken != null && authorizationKey != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(4.dp, SuccessText) else null,
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = if (isConfigured) {
            onCloudAppSelection
        } else {
            onCloudAppEditing
        }
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
                    color = MaterialTheme.colorScheme.primary,
                    text = "Name: "
                )

                cloudAppName?.let {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingLarge),
                        text = it
                    )
                }

                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Url: "
                )

                cloudAppUrl?.let {
                    Text(
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingLarge),
                        text = it
                    )
                }

            }

            Column(
                modifier = Modifier
                    .padding(all = LocalDimensions.current.paddingMedium)
                    .fillMaxHeight()
                    .weight(0.2f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    tint = if (authorizationKey != null)
                        Grey6
                    else
                        MaterialTheme.colorScheme.surface,
                    imageVector = Icons.Default.Key,
                    contentDescription = null
                )

                Icon(
                    tint = if (apiToken != null) {
                        if (apiTokenExpired)
                            ErrorText
                        else
                            Grey6
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    imageVector = Icons.Default.Token,
                    contentDescription = null
                )

                Icon(
                    tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    imageVector = Icons.Default.Edit,
                    modifier = Modifier.clickable { if (isConfigured) onCloudAppEditing() },
                    contentDescription = null
                )
            }
        }
    }
}