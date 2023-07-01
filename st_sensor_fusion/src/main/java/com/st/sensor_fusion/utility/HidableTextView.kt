package com.st.sensor_fusion.utility

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.AttrRes

class HidableTextView : androidx.appcompat.widget.AppCompatTextView {

    private var mIsVisible = false

    /** theme text color  */
    private var mOrigColor: ColorStateList? = null

    constructor(context: Context) : super(context) {
        initView()
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        setBackgroundColor(Color.TRANSPARENT)
        mIsVisible = false
        mOrigColor = textColors
        isClickable = true
        setOnClickListener {
            mIsVisible = !mIsVisible
            changeVisibility()
        } //onClick

        changeVisibility()
    }

    private fun changeVisibility() {
        if (mIsVisible) setTextColor(mOrigColor) else setTextColor(Color.TRANSPARENT)
    }
}