/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 * STMicroelectronics company nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 * in a directory whose title begins with st_images may only be used for internal purposes and
 * shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 * icons, pictures, logos and other images that are provided with the source code in a directory
 * whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.STWINBoard_Gui

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SubSensorDescriptor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SubSensorStatus
import com.st.BlueSTSDK.Node
import com.st.STWINBoard_Gui.Utils.SaveSettings
import com.st.STWINBoard_Gui.Utils.SensorViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOError

internal class HSDConfigViewModel : ViewModel(){

    private val _isConfLoading = MutableLiveData<Boolean>()
    val isConfigLoading:LiveData<Boolean>
        get() = _isConfLoading

    private val _isLogging = MutableLiveData<Boolean>()
    val isLogging:LiveData<Boolean>
        get() = _isLogging

    private val _fwErrorInfo = MutableLiveData<FWErrorInfo>()
    val fwErrorInfo:LiveData<FWErrorInfo>
        get() = _fwErrorInfo

    private var mCurrentConfig = mutableListOf<SensorViewData>()
    private val _boardConfiguration = MutableLiveData(mCurrentConfig.toList())
    val sensorsConfiguration:LiveData<List<SensorViewData>>
        get() = _boardConfiguration

    private var mHSDConfigFeature:FeatureHSDataLogConfig? = null
    private val mSTWINConfListener = Feature.FeatureListener { _: Feature, sample: Feature.Sample? ->
        if (sample == null)
            return@FeatureListener

        //isLogging
        val isLogging = FeatureHSDataLogConfig.isLogging(sample)
        if(isLogging!=null){
            _isLogging.postValue(isLogging)
        }

        //SensorStatus with Id
        val ssWId = FeatureHSDataLogConfig.getSensorStatusWId(sample)
        if(ssWId != null){
            val newSensor = mCurrentConfig[ssWId.sensorId!!].sensor.copy(sensorStatus = ssWId.sensorStatus!!)
            val newSensorViewData = mCurrentConfig[ssWId.sensorId!!].copy(sensor = newSensor)
            updateSensorConfig(newSensorViewData)
        }

        //Device
        val deviceConf = FeatureHSDataLogConfig.getDeviceConfig(sample) ?: return@FeatureListener
        if(deviceConf.deviceInfo != null){
            //check FW version
            if(deviceConf.deviceInfo!!.fwName == FeatureHSDataLogConfig.LATEST_FW_NAME){
                val curVer = deviceConf.deviceInfo!!.fwVersion!!.replace(".","").toInt()
                val targetVer = FeatureHSDataLogConfig.getFWVersionString().replace(".","").toInt()
                val curFW = deviceConf.deviceInfo!!.fwName + "_v" + deviceConf.deviceInfo!!.fwVersion
                val targetFW = FeatureHSDataLogConfig.LATEST_FW_NAME + "_v " + FeatureHSDataLogConfig.getFWVersionString()
                val targetFWUrl = FeatureHSDataLogConfig.LATEST_FW_URL
                if(curVer < targetVer){
                    _fwErrorInfo.postValue(FWErrorInfo("Obsolete FW Detected", curFW, targetFW, targetFWUrl))
                }
            } else {
                _fwErrorInfo.postValue(FWErrorInfo(
                        "Wrong FW Detected",
                        deviceConf.deviceInfo!!.fwName.toString(),
                        FeatureHSDataLogConfig.LATEST_FW_NAME,
                        FeatureHSDataLogConfig.LATEST_FW_URL))
            }
        }

        val newConfiguration = mutableListOf<SensorViewData>()
        val sensors = deviceConf.sensors ?: return@FeatureListener
        newConfiguration.addAll(sensors.map { it.toSensorViewData() })
        if(mCurrentConfig != newConfiguration){
            Log.e("TAG", "DeviceConfig changed1! newConfiguration.size=${newConfiguration.size}")
            if(mCurrentConfig.size > 0) {
                val collapsedMap = mutableMapOf<String, Boolean>()
                for (s in mCurrentConfig) {
                    collapsedMap[s.sensor.name] = s.isCollapsed
                }
                for (s in newConfiguration) {
                    s.isCollapsed = collapsedMap[s.sensor.name]!!
                }
            }
            mCurrentConfig = newConfiguration
            _isConfLoading.postValue(false)
            _boardConfiguration.postValue(mCurrentConfig.toList())
        }
    }

