package com.st.blue_voice.composable

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import com.st.blue_sdk.features.beam_forming.BeamDirectionType
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.R
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

private val DEFAULT_DIRECTION = BeamDirectionType.Right

private var mAudioTrack: AudioTrack? = null

@Composable
fun BeamFormingDemoContent(
    modifier: Modifier = Modifier,
    viewModel: BlueVoiceViewModel,
    nodeId: String
) {
    var lowerDimension by remember { mutableFloatStateOf(0f) }
    val directionsList by remember {
        mutableStateOf(
            when (viewModel.getBoardType(nodeId)) {
                Boards.Model.BLUE_COIN ->
                    listOf(
                        Pair(BeamDirectionType.Top, true),
                        Pair(BeamDirectionType.TopRight, true),
                        Pair(BeamDirectionType.Right, true),
                        Pair(BeamDirectionType.BottomRight, true),
                        Pair(BeamDirectionType.Bottom, true),
                        Pair(BeamDirectionType.BottomLeft, true),
                        Pair(BeamDirectionType.Left, true),
                        Pair(BeamDirectionType.TopLeft, true)
                    ).map { Pair(it.first.toString(), it.second) }

                Boards.Model.NUCLEO,
                Boards.Model.NUCLEO_F401RE,
                Boards.Model.NUCLEO_L476RG,
                Boards.Model.NUCLEO_L053R8,
                Boards.Model.NUCLEO_U575ZIQ,
                Boards.Model.NUCLEO_U5A5ZJQ,
                Boards.Model.NUCLEO_F446RE -> listOf(
                    Pair(BeamDirectionType.Top, false),
                    Pair(BeamDirectionType.TopRight, false),
                    Pair(BeamDirectionType.Right, true),
                    Pair(BeamDirectionType.BottomRight, false),
                    Pair(BeamDirectionType.Bottom, false),
                    Pair(BeamDirectionType.BottomLeft, false),
                    Pair(BeamDirectionType.Left, true),
                    Pair(BeamDirectionType.TopLeft, false)
                ).map { Pair(it.first.toString(), it.second) }

                else -> listOf(
                    Pair(BeamDirectionType.Top, false),
                    Pair(BeamDirectionType.TopRight, false),
                    Pair(BeamDirectionType.Right, false),
                    Pair(BeamDirectionType.BottomRight, false),
                    Pair(BeamDirectionType.Bottom, false),
                    Pair(BeamDirectionType.BottomLeft, false),
                    Pair(BeamDirectionType.Left, false),
                    Pair(BeamDirectionType.TopLeft, false)
                ).map { Pair(it.first.toString(), it.second) }
            })
    }

    var selectedDirection by remember { mutableStateOf(DEFAULT_DIRECTION) }

    //We plot only the first sample of each chunk
    var sample by remember {
        mutableStateOf<Short>(0)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.audioData(nodeId).onStart {
            val decodeParams = viewModel.getAudioDecodeParams(nodeId)
            initAudioTrack(decodeParams)
        }.onEach {
            sample = it[0]
        }.map { it.toByteArray() }
            .collect {
                playAudio(it)
            }
    }

    //This is true 2 seconds after having enabled the notifications
    val beamFormingEnabled by viewModel.beamFormingEnabled.observeAsState(false)

    //Enable the BeamForming
    LaunchedEffect(key1 = beamFormingEnabled) {
        if (beamFormingEnabled) {
            viewModel.enableBeamForming(
                nodeId,
                checked = true,
                useStrongBeamFormingAlgorithm = true
            )
        }
    }

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP ->  {
                mAudioTrack?.pause()
                mAudioTrack?.flush()
            }
            else -> Unit
        }
    }

    val configuration = LocalConfiguration.current

    val smallScreen by remember(key1 = configuration) {
        derivedStateOf {
            val screenHeight = configuration.screenHeightDp
            screenHeight < 800
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
        //Select the Beamforming Direction
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
                    .onSizeChanged { intSize ->
                        lowerDimension = if (intSize.width < intSize.height) {
                            intSize.width.toFloat() * 0.85f
                        } else {
                            intSize.height.toFloat() * 0.85f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                //Put the BoardImage in the center
                Icon(
                    modifier = Modifier
                        .size(size = if (smallScreen) LocalDimensions.current.imageMedium else LocalDimensions.current.imageLarge),
                    painter = painterResource(
                        findBoardImage(viewModel.getBoardType(nodeId))
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )


                if (beamFormingEnabled) {
                    Text(
                        modifier = Modifier.align(Alignment.TopStart),
                        text = "Select one Direction",
                        color = SuccessText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                //Put the Radio Buttons around the images
                CircularRadioButtonView(
                    radius = lowerDimension / 2,
                    values = if (beamFormingEnabled) directionsList else directionsList.map {
                        //This is for not displaying the Radio buttons until beamForming is enabled
                        Pair(
                            it.first,
                            false
                        )
                    },
                    initialValue = selectedDirection.toString(),
                    onValueSelected = {
                        selectedDirection = BeamDirectionType.valueOf(it)
                        viewModel.setBeamFormingDirection(nodeId, selectedDirection)
                    })
            }
        }

        //Plot the Audio Signal
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            WaveFormPlotView(name= "Audio In", sample = sample)
        }
    }
}

private fun findBoardImage(model: Boards.Model): Int {
    return when (model) {
        Boards.Model.BLUE_COIN ->
            R.drawable.ic_board_bluecoin_bg

        Boards.Model.NUCLEO,
        Boards.Model.NUCLEO_F401RE,
        Boards.Model.NUCLEO_L476RG,
        Boards.Model.NUCLEO_L053R8,
        Boards.Model.NUCLEO_U575ZIQ,
        Boards.Model.NUCLEO_U5A5ZJQ,
        Boards.Model.NUCLEO_F446RE -> R.drawable.ic_board_nucleo_bg

        else -> R.drawable.mic_on
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
