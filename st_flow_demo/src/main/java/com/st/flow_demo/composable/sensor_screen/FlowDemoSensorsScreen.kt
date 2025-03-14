package com.st.flow_demo.composable.sensor_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.DestinationFlowDemoSensorDetailScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoSensorsScreen(
    viewModel: FlowDemoViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.retrieveSensorsAdapter()
            else -> Unit
        }
    }

    val sensorsList by viewModel.sensorsList.collectAsStateWithLifecycle()

    val expansionSensorsList by viewModel.expansionSensorsList.collectAsStateWithLifecycle()

    //val mountedModel = viewModel.getMountedDil24FromOptionBytes()
    val mountedModels = viewModel.getPossibleMountedDil24sFromOptionBytes()

//    val sensorListOrdered =
//        expansionSensorsList.sortedBy { it.model != mountedModel }.sortedBy { it.model }

    val sensorListOrdered =
        expansionSensorsList.sortedBy { it.model }.sortedBy { !mountedModels.contains(it.model) }

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            text = "Sensors"
        )
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Text(
            fontSize = 16.sp,
            color = Grey6,
            text = "Available sensors"
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal
            ),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            if (sensorsList.isNotEmpty()) {
                items(sensorsList) { sensor ->
                    FlowDemoSensorListItem(sensor = sensor,
                        onSensorSelected = {
                            navController.navigate(
                                DestinationFlowDemoSensorDetailScreen.route + sensor.id
                            )
                        }
                    )
                }
            }
            if (sensorListOrdered.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = "Expansion Sensor Boards"
                    )
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

                    Text(
                        fontSize = 16.sp,
                        color = Grey6,
                        text = "Supported DIL24 STEVAL Sensors"
                    )

                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))
                }

                items(sensorListOrdered) { sensor ->
                    FlowDemoSensorListItem(
                        //mounted = sensor.model == mountedModel,
                        mounted = mountedModels.contains(sensor.model),
                        sensor = sensor,
                        onSensorSelected = {
                            navController.navigate(
                                DestinationFlowDemoSensorDetailScreen.route + sensor.id
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