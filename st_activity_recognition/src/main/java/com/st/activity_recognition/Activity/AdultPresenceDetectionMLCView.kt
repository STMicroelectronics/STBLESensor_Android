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

class AdultPresenceDetectionMLCView : ActivityView {
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
     * image to select for the adult not in car activity
     */
    private lateinit var mAdultNotInCarImage: ImageView

    /**
     * image to select for the adult in car activity
     */
    private lateinit var mAdultInCarImage: ImageView

    private fun initView(context: Context) {
        inflate(context, R.layout.view_adult_in_car_mlc, this)

        //extract all the image and set the alpha
        mAdultNotInCarImage = findViewById(R.id.activity_mlc_adultNotInCarImage)
        mAdultInCarImage = findViewById(R.id.activity_mlc_adultInCarImage)

        deselectAllImages()
    }

    override fun getSelectedImage(status: ActivityType): ImageView? {
        return when (status) {
            ActivityType.NoActivity -> mAdultNotInCarImage
            ActivityType.AdultInCar -> mAdultInCarImage
            else -> null
        }
    }
}