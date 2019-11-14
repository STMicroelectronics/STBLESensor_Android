package com.st.BlueMS.demos.plot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureProximity

internal data class PlotEntry(val x:Long,
                              val y:FloatArray)

internal class PlotDataViewModel  : ViewModel(){

    private var _lastPlotData = MutableLiveData<PlotEntry?>()
    val lastPlotData:LiveData<PlotEntry?>
    get() = _lastPlotData

    private val _isPlotting = MutableLiveData<Boolean>(false)
    val isPlotting:LiveData<Boolean>
    get() = _isPlotting

    private var _lastDataDescription = MutableLiveData<String>()

    val lastDataDescription:LiveData<String>
    get() = _lastDataDescription

    private var mFisrtNotificationTimeStamp:Long = 0
    private var mCurrentPlottingFeature:Feature? = null

    private fun filterSample(f:Feature, sample:Feature.Sample): Feature.Sample{
        if( f is FeatureProximity){
            if( FeatureProximity.isOutOfRangeDistance(sample)) {
                sample.data[0] = 0
            }
        }
        return sample
    }

    private val mFeatureListener = Feature.FeatureListener{ f,sample ->
        //remove the starting time to avoid to have enormous x values
        val plotEntry = filterSample(f,sample).toPlotEntry(xOffset = mFisrtNotificationTimeStamp)
        val lastX = _lastPlotData.value?.x ?: Long.MIN_VALUE
        if(plotEntry.x < lastX)
            return@FeatureListener
        _lastPlotData.postValue(plotEntry)
        _lastDataDescription.postValue(f.toString())
    }

    fun startPlotFeature(f:Feature){
        stopPlot()
        mFisrtNotificationTimeStamp = System.currentTimeMillis()
        f.addFeatureListener(mFeatureListener)
        f.enableNotification()
        _isPlotting.value=true
        mCurrentPlottingFeature = f
    }

    fun stopPlot(){
        mCurrentPlottingFeature?.apply {
            removeFeatureListener(mFeatureListener)
            disableNotification()
        }
        _isPlotting.value=false
        _lastPlotData.postValue(null)
    }

    fun onStartStopButtonPressed(selectedFeature:Feature){
        if(_isPlotting.value == true){
            stopPlot()
        }else{
            startPlotFeature(selectedFeature)
        }
    }

}

private fun Feature.Sample.toPlotEntry(xOffset:Long): PlotEntry{
    val yData = FloatArray(data.size) { i -> data[i].toFloat() }
    return PlotEntry(notificationTime-xOffset,yData)
}