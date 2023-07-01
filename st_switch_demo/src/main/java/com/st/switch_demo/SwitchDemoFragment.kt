/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.switch_demo

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
import com.st.blue_sdk.features.switchfeature.SwitchFeatureInfo
import com.st.blue_sdk.features.switchfeature.SwitchStatusType
import com.st.blue_sdk.models.Boards
import com.st.core.ARG_NODE_ID
import com.st.switch_demo.databinding.SwitchDemoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SwitchDemoFragment : Fragment() {

    private val viewModel: SwitchDemoViewModel by viewModels()
    private lateinit var binding: SwitchDemoFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mSwitchImage: ImageView
    private lateinit var mSwitchText: TextView

    private lateinit var currentSwitchValue: SwitchStatusType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = SwitchDemoFragmentBinding.inflate(inflater, container, false)

        mSwitchImage = binding.switchImage
        mSwitchText = binding.switchTitle

        mSwitchImage.setOnClickListener { viewModel.writeSwitchCommand(nodeId, currentSwitchValue) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.switchData.collect {
                    updateView(it)
                }
            }
        }

        val boardType = viewModel.getNode(nodeId)
        if (boardType == Boards.Model.SENSOR_TILE_BOX || boardType == Boards.Model.SENSOR_TILE_BOX_PRO || boardType == Boards.Model.SENSOR_TILE_BOX_PROB) {
            mSwitchText.setText(R.string.switch_eventDescription)
        } else {
            mSwitchText.setText(R.string.switch_onOffDescription)
        }
    }

    private fun updateView(it: SwitchFeatureInfo) {
        val value = it.status.value
        if (value == SwitchStatusType.Off) {
            mSwitchImage.setImageResource(R.drawable.switch_off)
            currentSwitchValue = SwitchStatusType.Off
        } else {
            mSwitchImage.setImageResource(R.drawable.switch_on)
            currentSwitchValue = SwitchStatusType.On
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
