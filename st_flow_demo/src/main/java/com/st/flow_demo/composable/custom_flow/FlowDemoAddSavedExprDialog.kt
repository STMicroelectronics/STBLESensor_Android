package com.st.flow_demo.composable.custom_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoRadioButtonGroupEntry
import com.st.flow_demo.helpers.canBeUsedAsExp
import com.st.flow_demo.models.Output
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoAddSavedExprDialog(
    viewModel: FlowDemoViewModel,
    onDismissRequest: () -> Unit
) {

    val availableCustomFlowList by viewModel.flowsCustomList.collectAsStateWithLifecycle()

    val filteredAvailableCustomFlowList =
        availableCustomFlowList.filter { it -> canBeUsedAsExp(it) }


    var expressionSelectedId = if (viewModel.expressionSelected != null) {
        viewModel.expressionSelected!!.id
    } else {
        filteredAvailableCustomFlowList[0].id
    }

    AlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = LocalDimensions.current.cornerMedium)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = LocalDimensions.current.paddingNormal)
                    .verticalScroll(rememberScrollState())
            ) {
                val valuesMap: List<Pair<Int, String>> =
                    filteredAvailableCustomFlowList.mapIndexed { index, flow -> index to flow.description }

                FlowDemoRadioButtonGroupEntry(
                    title = "Choose an expression",
                    values = valuesMap,
                    defaultValue = filteredAvailableCustomFlowList.indexOf(
                        filteredAvailableCustomFlowList.firstOrNull { it.id == expressionSelectedId }),
                    onValueSelected = {
                        expressionSelectedId = filteredAvailableCustomFlowList[it].id
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlueMsButton(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        text = "Cancel",
                        onClick = {
                            //we don't change the flow
                            onDismissRequest()
                        }
                    )
                    BlueMsButton(
                        text = stringResource(id = R.string.done_message),
                        enabled = true,
                        onClick = {
                            //save the selected Expr
                            viewModel.expressionSelected =
                                filteredAvailableCustomFlowList.firstOrNull { it.id == expressionSelectedId }
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}