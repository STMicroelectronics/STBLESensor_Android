/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.activity_recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.activity_recognition.Activity.ActivityView
import com.st.activity_recognition.databinding.ActivityRecognitionFragmentBinding
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActivityRecognitionFragment : Fragment() {

    private val viewModel: ActivityRecognitionViewModel by viewModels()
    private lateinit var binding: ActivityRecognitionFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mWaitView // dummy view to display until the first data isn't arrived
            : View

    private lateinit var mMotionARView // default view
            : ActivityView
    private lateinit var mGMPView // GMP activity recognition
            : ActivityView
    private lateinit var mIGNView // IGN activity recognition
            : ActivityView
    private lateinit var mHAR_MLCView // activity recognition from mlc
            : ActivityView
    private lateinit var mAPD_MLCView // adult presence recognition from mlc
            : ActivityView

    private val mAllView: MutableList<ActivityView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string \"nodeId\" arguments")

        binding = ActivityRecognitionFragmentBinding.inflate(inflater, container, false)

        mWaitView = binding.activityWaitingData
        mMotionARView = binding.activityViewMotionAR
        mGMPView = binding.activityViewGMP
        mIGNView = binding.activityViewIGN
        mHAR_MLCView = binding.activityViewHARMLC
        mAPD_MLCView = binding.activityViewAPDMLC


        mAllView.add(mMotionARView) //0
        mAllView.add(mGMPView) //1
        mAllView.add(mIGNView) //2
        mAllView.add(mHAR_MLCView)//3
        mAllView.add(mAPD_MLCView)//4

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityData.collect {
                    updateGui(it)
                }
            }
        }

    }

    private fun updateGui(it: ActivityInfo) {
        val activityType = it.activity.value
        val algorithm = if (it.algorithm.value == ActivityInfo.ALGORITHM_NOT_DEFINED) {
            0 //Default == mMotionARView
        } else {
            it.algorithm.value.toInt()
        }

        it.algorithm.value.toInt()
        mWaitView.visibility = View.GONE

        mAllView.forEachIndexed { index, view ->
            if (index == algorithm) {
                view.visibility = View.VISIBLE
                view.setActivity(activityType)
            } else {
                view.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(context, R.string.activityRecognition_started, Toast.LENGTH_SHORT).show()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
