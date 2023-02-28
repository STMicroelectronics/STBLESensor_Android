package com.st.BlueMS.demos

import android.animation.ValueAnimator.REVERSE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureGestureNavigation
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(name = "Gesture Navigation",
    iconRes = R.drawable.ic_baseline_control_navigation,
    demoCategory = ["Environmental Sensors"],
    requireAll = [FeatureGestureNavigation::class])
class GestureNavigationFragment : BaseDemoFragment() {

    private var mFeature: FeatureGestureNavigation? = null

    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView
    private lateinit var arrowUp: ImageView
    private lateinit var arrowDown: ImageView
    private lateinit var textGesture: TextView

    private lateinit var mScaleDownAnim: Animation
    private lateinit var mMoveLeftAnim: Animation
    private lateinit var mMoveRightAnim: Animation
    private lateinit var mMoveUpAnim: Animation
    private lateinit var mMoveDownAnim: Animation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Inflate the layout */
        val rootView = inflater.inflate(R.layout.fragment_demo_gesture_navigation, container, false)

        //Find the graphical elements
        arrowLeft = rootView.findViewById(R.id.gesture_navigation_left_arrow)
        arrowRight = rootView.findViewById(R.id.gesture_navigation_right_arrow)
        arrowUp = rootView.findViewById(R.id.gesture_navigation_up_arrow)
        arrowDown = rootView.findViewById(R.id.gesture_navigation_down_arrow)
        textGesture = rootView.findViewById(R.id.gesture_navigation_text)

        //Load the animations
        mScaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)
        mMoveRightAnim = AnimationUtils.loadAnimation(activity, R.anim.move_right)
        mMoveLeftAnim = AnimationUtils.loadAnimation(activity, R.anim.move_left)
        mMoveUpAnim = AnimationUtils.loadAnimation(activity,R.anim.move_up)
        mMoveDownAnim = AnimationUtils.loadAnimation(activity,R.anim.move_down)
        return rootView
    }


    private val featureListener = Feature.FeatureListener { _, sample ->
        val gesture = FeatureGestureNavigation.getNavigationGesture(sample)
        val button = FeatureGestureNavigation.getNavigationButton(sample)

        updateGui {
            textGesture.text = gesture.name.replace('_',' ')
            when(gesture) {
                FeatureGestureNavigation.NavigationGesture.SWYPE_LEFT_TO_RIGHT -> {
                    arrowRight.visibility = View.VISIBLE
                    arrowLeft.visibility = View.VISIBLE
                    arrowUp.visibility = View.INVISIBLE
                    arrowDown.visibility = View.INVISIBLE

                    arrowRight.startAnimation(mMoveRightAnim)
                    arrowLeft.startAnimation(mMoveRightAnim)
                }
                FeatureGestureNavigation.NavigationGesture.SWYPE_RIGHT_TO_LEFT -> {
                    arrowRight.visibility = View.VISIBLE
                    arrowLeft.visibility = View.VISIBLE
                    arrowUp.visibility = View.INVISIBLE
                    arrowDown.visibility = View.INVISIBLE

                    arrowLeft.startAnimation(mMoveLeftAnim)
                    arrowRight.startAnimation(mMoveLeftAnim)
                }
                FeatureGestureNavigation.NavigationGesture.SWYPE_DOWN_TO_UP -> {
                    arrowRight.visibility = View.INVISIBLE
                    arrowLeft.visibility = View.INVISIBLE
                    arrowUp.visibility = View.VISIBLE
                    arrowDown.visibility = View.VISIBLE

                    arrowDown.startAnimation(mMoveUpAnim)
                    arrowUp.startAnimation(mMoveUpAnim)

                }
                FeatureGestureNavigation.NavigationGesture.SWYPE_UP_TO_DOWN -> {
                    arrowRight.visibility = View.INVISIBLE
                    arrowLeft.visibility = View.INVISIBLE
                    arrowUp.visibility = View.VISIBLE
                    arrowDown.visibility = View.VISIBLE

                    arrowUp.startAnimation(mMoveDownAnim)
                    arrowDown.startAnimation(mMoveDownAnim)
                }
                FeatureGestureNavigation.NavigationGesture.SINGLE_PRESS ->  {
                    mScaleDownAnim.duration=100
                    mScaleDownAnim.repeatMode= REVERSE
                }
                FeatureGestureNavigation.NavigationGesture.DOUBLE_PRESS ->  {
                    mScaleDownAnim.duration=100
                    mScaleDownAnim.repeatMode= REVERSE
                }
                FeatureGestureNavigation.NavigationGesture.TRIPLE_PRESS ->  {
                    mScaleDownAnim.duration=100
                    mScaleDownAnim.repeatMode= REVERSE
                }
                FeatureGestureNavigation.NavigationGesture.LONG_PRESS -> {
                    mScaleDownAnim.duration=400
                    mScaleDownAnim.repeatMode= REVERSE
                }
                else -> {}
            }

            when(button) {
                FeatureGestureNavigation.NavigationButton.DOWN -> {
                    arrowRight.visibility = View.INVISIBLE
                    arrowLeft.visibility = View.INVISIBLE
                    arrowUp.visibility = View.INVISIBLE
                    arrowDown.visibility = View.VISIBLE

                    arrowDown.startAnimation(mScaleDownAnim)
                }
                FeatureGestureNavigation.NavigationButton.UP -> {
                    arrowRight.visibility = View.INVISIBLE
                    arrowLeft.visibility = View.INVISIBLE
                    arrowUp.visibility = View.VISIBLE
                    arrowDown.visibility = View.INVISIBLE

                    arrowUp.startAnimation(mScaleDownAnim)
                }
                FeatureGestureNavigation.NavigationButton.LEFT -> {
                    arrowRight.visibility = View.INVISIBLE
                    arrowLeft.visibility = View.VISIBLE
                    arrowUp.visibility = View.INVISIBLE
                    arrowDown.visibility = View.INVISIBLE

                    arrowLeft.startAnimation(mScaleDownAnim)
                }
                FeatureGestureNavigation.NavigationButton.RIGHT -> {
                    arrowRight.visibility = View.VISIBLE
                    arrowLeft.visibility = View.INVISIBLE
                    arrowUp.visibility = View.INVISIBLE
                    arrowDown.visibility = View.INVISIBLE

                    arrowRight.startAnimation(mScaleDownAnim)
                }
                else -> {}
            }
        }
    }

//    private fun showTextForTimeSlice(textView: TextView, text: String?) {
//        if(text!=null) {
//            textView.text = text
//            textView.visibility = View.VISIBLE
//            textView.postDelayed(Runnable { textView.visibility = View.INVISIBLE }, 400)
//        }
//    }

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureGestureNavigation::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureGestureNavigation::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }
}