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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.common.ClickableTest
import com.st.flow_demo.composable.common.FlowDemoAlertDialog
import com.st.flow_demo.helpers.getFunctionIconResourceByName
import com.st.flow_demo.helpers.getOutputIconResourceByName
import com.st.flow_demo.helpers.getSensorIconResourceByName
import com.st.flow_demo.helpers.isAlsoExp
import com.st.flow_demo.models.Flow
import com.st.flow_demo.models.Function
import com.st.flow_demo.models.Output
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.flow_demo.DestinationFlowDemoFlowSaveScreen
import com.st.flow_demo.DestinationFlowDemoFunctionConfigurationScreen
import com.st.flow_demo.DestinationFlowDemoOutputConfigurationScreen
import com.st.flow_demo.DestinationFlowDemoSensorConfigurationScreen
import com.st.flow_demo.models.createNewFlow
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFlowExpertEditingScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    //we start with the Flow
    var flow = viewModel.flowOnCreation

    var openConfirmationDialog by remember { mutableStateOf(value = false) }
    var openAddInputDialog by remember { mutableStateOf(value = false) }
    var openAddFunctionDialog by remember { mutableStateOf(value = false) }
    var openAddOutputDialog by remember { mutableStateOf(value = false) }

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
            Text(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                text = "Modify Application"
            )
            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                fontSize = 18.sp,
                text = flow!!.description
            )
        } else {
            Text(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                text = "Create Application"
            )

            //Create a new Flow
            flow = createNewFlow(viewModel.getBoardType().name)
        }

        var functions by remember(key1 = flow!!.functions) { mutableStateOf(value = flow!!.functions) }
        val inputs by remember(key1 = flow!!.sensors) { mutableStateOf(value = flow!!.sensors) }
        val outputs by remember(key1 = flow!!.outputs) { mutableStateOf(value = flow!!.outputs) }
        val flows by remember(key1 = flow!!.flows) { mutableStateOf(value = flow!!.flows) }

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
                text = stringResource(id = R.string.app_input),
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

        if (inputs.isEmpty() && flows.isEmpty()) {
            ClickableTest(
                text = stringResource(id = R.string.choose_in_new_app),
                onClick = { openAddInputDialog = true })
        } else {
            if (inputs.isNotEmpty()) {
                FlowDemoInputsConfigurationList(sensors = inputs, onConfig = { sensor ->
                    viewModel.sensorOnConfig = sensor.copy()
                    flow!!.sensors = inputs
                    flow!!.functions = functions
                    flow!!.outputs = outputs
                    viewModel.flowOnCreation = flow
                    navController.navigate(
                        DestinationFlowDemoSensorConfigurationScreen.route
                    )

                })
            }

            if (flows.isNotEmpty()) {
                FlowDemoInputsFlowConfigurationList(flows = flows)
            }

            ClickableTest(
                text = stringResource(id = R.string.change_in_new_app),
                onClick = { openAddInputDialog = true })
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
                text = stringResource(id = R.string.app_function),
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

        if (functions.isEmpty()) {
            ClickableTest(
                text = stringResource(id = R.string.choose_fun_new_app),
                onClick = { openAddFunctionDialog = true })
        } else {


            FlowDemoFunctionsConfigurationList(
                functions = functions,
                onConfig = { function ->
                    viewModel.functionOnConfig = function.copy()
                    flow!!.sensors = inputs
                    flow!!.functions = functions
                    flow!!.outputs = outputs
                    viewModel.flowOnCreation = flow
                    navController.navigate(
                        DestinationFlowDemoFunctionConfigurationScreen.route
                    )
                },
                onDelete = {
                    val tmp = functions.toMutableList()
                    tmp.removeIf { function -> function.id == it }
                    functions = tmp.toList()
                }
            )
            ClickableTest(
                text = stringResource(id = R.string.add_fun_new_app),
                onClick = { openAddFunctionDialog = true })
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
                text = stringResource(id = R.string.app_output),
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

        if (outputs.isEmpty()) {
            ClickableTest(
                text = stringResource(id = R.string.choose_out_new_app),
                onClick = { openAddOutputDialog = true })
        } else {
            FlowDemoOutputsConfigurationList(outputs = outputs, onConfig = { output ->
                viewModel.outputOnConfig = output.copy()
                flow!!.sensors = inputs
                flow!!.functions = functions
                flow!!.outputs = outputs
                viewModel.flowOnCreation = flow
                navController.navigate(
                    DestinationFlowDemoOutputConfigurationScreen.route
                )
            })

            ClickableTest(
                text = stringResource(id = R.string.change_out_new_app),
                onClick = { openAddOutputDialog = true })
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
                    openConfirmationDialog = true

                }
            )

            Spacer(modifier = Modifier.weight(1f))

            BlueMsButton(
                text = stringResource(id = R.string.save_new_app),
                iconPainter = painterResource(id = R.drawable.ic_done),
                enabled = outputs.isNotEmpty(),
                onClick = {
                    if (outputs.isNotEmpty()) {
                        //Save the Flow on the Disk
                        flow?.let {
                            flow!!.sensors = inputs
                            flow!!.functions = functions
                            flow!!.outputs = outputs
                        }
                        flow?.let {
                            //Create new Flow Id
                            flow!!.generateId()
                        }
                        viewModel.flowSelected = flow
                        navController.navigate(
                            DestinationFlowDemoFlowSaveScreen.route
                        )
//                    } else {
//                        Toast.makeText(context, "Flow Could not be saved", Toast.LENGTH_SHORT)
//                            .show()
                    }
                }
            )
        }

        if (openAddInputDialog) {
            flow?.let {
                flow!!.sensors = inputs
                flow!!.functions = functions
                flow!!.outputs = outputs
            }
            viewModel.flowOnCreation = flow?.copy()
            FlowDemoAddInputDialog(viewModel = viewModel,
                onDismissRequest = { openAddInputDialog = false })
        }

        if (openAddFunctionDialog) {
            flow?.let {
                flow!!.sensors = inputs
                flow!!.functions = functions
                flow!!.outputs = outputs
            }
            viewModel.flowOnCreation = flow?.copy()
            FlowDemoAddFunctionDialog(viewModel = viewModel,
                onDismissRequest = { openAddFunctionDialog = false })
        }

        if (openAddOutputDialog) {
            flow?.let {
                flow!!.sensors = inputs
                flow!!.functions = functions
                flow!!.outputs = outputs
            }
            viewModel.flowOnCreation = flow?.copy()
            FlowDemoAddOutputDialog(viewModel = viewModel,
                onDismissRequest = { openAddOutputDialog = false })
        }

        if (openConfirmationDialog) {
            FlowDemoAlertDialog(
                title = stringResource(id = context.applicationInfo.labelRes),
                message = "Losing all changes.\r\tContinue?",
                onDismiss = { openConfirmationDialog = false },
                onConfirmation = { navController.popBackStack() }
            )
        }

    }
}

