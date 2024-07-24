/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.medical_signal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.blue_sdk.features.extended.medical_signal.MedicalInfo
import com.st.core.ARG_NODE_ID
import com.st.medical_signal.R.array.dataSetColor
import com.st.medical_signal.databinding.MedicalSignalFragmentBinding
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
class MedicalSignalFragment : Fragment() {

    private val viewModel: MedicalSignalViewModel by viewModels()
    private lateinit var nodeId: String
    private lateinit var binding: MedicalSignalFragmentBinding

    private lateinit var mMed24Card: CardView
    private lateinit var mMed24Chart: LineChart

    private lateinit var mMed16Card: CardView
    private lateinit var mMed16Chart: LineChart

    private lateinit var mMed24Data: LineData
    private lateinit var mMed16Data: LineData

    private lateinit var mMed24StartStop: Button
    private lateinit var mMed16StartStop: Button
    private lateinit var mMedResetZoom: Button

    private var colorLabel by Delegates.notNull<Int>()
    private var colorLimit by Delegates.notNull<Int>()
    private lateinit var mLineColors: IntArray

    private var prevMed24Info: MedicalInfo?=null
    private var firstMed24InternalTimeStamp: Int?=null

    private var prevMed16Info: MedicalInfo?=null
    private var firstMed16InternalTimeStamp: Int?=null

    private lateinit var syntheticScrollView: ScrollView
    private lateinit var syntheticText: TextView

    private lateinit var mMed16Description: TextView
    private lateinit var mMed24Description: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = MedicalSignalFragmentBinding.inflate(inflater, container, false)

