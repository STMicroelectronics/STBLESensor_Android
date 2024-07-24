package com.st.cloud_azure_iot_central.composable

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.cloud_azure_iot_central.CloudAzureNavigationApplicationDetails
import com.st.cloud_azure_iot_central.CloudAzureNavigationApplicationSelection
import com.st.cloud_azure_iot_central.CloudAzureNavigationDeviceSelection
import com.st.cloud_azure_iot_central.CloudAzureNavigationDeviceConnection
import com.st.cloud_azure_iot_central.R
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey6
import com.st.ui.theme.PrimaryBlue2

@Composable
fun CloudAzureIotCentralStartScreen(
    modifier: Modifier,
    viewModel: CloudAzureIotCentralViewModel,
    navController: NavHostController = rememberNavController()
) {
    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    val isCloudAppSelected by viewModel.selectedCloudAppNum.collectAsStateWithLifecycle()

    val isCloudDeviceConfigured by viewModel.isCloudDeviceConfigured.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier,
        topBar = {
            CloudAzureConfig.CloudTabBar?.invoke("Cloud Application")
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
                        indicatorColor = PrimaryBlue2,
                        disabledIconColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.primary
                    ),
                    selected = 0 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 0
                        navController.navigate(CloudAzureNavigationApplicationSelection.route) {
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
                            painter = painterResource(id = R.drawable.cloud_app_config),
                            contentDescription = stringResource(id = R.string.navigation_tab_cloud_config)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.navigation_tab_cloud_config)) })

                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Grey0,
                        selectedTextColor = Grey0,
                        unselectedIconColor = Grey6,
                        unselectedTextColor = Grey6,
                        indicatorColor = PrimaryBlue2,
                        disabledIconColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = (isCloudAppSelected != viewModel.deviceCloutNotSELECTED),
                    selected = 1 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 1
                        navController.navigate(CloudAzureNavigationDeviceSelection.route) {
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
                            painter = painterResource(id = R.drawable.cloud_config),
                            contentDescription = stringResource(id = R.string.navigation_tab_device_config)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.navigation_tab_device_config)) })

                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Grey0,
                        selectedTextColor = Grey0,
                        unselectedIconColor = Grey6,
                        unselectedTextColor = Grey6,
                        indicatorColor = PrimaryBlue2,
                        disabledIconColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = isCloudDeviceConfigured,
                    selected = 2 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 2
                        navController.navigate(CloudAzureNavigationDeviceConnection.route) {
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
                            painter = painterResource(id = R.drawable.cloud_dev_upload),
                            contentDescription = stringResource(id = R.string.navigation_tab_device_connection)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.navigation_tab_device_connection)) })

            }
        }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = CloudAzureNavigationApplicationSelection.route
            ) {
                composable(route = CloudAzureNavigationApplicationSelection.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(500)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(500)
                        )
                    }) {
                    selectedIndex = 0
                    CloudAzureApplicationSelection(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(route = CloudAzureNavigationDeviceSelection.route,
                    enterTransition = {
                        if (initialState.destination.route == CloudAzureNavigationApplicationSelection.route) {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(500)
                            )
                        } else {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500)
                            )
                        }
                    },
                    exitTransition = {
                        if (targetState.destination.route == CloudAzureNavigationApplicationSelection.route) {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500)
                            )
                        } else {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(500)
                            )
                        }
                    }) {
                    selectedIndex = 1
                    CloudAzureDeviceSelection(
                        viewModel = viewModel
                    )
                }

                composable(route = CloudAzureNavigationDeviceConnection.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(500)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(500)
                        )
                    }) {
                    selectedIndex = 2
                    CloudAzureDeviceConnection(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = CloudAzureNavigationApplicationDetails.route + "{${CloudAzureNavigationApplicationDetails.appId}}",
                    arguments = listOf(navArgument(name = "appId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(500)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(500)
                        )
                    }
                ) { backStackEntry ->
                    backStackEntry.arguments?.getString("appId")?.let { appId ->
                        CloudAzureApplicationDetails(
                            viewModel = viewModel,
                            appId = appId.toInt(),
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
