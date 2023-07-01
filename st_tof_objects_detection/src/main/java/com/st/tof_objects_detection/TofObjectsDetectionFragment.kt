/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.tof_objects_detection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObjectInfo
import com.st.core.ARG_NODE_ID
import com.st.tof_objects_detection.databinding.TofObjectsDetectionFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TofObjectsDetectionFragment : Fragment() {

    private val viewModel: TofObjectsDetectionViewModel by viewModels()
    private lateinit var binding: TofObjectsDetectionFragmentBinding
    private lateinit var nodeId: String


    /**
     * for Switching between presence/object detection
     */
    private lateinit var mObjSwitch: SwitchCompat

    /**
     * Presence Section
     */
    private lateinit var mPresenceCard: CardView
    private lateinit var mPresenceImage: ImageView
    private lateinit var mPresenceText: TextView

    /**
     * Objects detection section
     */
    private lateinit var mCard_0: CardView
    private lateinit var mObjImg_0: ImageView
    private lateinit var mObjText_0: TextView

    /**
     * Object 1
     */
    private lateinit var mObjText_1: TextView
    private lateinit var mObjProg_1: ProgressBar
    private lateinit var mCard_1: CardView

    /**
     * Object 2
     */
    private lateinit var mObjText_2: TextView
    private lateinit var mObjProg_2: ProgressBar
    private lateinit var mCard_2: CardView

    /**
     * Object 3
     */
    private lateinit var mObjText_3: TextView
    private lateinit var mObjProg_3: ProgressBar
    private lateinit var mCard_3: CardView

    /**
     * Object 4
     */
    private lateinit var mObjText_4: TextView
    private lateinit var mObjProg_4: ProgressBar
    private lateinit var mCard_4: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = TofObjectsDetectionFragmentBinding.inflate(inflater, container, false)

        mObjSwitch = binding.ObjSwitch
        mPresenceCard = binding.ObjPresenceCard
        mPresenceImage = binding.ObjPresenceImage
        mPresenceText = binding.ObjPresenceText

        mCard_0 = binding.Obj0Card
        mObjImg_0 = binding.Obj0Image
        mObjText_0 = binding.Obj0Text

        mObjText_1 = binding.Obj1Text
        mObjProg_1 = binding.Obj1ProgressBar
        mCard_1 = binding.Obj1Card

        mObjText_2 = binding.Obj2Text
        mObjProg_2 = binding.Obj2ProgressBar
        mCard_2 = binding.Obj2Card

        mObjText_3 = binding.Obj3Text
        mObjProg_3 = binding.Obj3ProgressBar
        mCard_3 = binding.Obj3Card

        mObjText_4 = binding.Obj4Text
        mObjProg_4 = binding.Obj4ProgressBar
        mCard_4 = binding.Obj4Card

        mObjSwitch.setOnCheckedChangeListener { _: View, isChecked: Boolean ->
            viewModel.enableDisablePresence(isChecked, nodeId)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mObjSwitch.isChecked = viewModel.mPresenceDemo

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tofData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(tofInfo: ToFMultiObjectInfo) {
        mCard_1.visibility = View.INVISIBLE
        mCard_2.visibility = View.INVISIBLE
        mCard_3.visibility = View.INVISIBLE
        mCard_4.visibility = View.INVISIBLE
        if (viewModel.mPresenceDemo) {
            val numPresence = tofInfo.presenceFound.value
            val valueStr = getNumPresenceToString(numPresence)
            mPresenceText.text = valueStr
            if (numPresence.toInt() != 0) {
                mPresenceImage.setImageResource(R.drawable.tof_presence)
            } else {
                mPresenceImage.setImageResource(R.drawable.tof_not_presence)
            }
            mCard_0.visibility = View.INVISIBLE
            mPresenceCard.visibility = View.VISIBLE
        } else {
            mCard_0.visibility = View.VISIBLE
            mPresenceCard.visibility = View.INVISIBLE
            val numObjs = tofInfo.nObjsFound.value
            val valueStr = getNumObjectsToString(numObjs)
            if (numObjs.toInt() != 0) {
                obj_found(mObjImg_0)
            } else {
                obj_not_found(mObjImg_0)
            }
            mObjText_0.text = valueStr

            val distance1 = getDistance(tofInfo.distanceObjs, 0)
            val distance2 = getDistance(tofInfo.distanceObjs, 1)
            val distance3 = getDistance(tofInfo.distanceObjs, 2)
            val distance4 = getDistance(tofInfo.distanceObjs, 3)

            if (distance1 != 0) {
                val valueStr1: String = String.format("Distance %d mm", distance1)
                mObjText_1.text = valueStr1
                mObjProg_1.progress = (distance1 * 100) / 4000
                mCard_1.visibility = View.VISIBLE
            }
            if (distance2 != 0) {
                val valueStr2: String = String.format("Distance %d mm", distance2)
                mObjText_2.text = valueStr2
                mObjProg_2.progress = (distance2 * 100) / 4000
                mCard_2.visibility = View.VISIBLE
            }
            if (distance3 != 0) {
                val valueStr3: String = String.format("Distance %d mm", distance2)
                mObjText_3.text = valueStr3
                mObjProg_3.progress = (distance3 * 100) / 4000
                mCard_3.visibility = View.VISIBLE
            }
            if (distance4 != 0) {
                val valueStr4: String = String.format("Distance %d mm", distance4)
                mObjText_4.text = valueStr4
                mObjProg_4.progress = (distance4 * 100) / 4000
                mCard_4.visibility = View.VISIBLE
            }
        }
    }

    private fun getDistance(distanceObjs: List<FeatureField<Short>>, i: Int): Int {
        return if (i < distanceObjs.size) {
            distanceObjs[i].value.toInt()
        } else {
            0
        }
    }


    private fun getNumPresenceToString(numPresence: Short): String {
        return if (numPresence.toInt() == 1) {
            "1 Person Found"
        } else if (numPresence.toInt() > 1) {
            String.format("%d People Found", numPresence.toInt())
        } else {
            "No Presence found"
        }
    }

    private fun getNumObjectsToString(numObjs: Short): String {
        return if (numObjs.toInt() == 1) {
            "1 Object Found"
        } else if (numObjs.toInt() > 1) {
            String.format("%d Objects Found", numObjs.toInt())
        } else {
            "No objects found"
        }
    }


    /**
     * Function for changing the image when we detect one object
     * @param obj_view image type to change
     */
    private fun obj_found(obj_view: ImageView) {
        obj_view.setImageResource(R.drawable.tof_obj_found)
    }

    /**
     * Function for changing the image when we don't detect one object
     * @param obj_view image type to change
     */
    private fun obj_not_found(obj_view: ImageView) {
        obj_view.setImageResource(R.drawable.tof_obj_search)
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
