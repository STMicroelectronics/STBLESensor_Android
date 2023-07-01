/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.gesture_navigation

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationButton
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationGestureType
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationInfo
import com.st.core.ARG_NODE_ID
import com.st.gesture_navigation.databinding.GestureNavigationFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GestureNavigationDemoFragment : Fragment() {

    private val viewModel: GestureNavigationViewModel by viewModels()

    private lateinit var binding: GestureNavigationFragmentBinding

    private lateinit var nodeId: String

    private lateinit var mScaleDownAnim: Animation
    private lateinit var mMoveLeftAnim: Animation
    private lateinit var mMoveRightAnim: Animation
    private lateinit var mMoveUpAnim: Animation
    private lateinit var mMoveDownAnim: Animation


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GestureNavigationFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        //Load the animations
        mScaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)
        mMoveRightAnim = AnimationUtils.loadAnimation(activity, R.anim.move_right)
        mMoveLeftAnim = AnimationUtils.loadAnimation(activity, R.anim.move_left)
        mMoveUpAnim = AnimationUtils.loadAnimation(activity, R.anim.move_up)
        mMoveDownAnim = AnimationUtils.loadAnimation(activity, R.anim.move_down)

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

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    private fun updateGestureNavigationView(gestureInfo: GestureNavigationInfo) {
        val gesture = gestureInfo.gesture
        val button = gestureInfo.button

        binding.gestureNavigationText.text = gesture.value.name

        when (gesture.value) {
            GestureNavigationGestureType.SwipeLeftToRight -> {
                binding.gestureNavigationRightArrow.visibility = View.VISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.VISIBLE
                binding.gestureNavigationUpArrow.visibility = View.INVISIBLE
                binding.gestureNavigationDownArrow.visibility = View.INVISIBLE

                binding.gestureNavigationRightArrow.startAnimation(mMoveRightAnim)
                binding.gestureNavigationLeftArrow.startAnimation(mMoveRightAnim)
            }

            GestureNavigationGestureType.SwipeRightToLeft -> {
                binding.gestureNavigationRightArrow.visibility = View.VISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.VISIBLE
                binding.gestureNavigationUpArrow.visibility = View.INVISIBLE
                binding.gestureNavigationDownArrow.visibility = View.INVISIBLE

                binding.gestureNavigationRightArrow.startAnimation(mMoveLeftAnim)
                binding.gestureNavigationLeftArrow.startAnimation(mMoveLeftAnim)
            }

            GestureNavigationGestureType.SwipeDownToUp -> {
                binding.gestureNavigationRightArrow.visibility = View.INVISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.INVISIBLE
                binding.gestureNavigationUpArrow.visibility = View.VISIBLE
                binding.gestureNavigationDownArrow.visibility = View.VISIBLE

                binding.gestureNavigationUpArrow.startAnimation(mMoveUpAnim)
                binding.gestureNavigationDownArrow.startAnimation(mMoveUpAnim)
            }

            GestureNavigationGestureType.SwipeUpToDown -> {
                binding.gestureNavigationRightArrow.visibility = View.INVISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.INVISIBLE
                binding.gestureNavigationUpArrow.visibility = View.VISIBLE
                binding.gestureNavigationDownArrow.visibility = View.VISIBLE

                binding.gestureNavigationUpArrow.startAnimation(mMoveDownAnim)
                binding.gestureNavigationDownArrow.startAnimation(mMoveDownAnim)
            }

            GestureNavigationGestureType.SinglePress -> {
                mScaleDownAnim.duration = 100
                mScaleDownAnim.repeatMode = ValueAnimator.REVERSE
            }

            GestureNavigationGestureType.DoublePress -> {
                mScaleDownAnim.duration = 100
                mScaleDownAnim.repeatMode = ValueAnimator.REVERSE

            }

            GestureNavigationGestureType.TriplePress -> {
                mScaleDownAnim.duration = 100
                mScaleDownAnim.repeatMode = ValueAnimator.REVERSE

            }

            GestureNavigationGestureType.LongPress -> {
                mScaleDownAnim.duration = 400
                mScaleDownAnim.repeatMode = ValueAnimator.REVERSE
            }

            else -> {
                /** NOOP **/
            }
        }

        when (button.value) {
            GestureNavigationButton.Left -> {
                binding.gestureNavigationRightArrow.visibility = View.INVISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.VISIBLE
                binding.gestureNavigationUpArrow.visibility = View.INVISIBLE
                binding.gestureNavigationDownArrow.visibility = View.INVISIBLE

                binding.gestureNavigationLeftArrow.startAnimation(mScaleDownAnim)
            }

            GestureNavigationButton.Right -> {
                binding.gestureNavigationRightArrow.visibility = View.VISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.INVISIBLE
                binding.gestureNavigationUpArrow.visibility = View.INVISIBLE
                binding.gestureNavigationDownArrow.visibility = View.INVISIBLE

                binding.gestureNavigationRightArrow.startAnimation(mScaleDownAnim)
            }

            GestureNavigationButton.Up -> {
                binding.gestureNavigationRightArrow.visibility = View.INVISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.INVISIBLE
                binding.gestureNavigationUpArrow.visibility = View.VISIBLE
                binding.gestureNavigationDownArrow.visibility = View.INVISIBLE

                binding.gestureNavigationUpArrow.startAnimation(mScaleDownAnim)
            }

            GestureNavigationButton.Down -> {
                binding.gestureNavigationRightArrow.visibility = View.INVISIBLE
                binding.gestureNavigationLeftArrow.visibility = View.INVISIBLE
                binding.gestureNavigationUpArrow.visibility = View.INVISIBLE
                binding.gestureNavigationDownArrow.visibility = View.VISIBLE

                binding.gestureNavigationDownArrow.startAnimation(mScaleDownAnim)
            }

            else -> {
                /** NOOP **/
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
