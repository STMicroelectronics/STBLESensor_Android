/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.fft_amplitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.core.ARG_NODE_ID
import com.st.fft_amplitude.databinding.FftAmplitudeFragmentBinding
import com.st.fft_amplitude.utilites.LineConf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FFTAmplitudeFragment : Fragment() {
    private val viewModel: FFTAmplitudeViewModel by hiltNavGraphViewModels(R.id.fft_amplitude_nav_graph)
    private val navArgs: FFTAmplitudeFragmentArgs by navArgs()

    private lateinit var binding: FftAmplitudeFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mFFTChart: LineChart

    private lateinit var mRefreshProgress: ProgressBar

    private lateinit var mDetailsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FftAmplitudeFragmentBinding.inflate(inflater, container, false)

        mFFTChart = binding.fftAmplChart
        setUpChart(mFFTChart)

        mRefreshProgress = binding.fftAmplRefreshingProgres
        mRefreshProgress.max=100

        mDetailsButton = binding.fftAmplShowDetails
        mDetailsButton.setOnClickListener{ detailsClicked()}

        return binding.root
    }

    private fun detailsClicked() {
        findNavController().navigate(
            FFTAmplitudeFragmentDirections.actionFFTAmplitudeFragmnetToFFTAmplitudeDataStatsFragment(navArgs.nodeId)
        )
    }


    private fun setUpChart(chart: LineChart) {
        val ctx = chart.context
        //hide right axis
        chart.axisRight.isEnabled = false
        //move x axis on the bottom
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        //hide plot description
        chart.description.isEnabled = false
        chart.setNoDataText(ctx.getString(com.st.fft_amplitude.R.string.fftAmpl_noDataText))
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(),com.st.ui.R.color.labelPlotContrast))
        chart.setTouchEnabled(false)
        val legend = chart.legend
        legend.setDrawInside(true)
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.orientation = Legend.LegendOrientation.VERTICAL
        chart.axisLeft.textColor = ContextCompat.getColor(requireContext(),com.st.ui.R.color.labelPlotContrast)
        chart.xAxis.textColor = ContextCompat.getColor(requireContext(),com.st.ui.R.color.labelPlotContrast)
        chart.legend.textColor = ContextCompat.getColor(requireContext(),com.st.ui.R.color.labelPlotContrast)
    }

    private fun buildDataSet(
        conf: LineConf,
        yData: FloatArray,
        deltaX: Float
    ): LineDataSet {
        val data: MutableList<Entry> = java.util.ArrayList(yData.size)
        var x = 0f
        for (y in yData) {
            data.add(Entry(x, y))
            x += deltaX
        }
        val dataSet = LineDataSet(data, conf.name)
        dataSet.setDrawCircles(false)
        dataSet.color = conf.color
        dataSet.setDrawValues(false)
        return dataSet
    }


    private fun updatePlot(data: List<FloatArray>, frequencySteps: Float) {
        val nComponents = data.size.coerceAtMost(LineConf.LINES.size)
        val dataSets: MutableList<ILineDataSet> = ArrayList(nComponents)
        for (i in 0 until nComponents) {
            val line: LineDataSet = buildDataSet(
                    LineConf.LINES[i],
                    data[i], frequencySteps
                )
            dataSets.add(line)
        }
        val lineData = LineData(dataSets)

        mRefreshProgress.visibility = View.INVISIBLE
        mFFTChart.data = lineData
        mFFTChart.invalidate()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingStatus.collect {
                    if (it >= 100) { // complete
                        mRefreshProgress.visibility = View.INVISIBLE
                    } else {
                        mRefreshProgress.visibility = View.VISIBLE
                        mRefreshProgress.progress = it
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mFftData.collect {
                    updatePlot(it,viewModel.mFreqStep)
                }
            }
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
