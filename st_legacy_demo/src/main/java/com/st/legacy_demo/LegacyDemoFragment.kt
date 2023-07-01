/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.legacy_demo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.st.core.ARG_NODE_ID
import com.st.legacy_demo.databinding.LegacyDemoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LegacyDemoFragment : Fragment() {

    private lateinit var binding: LegacyDemoFragmentBinding
    private val viewModel: LegacyDemoViewModel by viewModels()
    private lateinit var nodeId: String

    private lateinit var googlePlayImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = LegacyDemoFragmentBinding.inflate(inflater, container, false)

        googlePlayImage = binding.imageviewLegacy
        googlePlayImage.setImageResource(R.drawable.google_play_badge)
        googlePlayImage.setOnClickListener { viewModel.openGooglePlayConsole(requireContext()) }
        return binding.root
    }

}
