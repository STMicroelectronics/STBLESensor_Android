package com.st.flow_demo.composable.example_flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.DestinationFlowDemoFlowDetailScreen
import com.st.flow_demo.DestinationFlowDemoFlowUploadScreen
import com.st.flow_demo.DestinationFlowDemoFlowsExpertScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFlowCategoryExampleScreen(
    viewModel: FlowDemoViewModel,
    category: String,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    BackHandler {
        navController.popBackStack()
    }

    val flowsExampleList by viewModel.flowsExampleList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            text = "Example"
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Text(
            fontSize = 16.sp,
            color = Grey6,
            text = "$category Apps"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            val appsExampleList = flowsExampleList.filter { it.category == category }
            if (appsExampleList.isNotEmpty()) {
                items(appsExampleList.toList()) { flow ->
                    FlowDemoFlowListItem(flow, onFlowSelected = {
                        viewModel.flowSelected = flow
                        navController.navigate(
                            DestinationFlowDemoFlowDetailScreen.route
                        )
                    }, onPlayFlow = {
                        if ((flow.expression != null) && (flow.statements.isNotEmpty())) {
                            viewModel.flowSelected = flow.statements[0]
                            viewModel.flowSelected!!.ex_app = flow.ex_app
                            viewModel.expressionSelected = flow.expression!!.flows[0]
                            viewModel.expressionSelected!!.ex_app = flow.ex_app
                        } else {
                            viewModel.flowSelected = flow
                            viewModel.expressionSelected = null
                        }
                        viewModel.reportExampleAppAnalytics(viewModel.flowSelected!!)
                        navController.navigate(
                            DestinationFlowDemoFlowUploadScreen.route
                        )
                    })
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(height = LocalDimensions.current.paddingMedium)
                        .weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlueMsButton(
                        text = stringResource(id = R.string.expert_view),
                        onClick = {
                            navController.navigate(
                                DestinationFlowDemoFlowsExpertScreen.route
                            )
                        }
                    )
                }
            }
        }

        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.navigationBars
            )
        )
    }
}