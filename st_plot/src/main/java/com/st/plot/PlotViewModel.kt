package com.st.plot

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.plot.utils.PLOTTABLE_FEATURE
import com.st.plot.utils.PlotBoundary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import javax.inject.Inject

@HiltViewModel
class PlotViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    companion object {
        private const val SECONDS_TO_PLOT_DEFAULT = 5
        private const val MIN_DEFAULT = -100.0f
        private const val MAX_DEFAULT = 100.0f
    }

    var snap: Bitmap? = null

    private val _plottableFeatures =
        MutableStateFlow<List<Feature<*>>>(listOf())
    val plottableFeatures: StateFlow<List<Feature<*>>>
        get() = _plottableFeatures.asStateFlow()

    private var privatePlottableFeatures: MutableList<Feature<*>> = mutableListOf()

    private val _selectedFeature =
        MutableStateFlow<Feature<*>?>(null)
    val selectedFeature: StateFlow<Feature<*>?>
        get() = _selectedFeature.asStateFlow()

    private val _featureUpdate =
        MutableStateFlow<FeatureUpdate<*>?>(null)
    val featureUpdate: StateFlow<FeatureUpdate<*>?>
        get() = _featureUpdate.asStateFlow()

    private val _isPlotting = MutableStateFlow(false)
    val isPlotting: StateFlow<Boolean>
        get() = _isPlotting.asStateFlow()

    private var _boundary = MutableStateFlow(PlotBoundary(min = null, max = null))
    val boundary: StateFlow<PlotBoundary>
        get() = _boundary.asStateFlow()

    var secondsToPlot = SECONDS_TO_PLOT_DEFAULT

    var autoScaleEnable: Boolean = true
    var minValue: Float = MIN_DEFAULT
    var maxValue: Float = MAX_DEFAULT

    fun startDemo(nodeId: String) {
        viewModelScope.launch {
            privatePlottableFeatures.addAll(
                blueManager.nodeFeatures(nodeId = nodeId)
                    .filter { PLOTTABLE_FEATURE.contains(it.name) }
            )
            _plottableFeatures.emit(privatePlottableFeatures.toList())
            _selectedFeature.emit(privatePlottableFeatures.firstOrNull())
        }
    }

    fun stopDemo(nodeId: String) {
        privatePlottableFeatures.clear()
        stopPlottingBlocking(nodeId)
    }

    fun startPlotting(nodeId: String) {
        selectedFeature.value?.let {feature ->
            viewModelScope.launch {
                _isPlotting.emit(true)
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(feature)
                ).collect { data ->
                    _featureUpdate.emit(data)
                }
            }
        }
    }

    fun stopPlotting(nodeId: String) {
        selectedFeature.value?.let { feature ->
            viewModelScope.launch {
                blueManager.disableFeatures(nodeId = nodeId, features = listOf(feature))
                _isPlotting.emit(false)
            }
        }
    }

    private fun stopPlottingBlocking(nodeId: String) {
        if(_isPlotting.value) {
            selectedFeature.value?.let { feature ->
                runBlocking {
                    blueManager.disableFeatures(nodeId = nodeId, features = listOf(feature))
                    _isPlotting.emit(false)
                }
            }
        }
    }

    fun setFeature(featureName: String) {
        viewModelScope.launch {
            val runningFeature = privatePlottableFeatures.firstOrNull { it.name == featureName }

            val boundary = PlotBoundary.getDefaultFor(featureName)
            _boundary.emit(boundary)
            autoScaleEnable = boundary.enableAutoScale

            minValue = boundary.min ?: minValue
            maxValue = boundary.max ?: maxValue

            _selectedFeature.emit(runningFeature)
        }
    }

    fun saveImage(context: Context, file: Uri?) : Boolean {
        snap?.let { image ->
            file?.let {
                try {
                    val stream = context.contentResolver.openOutputStream(file)
                    stream?.let {
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()
                        return true
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    return false
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    return false
                }
            }
        }
        return false
    }

    fun maxValue(max: Float) {
        maxValue = max
        viewModelScope.launch {
            _boundary.emit(_boundary.value.copy(min = minValue, max = maxValue))
        }
    }

    fun minValue(min: Float) {
        minValue = min
        viewModelScope.launch {
            _boundary.emit(_boundary.value.copy(min = minValue, max = maxValue))
        }
    }

    fun autoScaleValue(autoscale: Boolean) {
        autoScaleEnable = autoscale
        viewModelScope.launch {
            if(autoscale) {
                _boundary.emit(_boundary.value.copy(min = null, max = null))
            } else {
                _boundary.emit(_boundary.value.copy(min = minValue, max = maxValue))
            }
        }
    }
}