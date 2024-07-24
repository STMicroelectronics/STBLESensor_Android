/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.electric_charge_variation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.blue_sdk.features.extended.qvar.QVARInfo
import com.st.ui.R
import com.st.core.ARG_NODE_ID
import com.st.electric_charge_variation.databinding.ElectricChargeVariationFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class ElectricChargeVariationFragment : Fragment() {

    private val viewModel: ElectricChargeVariationViewModel by viewModels()
    private lateinit var binding: ElectricChargeVariationFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mQVARCard: CardView
    private lateinit var mFlagCard: CardView
    private lateinit var mDQVARCard: CardView
    private lateinit var mParameterCard: CardView

    private lateinit var mFlagText: TextView
    private lateinit var mParameterText: TextView

    private lateinit var mQVARChart: LineChart
    private lateinit var mQVARData: LineData
    private lateinit var mQVARDataSet: LineDataSet
    private lateinit var mQVARLimitLineYMax: LimitLine
    private lateinit var mQVARLimitLineYMin: LimitLine
    private var mQVARYMax: Float = 0f
    private var mQVARYMin: Float = 0f

    private lateinit var mDQVARChart: LineChart
    private lateinit var mDQVARData: LineData
    private lateinit var mDQVARDataSet: LineDataSet
    private lateinit var mDQVARLimitLineYMax: LimitLine
    private lateinit var mDQVARLimitLineYMin: LimitLine
    private var mDQVARYMax: Float = 0f
    private var mDQVARYMin: Float = 0f

    private var colorLabel by Delegates.notNull<Int>()
    private var colorLimit by Delegates.notNull<Int>()
    private var colorLine by Delegates.notNull<Int>()

    private var mFirstNotificationTimeStamp: Long = 0

    companion object {
        const val TAG = "ElectricChargeVarationFragment"

        @ExperimentalTime
        val SECONDS_TO_PLOT_DEFAULT = 5.seconds
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = ElectricChargeVariationFragmentBinding.inflate(inflater, container, false)


        mQVARCard = binding.qvarQvarCard
        mFlagCard = binding.qvarFlagCard
        mDQVARCard = binding.qvarDqvarCard
        mParameterCard = binding.qvarParamCard

        mQVARChart = binding.qvarQvarChart
        mDQVARChart = binding.qvarDqvarChart
        mFlagText = binding.qvarFlagText
        mParameterText = binding.qvarParamText

        //Color for Plot Label and axes
        colorLabel = ContextCompat.getColor(requireContext(), R.color.labelPlotContrast)
        //Color for Limit Lines
        colorLimit = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        //Color for Lines
        colorLine = ContextCompat.getColor(requireContext(), R.color.green)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // QVAR Plot
        // enable description text
        mQVARChart.description.isEnabled = true
        mQVARChart.description.text = "QVAR"
        mQVARChart.description.textColor = colorLabel
        mQVARChart.description.textSize = 14f

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
        mQVARDataSet.color = colorLine
        mQVARData = LineData(mQVARDataSet)
        //Color of the Text Values
        //mQVARData.setValueTextColor(Color.WHITE)
        //Disable the Text Values
        mQVARData.setDrawValues(false)
        // add empty data
        mQVARChart.data = mQVARData

        // remove the legend
        mQVARChart.legend.isEnabled = false
        // remove the label for x-Axe
        mQVARChart.xAxis.setDrawLabels(false)

        //Remove the Y Axe on the Right
        mQVARChart.axisRight.isEnabled = false
        //Set the Color for the Y Axe on the Left
        mQVARChart.axisLeft.textColor = colorLabel

        //Adding 2 LimitLines for following the Y Max and Min
        mQVARLimitLineYMax = LimitLine(0f, "0")
        mQVARLimitLineYMax.lineColor = colorLimit
        mQVARLimitLineYMax.textColor = colorLabel
        mQVARLimitLineYMax.textSize = 14f

        mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMax)
        mQVARLimitLineYMin = LimitLine(0f, "0")
        mQVARLimitLineYMin.lineColor = colorLimit
        mQVARLimitLineYMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
        mQVARLimitLineYMin.textColor = colorLabel
        mQVARLimitLineYMin.textSize = 14f
        mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMin)

        // DQVAR Plot
        // enable description text
        mDQVARChart.description.isEnabled = true
        mDQVARChart.description.text = "DQVAR"
        mDQVARChart.description.textColor = colorLabel
        mDQVARChart.description.textSize = 14f

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
        mDQVARDataSet.color = colorLine
        mDQVARData = LineData(mDQVARDataSet)
        //Color of the Text Values
        //mDQVARData.setValueTextColor(Color.WHITE)
        //Disable the Text Values
        mDQVARData.setDrawValues(false)
        // add empty data
        mDQVARChart.data = mDQVARData

        // remove the legend
        mDQVARChart.legend.isEnabled = false
        // remove the label for x-Axe
        mDQVARChart.xAxis.setDrawLabels(false)

        //Remove the Y Axe on the Right
        mDQVARChart.axisRight.isEnabled = false
        //Set the Color for the Y Axe on the Left
        mDQVARChart.axisLeft.textColor = colorLabel

        //Adding 2 LimitLines for following the Y Max and Min
        mDQVARLimitLineYMax = LimitLine(0f, "0")
        mDQVARLimitLineYMax.lineColor = colorLimit
        mDQVARLimitLineYMax.textColor = colorLabel
        mDQVARLimitLineYMax.textSize = 14f
        mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMax)
        mDQVARLimitLineYMin = LimitLine(0f, "0")
        mDQVARLimitLineYMin.lineColor = colorLimit
        mDQVARLimitLineYMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
        mDQVARLimitLineYMin.textColor = colorLabel
        mDQVARLimitLineYMin.textSize = 14f
        mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMin)

        mFirstNotificationTimeStamp = System.currentTimeMillis()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.qvarData.collect {
                    updateGui(it)
                }
            }
        }

    }

    @OptIn(ExperimentalTime::class)
    private fun updateGui(it: QVARInfo) {
        val mQVAR = it.qvar.value
        val mFlag = it.flag.value
        val mDQVAR = it.dqvar.value
        val mParameter = it.param.value

        val actualTimeStamp = System.currentTimeMillis()

        mQVARCard.visibility = View.VISIBLE
        var yData = mQVAR.toFloat()
        mQVARData.addEntry(
            Entry(
                (actualTimeStamp - mFirstNotificationTimeStamp).toFloat(),
                yData
            ), 0
        )
        mQVARData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

        var yMax = mQVARDataSet.yMax
        var yMin = mQVARDataSet.yMin

        //Update the LimitLine for MaxY value
        if (yMax != mQVARYMax) {
            mQVARYMax = yMax
            mQVARChart.axisLeft.removeLimitLine(mQVARLimitLineYMax)
            mQVARLimitLineYMax = LimitLine(yMax, yMax.toString())
            mQVARLimitLineYMax.lineColor = colorLimit
            mQVARLimitLineYMax.textColor = colorLabel
            mQVARLimitLineYMax.textSize = 14f
            mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMax)
        }

        //Update the LimitLine for MinY value
        if (yMin != mQVARYMin) {
            mQVARYMin = yMin
            mQVARChart.axisLeft.removeLimitLine(mQVARLimitLineYMin)
            mQVARLimitLineYMin = LimitLine(yMin, yMin.toString())
            mQVARLimitLineYMin.lineColor = colorLimit
            mQVARLimitLineYMin.textColor = colorLabel
            mQVARLimitLineYMin.textSize = 14f
            mQVARLimitLineYMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
            mQVARChart.axisLeft.addLimitLine(mQVARLimitLineYMin)
        }

        mQVARData.notifyDataChanged()
        mQVARChart.notifyDataSetChanged()
        mQVARChart.invalidate()


        if (mFlag != null) {
            mFlagCard.visibility = View.VISIBLE
            mFlagText.text = String.format("Flag\n0x%X", mFlag)
        } else {
            mFlagCard.visibility = View.GONE
        }

        if (mDQVAR != null) {
            mDQVARCard.visibility = View.VISIBLE
            yData = mDQVAR.toFloat()
            mDQVARData.addEntry(
                Entry(
                    (actualTimeStamp - mFirstNotificationTimeStamp).toFloat(),
                    yData
                ), 0
            )
            mDQVARData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

            yMax = mDQVARDataSet.yMax
            yMin = mDQVARDataSet.yMin

            //Update the LimitLine for MaxY value
            if (yMax != mDQVARYMax) {
                mDQVARYMax = yMax
                mDQVARChart.axisLeft.removeLimitLine(mDQVARLimitLineYMax)
                mDQVARLimitLineYMax = LimitLine(yMax, yMax.toString())
                mDQVARLimitLineYMax.lineColor = colorLimit
                mDQVARLimitLineYMax.textColor = colorLabel
                mDQVARLimitLineYMax.textSize = 14f
                mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMax)
            }

            //Update the LimitLine for MinY value
            if (yMin != mDQVARYMin) {
                mDQVARYMin = yMin
                mDQVARChart.axisLeft.removeLimitLine(mDQVARLimitLineYMin)
                mDQVARLimitLineYMin = LimitLine(yMin, yMin.toString())
                mDQVARLimitLineYMin.lineColor = colorLimit
                mDQVARLimitLineYMin.textColor = colorLabel
                mDQVARLimitLineYMin.textSize = 14f
                mDQVARLimitLineYMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
                mDQVARChart.axisLeft.addLimitLine(mDQVARLimitLineYMin)
            }

            mDQVARData.notifyDataChanged()
            mDQVARChart.notifyDataSetChanged()
            mDQVARChart.invalidate()

        } else {
            mDQVARCard.visibility = View.GONE
        }

        if (mParameter != null) {
            mParameterCard.visibility = View.VISIBLE
            mParameterText.text = String.format("Parameter\n0x%X", mParameter)
        } else {
            mParameterCard.visibility = View.GONE
        }
    }

    @ExperimentalTime
    private fun LineData.removeEntryOlderThan(timeRange: Duration?) {
        if (timeRange == null)
            return
        val plotRangeMs = (xMax - xMin).toDouble().milliseconds
        if (plotRangeMs > timeRange) {
            val minValidX = (xMax - timeRange.toDouble(DurationUnit.MILLISECONDS)).toFloat()
            dataSets.forEach {
                it.removeXLessThan(minValidX)
            }
        }
    }

    private fun ILineDataSet.removeXLessThan(value: Float) {
        while (getEntryForIndex(0).x < value) {
            removeFirst()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
