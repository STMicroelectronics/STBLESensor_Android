/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.plot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.plot.utils.PLOTTABLE_FEATURE
import com.st.plot.utils.toPlotDesc
import com.st.plot.utils.toPlotEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlotViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {
    companion object {
        const val TAG = "PlotViewModel"
    }

    private var _nodeId: String = ""
    private var featureUpdates: Flow<FeatureUpdate<*>> = emptyFlow()
    private var plottableFeatures: MutableList<Feature<*>> = mutableListOf()

    fun startDemo(nodeId: String) {
        Log.d(TAG, "startDemo")
        _nodeId = nodeId
        viewModelScope.launch {
            plottableFeatures.addAll(
                blueManager.nodeFeatures(nodeId = nodeId)
                    .filter { PLOTTABLE_FEATURE.contains(it.name) }
            )

            featureUpdates = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = plottableFeatures,
                autoEnable = false
            )

            featureUpdates.collect { update ->
                mCurrentPlottingFeature?.let {
                    val plotEntry =
                        update.toPlotEntry(feature = it, xOffset = mFirstNotificationTimeStamp)
                    val plotDesc = update.toPlotDesc(feature = it)
                    val lastX = _lastPlotData.value?.x ?: Long.MIN_VALUE
                    if (plotEntry != null) {
                        if (plotEntry.x >= lastX) {
                            _lastPlotData.postValue(plotEntry)
                            plotDesc?.let { desc ->
                                _lastDataDescription.postValue(desc)
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        Log.d(TAG, "stopDemo")
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId = nodeId, features = plottableFeatures)
        }
    }

    private var _lastDataDescription = MutableLiveData<String>()

    val lastDataDescription: LiveData<String>
        get() = _lastDataDescription

    private val _isPlotting = MutableLiveData<Boolean>(false)
    val isPlotting: LiveData<Boolean>
        get() = _isPlotting

    private var mFirstNotificationTimeStamp: Long = 0
    private var mCurrentPlottingFeature: Feature<*>? = null
    private var mNotificationEnabled = false

    private var _lastPlotData = MutableLiveData<PlotEntry?>()
    val lastPlotData: LiveData<PlotEntry?>
        get() = _lastPlotData

    private suspend fun notificationStartStop() {
        Log.d(TAG, "notificationStartStop")
        mCurrentPlottingFeature?.apply {
            mNotificationEnabled = if (mNotificationEnabled) {
                Log.d(TAG, "mNotificationEnabled")
                blueManager.disableFeatures(
                    nodeId = _nodeId, listOf(this)
                )
                false
            } else {
                Log.d(TAG, "not mNotificationEnabled")
                blueManager.enableFeatures(
                    nodeId = _nodeId, listOf(this)
                )
                true
            }
        }
    }

    fun startPlotFeature(f: Feature<*>) {
        viewModelScope.launch {
            startPlot(f)
        }
    }

    private suspend fun startPlot(f: Feature<*>) {
        Log.d(TAG, "startPlot")
        stopPlot()
        mFirstNotificationTimeStamp = System.currentTimeMillis()
        _isPlotting.value = true
        mCurrentPlottingFeature = f

        if (!mNotificationEnabled) {
            notificationStartStop()
        }
    }

    fun stopPlotFeature() {
        viewModelScope.launch {
            stopPlot()
        }
    }

    private suspend fun stopPlot() {
        Log.d(TAG, "stopPlot")
        mCurrentPlottingFeature?.apply {
            if (mNotificationEnabled) {
                notificationStartStop()
            }
        }
        _isPlotting.value = false
        _lastPlotData.postValue(null)
        mCurrentPlottingFeature = null
    }

    fun onStartStopButtonPressed(selectedFeature: Feature<*>) {
        viewModelScope.launch {
            if (_isPlotting.value == true) {
                stopPlot()
            } else {
                startPlotFeature(selectedFeature)
            }
        }
    }

    fun onResumePauseButtonPressed() {
        viewModelScope.launch {
            notificationStartStop()
        }
    }
}

data class PlotEntry(
    val x: Long, val y: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlotEntry

        if (x != other.x) return false
        if (!y.contentEquals(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.contentHashCode()
        return result
    }
}
