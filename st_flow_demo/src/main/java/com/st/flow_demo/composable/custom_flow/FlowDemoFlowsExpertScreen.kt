package com.st.flow_demo.composable.custom_flow

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.DestinationFlowDemoFlowDetailScreen
import com.st.flow_demo.DestinationFlowDemoFlowExpertEditingScreen
import com.st.flow_demo.DestinationFlowDemoFlowIfApplicationCreationScreen
import com.st.flow_demo.DestinationFlowDemoFlowUploadScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.common.FlowDemoAlertDialog
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFlowsExpertScreen(
    viewModel: FlowDemoViewModel,
    paddingValues: PaddingValues,
    navController: NavHostController
) {

    val context = LocalContext.current

    //navController.popBackStack(route = "flowExpert", inclusive = false)
    var openConfirmationDialog by remember { mutableStateOf(value = false) }
    val flowsCustomList by viewModel.flowsCustomList.collectAsStateWithLifecycle()


    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { dirUri ->
        dirUri?.let {
            viewModel.parseSavedFlow(context, DocumentFile.fromTreeUri(context, dirUri))
        }
    }

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            text = "Custom Apps"
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Text(
            fontSize = 16.sp,
            color = Grey6,
            text = "Upload and run the app on your board"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        Text(
            fontSize = 16.sp,
            color = Grey6,
            text = "Your Apps"
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = LocalDimensions.current.paddingNormal),
            horizontalArrangement = Arrangement.End
        ) {
            BlueMsButton(
                text = stringResource(id = R.string.create_new_app),
                iconPainter = painterResource(id = R.drawable.ic_add),
                onClick = {
                    //We reset the current Flow for creating a new One
                    viewModel.flowSelected = null
                    viewModel.expressionSelected = null
                    viewModel.flowOnCreation = null
                    viewModel.resetSavedFlowState()
                    navController.navigate(
                        DestinationFlowDemoFlowExpertEditingScreen.route
                    )
                }
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

            BlueMsButton(
                text = stringResource(id = R.string.create_new_if_app),
                onClick = {
                    viewModel.expressionSelected = null
                    viewModel.flowSelected = null
                    viewModel.flowOnCreation = null
                    navController.navigate(
                        DestinationFlowDemoFlowIfApplicationCreationScreen.route
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    end = LocalDimensions.current.paddingNormal,
                    top = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
            horizontalArrangement = Arrangement.End
        ) {
            BlueMsButton(
                text = if (flowsCustomList.isEmpty())
                    "Load Saved Flow"
                else
                    "Rescan Saved Flow",
                iconPainter = if (flowsCustomList.isEmpty())
                    painterResource(id = R.drawable.ic_file_open)
                else
                    painterResource(id = R.drawable.ic_reload),
                onClick = {
                    pickFileLauncher.launch(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS
                        ).toUri()
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            items(flowsCustomList.toList()) { flow ->
                FlowDemoFlowCustomItem(
                    flow = flow,
                    onFlowSelected = {
                        viewModel.flowSelected = flow
                        navController.navigate(
                            DestinationFlowDemoFlowDetailScreen.route
                        )
                    },
                    onPlayFlow = {
                        viewModel.flowSelected = flow

                        viewModel.reportExpertAppAnalytics(flow)
                        viewModel.reportExpertAppInputAnalytics(flow)
                        viewModel.reportExpertAppFunctionAnalytics(flow)
                        viewModel.reportExpertAppOutputAnalytics(flow)

                        navController.navigate(
                            DestinationFlowDemoFlowUploadScreen.route
                        )
                    },
                    onDeleteFlow = {
                        viewModel.flowSelected = flow
                        openConfirmationDialog = true
                    })
            }
        }
    }

    if (openConfirmationDialog) {
        FlowDemoAlertDialog(
            title = "Warning!",
            message = stringResource(id = R.string.request_delete_selected_flow),
            onDismiss = { openConfirmationDialog = false },
            onConfirmation = {
                openConfirmationDialog = false
                viewModel.deleteFlow()
            }
        )
    }
}

//class MapLauncherContract: ActivityResultContract<Uri?, Uri?>() {
//    @CallSuper
//    override fun createIntent(context: Context, input: Uri?): Intent {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//        if (input != null) {
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
//        }
////        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
////        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//        return intent
//    }
//
//    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
//        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
//    }
//}