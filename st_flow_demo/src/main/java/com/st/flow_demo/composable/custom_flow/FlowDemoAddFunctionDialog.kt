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
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.helpers.filterFunctionsByInputs
import com.st.flow_demo.helpers.filterFunctionsByMandatoryInputs
import com.st.flow_demo.helpers.filterFunctionsByRepeatCount
import com.st.flow_demo.helpers.findFunctionById
import com.st.flow_demo.helpers.getFlowFunctionInputs
import com.st.flow_demo.helpers.getFlowSensorInputs
import com.st.flow_demo.helpers.hasFunctionAmbiguousInputs
import com.st.flow_demo.models.Output
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoAddFunctionDialog(
    viewModel: FlowDemoViewModel,
    onDismissRequest: () -> Unit
) {

    val flowOnCreation = viewModel.flowOnCreation
    val availableFunctions by viewModel.availableFunctions.collectAsStateWithLifecycle()

    var errorText by remember { mutableStateOf<String?>(value = null) }

    AlertDialog(
        //properties = DialogProperties(usePlatformDefaultWidth = false),
        //onDismiss we don't change the flow
        onDismissRequest = onDismissRequest
    ) {
        if (flowOnCreation != null) {

            val combinedInputs by remember {
                derivedStateOf {
                    //val tmpList = flowOnCreation.sensors.map {it.id}.toMutableList()
                    val tmpList = mutableListOf<String>()
                    if (flowOnCreation.functions.isEmpty()) {
                        getFlowSensorInputs(flowOnCreation, tmpList)
                        getFlowFunctionInputs(flowOnCreation, tmpList)
                    } else {
                        val lastFunction = flowOnCreation.functions.last()
                        tmpList.add(lastFunction.id)
                    }
                    tmpList.toList()
                }
            }


            val filterFunctions by remember {
                derivedStateOf {
                    val tmpList = availableFunctions.toMutableList()

                    //Log.i("AddFunction","tmpList=${tmpList.size} ${combinedInputs.size}")
                    filterFunctionsByMandatoryInputs(tmpList, combinedInputs)
                    //Log.i("AddFunction","tmpList=${tmpList.size}")
                    filterFunctionsByInputs(tmpList, combinedInputs)
                    //Log.i("AddFunction","tmpList=${tmpList.size}")
                    filterFunctionsByRepeatCount(tmpList, flowOnCreation)
                    //Log.i("AddFunction","tmpList=${tmpList.size}")
                    tmpList.toList()
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
//                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                    ) {
                        items(filterFunctions) {
                            val currentFunctionFlow =
                                findFunctionById(flowOnCreation.functions, it.id)
                            var booleanData = currentFunctionFlow != null

                            FlowDemoBooleanProperty(
                                label = it.description,
                                value = booleanData,
                                onValueChange = { value ->
                                    booleanData = value
                                    val tmpList = flowOnCreation.functions.toMutableList()
                                    if (booleanData) {
                                        tmpList.add(it)
                                    } else {
                                        tmpList.remove(it)
                                    }
                                    flowOnCreation.functions = tmpList.toList()

                                    errorText = if (hasFunctionAmbiguousInputs(
                                            it,
                                            combinedInputs
                                        )
                                    ) {
                                        "The selected function is incompatible with multiple chosen inputs. Re-check the input section."
                                    } else {
                                        null
                                    }

                                })
                        }

                        if (filterFunctions.isEmpty()) {
                            item {
                                Text(
                                    fontSize = 14.sp,
                                    color = InfoText,
                                    text = "No Available Function"
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
                                if (filterFunctions.isEmpty()) {
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
                                        modifier = Modifier.padding(end= LocalDimensions.current.paddingSmall),
                                        text = "Cancel",
                                        onClick = {
                                            //we don't change the flow
                                            onDismissRequest()
                                        }
                                    )
                                    BlueMsButton(
                                        text = stringResource(id = R.string.done_message),
                                        enabled = errorText == null,
                                        onClick = {
                                            //save the current Modified Flow
                                            //  resetting outputs
                                            val tmp2: MutableList<Output> = flowOnCreation.outputs.toMutableList()
                                            tmp2.clear()
                                            flowOnCreation.outputs = tmp2.toList()
                                            viewModel.flowOnCreation = flowOnCreation
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