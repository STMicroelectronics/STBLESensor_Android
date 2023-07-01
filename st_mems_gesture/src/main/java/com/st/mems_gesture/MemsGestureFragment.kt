/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.mems_gesture

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.mems_gesture.MemsGestureInfo
import com.st.blue_sdk.features.mems_gesture.MemsGestureType
import com.st.core.ARG_NODE_ID
import com.st.mems_gesture.databinding.MemsGestureFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MemsGestureFragment : Fragment() {

    private val viewModel: MemsGestureViewModel by viewModels()
    private lateinit var binding: MemsGestureFragmentBinding
    private lateinit var nodeId: String

    private val AUTOMATIC_DESELECT_TIMEOUT_MS: Long = 3000

    /**
     * image link to the Pick Up event
     */
    private lateinit var mPickUpImage: ImageView

    /**
     * image link to the Wake Up event
     */
    private lateinit var mWakeUpImage: ImageView

    /**
     * image link to the Glance Image event
     */
    private lateinit var mGlanceImage: ImageView

    private lateinit var mScaleDownAnim: Animation

    private lateinit var sToGrayScale: ColorMatrixColorFilter

    /**
     * Current Image
     */
    private var mCurrentImage: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = MemsGestureFragmentBinding.inflate(inflater, container, false)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        sToGrayScale = ColorMatrixColorFilter(colorMatrix)

        mPickUpImage = binding.pickUpImage
        mPickUpImage.colorFilter = sToGrayScale
        mWakeUpImage = binding.wakeUpImage
        mWakeUpImage.colorFilter = sToGrayScale
        mGlanceImage = binding.glanceImage
        mGlanceImage.colorFilter = sToGrayScale

        mScaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)

        mCurrentImage = null
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gestureData.collect {
                    updateGestureNavigationView(it)
                }
            }
        }
    }

    private fun updateGestureNavigationView(it: MemsGestureInfo) {
        val gestureImage: ImageView? = when (it.gesture.value) {
            MemsGestureType.PickUp -> mPickUpImage
            MemsGestureType.Glance -> mGlanceImage
            MemsGestureType.WakeUp -> mWakeUpImage
            else -> null
        }
        if (gestureImage != null) {
            if (mCurrentImage != null) {
                mCurrentImage!!.colorFilter = sToGrayScale
            }

            mCurrentImage = gestureImage
            if (mCurrentImage != null) {
                mCurrentImage!!.colorFilter = null
                mCurrentImage!!.startAnimation(mScaleDownAnim)

                mCurrentImage!!.postDelayed({
                    mCurrentImage!!.colorFilter = sToGrayScale
                }, AUTOMATIC_DESELECT_TIMEOUT_MS)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Toast.makeText(context, R.string.memsGestureStarted, Toast.LENGTH_SHORT).show()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
