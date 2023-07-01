/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.proximity_gesture_recognition

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.proximity_gesture.ProximityGestureInfo
import com.st.blue_sdk.features.proximity_gesture.ProximityGestureType
import com.st.core.ARG_NODE_ID
import com.st.proximity_gesture_recognition.databinding.ProximityGestureRecognitionFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProximityGestureRecognitionFragment : Fragment() {

    private val viewModel: ProximityGestureRecognitionViewModel by viewModels()
    private lateinit var binding: ProximityGestureRecognitionFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mTapImage: ImageView
    private lateinit var mLeftImage: ImageView
    private lateinit var mRightImage: ImageView

    private var mCurrentImage: ImageView? = null

    private lateinit var mScaleDownAnim: Animation
    private lateinit var mMoveLeftAnim: Animation
    private lateinit var mMoveRightAnim: Animation

    private lateinit var sToGrayScale: ColorMatrixColorFilter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = ProximityGestureRecognitionFragmentBinding.inflate(inflater, container, false)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        sToGrayScale = ColorMatrixColorFilter(colorMatrix)

        mTapImage = binding.tapImage
        mTapImage.colorFilter = sToGrayScale
        mLeftImage = binding.leftImage
        mLeftImage.colorFilter = sToGrayScale
        mRightImage = binding.rightImage
        mRightImage.colorFilter = sToGrayScale

        //Load the animations
        mScaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)
        mMoveRightAnim = AnimationUtils.loadAnimation(activity, R.anim.move_right)
        mMoveLeftAnim = AnimationUtils.loadAnimation(activity, R.anim.move_left)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gestureData.collect {
                    updateProximityGestureNavigationView(it)
                }
            }
        }

    }

    private fun updateProximityGestureNavigationView(it: ProximityGestureInfo) {
        when (it.gesture.value) {
            ProximityGestureType.Tap -> {
                selectedImage(mTapImage)
                mTapImage.startAnimation(mScaleDownAnim)
            }
            ProximityGestureType.Left -> {
                selectedImage(mLeftImage)
                mLeftImage.startAnimation(mMoveLeftAnim)
            }
            ProximityGestureType.Right -> {
                selectedImage(mRightImage)
                mRightImage.startAnimation(mMoveRightAnim)
            }
            else -> {

            }
        }
    }

    private fun selectedImage(image: ImageView) {
        if (mCurrentImage != null) {
            mRightImage.colorFilter = sToGrayScale
        }
        mCurrentImage = image
        image.colorFilter = null
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
