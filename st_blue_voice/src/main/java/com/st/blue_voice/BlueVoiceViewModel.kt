/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMFeature
import com.st.blue_sdk.features.beam_forming.BeamDirectionType
import com.st.blue_sdk.features.beam_forming.BeamForming
import com.st.blue_sdk.features.beam_forming.request.ChangeBeamFormingDirection
import com.st.blue_sdk.features.beam_forming.request.EnableDisableBeamForming
import com.st.blue_sdk.features.beam_forming.request.UseStrongBeamFormingAlgorithm
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusFeature
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.audio.AudioService
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.opus.OpusParamsFullDuplex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BlueVoiceViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val audioService: AudioService,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private lateinit var audioRecord: AudioRecord
    private var sendAudioJob: Job? = null
    private var beamformingFeature: BeamForming? = null

    private val _beamFormingEnabled = MutableLiveData(false)
    val beamFormingEnabled: LiveData<Boolean>
        get() = _beamFormingEnabled

    fun startDemo(nodeId: String) {
        blueManager.nodeFeatures(nodeId)
            .firstOrNull { featureName -> featureName.name == BeamForming.NAME }
            ?.let { beamformingFeature = it as BeamForming }


        viewModelScope.launch {
            beamformingFeature?.let { feature ->
                blueManager.getFeatureUpdates(nodeId, listOf(feature), onFeaturesEnabled = {postDelayEnableBeamForming()})
            }
        }
    }

    private fun postDelayEnableBeamForming() {
        viewModelScope.launch {
            delay(2000)
            _beamFormingEnabled.postValue(true)
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            sendAudioJob?.cancel()
            audioService.destroy(nodeId)
            if (beamformingFeature != null) {
                blueManager.disableFeatures(
                    nodeId = nodeId,
                    features = listOf(beamformingFeature!!)
                )
                _beamFormingEnabled.postValue(false)
                beamformingFeature=null
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initAudioRecord(samplingFreq: Int, channels: Short): AudioRecord {
        val ch =
            if (channels.toInt() == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
        val minBufSize = AudioTrack.getMinBufferSize(
            samplingFreq,
            ch,
            AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(samplingFreq)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .build()

    }

    fun getFeatureName(nodeId: String) =
        if (getAudioCodecType(nodeId) == CodecType.OPUS) AudioOpusFeature.NAME else AudioADPCMFeature.NAME

    fun getAudioCodecType(nodeId: String) = audioService.getCodecType(nodeId = nodeId)

    fun getAudioDecodeParams(nodeId: String) = audioService.getDecodeParams(nodeId = nodeId)

    data class Wrapper(val data: ShortArray?, val t: Float = Random.nextFloat())

    val flow = MutableStateFlow(Wrapper(null))

    fun stopAudioRecord(nodeId: String) {
        sendAudioJob?.cancel()
        audioRecord.stop()
        audioService.disableAudio(nodeId)
    }

    fun startAudioRecord(nodeId: String): Flow<Wrapper> {

        audioRecord = initAudioRecord(16000, 1)

        audioRecord.startRecording()
        audioService.enableAudio(nodeId)
        audioService.setEncodeParams(nodeId, OpusParamsFullDuplex())

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "${exception.message}", exception)
        }

        sendAudioJob = viewModelScope.launch(handler) {
            withContext(Dispatchers.IO) {
                val buffer = ShortArray(320)
                while (isActive) {
                    audioRecord.read(buffer, 0, 320)
                    audioService.sendVoiceAudioStream(nodeId, buffer)
                    delay(5)
                    flow.update { Wrapper(buffer) }
                }
            }
        }

        return flow
    }

    suspend fun audioData(nodeId: String): Flow<ShortArray> {

        if (audioService.init(nodeId).not()) {
            Log.e(TAG, "Audio codec initialization failed")
            return flowOf()
        }

        return audioService.startDecondingIncomingAudioStream(nodeId)
    }

    fun enableBeamForming(
        nodeId: String,
        checked: Boolean,
        useStrongBeamFormingAlgorithm: Boolean = false
    ) {
        beamformingFeature?.let {
            viewModelScope.launch {
                val enableBeamFormingCommand =
                    EnableDisableBeamForming(it, checked)
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = enableBeamFormingCommand,
                    responseTimeout = 0
                )

                if (checked) {
                    val useStrongAlgorithm =
                        UseStrongBeamFormingAlgorithm(
                            it,
                            useStrongBeamFormingAlgorithm
                        )

                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = useStrongAlgorithm,
                        responseTimeout = 0
                    )

                    val changeDirection =
                        ChangeBeamFormingDirection(
                            it,
                            BeamDirectionType.Right
                        )

                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = changeDirection,
                        responseTimeout = 0
                    )
                }
            }
        }
    }

    fun getBoardType(nodeId: String): Boards.Model {
        var boardType = Boards.Model.GENERIC
        val node = blueManager.getNode(nodeId)
        node?.let {
            boardType = node.boardType
        }

        return boardType
    }

    fun setBeamFormingDirection(nodeId: String, newDirection: BeamDirectionType) {
        beamformingFeature?.let {
            viewModelScope.launch {
                val changeDirection =
                    ChangeBeamFormingDirection(
                        it,
                        newDirection
                    )
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = changeDirection,
                    responseTimeout = 0
                )
            }
        }
    }


    companion object {
        const val TAG = "BlueVoiceViewModel"
    }
}