@Composable
fun FlowDemoFunctionsConfigurationList(
    functions: List<Function>,
    onConfig: (Function) -> Unit = { /** NOOP**/ },
    onDelete: (String) -> Unit = { /** NOOP **/ }
) {
    functions.forEach { function ->
        FlowDemoInputFunctionOutputConfigurationListItem(
            iconId = getFunctionIconResourceByName("ic_function"),
            label = function.description,
            hasSettings = function.hasSettings,
            couldBeDeleted = true,
            onConfig = { onConfig(function) },
            onDelete = { onDelete(function.id) }
        )
    }
}

@Composable
fun FlowDemoInputsConfigurationList(
    sensors: List<Sensor>,
    onConfig: (Sensor) -> Unit = { /** NOOP**/ }
) {
    sensors.forEach { sensor ->
        FlowDemoInputFunctionOutputConfigurationListItem(
            iconId = getSensorIconResourceByName(sensor.icon),
            label = sensor.description,
            hasSettings = sensor.hasSettings(),
            onConfig = { onConfig(sensor) },
        )
    }
}

@Composable
fun FlowDemoInputsFlowConfigurationList(
    flows: List<Flow>,
) {
    flows.forEach { flow ->
        FlowDemoInputFunctionOutputConfigurationListItem(
            iconId =
            if (isAlsoExp(flow)) {
                getOutputIconResourceByName("ic_expr")
            } else {
                if (flow.outputs.size > 1) {
                    getOutputIconResourceByName("ic_multi")
                } else {
                    getOutputIconResourceByName(flow.outputs[0].icon)
                }
            },
            label = flow.description,
            hasSettings = false,
        )
    }
}


@Composable
fun FlowDemoOutputsConfigurationList(
    outputs: List<Output>,
    onConfig: (Output) -> Unit = { /** NOOP**/ }
) {
    outputs.forEach { output ->
        FlowDemoInputFunctionOutputConfigurationListItem(
            iconId = getOutputIconResourceByName(output.icon),
            label = output.description,
            hasSettings = output.hasSettings,
            onConfig = { onConfig(output) }
        )
    }
}