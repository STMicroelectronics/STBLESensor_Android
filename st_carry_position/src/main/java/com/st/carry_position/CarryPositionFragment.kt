/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.carry_position

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.carry_position.CarryPositionInfo
import com.st.blue_sdk.features.carry_position.CarryPositionType
import com.st.carry_position.databinding.CarryPositionFragmentBinding
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CarryPositionFragment : Fragment() {

    private val viewModel: CarryPositionViewModel by viewModels()
    private lateinit var binding: CarryPositionFragmentBinding
    private lateinit var nodeId: String

    /**
     * alpha to use for the selected image
     */
    private val SELECTED_ALPHA = 1.0f

    /**
     * alpha to use for the other images
     */
    private val DEFAULT_ALPHA = 0.3f

    /**
     * image to select for the unknown position
     */
    //private lateinit var mUnknownImage : ImageView
    /**
     * image to select for on desk position
     */
    private lateinit var mOnDeskImage: ImageView

    /**
     * image to select for the in hand position
     */
    private lateinit var mInHandImage: ImageView

    /**
     * image to select for the near head position
     */
    private lateinit var mNearHeadImage: ImageView

    /**
     * image to select for the shirt pocket position
     */
    private lateinit var mShirtPocketImage: ImageView

    /**
     * image to select for the trousers pocket position
     */
    private lateinit var mTrousersPocketImage: ImageView

    /**
     * image to select for the trousers pocket position
     */
    private lateinit var mArmSwingImage: ImageView

    /**
     * currently selected image
     */
    private var mSelectedImage: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = CarryPositionFragmentBinding.inflate(inflater, container, false)

        //extract all the image and set the alpha
        //mUnknownImage = (ImageView) root.findViewById(R.id.unknownImage);
        //mUnknownImage.alpha = DEFAULT_ALPHA

        mOnDeskImage = binding.onDeskImage
        mOnDeskImage.alpha = DEFAULT_ALPHA
        mInHandImage = binding.inHandImage
        mInHandImage.alpha = DEFAULT_ALPHA
        mNearHeadImage = binding.nearHeadImage
        mNearHeadImage.alpha = DEFAULT_ALPHA
        mShirtPocketImage = binding.shirtPocketImage
        mShirtPocketImage.alpha = DEFAULT_ALPHA
        mTrousersPocketImage = binding.trousersPocketImage
        mTrousersPocketImage.alpha = DEFAULT_ALPHA
        mArmSwingImage = binding.armSwingImage
        mArmSwingImage.alpha = DEFAULT_ALPHA

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.positionData.collect {
                    updateGui(it)
                }
            }
        }

    }

    private fun updateGui(it: CarryPositionInfo) {
        val position = it.position.value

        //Deselect previous Image
        if (mSelectedImage != null) {
            mSelectedImage!!.alpha = DEFAULT_ALPHA
        }

        //Find new Image
        mSelectedImage = when (position) {
            CarryPositionType.OnDesk -> mOnDeskImage
            CarryPositionType.InHand -> mInHandImage
            CarryPositionType.NearHead -> mNearHeadImage
            CarryPositionType.ShirtPocket -> mShirtPocketImage
            CarryPositionType.TrousersPocket -> mTrousersPocketImage
            CarryPositionType.ArmSwing -> mArmSwingImage
            else -> null
        }

        //Select current Image
        if (mSelectedImage != null) {
            mSelectedImage!!.alpha = SELECTED_ALPHA
        }
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(context, R.string.carryPositionStarted, Toast.LENGTH_SHORT).show()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
