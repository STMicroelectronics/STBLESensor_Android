package com.st.blue_voice.beamforming

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.beam_forming.BeamDirectionType
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.R
import com.st.blue_voice.Utils.SquareImageView
import com.st.blue_voice.Utils.WaveformView
import com.st.blue_voice.databinding.FragmentBeamformingBinding
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@AndroidEntryPoint
class BeamFormingFragment: Fragment() {
    companion object {
        private val DEFAULT_DIRECTION = BeamDirectionType.Right
    }

    private lateinit var binding: FragmentBeamformingBinding
    private lateinit var nodeId: String

    private lateinit var mWaveformView: WaveformView
    private var mCurrentDirId = DEFAULT_DIRECTION

    private val viewModel: BlueVoiceViewModel by viewModels()
    private val mButtonToDirection: MutableMap<CompoundButton, BeamDirectionType> = HashMap()

    private lateinit var mBoard: SquareImageView
    private lateinit var mTopButton: CompoundButton
    private lateinit var mTopRightButton: CompoundButton
    private lateinit var mRightButton: CompoundButton
    private lateinit var mBottomRightButton: CompoundButton
    private lateinit var mBottomButton: CompoundButton
    private lateinit var mBottomLeftButton: CompoundButton
    private lateinit var mLeftButton: CompoundButton
    private lateinit var mTopLeftButton: CompoundButton

    private var mAudioTrack: AudioTrack?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FragmentBeamformingBinding.inflate(inflater, container, false)

        mWaveformView = binding.blueVoiceWaveformView

        mTopButton =  binding.radioBFdirTop
        mTopRightButton = binding.radioBFdirTopRight
        mRightButton = binding.radioBFdirRight
        mBottomRightButton = binding.radioBFdirBottomRight
        mBottomButton = binding.radioBFdirBottom
        mBottomLeftButton = binding.radioBFdirBottomLeft
        mLeftButton = binding.radioBFdirLeft
        mTopLeftButton = binding.radioBFdirTopLeft

        //Set the button,direction Map
        mButtonToDirection[mTopButton] =  BeamDirectionType.Top
        mButtonToDirection[mTopRightButton] =  BeamDirectionType.TopRight
        mButtonToDirection[mRightButton] =  BeamDirectionType.Right
        mButtonToDirection[mBottomRightButton] =  BeamDirectionType.BottomRight
        mButtonToDirection[mBottomButton] =  BeamDirectionType.Bottom
        mButtonToDirection[mBottomLeftButton] =  BeamDirectionType.BottomLeft
        mButtonToDirection[mLeftButton] =  BeamDirectionType.Left
        mButtonToDirection[mTopLeftButton] =  BeamDirectionType.TopLeft

        mBoard = binding.beamformingBoardImage
        when(viewModel.getBoardType(nodeId)) {
            Boards.Model.BLUE_COIN ->  {
                mBoard.setImageResource(R.drawable.ic_board_bluecoin_bg)
                show4MicConfiguration()
            }

            Boards.Model.NUCLEO,
            Boards.Model.NUCLEO_F401RE,
            Boards.Model.NUCLEO_L476RG,
            Boards.Model.NUCLEO_L053R8,
            Boards.Model.NUCLEO_F446RE ->  {
                mBoard.setImageResource(R.drawable.ic_board_nucleo_bg)
                show2MicConfiguration()
            }

            else -> mBoard.setImageResource(R.drawable.mic_on)
        }

