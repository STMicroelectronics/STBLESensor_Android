package com.st.BlueMS.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureEventCounter
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(name = "Event Counter",
    iconRes = R.drawable.ic_baseline_numbers_24,
    demoCategory = ["Control"],
    requireAll = [FeatureEventCounter::class])
class EventCounterFragment : BaseDemoFragment() {

    private var mFeature: FeatureEventCounter? = null
    private lateinit var mTextView: TextView
    private lateinit var mScaleUpAnim: Animation

    private var mDefaultTextColor : Int=0

    private val featureListener = Feature.FeatureListener { _, sample ->
        val number = FeatureEventCounter.getEventCounter(sample).toLong()
        updateGui {
            mTextView.text = number.toString()
            mTextView.setTextColor(resources.getColor(R.color.dotAmber))
            mTextView.postDelayed({ mTextView.setTextColor(mDefaultTextColor) }, 300)
            mTextView.startAnimation(mScaleUpAnim)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Inflate the layout */
        val rootView = inflater.inflate(R.layout.fragment_demo_event_counter, container, false)

        //Find the graphical elements
        mTextView = rootView.findViewById(R.id.demo_event_counter_number)
        mDefaultTextColor = mTextView.currentTextColor

        //Load the animations
        mScaleUpAnim = AnimationUtils.loadAnimation(activity,R.anim.scale_up)

        return rootView
    }


    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureEventCounter::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureEventCounter::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }
}