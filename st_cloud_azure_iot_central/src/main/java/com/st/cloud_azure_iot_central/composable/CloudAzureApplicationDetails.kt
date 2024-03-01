package com.st.cloud_azure_iot_central.composable

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.cloud_azure_iot_central.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudAzureApplicationDetails(
    modifier: Modifier = Modifier,
    viewModel: CloudAzureIotCentralViewModel,
    appId: Int,
    navController: NavHostController,
) {
    val selectedApp = viewModel.listCloudApps.collectAsState().value[appId]

    var openHelpDialog by remember { mutableStateOf(value = false) }

    var isValidToken by remember { mutableStateOf(selectedApp.apiToken != null) }

    var isTokenExpired by remember(isValidToken) { mutableStateOf(selectedApp.apiTokenExpired) }

    var openCamera by remember {
        mutableStateOf(value = false)
    }

    var enableTokenRequestButton by remember {
        mutableStateOf(value = false)
    }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(LocalDimensions.current.paddingNormal)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = selectedApp.cloudApp.name ?: "Configure Cloud Application"
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                tint = if (selectedApp.authorizationKey != null)
                    Grey6
                else
                    MaterialTheme.colorScheme.surface,
                imageVector = Icons.Default.Key,
                contentDescription = null
            )

            Icon(
                tint = if (isValidToken) {
                    if (isTokenExpired) ErrorText else Grey6
                } else
                    MaterialTheme.colorScheme.surface,
                imageVector = Icons.Default.Token,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(0.9f),
                style = MaterialTheme.typography.bodySmall,
                color = Grey6,
                text = buildAnnotatedString {
                    append("Here you can configure your Azure Iot Central application. For more information click the '")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Help")
                    }
                    append("' button")
                })

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconSmall)
                    .clickable { openHelpDialog = true },
                tint = MaterialTheme.colorScheme.primary,
                imageVector = Icons.Default.Help,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.titleSmall,
            text = "Shareable Link"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier.fillMaxWidth(0.9f),
                style = MaterialTheme.typography.bodySmall,
                color = Grey6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = selectedApp.cloudApp.shareableLink ?: "Shareable Link Not present"
            )

            selectedApp.cloudApp.shareableLink?.let {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconSmall)
                        .clickable {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, selectedApp.cloudApp.shareableLink)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(context, shareIntent, null)
                        },
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.Share,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.titleSmall,
            text = "App Name"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        var urlAppName by remember {
            mutableStateOf(
                value = if (selectedApp.cloudApp.url != null) {
                    selectedApp.cloudApp.url!!.removePrefix("https://")
                        .removeSuffix(".azureiotcentral.com")
                } else {
                    ""
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                modifier = Modifier.weight(3f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                value = urlAppName,
                onValueChange = {
                    urlAppName = it
                    if (it.isNotBlank()) {
                        selectedApp.cloudApp.url = "https://${urlAppName}.azureiotcentral.com"
                    }
                }
            )

            Text(
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodySmall,
                color = Grey6,
                text = ".azureiotcentral.com"
            )
        }

        var authorizationKey by remember {
            mutableStateOf(
                value = selectedApp.authorizationKey ?: ""
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        if (urlAppName.isNotBlank()) {
            Text(
                style = MaterialTheme.typography.titleSmall,
                text = "API Token"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            BlueMsButton(
                text = "Configure with QR Code",
                iconPainter = painterResource(id = R.drawable.qr_code_scanner),
                onClick = { openCamera = true }
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                value = authorizationKey,
                onValueChange = {
                    authorizationKey = it
                    enableTokenRequestButton = true
                }
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        if (enableTokenRequestButton) {
            BlueMsButtonOutlined(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                text = "Token Request",
                iconPainter = painterResource(id = R.drawable.cloud_token),
                onClick = {
                    selectedApp.authorizationKey = authorizationKey
                    CoroutineScope(Dispatchers.IO).launch {
                        selectedApp.apiToken = viewModel.readAPITokenDetails(selectedApp)
                        selectedApp.apiToken?.let {
                            isValidToken = true
                            enableTokenRequestButton = false
                            isTokenExpired = selectedApp.apiToken!!.expire < Date()
                            selectedApp.apiTokenExpired = isTokenExpired
                        }
                    }
                }
            )
        }

        if (isValidToken) {
            val tokenExpire = SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss",
                Locale.getDefault()
            ).format(selectedApp.apiToken!!.expire)
            if (isTokenExpired) {
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingMedium),
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorText,
                    text = "Token Expired $tokenExpire!!"
                )
            } else {
                Text(
                    modifier = Modifier.padding(start = LocalDimensions.current.paddingMedium),
                    style = MaterialTheme.typography.bodySmall,
                    color = Grey6,
                    text = "Token Expiry $tokenExpire"
                )
            }

            if (isTokenExpired) {
                BlueMsButton(
                    modifier = Modifier.padding(top = LocalDimensions.current.paddingSmall, start = LocalDimensions.current.paddingMedium),
                    text = "Token Update",
                    iconPainter = painterResource(id = R.drawable.cloud_token),
                    color = SuccessText,
                    onClick = {
                        selectedApp.authorizationKey = authorizationKey
                        CoroutineScope(Dispatchers.IO).launch {
                            selectedApp.apiToken = viewModel.readAPITokenDetails(selectedApp)
                            selectedApp.apiToken?.let {
                                isValidToken = true
                                enableTokenRequestButton = false
                                isTokenExpired = selectedApp.apiToken!!.expire < Date()
                                selectedApp.apiTokenExpired = isTokenExpired
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BlueMsButton(
                text = stringResource(id = android.R.string.cancel),
                onClick = {
                    navController.popBackStack()
                }
            )

            BlueMsButton(
                enabled = isValidToken,
                color = ErrorText ,
                text = "Reset",
                onClick = {
                    selectedApp.apiTokenExpired = false
                    isValidToken = false
                    selectedApp.apiToken=null
                    selectedApp.authorizationKey =null
                    authorizationKey = ""

                    // Reset the current Saved Cloud App
                    viewModel.resetSavedCurrentCloudApp(selectedApp)

                    //Check if we had one application configured for the running fw
                    viewModel.checkIfOneCloudAppIsConfigured()
                }
            )

            BlueMsButton(
                color = if(isTokenExpired) WarningText else null,
                text = stringResource(id = android.R.string.ok),
                onClick = {
                    //Set CloudApp Configuration Done
                    if(isValidToken) {
                        //Save the configured Cloud App
                        viewModel.saveCurrentCloudApp(selectedApp)
                        viewModel.cloudAppConfigurationDone()
                    }

                    navController.popBackStack()
                }
            )
        }

        if (openHelpDialog) {
            Dialog(onDismissRequest = { openHelpDialog = false }) {
                CloudAzureHelpAppConfiguration(onDismissRequest = { openHelpDialog = false })
            }
        }

        if (openCamera) {
            Dialog(onDismissRequest = { openCamera = false }) {
                CameraBarCodeScanner(onClose = { barCode ->
                    openCamera = false
                    barCode?.let {
                        authorizationKey = barCode

                        selectedApp.authorizationKey = authorizationKey

                        CoroutineScope(Dispatchers.IO).launch {
                            selectedApp.apiToken = viewModel.readAPITokenDetails(selectedApp)
                            selectedApp.apiToken?.let {
                                isValidToken = true
                                isTokenExpired = selectedApp.apiToken!!.expire < Date()
                                selectedApp.apiTokenExpired = isTokenExpired
                            }
                        }
                    }
                })
            }
        }
    }
}