package com.st.cloud_mqtt.composable

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.st.cloud_mqtt.CloudMqttNavigationApplicationConfiguration
import com.st.cloud_mqtt.CloudMqttNavigationDeviceConnection
import com.st.cloud_mqtt.CloudMqttViewModel
import com.st.cloud_mqtt.R
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey6

@Composable
fun CloudMqttStartScreen(
    modifier: Modifier,
    viewModel: CloudMqttViewModel,
    navController: NavHostController = rememberNavController()
) {

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    val isBrokerConfigured by viewModel.isBrokerConfigured.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier,
        topBar = {
            CloudMqttConfig.CloudTabBar?.invoke("Cloud MQTT")
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = Grey0
            ) {
                BottomNavigationItem(
                    selectedContentColor = Grey0,
                    unselectedContentColor = Grey6,
                    selected = 0 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 0
                        navController.navigate(CloudMqttNavigationApplicationConfiguration.route) {
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
                            painter = painterResource(id = R.drawable.ic_construction_24),
                            contentDescription = stringResource(id = R.string.navigation_tab_cloud_config)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.navigation_tab_cloud_config)) })

                BottomNavigationItem(
                    selectedContentColor = Grey0,
                    unselectedContentColor = if (isBrokerConfigured) Grey6 else MaterialTheme.colorScheme.primary,
                    enabled = isBrokerConfigured,
                    selected = 1 == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIndex = 1
                        navController.navigate(CloudMqttNavigationDeviceConnection.route) {
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
                            painter = painterResource(id = R.drawable.cloud_sync),
                            contentDescription = stringResource(id = R.string.navigation_tab_device_connection)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.navigation_tab_device_connection)) })
            }
        }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = CloudMqttNavigationApplicationConfiguration.route
            ) {
                composable(route = CloudMqttNavigationApplicationConfiguration.route,
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
                    CloudMqttApplicationConfiguration(
                        viewModel = viewModel
                    )
                }


                composable(route = CloudMqttNavigationDeviceConnection.route,
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
                    selectedIndex = 1
                    CloudMqttDeviceConnection(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}