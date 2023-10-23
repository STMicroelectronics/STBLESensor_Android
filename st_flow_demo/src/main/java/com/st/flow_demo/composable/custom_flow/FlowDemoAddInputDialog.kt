package com.st.flow_demo.composable.custom_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.models.Function
import com.st.flow_demo.models.Output
import com.st.flow_demo.helpers.bothMLCAndFSMArePresent
import com.st.flow_demo.helpers.canBeUsedAsExp
import com.st.flow_demo.helpers.findFlowById
import com.st.flow_demo.helpers.findSensorById
import com.st.flow_demo.helpers.isAlsoExp
import com.st.flow_demo.helpers.multipleAccelerometerAreSelected
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoAddInputDialog(
    viewModel: FlowDemoViewModel,
    onDismissRequest: () -> Unit
) {

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.retrieveSensorsAdapter()
            else -> Unit
        }
    }

    val flowOnCreation = viewModel.flowOnCreation
    val availableInput by viewModel.sensorsList.collectAsStateWithLifecycle()

    val availableExpr by viewModel.availableExpFlow.collectAsStateWithLifecycle()
    val availableCustomInput by viewModel.flowsCustomList.collectAsStateWithLifecycle()

    val availableExpansionSensor  by viewModel.expansionSensorsList.collectAsStateWithLifecycle()
    val availableExpansionSensorFilter = availableExpansionSensor.filter { it.model== viewModel.getMountedDil24FromOptionBytes()}


    var errorText by remember { mutableStateOf<String?>(value = null) }

    val availableCustomInputFilter = availableCustomInput.filter { it.canBeUsedAsInput() }
    val availableExprFilter =
        availableExpr.filter { it.canBeUsedAsInput() } + availableCustomInput.filter {
            canBeUsedAsExp(it)
        }

    AlertDialog(
        // properties = DialogProperties(usePlatformDefaultWidth = false),
        //onDismiss we don't change the flow
        onDismissRequest = onDismissRequest
    ) {
        if (flowOnCreation != null) {

            var numberOfExpSelected by remember {
                mutableStateOf(flowOnCreation.flows.filter { it ->
                    isAlsoExp(
                        it
                    )
                }.size)
            }
            var numberOfSensorSelected by remember(numberOfExpSelected) {
                mutableStateOf(
                    flowOnCreation.sensors.size + flowOnCreation.flows.size - numberOfExpSelected
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(size = LocalDimensions.current.cornerMedium)
            ) {
                Column() {
                    if (errorText != null) {
                        Text(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ErrorText,
                            text = errorText!!
                        )
                    }
                    LazyColumn(
                        //modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                    ) {

                        item {
                            Text(
                                modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                text = "Sensors"
                            )
                        }

                        items(availableInput) { it ->
                            val currentSensorFlow = findSensorById(flowOnCreation.sensors, it.id)
                            var booleanData = currentSensorFlow != null

                            FlowDemoBooleanProperty(
                                label = it.description,
                                value = booleanData,
                                onValueChange = { value ->
                                    booleanData = value
                                    val tmpList = flowOnCreation.sensors.toMutableList()
                                    if (booleanData) {
                                        numberOfSensorSelected++
                                        tmpList.add(it)
                                    } else {
                                        numberOfSensorSelected--
                                        tmpList.remove(it)
                                    }
                                    flowOnCreation.sensors = tmpList.toList()

                                    errorText = if (multipleAccelerometerAreSelected(tmpList)) {
                                        "Multiple Accelerometers are Selected"
                                    } else if (bothMLCAndFSMArePresent(tmpList)) {
                                        "MLC Virtual Sensor and FSM Virtual Sensor cannot be selected together"
                                    } else {
                                        if ((numberOfSensorSelected > 0) && (numberOfExpSelected > 0)) {
                                            "Input Sensor and Exp could not be selected together"
                                        } else {
                                            null
                                        }
                                    }
                                })
                        }

                        if(availableExpansionSensorFilter.isNotEmpty()) {
                            item {
                                Text(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    text = "Expansion DIL24"
                                )
                            }

                            items(availableExpansionSensorFilter) { it ->
                                val currentSensorFlow = findSensorById(flowOnCreation.sensors, it.id)
                                var booleanData = currentSensorFlow != null

                                FlowDemoBooleanProperty(
                                    label = it.description,
                                    value = booleanData,
                                    onValueChange = { value ->
                                        booleanData = value
                                        val tmpList = flowOnCreation.sensors.toMutableList()
                                        if (booleanData) {
                                            numberOfSensorSelected++
                                            tmpList.add(it)
                                        } else {
                                            numberOfSensorSelected--
                                            tmpList.remove(it)
                                        }
                                        flowOnCreation.sensors = tmpList.toList()

                                        errorText = if (multipleAccelerometerAreSelected(tmpList)) {
                                            "Multiple Accelerometers are Selected"
                                        } else if (bothMLCAndFSMArePresent(tmpList)) {
                                            "MLC Virtual Sensor and FSM Virtual Sensor cannot be selected together"
                                        } else {
                                            if ((numberOfSensorSelected > 0) && (numberOfExpSelected > 0)) {
                                                "Input Sensor and Exp could not be selected together"
                                            } else {
                                                null
                                            }
                                        }
                                    })
                            }

                        }

                        if(availableExprFilter.isNotEmpty()) {

                            item {
                                Text(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    text = "Exps"
                                )
                            }

                            items(availableExprFilter) {
                                val currentFloFlow = findFlowById(flowOnCreation.flows, it.id)
                                var booleanData = currentFloFlow != null

                                FlowDemoBooleanProperty(
                                    label = it.description,
                                    value = booleanData,
                                    onValueChange = { value ->
                                        booleanData = value
                                        val tmpList = flowOnCreation.flows.toMutableList()
                                        if (booleanData) {
                                            numberOfExpSelected++
                                            tmpList.add(it)
                                        } else {
                                            numberOfExpSelected--
                                            tmpList.remove(it)
                                        }
                                        flowOnCreation.flows = tmpList.toList()

                                        errorText =
                                            if ((numberOfSensorSelected > 0) && (numberOfExpSelected > 0)) {
                                                "Input Sensor (or App as Input) and Exp could not be selected together"
                                            } else {
                                                null
                                            }
                                    })
                            }
                        }

                        if(availableCustomInputFilter.isNotEmpty()) {
                            item {
                                Text(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    text = "App as Input"
                                )
                            }

                            items(availableCustomInputFilter) {
                                val currentFloFlow = findFlowById(flowOnCreation.flows, it.id)
                                var booleanData = currentFloFlow != null

                                FlowDemoBooleanProperty(
                                    label = it.description,
                                    value = booleanData,
                                    onValueChange = { value ->
                                        booleanData = value
                                        val tmpList = flowOnCreation.flows.toMutableList()
                                        if (booleanData) {
                                            numberOfSensorSelected++
                                            tmpList.add(it)
                                        } else {
                                            numberOfSensorSelected--
                                            tmpList.remove(it)
                                        }
                                        flowOnCreation.flows = tmpList.toList()

                                        errorText =
                                            if ((numberOfSensorSelected > 0) && (numberOfExpSelected > 0)) {
                                                "App as Input Sensor and Exp could not be selected together"
                                            } else {
                                                null
                                            }
                                    })
                            }
                        }

                        if (availableInput.isEmpty() && availableExprFilter.isEmpty() && availableCustomInputFilter.isEmpty() && availableExpansionSensorFilter.isEmpty()) {
                            item {
                                Text(
                                    fontSize = 14.sp,
                                    color = InfoText,
                                    text = "No Available Input"
                                )
                            }
                        }

                        item {
                            Spacer(
                                modifier = Modifier
                                    .height(height = LocalDimensions.current.paddingMedium)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (availableInput.isEmpty() && availableExprFilter.isEmpty() && availableCustomInputFilter.isEmpty() && availableExpansionSensorFilter.isEmpty()) {
                                    BlueMsButton(
                                        text = stringResource(id = R.string.done_message),
                                        enabled = errorText == null,
                                        onClick = {
                                            //Do not update currentFlow
                                            onDismissRequest()
                                        }
                                    )
                                } else {
                                    BlueMsButton(
                                        text = stringResource(id = android.R.string.cancel),
                                        enabled = errorText == null,
                                        onClick = {
                                            //Do not update currentFlow
                                            onDismissRequest()
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))

                                    BlueMsButton(
                                        text = stringResource(id = R.string.done_message),
                                        enabled = errorText == null,
                                        onClick = {
                                            //save the current Modified Flow
                                            //  resetting functions and outputs
                                            val tmp1: MutableList<Function> =
                                                flowOnCreation.functions.toMutableList()
                                            tmp1.clear()
                                            flowOnCreation.functions = tmp1.toList()

                                            val tmp2: MutableList<Output> =
                                                flowOnCreation.outputs.toMutableList()
                                            tmp2.clear()
                                            flowOnCreation.outputs = tmp2.toList()
                                            viewModel.flowOnCreation = flowOnCreation.copy()
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
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
}