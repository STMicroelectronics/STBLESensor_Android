package com.st.fft_amplitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.core.ARG_NODE_ID
import com.st.fft_amplitude.databinding.FragmentFftStatBinding
import com.st.fft_amplitude.utilites.LineConf
import com.st.fft_amplitude.utilites.TimeDomainStats
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class FFTAmplitudeDataStatsFragment : DialogFragment() {

    private val viewModel: FFTAmplitudeViewModel by hiltNavGraphViewModels(R.id.fft_amplitude_nav_graph)

    private lateinit var binding: FragmentFftStatBinding
    private lateinit var nodeId: String

    private var mFreqStats: Array<TextView?> = Array(3) {null}
    private lateinit var mTimeStatsX: TextView
    private lateinit var mTimeStatsY: TextView
    private lateinit var mTimeStatsZ: TextView

    private lateinit var mTimeStatsLinearLayout: LinearLayout
    private lateinit var mTimeStatsNotAvailable: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FragmentFftStatBinding.inflate(inflater, container, false)

        mFreqStats[0] = binding.fftAmplXData
        mFreqStats[1] = binding.fftAmplYData
        mFreqStats[2] = binding.fftAmplZData

        mTimeStatsX = binding.fftAmplXTimeData
        mTimeStatsY = binding.fftAmplYTimeData
        mTimeStatsZ = binding.fftAmplZTimeData

        mTimeStatsLinearLayout = binding.fftTimeDomainAvailable
        mTimeStatsNotAvailable = binding.fftTimeDomainNotAvailable

        return binding.root
    }

    private fun updateTimeDomainLabel(label: TextView, name: String, data: TimeDomainStats?) {
        if (data == null) {
            label.setText(R.string.fftDetails_timeInfo_not_available)
        } else {
            val xData = getString(
                R.string.fftDetails_timeInfo_format, name,
                data.accPeak, "m/s^2",
                data.rmsSpeed, "mm/s"
            )
            label.text = xData
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //FFTUpdate
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mFftMax.collect {
                   val lines = LineConf.LINES
                    var nComponents = minOf(lines.size,it.size)
                    nComponents = minOf(nComponents,mFreqStats.size)

                    for (i in 0 until nComponents) {
                        val max = it[i]
                        if(mFreqStats[i]!=null) {
                            mFreqStats[i]!!.text = getString(
                                R.string.fftDetails_freqInfo_format,
                                lines[i].name, max.amplitude, max.frequency
                            )
                        }
                    }
                }
            }
        }

        //TimeDomainUpdate
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mXStats.collect {
                    updateTimeDomainLabel(mTimeStatsX,"X",it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mYStats.collect {
                    updateTimeDomainLabel(mTimeStatsY,"Y",it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mZStats.collect {
                    updateTimeDomainLabel(mTimeStatsZ,"Z",it)
                }
            }
        }

        viewModel.timeParameterAvailable.observe(this) { value ->
            if (value) {
                mTimeStatsLinearLayout.visibility = View.VISIBLE
                mTimeStatsNotAvailable.visibility = View.GONE
            } else {
                mTimeStatsLinearLayout.visibility = View.GONE
                mTimeStatsNotAvailable.visibility = View.VISIBLE
            }
        }
    }
}