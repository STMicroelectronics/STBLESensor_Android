package com.st.acceleration_event.event_view

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import com.st.acceleration_event.R
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType

class MultipleEventView : LinearLayout, EventView {
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
     * image view that will show the current chip orientation
     */
    private lateinit var mOrientationIcon: ImageView
    private lateinit var mWakeUpOrDoubleTapIcon: ImageView
    private lateinit var mWakeUpOrDoubleTapText: TextView
    private lateinit var mTapAnim: Animator
    private lateinit var mFreeFallAnim: Animator
    private lateinit var mWakeUpOrDobuleTapAnim: Animator
    private lateinit var mTiltAnim: Animator

    /**
     * format to use for print hte pedometer data
     */
    private lateinit var mStepCountTextFormat: String
    private lateinit var mPedometerAnim: Animator
    private lateinit var mPedometerText: TextView
    private var mNSteps = 0

    private fun initView(context: Context) {
        inflate(context, R.layout.view_acc_event_multiple, this)

        mStepCountTextFormat = resources.getString(R.string.stepCounterStringFormat)

        mOrientationIcon = findViewById(R.id.accEvent_multiple_orientationIcon)

        val pedometerIcon = findViewById<ImageView>(R.id.accEvent_multiple_pedometerIcon)
        mPedometerAnim = createNewEventAnimation(context, pedometerIcon)
        mPedometerText = findViewById(R.id.accEvent_multiple_pedometerText)

        val tapIcon = findViewById<ImageView>(R.id.accEvent_multiple_tapIcon)
        mTapAnim = createNewEventAnimation(context, tapIcon)

        val freeFallIcon = findViewById<ImageView>(R.id.accEvent_multiple_freeFallIcon)
        mFreeFallAnim = createNewEventAnimation(context, freeFallIcon)

        mWakeUpOrDoubleTapIcon = findViewById(R.id.accEvent_multiple_wakeUpIcon)
        mWakeUpOrDobuleTapAnim = createNewEventAnimation(context, mWakeUpOrDoubleTapIcon)
        mWakeUpOrDoubleTapText = findViewById(R.id.accEvent_multiple_wakeUpText)

        val tiltIcon = findViewById<ImageView>(R.id.accEvent_multiple_tiltIcon)
        mTiltAnim = createNewEventAnimation(context, tiltIcon)
    }

    /////////////////////
    private fun createNewEventAnimation(context: Context, target: ImageView): Animator {
        val anim = AnimatorInflater.loadAnimator(
            context,
            R.animator.shake
        )
        anim.setTarget(target)
        return anim
    }

    private fun updatePedometer(nSteps: Int) {
        if (nSteps == mNSteps) {
            return
        }
        mNSteps = nSteps
        mPedometerAnim.start()
        updatePedometerString(nSteps)
    }

    private fun updatePedometerString(nSteps: Int) {
        mPedometerText.text = String.format(mStepCountTextFormat, nSteps)
    }

    override fun enableEvent(eventType: DetectableEventType) {
        mWakeUpOrDoubleTapIcon.setImageResource(R.drawable.acc_event_tap_double)
        mWakeUpOrDoubleTapText.setText(R.string.accEvent_double_tap)
    }

    override fun displayEvent(eventTypeList: List<AccelerationType>, data: Int) {
        if(eventTypeList.isNotEmpty()) {
            eventTypeList.forEach { accType ->
                if (isOrientationEvent(accType)) {
                    mOrientationIcon.setImageResource(getEventIcon(accType))
                }

                if (accType == AccelerationType.FreeFall) {
                    mFreeFallAnim.start()
                }
                if (accType == AccelerationType.SingleTap) {
                    mTapAnim.start()
                }

                if ((accType == AccelerationType.WakeUp) ||
                    (accType == AccelerationType.DoubleTap)
                ) {
                    mWakeUpOrDobuleTapAnim.start()
                }
                if (accType == AccelerationType.Tilt) {
                    mTiltAnim.start()
                }
                if (accType == AccelerationType.Pedometer) {
                    updatePedometer(data)
                }
            }
        }
    }
}