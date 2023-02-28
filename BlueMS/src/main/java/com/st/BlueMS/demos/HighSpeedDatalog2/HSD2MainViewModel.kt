package com.st.BlueMS.demos.HighSpeedDatalog2

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Features.PnPL.PnPLGetComponentStatusCmd
import com.st.BlueSTSDK.Features.PnPL.PnPLGetDeviceStatusCmd
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Node
import java.text.SimpleDateFormat
import java.util.*

class HSD2MainViewModel : ViewModel(){

    private val _boardName = MutableLiveData<String?>(null)
    val boardName:LiveData<String?>
    get() = _boardName

    private val _boardId = MutableLiveData<String?>(null)
    val boardId: LiveData<String?>
        get() = _boardId

    private val _skipConfig = MutableLiveData(false)
    val skipConfig: LiveData<Boolean>
        get() = _skipConfig

    private var mPnPLFeature:FeaturePnPL? = null
    private val featureListener = Feature.FeatureListener { _: Feature, sample: Feature.Sample? ->
        if (sample == null)
            return@FeatureListener

        val componentStatus = FeaturePnPL.getPnPLComponentStatus(sample)

        if(componentStatus?.comp_name == "fwinfo"){
            Log.d("HSD2MainViewModel","--> fwinfo status received")
            _boardName.postValue(componentStatus.cont_list.find{it.cont_name == "alias"}?.cont_info as String)
        }

        if(componentStatus?.comp_name == "deviceinfo"){
            Log.d("HSD2MainViewModel","--> deviceinfo status received")
            _boardId.postValue(componentStatus.cont_list.find{it.cont_name == "model"}?.cont_info as String)
        }

        if(componentStatus?.comp_name == "log_controller"){
            Log.d("HSD2MainViewModel","--> log_controller status received")
            val isLogging = componentStatus.cont_list.find{it.cont_name == "log_status"}?.cont_info as Boolean
            if(_skipConfig.value != isLogging){
                _skipConfig.postValue(isLogging)
            }
            if(!isLogging){
                Log.e("HSD2MainViewModel","--> sendPnPLGetDeviceStatus()")
                sendPnPLGetDeviceStatus()
            }
        }
    }

    fun sendPnPLGetDeviceStatus(){
        mPnPLFeature?.apply {
            sendPnPLGetDeviceStatusCmd(PnPLGetDeviceStatusCmd())
        }
    }

    fun enableNotificationfromNode(node: Node){
        mPnPLFeature = node.getFeature(FeaturePnPL::class.java)?.apply {
            addFeatureListener(featureListener)
            enableNotification()
            sendPnPLGetComponentStatusCmd(PnPLGetComponentStatusCmd("fwinfo"))
            sendPnPLGetComponentStatusCmd(PnPLGetComponentStatusCmd("deviceinfo"))
            sendPnPLGetComponentStatusCmd(PnPLGetComponentStatusCmd("log_controller"))
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val timeInMillis = calendar.timeInMillis
            val sdf = SimpleDateFormat("yyyyMMdd_hh_mm_ss", Locale.ROOT) //internal timestamp format (known by the FW)
            val datetime = sdf.format(Date(timeInMillis))
            sendPnPLCommandCmd("log_controller","set_time", mapOf("datetime" to datetime))
        }
    }

    fun disableNotificationFromNode(node: Node){
        node.getFeature(FeaturePnPL::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
    }
}