    private fun getMLCSensorID(subSensorDescriptorList:List<SubSensorDescriptor>):Int {
        for (ssd in subSensorDescriptorList){
            if (ssd.sensorType == SensorType.MLC){
                return ssd.id
            }
        }
        return -1
    }

    private fun Sensor.toSensorViewData(): SensorViewData {
        val mlcId = getMLCSensorID(this.sensorDescriptor.subSensorDescriptors)
        if (mlcId != -1) {
            return SensorViewData(
                    sensor = this,
                    isCollapsed = true,
                    hasLockedParams = this.sensorStatus.subSensorStatusList[mlcId].isActive && this.sensorStatus.subSensorStatusList[mlcId].ucfLoaded
            )
        }
        return SensorViewData(
                sensor = this,
                isCollapsed = true,
                hasLockedParams = false
                )
    }

    private val _error = MutableLiveData<IOConfError?>(null)
    val error:LiveData<IOConfError?>
        get() = _error

    private val _savedConfiguration = MutableLiveData<List<Sensor>?>(null)
    val savedConfuguration:LiveData<List<Sensor>?>
        get() = _savedConfiguration

    //the UI will flip this flag when it start the view to request the data
    val requestFileLocation = MutableLiveData(false)


    fun loadConfigFromFile(file: Uri?, contentResolver: ContentResolver){
        if(file == null){
            _error.postValue(IOConfError.InvalidFile)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = contentResolver.openInputStream(file)
                if(stream==null){
                    _error.postValue(IOConfError.ImpossibleReadFile)
                    return@launch
                }
                val strData = stream.readBytes().toString(Charsets.UTF_8)
                stream.close()
                val config = DeviceParser.extractSensors(strData)
                if(config == null){
                    _error.postValue(IOConfError.InvalidFile)
                    return@launch
                }

                val newConfig = mutableListOf<SensorViewData>()
                newConfig.addAll(config.map { it.toSensorViewData() })
                if(mCurrentConfig != newConfig){
                    Log.e("TAG", "DeviceConfig changed2! newConfig.size=${newConfig.size}")
                    if(mCurrentConfig.size > 0) {
                        val collapsedMap = mutableMapOf<String, Boolean>()
                        for (s in mCurrentConfig) {
                            collapsedMap[s.sensor.name] = s.isCollapsed
                        }
                        for (s in newConfig) {
                            s.isCollapsed = collapsedMap[s.sensor.name]!!
                        }
                    }
                    applyNewConfig(newConfig)
                    mCurrentConfig = newConfig
                    _isConfLoading.postValue(false)
                    _boardConfiguration.postValue(mCurrentConfig.toList())
                }

            }catch (e: FileNotFoundException){
                e.printStackTrace()
                _error.postValue(IOConfError.FileNotFound)
            }catch (e: IOError){
                e.printStackTrace()
                _error.postValue(IOConfError.ImpossibleReadFile)
            }
        }
    }

    private fun List<SensorViewData>.getSensorWithId(id:Int):SensorViewData? = find { it.sensor.id == id }

    private fun applyNewConfig(newConfig: MutableList<SensorViewData>) {
        newConfig.forEach { localSensor ->
            val currentSensor =  mCurrentConfig.getSensorWithId(localSensor.sensor.id) ?: return@forEach

            val sssList = currentSensor.sensor.sensorStatus.subSensorStatusList
            val ssdList = currentSensor.sensor.sensorDescriptor.subSensorDescriptors
            for(i in sssList.indices){
                if(ssdList[i].sensorType == SensorType.MLC){
                    if(!sssList[i].ucfLoaded){
                        localSensor.sensor.sensorStatus.subSensorStatusList[i].ucfLoaded = false
                    }
                }
            }

            if(currentSensor.sensor.sensorStatus != localSensor.sensor.sensorStatus){
                //todo CHECK THE DESCRIPTION TO BE COMPATIBLE?
                val updateCommand = buildSensorChangesCommand(localSensor.sensor.id,currentSensor.sensor,localSensor.sensor)
                mHSDConfigFeature?.sendSetCmd(updateCommand)
            }
        }
    }

    private fun buildSensorChangesCommand(id: Int, currentSensor: Sensor, newSensor: Sensor): HSDSetSensorCmd{
        val subSensorChanges = mutableListOf<SubSensorStatusParam>()
        newSensor.sensorDescriptor.subSensorDescriptors.forEach { subSensorDesc ->
            val currentStatus = currentSensor.getSubSensorStatusForId(subSensorDesc.id) ?: return@forEach
            val newStatus = newSensor.getSubSensorStatusForId(subSensorDesc.id) ?: return@forEach
            if(currentStatus!=newStatus){
                subSensorChanges.addAll(
                        buildSubSensorStatusParamDiff(subSensorDesc.id,currentStatus,newStatus)
                )
            }
        }
        return HSDSetSensorCmd(id,subSensorChanges)
    }

    private fun buildSubSensorStatusParamDiff(subSensorId: Int,
                                              currentSensor: SubSensorStatus, newSensor: SubSensorStatus):List<SubSensorStatusParam> {
        val diff = mutableListOf<SubSensorStatusParam>()
        if(newSensor.isActive != currentSensor.isActive){
            diff.add(IsActiveParam(subSensorId,newSensor.isActive))
        }
        val odr = newSensor.odr
        if(odr!=null && newSensor.odr != currentSensor.odr){
            diff.add(ODRParam(subSensorId,odr))
        }
        val fs = newSensor.fs
        if(fs!=null && newSensor.fs != currentSensor.fs){
            diff.add(FSParam(subSensorId,fs))
        }
        val ts = newSensor.samplesPerTs
        if(ts!=currentSensor.samplesPerTs){
            diff.add(SamplePerTSParam(subSensorId,ts))
        }
        return diff
    }

    fun storeConfigToFile(file: Uri?, contentResolver: ContentResolver) {
        if(file == null){
            _error.postValue(IOConfError.InvalidFile)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = contentResolver.openOutputStream(file)
                if(stream==null){
                    _error.postValue(IOConfError.ImpossibleWriteFile)
                    return@launch
                }
                val sensors = mCurrentConfig.map { it.sensor }
                val jsonStr = DeviceParser.toJsonStr(sensors)
                stream.write(jsonStr.toByteArray(Charsets.UTF_8))
                stream.close()
                _savedConfiguration.postValue(sensors)
            }catch (e: FileNotFoundException){
                e.printStackTrace()
                _error.postValue(IOConfError.ImpossibleCreateFile)
            }catch (e:IOError){
                e.printStackTrace()
                _error.postValue(IOConfError.ImpossibleWriteFile)
            }
        }
    }

    fun enableNotificationFromNode(node: Node){
        mHSDConfigFeature=node.getFeature(FeatureHSDataLogConfig::class.java)

        mHSDConfigFeature?.apply {
            addFeatureListener(mSTWINConfListener)
            enableNotification()
            if(mCurrentConfig.isEmpty()) {
                _isConfLoading.postValue(true)
                sendGetCmd(HSDGetDeviceCmd())
            }
        }
    }

    fun disableNotificationFromNode(node: Node){
        node.getFeature(FeatureHSDataLogConfig::class.java)?.apply {
            removeFeatureListener(mSTWINConfListener)
            disableNotification()
        }
    }

    private fun getSubSensorStatus(sensorId:Int,subSensorId:Int): SubSensorStatus?{
        return mCurrentConfig.getSensorWithId(sensorId)?.sensor?.getSubSensorStatusForId(subSensorId)
    }

    fun changeODRValue(sensor: Sensor, subSensor: SubSensorDescriptor, newOdrValue: Double) {
        Log.d("ConfigVM","onSubSensorODRChange ${sensor.id} -> ${subSensor.id} -> $newOdrValue")
        val paramList = listOf(ODRParam(subSensor.id,newOdrValue))
        val ssODRCmd = HSDSetSensorCmd( sensor.id, paramList)
        mHSDConfigFeature?.sendSetCmd(ssODRCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.odr = newOdrValue
        _boardConfiguration.postValue(mCurrentConfig.toList())
    }

    fun changeFullScale(sensor: Sensor, subSensor: SubSensorDescriptor, newFSValue: Double) {
        Log.d("ConfigVM","onSubSensorFSChange ${sensor.id} -> ${subSensor.id} -> $newFSValue")
        val paramList = listOf(FSParam(subSensor.id,newFSValue))
        val ssFSCmd = HSDSetSensorCmd(sensor.id, paramList)
        mHSDConfigFeature?.sendSetCmd(ssFSCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.fs = newFSValue
        _boardConfiguration.postValue(mCurrentConfig.toList())
    }

    fun changeSampleForTimeStamp(sensor: Sensor, subSensor: SubSensorDescriptor, newSampleValue: Int) {
        Log.d("ConfigVM","onSubSensorSampleChange ${sensor.id} -> ${subSensor.id} -> $newSampleValue")
        val paramList = listOf(SamplePerTSParam(subSensor.id,newSampleValue))
        val ssSamplePerTSCmd = HSDSetSensorCmd(sensor.id, paramList)
        mHSDConfigFeature?.sendSetCmd(ssSamplePerTSCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.samplesPerTs = newSampleValue
        _boardConfiguration.postValue(mCurrentConfig.toList())
    }

    fun changeEnableState(sensor: Sensor, subSensor: SubSensorDescriptor, newState: Boolean, paramsLocked: Boolean) {
        Log.d("ConfigVM","onSubSensorEnableChange ${sensor.id} -> ${subSensor.id} -> $newState")
        val paramList = listOf(IsActiveParam(subSensor.id,newState))
        val ssIsActiveCmd = HSDSetSensorCmd(sensor.id, paramList)
        mHSDConfigFeature?.sendSetCmd(ssIsActiveCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.isActive = newState
        if (subSensor.sensorType == SensorType.MLC) {
            val newSensorStatus = mCurrentConfig[sensor.id].sensor.sensorStatus.copy(paramsLocked = paramsLocked)
            val newSensor = mCurrentConfig[sensor.id].sensor.copy(sensorStatus = newSensorStatus)
            val newSensorViewData = mCurrentConfig[sensor.id].copy(hasLockedParams = paramsLocked, sensor = newSensor)
            updateSensorConfig(newSensorViewData)
        }
        else {
            _boardConfiguration.postValue(mCurrentConfig.toList())
        }
    }

    fun changeMLCLockedParams(newState: Boolean){
        Log.d("ConfigVM","refreshDevice")
        var mlcSId = -1
        var mlcSsId = -1
        for(sc in mCurrentConfig){
            for(sd in sc.sensor.sensorDescriptor.subSensorDescriptors){
                if (sd.sensorType == SensorType.MLC || sd.sensorType == SensorType.STREDL){
                    mlcSId = sc.sensor.id
                    mlcSsId = sd.id
                }
            }
        }
        val newSubSensorStatusList: MutableList<SubSensorStatus> = mutableListOf()
        for (i in mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList.indices)
            newSubSensorStatusList += if (i == mlcSsId)
                mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList[i].copy(ucfLoaded = newState)
            else
                mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList[i].copy()
        val newSensorStatus = mCurrentConfig[mlcSId].sensor.sensorStatus.copy(subSensorStatusList = newSubSensorStatusList)
        val newSensor = mCurrentConfig[mlcSId].sensor.copy(sensorStatus = newSensorStatus)
        val newSensorViewData = mCurrentConfig[mlcSId].copy(sensor = newSensor)
        updateSensorConfig(newSensorViewData)
        return
    }

    private fun setCurrentConfAsDefault() {
        Log.d("ConfigVM","set as default")
        mHSDConfigFeature?.sendControlCmd(HSDSaveCmd())
    }

    fun saveConfiguration(saveSettings: SaveSettings){
        if(saveSettings.setAsDefault){
            setCurrentConfAsDefault()
        }
        if(saveSettings.storeLocalCopy){
            requestFileLocation.postValue(true)
        }else{
            _savedConfiguration.postValue(mCurrentConfig.map { it.sensor })
        }
    }

    private fun updateSensorConfig(newSensor: SensorViewData) {
        mCurrentConfig[newSensor.sensor.id] = newSensor
        _boardConfiguration.postValue(mCurrentConfig.toList())
    }

    fun collapseSensor(selected: SensorViewData) {
        Log.d("ConfigViewMode","select to: ${selected.sensor.name}")
        val newSensor = selected.copy(isCollapsed = true)
        updateSensorConfig(newSensor)
    }

    fun expandSensor(selected: SensorViewData) {
        Log.d("ConfigViewMode","select to: ${selected.sensor.name}")
        val newSensor = selected.copy(isCollapsed = false)
        updateSensorConfig(newSensor)
    }

    fun checkIsLogging() {
        mHSDConfigFeature?.sendGetCmd(HSDGetLogStatusCmd())
    }
}