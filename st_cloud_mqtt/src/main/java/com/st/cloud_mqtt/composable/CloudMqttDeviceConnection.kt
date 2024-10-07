package com.st.cloud_mqtt.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.st.cloud_mqtt.CloudMqttViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.cloud_mqtt.CloudMqttNavigationApplicationConfiguration
import com.st.ui.composables.BlueMSFeatureItem
import com.st.ui.composables.BlueMSSnackBarMaterial3
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlinx.coroutines.launch
import com.st.cloud_mqtt.R
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningText

@Composable
fun CloudMqttDeviceConnection(
    modifier: Modifier = Modifier,
    viewModel: CloudMqttViewModel,
    navController: NavHostController
) {
    val deviceConnected by viewModel.deviceConnected.collectAsStateWithLifecycle()

    val features = viewModel.availableFeatures
    val featuresEnabled = viewModel.featuresEnabled

    val sendFeatureValue by viewModel.sendFeatureValue.collectAsStateWithLifecycle()

    val sendTopic by viewModel.sendTopic.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val retValue by viewModel.retValue.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val possibleUpdateIntervals: List<Int> =
        listOf(500, 1000, 2000, 5000, 10000)

    var updateInterval by remember { mutableIntStateOf(viewModel.updateInterval) }
    var openUpdateIntervalSelectionDialog by remember { mutableStateOf(value = false) }

    BackHandler {
        if (deviceConnected) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(
                    message = "Disconnect the device before",
                    actionLabel = "dismiss",
                    duration = SnackbarDuration.Indefinite
                )
            }
        } else {
            navController.navigate(CloudMqttNavigationApplicationConfiguration.route) {
                navController.graph.startDestinationRoute?.let { screenRoute ->
                    popUpTo(screenRoute) {
                        saveState = false
                    }
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    Scaffold(
        modifier = modifier.padding(LocalDimensions.current.paddingNormal),
        snackbarHost = {
            BlueMSSnackBarMaterial3(
                snackBarHostState = snackBarHostState
            )
        }
    ) { paddingValue ->

        Column(
            modifier = modifier
                .padding(paddingValue)
                .fillMaxSize()
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = "Device Connection"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationSmall
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LocalDimensions.current.paddingNormal),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = { if (deviceConnected) viewModel.disconnectDevice() else viewModel.connectDevice() }) {

                        if (deviceConnected) {
                            Icon(
                                modifier = Modifier
                                    .background(
                                        ErrorText,
                                        Shapes.medium
                                    )
                                    .padding(
                                        LocalDimensions.current.paddingSmall
                                    ),
                                tint = Grey0,
                                imageVector = Icons.Default.Stop,
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        Shapes.medium
                                    )
                                    .padding(
                                        LocalDimensions.current.paddingSmall
                                    ),
                                tint = Grey0,
                                painter = painterResource(id = R.drawable.cloud_dev_upload),
                                contentDescription = null
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LocalDimensions.current.paddingNormal)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    ) {

                        Text(
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            text = "Device name:"
                        )

                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = viewModel.cloudMqttServerConfig.value?.deviceId ?: ""
                        )

                        Text(
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            text = "Status:"
                        )

                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = if (deviceConnected) "Connected" else {
                                if (isLoading) "Connecting" else "Not Connected"
                            },
                            color = if (deviceConnected) SuccessText else {
                                if (isLoading) Color.Unspecified else WarningText
                            }
                        )


                        if (isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        LocalDimensions.current.paddingNormal
                                    )
                            )
                        }

                        if (deviceConnected) {
                            HorizontalDivider(modifier = Modifier.padding(LocalDimensions.current.paddingNormal))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    text = "Update Interval:"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { openUpdateIntervalSelectionDialog = true },
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {


                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                        color = SecondaryBlue,
                                        text = updateInterval.toString()
                                    )

                                    Text(
                                        modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = "[mSec]"
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(LocalDimensions.current.paddingNormal))

                            sendFeatureValue?.let {
                                Text(
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    text = "Publish Sensor:"
                                )

                                sendTopic?.let {
                                    if (sendTopic!!.isNotBlank()) {
                                        val topic = "topic $sendTopic"
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodySmall,
                                            text = topic,
                                            maxLines = 1,
                                            minLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = LocalDimensions.current.paddingNormal),
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    minLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                        }

                    }
                }
            }

            AnimatedVisibility(
                visible = deviceConnected,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    Text(
                        modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        text = "Select Features:"
                    )
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
                    if ((features != null) && (featuresEnabled != null)) {

                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(),
                            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                        ) {
                            itemsIndexed(features) { index, feature ->
                                BlueMSFeatureItem(
                                    isChecked = featuresEnabled[index],
                                    featureName = feature.name,
                                    onSelected = { enable ->

                                        featuresEnabled[index] = enable

                                        if (enable) {
                                            viewModel.enableFeature(feature)
                                        } else {
                                            viewModel.disableFeature(feature)
                                        }
                                    })
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ErrorText,
                            text = "No Features available"
                        )
                    }
                }
            }
        }

        if (openUpdateIntervalSelectionDialog) {
            UpdateIntervalSelectionDialog(possibleUpdateIntervals = possibleUpdateIntervals,
                updateInterval = updateInterval,
                onDismiss = { openUpdateIntervalSelectionDialog = false },
                onConfirmation = { value ->
                    updateInterval = value
                    viewModel.updateInterval = value
                    openUpdateIntervalSelectionDialog = false
                })
        }
    }

    retValue?.let {
        val text = retValue!!
        viewModel.cleanError()
        coroutineScope.launch {
            snackBarHostState.showSnackbar(message = text, duration = SnackbarDuration.Short)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateIntervalSelectionDialog(
    possibleUpdateIntervals: List<Int>,
    updateInterval: Int,
    onDismiss: () -> Unit = { /** NOOP**/ },
    onConfirmation: (Int) -> Unit = { /** NOOP**/ }
) {

    var expanded by remember { mutableStateOf(value = false) }

    var selectedInterval by remember(updateInterval) { mutableIntStateOf(value = updateInterval) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Update Interval [mSec]"
                )

                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal),
                    expanded = expanded,
                    onExpandedChange = { newValue ->
                        expanded = newValue
                    }
                ) {
                    OutlinedTextField(
                        value = selectedInterval.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        possibleUpdateIntervals.forEach { value ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedInterval = value
                                    expanded = false
                                },
                                text = {
                                    Text(value.toString())
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    BlueMsButton(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        text = stringResource(id = android.R.string.cancel),
                        onClick = {
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    BlueMsButton(
                        text = stringResource(id = android.R.string.ok),
                        enabled = true,
                        onClick = {
                            onConfirmation(selectedInterval)
                        }
                    )
                }
            }
        }
    }
}