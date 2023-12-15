package com.st.flow_demo.composable.common

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.ui.composables.BlueMsButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import com.st.flow_demo.DestinationFlowDemoFlowExpertEditingScreen
import com.st.flow_demo.DestinationFlowDemoFlowUploadScreen
import com.st.flow_demo.helpers.canBeUsedAsExp
import com.st.flow_demo.helpers.getFunctionIconResourceByName
import com.st.flow_demo.helpers.getOutputIconResourceByName
import com.st.flow_demo.helpers.getSensorIconResourceByName
import com.st.flow_demo.helpers.isAlsoExp
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoFlowDetailScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    BackHandler {
        navController.popBackStack()
    }

    val flow = viewModel.flowSelected

    if (flow != null) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                text = flow.description
            )
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Grey6,
                text = "Description"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            if (flow.notes != null) {
                Text(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.15.sp,
                    text = flow.notes!!.trim()
                )
            } else {
                Text(
                    fontSize = 12.sp,
                    text = stringResource(id = R.string.no_description),
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = LocalDimensions.current.paddingSmall)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = LocalDimensions.current.paddingNormal),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        BlueMsButton(
                            enabled = flow.expression == null,
                            text = stringResource(id = R.string.edit_flow),
                            iconPainter = painterResource(id = R.drawable.ic_edit),
                            onClick = {
                                viewModel.flowSelected = flow
                                viewModel.flowOnCreation = flow.copy()
                                viewModel.resetSavedFlowState()
                                navController.navigate(
                                    DestinationFlowDemoFlowExpertEditingScreen.route
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

                        BlueMsButton(
                            text = stringResource(id = R.string.play_flow),
                            enabled = (!canBeUsedAsExp(flow)) && (!flow.canBeUsedAsInput()),
                            iconPainter = painterResource(id = R.drawable.ic_upload),
                            onClick = {
                                if ((flow.expression != null) && (flow.statements.isNotEmpty())) {
                                    viewModel.flowSelected = flow.statements[0]
                                    viewModel.expressionSelected = flow.expression!!.flows[0]
                                } else {
                                    viewModel.flowSelected = flow
                                    viewModel.expressionSelected = null
                                }
                                navController.navigate(
                                    DestinationFlowDemoFlowUploadScreen.route
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

                    Text(
                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        text = stringResource(id = R.string.app_overview)
                    )

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

                    flow.sensors.forEach { sensor ->
                        FlowDemoInputFunctionOutputListItem(
                            iconId = getSensorIconResourceByName(sensor.icon),
                            label = sensor.description
                        )
                    }

                    if ((flow.expression != null) && (flow.statements.isNotEmpty())) {
                        FlowDemoInputFunctionOutputListItem(
                            iconId = getOutputIconResourceByName("ic_expr"),
                            label = flow.expression!!.flows[0].description
                        )
                    }

                    flow.flows.forEach { flow ->
                        FlowDemoInputFunctionOutputListItem(
                            iconId = if (isAlsoExp(flow)) {
                                //this in theory should not be present for Custom Flow
                                getOutputIconResourceByName("ic_expr")
                            } else {
                                if (flow.outputs.size > 1) {
                                    getOutputIconResourceByName("ic_multi")
                                } else {
                                    getOutputIconResourceByName(flow.outputs[0].icon)
                                }
                            },
                            label = flow.description
                        )
                    }

                    if (flow.functions.isNotEmpty()) {
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

                        flow.functions.forEach { function ->
                            FlowDemoInputFunctionOutputListItem(
                                iconId = getFunctionIconResourceByName("ic_function"),
                                label = function.description
                            )
                        }

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

                    flow.outputs.forEach { output ->
                        FlowDemoInputFunctionOutputListItem(
                            iconId = getOutputIconResourceByName(output.icon),
                            label = output.description
                        )
                    }
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
                }
            }
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
        }
    }
}