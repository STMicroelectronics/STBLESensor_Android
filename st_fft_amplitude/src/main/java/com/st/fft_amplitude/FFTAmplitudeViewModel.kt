/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.fft_amplitude

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.motor_time_param.MotorTimeParameter
import com.st.blue_sdk.features.extended.motor_time_param.MotorTimeParameterInfo
import com.st.blue_sdk.features.fft.FFTAmplitudeFeature
import com.st.blue_sdk.features.fft.FFTData
import com.st.fft_amplitude.utilites.FFTPoint
import com.st.fft_amplitude.utilites.TimeDomainStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject

@HiltViewModel
class FFTAmplitudeViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val features: MutableList<Feature<*>> = mutableListOf()
    private lateinit var featureFFTAmplitudeFeature: FFTAmplitudeFeature

    var snap: Bitmap? = null

    private val _loadingStatus =
        MutableStateFlow(0)
    val loadingStatus: StateFlow<Int>
        get() = _loadingStatus.asStateFlow()

    private val _mFftData =
        MutableStateFlow<List<FloatArray>>(mutableListOf())
    val mFftData: StateFlow<List<FloatArray>>
        get() = _mFftData.asStateFlow()

    var mFreqStep: Float = 0f

    private val _mXStats =
        MutableStateFlow<TimeDomainStats?>(null)
    val mXStats: StateFlow<TimeDomainStats?>
        get() = _mXStats.asStateFlow()

    private val _mYStats =
        MutableStateFlow<TimeDomainStats?>(null)
    val mYStats: StateFlow<TimeDomainStats?>
        get() = _mYStats.asStateFlow()

    private val _mZStats =
        MutableStateFlow<TimeDomainStats?>(null)
    val mZStats: StateFlow<TimeDomainStats?>
        get() = _mZStats.asStateFlow()

    private val _mFftMax =
        MutableStateFlow<List<FFTPoint>>(listOf())
    val mFftMax: StateFlow<List<FFTPoint>>
        get() = _mFftMax.asStateFlow()

    val timeParameterAvailable = MutableLiveData(false)

    private fun findIndexMax(data: FloatArray): Int {
        var max = data[0]
        var index = 0
        for (i in 1 until data.size) {
            if (data[i] > max) {
                max = data[i]
                index = i
            }
        }
        return index
    }

    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            viewModelScope.launch {
                blueManager.nodeFeatures(nodeId).filter {
                    FFTAmplitudeFeature.NAME == it.name || MotorTimeParameter.NAME == it.name
                }.let { it ->
                    val feature = it.find { it2 -> it2.name == FFTAmplitudeFeature.NAME }
                    if (feature != null) {
                        featureFFTAmplitudeFeature = feature as FFTAmplitudeFeature
                        featureFFTAmplitudeFeature.resetFeature()
                    }

                    features.addAll(it)
                }
            }
        }


        if (features.isNotEmpty()) {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, features).collect {
                    val data = it.data

                    if (data is FFTData) {
                        if (data.isComplete) {
                            val dataFFT = data.getComponents()
                            mFreqStep = data.freqStep.value
                            _mFftData.emit(dataFFT)

                            //Search the max Value
                            val nComponents: Int = dataFFT.size
                            val maxs = ArrayList<FFTPoint>(nComponents)

                            for (i in 0 until nComponents) {
                                val compData: FloatArray = dataFFT[i]
                                if (compData.isNotEmpty()) {
                                    val maxIndex: Int = findIndexMax(compData)
                                    maxs.add(FFTPoint(mFreqStep * maxIndex, compData[maxIndex]))
                                }
                            }
                            if (maxs.size > 0) {
                                _mFftMax.emit(maxs)
                            }

                        } else {
                            val loadPerc = data.getDataLoadPercentage()
                            _loadingStatus.emit(loadPerc)
                        }
                    }

                    if (data is MotorTimeParameterInfo) {
                        var acc = data.accPeakX.value
                        var speed = data.rmsSpeedX.value
                        if ((!acc.isNaN()) && (!speed.isNaN())) {
                            _mXStats.emit(TimeDomainStats(acc, speed))
                            if (timeParameterAvailable.value == false) {
                                timeParameterAvailable.postValue(true)
                            }
                        }

                        acc = data.accPeakY.value
                        speed = data.rmsSpeedY.value
                        if ((!acc.isNaN()) && (!speed.isNaN())) {
                            _mYStats.emit(TimeDomainStats(acc, speed))
                            if (timeParameterAvailable.value == false) {
                                timeParameterAvailable.postValue(true)
                            }
                        }

                        acc = data.accPeakZ.value
                        speed = data.rmsSpeedZ.value
                        if ((!acc.isNaN()) && (!speed.isNaN())) {
                            _mZStats.emit(TimeDomainStats(acc, speed))
                            if (timeParameterAvailable.value == false) {
                                timeParameterAvailable.postValue(true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        if (features.isNotEmpty()) {
            coroutineScope.launch {
                val retValue = blueManager.disableFeatures(nodeId, features)
                if (!retValue) {
                    //This should be not necessary... but just in order to be sure...
                    featureFFTAmplitudeFeature.resetFeature()
                }
            }
        }
    }

    fun saveImage(context: Context, file: Uri?): Boolean {
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
}
