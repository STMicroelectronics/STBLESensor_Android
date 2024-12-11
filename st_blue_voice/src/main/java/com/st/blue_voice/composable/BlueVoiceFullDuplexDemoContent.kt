package com.st.blue_voice.composable

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.R
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.Locale

private var mAudioTrack: AudioTrack? = null
private var audioManager: AudioManager? = null

@Composable
fun BlueVoiceFullDuplexDemoContent(
    modifier: Modifier = Modifier,
    viewModel: BlueVoiceViewModel,
    nodeId: String
) {


    //We plot only the first sample of each chunk
    var sampleReceived by remember {
        mutableStateOf<Short>(0)
    }

    //We plot only the first sample of each chunk
    var sampleSent by remember {
        mutableStateOf<Short>(0)
    }

    var sendingAudio by remember {
        mutableStateOf(false)
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

            initAudioTrack(decodeParams)
        }.onEach {
            if(it.isNotEmpty()) {
                sampleReceived = it[0]
            }
        }.map { it.toByteArray() }
            .collect {
                playAudio(it)
            }
    }

    LaunchedEffect(key1 = sendingAudio) {
        if (sendingAudio) {
            viewModel.startAudioRecord(nodeId)
                .collect {
                    it.data?.let { data ->
                        sampleSent = data[0]
                    }
                }
        }
    }

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                mAudioTrack?.pause()
                mAudioTrack?.flush()
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = LocalDimensions.current.paddingNormal,
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {
        //Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f),
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
                Text(
                    text = "Codec: $codecType",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Sampling Rate: $frequency",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        //Audio Sent
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal),
                        painter = painterResource(
                            R.drawable.ic_baseline_arrow_forward_24
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )

                    Text(text = "Audio Sent", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (sendingAudio) "Stop" else "Start",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        modifier = Modifier.padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                        checked = sendingAudio, onCheckedChange = {
                            sendingAudio = it
                            if (!sendingAudio) {
                                viewModel.stopAudioRecord(nodeId)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedTrackColor = Grey6,
                            disabledUncheckedTrackColor = Grey3
                        )
                    )

                }
                WaveFormPlotView(sample = sampleSent)
            }

        }

        //Audio Received
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal),
                        painter = painterResource(
                            R.drawable.ic_baseline_arrow_back_24
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )

                    Text(text = "Audio Received", style = MaterialTheme.typography.titleMedium)

                }
                WaveFormPlotView(sample = sampleReceived)
            }
        }

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