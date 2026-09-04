package com.st.high_speed_data_log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.st.high_speed_data_log.composable.AIoTCraftHsdlSensors
import com.st.high_speed_data_log.composable.AIoTCraftHsdlTags
import com.st.high_speed_data_log.composable.VespucciCharts
import com.st.high_speed_data_log.composable.VespucciHsdlTags
import com.st.high_speed_data_log.model.StreamData
import com.st.ui.composables.CommandRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data object AIoTHsdlSensorsNavKey : NavKey

@Serializable
data object AIoTHsdlTagsNavKey : NavKey


@Composable
fun EntryProviderScope<NavKey>.AIoTCraftHsdlTagsScreen(
    lazyState: LazyListState,
    tags: List<ComponentWithInterface>,
    status: List<JsonObject>,
    isLoading: Boolean,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    acquisitionName: String,
    vespucciTagsActivation: List<String>,
    vespucciTags: Map<String, Boolean>,
    isLogging: Boolean,
    onTagChangeState: (String, Boolean) -> Unit
) {
    entry<AIoTHsdlTagsNavKey> {
        if (HsdlConfig.isVespucci.not()) {
            AIoTCraftHsdlTags(
                state = lazyState,
                tags = tags,
                status = status,
                isLoading = isLoading,
                onValueChange = onValueChange,
                onSendCommand = onSendCommand,
            )
        } else {
            VespucciHsdlTags(
                acquisitionInfo = acquisitionName.formatDate(),
                vespucciTagsActivation = vespucciTagsActivation,
                vespucciTags = vespucciTags,
                isLoading = isLoading,
                isLogging = isLogging,
                onTagChangeState = onTagChangeState
            )
        }
    }
}

@Composable
fun EntryProviderScope<NavKey>.AIoTCraftHsdlSensorsOrChartScreen(
    isLogging: Boolean,
    sensors: List<ComponentWithInterface>,
    lazyState: LazyListState,
    status: List<JsonObject>,
    isLoading: Boolean,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onAfterUcf: () -> Unit,
    onBeforeUcf: () -> Unit,
    onErrorUcf: (String) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    streamSensors: List<ComponentWithInterface>,
    streamData: StreamData?,
    currentSensorEnabled: String,
    vespucciTags: Map<String, Boolean>,
    onSensorSelected: (String) -> Unit
) {
    entry<AIoTHsdlSensorsNavKey> {
        if (isLogging.not()) {
            AIoTCraftHsdlSensors(
                sensors = sensors,
                state = lazyState,
                status = status,
                isLoading = isLoading,
                onValueChange = onValueChange,
                onAfterUcf = onAfterUcf,
                onBeforeUcf = onBeforeUcf,
                onErrorUcf = onErrorUcf,
                onSendCommand = onSendCommand,
            )
        } else {
            if (HsdlConfig.isVespucci.not()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.st_hsdl_logging))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                VespucciCharts(
                    sensors = streamSensors,
                    status = status,
                    streamData = streamData,
                    currentSensorEnabled = currentSensorEnabled,
                    vespucciTags = vespucciTags,
                    onSensorSelected = onSensorSelected,
                    showTagsEnabled = true
                )
            }
        }
    }
}