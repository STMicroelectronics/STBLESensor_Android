/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pedometer

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.pedometer.PedometerInfo
import com.st.core.ARG_NODE_ID
import com.st.pedometer.databinding.PedometerFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PedometerFragment : Fragment() {

    private val viewModel: PedometerViewModel by viewModels()
    private lateinit var binding: PedometerFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mStepsCount: TextView
    private lateinit var mStepsFrequency: TextView

    private var mFlipPosition = false
    private lateinit var mFlipImageLeft: AnimatorSet
    private lateinit var mFlipImageRight: AnimatorSet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = PedometerFragmentBinding.inflate(inflater, container, false)

        mStepsCount = binding.stepCount
        mStepsFrequency = binding.stepFrequency
        val pedometerImage = binding.stepImage

        pedometerImage.setOnClickListener {
            viewModel.readFeature(nodeId)
        }

        mFlipImageLeft = AnimatorInflater.loadAnimator(
            activity,
            R.animator.flip_image_right
        ) as AnimatorSet
        mFlipImageLeft.setTarget(pedometerImage)

        mFlipImageRight = AnimatorInflater.loadAnimator(
            activity,
            R.animator.flip_image_left
        ) as AnimatorSet
        mFlipImageRight.setTarget(pedometerImage)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stepData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: PedometerInfo) {
        val steps = it.steps.value
        val frequency = it.frequency.value

        val stepCountStr = String.format("%1d steps", steps)
        val stepFreqStr = String.format(" %1d %2s", frequency, it.frequency.unit)

        //Set Strings
        mStepsCount.text = stepCountStr
        mStepsFrequency.text = stepFreqStr

        //Animate the Icon
        if (mFlipImageRight.isRunning || mFlipImageLeft.isRunning) {
            return
        }

        if (mFlipPosition) {
            mFlipImageLeft.start()
        } else {
            mFlipImageRight.start()
        }
        mFlipPosition = !mFlipPosition
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
