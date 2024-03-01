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
import androidx.constraintlayout.widget.ConstraintLayout
import com.st.blue_sdk.features.activity.ActivityType

/**
 * common class between all the ones that display different activity icons
 */
abstract class ActivityView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    /**
     * currently selected image
     */
    private var mSelectedImage: ImageView? = null

    /**
     * utility method to deselect all the demo icons
     */
    protected fun deselectAllImages() {
        for (type in ActivityType.entries) {
            val image = getSelectedImage(type)
            if (image != null) {
                image.alpha = DEFAULT_ALPHA
            }
        }
    }

    /**
     * map an activity type to the linked imageview
     * @param status current activity
     * @return image view associated to the status activity or null if is an invalid status
     */
    abstract fun getSelectedImage(status: ActivityType): ImageView?

    /***
     * select a specific images
     * @param type activity image to select
     */
    fun setActivity(type: ActivityType?) {
        //restore the alpha for the old activity
        if (mSelectedImage != null) {
            mSelectedImage!!.alpha = DEFAULT_ALPHA
        }

        //find the new activity
        if (type != null) {
            mSelectedImage = getSelectedImage(type)
        }

        //set the alpha for the new activity
        if (mSelectedImage != null) {
            mSelectedImage!!.alpha = SELECTED_ALPHA
        }
    }

    companion object {
        /**
         * alpha to use for the selected image
         */
        private const val SELECTED_ALPHA = 1.0f

        /**
         * alpha to use for the other images
         */
        private const val DEFAULT_ALPHA = 0.3f
    }
}