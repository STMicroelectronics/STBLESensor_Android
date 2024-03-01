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
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.cloud_mqtt.CloudMqttNavigationApplicationConfiguration
import com.st.ui.composables.BlueMSFeatureItem
import com.st.ui.composables.BlueMSSnackBar
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlinx.coroutines.launch

@Composable
fun CloudMqttDeviceConnection(
    modifier: Modifier = Modifier,
    viewModel: CloudMqttViewModel,
    navController: NavHostController
) {
    val deviceConnected by viewModel.deviceConnected.collectAsStateWithLifecycle()

    val features = viewModel.availableFeatures
    val featuresEnabled= viewModel.featuresEnabled

    val sendFeatureValue by viewModel.sendFeatureValue.collectAsStateWithLifecycle()

    val sendTopic by viewModel.sendTopic.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val retValue by viewModel.retValue.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val possibleUpdateIntervals: List<Int> =
        listOf(500, 1000, 2000, 5000, 10000)

    var updateInterval by remember { mutableIntStateOf(viewModel.updateInterval) }
    var openUpdateIntervalSelectionDialog by remember { mutableStateOf(value = false) }

    BackHandler {
        if (deviceConnected) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
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
            BlueMSSnackBar(
                snackbarHostState = snackbarHostState,
                onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "Device [ ${viewModel.cloudMqttServerConfig.value?.deviceId ?: ""} ] Status"
                            )
                            Text(
                                modifier = Modifier.padding(start = LocalDimensions.current.paddingMedium),
                                text = if (deviceConnected) "Connected" else {
                                    if (isLoading) "Connecting" else "Not Connected"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

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
                                    imageVector = Icons.Default.CloudOff,
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
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null
                                )
                            }
                        }
                    }

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
                        Row(
                            modifier = Modifier
                                .padding(
                                    top = LocalDimensions.current.paddingNormal,
                                    bottom = LocalDimensions.current.paddingNormal
                                )
                                .fillMaxWidth()
                                .clickable { openUpdateIntervalSelectionDialog = true },
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "Update Interval:"
                            )

                            Text(
                                modifier = Modifier
                                    .padding(LocalDimensions.current.paddingNormal),
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = TextDecoration.Underline,
                                color = SecondaryBlue,
                                text = updateInterval.toString()
                            )

                            Text(
                                modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                                style = MaterialTheme.typography.bodyLarge,
                                text = "[mSec]"
                            )
                        }
                        
                        sendFeatureValue?.let {
                            Text(
                                modifier = Modifier.padding(LocalDimensions.current.paddingSmall),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "Sending Sensor:"
                            )

                            sendTopic?.let {
                                Text(
                                    modifier = Modifier
                                        .padding(start = LocalDimensions.current.paddingMedium)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    text = sendTopic!!,
                                    maxLines = 1,
                                    minLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                modifier = Modifier
                                    .padding(start = LocalDimensions.current.paddingMedium)
                                    .fillMaxWidth(),
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
                    if ((features != null) && (featuresEnabled!=null)){

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
            snackbarHostState.showSnackbar(message = text, duration = SnackbarDuration.Short)
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

    AlertDialog(
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
                    TextField(
                        value = selectedInterval.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
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