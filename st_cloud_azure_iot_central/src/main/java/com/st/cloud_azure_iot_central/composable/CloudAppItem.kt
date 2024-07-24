package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.st.cloud_azure_iot_central.model.CloudAPIToken
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import com.st.cloud_azure_iot_central.R
import java.text.SimpleDateFormat
import java.util.Locale

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingNormal),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                painter = painterResource(id = R.drawable.cloud_write),
                modifier = Modifier.clickable { if (isConfigured) onCloudAppEditing() },
                contentDescription = null
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Name:"
                )

                cloudAppName?.let {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = it
                    )
                }

                Text(
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Url:"
                )

                cloudAppUrl?.let {
                    Text(
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = it
                    )
                }

                Text(
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Api Token:"
                )

                if (isConfigured) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessText,
                            text = "Configured"
                        )

                        Icon(
                            tint = SuccessText,
                            painter = painterResource(id = R.drawable.cloud_key),
                            contentDescription = null
                        )
                    }

                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        text = "Token Validity:"
                    )

                    if (apiTokenExpired) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorText,
                                text = "Expired"
                            )

                            Icon(
                                tint = ErrorText,
                                painter = painterResource(id = R.drawable.cloud_token_expired),
                                contentDescription = null
                            )
                        }

                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {

                            val tokenExpire = "Valid until " + SimpleDateFormat(
                                "dd/MM/yyyy hh:mm:ss",
                                Locale.getDefault()
                            ).format(apiToken!!.expire)

                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessText,
                                text = tokenExpire
                            )

                            Icon(
                                tint = SuccessText,
                                painter = painterResource(id = R.drawable.cloud_token),
                                contentDescription = null
                            )
                        }
                    }
                } else {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorText,
                        text = "Not Configured"
                    )
                }
            }
        }
    }
}