        //when the view is displayed
        binding.root.post { alignDirectionButtonOnCircle() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioData(nodeId).onStart {
                    val decodeParams = viewModel.getAudioDecodeParams(nodeId)
                    initAudioTrack(decodeParams)
                }.onEach {
                    plotAudio(it)
                }.map { it.toByteArray() }
                    .collect {
                        playAudio(it)
                    }
            }
        }

        viewModel.beamFormingEnabled.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                mCurrentDirId = DEFAULT_DIRECTION
                viewModel.enableBeamForming(nodeId, checked = true, useStrongBeamFormingAlgorithm = true)
                setBeamFormingButton(mCurrentDirId)

                setupOnCheckedDirListener()
            }
        })
    }

    private fun setupOnCheckedDirListener() {
        //Set the on Checked change listener
        mTopButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mTopRightButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mRightButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mBottomRightButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mBottomButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mBottomLeftButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mLeftButton.setOnCheckedChangeListener(mOnDirectionSelected)
        mTopLeftButton.setOnCheckedChangeListener(mOnDirectionSelected)
    }


    private val mOnDirectionSelected =
        CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, isSelected: Boolean ->
            if (!isSelected) return@OnCheckedChangeListener
            val buttonDir= mButtonToDirection[compoundButton]
            if(buttonDir!=null) {
                setBeamFormingButton(buttonDir)
            }
        }

    private fun setBeamFormingButton(newDirection: BeamDirectionType) {
        val selectedButton = mButtonToDirection.entries.find{it.value== newDirection}?.key
        if (selectedButton != null) {
            mCurrentDirId = newDirection
            viewModel.setBeamFormingDirection(nodeId,mCurrentDirId)
            selectedButton.isChecked = true
            deselectAllButtonDifferentFrom(selectedButton)
        }
    }

    private fun deselectAllButtonDifferentFrom(selected: CompoundButton) {
        for (button in mButtonToDirection.keys) {
            if (button !== selected) {
                button.isChecked = false
            }
        }
    }

    private fun playAudio(sample: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioTrack?.write(
                sample, 0, sample.size,
                AudioTrack.WRITE_NON_BLOCKING
            )
        }
    }

    private fun plotAudio(sample: ShortArray) {
        mWaveformView.updateAudioData(sample)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
        mWaveformView.startPlotting()
    }

    override fun onPause() {
        super.onPause()
        mWaveformView.stopPlotting()
        viewModel.enableBeamForming(nodeId=nodeId, checked = false)
        viewModel.stopDemo(nodeId = nodeId)
    }

    override fun onStop() {
        super.onStop()
        mAudioTrack?.pause()
        mAudioTrack?.flush()
    }


    private fun show2MicConfiguration() {
        mTopButton.visibility = View.GONE
        mTopRightButton.visibility = View.GONE
        mRightButton.visibility = View.VISIBLE
        mBottomRightButton.visibility = View.GONE
        mBottomButton.visibility = View.GONE
        mBottomLeftButton.visibility = View.GONE
        mLeftButton.visibility = View.VISIBLE
        mTopLeftButton.visibility = View.GONE
    }

    private fun show4MicConfiguration() {
        mTopButton.visibility = View.VISIBLE
        mTopRightButton.visibility = View.VISIBLE
        mRightButton.visibility = View.VISIBLE
        mBottomRightButton.visibility = View.VISIBLE
        mBottomButton.visibility = View.VISIBLE
        mBottomLeftButton.visibility = View.VISIBLE
        mLeftButton.visibility = View.VISIBLE
        mTopLeftButton.visibility = View.VISIBLE
    }


    private fun alignDirectionButtonOnCircle() {
        //the image is squared
        val imageSize = mBoard.height.toFloat()
        //the radio button is square
        val buttonHalfSize = (mRightButton.width / 2).toFloat()
        val r = (mRightButton.x - mLeftButton.x) / 2
        val margin2 = (imageSize / 2 - r / sqrt(2.0) - buttonHalfSize).toInt()
        var relBtn: RelativeLayout.LayoutParams = mTopRightButton.layoutParams as RelativeLayout.LayoutParams
        relBtn.topMargin = margin2
        relBtn.marginEnd = margin2
        //update the parameters
        mTopRightButton.layoutParams = relBtn
        relBtn = mBottomRightButton.layoutParams as RelativeLayout.LayoutParams
        relBtn.bottomMargin = margin2
        relBtn.marginEnd = margin2
        //update the parameters
        mBottomRightButton.layoutParams = relBtn
        relBtn = mBottomLeftButton.layoutParams as RelativeLayout.LayoutParams
        relBtn.bottomMargin = margin2
        relBtn.marginStart = margin2
        //update the parameters
        mBottomLeftButton.layoutParams = relBtn
        relBtn = mTopLeftButton.layoutParams as RelativeLayout.LayoutParams
        relBtn.topMargin = margin2
        relBtn.marginStart = margin2
        //update the parameters
        mTopLeftButton.layoutParams = relBtn
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
}