/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.motion_intensity

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.res.Resources
import android.content.res.TypedArray
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
import com.st.blue_sdk.features.motion_intensity.MotionIntensityInfo
import com.st.core.ARG_NODE_ID
import com.st.motion_intensity.databinding.MotionIntensityFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MotionIntensityFragment : Fragment() {

    private val viewModel: MotionIntensityViewModel by viewModels()
    private lateinit var binding: MotionIntensityFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mIntensityValueFormat: String
    private lateinit var mIntensityNeedle: ImageView
    private lateinit var mNeedleOffset: TypedArray
    private lateinit var mIntensityValue: TextView

    private lateinit var mRotationAnim: ValueAnimator

    private var mLastValue: Short = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string \"nodeId\" arguments")

        binding = MotionIntensityFragmentBinding.inflate(inflater, container, false)

        mIntensityValue = binding.motionIdIntensityValue
        mIntensityNeedle = binding.motionIdNeedleImage

        val res: Resources = binding.root.resources
        mNeedleOffset = res.obtainTypedArray(R.array.motionId_angleOffset)

        mIntensityValueFormat = res.getString(R.string.motionId_valueTextFormat)

        mRotationAnim = AnimatorInflater.loadAnimator(
            activity,
            R.animator.needle_rotation
        ) as ValueAnimator

        mRotationAnim.setTarget(mIntensityNeedle)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.motIntData.collect {
                    updateGui(it)
                }
            }
        }

    }

    private fun updateGui(it: MotionIntensityInfo) {
        val newValue = it.intensity.value

        if (newValue != mLastValue) {
            mLastValue = newValue
            val rotationOffset = mNeedleOffset.getFloat(newValue.toInt(), 0.0f)
            val valueStr = String.format(mIntensityValueFormat, newValue)

            mIntensityValue.text = valueStr
            if (mRotationAnim.isRunning) {
                mRotationAnim.pause()
            }

            val currentPosition = mIntensityNeedle.rotation
            mRotationAnim.setFloatValues(currentPosition, rotationOffset)
            mRotationAnim.start()
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
