/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_voice.full_duplex

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.R
import com.st.blue_voice.databinding.FragmentBluevoiceFullduplexBinding
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class BlueVoiceFullDuplexFragment : Fragment() {
    companion object {
        const val TAG = "BlueVoiceFDFragm"
        private const val BVCONF_PREFIX_KEY = "BlueVoiceFragment"
        const val SENDING_STATUS_KEY = "$BVCONF_PREFIX_KEY.SENDING_STATUS_KEY"
    }

    private var audioManager: AudioManager? = null
    private var audioTrack: AudioTrack? = null
    private val viewModel: BlueVoiceViewModel by viewModels()
    private lateinit var binding: FragmentBluevoiceFullduplexBinding
    private lateinit var nodeId: String
    private var isSending = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FragmentBluevoiceFullduplexBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startRecSwitch.setOnCheckedChangeListener { _, checked ->
            isSending = checked
            storeIsSendingStatus(isSending)
            if (checked) {
                binding.blueVoiceFDOutWaveformView.startPlotting()
                binding.startRecSwitch.setText(R.string.blueVoiceFD_stop)
                startProcess()
            } else {
                binding.blueVoiceFDOutWaveformView.stopPlotting()
                binding.startRecSwitch.setText(R.string.blueVoiceFD_start)
                stopProcess()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioData(nodeId).onStart {
                    val decodeParams = viewModel.getAudioDecodeParams(nodeId)
                    binding.blueVoiceFDSamplingRateValue.text =
                        String.format(
                            Locale.getDefault(), "%d kHz",
                            decodeParams.samplingFreq / 1000
                        )
                    binding.blueVoiceFDCodecValue.text =
                        viewModel.getAudioCodecType(nodeId).name

                    audioManager =
                        requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    initAudioTrack(decodeParams)
                }.onEach {
                    plotAudio(it)
                }.map { it.toByteArray() }
                    .collect {
                        playAudio(it)
                    }
            }
        }
    }

    private fun startProcess() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.startAudioRecord(nodeId)
                .collect {
                    it.data?.let { data ->
                        try {
                            plotSentAudio(data)
                        } catch (ex: Exception) {
                            Log.e(TAG, ex.message, ex)
                        }
                    }
                }
        }
    }

    private fun stopProcess() {
        viewModel.stopAudioRecord(nodeId)
    }

    private fun restoreGuiStatus() {
        binding.startRecSwitch.isChecked = getIsSendingStatus()
    }

    private fun getIsSendingStatus(): Boolean {
        return requireActivity().getSharedPreferences(
            SENDING_STATUS_KEY,
            Context.MODE_PRIVATE
        )
            .getBoolean(SENDING_STATUS_KEY, false)
    }

    private fun storeIsSendingStatus(isSendingStatus: Boolean) {
        requireActivity().getSharedPreferences(
            SENDING_STATUS_KEY,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(SENDING_STATUS_KEY, isSendingStatus)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        restoreGuiStatus()
        binding.blueVoiceFDInWaveformView.startPlotting()
        binding.blueVoiceFDOutWaveformView.startPlotting()
    }

    override fun onPause() {
        super.onPause()
        binding.blueVoiceFDInWaveformView.stopPlotting()
        binding.blueVoiceFDOutWaveformView.stopPlotting()
        viewModel.stopDemo(nodeId = nodeId)
    }

    override fun onStop() {
        super.onStop()
        audioTrack?.pause()
        audioTrack?.flush()
    }

    private fun initAudioTrack(decodeParams: DecodeParams) {

        val minBufSize = AudioTrack.getMinBufferSize(
            decodeParams.samplingFreq,
            if (decodeParams.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            decodeParams.samplingFreq,
            decodeParams.channels,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()
    }

    private fun playAudio(sample: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack?.write(
                sample, 0, sample.size,
                AudioTrack.WRITE_NON_BLOCKING
            )
        }
    }

    private fun plotAudio(sample: ShortArray) {
        binding.blueVoiceFDInWaveformView.updateAudioData(sample)
    }

    private fun plotSentAudio(sample: ShortArray) {
        binding.blueVoiceFDOutWaveformView.updateAudioData(sample)
    }
}