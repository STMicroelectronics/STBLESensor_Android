/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.color_ambient_light

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.extended.color_ambient_light.ColorAmbientLightInfo
import com.st.color_ambient_light.databinding.ColorAmbientLightFragmentBinding
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ColorAmbientLightFragment : Fragment() {

    private val viewModel: ColorAmbientLightViewModel by viewModels()
    private lateinit var binding: ColorAmbientLightFragmentBinding
    private lateinit var nodeId: String

    private lateinit var textViewLux: TextView
    private lateinit var textViewCCT: TextView
    private lateinit var textViewUVIndex: TextView

    private lateinit var progressBarLux: ProgressBar
    private lateinit var progressBarCCT: ProgressBar
    private lateinit var progressBarUVIndex: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = ColorAmbientLightFragmentBinding.inflate(inflater, container, false)

        textViewLux = binding.textViewLux
        textViewCCT = binding.textViewCCT
        textViewUVIndex = binding.textViewUVIndex

        progressBarLux = binding.progressBarLux
        progressBarCCT = binding.progressBarCCT
        progressBarUVIndex = binding.progressBarUVIndex

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.colorData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: ColorAmbientLightInfo) {
        val percentageUVIndex =
            ((it.uvIndex.value - it.uvIndex.min!!) * 100) / (it.uvIndex.max!! - it.uvIndex.min!!)
        progressBarUVIndex.progress = percentageUVIndex
        val textUVIndex = "Value: ${it.uvIndex.value} UV Index"
        textViewUVIndex.text = textUVIndex

        val percentageCCT = ((it.cct.value - it.cct.min!!) * 100) / (it.cct.max!! - it.cct.min!!)
        progressBarCCT.progress = percentageCCT
        val textCCT = "Value: ${it.cct.value} CCT"
        textViewCCT.text = textCCT

        val percentageLux = ((it.lux.value - it.lux.min!!) * 100) / (it.lux.max!! - it.lux.min!!)
        progressBarLux.progress = percentageLux
        val textLux = "Value: ${it.lux.value} UV Lux"
        textViewLux.text = textLux
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
