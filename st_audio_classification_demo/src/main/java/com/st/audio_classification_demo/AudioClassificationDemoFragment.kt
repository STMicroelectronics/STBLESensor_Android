/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.audio_classification_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.audio_classification_demo.audio_view.AudioView
import com.st.audio_classification_demo.databinding.AudioClassificationDemoFragmentBinding
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AudioClassificationDemoFragment : Fragment() {

    private val viewModel: AudioClassificationDemoViewModel by viewModels()
    private lateinit var binding: AudioClassificationDemoFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mWaitView: View

    // Scene Classification view
    private lateinit var mSceneClassificationView: AudioView

    // Baby Crying view
    private lateinit var mBabyCryingView: AudioView

    private var mAllView : MutableList<AudioView> = mutableListOf()

    private var mCurrentAlgorithm = 0
    private var mCurrentAudioClass = AudioClassType.Unknown

    private fun getViewFromAlgoId(algorithmId: Int): AudioView? {
        when (algorithmId) {
            0 -> return mSceneClassificationView
            1 -> return mBabyCryingView
        }
        return null
    }

    private fun showActivity(algorithmId: Short, type: AudioClassType) {
        val activeView = getViewFromAlgoId(algorithmId.toInt())
        mWaitView.visibility = View.GONE
        for (view in mAllView) {
            if (activeView === view) {
                view.visibility = View.VISIBLE
                view.setAudioImage(type)
            } else {
                view.visibility = View.GONE
            }
        }
        mCurrentAudioClass = type
        mCurrentAlgorithm = algorithmId.toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = AudioClassificationDemoFragmentBinding.inflate(inflater, container, false)

        mWaitView = binding.audioWaitingData
        mBabyCryingView = binding.audioViewBabyCrying
        mSceneClassificationView = binding.audioViewSceneClassification


        mAllView.add(mSceneClassificationView)
        mAllView.add(mBabyCryingView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioClassificationData.collect {
                    showActivity(it.algorithm.value,it.classification.value)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
