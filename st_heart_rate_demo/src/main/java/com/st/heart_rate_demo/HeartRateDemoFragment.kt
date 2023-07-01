/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.heart_rate_demo

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.external.std.HeartRateInfo
import com.st.core.ARG_NODE_ID
import com.st.heart_rate_demo.databinding.HeartRateDemoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeartRateDemoFragment : Fragment() {

    private val viewModel: HeartRateDemoViewModel by viewModels()
    private lateinit var binding: HeartRateDemoFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mHeartRateLabel: TextView
    private lateinit var mEnergyExtendedLabel: TextView
    private lateinit var mRRIntervalLabel: TextView
    private lateinit var mHeartImage: ImageView

    private lateinit var mPulseAnim: AnimatorSet

    private lateinit var sToGrayScale: ColorMatrixColorFilter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = HeartRateDemoFragmentBinding.inflate(inflater, container, false)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        sToGrayScale = ColorMatrixColorFilter(colorMatrix)

        mHeartRateLabel = binding.heartRateLabel
        mRRIntervalLabel = binding.rrIntervalLabel
        mEnergyExtendedLabel = binding.energyExtendedLabel

        mHeartImage = binding.heartImage

        mHeartImage.setOnClickListener {
            viewModel.readFeature(nodeId = nodeId)
        }

        mPulseAnim = AnimatorInflater.loadAnimator(
            activity,
            R.animator.pulse
        ) as AnimatorSet
        mPulseAnim.setTarget(mHeartImage)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.heartData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: HeartRateInfo) {
        val heartRate = it.heartRate.value
        val energy = it.energyExpended.value
        val rrInterval = it.rrInterval.value

        if(heartRate<=0) {
            mHeartRateLabel.text = ""
            mHeartImage.colorFilter = sToGrayScale
        } else {
            mHeartRateLabel.text = getString(R.string.heartRateDataFormat, heartRate,it.heartRate.unit)
            mHeartImage.colorFilter = null
            if(energy>0) {
                mEnergyExtendedLabel.text =
                    getString(R.string.energyExpendedDataFormat, energy, it.energyExpended.unit)
            }
            if(!rrInterval.isNaN()) {
                mRRIntervalLabel.text = getString(R.string.rrIntervalDataFormat, rrInterval,it.rrInterval.unit)
            }
            if(!mPulseAnim.isRunning) {
                mPulseAnim.start()
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
