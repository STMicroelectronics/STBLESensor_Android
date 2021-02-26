package com.st.BlueMS.demos

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureQVAR
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds


@DemoDescriptionAnnotation(name = "Electric Charge Variation", iconRes = R.drawable.ic_qvar_demo,
        requareAll = [FeatureQVAR::class])
class QVARFragment  : BaseDemoFragment() {
    private var mFeature : FeatureQVAR? =null
    private var mQVAR :Long? = null
    private var mFlag: Byte?=null
    private var mDQVAR :Long? = null
    private var mParameter :Long? = null

    private lateinit var mQVARCard : CardView
    private lateinit var mFlagCard : CardView
    private lateinit var mDQVARCard : CardView
    private lateinit var mParameterCard : CardView

    private lateinit var mFlagText : TextView
    private lateinit var mParameterText : TextView

    private lateinit var mQVARChart: LineChart
    private lateinit var mQVARData: LineData
    private lateinit var mQVARDataSet: LineDataSet
    private lateinit var mQVARLimitLineYMax: LimitLine
    private lateinit var mQVARLimitLineYMin: LimitLine
    private var mQVARYMax: Float =0f;
    private var mQVARYMin: Float =0f;

    private lateinit var mDQVARChart: LineChart
    private lateinit var mDQVARData: LineData
    private lateinit var mDQVARDataSet: LineDataSet
    private lateinit var mDQVARLimitLineYMax: LimitLine
    private lateinit var mDQVARLimitLineYMin: LimitLine
    private var mDQVARYMax: Float =0f;
    private var mDQVARYMin: Float =0f;

    private var mFirstNotificationTimeStamp:Long = 0

    companion object {
        const val TAG = "QVARFragment"
        @ExperimentalTime
        val SECONDS_TO_PLOT_DEFAULT = 5.seconds
    }

    @ExperimentalTime
    private val featureListener = Feature.FeatureListener { _, sample ->
        mQVAR = FeatureQVAR.getQVARValue(sample)
        mFlag = FeatureQVAR.getFlagValue(sample)
        mDQVAR = FeatureQVAR.getDQVARValue(sample)
        mParameter = FeatureQVAR.getParamValue(sample)
        val numFeatures = FeatureQVAR.getNumFields(sample)

        updateGui {
            //QVAR
            if (numFeatures>0) {
                mQVARCard.visibility = View.VISIBLE
                val yData= mQVAR!!.toFloat()
                mQVARData.addEntry(Entry((sample.notificationTime - mFirstNotificationTimeStamp).toFloat(), yData), 0)
                mQVARData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                val yMax = mQVARDataSet.yMax
                val yMin = mQVARDataSet.yMin

                //Update the LimitLine for MaxY value
                if(yMax!=mQVARYMax) {
                    mQVARYMax = yMax
                    mQVARChart.axisLeft.removeLimitLine(mQVARLimitLineYMax)
                    mQVARLimitLineYMax = LimitLine(yMax, yMax.toString());
                    mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMax)
                }

                //Update the LimitLine for MinY value
                if(yMin!=mQVARYMin) {
                    mQVARYMin = yMin
                    mQVARChart.axisLeft.removeLimitLine(mQVARLimitLineYMin)
                    mQVARLimitLineYMin = LimitLine(yMin, yMin.toString());
                    mQVARLimitLineYMin.labelPosition=LimitLabelPosition.LEFT_BOTTOM
                    mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMin)
                }

                mQVARData.notifyDataChanged()
                mQVARChart.notifyDataSetChanged()
                mQVARChart.invalidate()
            } else {
                mQVARCard.visibility = View.GONE
            }



            //mFlag
            if (numFeatures>1) {
                mFlagCard.visibility = View.VISIBLE
                mFlagText.text = "Flag\n0x%X".format(mFlag)
            } else {
                mFlagCard.visibility = View.GONE
            }



           // DQVAR
            if (numFeatures>2) {
                mDQVARCard.visibility = View.VISIBLE
                val yData= mDQVAR!!.toFloat()
                mDQVARData.addEntry(Entry((sample.notificationTime - mFirstNotificationTimeStamp).toFloat(), yData), 0)
                mDQVARData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                val yMax = mDQVARDataSet.yMax
                val yMin = mDQVARDataSet.yMin

                //Update the LimitLine for MaxY value
                if(yMax!=mDQVARYMax) {
                    mDQVARYMax = yMax
                    mDQVARChart.axisLeft.removeLimitLine(mDQVARLimitLineYMax)
                    mDQVARLimitLineYMax = LimitLine(yMax, yMax.toString());
                    mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMax)
                }

