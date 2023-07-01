/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.motion_algorithms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsSpinner
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.extended.motion_algorithm.AlgorithmType
import com.st.blue_sdk.features.extended.motion_algorithm.DesktopType
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithmInfo
import com.st.blue_sdk.features.extended.motion_algorithm.PoseType
import com.st.blue_sdk.features.extended.motion_algorithm.VerticalContextType
import com.st.core.ARG_NODE_ID
import com.st.motion_algorithms.databinding.MotionAlgorithmsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MotionAlgorithmsFragment : Fragment() {

    private val viewModel: MotionAlgorithmsViewModel by viewModels()
    private lateinit var binding: MotionAlgorithmsFragmentBinding
    private lateinit var nodeId: String

    private val DEFAULT_ALGO = AlgorithmType.PoseEstimation


    private lateinit var mAlgoSelector: AbsSpinner
    private lateinit var mResultIcon: ImageView
    private lateinit var mResultLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = MotionAlgorithmsFragmentBinding.inflate(inflater, container, false)

        mAlgoSelector = binding.motionAlgoSelector
        mResultIcon = binding.motionAlgoResultIcon
        mResultLabel = binding.motionAlgoResultLabel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set up Algorithms selection
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.motionAlgo_algoValues,
            android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mAlgoSelector.adapter = adapter


        mAlgoSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                val algoType = AlgorithmType.getAlgorithmType(position.toShort())
                if (algoType != AlgorithmType.Unknown) {
                    viewModel.setAlgorithmTypeCommand(nodeId, algoType)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /** NOOP **/
            }
        }

        mAlgoSelector.setSelection(AlgorithmType.getAlgorithmCode(DEFAULT_ALGO).toInt())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.motAlgData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: MotionAlgorithmInfo) {
        val algorithmType = it.algorithmType.value
        val statusType = it.statusType.value

        when (algorithmType) {
            AlgorithmType.PoseEstimation -> {
                mResultIcon.setImageResource(poseContextIcon(PoseType.getPoseType(statusType)))
                mResultLabel.setText(poseContextString(PoseType.getPoseType(statusType)))
            }

            AlgorithmType.DesktopTypeDetection -> {
                mResultIcon.setImageResource(desktopIcon(DesktopType.getDesktopType(statusType)))
                mResultLabel.setText(desktopString(DesktopType.getDesktopType(statusType)))
            }

            AlgorithmType.VerticalContext -> {
                mResultIcon.setImageResource(
                    verticalContextIcon(
                        VerticalContextType.getVerticalContextType(
                            statusType
                        )
                    )
                )
                mResultLabel.setText(
                    verticalContextString(
                        VerticalContextType.getVerticalContextType(
                            statusType
                        )
                    )
                )
            }
            //AlgorithmType.Unknown
            else -> {
                /** NOOP **/
            }
        }
    }

    @DrawableRes
    private fun poseContextIcon(type: PoseType): Int {
        return when (type) {
            PoseType.Unknown -> R.drawable.motion_algo_unknown
            PoseType.Sitting -> R.drawable.motion_algo_pose_sitting
            PoseType.Standing -> R.drawable.motion_algo_pose_standing
            PoseType.LyingDown -> R.drawable.motion_algo_pose_lying_down
        }
    }

    @StringRes
    private fun poseContextString(type: PoseType): Int {
        return when (type) {
            PoseType.Unknown -> R.string.motionAlgo_unknown
            PoseType.Sitting -> R.string.motionAlgo_pose_sitting
            PoseType.Standing -> R.string.motionAlgo_pose_standing
            PoseType.LyingDown -> R.string.motionAlgo_pose_layingDown
        }
    }

    @DrawableRes
    private fun desktopIcon(type: DesktopType): Int {
        return when (type) {
            DesktopType.Unknown -> R.drawable.motion_algo_unknown
            DesktopType.Sitting -> R.drawable.desktop_type_sitting
            DesktopType.Standing -> R.drawable.desktop_type_standing
        }
    }

    @StringRes
    private fun desktopString(type: DesktopType): Int {
        return when (type) {
            DesktopType.Unknown -> R.string.motionAlgo_unknown
            DesktopType.Sitting -> R.string.motionAlgo_desktop_sitting
            DesktopType.Standing -> R.string.motionAlgo_desktop_standing
        }
    }

    @DrawableRes
    private fun verticalContextIcon(type: VerticalContextType): Int {
        return when (type) {
            VerticalContextType.Unknown -> R.drawable.motion_algo_unknown
            VerticalContextType.Floor -> R.drawable.motion_algo_vertical_floor
            VerticalContextType.UpDown -> R.drawable.motion_algo_vertical_updown
            VerticalContextType.Stairs -> R.drawable.motion_algo_vertical_stairs
            VerticalContextType.Elevator -> R.drawable.motion_algo_vertical_elevator
            VerticalContextType.Escalator -> R.drawable.motion_algo_vertical_escalator
        }
    }

    @StringRes
    private fun verticalContextString(type: VerticalContextType): Int {
        return when (type) {
            VerticalContextType.Unknown -> R.string.motionAlgo_unknown
            VerticalContextType.Floor -> R.string.motionAlgo_vertical_floor
            VerticalContextType.UpDown -> R.string.motionAlgo_vertical_upDown
            VerticalContextType.Stairs -> R.string.motionAlgo_vertical_stairs
            VerticalContextType.Elevator -> R.string.motionAlgo_vertical_elevator
            VerticalContextType.Escalator -> R.string.motionAlgo_vertical_escalator
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
