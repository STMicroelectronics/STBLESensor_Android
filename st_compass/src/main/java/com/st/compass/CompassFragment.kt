/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.st.compass.databinding.CompassFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompassFragment : Fragment() {

    private val viewModel: CompassViewModel by viewModels()
    private val navArgs: CompassFragmentArgs by navArgs()
    private lateinit var binding: CompassFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CompassFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.compassStartCalibration.setOnClickListener {
            findNavController().navigate(
                CompassFragmentDirections.actionCompassFragmentToCalibrationDialog(navArgs.nodeId)
            )
        }

        viewModel.compassInfo.observe(viewLifecycleOwner) { compassInfo ->
            val angle = compassInfo.angle.value
            binding.compassDirection.text = String.format(getOrientationName(angle))
            binding.compassNeedle.rotation = angle
            binding.compassAngle.text =
                String.format(getString(R.string.st_compass_angleFormatter), angle)
        }

        viewModel.calibrationStatus.observe(viewLifecycleOwner) { calibrationStatus ->
            binding.compassStartCalibration.setImageResource(
                if (calibrationStatus)
                    R.drawable.compass_calibration_calibrated
                else
                    R.drawable.compass_calibration_uncalibrated
            )
        }
    }

    private fun getOrientationName(angle: Float): String {
        val orientations =
            requireContext().resources.getStringArray(R.array.st_compass_orientationArray)
        val nOrientation: Int = orientations.size
        val section = 360.0f / nOrientation
        val relativeAngle = angle - section / 2 + 360.0f
        val index = (relativeAngle / section).toInt() + 1
        return orientations[index % nOrientation]
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(navArgs.nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(navArgs.nodeId)
    }
}
