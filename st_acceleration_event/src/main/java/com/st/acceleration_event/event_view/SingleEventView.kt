package com.st.acceleration_event.event_view

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import com.st.acceleration_event.R
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType

class SingleEventView : LinearLayout, EventView {
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

    private var mCurrentIconId = 0
    private lateinit var mEventIcon: ImageView
    private lateinit var mEventText: TextView
    private lateinit var mShakeImage: Animator
    private var mCurrentDetectEvent = DetectableEventType.None

    /**
     * string were write the number of steps
     */
    private var mStepCountTextFormat: String? = null

    private fun initView(context: Context) {
        inflate(context, R.layout.view_acc_event_single, this)
        mEventIcon = findViewById(R.id.accEvent_singleEventIcon)
        mEventText = findViewById(R.id.accEvent_singleEventLabel)

        mStepCountTextFormat = resources.getString(R.string.stepCounterStringFormat)

        mShakeImage = AnimatorInflater.loadAnimator(
            context,
            R.animator.shake
        )

        mShakeImage.setTarget(mEventIcon)
    }

    override fun enableEvent(eventType: DetectableEventType) {
        mCurrentDetectEvent = eventType
        changeIcon(getDefaultIcon(eventType))
        mEventText.text = null
    }

    private fun changeIcon(@DrawableRes icon: Int) {
        if (icon == mCurrentIconId) {
            return
        }
        mEventIcon.setImageResource(icon)
        mCurrentIconId = icon
    }

    override fun displayEvent(eventTypeList: List<AccelerationType>, data: Int) {
        if(eventTypeList.isNotEmpty()) {
            val accType = eventTypeList[0]
            if (isOrientationEvent(accType)) {
                changeOrientationIcon(accType)
            } else {
                mShakeImage.start()
            }
        }
        if ((mCurrentDetectEvent == DetectableEventType.Pedometer) && (data >= 0)) {
            mEventText.text = String.format(mStepCountTextFormat!!, data)
        }
    }

    private fun changeOrientationIcon(eventType: AccelerationType) {
        @DrawableRes val newIcon = getEventIcon(eventType)
        if (newIcon != mCurrentIconId) {
            changeIcon(newIcon)
        } else {
            mShakeImage.start()
        }
    }
}