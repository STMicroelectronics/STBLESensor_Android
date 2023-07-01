/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.activity_recognition.Activity

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.st.activity_recognition.R
import com.st.blue_sdk.features.activity.ActivityType

class MotionARView : ActivityView {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    /**
     * image to select for the stationary activity
     */
    private lateinit var mStationaryImage: ImageView

    /**
     * image to select for the walking activity
     */
    private lateinit var mWalkingImage: ImageView

    /**
     * image to select for the fast walking activity
     */
    private lateinit var mFastWalkingImage: ImageView

    /**
     * image to select for the jogging activity
     */
    private lateinit var mJoggingImage: ImageView

    /**
     * image to select for the biking activity
     */
    private lateinit var mBikingImage: ImageView

    /**
     * image to select for the driving activity
     */
    private lateinit var mDrivingImage: ImageView

    private fun initView(context: Context) {
        inflate(context, R.layout.view_actvity_motion_ar, this)

        //extract all the image and set the alpha
        mStationaryImage = findViewById(R.id.activity_ar_stationaryImage)
        mWalkingImage = findViewById(R.id.activity_ar_walkingImage)
        mFastWalkingImage = findViewById(R.id.activity_ar_fastWalkImage)
        mJoggingImage = findViewById(R.id.activity_ar_joggingImage)
        mBikingImage = findViewById(R.id.activity_ar_bikingImage)
        mDrivingImage = findViewById(R.id.activity_ar_drivingImage)

        deselectAllImages()
    }

    override fun getSelectedImage(status: ActivityType): ImageView? {
        return when (status) {
            ActivityType.Stationary -> mStationaryImage
            ActivityType.Walking -> mWalkingImage
            ActivityType.FastWalking -> mFastWalkingImage
            ActivityType.Jogging -> mJoggingImage
            ActivityType.Biking -> mBikingImage
            ActivityType.Driving -> mDrivingImage
            else -> null
        }
    }
}