                //Update the LimitLine for MinY value
                if(yMin!=mDQVARYMin) {
                    mDQVARYMin = yMin
                    mDQVARChart.axisLeft.removeLimitLine(mDQVARLimitLineYMin)
                    mDQVARLimitLineYMin = LimitLine(yMin, yMin.toString());
                    mDQVARLimitLineYMin.labelPosition=LimitLabelPosition.LEFT_BOTTOM
                    mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMin)
                }

                mDQVARData.notifyDataChanged()
                mDQVARChart.notifyDataSetChanged()
                mDQVARChart.invalidate()

            } else {
                mDQVARCard.visibility = View.GONE
            }


            //Parameter
            if (numFeatures==4) {
                mParameterCard.visibility = View.VISIBLE
                mParameterText.text = "Parameter\n0x%X".format(mParameter)
            } else {
                mParameterCard.visibility = View.GONE

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_qvar_demo, container, false)

        mQVARCard = rootView.findViewById(R.id.qvar_qvar_card)
        mFlagCard = rootView.findViewById(R.id.qvar_flag_card)
        mDQVARCard = rootView.findViewById(R.id.qvar_dqvar_card)
        mParameterCard = rootView.findViewById(R.id.qvar_param_card)
        mQVARChart = rootView.findViewById(R.id.qvar_qvar_chart)
        mDQVARChart = rootView.findViewById(R.id.qvar_dqvar_chart)
        mFlagText = rootView.findViewById(R.id.qvar_flag_text)
        mParameterText = rootView.findViewById(R.id.qvar_param_text)

        mFirstNotificationTimeStamp = System.currentTimeMillis()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // QVAR Plot
        // enable description text
        mQVARChart.description.isEnabled = true
        mQVARChart.description.text ="QVAR"

        // disable touch gestures
        mQVARChart.setTouchEnabled(false)
        // enable scaling and dragging
        mQVARChart.isDragEnabled = true
        mQVARChart.setScaleEnabled(true)
        mQVARChart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mQVARChart.setPinchZoom(false)
        // set an alternative background color
        //mQVARChart.setBackgroundColor(Color.LTGRAY)
        mQVARDataSet = LineDataSet(LinkedList(), "QVAR")
        //Remove the Circles
        mQVARDataSet.setDrawCircles(false)
        mQVARData = LineData(mQVARDataSet)
        mQVARData.setValueTextColor(Color.WHITE)
        // add empty data
        mQVARChart.data = mQVARData

        // remove the legend
        mQVARChart.legend.isEnabled=false
        // remove the label for x-Axe
        mQVARChart.xAxis.setDrawLabels(false)

        mQVARChart.axisRight.isEnabled=false

        //Adding 2 LimitLines for following the Y Max and Min
        mQVARLimitLineYMax = LimitLine(0f, "0");
        mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMax)
        mQVARLimitLineYMin = LimitLine(0f, "0");
        mQVARLimitLineYMin.labelPosition=LimitLabelPosition.LEFT_BOTTOM
        mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMin)

        // DQVAR Plot
        // enable description text
        mDQVARChart.description.isEnabled = true
        mDQVARChart.description.text ="DQVAR"

        // disable touch gestures
        mDQVARChart.setTouchEnabled(false)
        // enable scaling and dragging
        mDQVARChart.isDragEnabled = true
        mDQVARChart.setScaleEnabled(true)
        mDQVARChart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mDQVARChart.setPinchZoom(false)
        // set an alternative background color
        //mQVARChart.setBackgroundColor(Color.LTGRAY)
        mDQVARDataSet = LineDataSet(LinkedList(), "DQVAR")
        //Remove the Circles
        mDQVARDataSet.setDrawCircles(false)
        mDQVARData = LineData(mDQVARDataSet)
        mDQVARData.setValueTextColor(Color.WHITE)
        // add empty data
        mDQVARChart.data = mDQVARData

        // remove the legend
        mDQVARChart.legend.isEnabled=false
        // remove the label for x-Axe
        mDQVARChart.xAxis.setDrawLabels(false)

        mDQVARChart.axisRight.isEnabled=false

        //Adding 2 LimitLines for following the Y Max and Min
        mDQVARLimitLineYMax = LimitLine(0f, "0");
        mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMax)
        mDQVARLimitLineYMin = LimitLine(0f, "0");
        mDQVARLimitLineYMin.labelPosition=LimitLabelPosition.LEFT_BOTTOM
        mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMin)

        mFirstNotificationTimeStamp = System.currentTimeMillis()
    }

    @ExperimentalTime
    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureQVAR::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    @ExperimentalTime
    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureQVAR::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }

    @ExperimentalTime
    private fun LineData.removeEntryOlderThan(timeRange: Duration?){
        if (timeRange == null)
            return
        val plotRangeMs = (xMax-xMin).toDouble().milliseconds
        if(plotRangeMs > timeRange){
            val minValidX = (xMax - timeRange.inMilliseconds).toFloat()
            dataSets.forEach {
                it.removeXLessThan(minValidX)
            }
        }
    }

    private fun ILineDataSet.removeXLessThan(value: Float){
        while (getEntryForIndex(0).x<value){
            removeFirst()
        }
    }
}