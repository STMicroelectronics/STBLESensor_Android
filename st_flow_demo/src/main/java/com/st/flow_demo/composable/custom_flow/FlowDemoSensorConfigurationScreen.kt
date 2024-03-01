package com.st.flow_demo.composable.custom_flow

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.blue_sdk.models.Boards
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.common.ClickableTest
import com.st.flow_demo.composable.common.FlowDemoAlertDialog
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoCheckDecimalDropDownMenuEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoDecimalEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoDropDownMenuEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoRadioButtonGroupEntry
import com.st.flow_demo.helpers.checkIfRemoveBluetooth
import com.st.flow_demo.helpers.extractFSMLabels
import com.st.flow_demo.helpers.extractMLCLabels
import com.st.flow_demo.helpers.fillFilterSectionByBoardSensorPowerMode
import com.st.flow_demo.helpers.generateFSMLabelsString
import com.st.flow_demo.helpers.generateMLCLabelsString
import com.st.flow_demo.helpers.parseUcfFile
import com.st.blue_sdk.board_catalog.models.FilterConfiguration
import com.st.flow_demo.models.MlcFsmDecisionTreeOutput
import com.st.flow_demo.models.MlcFsmLabelEntry
import com.st.blue_sdk.board_catalog.models.PowerMode
import com.st.blue_sdk.board_catalog.models.SensorConfiguration
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoSensorConfigurationScreen(
    paddingValues: PaddingValues,
    viewModel: FlowDemoViewModel,
    navController: NavHostController
) {

    var openConfirmationDialog by remember { mutableStateOf(value = false) }

    var isMLC by remember { mutableStateOf(value = false) }

    var isMLCChanged by remember { mutableStateOf(false) }
    var isFSMChanged by remember { mutableStateOf(false) }

    val boardType by remember { mutableStateOf(viewModel.getBoardType()) }

    BackHandler {
        openConfirmationDialog = true
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            text = "Input Configuration"
        )

        if (viewModel.sensorOnConfig != null) {
            val sensorOnConfig by remember { mutableStateOf(value = viewModel.sensorOnConfig!!.copy()) }
            val configOnConfig by remember {
                mutableStateOf(
                    value =
                    if (sensorOnConfig.configuration != null) {
                        sensorOnConfig.configuration!!.copy()
                    } else {
                        SensorConfiguration()
                    }
                )
            }

            val pickFileLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { fileUri ->
                if (fileUri != null) {
                    val errorText = parseUcfFile(
                        context = context,
                        uri = fileUri,
                        sensorConfiguration = configOnConfig,
                        isMLC = isMLC,
                        board = boardType
                    )
                    errorText?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                    isMLCChanged = !isMLCChanged
                    isFSMChanged = !isFSMChanged
                }
            }

            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingMedium
                ),
                fontSize = 18.sp,
                text = sensorOnConfig.description
            )

            var needToResetSelectedLowHighPassFilters by remember { mutableStateOf(false) }

            fillFilterSectionByBoardSensorPowerMode(
                context = context,
                sensorId = sensorOnConfig.id,
                powerMode = sensorOnConfig.configuration?.powerMode,
                sensorConfiguration = configOnConfig,
                board = boardType
            )

            if (configOnConfig.regConfig != null) {
                //MLC Section
                configOnConfig.mlcLabels?.let {

                    val mlcDecisionTreeOutputList by
                    remember(isMLCChanged) {
                        mutableStateOf(
                            if (configOnConfig.mlcLabels!!.isNotEmpty()) {
                                extractMLCLabels(configOnConfig.mlcLabels!!)
                            } else {
                                val tmpList = mutableListOf<MlcFsmDecisionTreeOutput>()
                                val numberReg =
                                    if (boardType == Boards.Model.SENSOR_TILE_BOX) {
                                        8
                                    } else {
                                        4
                                    }
                                for (i in 0 until numberReg) {
                                    tmpList.add(
                                        MlcFsmDecisionTreeOutput(
                                            number = i,
                                            name = "",
                                            mlcFsmLabels = listOf()
                                        )
                                    )
                                }
                                tmpList.toList()
                            }
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LocalDimensions.current.paddingSmall),
                        shape = Shapes.small,
                        shadowElevation = LocalDimensions.current.elevationSmall
                    ) {

                        Column(
                            modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
                        ) {
                            Text(
                                modifier = Modifier.padding(
                                    bottom = LocalDimensions.current.paddingNormal
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                text = "MLC Configuration"
                            )

                            Row(
                                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BlueMsButton(
                                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                                    text = ".ucf file",
                                    iconPainter = painterResource(id = R.drawable.ic_file_open),
                                    onClick = {
                                        isMLC = true
                                        pickFileLauncher.launch(arrayOf("*/*"))
                                    }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                BlueMsButton(
                                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                                    text = "Reset",
                                    iconPainter = painterResource(id = R.drawable.ic_restart),
                                    onClick = {
                                        configOnConfig.mlcLabels = ""
                                        isMLCChanged = !isMLCChanged
                                    }
                                )

                            }
                            Text(
                                modifier = Modifier.padding(
                                    bottom = LocalDimensions.current.paddingSmall
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                text = "Labels:"
                            )
                            mlcDecisionTreeOutputList.forEach { decisionTree ->
                                var isOpen by remember(isMLCChanged) { mutableStateOf(value = false) }
                                var decisionTreeSize by remember(isMLCChanged) {
                                    mutableIntStateOf(
                                        value = decisionTree.mlcFsmLabels.size
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(
                                        start = LocalDimensions.current.paddingNormal,
                                        bottom = LocalDimensions.current.paddingNormal
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                                            fontSize = 14.sp,
                                            text = "DecTree${decisionTree.number}"
                                        )

                                        var internalState by remember(isMLCChanged) {
                                            mutableStateOf(value = decisionTree.name)
                                        }

                                        OutlinedTextField(
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                            value = internalState,
                                            onValueChange = {
                                                internalState = it
                                                decisionTree.name = it
                                                decisionTree.enabled = true
                                            },
                                            placeholder = {
                                                Text(
                                                    "DecTree${decisionTree.number}",
                                                    fontStyle = FontStyle.Italic
                                                )
                                            },
                                        )

                                        if (isOpen) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    isOpen = !isOpen
                                                }
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    isOpen = !isOpen
                                                }
                                            )
                                        }
                                    }
                                    if (isOpen) {
                                        Row(
                                            modifier = Modifier.padding(
                                                start = LocalDimensions.current.paddingNormal,
                                                bottom = LocalDimensions.current.paddingSmall
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Text(
                                                modifier = Modifier.weight(0.3f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                text = "Output"
                                            )

                                            Text(
                                                modifier = Modifier.weight(0.6f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                text = "Label"
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.7f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LibraryAdd,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clickable {
                                                        val tmpList =
                                                            decisionTree.mlcFsmLabels.toMutableList()
                                                        tmpList.add(
                                                            MlcFsmLabelEntry(
                                                                value = tmpList.size,
                                                                label = ""
                                                            )
                                                        )
                                                        decisionTree.mlcFsmLabels =
                                                            tmpList.toList()

                                                        decisionTreeSize++
                                                    }
                                            )
                                        }

                                        for (index in 0 until decisionTreeSize) {

                                            var internalStringState by remember(
                                                isMLCChanged,
                                                decisionTreeSize
                                            ) {
                                                mutableStateOf(value = decisionTree.mlcFsmLabels[index].label)
                                            }

                                            var internalNumberState by remember(
                                                isMLCChanged,
                                                decisionTreeSize
                                            ) {
                                                mutableStateOf(value = decisionTree.mlcFsmLabels[index].value.toString())
                                            }

                                            Row(
                                                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                var errorRegValue by remember(
                                                    isMLCChanged,
                                                    decisionTreeSize
                                                ) { mutableStateOf(false) }

                                                OutlinedTextField(
                                                    modifier = Modifier.weight(3f),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number
                                                    ),
                                                    value = internalNumberState,
                                                    onValueChange = {
                                                        it.toIntOrNull()?.let { number ->
                                                            if ((number >= 0) && (number < 256)) {
                                                                errorRegValue = false
                                                                decisionTree.mlcFsmLabels[index].value =
                                                                    number
                                                            } else {
                                                                errorRegValue = true
                                                            }
                                                        }
                                                        internalNumberState = it
                                                    },
                                                    isError = errorRegValue,
                                                    label = { if (errorRegValue) Text(text = "0..255") }
                                                )

                                                OutlinedTextField(
                                                    modifier = Modifier.weight(6f),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text
                                                    ),
                                                    value = internalStringState,
                                                    placeholder = {
                                                        Text(
                                                            "Label",
                                                            fontStyle = FontStyle.Italic
                                                        )
                                                    },
                                                    onValueChange = {
                                                        internalStringState = it
                                                        decisionTree.mlcFsmLabels[index].label = it
                                                    }
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    tint = ErrorText,
                                                    contentDescription = null,
                                                    modifier = Modifier.clickable {
                                                        val tmpList =
                                                            decisionTree.mlcFsmLabels.toMutableList()
                                                        tmpList.removeAt(index)
                                                        decisionTree.mlcFsmLabels =
                                                            tmpList.toList()
                                                        decisionTreeSize--
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                ClickableTest(text = "Save MLC", onClick = {
                                    configOnConfig.mlcLabels =
                                        generateMLCLabelsString(mlcDecisionTreeOutputList)
                                    Toast.makeText(
                                        context,
                                        "MLC Config Updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            }
                        }
                    }
                }

                //FSM Section
                configOnConfig.fsmLabels?.let {

                    val fsmDecisionTreeOutputList by
                    remember(isFSMChanged) {
                        mutableStateOf(
                            if (configOnConfig.fsmLabels!!.isNotEmpty()) {
                                extractFSMLabels(configOnConfig.fsmLabels!!)
                            } else {
                                val tmpList = mutableListOf<MlcFsmDecisionTreeOutput>()
                                val numberReg =
                                    if (boardType == Boards.Model.SENSOR_TILE_BOX) {
                                        16
                                    } else {
                                        8
                                    }
                                for (i in 0 until numberReg) {
                                    tmpList.add(
                                        MlcFsmDecisionTreeOutput(
                                            number = i,
                                            name = "",
                                            mlcFsmLabels = listOf()
                                        )
                                    )
                                }
                                tmpList.toList()
                            }
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LocalDimensions.current.paddingSmall),
                        shape = Shapes.small,
                        shadowElevation = LocalDimensions.current.elevationSmall
                    ) {

                        Column(
                            modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
                        ) {
                            Text(
                                modifier = Modifier.padding(
                                    bottom = LocalDimensions.current.paddingNormal
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                text = "FSM Configuration"
                            )

                            Row(
                                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BlueMsButton(
                                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                                    text = ".ucf file",
                                    iconPainter = painterResource(id = R.drawable.ic_file_open),
                                    onClick = {
                                        isMLC = false
                                        pickFileLauncher.launch(arrayOf("*/*"))
                                    }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                BlueMsButton(
                                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                                    text = "Reset",
                                    iconPainter = painterResource(id = R.drawable.ic_restart),
                                    onClick = {
                                        configOnConfig.fsmLabels = ""
                                        isFSMChanged = !isFSMChanged
                                    }
                                )

                            }
                            Text(
                                modifier = Modifier.padding(
                                    bottom = LocalDimensions.current.paddingSmall
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                text = "Labels:"
                            )
                            fsmDecisionTreeOutputList.forEach { decisionTree ->
                                var isOpen by remember(isFSMChanged) { mutableStateOf(value = false) }
                                var decisionTreeSize by remember(isFSMChanged) {
                                    mutableIntStateOf(
                                        value = decisionTree.mlcFsmLabels.size
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(
                                        start = LocalDimensions.current.paddingNormal,
                                        bottom = LocalDimensions.current.paddingNormal
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                                            fontSize = 14.sp,
                                            text = "DecTree${decisionTree.number}"
                                        )

                                        var internalState by remember(isFSMChanged) {
                                            mutableStateOf(value = decisionTree.name)
                                        }

                                        OutlinedTextField(
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                            value = internalState,
                                            onValueChange = {
                                                internalState = it
                                                decisionTree.name = it
                                                decisionTree.enabled = true
                                            },
                                            placeholder = {
                                                Text(
                                                    "DecTree${decisionTree.number}",
                                                    fontStyle = FontStyle.Italic
                                                )
                                            },
                                        )

                                        if (isOpen) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    isOpen = !isOpen
                                                }
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    isOpen = !isOpen
                                                }
                                            )
                                        }
                                    }
                                    if (isOpen) {
                                        Row(
                                            modifier = Modifier.padding(
                                                start = LocalDimensions.current.paddingNormal,
                                                bottom = LocalDimensions.current.paddingSmall
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Text(
                                                modifier = Modifier.weight(0.3f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                text = "Output"
                                            )

                                            Text(
                                                modifier = Modifier.weight(0.6f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                text = "Label"
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.7f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LibraryAdd,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clickable {
                                                        val tmpList =
                                                            decisionTree.mlcFsmLabels.toMutableList()
                                                        tmpList.add(
                                                            MlcFsmLabelEntry(
                                                                value = tmpList.size,
                                                                label = ""
                                                            )
                                                        )
                                                        decisionTree.mlcFsmLabels =
                                                            tmpList.toList()

                                                        decisionTreeSize++
                                                    }
                                            )
                                        }

                                        for (index in 0 until decisionTreeSize) {

                                            var internalStringState by remember(
                                                isFSMChanged,
                                                decisionTreeSize
                                            ) {
                                                mutableStateOf(value = decisionTree.mlcFsmLabels[index].label)
                                            }

                                            var internalNumberState by remember(
                                                isFSMChanged,
                                                decisionTreeSize
                                            ) {
                                                mutableStateOf(value = decisionTree.mlcFsmLabels[index].value.toString())
                                            }

                                            Row(
                                                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                var errorRegValue by remember(
                                                    isFSMChanged,
                                                    decisionTreeSize
                                                ) { mutableStateOf(false) }

                                                OutlinedTextField(
                                                    modifier = Modifier.weight(3f),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number
                                                    ),
                                                    value = internalNumberState,
                                                    onValueChange = {
                                                        it.toIntOrNull()?.let { number ->
                                                            if ((number >= 0) && (number < 256)) {
                                                                errorRegValue = false
                                                                decisionTree.mlcFsmLabels[index].value =
                                                                    number
                                                            } else {
                                                                errorRegValue = true
                                                            }
                                                        }
                                                        internalNumberState = it
                                                    },
                                                    isError = errorRegValue,
                                                    label = { if (errorRegValue) Text(text = "0..255") }
                                                )

                                                OutlinedTextField(
                                                    modifier = Modifier.weight(6f),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text
                                                    ),
                                                    value = internalStringState,
                                                    placeholder = {
                                                        Text(
                                                            "Label",
                                                            fontStyle = FontStyle.Italic
                                                        )
                                                    },
                                                    onValueChange = {
                                                        internalStringState = it
                                                        decisionTree.mlcFsmLabels[index].label = it
                                                    }
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    tint = ErrorText,
                                                    contentDescription = null,
                                                    modifier = Modifier.clickable {
                                                        val tmpList =
                                                            decisionTree.mlcFsmLabels.toMutableList()
                                                        tmpList.removeAt(index)
                                                        decisionTree.mlcFsmLabels =
                                                            tmpList.toList()
                                                        decisionTreeSize--
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                ClickableTest(text = "Save FSM", onClick = {
                                    configOnConfig.fsmLabels =
                                        generateFSMLabelsString(fsmDecisionTreeOutputList)
                                    Toast.makeText(
                                        context,
                                        "FSM Config Updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            }
                        }
                    }
                }
            }

            //Acquisition Time Section
            if (configOnConfig.acquisitionTime != null) {
                var selectedValue by remember { mutableFloatStateOf(value = configOnConfig.acquisitionTime!!.toFloat() / 60) }

                FlowDemoDecimalEntry(
                    title = "Acquisition Time",
                    label = "Minutes",
                    defaultValue = selectedValue,
                    onModified = {
                        selectedValue = it
                        configOnConfig.acquisitionTime = it.times(60.0)
                    }
                )
            }

            //PowerMode section
            if (sensorOnConfig.powerModes != null) {

                if (configOnConfig.powerMode == null) {
                    configOnConfig.powerMode =
                        sensorOnConfig.powerModes!![0].mode
                }

                var selectedPowerModeId by remember { mutableIntStateOf(value = configOnConfig.powerMode!!.id) }

                var selectedOdrForDropDown by remember(selectedPowerModeId) {
                    mutableStateOf(
                        configOnConfig.odr
                    )
                }

                var selectedPowerModeDescription by remember(selectedPowerModeId) {
                    mutableStateOf(
                        value = sensorOnConfig.powerModes!!.firstOrNull { it.mode.id == selectedPowerModeId })
                }

                val minCustomSampleTime by remember(selectedPowerModeDescription) {
                    mutableStateOf(
                        value = selectedPowerModeDescription?.minCustomSampleTime
                    )
                }

                val valuesForDropDown: List<String> =
                    if (selectedPowerModeDescription != null) {
                        selectedPowerModeDescription!!.odrs.map { "$it Hz" }
                    } else {
                        listOf()
                    }

                if (sensorOnConfig.powerModes!!.size > 1) {
                    val values = mutableListOf<Pair<Int, String>>()

                    sensorOnConfig.powerModes!!.forEach { powerMode ->
                        values.add(powerMode.mode.id to powerMode.label)
                    }

                    FlowDemoRadioButtonGroupEntry(
                        title = "PowerMode",
                        values = values,
                        defaultValue = selectedPowerModeId,
                        onValueSelected = {
                            selectedPowerModeId = it
                            configOnConfig.powerMode =
                                PowerMode.Mode.entries.toTypedArray()[it]

                            //configOnConfig.odr = null
                            selectedPowerModeDescription =
                                sensorOnConfig.powerModes!!.firstOrNull { powerMode -> powerMode.mode.id == selectedPowerModeId }
                            selectedOdrForDropDown = selectedPowerModeDescription!!.odrs[0]
                            configOnConfig.odr = selectedOdrForDropDown

                            configOnConfig.filters = FilterConfiguration()
                            needToResetSelectedLowHighPassFilters = true
                            fillFilterSectionByBoardSensorPowerMode(
                                context = context,
                                sensorId = sensorOnConfig.id,
                                powerMode = configOnConfig.powerMode,
                                sensorConfiguration = configOnConfig,
                                board = boardType
                            )
                        }
                    )
                }

                //ODR Section
                if (configOnConfig.powerMode != null) {


                    var checkedCheckBox by remember {
                        mutableStateOf(value = configOnConfig.oneShotTime != null)
                    }
                    var defaultValueDecimalEntry by remember {
                        mutableStateOf(value = configOnConfig.oneShotTime)
                    }


                    FlowDemoCheckDecimalDropDownMenuEntry(
                        title = if (minCustomSampleTime != null) {
                            "Output Data Rate/Sample Time"
                        } else {
                            "Output Data Rate"
                        },
                        subTitle1 = "Custom Sample Time",
                        subTitle2 = "Standard ODR",
                        checkDecimalVisible = minCustomSampleTime != null,
                        checkedCheckBox = checkedCheckBox,
                        enabledCheckBox = true,
                        onCheckChange = {
                            checkedCheckBox = it
                            if (!it) {
                                defaultValueDecimalEntry = null
                                configOnConfig.oneShotTime = null
                            }
                        },
                        selectedValueDecimalEntry = defaultValueDecimalEntry?.toFloat() ?: 0f,
                        textDecimalEntry = if (minCustomSampleTime != null)
                            String.format(
                                "Min Sample Time  %.2f s",
                                selectedPowerModeDescription!!.minCustomSampleTime!!
                            )
                        else "",
                        onModifiedDecimalEntry = {
                            defaultValueDecimalEntry = it.toDouble()
                            configOnConfig.oneShotTime =
                                defaultValueDecimalEntry
                            configOnConfig.filters = FilterConfiguration()
                            needToResetSelectedLowHighPassFilters = true
                            fillFilterSectionByBoardSensorPowerMode(
                                context = context,
                                sensorId = sensorOnConfig.id,
                                powerMode = null,
                                sensorConfiguration = configOnConfig,
                                board = boardType
                            )
                        },
                        values = valuesForDropDown,
                        initialValueDropDown = "${selectedOdrForDropDown!!} Hz",
                        errorTextDropDown = if (checkedCheckBox) {
                            if ((minCustomSampleTime != null) && (defaultValueDecimalEntry != null)) {
                                if (defaultValueDecimalEntry!! < minCustomSampleTime!!) {
                                    "Must be bigger than $minCustomSampleTime"
                                } else {
                                    if (selectedPowerModeDescription!!.odrs.contains(
                                            defaultValueDecimalEntry
                                        )
                                    ) {
                                        "Must be different from one Default ODR value"
                                    } else {
                                        null
                                    }
                                }
                            } else {
                                null
                            }
                        } else {
                            if (sensorOnConfig.bleMaxOdr != null) {
                                if (selectedOdrForDropDown!! > sensorOnConfig.bleMaxOdr!!) {
                                    "Stream to BLE not available!"
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        },
                        onValueSelectedDropDown = {
                            selectedOdrForDropDown = it.toDoubleOrNull() ?: 0f.toDouble()
                            configOnConfig.odr = selectedOdrForDropDown
                            configOnConfig.filters = FilterConfiguration()
                            needToResetSelectedLowHighPassFilters = true
                            fillFilterSectionByBoardSensorPowerMode(
                                context = context,
                                sensorId = sensorOnConfig.id,
                                powerMode = null,
                                sensorConfiguration = configOnConfig,
                                board = boardType
                            )
                        }
                    )
                }
            }

            if (configOnConfig.filters != null) {
                var selectedValueLowPass by remember {
                    mutableStateOf(
                        value = if (configOnConfig.filters!!.lowPass != null) {
                            "${configOnConfig.filters!!.lowPass!!.label} Hz"
                        } else {
                            "NoFilter"
                        }
                    )
                }

                var selectedValueHighPass by remember {
                    mutableStateOf(
                        value = if (configOnConfig.filters!!.highPass != null) {
                            "${configOnConfig.filters!!.highPass!!.label} Hz"
                        } else {
                            "NoFilter"
                        }
                    )
                }

                if (needToResetSelectedLowHighPassFilters) {
                    needToResetSelectedLowHighPassFilters = false
                    selectedValueLowPass = "NoFilter"
                    selectedValueHighPass = "NoFilter"
                }

                //LowPass Filter
                if (configOnConfig.lowPassCutoffs != null) {

                    val values: MutableList<String> = mutableListOf()
                    values.add("No Filter")
                    configOnConfig.lowPassCutoffs!!.forEach { filter ->
                        values.add(
                            "${filter.label} Hz"
                        )
                    }

                    FlowDemoDropDownMenuEntry(
                        title = "LowPass Filter",
                        values = values,
                        initialValue = selectedValueLowPass,
                        onValueSelected = {
                            selectedValueLowPass = it
                            val cutOffEntry = values.indexOf(it)
                            if (cutOffEntry > 0) {
                                configOnConfig.filters!!.lowPass =
                                    configOnConfig.lowPassCutoffs!![cutOffEntry - 1].copy()
                            }
                        })
                }

                //HighPass filter section
                if (configOnConfig.highPassCutoffs != null) {

                    val values: MutableList<String> = mutableListOf()
                    values.add("No Filter")
                    configOnConfig.highPassCutoffs!!.forEach { filter ->
                        values.add(
                            "${filter.label} Hz"
                        )
                    }


                    FlowDemoDropDownMenuEntry(
                        title = "HighPass Filter",
                        values = values,
                        initialValue = selectedValueHighPass,
                        onValueSelected = {
                            selectedValueHighPass = it
                            val cutOffEntry = values.indexOf(it)
                            if (cutOffEntry > 0) {
                                configOnConfig.filters!!.highPass =
                                    configOnConfig.highPassCutoffs!![cutOffEntry - 1].copy()
                            }
                        })
                }
            }

            //FullScale section
            if (sensorOnConfig.fullScales != null) {
                val measureUnit = sensorOnConfig.fullScaleUm
                val values = sensorOnConfig.fullScales!!.map { "$it $measureUnit" }
                var selectedValue by remember { mutableStateOf(value = "${configOnConfig.fullScale} $measureUnit") }

                FlowDemoDropDownMenuEntry(
                    title = "Full-Scale (FS)",
                    values = values,
                    initialValue = selectedValue,
                    onValueSelected = {
                        selectedValue = it
                        val index = values.indexOf(it)
                        configOnConfig.fullScale =
                            sensorOnConfig.fullScales!![index]
                    })
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = LocalDimensions.current.paddingNormal),
            ) {
                BlueMsButton(
                    text = "Cancel",
                    iconPainter = painterResource(id = R.drawable.ic_close),
                    onClick = {
                        //Don't save the new Sensor Configuration and come back to previous screen
                        openConfirmationDialog = true
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                BlueMsButton(
                    text = stringResource(id = R.string.done_message),
                    iconPainter = painterResource(id = R.drawable.ic_done),
                    enabled = true,
                    onClick = {
                        //We need to save the new Sensor Configuration
                        val flow = viewModel.flowOnCreation
                        flow?.let {
                            val sensorToChange =
                                flow.sensors.firstOrNull { it.id == sensorOnConfig.id }
                            if (sensorToChange != null) {

                                sensorToChange.configuration = configOnConfig.copy()

                                viewModel.flowOnCreation!!.outputs = checkIfRemoveBluetooth(
                                    viewModel.flowOnCreation!!.outputs,
                                    sensorToChange
                                )
                            }
                        }

                        //Come back to previous screen
                        navController.popBackStack()
                    }
                )
            }

        } else {
            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                color = ErrorText,
                fontSize = 18.sp,
                text = "Something wrong.."
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = LocalDimensions.current.paddingNormal),
            ) {
                BlueMsButton(
                    text = "Cancel",
                    iconPainter = painterResource(id = R.drawable.ic_close),
                    onClick = {
                        //Don't save the new Sensor Configuration and come back to previous screen
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    if (openConfirmationDialog) {
        FlowDemoAlertDialog(
            title = stringResource(id = context.applicationInfo.labelRes),
            message = "Losing all changes.\r\tContinue?",
            onDismiss = { openConfirmationDialog = false },
            onConfirmation = { navController.popBackStack() }
        )
    }
}