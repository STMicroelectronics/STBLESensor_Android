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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.helpers.findOutputById
import com.st.flow_demo.helpers.getAvailableOutputs
import com.st.flow_demo.helpers.hasOutputAmbiguousInputs
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoAddOutputDialog(
    viewModel: FlowDemoViewModel,
    onDismissRequest: () -> Unit
) {

    val flowOnCreation = viewModel.flowOnCreation
    val availableOutputs by viewModel.availableOutputs.collectAsStateWithLifecycle()

    var errorText by remember { mutableStateOf<String?>(value = null) }

    val context = LocalContext.current

    BasicAlertDialog(
        //properties = DialogProperties(usePlatformDefaultWidth = false),
        //onDismiss we don't change the flow
        onDismissRequest = onDismissRequest
    ) {
        if (flowOnCreation != null) {

            val combinedOutputs by remember {
                derivedStateOf {
                    val setList = mutableListOf<Set<String>>()
                    val outputIds = mutableListOf<String>()
                    getAvailableOutputs(flowOnCreation, setList)

                    if (setList.isNotEmpty()) {
                        val out = setList[0].toMutableSet()
                        setList.forEach { set ->
                            out.retainAll(set)
                        }
                        out.forEach { outputIds.add(it) }
                    }

                    val filteredOutput = availableOutputs.toMutableList()
                    for (i in filteredOutput.indices.reversed()) {
                        val output = filteredOutput[i]
                        if (!outputIds.contains(output.id))
                            filteredOutput.remove(output)
                    }

                    val sensorList = flowOnCreation.sensors

                    //Remove Bluetooth output if any sensor have a ODR > BleMaxOdr
                    availableOutputs.forEach { output ->
                        if (output.id == "O3") {
                            sensorList.forEach { sensor ->
                                sensor.configuration?.let {
                                    if ((sensor.configuration!!.odr != null) && (sensor.bleMaxOdr != null)) {
                                        if (sensor.configuration!!.odr!! > sensor.bleMaxOdr!!) {
                                            filteredOutput.remove(output)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    filteredOutput
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            ) {
                Column {
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
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                    ) {
                        items(combinedOutputs) {
                            val currentOutputFlow = findOutputById(flowOnCreation.outputs, it.id)
                            var booleanData = currentOutputFlow != null

                            FlowDemoBooleanProperty(
                                label = it.description,
                                value = booleanData,
                                onValueChange = { value ->
                                    booleanData = value
                                    val tmpList = flowOnCreation.outputs.toMutableList()
                                    if (booleanData) {
                                        if (it.id == "O5") {
                                            //If we are saving one output like Exp... we set the condition
                                            //for switching on LED when condition true
                                            if (it.properties != null) {
                                                it.properties!![0].value = true
                                            }
                                        }
                                        tmpList.add(it)
                                    } else {
                                        tmpList.remove(it)
                                    }
                                    flowOnCreation.outputs = tmpList.toList()

                                    errorText = hasOutputAmbiguousInputs(tmpList, context)
                                })
                        }

                        if (combinedOutputs.isEmpty()) {
                            item {
                                Text(
                                    fontSize = 14.sp,
                                    color = InfoText,
                                    text = "No Available Output"
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
                                BlueMsButton(
                                    text = stringResource(id = R.string.done_message),
                                    enabled = errorText == null,
                                    onClick = {
                                        //save the current Modified Flow
                                        viewModel.flowOnCreation = flowOnCreation.copy()
                                        onDismissRequest()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                fontSize = 16.sp,
                color = ErrorText,
                text = "Error..."
            )
        }
    }
}