/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.head_bone_conduction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMFeature
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusFeature
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeatureInfo
import com.st.blue_sdk.features.pedometer.Pedometer
import com.st.blue_sdk.features.pedometer.PedometerInfo
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionInfo
import com.st.blue_sdk.features.sensor_fusion.Quaternion
import com.st.blue_sdk.services.audio.AudioService
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.preferences.StPreferences
import com.st.blue_sdk.bt.advertise.getOptBytes
import com.st.blue_sdk.bt.advertise.getOptBytesOffset
import com.st.blue_sdk.models.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadBoneConductionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val audioService: AudioService,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope,
) : ViewModel() {

    var node: Node? = null
    private var featureSensorFusion: Feature<*>? = null
    private var featureMCL: Feature<*>? = null

    private var featurePedometer: Feature<*>? = null

    private val _fusionData =
        MutableStateFlow<Quaternion?>(null)
    val fusionData: StateFlow<Quaternion?>
        get() = _fusionData.asStateFlow()

    private val _registersData =
        MutableStateFlow<List<Short>>(
            emptyList()
        )
    val registersData: StateFlow<List<Short>>
        get() = _registersData.asStateFlow()

    private val _isBetaRelease = MutableStateFlow(false)
    val isBetaRelease = _isBetaRelease.asStateFlow()

    var fusionJob: Job? = null
    var mlcJob: Job? = null

    var pedometerJob: Job? = null

    private var _fusionEnabled = MutableStateFlow(false)
    val fusionEnabled: StateFlow<Boolean>
        get() = _fusionEnabled.asStateFlow()

    private var _audioEnabled = MutableStateFlow(false)
    val audioEnabled: StateFlow<Boolean>
        get() = _audioEnabled.asStateFlow()

    private var _mlcEnabled = MutableStateFlow(false)
    val mlcEnabled: StateFlow<Boolean>
        get() = _mlcEnabled.asStateFlow()

    private var _pedometerEnabled = MutableStateFlow(false)
    val pedometerEnabled: StateFlow<Boolean>
        get() = _pedometerEnabled.asStateFlow()

    // Holds the audio modes extracted from option bytes
    private val _audioModeFromOptionByte = MutableStateFlow<List<String>>(emptyList())
    val audioModeFromOptionByte: StateFlow<List<String>> = _audioModeFromOptionByte.asStateFlow()

    private val _boardId = MutableStateFlow<Int?>(null)
    val boardId: StateFlow<Int?> = _boardId.asStateFlow()

    // Individual tab enable flags from ADV option bytes (LSB -> MSB mapping)
    private val recordingTabEnable = MutableStateFlow(false)
    val recordingEnableValue = recordingTabEnable.asStateFlow()

    private val streamingTabEnable = MutableStateFlow(false)
    val streamingEnableValue = streamingTabEnable.asStateFlow()

    private val vadTabEnable = MutableStateFlow(false)
    val vadEnableValue = vadTabEnable.asStateFlow()

    private val positionTabEnable = MutableStateFlow(false)
    val positionEnableValue = positionTabEnable.asStateFlow()

    private val mlc3TabEnable = MutableStateFlow(false)
    val mlc3EnableValue = mlc3TabEnable.asStateFlow()

    private val mlc4TabEnable = MutableStateFlow(false)
    val mlc4EnableValue = mlc4TabEnable.asStateFlow()

    private val mlc5TabEnable = MutableStateFlow(false)
    val mlc5EnableValue = mlc5TabEnable.asStateFlow()

    private val mlc6TabEnable = MutableStateFlow(false)
    val mlc6EnableValue = mlc6TabEnable.asStateFlow()

    private val _debugConsole = MutableStateFlow("")
    val debugConsole: StateFlow<String> = _debugConsole.asStateFlow()

    var voskModelVersion = ""
    fun getVoskModelVersion() {
        voskModelVersion = "vosk-model-small-en-us-0.15"
    }

    private val _stepData =
        MutableStateFlow<Pair<PedometerInfo, Long?>>(
            Pair(
                PedometerInfo(
                    steps = FeatureField(
                        name = "Steps",
                        value = 0
                    ),
                    frequency = FeatureField(
                        name = "Frequency",
                        value = 0
                    )
                ), null
            )
        )
    val stepData: StateFlow<Pair<PedometerInfo, Long?>>
        get() = _stepData.asStateFlow()


    fun checkVersionBetaRelease() {
        _isBetaRelease.value = stPreferences.isBetaApplication()
    }

    private fun getPossibleFunctionAudioFromOptionBytes(): List<String> {
        val node = this.node ?: return emptyList()
        val advertiseInfo = node.advertiseInfo ?: return emptyList()
        val catalogInfo = node.catalogInfo ?: return emptyList()

        val optBytes = advertiseInfo.getOptBytes()
        val offset = advertiseInfo.getOptBytesOffset()
        val boardIDint = advertiseInfo.getDeviceId()
        _boardId.value = boardIDint.toInt()
        Log.i("getPossibleFunctionAudioFromOptionBytes", "Checking Audio OptionBytes. Full Data: ${optBytes.joinToString(", ") { (it and 0xFF).toString() }}")
        Log.i("getPossibleFunctionAudioFromOptionBytes", "DeviceId: $boardIDint")
        // Use the metadata for "loaded app:" which is at index 2 in the catalog
        val optionByte = catalogInfo.optionBytes.getOrNull(2)

        if (optionByte != null) {
            // As confirmed by logs, the actual value is always at position 3 in advertisement data
            val bytePosition = 3 + offset

            if (bytePosition < optBytes.size) {
                val value = optBytes[bytePosition] and 0xFF
                Log.i("getPossibleFunctionAudioFromOptionBytes", "Reading value at fixed position $bytePosition: $value")

                // Try to find a match in the allowed string values defined in the catalog
                val match = optionByte.stringValues?.find { it.value == value }
                if (match != null) {
                    val result = match.displayName?.split("/")?.map { it.trim() } ?: emptyList()
                    Log.i("getPossibleFunctionAudioFromOptionBytes", "Match found at fixed position $bytePosition: $result")
                    return result
                }
            } else {
                Log.e("getPossibleFunctionAudioFromOptionBytes", "Advertisement data too short. Required position: $bytePosition")
            }
        }
        
        return emptyList()
    }

    /**
     * Unpacks the first 8 bits (LSB) of the option byte into separate enable flags.
     * Binary convention (HGFEDCBA): bit0=A, bit1=B, bit2=C, bit3=D, bit4=E, bit5=F, bit7=H.
     */
    private fun updateTabsEnableFromOptionBytes() {
        val node = this.node ?: return
        val advertiseInfo = node.advertiseInfo ?: return
        val optBytes = advertiseInfo.getOptBytes()
        val offset = advertiseInfo.getOptBytesOffset()
        val bytePosition = 3 + offset

        if (bytePosition < optBytes.size) {
            val value = optBytes[bytePosition] and 0xFF
            Log.i("HeadBoneVM", "Unpacking binary option byte: ${Integer.toBinaryString(value).padStart(8, '0')}")

            recordingTabEnable.value = (value and 0x01) != 0 // bit 0 (A)
            streamingTabEnable.value = (value and 0x02) != 0 // bit 1 (B)
            vadTabEnable.value = (value and 0x04) != 0       // bit 2 (C)
            positionTabEnable.value = (value and 0x08) != 0 // bit 3 (D)
            mlc3TabEnable.value = (value and 0x10) != 0     // bit 4 (E)
            mlc4TabEnable.value = (value and 0x20) != 0     // bit 5 (F)
            mlc5TabEnable.value = (value and 0x40) != 0     // bit 6 (G)
            mlc6TabEnable.value = (value and 0x80) != 0     // bit 7 (H)
        }
    }

    fun startDemo(nodeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Synchronous retrieval of firmware info
            node = blueManager.getNodeWithFirmwareInfo(nodeId)

            node?.let {
                // 2. Calculation of the audio mode once data is ready
                val audioMode = getPossibleFunctionAudioFromOptionBytes()
                _audioModeFromOptionByte.value = audioMode

                // 2b. Unpack binary bits for tab enablement
                updateTabsEnableFromOptionBytes()
            }
            
            // 3. Features initialization
            initializeFeatures(nodeId)
        }

        viewModelScope.launch {
            blueManager.getDebugMessages(nodeId = nodeId)?.collect {
                val message = it.payload
                if (message.isNotEmpty()) {
                    _debugConsole.value += message
                }
            }
        }
    }

    fun sendDebugCommand(nodeId: String, command: String) {
        viewModelScope.launch {
            blueManager.writeDebugMessage(nodeId, command)
        }
    }

    private fun initializeFeatures(nodeId: String) {
        //Sensor Fusion Feature
        if (featureSensorFusion == null) {
            blueManager.nodeFeatures(nodeId).find {
                MemsSensorFusionCompat.NAME == it.name || MemsSensorFusion.NAME == it.name
            }?.let { f ->
                featureSensorFusion = f
            }
        }

        //MLC Feature
        if (featureMCL == null) {
            blueManager.nodeFeatures(nodeId).find {
                RegistersFeature.ML_CORE_NAME == it.name
            }?.let { f ->
                featureMCL = f
            }
        }

        //Pedometer Feature
        if (featurePedometer == null) {
            blueManager.nodeFeatures(nodeId).find {
                Pedometer.NAME == it.name
            }?.let { f ->
                featurePedometer = f
            }
        }
    }

    fun stopDemo(nodeId: String) {
        fusionJob?.cancel()
        fusionJob = null
        featureSensorFusion?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
        _fusionEnabled.value = false

        mlcJob?.cancel()
        mlcJob = null
        featureMCL?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
        _mlcEnabled.value = false

        pedometerJob?.cancel()
        pedometerJob = null
        featurePedometer?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
        _pedometerEnabled.value = false

        _audioEnabled.value = false
        coroutineScope.launch {
            audioService.destroy(nodeId)
        }
    }

    fun destroyAudioService(nodeId: String) {
        coroutineScope.launch {
            audioService.destroy(nodeId)
        }
    }

    suspend fun audioData(nodeId: String): Flow<ShortArray> {
        if (audioService.init(nodeId).not()) {
            Log.e("HeadBoneConductionVM", "Audio codec initialization failed")
            return flowOf()
        }

        return audioService.startDecodingIncomingAudioStream(nodeId)
    }

    fun startStopAudio() {
        _audioEnabled.value = !_audioEnabled.value
    }

    fun startStopFusion(nodeId: String) {
        if (fusionJob != null) {
            featureSensorFusion?.let { feature ->
                fusionJob?.let {
                    it.cancel()
                    coroutineScope.launch {
                        blueManager.disableFeatures(nodeId, listOf(feature))
                    }
                    fusionJob = null
                }

                _fusionEnabled.value = false
            }
        } else {
            fusionJob = featureSensorFusion?.let {
                _fusionEnabled.value = true
                viewModelScope.launch {
                    blueManager.getFeatureUpdates(
                        nodeId,
                        listOf(it)
                    ).collect { update ->
                        val data = update.data
                        if (data is MemsSensorFusionInfo) {

                            if (data.quaternions.size == 1) {
                                _fusionData.emit(data.quaternions[0].value)
                            } else {
                                var prevTimeStamp: Long = -1
                                for (current in data.quaternions) {
                                    val currentTimeStamp = current.value.timeStamp
                                    if (prevTimeStamp != -1L) {
                                        delay(currentTimeStamp - prevTimeStamp)
                                    }
                                    _fusionData.emit(current.value)
                                    prevTimeStamp = currentTimeStamp
                                }
                                // _fusionData.emit(data.quaternions[0].value)
                            }
                        }
                    }
                }
            }
        }
    }

    fun startStopMLC(nodeId: String) {
        if (mlcJob != null) {
            featureMCL?.let { feature ->
                mlcJob?.let {
                    it.cancel()
                    coroutineScope.launch {
                        blueManager.disableFeatures(nodeId, listOf(feature))
                    }
                    mlcJob = null
                }
                _mlcEnabled.value = false
            }
        } else {
            featureMCL?.let {
                _mlcEnabled.value = true
                mlcJob = viewModelScope.launch {
                    blueManager.getFeatureUpdates(
                        nodeId,
                        listOf(it)
                    ).collect { update ->
                        val data = update.data
                        if (data is RegistersFeatureInfo) {
                            _registersData.value = data.registers.map { it2 -> it2.value }
                        }
                    }
                }
            }
        }
    }

    fun startStopPedometer(nodeId: String) {
        if (pedometerJob != null) {
            featurePedometer?.let { feature ->
                pedometerJob?.let {
                    it.cancel()
                    coroutineScope.launch {
                        blueManager.disableFeatures(nodeId, listOf(feature))
                    }
                    pedometerJob = null
                }
                _pedometerEnabled.value = false
            }
        } else {
            featurePedometer?.let {
                _pedometerEnabled.value = true
                pedometerJob = viewModelScope.launch {
                    blueManager.getFeatureUpdates(
                        nodeId,
                        listOf(it)
                    ).collect { update ->
                        val data = update.data
                        if (data is PedometerInfo) {
                            _stepData.emit(Pair(data, update.timeStamp))
                        }
                    }
                }
            }
        }
    }

    fun getFeatureName(nodeId: String) =
        if (getAudioCodecType(nodeId) == CodecType.OPUS) AudioOpusFeature.NAME else AudioADPCMFeature.NAME

    fun getAudioCodecType(nodeId: String) = audioService.getCodecType(nodeId = nodeId)

    fun getAudioDecodeParams(nodeId: String) = audioService.getDecodeParams(nodeId = nodeId)

}
