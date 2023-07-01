/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.event_counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.event_counter.EventCounterInfo
import com.st.core.ARG_NODE_ID
import com.st.event_counter.databinding.EventCounterFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventCounterFragment : Fragment() {

    private val viewModel: EventCounterViewModel by viewModels()

    private lateinit var binding: EventCounterFragmentBinding

    private lateinit var nodeId: String

    private lateinit var mScaleUpAnim: Animation
    private var mDefaultTextColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EventCounterFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        //Read the Default TextColor
        mDefaultTextColor = binding.demoEventCounterNumber.currentTextColor

        //Load the animations
        mScaleUpAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_up)

        return binding.root
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                viewModel.stopDemo(nodeId = nodeId)
//                findNavController().popBackStack()
//            }
//        })
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventCounterData.collect {
                    updateEventCounterView(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    private fun updateEventCounterView(eventCounterInfo: EventCounterInfo) {
        val number = eventCounterInfo.count.value

        binding.demoEventCounterNumber.text = number.toString()
        binding.demoEventCounterNumber.setTextColor(ContextCompat.getColor(requireContext(), com.st.ui.R.color.PrimaryYellow))
        binding.demoEventCounterNumber.postDelayed({
            binding.demoEventCounterNumber.setTextColor(
                mDefaultTextColor
            )
        }, 300)
        binding.demoEventCounterNumber.startAnimation(mScaleUpAnim)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
