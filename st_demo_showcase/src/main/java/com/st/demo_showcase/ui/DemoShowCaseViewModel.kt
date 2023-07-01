/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiModel
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.services.audio.AudioService
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.core.api.ApplicationAnalyticsService
import com.st.demo_showcase.models.Demo
import com.st.demo_showcase.utils.CustomDTMI
import com.st.preferences.StPreferences
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoShowCaseViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val audioService: AudioService,
    private val stPreferences: StPreferences,
    @ApplicationContext applicationContext: Context,
    private val appAnalyticsService: Set<@JvmSuppressWildcards ApplicationAnalyticsService>
) : ViewModel() {

    private val contentResolver = applicationContext.contentResolver
    private val _currentDemo: MutableStateFlow<Demo?> = MutableStateFlow(null)
    val currentDemo: StateFlow<Demo?> = _currentDemo.asStateFlow()
    private val _currentFw: MutableStateFlow<String> = MutableStateFlow("")
    val currentFw: StateFlow<String> = _currentFw.asStateFlow()
    private val _updateChangeLog: MutableStateFlow<String> = MutableStateFlow("")
    val updateChangeLog: StateFlow<String> = _updateChangeLog.asStateFlow()
    private val _updateUrl: MutableStateFlow<String> = MutableStateFlow("")
    val updateUrl: StateFlow<String> = _updateUrl.asStateFlow()
    private val _updateFw: MutableStateFlow<String> = MutableStateFlow("")
    val updateFw: StateFlow<String> = _updateFw.asStateFlow()
    private val _hasPnplSettings: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasPnplSettings: StateFlow<Boolean> = _hasPnplSettings.asStateFlow()
    private val _showSettingsMenu: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showSettingsMenu: StateFlow<Boolean> = _showSettingsMenu.asStateFlow()
    private val _showFwUpdate: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showFwUpdate: StateFlow<Boolean> = _showFwUpdate.asStateFlow()
    private val _nodeId: MutableStateFlow<String> = MutableStateFlow("")
    val nodeId: StateFlow<String> = _nodeId.asStateFlow()
    private val _availableDemo: MutableStateFlow<List<Demo>> = MutableStateFlow(emptyList())
    val availableDemo: StateFlow<List<Demo>> = _availableDemo.asStateFlow()
    private val _modelUpdates: MutableStateFlow<DtmiModel?> = MutableStateFlow(null)
    private val _device: MutableStateFlow<Node?> = MutableStateFlow(null)
    val device: StateFlow<Node?> = _device.asStateFlow()
    private val _customDTMI: MutableStateFlow<CustomDTMI> = MutableStateFlow(CustomDTMI.NotNecessary)
    val customDTMI: StateFlow<CustomDTMI> = _customDTMI.asStateFlow()
    val pinnedDevices: Flow<List<String>> = stPreferences.getFavouriteDevices()
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()
    private val _isExpert = MutableStateFlow(false)
    val isExpert = _isExpert.asStateFlow()

    private var activityResultRegistryOwner: ActivityResultRegistryOwner? = null

    private val destinationWithoutSettingsMenu = listOf(
        "FwUpgrade",
        "PnplFragment",
        "DebugConsole",
        "LogSettings"
    )

    fun onDestinationChanged(
        prevDestination: NavDestination?,
        destination: NavDestination
    ) {
        val isDemoList = destination.label == "DemoList"
        val isDemoHome = prevDestination?.label == "DemoList"

        _showSettingsMenu.value =
            isDemoList || (
                    isDemoHome && destinationWithoutSettingsMenu.contains(destination.label)
                        .not()
                    )

        //Start Demo?
        if (isDemoHome) {
            appAnalyticsService.forEach {
                currentDemo.value?.let { demo ->
                    it.startDemoAnalytics(demo.displayName)
                }
            }
        } else {
            appAnalyticsService.forEach {
                it.stopDemoAnalytics()
            }
        }


        _hasPnplSettings.value = _currentDemo.value?.let {
            val currentDemoName = it.displayName.lowercase().replace(" ", "_")

            _modelUpdates.value?.extractComponents(demoName = currentDemoName).isNullOrEmpty().not()
        } ?: false
    }

    fun setCurrentDemo(demo: Demo?) {
        _currentDemo.value = demo
    }

    fun setNodeId(nodeId: String) {
        viewModelScope.launch {
            _nodeId.value = nodeId

            val firmwareInfo = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
            _device.value = firmwareInfo

            checkFwUpdate()

            appAnalyticsService.forEach {
                if ((firmwareInfo.advertiseInfo != null) && (firmwareInfo.catalogInfo != null)) {
                    it.reportNodeAnalytics(
                        firmwareInfo.advertiseInfo!!.getName(),
                        firmwareInfo.catalogInfo!!.brdName,
                        firmwareInfo.catalogInfo!!.fwVersion,
                        firmwareInfo.runningFw ?: firmwareInfo.catalogInfo!!.friendlyName()
                    )
                } else {
                    if (firmwareInfo.advertiseInfo != null) {
                        it.reportNodeAnalytics(
                            firmwareInfo.advertiseInfo!!.getName(),
                            firmwareInfo.advertiseInfo!!.getBoardType().name,
                            "Unknown",
                            "Unknown"
                        )
                    } else {
                        it.reportNodeAnalytics("Unknown", "Unknown", "Unknown", "Unknown")
                    }
                }
            }
            val model = blueManager.getDtmiModel(nodeId = nodeId)
            _modelUpdates.value = model
            if (model != null) {
                if(model.customDTMI) {
                    _customDTMI.value = CustomDTMI.Loaded
                } else {
                    _customDTMI.value = CustomDTMI.NotNecessary
                }
            } else {
                if ((firmwareInfo.advertiseInfo != null) && (firmwareInfo.catalogInfo != null)) {
                    if (firmwareInfo.catalogInfo!!.dtmi != null) {
                        _customDTMI.value = CustomDTMI.NotLoaded
                    }
                }
            }

            buildDemoList(nodeId)
        }
    }

    fun disconnect(nodeId: String) {
        _availableDemo.value = emptyList()
        viewModelScope.launch {
            blueManager.disconnect(nodeId = nodeId)
        }
    }

    fun dismissUpdateDialog(doNotShowAgain: Boolean) {
        if (doNotShowAgain) {
            viewModelScope.launch {
                _currentFw.value.let {
                    stPreferences.doNotShowAgainFwUpdate(
                        nodeId = _nodeId.value,
                        currentFw = it
                    )
                }
            }
        }
    }

    fun setDtmiModel(nodeId: String, fileUri: Uri) {
        viewModelScope.launch {
            blueManager.setDtmiModel(
                nodeId = nodeId,
                fileUri = fileUri,
                contentResolver = contentResolver
            )?.extractComponents() ?: emptyList()

            val model = blueManager.getDtmiModel(nodeId = nodeId)
            _modelUpdates.value = model
            if (model != null) {
                if(model.customDTMI) {
                    _customDTMI.value = CustomDTMI.Loaded
                } else {
                    _customDTMI.value = CustomDTMI.NotNecessary
                }
            } else {
                _customDTMI.value = CustomDTMI.NotLoaded
            }
            //Re-create the DemoList after Custom DTMI model
            buildDemoList(nodeId)
        }
    }

    private fun checkFwUpdate() {
        viewModelScope.launch {
            _device.value?.let { currentFirmwareInfo ->
                val updateFirmware = currentFirmwareInfo.fwUpdate
                val currentFwStr = currentFirmwareInfo.catalogInfo?.friendlyName() ?: ""
                _currentFw.value = currentFwStr
                if (stPreferences.mustShowFwUpdate(
                        nodeId = _nodeId.value,
                        currentFw = currentFwStr
                    ) && updateFirmware != null
                ) {
                    val fotaUrl = updateFirmware.fota.fwUrl
                    _updateChangeLog.value = updateFirmware.changelog ?: ""

                    fotaUrl?.let { url ->
                        if (url.isEmpty().not()) {
                            _updateUrl.value = fotaUrl

                            _updateFw.value = updateFirmware.friendlyName()

                            _showFwUpdate.value = true
//                                currentFirmwareInfo.advertiseInfo?.getProtocolVersion() == 2.toShort() && currentFirmwareInfo.catalogInfo?.fota?.type == BoardFotaType.WB_READY
                        }
                    }
                }
            }
        }
    }

    private fun buildDemoList(nodeId: String) {
        val buildDemoList = Demo.buildDemoList(
            blueManager = blueManager,
            audioService = audioService,
            nodeId = nodeId
        ).toMutableList()

        if(audioService.isServerEnable(nodeId)) {
            if (audioService.isMusicServerEnable(nodeId)) {
                val match =
                    buildDemoList.filter { it == Demo.BlueVoiceOpus || it == Demo.SpeechToTextDemo}
                buildDemoList.removeAll(match.toSet())
                buildDemoList.add(Demo.BlueVoiceFullBand)
            } else if (audioService.isFullDuplexEnable(nodeId)) {
                val match =
                    buildDemoList.filter { it == Demo.BlueVoiceOpus || it == Demo.BlueVoiceADPCM }
                buildDemoList.removeAll(match.toSet())
                buildDemoList.add(Demo.BlueVoiceFullDuplex)
            }
        }

        //Check if we could add the Textual and Cloud Demos... we must have at least one Feature that notify something
        if (blueManager.nodeFeatures(nodeId).any { it.isDataNotifyFeature }) {
            buildDemoList.add(Demo.TextualMonitor)
            buildDemoList.add(Demo.Cloud)
        }

        //Add the Flow Demo only to SensorTile.box and SensorTile.box-Pro
        if (_device.value != null) {
            if ((_device.value!!.boardType == Boards.Model.SENSOR_TILE_BOX) || (_device.value!!.boardType == Boards.Model.SENSOR_TILE_BOX_PRO)) {
                buildDemoList.add(Demo.Flow)
            }
        }

        //Remove the PnP-L, HighSpeedDataLog and BinaryContent Demo if there is not a valid DTMI
        if(_customDTMI.value==CustomDTMI.NotLoaded) {
            val match =
            buildDemoList.filter { it == Demo.Pnpl || it == Demo.HighSpeedDataLog2 || it == Demo.BinaryContentDemo}
            buildDemoList.removeAll(match.toSet())
        }

        _availableDemo.value = buildDemoList

        _device.value?.device?.address?.let { id ->
            val initOrder = stPreferences.getDemoOrder(id)
            val currentOrder = buildDemoList.map { it.name }

            if (initOrder.isNotEmpty() && initOrder.containsAll(currentOrder)) {
                _availableDemo.value = initOrder.mapNotNull { demoName ->
                    buildDemoList.find { it.name == demoName }
                }
            }
        }
    }

    private var _updateDemoOrder: Job? = null
    fun saveReorder(from: Int, to: Int) {
        _availableDemo.value = _availableDemo.value.toMutableList().apply {
            add(to, removeAt(from))
        }

        _updateDemoOrder?.cancel()
        _updateDemoOrder = viewModelScope.launch {
            delay(300)

            _device.value?.device?.address?.let {
                stPreferences.setDemoOrder(it, _availableDemo.value.map { it.name })
            }
        }
    }

    fun addToPinDevices(id: String) {
        stPreferences.setFavouriteDevice(id)
    }

    fun removeFromPinDevices(id: String) {
        stPreferences.unsetFavouriteDevice(id)
    }

    fun initExpert() {
        viewModelScope.launch {
            _isExpert.emit(LevelProficiency.fromString(stPreferences.getLevelProficiency()) == LevelProficiency.EXPERT)
        }
    }

    fun profileShow(level: LevelProficiency, type: ProfileType) {
        stPreferences.setLevelProficiency(level = level.name)
        stPreferences.setProfileType(profile = type.name)
    }

    fun initLoginManager(
        activity: Activity,
    ) {
        viewModelScope.launch {
//            activityResultRegistryOwner = activity as ActivityResultRegistryOwner
//            _isLoggedIn.value = loginManager.isLoggedIn()
            _isLoggedIn.value = true
        }
    }

    fun login() {
        viewModelScope.launch {
//            activityResultRegistryOwner?.let {
//                loginManager.login(it.activityResultRegistry)
//                _isLoggedIn.value = loginManager.isLoggedIn()
//            }
            _isLoggedIn.value = true
        }
    }
}

sealed class DebugConsoleMsg {
    data class DebugConsoleCommand(
        val command: String,
        val time: String
    ) : DebugConsoleMsg()

    data class DebugConsoleResponse(
        val response: DebugMessage,
        val time: String?
    ) : DebugConsoleMsg()
}
