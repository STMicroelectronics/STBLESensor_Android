package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.cloud_azure_iot_central.CloudAzureNavigationApplicationDetails
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun CloudAzureApplicationSelection(
    modifier: Modifier = Modifier,
    viewModel: CloudAzureIotCentralViewModel,
    navController: NavHostController
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.loadCloudAppConfigurationSaved()
            else -> Unit
        }
    }

    val listCloudApps by viewModel.listCloudApps.collectAsStateWithLifecycle()
    val isOneCloudAppConfig = viewModel.isOneCloudAppConfig.value

    val isCloudAppSelected by viewModel.selectedCloudAppNum.collectAsStateWithLifecycle()

    var oneCloudSelected by rememberSaveable { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .padding(PaddingValues(LocalDimensions.current.paddingNormal))
            .fillMaxSize()
    ) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = "Cloud Applications"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Text(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            style = MaterialTheme.typography.bodyLarge,
            color = Grey6,
            textAlign = TextAlign.Center,
            text = if (isOneCloudAppConfig) "Select one of the following application" else "Configure one of the following Cloud applications"
        )

        if (oneCloudSelected) {
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
                        append("Dev Config")
                    }
                    append("'")
                }
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            if (listCloudApps.isNotEmpty()) {
                itemsIndexed(listCloudApps) { index, cloudApp ->
                    CloudAppItem(
                        apiToken = cloudApp.apiToken,
                        authorizationKey = cloudApp.authorizationKey,
                        apiTokenExpired = cloudApp.apiTokenExpired,
                        cloudAppName = cloudApp.cloudApp.name,
                        cloudAppUrl = cloudApp.cloudApp.url,
                        isSelected = index == isCloudAppSelected,
                        //isSelected = isCloudAppSelected != viewModel.DEVICE_NOT_SELECTED,
                        onCloudAppSelection = {
                            oneCloudSelected = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setSelectedCloudApp(cloudApp.appIndex)
                        },
                        onCloudAppEditing = {
                            navController.navigate(
                                CloudAzureNavigationApplicationDetails.route + cloudApp.appIndex.toString()
                            )
                        })
                }
            }
        }
    }
}