        //Color for Plot Label and axes
        colorLabel = ContextCompat.getColor(requireContext(), com.st.ui.R.color.labelPlotContrast)
        //Color for Limit Lines
        colorLimit = ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorAccent)
        //Color for Lines
        mLineColors = resources.getIntArray(dataSetColor)

        //Med16
        mMed16Card = binding.medSig16bitCard
        mMed16Chart = binding.medSig16bitChart
        mMed16StartStop = binding.medSig16bitStartTop

        mMed16StartStop.setOnClickListener { startStopMed16() }

        //Med24
        mMed24Card = binding.medSig24bitCard
        mMed24Chart = binding.medSig24bitChart
        mMed24StartStop = binding.medSig24bitStartTop

        mMed24StartStop.setOnClickListener { startStopMed24() }

        //Synthetic data
        syntheticScrollView = binding.medSigSyntheticScrollview
        syntheticText = binding.medSigSyntheticText

        mMedResetZoom = binding.medSig16bitResetZoom
        mMedResetZoom.setOnClickListener { resetChartsZoom() }


        mMed16Description = binding.medSig16bitText
        mMed24Description = binding.medSig24bitText

        return binding.root
    }

    private fun resetChartsZoom() {
        resetZoom(mMed16Chart)
        resetZoom(mMed24Chart)
    }

    private fun startStopMed24() {
        if(viewModel.isMed24Streaming) {
            viewModel.stopMed24(nodeId)
            mMed24StartStop.text = resources.getString(com.st.medical_signal.R.string.med24_button_start_descr)
            prevMed24Info = null
        } else {
            viewModel.startMed24(nodeId)
            mMed24StartStop.text = resources.getString(com.st.medical_signal.R.string.med24_button_stop_descr)
        }
    }

    private fun startStopMed16() {
        if(viewModel.isMed16Streaming) {
            viewModel.stopMed16(nodeId)
            mMed16StartStop.text = resources.getString(com.st.medical_signal.R.string.med16_button_start_desc)

            prevMed16Info = null
        } else {
            viewModel.startMed16(nodeId)
            mMed16StartStop.text = resources.getString(com.st.medical_signal.R.string.med16_button_stop_desc)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Med16
        // enable description text
        mMed16Chart.description.isEnabled = false
        //mMed16Chart.description.text = "Med16"
        mMed16Chart.description.textColor = colorLabel
        mMed16Chart.description.textSize = 14f

        // disable touch gestures
        mMed16Chart.setTouchEnabled(true)
        // enable scaling and dragging
        mMed16Chart.isDragEnabled = true
        mMed16Chart.setScaleEnabled(true)
        mMed16Chart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mMed16Chart.setPinchZoom(true)


        // remove the label for x-Axe
        mMed16Chart.xAxis.setDrawLabels(false)

        //Remove the Y Axe on the Right
        mMed16Chart.axisRight.isEnabled = false
        //Set the Color for the Y Axe on the Left
        mMed16Chart.axisLeft.textColor = colorLabel

        mMed16Chart.setNoDataText("Waiting Sample")
        mMed16Chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorAccent))

        //Med24
        // enable description text
        mMed24Chart.description.isEnabled = false
        //mMed24Chart.description.text = "Med24"
        mMed24Chart.description.textColor = colorLabel
        mMed24Chart.description.textSize = 14f

        // disable touch gestures
        mMed24Chart.setTouchEnabled(true)
        // enable scaling and dragging
        mMed24Chart.isDragEnabled = true
        mMed24Chart.setScaleEnabled(true)
        mMed24Chart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mMed24Chart.setPinchZoom(true)


        // remove the label for x-Axe
        mMed24Chart.xAxis.setDrawLabels(false)

        //Remove the Y Axe on the Right
        mMed24Chart.axisRight.isEnabled = false
        //Set the Color for the Y Axe on the Left
        mMed24Chart.axisLeft.textColor = colorLabel

        mMed24Chart.setNoDataText("Waiting Sample")
        mMed24Chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorAccent))

        val mv = CustomMarkerView(requireContext(), R.layout.custom_marker)
        // set the marker to the chart
        mMed16Chart.marker = mv
        mMed24Chart.marker = mv

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataFeature24.collect {
                    updateMed24Gui(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataFeature16.collect {
                    updateMed16Gui(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syntheticData.collect {
                    syntheticScrollView.visibility = View.VISIBLE
                    syntheticText.text = it
                }
            }
        }
    }

    private fun resetZoom(chart: LineChart){
        chart.fitScreen()
        chart.invalidate()
    }

    @OptIn(ExperimentalTime::class)
    private fun updateMed24Gui(curMedInfo: MedicalInfo?) {
        curMedInfo?.let { curr ->
            mMed24Card.visibility = View.VISIBLE
            mMed24StartStop.isEnabled = true
            mMedResetZoom.isEnabled = true
            mMed24StartStop.visibility = View.VISIBLE

            //mMed24Chart.description.text = curMedInfo.sigType.value.description
            var string = curMedInfo.sigType.value.description
            if(curMedInfo.sigType.value.yMeasurementUnit!=null) {
                string += " [" + curMedInfo.sigType.value.yMeasurementUnit+"]"
            }
            mMed24Description.text = string

            val yAxis = mMed24Chart.axisLeft
            if(curMedInfo.sigType.value.nLabels!=0) {
                yAxis.labelCount = curMedInfo.sigType.value.nLabels
            }

            mMed24Chart.setScaleEnabled(curMedInfo.sigType.value.isAutoscale)
            if(!curMedInfo.sigType.value.isAutoscale) {
                yAxis.apply {
                    axisMaximum = curMedInfo.sigType.value.maxGraphValue.toFloat()
                    axisMinimum = curMedInfo.sigType.value.minGraphValue.toFloat()
                }
            }

            if (prevMed24Info == null) {
                //We don't draw nothing... only save data
                prevMed24Info = curr
                firstMed24InternalTimeStamp = curr.internalTimeStamp.value

                if(curr.sigType.value.numberOfSignals>1) {
                    mMed24Chart.legend.isEnabled = true
                    val dataSets = ArrayList<ILineDataSet>()
                    for (i in 0 until curr.sigType.value.numberOfSignals) {
                        val mMed24DataSet = LineDataSet(LinkedList(), curr.sigType.value.signalLabels.getOrNull(i) ?: "Med24_${i}")
                        //Remove the Circles
                        mMed24DataSet.setDrawCircles(false)

                        if(curr.sigType.value.cubicInterpolation) {
                            mMed24DataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                            mMed24DataSet.setCubicIntensity(0.2f)
                        }

                        mMed24DataSet.color = mLineColors[i%mLineColors.size]
                        dataSets.add(mMed24DataSet)
                    }
                    mMed24Data = LineData(dataSets)


                } else {
                    val mMed24DataSet = LineDataSet(LinkedList(), "Med24")
                    //Remove the Circles
                    mMed24DataSet.setDrawCircles(false)

                    if(curr.sigType.value.cubicInterpolation) {
                        mMed24DataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                        mMed24DataSet.setCubicIntensity(0.2f)
                    }

                    mMed24DataSet.color = mLineColors[0]
                    mMed24Data = LineData(mMed24DataSet)
                }
                //Show/hide legend
                mMed24Chart.legend.isEnabled = curr.sigType.value.showLegend
                //Disable the Text Values
                mMed24Data.setDrawValues(false)
                // add empty data
                mMed24Chart.data = mMed24Data
            } else {
                //Compute the delta Time respect the previous Sample
                val timeDiff = curr.internalTimeStamp.value - prevMed24Info!!.internalTimeStamp.value
                //This is the delta time between Samples
                val deltaBetweenSample = timeDiff * prevMed24Info!!.sigType.value.numberOfSignals / prevMed24Info!!.values.value.size

                //Fill the data..
                if(prevMed24Info!!.sigType.value.numberOfSignals>1) {
                    val dataSets = prevMed24Info!!.values.value.chunked(prevMed24Info!!.sigType.value.numberOfSignals)
                    dataSets.forEachIndexed { indexSet, dataSet ->
                        dataSet.forEachIndexed { index, data ->
                            mMed24Data.addEntry(
                                Entry(
                                    (prevMed24Info!!.internalTimeStamp.value + deltaBetweenSample * indexSet - firstMed24InternalTimeStamp!!).toFloat(),
                                    data.toFloat()
                                ), index
                            )
                        }
                    }
                } else {
                    prevMed24Info!!.values.value.forEachIndexed { index, data ->
                        mMed24Data.addEntry(
                            Entry(
                                (prevMed24Info!!.internalTimeStamp.value + deltaBetweenSample * index - firstMed24InternalTimeStamp!!).toFloat(),
                                data.toFloat()
                            ), 0
                        )
                    }
                }

                mMed24Data.removeEntryOlderThan(prevMed24Info!!.sigType.value.displayWindowTimeSecond.seconds)

                mMed24Data.notifyDataChanged()
                mMed24Chart.notifyDataSetChanged()
                mMed24Chart.invalidate()

                prevMed24Info = curr
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun updateMed16Gui(curMedInfo: MedicalInfo?) {
        curMedInfo?.let { curr ->
            mMed16Card.visibility = View.VISIBLE
            mMed16StartStop.isEnabled = true
            mMedResetZoom.isEnabled = true
            mMed16StartStop.visibility = View.VISIBLE

            //mMed16Chart.description.text = curMedInfo.sigType.value.description
            var string = curMedInfo.sigType.value.description
            if(curMedInfo.sigType.value.yMeasurementUnit!=null) {
                string += " [" + curMedInfo.sigType.value.yMeasurementUnit+"]"
            }
            mMed16Description.text = string

            val yAxis = mMed16Chart.axisLeft
            if(curMedInfo.sigType.value.nLabels!=0) {
                yAxis.labelCount = curMedInfo.sigType.value.nLabels
            }

            mMed16Chart.setScaleEnabled(curMedInfo.sigType.value.isAutoscale)
            if(!curMedInfo.sigType.value.isAutoscale) {
                yAxis.apply {
                    axisMaximum = curMedInfo.sigType.value.maxGraphValue.toFloat()
                    axisMinimum = curMedInfo.sigType.value.minGraphValue.toFloat()
                }
            }

            if (prevMed16Info == null) {
                //We don't draw nothing... only save data
                prevMed16Info = curr
                firstMed16InternalTimeStamp = curr.internalTimeStamp.value

                if(curr.sigType.value.numberOfSignals>1) {
                    mMed16Chart.legend.isEnabled = true
                    val dataSets = ArrayList<ILineDataSet>()
                  for (i in 0 until curr.sigType.value.numberOfSignals) {
                      val mMed16DataSet = LineDataSet(LinkedList(), curr.sigType.value.signalLabels.getOrNull(i) ?: "Med16_${i}")
                      //Remove the Circles
                      mMed16DataSet.setDrawCircles(false)

                      if(curr.sigType.value.cubicInterpolation) {
                          mMed16DataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                          mMed16DataSet.setCubicIntensity(0.2f)
                      }

                      mMed16DataSet.color = mLineColors[i%mLineColors.size]
                      dataSets.add(mMed16DataSet)

                  }
                    mMed16Data = LineData(dataSets)
                } else {
                    val mMed16DataSet = LineDataSet(LinkedList(), "Med16")
                    //Remove the Circles
                    mMed16DataSet.setDrawCircles(false)

                    if(curr.sigType.value.cubicInterpolation) {
                        mMed16DataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                        mMed16DataSet.setCubicIntensity(0.2f)
                    }

                    mMed16DataSet.color = mLineColors[0]
                    mMed16Data = LineData(mMed16DataSet)
                }
                //Show/hide legend
                mMed16Chart.legend.isEnabled = curr.sigType.value.showLegend
                mMed16Chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT

                //Disable the Text Values
                mMed16Data.setDrawValues(false)
                // add empty data
                mMed16Chart.data = mMed16Data
            } else {
                //Compute the delta Time respect the previous Sample
                val timeDiff = curr.internalTimeStamp.value - prevMed16Info!!.internalTimeStamp.value

                //This is the delta time between Samples
                val deltaBetweenSample = timeDiff * prevMed16Info!!.sigType.value.numberOfSignals / prevMed16Info!!.values.value.size

                //Fill the data..
                if(prevMed16Info!!.sigType.value.numberOfSignals>1) {
                    val dataSets = prevMed16Info!!.values.value.chunked(prevMed16Info!!.sigType.value.numberOfSignals)
                    dataSets.forEachIndexed { indexSet, dataSet ->
                        dataSet.forEachIndexed { index, data ->
                            mMed16Data.addEntry(
                                Entry(
                                    (prevMed16Info!!.internalTimeStamp.value + deltaBetweenSample * indexSet - firstMed16InternalTimeStamp!!).toFloat(),
                                    data.toFloat()
                                ), index
                            )
                        }
                    }
                } else {
                    prevMed16Info!!.values.value.forEachIndexed { index, data ->
                        mMed16Data.addEntry(
                            Entry(
                                (prevMed16Info!!.internalTimeStamp.value + deltaBetweenSample * index - firstMed16InternalTimeStamp!!).toFloat(),
                                data.toFloat()
                            ), 0
                        )
                    }
                }

                mMed16Data.removeEntryOlderThan(prevMed16Info!!.sigType.value.displayWindowTimeSecond.seconds)

                mMed16Data.notifyDataChanged()
                mMed16Chart.notifyDataSetChanged()
                mMed16Chart.invalidate()

                prevMed16Info = curr
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
