/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.plot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.plot.utils.PLOTTABLE_FEATURE
import com.st.plot.utils.PlotBoundary
import com.st.plot.utils.fieldsDesc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class PlotSettingsViewModel
@Inject internal constructor(
    private val blueManager: BlueManager
) : ViewModel() {
    companion object {
        private const val SECONDS_TO_PLOT_DEFAULT = 5
    }

    private var feature: Feature<*>? = null
    var isExpert: Boolean = false

    private var _selectedFeature = MutableLiveData<Feature<*>>()
    val selectedFeature: LiveData<Feature<*>>
        get() = _selectedFeature

    private var _selectedFeatureIndex = MutableLiveData<Int>()
    val selectedFeatureIndex: LiveData<Int>
        get() = _selectedFeatureIndex

    private var _plotDuration = MutableLiveData(SECONDS_TO_PLOT_DEFAULT.seconds)
    val plotDuration: LiveData<Duration>
        get() = _plotDuration

    private var _legendItems = MutableLiveData<Array<String>>()
    val legendItems: LiveData<Array<String>>
        get() = _legendItems

    private var _yAxisLabel = MutableLiveData<String?>()
    val yAxisLabel: LiveData<String?>
        get() = _yAxisLabel

    private var _plotBoundaries = MutableLiveData<PlotBoundary>()
    val plotBoundary: MutableLiveData<PlotBoundary>
        get() = _plotBoundaries

    fun init(nodeId: String, isExpert: Boolean) {
        this.isExpert = isExpert
        viewModelScope.launch {
            plottableFeature =
                blueManager.nodeFeatures(nodeId = nodeId).filter {
                    PLOTTABLE_FEATURE.contains(it.name)
                }

            supportedFeature.value = plottableFeature.map { it.name }
        }
    }

    private var plottableFeature: List<Feature<*>> = emptyList()

    var supportedFeature: MutableLiveData<List<String>> = MutableLiveData()

    fun changeMimimumYValue(minimum: Float?) {
        val currentValue = _plotBoundaries.value ?: PlotBoundary()
        val newValue = currentValue.copy(min = minimum)
        _plotBoundaries.postValue(newValue)
    }

    fun changeMaximumYValue(maximum: Float?) {
        val currentValue = _plotBoundaries.value ?: PlotBoundary()
        val newValue = currentValue.copy(max = maximum)
        _plotBoundaries.postValue(newValue)
    }

    fun changePlotDuration(newDuration: Duration?) {
        _plotDuration.postValue(newDuration ?: SECONDS_TO_PLOT_DEFAULT.seconds)
    }

    fun enableAutoScale() {
        _plotBoundaries.postValue(PlotBoundary())
    }

    fun startPlotSelectedFeature() {
        val f = _selectedFeature.value ?: return

        val items = f.fieldsDesc()
        _legendItems.postValue(items.keys.toTypedArray())
        val unit = items.values.firstOrNull()
        if (unit.isNullOrEmpty()) {
            _yAxisLabel.postValue(f.name)
        } else {
            _yAxisLabel.postValue("${f.name} (${unit})")
        }
    }

    fun onSelectedIndex(index: Int) {
        if (_selectedFeature.value != plottableFeature[index]) {
            val feature = plottableFeature[index]
            _selectedFeature.postValue(feature)
            _selectedFeatureIndex.postValue(index)
            _plotBoundaries.postValue(PlotBoundary.getDefaultFor(feature.name))
        }
    }
}