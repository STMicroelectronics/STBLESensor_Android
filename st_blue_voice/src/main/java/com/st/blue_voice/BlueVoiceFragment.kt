/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.Utils.AudioRecorder
import com.st.blue_voice.databinding.FragmentBluevoiceBinding
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class BlueVoiceFragment : Fragment() {
    companion object {
        const val BVCONF_PREFIX_KEY = "BlueVoiceFragment"
        const val VOLUME_LEVEL_KEY = "$BVCONF_PREFIX_KEY.VOLUME_LEVEL_KEY"
        const val IS_MUTE_KEY = "$BVCONF_PREFIX_KEY.IS_MUTE"
    }

    private var volumeBar: SeekBar? = null
    private var manageMuteButton: ManageMuteButton? = null
    private var manageRecButton: ManageRecButton? = null
    private var audioManager: AudioManager? = null
    private var audioWavDump: AudioRecorder? = null
    private var audioTrack: AudioTrack? = null
    private val viewModel: BlueVoiceViewModel by viewModels()
    private lateinit var binding: FragmentBluevoiceBinding
    private lateinit var nodeId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FragmentBluevoiceBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioData(nodeId).onStart {
                    val decodeParams = viewModel.getAudioDecodeParams(nodeId)
                    binding.blueVoiceSamplingRateValue.text =
                        String.format(
                            Locale.getDefault(), "%d kHz",
                            decodeParams.samplingFreq / 1000
                        )
                    binding.blueVoiceCodecValue.text =
                        viewModel.getAudioCodecType(nodeId).name

                    audioManager =
                        requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    volumeBar = binding.blueVoiceVolumeValue
                    audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        ?.let { initializeVolumeBar(it) }
                    manageMuteButton = ManageMuteButton(binding.blueVoiceMuteButton)
                    manageRecButton = ManageRecButton(binding.blueVoiceRecButton)
                    initializeAudioDump(viewModel.getFeatureName(nodeId))
                    initAudioTrack(decodeParams)
                    restoreGuiStatus()
                }.onEach {
                    plotAudio(it)
                    recAudio(it)
                }.map { it.toByteArray() }
                    .collect {
                        playAudio(it)
                    }
            }
        }

        viewModel.beamFormingEnabled.observe(viewLifecycleOwner, Observer {
                binding.blueVoiceBeamformingValue.isEnabled = it
        })

        binding.blueVoiceBeamformingValue.setOnCheckedChangeListener { _: View, isChecked: Boolean ->
            viewModel.enableBeamForming(nodeId, isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
        binding.blueVoiceWaveformView.startPlotting()
    }

    override fun onPause() {
        super.onPause()
        binding.blueVoiceWaveformView.stopPlotting()
        volumeBar?.let {
            storeVolumeLevel(it.progress)
        }
        manageMuteButton?.let {
            storeMuteButton(it.isMute)
        }
        manageRecButton?.let {
            if (it.isRec) {
                it.changeState()
            }
        }
        viewModel.stopDemo(nodeId = nodeId)
    }

    override fun onStop() {
        super.onStop()
        audioTrack?.pause()
        audioTrack?.flush()
    }

    private fun restoreGuiStatus() {
        volumeBar?.progress = getVolumeLevel()
        if (getMuteStatus() != manageMuteButton?.isMute) manageMuteButton?.changeState()
    }

    private fun getVolumeLevel(): Int {
        return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.let {
            requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY, Context.MODE_PRIVATE)
                .getInt(VOLUME_LEVEL_KEY, it)
        } ?: -1
    }

    private fun getMuteStatus(): Boolean {
        return requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY, Context.MODE_PRIVATE)
            .getBoolean(IS_MUTE_KEY, false)
    }


    private fun storeMuteButton(isMute: Boolean) {
        requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY, Context.MODE_PRIVATE).edit()
            .putBoolean(IS_MUTE_KEY, isMute)
            .apply()
    }

    private fun storeVolumeLevel(volumeLevel: Int) {
        requireActivity().getSharedPreferences(
            BVCONF_PREFIX_KEY,
            Context.MODE_PRIVATE
        ).edit()
            .putInt(VOLUME_LEVEL_KEY, volumeLevel)
            .apply()
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

    private fun initializeAudioDump(name: String) {
        audioWavDump = AudioRecorder(
            requireActivity(),
            name
        )
    }

    private fun initializeVolumeBar(maxVolume: Int) {
        volumeBar?.max = maxVolume
        volumeBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, newVolumeLevel: Int, b: Boolean) {
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolumeLevel, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                /** NOOP **/
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                /** NOOP **/
            }
        })
        volumeBar?.progress = maxVolume / 2
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
        binding.blueVoiceWaveformView.updateAudioData(sample)
    }

    private fun recAudio(sample: ShortArray) {
        if (manageRecButton?.isRec == true) {
            audioWavDump?.writeSample(sample)
        }
    }

    inner class ManageMuteButton internal constructor(private val mMuteButton: ImageButton) :
        View.OnClickListener {
        var isMute = false
            private set

        override fun onClick(view: View) {
            changeState()
        }

        fun changeState() {
            isMute = !isMute
            if (isMute) muteAudio() else unMuteAudio()
        }

        private fun muteAudio() {
            mMuteButton.setImageResource(R.drawable.ic_volume_off_black_32dp)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            binding.blueVoiceVolumeValue.isEnabled = false
        }

        private fun unMuteAudio() {
            mMuteButton.setImageResource(R.drawable.ic_volume_up_black_32dp)
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                binding.blueVoiceVolumeValue.progress,
                0
            )
            binding.blueVoiceVolumeValue.isEnabled = true
        }

        init {
            mMuteButton.setOnClickListener(this)
        }
    }

    inner class ManageRecButton internal constructor(private val mRecButton: ImageButton) :
        View.OnClickListener {
        var isRec = false
            private set

        override fun onClick(view: View) {
            changeState()
        }

        fun changeState() {
            isRec = !isRec
            if (isRec) startRec() else stopRec()
        }

        private fun startRec() {
            mRecButton.setImageResource(R.drawable.ic_stop)

            audioWavDump?.startRec()
        }

        private fun stopRec() {
            mRecButton.setImageResource(R.drawable.ic_record)

            audioWavDump?.stopRec()
        }

        init {
            mRecButton.setOnClickListener(this)
        }
    }
}