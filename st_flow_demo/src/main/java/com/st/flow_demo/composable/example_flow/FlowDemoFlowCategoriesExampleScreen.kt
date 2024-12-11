package com.st.flow_demo.composable.example_flow

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
import com.st.flow_demo.DestinationFlowDemoFlowCategoryExampleScreen
import com.st.flow_demo.DestinationFlowDemoFlowsExpertScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFlowCategoriesExampleScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    //navController.popBackStack(route = "categoriesExample", inclusive = false)
    val flowsExampleList by viewModel.flowsExampleList.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            text = "Examples"
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Text(
            fontSize = 16.sp,
            color = Grey6,
            text = "Apps Categories"
        )

        val categoriesList: MutableSet<String> = mutableSetOf()
        flowsExampleList.forEach { if (it.category != null) categoriesList.add(it.category!!) }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = LocalDimensions.current.paddingNormal,
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal
            ),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            if (flowsExampleList.isNotEmpty()) {
                items(categoriesList.toList()) { category ->
                    FlowDemoCategoryListItem(category = category, onCategorySelected = {
                        navController.navigate(
                            DestinationFlowDemoFlowCategoryExampleScreen.route + category
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

            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBars
                    )
                )
            }
        }
    }
}