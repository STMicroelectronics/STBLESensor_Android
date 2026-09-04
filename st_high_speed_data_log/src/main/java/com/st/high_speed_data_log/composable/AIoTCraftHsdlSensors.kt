package com.st.high_speed_data_log.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.high_speed_data_log.ComponentWithInterface
import com.st.high_speed_data_log.HsdlConfig
import com.st.pnpl.composable.Component
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.CommandRequest
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.utils.sensorDisplayName
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.collections.forEach

data class SensorsFilter(
    val sensors: Set<String> = emptySet()
)

data class MapEntry<K, V>(
    override val key: K,
    // Make it var if you need setValue, otherwise val
    private val _value: V
) : Map.Entry<K, V> {

    override val value: V
        get() = _value

//    fun setValue(newValue: V): V {
//        val oldValue = _value
//        _value = newValue
//        return oldValue
//    }
}

val mutualSensorsAIoTCraft = listOf(
    MutualSensor(sensorA = "LSM6DSV80X", sensorB = "LSM6DSV320X")
    //MutualSensor(sensorA = "LPS22DF", sensorB = "LIS2MDL")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIoTCraftHsdlSensors(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sensors: List<ComponentWithInterface> = emptyList(),
    status: List<JsonObject>,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onAfterUcf: () -> Unit,
    onErrorUcf: (String) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    state: LazyListState,
    viewModel: AIoTCraftHsdlSensorsViewModel = viewModel()
) {
    var isOpen by rememberSaveable(sensors) { mutableStateOf(value = "") }

    var filter by remember { mutableStateOf(value = SensorsFilter()) }

    var currentMutualSensor: MutualSensor? by remember { mutableStateOf(null) }

    val dialogAlreadyShown by viewModel.dialogShownOnce.collectAsState()

    val sensorsMounted by remember(sensors, status) {
        derivedStateOf {
            sensors.filter { sensor ->
                val interfaceModel = sensor.second
                val name = sensor.first.name
                val data = (status.find { it.containsKey(name) })?.get(name)

                val isMounted = interfaceModel.contents
                    .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
                    .find { it.name == "mounted" }
                    ?.let { enableProperty ->
                        val defaultData = true
                        var booleanData = true
                        if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                            booleanData =
                                (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                                    ?: defaultData
                        }
                        if (data == null) {
                            booleanData = false
                        }
                        booleanData
                    } ?: true

                isMounted
            }
        }
    }

    val sensorGroups by remember(sensorsMounted, filter.sensors.size) {
        derivedStateOf {
            val groups = sensorsMounted.groupBy { it.first.displayName.sensorDisplayName }
            if (filter.sensors.isEmpty()) {
                //groups
                //This "sortedByDescending{it.second.size}.toMap()" for putting the Combo components at the beginning
                groups.toList().sortedByDescending{it.second.size}.toMap()
            } else {
                //groups.filterKeys { sensorName -> filter.sensors.contains(sensorName).not() }
                //This "sortedByDescending{it.second.size}.toMap()" for putting the Combo components at the beginning
                groups.filterKeys { sensorName -> filter.sensors.contains(sensorName).not() }.toList().sortedByDescending{it.second.size}.toMap()
            }
        }
    }

    val showDialogMutualSensor by remember(sensorGroups) {
        derivedStateOf {
            if (dialogAlreadyShown) {
                return@derivedStateOf false
            }

            var thereAreMutualSensors = false

            val sensorsMounted = mutualSensorsAIoTCraft.filter { mutualSensor ->
                sensorGroups.containsKey(mutualSensor.sensorA) && sensorGroups.containsKey(
                    mutualSensor.sensorB
                )
            }
            if (sensorsMounted.isNotEmpty()) {
                thereAreMutualSensors = true
                currentMutualSensor = mutualSensorsAIoTCraft.firstOrNull { mutualSensor ->
                    !(filter.sensors.contains(mutualSensor.sensorA) || filter.sensors.contains(
                        mutualSensor.sensorB
                    ))
                }
            }

            thereAreMutualSensors
        }
    }

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
    ) {
        sensorGroups.forEach { sensorGroup ->
            if (sensorGroup.value.size > 1) {
                //Combo Component
                //Check if the sensors are mounted or they have received the data
                if (sensorGroup.value.firstOrNull { componentWithInterface ->
                        val name = componentWithInterface.first.name
                        val data = (status.find { it.containsKey(name) })?.get(name)

                        data != null
                    } != null) {
                    item {
                        //combo with multiple sensors active
                        ComboComponent(
                            modifier = modifier.padding(bottom = LocalDimensions.current.paddingMedium),
                            filterMountedSensor = sensorGroup,
                            status = status,
                            isOpen = isOpen == sensorGroup.key,
                            isLoading = isLoading,
                            showNotMounted = false,
                            onValueChange = onValueChange,
                            onBeforeUcf = onBeforeUcf,
                            onAfterUcf = onAfterUcf,
                            onErrorUcf = onErrorUcf,
                            onSendCommand = onSendCommand,
                            onOpenComboComponent = {
                                isOpen = if (it == isOpen) "" else it
                            }
                        )
                    }
                }
            } else {
                item {
                    //Single Component
                    val componentWithInterface = sensorGroup.value.first()
                    val name = componentWithInterface.first.name
                    val data = (status.find { it.containsKey(name) })?.get(name)
                    data?.let {
                        Component(
                            modifier = modifier.padding(bottom = LocalDimensions.current.paddingMedium),
                            name = name,
                            data = data,
                            enabled = isLoading.not(),
                            enableCollapse = true,
                            isOpen = isOpen == name,
                            isVespucci = HsdlConfig.isVespucci,
                            showNotMounted = false,
                            componentModel = componentWithInterface.first,
                            interfaceModel = componentWithInterface.second,
                            onValueChange = { onValueChange(name, it) },
                            onSendCommand = { onSendCommand(name, it) },
                            onBeforeUcf = onBeforeUcf,
                            onAfterUcf = onAfterUcf,
                            onErrorUcf = onErrorUcf,
                            onOpenComponent = {
                                isOpen = if (it == isOpen) "" else it
                            }
                        )
                    }
                }
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


    if (showDialogMutualSensor) {
        currentMutualSensor?.let { sensor ->
            Dialog(
                onDismissRequest = {
                    viewModel.markDialogAsShown()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                ) {
                    var expanded by remember(sensor) { mutableStateOf(value = false) }
                    var selectedSensor by remember(sensor) { mutableStateOf(sensor.sensorA) }
                    var hiddenSensor by remember(sensor) { mutableStateOf(sensor.sensorB) }

                    Column(
                        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                    ) {
                        Text(
                            text = "high-g / low-g selection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        Text(
                            text = "Multiple high-g and low-g sensors have been detected on your board. Please select the sensor you wish to use.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = PrimaryBlue
                        )

                        Text(
                            text = "Note: if you have already used another high-g / low-g sensor and have not reeboted the board, please reboot it first. Otherwise, the new sensor you selected will not function correctly.",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            fontWeight = FontWeight.Normal,
                            color = PrimaryBlue
                        )

                        ExposedDropdownMenuBox(
                            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                            expanded = expanded,
                            onExpandedChange = { newValue ->
                                expanded = newValue
                            }
                        ) {
                            OutlinedTextField(
                                value = selectedSensor,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(),
                                modifier = Modifier.menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    true
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        selectedSensor = sensor.sensorA
                                        hiddenSensor = sensor.sensorB
                                        expanded = false
                                    },
                                    text = {
                                        Text(sensor.sensorA)
                                    }
                                )

                                DropdownMenuItem(
                                    onClick = {
                                        selectedSensor = sensor.sensorB
                                        hiddenSensor = sensor.sensorA
                                        expanded = false
                                    },
                                    text = {
                                        Text(sensor.sensorB)
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            BlueMsButton(
                                text = "Ok",
                                onClick = {
                                    filter =
                                        filter.copy(sensors = filter.sensors + hiddenSensor)
                                    viewModel.markDialogAsShown()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}