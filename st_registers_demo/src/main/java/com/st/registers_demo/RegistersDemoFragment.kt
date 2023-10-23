/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.registers_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.st.core.ARG_DEMO_TYPE
import com.st.core.ARG_NODE_ID
import com.st.registers_demo.common.RegisterStatusViewAdapter
import com.st.registers_demo.common.RegistersDemoType
import com.st.registers_demo.databinding.RegistersDemoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistersDemoFragment : Fragment() {

    private val viewModel: RegistersDemoViewModel by viewModels()
    private lateinit var binding: RegistersDemoFragmentBinding
    private lateinit var nodeId: String
    private lateinit var demoType: RegistersDemoType

    private lateinit var registerView: RecyclerView
    private lateinit var registerAdapter: RegisterStatusViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: MlcConfig.nodeId
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        val demoTypeString = arguments?.getString(ARG_DEMO_TYPE)
            ?: MlcConfig.demoType
            ?: throw IllegalArgumentException("Missing string $ARG_DEMO_TYPE arguments")

        try {
            demoType = RegistersDemoType.valueOf(demoTypeString)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        binding = RegistersDemoFragmentBinding.inflate(inflater, container, false)

        registerView = binding.registerStatusList

        registerAdapter = when (demoType) {
            RegistersDemoType.MLC -> RegisterStatusViewAdapter(
                R.string.mlc_registerId_format
            )

            RegistersDemoType.FSM -> RegisterStatusViewAdapter(
                R.string.fsm_registerId_format
            )

            RegistersDemoType.STRED -> RegisterStatusViewAdapter(
                R.string.stred_registerId_format
            )
        }
        registerView.adapter = registerAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registersData.collect {
                    registerAdapter.submitList(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId, demoType = demoType)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
