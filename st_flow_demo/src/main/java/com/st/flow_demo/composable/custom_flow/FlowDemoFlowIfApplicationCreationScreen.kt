package com.st.flow_demo.composable.custom_flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.DestinationFlowDemoFlowUploadScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.common.ClickableTest
import com.st.flow_demo.composable.common.FlowDemoAlertDialog
import com.st.flow_demo.helpers.canBeUsedAsExp
import com.st.flow_demo.helpers.getOutputIconResourceByName
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.WarningText

@Composable
fun FlowDemoFlowIfApplicationCreationScreen(
    viewModel: FlowDemoViewModel,
    paddingValues: PaddingValues,
    navController: NavHostController
) {

    val context = LocalContext.current
    var openConfirmationDialog by remember { mutableStateOf(value = false) }
    var openAddSavedExprDialog  by remember { mutableStateOf(value = false) }
    var openAddApplicationDialog  by remember { mutableStateOf(value = false) }

    //var selectedExpression by remember { mutableStateOf( viewModel.expressionSelected)}

    val expressionSelected = viewModel.expressionSelected
    val flowSelected = viewModel.flowSelected

    val availableCustomFlowList by viewModel.flowsCustomList.collectAsStateWithLifecycle()

    val filteredAvailableCustomExprFlowList = availableCustomFlowList.filter { it -> canBeUsedAsExp(it) }

    BackHandler {
        openConfirmationDialog = true
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            text = "IF condition"
        )

        if(filteredAvailableCustomExprFlowList.isEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = WarningText,
                text = "It's necessary to create before at least one New Application selecting like output \"Save as EXP\""
            )
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(
                        start = LocalDimensions.current.paddingLarge,
                        top = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingLarge,
                        end = LocalDimensions.current.paddingNormal
                    ),
                    fontSize = 20.sp,
                    text = "Condition",
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal)
                        .padding(end = LocalDimensions.current.paddingNormal)
                        .rotate(90f),
                    painter = painterResource(id = R.drawable.ic_start),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }

            if (expressionSelected == null) {
                ClickableTest(
                    text = stringResource(id = R.string.choose_expression_new_app),
                    onClick = { openAddSavedExprDialog = true })
            } else {
                FlowDemoInputFunctionOutputConfigurationListItem(
                    iconId = getOutputIconResourceByName(expressionSelected.outputs[0].icon),
                    label = expressionSelected.description,
                    hasSettings = false,
                )

                ClickableTest(
                    text = stringResource(id = R.string.change_expression_new_app),
                    onClick = { openAddSavedExprDialog = true })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(
                        start = LocalDimensions.current.paddingLarge,
                        top = LocalDimensions.current.paddingNormal,
                        bottom = LocalDimensions.current.paddingNormal
                    ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(
                        start = LocalDimensions.current.paddingLarge,
                        end = LocalDimensions.current.paddingNormal
                    ),
                    fontSize = 20.sp,
                    text = "Execute",
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal)
                        .padding(end = LocalDimensions.current.paddingNormal),
                    painter = painterResource(id = R.drawable.ic_arrow_downward),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        }

        if(flowSelected==null) {
            ClickableTest(
                text = stringResource(id = R.string.choose_app_to_upload_new_app),
                onClick = { openAddApplicationDialog = true })
        } else {
            FlowDemoInputFunctionOutputConfigurationListItem(
                iconId = getOutputIconResourceByName(flowSelected.outputs[0].icon),
                label = flowSelected.description,
                hasSettings = false,
            )

            ClickableTest(
                text = stringResource(id = R.string.change_app_to_upload_new_app),
                onClick = { openAddApplicationDialog = true })
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

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
                    viewModel.flowSelected = null
                    viewModel.expressionSelected = null
                    openConfirmationDialog = true

                }
            )

            Spacer(modifier = Modifier.weight(1f))

            BlueMsButton(
                text = stringResource(id = R.string.play_flow),
                iconPainter = painterResource(id =R.drawable.ic_upload),
                enabled = (viewModel.expressionSelected!=null) &&  (viewModel.flowSelected!=null),
                onClick = {
                    navController.navigate(
                        DestinationFlowDemoFlowUploadScreen.route
                    )
                }
            )
        }

    }

    if (openConfirmationDialog) {
        FlowDemoAlertDialog(
            title = stringResource(id = context.applicationInfo.labelRes),
            message = "Losing all changes.\r\tContinue?",
            onDismiss = { openConfirmationDialog = false },
            onConfirmation = {
                viewModel.expressionSelected = null
                viewModel.flowSelected = null
                navController.popBackStack() }
        )
    }

    if(openAddSavedExprDialog) {
        FlowDemoAddSavedExprDialog(viewModel = viewModel,
            onDismissRequest = {openAddSavedExprDialog=false})
    }

    if(openAddApplicationDialog) {
        FlowDemoAddAppExprDialog(viewModel = viewModel,
            onDismissRequest = {openAddApplicationDialog=false})
    }
}