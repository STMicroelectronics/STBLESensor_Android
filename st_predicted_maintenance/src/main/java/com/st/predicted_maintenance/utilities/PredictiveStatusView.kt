package com.st.predicted_maintenance.utilities

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import com.st.blue_sdk.features.extended.predictive.Status
import com.st.predicted_maintenance.R

class PredictiveStatusView : CardView {

    constructor(context: Context) : super(context) {
        initView(context,null,0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context,attrs,0)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context,attrs, defStyleAttr)
    }

    private lateinit var mValueFormat: String

    private lateinit var mXStatusText: TextView
    private lateinit var mXFreqText: TextView
    private lateinit var mXValueText: TextView
    private lateinit var mXStatusImage: ImageView

    private lateinit var mYStatusText: TextView
    private lateinit var mYFreqText: TextView
    private lateinit var mYValueText: TextView
    private lateinit var mYStatusImage: ImageView

    private lateinit var mZStatusText: TextView
    private lateinit var mZFreqText: TextView
    private lateinit var mZValueText: TextView
    private lateinit var mZStatusImage: ImageView

    private fun initView(context: Context,attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        inflate(context, R.layout.view_preditive_status, this)
        setCardTitle(context, attrs, defStyleAttr)

        mValueFormat = loadValueFormat(context, attrs, defStyleAttr)

        mXStatusText = findViewById(R.id.predictive_xStatus)
        mXFreqText = findViewById(R.id.predictive_xFreq)
        mXValueText = findViewById(R.id.predictive_xValue)
        mXStatusImage = findViewById(R.id.predictive_xStatusImage)

        mYStatusText = findViewById(R.id.predictive_yStatus)
        mYFreqText = findViewById(R.id.predictive_yFreq)
        mYValueText = findViewById(R.id.predictive_yValue)
        mYStatusImage = findViewById(R.id.predictive_yStatusImage)

        mZStatusText = findViewById(R.id.predictive_zStatus)
        mZFreqText = findViewById(R.id.predictive_zFreq)
        mZValueText = findViewById(R.id.predictive_zValue)
        mZStatusImage = findViewById(R.id.predictive_zStatusImage)
    }

    private fun loadValueFormat(context: Context, attrs: AttributeSet?, defStyleAttr: Int): String {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.PredictiveStatusView, defStyleAttr, 0)
        val format = a.getString(R.styleable.PredictiveStatusView_valueFormat)
        a.recycle()

        return format ?: context.getString(R.string.predictive_statusView_defaultValueFormat)
    }

    private fun setCardTitle(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.PredictiveStatusView, defStyleAttr, 0)

        val title = a.getString(R.styleable.PredictiveStatusView_title)
        val titleView = findViewById<TextView>(R.id.predictive_title)
        if (title == null) {
            titleView.visibility = GONE
        } else {
            titleView.text = title
        }

        a.recycle()
    }

    private fun getStatusString(s: Status): String? {
        val res = resources
        return when (s) {
            Status.GOOD -> res.getString(R.string.predictive_statusView_good)
            Status.WARNING -> res.getString(R.string.predictive_statusView_warning)
            Status.BAD -> res.getString(R.string.predictive_statusView_bad)
            else -> res.getString(R.string.predictive_statusView_unknown)
        }
    }

    @DrawableRes
    private fun getStatusImage(s: Status): Int {
        return when (s) {
            Status.GOOD -> R.drawable.predictive_status_good
            Status.WARNING -> R.drawable.predictive_status_warnings
            Status.BAD -> R.drawable.predictive_status_bad
            else -> R.drawable.predictive_status_unknown
        }
    }

    private fun updateAxisStatus(
        @StringRes statusFormat: Int,
        statusText: TextView,
        statusImage: ImageView,
        newStatus: Status?
    ) {
        val text: CharSequence = resources.getString(statusFormat, getStatusString(newStatus!!))
        statusText.text = text
        statusImage.setImageResource(getStatusImage(newStatus))
    }

    private fun updateFrequency(freqText: TextView, value: Float?) {
        if(value!=null) {
            if (value.isNaN()) {
                freqText.visibility = GONE
            } else {
                freqText.visibility = VISIBLE
                val text = resources.getString(R.string.predictive_statusView_freqFormat, value)
                freqText.text = text
            }
        } else {
            freqText.visibility = GONE
        }
    }

    private fun updateValue(freqText: TextView, value: Float? ) {
        if(value!=null) {
            if (value.isNaN()) {
                freqText.visibility = GONE
            } else {
                freqText.visibility = VISIBLE
                val text = String.format(mValueFormat, value)
                freqText.text = text
            }
        } else {
            freqText.visibility = GONE
        }
    }

    fun updateStatus(newStatus: ViewStatus) {
        updateAxisStatus(
            R.string.predictive_statusView_xStatusFormat,
            mXStatusText,
            mXStatusImage,
            newStatus.xStatus
        )
        if (newStatus.x != null) {
            updateFrequency(mXFreqText, newStatus.x.freq)
            updateValue(mXValueText, newStatus.x.value)
        }
        updateAxisStatus(
            R.string.predictive_statusView_yStatusFormat,
            mYStatusText,
            mYStatusImage,
            newStatus.yStatus
        )
        if (newStatus.y != null) {
            updateFrequency(mYFreqText, newStatus.y.freq)
            updateValue(mYValueText, newStatus.y.value)
        }
        updateAxisStatus(
            R.string.predictive_statusView_zStatusFormat,
            mZStatusText,
            mZStatusImage,
            newStatus.zStatus
        )
        if (newStatus.z != null) {
            updateFrequency(mZFreqText, newStatus.z.freq)
            updateValue(mZValueText, newStatus.z.value)
        }
    }
}