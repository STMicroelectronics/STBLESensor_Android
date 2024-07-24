package com.st.flow_demo.composable.common

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.core.GlobalConfig
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.helpers.gzip
import com.st.flow_demo.uploader.DeviceFlow
import com.st.flow_demo.uploader.CommunicationError
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.WarningText

@Composable
fun FlowDemoFlowUploadScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val flow = viewModel.flowSelected
    val expr = viewModel.expressionSelected

    var openConfirmationDialog by remember { mutableStateOf(value = false) }
    var flowLoaded by remember { mutableStateOf(false) }
    var flowReceived by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }

    var isError by remember {
        mutableStateOf(false)
    }

    val flowMessageReceived by viewModel.flowMessageReceived.collectAsStateWithLifecycle()
    val flowByteSent by viewModel.flowByteSent.collectAsStateWithLifecycle()

    val context = LocalContext.current

    BackHandler {
        openConfirmationDialog = true
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        if (flow != null) {

            val dataToSend by remember {
                derivedStateOf {
                    if (expr != null) {
                        DeviceFlow.getBoardStream(exp = expr, stats = listOf(flow))
                    } else {
                        DeviceFlow.getBoardStream(listOf(flow))
                    }
                }
            }

            val dataCompressed = gzip(dataToSend)



            when (flowMessageReceived.first) {

                CommunicationError.FLOW_NO_ERROR -> {
                    isError = false
                }

                CommunicationError.FLOW_RECEIVED_AND_PARSED -> {
                    isError = false
                    loading = false
                    flowLoaded = true
                    flowReceived = true
                }

                CommunicationError.FLOW_RECEIVED -> {
                    isError = false
                    loading = false
                    flowLoaded = false
                    flowReceived = true

                }
                else -> {
                    isError = true
                    loading = false
                    flowReceived = false
                }
            }

            Text(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                text = "Download Application"
            )
            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                fontSize = 18.sp,
                text = flow.description
            )

            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingLarge,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                fontSize = 14.sp,
                text = "Size Flow =${dataToSend.length} Bytes"
            )

            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingLarge,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                fontSize = 14.sp,
                text = "Size Compressed Flow =${dataCompressed.size} Bytes"
            )

            if ((!flowLoaded) && (!flowReceived)) {
                BlueMsButton(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
                    text = "Upload",
                    enabled = !loading,
                    onClick = {
                        loading = true
                        Log.i("FlowTmp", dataToSend)
                        viewModel.sendFlowToBoard(dataCompressed)
                    }
                )
            }

            if (loading) {
                currentProgress = (flowByteSent.toFloat() / dataCompressed.size)
                LinearProgressIndicator(
                    progress = {
                        currentProgress
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                    )
            }

            if (flowMessageReceived.second != null) {
                Text(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
                    fontSize = 18.sp,
                    color = (if (isError) ErrorText else InfoText),
                    text = flowMessageReceived.second!!
                )
            }

            if (flowLoaded) {

                Text(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal,
                        top = LocalDimensions.current.paddingNormal
                    ),
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = WarningText,
                    text = "Disconnect and reconnect to the board for enabling the new functionality"
                )

                BlueMsButton(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
                    text = "Done",
                    enabled = true,
                    onClick = {
                        val nodeId = viewModel.getNodeId()
                        nodeId?.let {
                            GlobalConfig.navigateBack?.let { it(nodeId)}
                        }
//                        val navOptions: NavOptions = navOptions {
//                            popUpTo("categoriesExample") { inclusive = true }
//                        }
//
//                        navController.navigate("categoriesExample", navOptions)
                    }
                )
            }
        } else {
            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ErrorText,
                text = "Error..."
            )
        }
    }

    if (openConfirmationDialog) {
        if (!flowLoaded) {
            FlowDemoAlertDialog(
                title = stringResource(id = context.applicationInfo.labelRes),
                message = "Stop Downloading Process\r\tContinue?",
                onDismiss = { openConfirmationDialog = false },
                onConfirmation = {
                    isError = false
                    loading = false
                    flowLoaded = false
                    currentProgress = 0f
                    navController.popBackStack() }
            )
        } else {
            flowLoaded = false
            isError = false
            loading = false
            currentProgress = 0f
            openConfirmationDialog = false
            navController.popBackStack()
        }
    }
}