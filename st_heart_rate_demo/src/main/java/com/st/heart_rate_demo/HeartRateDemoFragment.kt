/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.heart_rate_demo

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.blue_sdk.features.external.std.HeartRateInfo
import com.st.core.ARG_NODE_ID
import com.st.heart_rate_demo.databinding.HeartRateDemoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class HeartRateDemoFragment : Fragment() {

    private val viewModel: HeartRateDemoViewModel by viewModels()
    private lateinit var binding: HeartRateDemoFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mHeartRateLabel: TextView
    private lateinit var mEnergyExtendedLabel: TextView
    private lateinit var mHeartImage: ImageView
    private lateinit var mEnergyExtendedCard: CardView

    private lateinit var mPulseAnim: AnimatorSet
    private lateinit var sToGrayScale: ColorMatrixColorFilter

    private lateinit var mHeartRateChart: LineChart
    private lateinit var mHeartRateData: LineData
    private lateinit var mHeartRateDataSet: LineDataSet

    private lateinit var mEnergyExtendedChart: LineChart
    private lateinit var mEnergyExtendedData: LineData
    private lateinit var mEnergyExtendedDataSet: LineDataSet

    private var colorLabel by Delegates.notNull<Int>()
    private var colorLineHeartRate by Delegates.notNull<Int>()
    private var colorLineEnergyExtended by Delegates.notNull<Int>()

    private var mFirstNotificationTimeStamp: Long = 0

    private lateinit var bodyView:     HeartRateBodyView
    private lateinit var positionView: AppCompatTextView

    private lateinit var contactSkinIcon: AppCompatImageView
    private lateinit var contactSkinValue:  AppCompatTextView

    private lateinit var rrIntervalsValue: AppCompatTextView
    private lateinit var rrIntervalsIcon: AppCompatImageView


    companion object {
        @ExperimentalTime
        val SECONDS_TO_PLOT_DEFAULT = 20.seconds
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = HeartRateDemoFragmentBinding.inflate(inflater, container, false)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        sToGrayScale = ColorMatrixColorFilter(colorMatrix)

        mHeartRateLabel = binding.heartRateLabel
        mEnergyExtendedLabel = binding.energyExtendedLabel

        mEnergyExtendedCard = binding.energyExtendedCard

        mHeartImage = binding.heartImage

        mHeartRateChart = binding.heartRateChart
        mEnergyExtendedChart = binding.energyExtendedChart

        mHeartImage.setOnClickListener {
            viewModel.readFeatureHeartRate(nodeId = nodeId)
        }

        mPulseAnim = AnimatorInflater.loadAnimator(
            activity,
            R.animator.pulse
        ) as AnimatorSet
        mPulseAnim.setTarget(mHeartImage)

        //Color for Plot Label and axes
        colorLabel = ContextCompat.getColor(requireContext(), com.st.ui.R.color.labelPlotContrast)
        //Color for Lines
        colorLineHeartRate = ContextCompat.getColor(requireContext(), com.st.ui.R.color.ErrorText)
        colorLineEnergyExtended = ContextCompat.getColor(requireContext(), com.st.ui.R.color.InfoText)

        bodyView            = binding.bodySection.heartRateBodyView
        positionView        = binding.bodySection.positionValue
        contactSkinIcon     = binding.bodySection.skinContactIcon
        contactSkinValue    = binding.bodySection.skinContactValue
        rrIntervalsValue = binding.bodySection.rrIntervalsValue
        rrIntervalsIcon = binding.bodySection.rrIntervalsIcon

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Heart Rate Plot
        // enable description text
        //mHeartRateChart.description.isEnabled = true
        //mHeartRateChart.description.text = "HearRate"
        //mHeartRateChart.description.textColor = colorLabel
        //mHeartRateChart.description.textSize = 14f

        //disable description text
        mHeartRateChart.description.isEnabled = false

        // disable touch gestures
        mHeartRateChart.setTouchEnabled(false)
        // enable scaling and dragging
        mHeartRateChart.isDragEnabled = true
        mHeartRateChart.setScaleEnabled(true)
        mHeartRateChart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mHeartRateChart.setPinchZoom(false)
        // set an alternative background color
        //mHeartRateChart.setBackgroundColor(Color.LTGRAY)
        mHeartRateDataSet = LineDataSet(LinkedList(), "bpm")
        //Enable the Circles
        mHeartRateDataSet.setDrawCircles(true)
        mHeartRateDataSet.circleColors = listOf(colorLineHeartRate)
        //Size of the circles (default = 4f)
        //mHeartRateDataSet.circleRadius = 5f

        mHeartRateDataSet.color = colorLineHeartRate
        //Smooth plot
        mHeartRateDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        mHeartRateData = LineData(mHeartRateDataSet)
        //Color of the Text Values
        //mHeartRateData.setValueTextColor(Color.WHITE)
        //Disable the Text Values
        mHeartRateData.setDrawValues(false)
        // add empty data
        mHeartRateChart.data = mHeartRateData

        // remove the legend
        //mHeartRateChart.legend.isEnabled = false

        // remove the label for x-Axis
        mHeartRateChart.xAxis.setDrawLabels(false)

        //Remove the Y Axis on the Right
        mHeartRateChart.axisRight.isEnabled = false
        //Set the Color for the Y Axis on the Left
        mHeartRateChart.axisLeft.textColor = colorLabel

        //Disable grid lines for x/y Axis
        mHeartRateChart.xAxis.setDrawGridLines(false)
        mHeartRateChart.axisLeft.setDrawGridLines(false)

        // Energy Plot
        // enable description text
        //mEnergyExtendedChart.description.isEnabled = true
        //mEnergyExtendedChart.description.text = "Energy"
        //mEnergyExtendedChart.description.textColor = colorLabel
        //mEnergyExtendedChart.description.textSize = 14f

        //disable description text
        mEnergyExtendedChart.description.isEnabled = false

        // disable touch gestures
        mEnergyExtendedChart.setTouchEnabled(false)
        // enable scaling and dragging
        mEnergyExtendedChart.isDragEnabled = true
        mEnergyExtendedChart.setScaleEnabled(true)
        mEnergyExtendedChart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mEnergyExtendedChart.setPinchZoom(false)
        // set an alternative background color
        //mEnergyExtendedChart.setBackgroundColor(Color.LTGRAY)
        mEnergyExtendedDataSet = LineDataSet(LinkedList(), "kJ")
        //Enable the Circles
        mEnergyExtendedDataSet.setDrawCircles(true)
        //Size of the circles (default = 4f)
        //mEnergyExtendedDataSet.circleRadius = 5f
        mEnergyExtendedDataSet.circleColors = listOf(colorLineEnergyExtended)
        mEnergyExtendedDataSet.color = colorLineEnergyExtended

        //Smooth plot
        mEnergyExtendedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        mEnergyExtendedData = LineData(mEnergyExtendedDataSet)
        //Color of the Text Values
        //mEnergyExtendedData.setValueTextColor(Color.WHITE)
        //Disable the Text Values
        mEnergyExtendedData.setDrawValues(false)
        // add empty data
        mEnergyExtendedChart.data = mEnergyExtendedData

        // remove the legend
        //mEnergyExtendedChart.legend.isEnabled = false

        // remove the label for x-Axis
        mEnergyExtendedChart.xAxis.setDrawLabels(false)

        //Remove the Y Axis on the Right
        mEnergyExtendedChart.axisRight.isEnabled = false
        //Set the Color for the Y Axis on the Left
        mEnergyExtendedChart.axisLeft.textColor = colorLabel

        //Disable grid lines for x/y Axis
        mEnergyExtendedChart.xAxis.setDrawGridLines(false)
        mEnergyExtendedChart.axisLeft.setDrawGridLines(false)

        mFirstNotificationTimeStamp = System.currentTimeMillis()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.heartData.collect {
                    updateGui(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationData.collect {

                    val bodySensorLocation = it.bodySensorLocation.value
                    bodyView.bodySensorLocation = bodySensorLocation
                    positionView.text = bodySensorLocation.name
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun updateGui(it: HeartRateInfo) {
        val heartRate = it.heartRate.value
        val energy = it.energyExpended.value
        val rrInterval = it.rrInterval.value

        val actualTimeStamp = System.currentTimeMillis()

        if(heartRate<=0) {
            mHeartRateLabel.text = ""
            mHeartImage.colorFilter = sToGrayScale
        } else {
            val yData = heartRate.toFloat()
            mHeartRateData.addEntry(
                Entry(
                    (actualTimeStamp - mFirstNotificationTimeStamp).toFloat(),
                    yData
                ), 0
            )
            mHeartRateData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

            mHeartRateData.notifyDataChanged()
            mHeartRateChart.notifyDataSetChanged()
            mHeartRateChart.invalidate()

            mHeartRateLabel.text = getString(R.string.heartRateDataFormat, heartRate,it.heartRate.unit)
            mHeartImage.colorFilter = null

            if(energy>0) {
                mEnergyExtendedCard.visibility = View.VISIBLE
                val yEnergyData = energy.toFloat()
                mEnergyExtendedData.addEntry(
                    Entry(
                        (actualTimeStamp - mFirstNotificationTimeStamp).toFloat(),
                        yEnergyData
                    ), 0
                )
                mEnergyExtendedData.removeEntryOlderThan(SECONDS_TO_PLOT_DEFAULT)

                mEnergyExtendedData.notifyDataChanged()
                mEnergyExtendedChart.notifyDataSetChanged()
                mEnergyExtendedChart.invalidate()

                mEnergyExtendedLabel.text =
                    getString(R.string.energyExpendedDataFormat, energy, it.energyExpended.unit)

                if(it.skinContactDetected.value) {
                    contactSkinValue.text = requireContext().getString(R.string.yes)
                    contactSkinIcon.setImageResource(R.drawable.ic_baseline_check_24)
                    contactSkinIcon.setColorFilter(ContextCompat.getColor(requireContext(), com.st.ui.R.color.SuccessText))
                } else {
                    contactSkinValue.text = requireContext().getString(R.string.no)
                    contactSkinIcon.setImageResource(R.drawable.ic_baseline_close_24)
                    contactSkinIcon.setColorFilter(ContextCompat.getColor(requireContext(), com.st.ui.R.color.ErrorText))
                }
            } else {
                mEnergyExtendedLabel.text=""
                contactSkinIcon.setImageResource(R.drawable.ic_baseline_block_24)
                contactSkinValue.text = requireContext().getString(R.string.not_supported)
                contactSkinIcon.setColorFilter(ContextCompat.getColor(requireContext(), com.st.ui.R.color.ErrorText))
            }
            if(!rrInterval.isNaN()) {
                rrIntervalsValue.text = getString(R.string.rrIntervalDataFormat, rrInterval,it.rrInterval.unit)
                rrIntervalsIcon.visibility = View.GONE
            } else {
                rrIntervalsIcon.visibility = View.VISIBLE
                rrIntervalsIcon.setColorFilter(ContextCompat.getColor(requireContext(), com.st.ui.R.color.ErrorText))
                rrIntervalsIcon.setImageResource(R.drawable.ic_baseline_block_24)
                rrIntervalsValue.text = requireContext().getString(R.string.not_supported)
            }
            if(!mPulseAnim.isRunning) {
                mPulseAnim.start()
            }
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
