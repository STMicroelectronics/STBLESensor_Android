package com.st.blue_voice.composable

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.R
import com.st.blue_voice.utils.AudioRecorder
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.Locale

private var mAudioTrack: AudioTrack? = null
private var audioManager: AudioManager? = null


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueVoiceDemoContent(
    modifier: Modifier = Modifier,
    viewModel: BlueVoiceViewModel,
    nodeId: String
) {

    var audioWavDump by remember {
        mutableStateOf<AudioRecorder?>(null)
    }

    var isMute by remember {
        mutableStateOf(false)
    }

    var isRecording by remember {
        mutableStateOf(false)
    }

    val beamFormingEnabled by viewModel.beamFormingEnabled.observeAsState(false)

    var beamFormingSelected by remember {
        mutableStateOf(true)
    }

    var maxVolume by remember {
        //Start with Max Volume = 100
        mutableFloatStateOf(100f)
    }
    var currentVolume by remember {
        //Start with the half of the max value
        mutableFloatStateOf(50f)
    }

    //We plot only the first sample of each chunk
    var sample by remember {
        mutableStateOf<Short>(0)
    }

    var frequency by remember { mutableStateOf("") }
    var codecType by remember { mutableStateOf("") }

    val context = LocalContext.current


    LaunchedEffect(key1 = Unit) {
        viewModel.audioData(nodeId).onStart {
            val decodeParams = viewModel.getAudioDecodeParams(nodeId)

            frequency =
                String.format(
                    Locale.getDefault(), "%d kHz",
                    decodeParams.samplingFreq / 1000
                )

            codecType =
                viewModel.getAudioCodecType(nodeId).name

            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                ?.let {
                    maxVolume = it.toFloat()
                    currentVolume = maxVolume / 2
                }

            audioWavDump = initializeAudioDump(viewModel.getFeatureName(nodeId).replace("Feature", ""), context)

            initAudioTrack(decodeParams)
        }.onEach {
            sample = it[0]
            if (isRecording) {
                audioWavDump?.writeSample(it)
            }
        }.map { it.toByteArray() }
            .collect {
                playAudio(it)
            }
    }

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP ->  {
                mAudioTrack?.pause()
                mAudioTrack?.flush()
                if (isRecording) {
                    stopRec(audioWavDump)
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {
        //Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LocalDimensions.current.paddingNormal,
                        end = LocalDimensions.current.paddingNormal
                    ),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Codec: $codecType",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.weight(2f))

                    Text(
                        text = "Rec",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal)
                            .clickable {
                                isRecording = !isRecording

                                if (isRecording) {
                                    startRec(audioWavDump)
                                } else {
                                    stopRec(audioWavDump)
                                    audioWavDump?.let {
                                        Toast
                                            .makeText(
                                                context,
                                                "Audio saved on: ${
                                                    audioWavDump!!.fileName.removePrefix(
                                                        audioWavDump!!.fileName.substringBefore("Download")
                                                    )
                                                }",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                            },
                        painter = painterResource(
                            if (isRecording) {
                                R.drawable.ic_stop
                            } else {
                                R.drawable.ic_record
                            }
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }

                Text(
                    text = "Sampling Rate: $frequency",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.elevationNormal
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Volume:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal)
                            .clickable {
                                isMute = !isMute
                                if (isMute) {
                                    muteAudio()
                                } else {
                                    unMuteAudio(currentVolume.toInt())
                                }
                            },
                        painter = painterResource(
                            if (isMute) {
                                R.drawable.ic_volume_off_black_32dp
                            } else {
                                R.drawable.ic_volume_up_black_32dp
                            }
                        ),
                        tint = PrimaryBlue,
                        contentDescription = null
                    )

                    Slider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f),
                        value = currentVolume,
                        enabled = !isMute,
                        onValueChange = { currentVolume = it },
                        colors = SliderDefaults.colors(
                            thumbColor = SecondaryBlue,
                            activeTrackColor = SecondaryBlue,
                            inactiveTrackColor = Grey6
                        ),
                        valueRange =
                        0f..maxVolume,
                        thumb = {
                            SliderLabel(
                                isEnable = !isMute,
                                label = currentVolume.toInt().toString(),
                                minWidth = 50.dp
                            )
                        },
                        onValueChangeFinished = {
                            // launch some business logic update with the state you hold
                            unMuteAudio(currentVolume.toInt())
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        LocalDimensions.current.elevationNormal
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beamforming",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Switch(
                        modifier = Modifier.padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                        enabled = beamFormingEnabled,
                        checked = beamFormingSelected, onCheckedChange = {
                            beamFormingSelected = it
                            viewModel.enableBeamForming(nodeId, it)
                        },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedTrackColor = Grey6,
                            disabledUncheckedTrackColor = Grey3
                        )
                    )
                }
            }
        }

        //Plot
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {

            WaveFormPlotView(name = "Audio In",sample = sample)
        }
    }
}

@Composable
fun SliderLabel(isEnable: Boolean, label: String, minWidth: Dp, modifier: Modifier = Modifier) {
    Surface(
        shape = Shapes.small
    ) {
        Text(
            label,
            textAlign = TextAlign.Center,
            color = Grey0,
            modifier = modifier
                .background(
                    color = if (isEnable) SecondaryBlue else Grey3,
                    shape = Shapes.extraSmall
                )
                .padding(4.dp)
                .defaultMinSize(minWidth = minWidth)
        )
    }
}

private fun initAudioTrack(decodeParams: DecodeParams) {

    val minBufSize = AudioTrack.getMinBufferSize(
        decodeParams.samplingFreq,
        if (decodeParams.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    mAudioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        decodeParams.samplingFreq,
        decodeParams.channels,
        AudioFormat.ENCODING_PCM_16BIT,
        minBufSize,
        AudioTrack.MODE_STREAM
    )

    mAudioTrack?.play()
}

private fun playAudio(sample: ByteArray) {
    mAudioTrack?.write(
        sample, 0, sample.size,
        AudioTrack.WRITE_NON_BLOCKING
    )
}

private fun initializeAudioDump(name: String, context: Context): AudioRecorder? {
    return AudioRecorder(
        context,
        name
    )
}

private fun muteAudio() {
    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
}

private fun unMuteAudio(volume: Int) {
    audioManager?.setStreamVolume(
        AudioManager.STREAM_MUSIC,
        volume,
        0
    )
}

private fun startRec(audioWavDump: AudioRecorder?) {
    audioWavDump?.startRec()
}

private fun stopRec(audioWavDump: AudioRecorder?) {
    audioWavDump?.stopRec()
}