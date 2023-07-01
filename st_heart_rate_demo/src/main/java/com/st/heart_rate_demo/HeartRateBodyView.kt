package com.st.heart_rate_demo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.st.blue_sdk.features.external.std.BodySensorLocationType
import com.st.heart_rate_demo.databinding.HeartRateBodyLayoutBinding

class HeartRateBodyView(context: Context, attrs: AttributeSet?) : LinearLayoutCompat(context, attrs) {

    private val circleRatio = 0.07f

    private var bodyImage:  AppCompatImageView
    private var circleImage: AppCompatImageView

    var bodySensorLocation: BodySensorLocationType = BodySensorLocationType.NotKnown
        set(value) {
            field = value
            setCirclePosition()
        }


    private val circleLocationMap = hashMapOf(
        BodySensorLocationType.Chest to Pair(0.52f, 0.25f),
        BodySensorLocationType.EarLobe to Pair(0.44f, 0.08f),
        BodySensorLocationType.Foot to Pair(0.46f, 0.97f),
        BodySensorLocationType.Wrist to Pair(0.66f, 0.48f),
        BodySensorLocationType.Hand to Pair(0.67f, 0.53f),
        BodySensorLocationType.Finger to Pair(0.71f, 0.58f)

    )

    init {
        val binding = HeartRateBodyLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        bodyImage = binding.heartRateBody
        circleImage = binding.hearRateCircle
        orientation = VERTICAL

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setBodySize()
        setCirclePosition()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun setBodySize() {
        this.layoutParams.height = this.layoutParams.width
        circleImage.layoutParams.height = (this.layoutParams.height * circleRatio).toInt()
        circleImage.layoutParams.width = (this.layoutParams.height * circleRatio).toInt()
        this.requestLayout()
    }

    private fun setCirclePosition() {
        val locations = circleLocationMap[bodySensorLocation]
        if (locations == null) {
            circleImage.visibility = View.GONE
            return
        }

        circleImage.x = (this.layoutParams.width * locations.first) - circleImage.layoutParams.width / 2
        circleImage.y =  (this.layoutParams.height * locations.second) - circleImage.layoutParams.height / 2
        circleImage.visibility = View.VISIBLE
    }


}