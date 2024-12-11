package com.st.flow_demo.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.flow_demo.DestinationFlowDemoSensorsScree
import com.st.flow_demo.DestinationFlowDemoFlowCategoriesExampleScreen
import com.st.flow_demo.DestinationFlowDemoFlowCategoryExampleScreen
import com.st.flow_demo.DestinationFlowDemoFlowDetailScreen
import com.st.flow_demo.DestinationFlowDemoFlowExpertEditingScreen
import com.st.flow_demo.DestinationFlowDemoFlowIfApplicationCreationScreen
import com.st.flow_demo.DestinationFlowDemoFlowSaveScreen
import com.st.flow_demo.DestinationFlowDemoFlowUploadScreen
import com.st.flow_demo.DestinationFlowDemoFlowsExpertScreen
import com.st.flow_demo.DestinationFlowDemoFunctionConfigurationScreen
import com.st.flow_demo.DestinationFlowDemoMoreInfoScreen
import com.st.flow_demo.DestinationFlowDemoOutputConfigurationScreen
import com.st.flow_demo.DestinationFlowDemoPnPLControlScreen
import com.st.flow_demo.DestinationFlowDemoSensorConfigurationScreen
import com.st.flow_demo.DestinationFlowDemoSensorDetailScreen
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
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoContent(
    modifier: Modifier,
    viewModel: FlowDemoViewModel,
    navController: NavHostController = rememberNavController()
) {
    var selectedIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    val haptic = LocalHapticFeedback.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            if(FlowConfig.FlowTabBar!=null) {
                FlowConfig.FlowTabBar?.invoke("Flow creation")
            } else {
                PrimaryTabRow(modifier = Modifier
                    .fillMaxWidth(),
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = selectedIndex,
                                matchContentSize = false
                            ),
                            width = 60.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            height = 4.dp,
                            shape = RoundedCornerShape(size = LocalDimensions.current.cornerMedium)
                        )
                    }) {
                    Tab(
                        selected = 0 == selectedIndex,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedIndex = 0
                            navController.navigate(DestinationFlowDemoFlowCategoriesExampleScreen.route) {
                                navController.graph.startDestinationRoute?.let { screenRoute ->
                                    popUpTo(screenRoute) {
                                        saveState = false
                                    }
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_flows),
                                contentDescription = stringResource(id = R.string.navigation_tab_flows)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.navigation_tab_flows)) },
                    )

                    if (viewModel.isPnPLExported()) {
                        Tab(
                            selected = 1 == selectedIndex,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedIndex = 1
                                navController.navigate(DestinationFlowDemoPnPLControlScreen.route) {
                                    navController.graph.startDestinationRoute?.let { screenRoute ->
                                        popUpTo(screenRoute) {
                                            saveState = false
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.pnpl_icon),
                                        contentDescription = "Control"
                                    )
                            },
                            text = { Text(text = viewModel.getRunningFlowFromOptionBytes()
                                ?: stringResource(id = R.string.navigation_tab_control)) },
                        )
                    }


                    Tab(
                        selected = if(viewModel.isPnPLExported()) {
                            2 == selectedIndex } else {1 == selectedIndex},
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedIndex = if(viewModel.isPnPLExported())
                                2
                            else
                                1
                            navController.navigate(DestinationFlowDemoSensorsScree.route) {
                                navController.graph.startDestinationRoute?.let { screenRoute ->
                                    popUpTo(screenRoute) {
                                        saveState = false
                                    }
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sensor),
                                contentDescription = stringResource(id = R.string.navigation_tab_sensors)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.navigation_tab_sensors)) },
                    )

                    Tab(
                        selected =  if(viewModel.isPnPLExported()) {
                            3 == selectedIndex } else {2 == selectedIndex},
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedIndex = if(viewModel.isPnPLExported())
                                3
                            else
                                2
                            navController.navigate(DestinationFlowDemoMoreInfoScreen.route) {
                                navController.graph.startDestinationRoute?.let { screenRoute ->
                                    popUpTo(screenRoute) {
                                        saveState = false
                                    }
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more),
                                contentDescription = stringResource(id = R.string.navigation_tab_more)
                            )
                        },
                        text = { Text(text =  stringResource(id = R.string.navigation_tab_more)) },
                    )
                }
            }
        }
    ) { paddingValues ->
        Box {
            NavHost(
                modifier = Modifier.consumeWindowInsets(paddingValues).padding(paddingValues),
                navController = navController,
                startDestination = DestinationFlowDemoFlowCategoriesExampleScreen.route
            ) {
                composable(
                    route = DestinationFlowDemoSensorsScree.route
                ) {
                    FlowDemoSensorsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoPnPLControlScreen.route
                ) {
                    FlowDemoPnPLControlScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowCategoriesExampleScreen.route
                ) {
                    FlowDemoFlowCategoriesExampleScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowsExpertScreen.route
                ) {
                    FlowDemoFlowsExpertScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoMoreInfoScreen.route
                ) {
                    FlowDemoMoreInfoScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowUploadScreen.route
                ) {
                    FlowDemoFlowUploadScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowExpertEditingScreen.route
                ) {
                    FlowDemoFlowExpertEditingScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowDetailScreen.route
                ) {
                    FlowDemoFlowDetailScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowIfApplicationCreationScreen.route
                ) {
                    FlowDemoFlowIfApplicationCreationScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowSaveScreen.route
                ) {
                    FlowDemoFlowSaveScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoSensorConfigurationScreen.route
                ) {
                    FlowDemoSensorConfigurationScreen(
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFunctionConfigurationScreen.route
                ) {
                    FlowDemoFunctionConfigurationScreen(
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoOutputConfigurationScreen.route
                ) {
                    FlowDemoOutputConfigurationScreen(
                        paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoSensorDetailScreen.route + "{${DestinationFlowDemoSensorDetailScreen.sensorId}}",
                    arguments = listOf(navArgument(name = "sensorId") { type = NavType.StringType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("sensorId")?.let { sensorId ->
                        FlowDemoSensorDetailScreen(
                            viewModel = viewModel,
                            sensorId = sensorId,
                            paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                            navController = navController
                        )
                    }
                }

                composable(
                    route = DestinationFlowDemoFlowCategoryExampleScreen.route + "{${DestinationFlowDemoFlowCategoryExampleScreen.categoryType}}",
                    arguments = listOf(navArgument(name = "categoryType") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("categoryType")?.let { categoryType ->
                        FlowDemoFlowCategoryExampleScreen(
                            viewModel = viewModel,
                            category = categoryType,
                            paddingValues = PaddingValues(start = LocalDimensions.current.paddingNormal, end = LocalDimensions.current.paddingNormal, top = LocalDimensions.current.paddingNormal),
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}