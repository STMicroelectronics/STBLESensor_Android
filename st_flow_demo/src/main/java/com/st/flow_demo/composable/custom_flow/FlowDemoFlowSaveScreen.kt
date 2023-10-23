package com.st.flow_demo.composable.custom_flow

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.helpers.FlowSaveDeleteState
import com.st.flow_demo.helpers.CUSTOM_FLOW_FOLDER
import com.st.flow_demo.helpers.FLOW_FILE_EXTENSION
import com.st.flow_demo.helpers.FLOW_FILE_TYPE
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFlowSaveScreen(
    viewModel: FlowDemoViewModel,
    paddingValues: PaddingValues,
    navController: NavHostController
) {
    //val savedFlowState by viewModel.savedFlowState.collectAsStateWithLifecycle()
    val flowSaveDeleteState by viewModel.flowSaveDeleteState.collectAsState()
    val flow = viewModel.flowSelected

    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(FLOW_FILE_TYPE)
    ) { fileUri ->
        fileUri?.let {
            viewModel.saveFlowOnPhone(context, fileUri)
        }
    }

    if (flow != null) {
        var name by remember { mutableStateOf(value = flow.description) }
        var description by remember { mutableStateOf(value = flow.notes) }


        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                text = "App Details"
            )
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Grey6,
                text = "Add a name and notes to your Application\nThe Application Name will be also the FileName"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Text(
                fontSize = 14.sp,
                color = Grey6,
                text = "Name"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            TextField(value = name, onValueChange = {
                name = it
            })

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Text(
                fontSize = 14.sp,
                color = Grey6,
                text = "Description"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            if (description != null) {
                TextField(value = description!!, onValueChange = {
                    description = it
                })
            } else {
                description = ""
                TextField(value = description!!, onValueChange = {
                    description = it
                })
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = LocalDimensions.current.paddingNormal),
            ) {
                BlueMsButton(
                    text = stringResource(id = R.string.terminate_new_app),
                    iconPainter = painterResource(id = R.drawable.ic_close),
                    onClick = {
                        //Don't save the Flow and come back to previous screen
                        navController.popBackStack()
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                BlueMsButton(
                    text = stringResource(id = R.string.save_new_app),
                    iconPainter = painterResource(id = R.drawable.ic_done),
                    onClick = {
                        //Update Name and Description
                        flow.description = name
                        flow.notes = description
                        //Add the board compatibility
                        val boardName = viewModel.getBoardType().name
                        if (!flow.board_compatibility.contains(boardName)) {
                            flow.board_compatibility.add(boardName)
                        }
                        viewModel.flowSelected = flow

                        //Save the Flow on the Disk
                        pickFileLauncher.launch(flow.description + FLOW_FILE_EXTENSION)
                    }
                )
            }

        }
    }

    when (flowSaveDeleteState) {
        FlowSaveDeleteState.DEAD_BEEF -> {}
        FlowSaveDeleteState.SAVED -> {
            //LaunchedEffect(savedFlowState) {
            Toast.makeText(
                context,
                "Flow Saved on Documents/$CUSTOM_FLOW_FOLDER",
                Toast.LENGTH_SHORT
            ).show()
            //}
            viewModel.resetSavedFlowState()
            navController.popBackStack(route = "flowsExpert", inclusive = false)
            navController.navigate("flowsExpert")
        }
        FlowSaveDeleteState.DELETED -> {
            Toast.makeText(
                context,
                "Flow Deleted on Documents/$CUSTOM_FLOW_FOLDER",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetSavedFlowState()
        }

        FlowSaveDeleteState.ERROR_DELETING -> {
            Toast.makeText(
                context,
                "Error Deleting the Flow: ${flowSaveDeleteState.name}",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetSavedFlowState()
        }

        else -> {
            Toast.makeText(
                context,
                "Error Saving the Flow: ${flowSaveDeleteState.name}",
                Toast.LENGTH_SHORT
            ).show()

            viewModel.resetSavedFlowState()
        }
    }
}