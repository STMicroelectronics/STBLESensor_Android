/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.extended.euler_angle.EulerAngleInfo
import com.st.core.ARG_NODE_ID
import com.st.level.databinding.LevelFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.sin

@AndroidEntryPoint
class LevelDemoFragment : Fragment() {

    private val viewModel: LevelViewModel by viewModels()

    private lateinit var binding: LevelFragmentBinding

    private lateinit var nodeId: String

    private var widthImage = 0

    private var spinnerPossibility: List<String> = listOf("Pitch/Roll", "Pitch", "Roll")
    private var currentSpinnerPosition = 0

    private var mZeroPitch = 0f
    private var mZeroRoll = 0f

    private var mPitch = 0f
    private var mRoll = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LevelFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.levelData.collect {
                    updateLevelView(it)
                }
            }
        }

        //Circle Radius
        widthImage = binding.levelOffset2.drawable.intrinsicWidth / 2

        binding.levelButtonSetZero.setOnClickListener {
            mZeroPitch = mPitch
            mZeroRoll = mRoll
        }

        binding.levelButtonResetZero.setOnClickListener {
            mZeroPitch = 0f
            mZeroRoll = 0f
        }

        //Creating the ArrayAdapter instance having the country list
        val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerPossibility
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //Setting the ArrayAdapter data on the Spinner
        binding.levelSpinner.adapter = adapter
        binding.levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                currentSpinnerPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.valYaw.setOnClickListener {
            if (binding.valYaw.alpha == 0f) {
                binding.valYaw.alpha = 1f
            } else {
                binding.valYaw.alpha = 0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    private fun updateLevelView(angleInfo: EulerAngleInfo) {

        val yaw: Float = angleInfo.yaw.value
        mPitch = angleInfo.pitch.value
        mRoll = angleInfo.roll.value

        val pitch = mPitch - mZeroPitch
        val roll = mRoll - mZeroRoll

        val deltaY: Float
        val deltaX: Float

        when (currentSpinnerPosition) {
            0 -> {
                deltaY = (widthImage * sin(Math.toRadians(roll.toDouble()))).toFloat()
                deltaX = (widthImage * sin(Math.toRadians(pitch.toDouble()))).toFloat()
            }

            1 -> {
                deltaY = 0f
                deltaX = (widthImage * sin(Math.toRadians(pitch.toDouble()))).toFloat()
            }

            else -> {
                deltaY = (widthImage * sin(Math.toRadians(roll.toDouble()))).toFloat()
                deltaX = 0f
            }
        }
        binding.levelOffset1.translationX = deltaX
        binding.levelOffset1.translationY = deltaY

        binding.lineRoll.rotation = roll
        binding.linePitch.rotation = pitch

        binding.valPitch.text = resources.getString(R.string.level_offset_angle_format, pitch)
        binding.valYaw.text = resources.getString(R.string.level_offset_angle_format, yaw)
        binding.valRoll.text = resources.getString(R.string.level_offset_angle_format, roll)

    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
