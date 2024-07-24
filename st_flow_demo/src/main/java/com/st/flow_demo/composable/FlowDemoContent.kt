package com.st.flow_demo.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue2

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
        modifier = modifier,
        topBar = {
            FlowConfig.FlowTabBar?.invoke("Flow creation")
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Grey0
            ) {
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Grey0,
                        selectedTextColor = Grey0,
                        unselectedIconColor = Grey6,
                        unselectedTextColor = Grey6,
                        indicatorColor = PrimaryBlue2
                    ),
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
                    label = { Text(text = stringResource(id = R.string.navigation_tab_flows)) })

                if (viewModel.isPnPLExported()) {
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Grey0,
                            selectedTextColor = Grey0,
                            unselectedIconColor = Grey6,
                            unselectedTextColor = Grey6,
                            indicatorColor = PrimaryBlue2
                        ),
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
                        label = {
                            Text(
                                text = viewModel.getRunningFlowFromOptionBytes()
                                    ?: stringResource(id = R.string.navigation_tab_control)
                            )
                        })
                }


                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Grey0,
                        selectedTextColor = Grey0,
                        unselectedIconColor = Grey6,
                        unselectedTextColor = Grey6,
                        indicatorColor = PrimaryBlue2
                    ),
                    selected = 2 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 2
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
                    label = { Text(text = stringResource(id = R.string.navigation_tab_sensors)) })


                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Grey0,
                        selectedTextColor = Grey0,
                        unselectedIconColor = Grey6,
                        unselectedTextColor = Grey6,
                        indicatorColor = PrimaryBlue2
                    ),
                    selected = 3 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 3
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
                    label = { Text(text = stringResource(id = R.string.navigation_tab_more)) })


            }
        }
    ) { paddingValues ->
        Box {
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = DestinationFlowDemoFlowCategoriesExampleScreen.route
            ) {
                composable(
                    route = DestinationFlowDemoSensorsScree.route
                ) {
                    FlowDemoSensorsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoPnPLControlScreen.route
                ) {
                    FlowDemoPnPLControlScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowCategoriesExampleScreen.route
                ) {
                    FlowDemoFlowCategoriesExampleScreen(
                        viewModel = viewModel,
                        navController = navController,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowsExpertScreen.route
                ) {
                    FlowDemoFlowsExpertScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoMoreInfoScreen.route
                ) {
                    FlowDemoMoreInfoScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
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
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal)
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowDetailScreen.route
                ) {
                    FlowDemoFlowDetailScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowIfApplicationCreationScreen.route
                ) {
                    FlowDemoFlowIfApplicationCreationScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFlowSaveScreen.route
                ) {
                    FlowDemoFlowSaveScreen(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoSensorConfigurationScreen.route
                ) {
                    FlowDemoSensorConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoFunctionConfigurationScreen.route
                ) {
                    FlowDemoFunctionConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = DestinationFlowDemoOutputConfigurationScreen.route
                ) {
                    FlowDemoOutputConfigurationScreen(
                        paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
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
                            paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
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
                            paddingValues = PaddingValues(all = LocalDimensions.current.paddingNormal),
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}