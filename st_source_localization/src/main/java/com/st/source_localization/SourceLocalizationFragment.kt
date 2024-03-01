/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.source_localization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrivalInfo
import com.st.blue_sdk.models.Boards
import com.st.core.ARG_NODE_ID
import com.st.source_localization.databinding.SourceLocalizationFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SourceLocalizationFragment : Fragment() {

    private val viewModel: SourceLocalizationViewModel by viewModels()
    private lateinit var binding: SourceLocalizationFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mSLocNeedleImage: ImageView
    private lateinit var mSLocAngleText: TextView
    private lateinit var mAngleFormat: String
    private lateinit var mBoardImage: ImageView
    private lateinit var sensSwitch: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = SourceLocalizationFragmentBinding.inflate(inflater, container, false)

        mSLocNeedleImage = binding.sourceLocNeedle
        mSLocAngleText = binding.sourceLocAngle
        mBoardImage = binding.sourceLocImageBackground
        sensSwitch = binding.sourceLocSensitivitySwitch

        sensSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.enableLowSensitivity(nodeId, isChecked)
        }

        mAngleFormat = resources.getString(R.string.source_loc_angle_format)

        when (viewModel.getNode(nodeId)) {
            Boards.Model.BLUE_COIN -> mBoardImage.setImageResource(R.drawable.ic_board_bluecoin_bg)
            Boards.Model.NUCLEO,
            Boards.Model.NUCLEO_F401RE,
            Boards.Model.NUCLEO_L476RG,
            Boards.Model.NUCLEO_L053R8,
            Boards.Model.NUCLEO_U575ZIQ,
            Boards.Model.NUCLEO_U5A5ZJQ,
            Boards.Model.NUCLEO_F446RE -> {
                mBoardImage.setImageResource(R.drawable.ic_board_nucleo_bg)
                mBoardImage.rotation = 90F
            }

            else -> mBoardImage.setImageResource(R.drawable.mic_on)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.directionData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: DirectionOfArrivalInfo) {
        val angle = it.angle.value
        val angleStr = String.format(mAngleFormat, angle)
        mSLocNeedleImage.rotation = angle.toFloat()
        mSLocAngleText.text = angleStr
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId, lowSensitivity = !sensSwitch.isChecked)

        if (sensSwitch.isChecked) {
            sensSwitch.toggle()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
