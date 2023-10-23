package com.st.flow_demo.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.flow_demo.FlowConfig
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.common.FlowDemoFlowDetailScreen
import com.st.flow_demo.composable.common.FlowDemoFlowUploadScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoFlowExpertEditingScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoFlowIfApplicationCreationScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoFlowSaveScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoFlowsExpertScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoFunctionConfigurationScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoOutputConfigurationScreen
import com.st.flow_demo.composable.custom_flow.FlowDemoSensorConfigurationScreen
import com.st.flow_demo.composable.example_flow.FlowDemoFlowCategoriesExampleScreen
import com.st.flow_demo.composable.example_flow.FlowDemoFlowCategoryExampleScreen
import com.st.flow_demo.composable.more_info.FlowDemoMoreInfoScreen
import com.st.flow_demo.composable.example_flow.FlowDemoPnPLControlScreen
import com.st.flow_demo.composable.sensor_screen.FlowDemoSensorsScreen
import com.st.flow_demo.composable.sensor_screen.FlowDemoSensorDetailScreen
import com.st.ui.composables.BottomAppBarItem
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoContent(
    modifier: Modifier,
    viewModel: FlowDemoViewModel,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            FlowConfig.FlowTabBar?.invoke("Flow creation")
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = Grey0,
                cutoutShape = CircleShape,
                contentPadding =
                PaddingValues(all = LocalDimensions.current.paddingNormal)
            ) {
                BottomAppBarItem(
                    painter = painterResource(id = R.drawable.ic_flows),
                    label = stringResource(id = R.string.navigation_tab_flows),
                    onClick = {
                        navController.navigate("categoriesExample") {
                            navController.graph.startDestinationRoute?.let { screen_route ->
                                popUpTo(screen_route) {
                                    saveState = false
                                }
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )

                if (viewModel.isPnPLExported()) {
                    Spacer(modifier = Modifier.weight(weight = 0.15f))

                    BottomAppBarItem(
                        painter = painterResource(id = R.drawable.pnpl_icon),
                        label = viewModel.getRunningFlowFromOptionBytes()
                            ?: stringResource(id = R.string.navigation_tab_control),
                        onClick = {
                            navController.navigate("pnpLControl") {
                                navController.graph.startDestinationRoute?.let { screen_route ->
                                    popUpTo(screen_route) {
                                        saveState = false
                                    }
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(weight = 0.15f))

                BottomAppBarItem(
                    painter = painterResource(id = R.drawable.ic_sensor),
                    label = stringResource(id = R.string.navigation_tab_sensors),
                    onClick = {
                        navController.navigate("boardSensors") {
                            navController.graph.startDestinationRoute?.let { screen_route ->
                                popUpTo(screen_route) {
                                    saveState = false
                                }
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(weight = 0.15f))

                BottomAppBarItem(
                    painter = painterResource(id = R.drawable.ic_more),
                    label = stringResource(id = R.string.navigation_tab_more),
                    onClick = {
                        navController.navigate("moreInfo") {
                            navController.graph.startDestinationRoute?.let { screen_route ->
                                popUpTo(screen_route) {
                                    saveState = false
                                }
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box {
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = "categoriesExample",
            ) {
                composable(
                    route = "boardSensors"
                ) {
                    FlowDemoSensorsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "pnpLControl"
                ) {
                    FlowDemoPnPLControlScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "categoriesExample"
                ) {
                    FlowDemoFlowCategoriesExampleScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "flowsExpert"
                ) {
                    FlowDemoFlowsExpertScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = "moreInfo"
                ) {
                    FlowDemoMoreInfoScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "flowUpload"
                ) {
                    FlowDemoFlowUploadScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "flowEditing"
                ) {
                    FlowDemoFlowExpertEditingScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = "flowDetails"
                ) {
                    FlowDemoFlowDetailScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = "flowIfEditing"
                ) {
                    FlowDemoFlowIfApplicationCreationScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = "flowSaving"
                ) {
                    FlowDemoFlowSaveScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = "sensorConfiguration"
                ) {
                    FlowDemoSensorConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = "functionConfiguration"
                ) {
                    FlowDemoFunctionConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = "outputConfiguration"
                ) {
                    FlowDemoOutputConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = "detail/{sensorId}/sensor",
                    arguments = listOf(navArgument(name = "sensorId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("sensorId")?.let { sensorId ->
                        FlowDemoSensorDetailScreen(
                            viewModel = viewModel,
                            sensorId = sensorId,
                            paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                            navController = navController
                        )
                    }
                }

                composable(
                    route = "detail/{categoryType}/category",
                    arguments = listOf(navArgument(name = "categoryType") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("categoryType")?.let { categoryType ->
                        FlowDemoFlowCategoryExampleScreen(
                            viewModel = viewModel,
                            category = categoryType,
                            paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}