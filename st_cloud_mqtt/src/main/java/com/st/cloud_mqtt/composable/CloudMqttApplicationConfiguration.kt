package com.st.cloud_mqtt.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.cloud_mqtt.CloudMqttViewModel
import com.st.cloud_mqtt.model.CloudMqttServerConfig
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun CloudMqttApplicationConfiguration(
    modifier: Modifier = Modifier,
    viewModel: CloudMqttViewModel
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.loadMqttCloudAppConfigurationSaved()
            else -> Unit
        }
    }
    val cloudMqttServerConfig by viewModel.cloudMqttServerConfig.collectAsStateWithLifecycle()

    var hostUrl: String by remember(cloudMqttServerConfig) {
        mutableStateOf(
            cloudMqttServerConfig?.hostUrl ?: ""
        )
    }

    var hostPort: Int by remember(cloudMqttServerConfig) {
        mutableIntStateOf(
            cloudMqttServerConfig?.hostPort
                ?: 1883
        )
    }

    var userName: String by remember(cloudMqttServerConfig) {
        mutableStateOf(
            cloudMqttServerConfig?.userName ?: ""
        )
    }

    var userPassWd: String by remember(cloudMqttServerConfig) {
        mutableStateOf(
            cloudMqttServerConfig?.userPassWd ?: ""
        )
    }

    var deviceId: String by remember(cloudMqttServerConfig) {
        mutableStateOf(
            cloudMqttServerConfig?.deviceId ?: ""
        )
    }

    var showPassword by remember { mutableStateOf(value = false) }

    var showInfo by remember { mutableStateOf(value = false) }

    var cloudConfigured by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(PaddingValues(LocalDimensions.current.paddingNormal))
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = "MQTT Server Configuration"
        )

        if (cloudConfigured) {
            Text(
                modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
                style = MaterialTheme.typography.bodyLarge,
                color = Grey6,
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    append("Click on  '")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Dev Upload")
                    }
                    append("'")
                }
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            shadowElevation = LocalDimensions.current.elevationNormal,
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(LocalDimensions.current.paddingNormal)
            ) {

                //Host
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.titleSmall,
                    text = "Host Url"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri).copy(
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = hostUrl,
                    placeholder = { Text("Broker URL", color = Grey6) },
                    onValueChange = {
                        hostUrl = it
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.titleSmall,
                    text = "Port"
                )

                OutlinedTextField(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = hostPort.toString(),
                    onValueChange = {
                        hostPort = it.toIntOrNull() ?: 1883
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                //Credentials
                //User Name
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.titleSmall,
                    text = "User Name"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri).copy(
                        imeAction = ImeAction.Done
                    ),
                    placeholder = { Text("Optional", color = Grey6) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = userName,
                    onValueChange = {
                        userName = it
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                //Password
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.titleSmall,
                    text = "Password"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password).copy(
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = userPassWd,
                    onValueChange = {
                        userPassWd = it
                    },
                    placeholder = { Text("Optional", color = Grey6) },
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        if (showPassword) {
                            IconButton(onClick = { showPassword = false }) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "hide_password"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { showPassword = true }) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "hide_password"
                                )
                            }
                        }
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                //Client ID
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                    style = MaterialTheme.typography.titleSmall,
                    text = "Device ID"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LocalDimensions.current.paddingNormal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text).copy(
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = deviceId,
                    onValueChange = {
                        deviceId = it
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
            }
        }

        Row(modifier = Modifier
            .clickable { showInfo = !showInfo }
            .padding(start = LocalDimensions.current.paddingNormal)) {
            Icon(
                modifier = Modifier
                    .padding(end = LocalDimensions.current.paddingMedium)
                    .size(size = LocalDimensions.current.iconSmall),
                tint = MaterialTheme.colorScheme.primary,
                imageVector = Icons.Default.Info,
                contentDescription = null
            )

            Text(
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                text = if (showInfo) "Hide Info" else "Show Info"
            )
        }

        if (showInfo) {
            Text(
                modifier = Modifier.padding(top = LocalDimensions.current.paddingSmall),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                text = buildAnnotatedString {
                    append("Every feature will be published on one topic \"")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("DeviceId/featureName")
                    }
                    append("\".\nExample for ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Accelerometer")
                    }
                    append(" with DeviceId=")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("TestDevice")
                    }
                    append(":\ntopic:\n\t")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("TestDevice/Accelerometer")
                    }
                    append("\nmessage:\n\t")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("{\"x\": \"400.0\", \"y\": \"446.0\" , \"z\": \"829.0\"}")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(2f))

        if (hostUrl.isNotEmpty() && deviceId.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LocalDimensions.current.paddingNormal),
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                BlueMsButton(
                    color = ErrorText,
                    text = "Reset",
                    onClick = {
                        cloudConfigured = false
                        viewModel.resetAndDeletedSavedMqttCloudApp()
                    }
                )


                BlueMsButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        cloudConfigured = true
                        viewModel.configureAndSaveMqttCloudApp(
                            CloudMqttServerConfig(
                                hostUrl = hostUrl,
                                hostPort = hostPort,
                                userName = userName,
                                userPassWd = userPassWd,
                                deviceId = deviceId
                            )
                        )
                    }
                )
            }
        }
    }
}