package com.st.BlueMS.demos.HighSpeedDataLog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.HSDGetLogStatusCmd
import com.st.BlueSTSDK.Node

class HighSpeedDataLogViewModel : ViewModel(){

    private val _boardName = MutableLiveData<String?>(null)
    val boardName:LiveData<String?>
    get() = _boardName

    private val _boardId = MutableLiveData<String?>(null)
    val boardId: LiveData<String?>
        get() = _boardId

    private val _boardBatteryValue = MutableLiveData<Double?>(null)
    val boardBatteryValue:LiveData<Double?>
        get() = _boardBatteryValue

    private val _boardCPUusageValue = MutableLiveData<Double?>(null)
    val boardCPUusageValue: LiveData<Double?>
        get() = _boardCPUusageValue

    private val _skipConfig = MutableLiveData(false)
    val skipConfig: LiveData<Boolean>
        get() = _skipConfig

    private var mHSDConfigFeature:FeatureHSDataLogConfig? = null

    private val featureListener = Feature.FeatureListener { _, sample ->
        val info = FeatureHSDataLogConfig.getDeviceInfo(sample)
        if(info!=null) {
            _boardName.postValue(info.alias)
            _boardId.postValue(info.serialNumber)
        }

        val status = FeatureHSDataLogConfig.getDeviceStatus(sample)
        if(status!=null){
            _boardBatteryValue.postValue(status.batteryLevel)
            _boardCPUusageValue.postValue(status.cpuUsage)
        }

        val isLogging = FeatureHSDataLogConfig.isLogging(sample)
        if(isLogging!=null && _skipConfig.value!=isLogging){
            _skipConfig.postValue(isLogging)
        }
    }

    fun enableNotification(node: Node){
        mHSDConfigFeature = node.getFeature(FeatureHSDataLogConfig::class.java)?.apply {
            addFeatureListener(featureListener)
            enableNotification()
            sendGetCmd(HSDGetLogStatusCmd())
        }

    }

    fun disableNotification(node: Node){
        node.getFeature(FeatureHSDataLogConfig::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
    }

}