/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.predicted_maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatusInfo
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatusInfo
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatusInfo
import com.st.core.ARG_NODE_ID
import com.st.predicted_maintenance.databinding.PredictedMaintenanceFragmentBinding
import com.st.predicted_maintenance.utilities.PredictiveStatusView
import com.st.predicted_maintenance.utilities.ViewStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PredictedMaintenanceFragment : Fragment() {

    private val viewModel: PredictedMaintenanceViewModel by viewModels()
    private lateinit var binding: PredictedMaintenanceFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mSpeedStatusView: PredictiveStatusView
    private lateinit var mAccelerationStatusView: PredictiveStatusView
    private lateinit var mFrequencyStatusView: PredictiveStatusView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = PredictedMaintenanceFragmentBinding.inflate(inflater, container, false)

        mSpeedStatusView = binding.predictiveSpeedStatus
        mSpeedStatusView.visibility = View.GONE
        mFrequencyStatusView = binding.predictiveFrequencyDomainStatus
        mFrequencyStatusView.visibility = View.GONE
        mAccelerationStatusView = binding.predictiveAccelerationStatus
        mAccelerationStatusView.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speedData.collect {
                    mSpeedStatusView.updateStatus(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.freqData.collect {
                    mFrequencyStatusView.updateStatus(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accData.collect {
                    mAccelerationStatusView.updateStatus(it)
                }
            }
        }

        viewModel.mSpeedStatusVisibility.observe(viewLifecycleOwner) { visible ->
            if(visible) {
                mSpeedStatusView.visibility = View.VISIBLE
            }else{
                mSpeedStatusView.visibility = View.GONE
            }
        }

        viewModel.mAccStatusVisibility.observe(viewLifecycleOwner) {  visible ->
            if(visible) {
                mAccelerationStatusView.visibility = View.VISIBLE
            }else{
                mAccelerationStatusView.visibility = View.GONE
            }
        }

        viewModel.mFrequencyStatusVisibility.observe(viewLifecycleOwner) {  visible ->
            if(visible) {
                mFrequencyStatusView.visibility = View.VISIBLE
            }else{
                mFrequencyStatusView.visibility = View.GONE
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
