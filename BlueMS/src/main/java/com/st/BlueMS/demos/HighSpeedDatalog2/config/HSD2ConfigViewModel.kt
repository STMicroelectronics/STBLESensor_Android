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

package com.st.BlueMS.demos.HighSpeedDatalog2.config

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.internal.LazilyParsedNumber
import com.st.BlueMS.demos.PnPL.PnPLComponentViewData
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.PnPL.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Node
import com.st.STWINBoard_Gui.IOConfError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOError

internal class HSD2ConfigViewModel : ViewModel(){

    var context: Context?=null

    private val _isConfLoading = MutableLiveData<Boolean>()
    val isConfigLoading:LiveData<Boolean>
        get() = _isConfLoading

    private val _isLogging = MutableLiveData<Boolean>()
    val isLogging:LiveData<Boolean>
        get() = _isLogging

    private var isSDCardInserted = false

    private var mSensorCurrentConfig = mutableListOf<PnPLComponentViewData>()
    private val _sensorCompList = MutableLiveData(mSensorCurrentConfig.toList())
    val sensorCompList:LiveData<List<PnPLComponentViewData>>
        get() = _sensorCompList

    private val _error = MutableLiveData<IOConfError?>(null)
    val error:LiveData<IOConfError?>
        get() = _error

    private val _savedConfiguration = MutableLiveData<List<PnPLComponent>?>(null)
    val savedConfiguration:LiveData<List<PnPLComponent>?>
        get() = _savedConfiguration

    //the UI will flip this flag when it start the view to request the data
    val requestFileLocation = MutableLiveData(false)

    private var mPnPLConfigFeature:FeaturePnPL? = null
    private val mPnPLListener = Feature.FeatureListener { _: Feature, sample: Feature.Sample? ->
        if (sample == null)
            return@FeatureListener

        val componentStatus = FeaturePnPL.getPnPLComponentStatus(sample)
        if(componentStatus?.comp_name == "log_controller"){
            Log.d("HSD2ConfigViewModel","--> log_controller status received")
            val isLogging = componentStatus.cont_list.find{it.cont_name == "log_status"}?.cont_info as Boolean
            if(_isLogging.value != isLogging){
                _isLogging.postValue(isLogging)
            }
            isSDCardInserted = componentStatus.cont_list.find{it.cont_name == "sd_mounted"}?.cont_info as Boolean
            /*if(!isLogging){
                Log.d("HSD2ConfigViewModel","--> sendPnPLGetDeviceStatus() (ConfigMainViewModel)")
                sendPnPLGetDeviceStatus()
            }*/
        }

        if(componentStatus?.comp_name == "ism330dhcx_mlc"){
            Log.d("HSD2ConfigViewModel","--> ism330dhcx_mlc status received")
            val isMLCUCFLoaded = componentStatus.cont_list.find{it.cont_name == "ucf_status"}?.cont_info as Boolean
            mSensorCurrentConfig.find { it.component.comp_name == "ism330dhcx_mlc" }?.component?.cont_list?.find { it.cont_name == "ucf_status" }?.file_loaded_status = isMLCUCFLoaded
            mSensorCurrentConfig.find { it.component.comp_name == "ism330dhcx_mlc" }?.component?.cont_list?.find { it.cont_name == "ucf_status" }?.cont_info = isMLCUCFLoaded
            _sensorCompList.postValue(mSensorCurrentConfig.toList())
        }

        val deviceStatus = FeaturePnPL.getPnPLDeviceStatus(sample)
        if (deviceStatus != null) {
            val componentsStatus = deviceStatus?.comp_list
            Log.d("HSD2ConfigViewModel","--> device Status status received")

            //Device Sensors
            for (c in mSensorCurrentConfig) {
                val newC: PnPLComponent? = componentsStatus?.find {
                    it.comp_name == c.component.comp_name
                }
                if (newC != null) {
                    val currContList = (c.component.cont_list as ArrayList<PnPLContent>)
                    val newContList = (newC.cont_list as ArrayList<PnPLContent>)
                    for (cont in currContList) {
                        val newCont: PnPLContent? =
                            newContList.find { it.cont_name == cont.cont_name }
                        if (newCont != null) {
                            if (newCont.cont_name == cont.cont_name) {
                                if (cont.cont_schema == "enum_int" || cont.cont_schema == "enum_string") {
                                    cont.cont_enum_pos = (newCont.cont_info as LazilyParsedNumber).toInt()
                                }
                                else if (cont.cont_schema == "object") {
                                    if (newCont.sub_cont_list != null) {
                                        val newSubContents = (newCont.sub_cont_list as ArrayList<*>)
                                        var subContents = (cont.sub_cont_list as ArrayList<*>)
                                        for (nsc in newSubContents) {
                                            for (sc in subContents) {
                                                if ((nsc as PnPLContent).cont_name == (sc as PnPLContent).cont_name) {
                                                    sc.cont_info = nsc.cont_info
                                                    break
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (newCont.cont_name == "ucf_status"){
                                        cont.file_loaded_status = newCont.cont_info as Boolean
                                    }
                                    cont.cont_info = newCont.cont_info
                                }
                            }
                        }
                    }
                }
            }
            _sensorCompList.postValue(mSensorCurrentConfig.toList())
        }
        _isConfLoading.postValue(false)
    }

    fun PnPLComponent.toPnPLComponentViewData(): PnPLComponentViewData {
        return PnPLComponentViewData(
            component = this,
            isCollapsed = true,
            hasLockedParams = false
        )
    }

    fun parseDeviceModel(strData: String): Boolean{
        val newConfiguration = mutableListOf<PnPLComponentViewData>()
        val components = PnPLParser.getPnPLSensorsComponentsMap(strData)?.values
        if(components != null){
            PnPLParser.filterContent(listOf("odr","fs","aop","enable","load_file","ucf_status"),components.toMutableList())
            newConfiguration.addAll(components.map { it.toPnPLComponentViewData() })
            mSensorCurrentConfig = newConfiguration
            _sensorCompList.postValue(mSensorCurrentConfig)
            return true
        }
        return false
    }

    fun loadConfigFromFile(file: Uri?, contentResolver: ContentResolver){
        if(file == null){
            _error.postValue(IOConfError.InvalidFile)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isConfLoading.postValue(true)
                val stream = contentResolver.openInputStream(file)
                if(stream==null){
                    _error.postValue(IOConfError.ImpossibleReadFile)
                    return@launch
                }
                val strData = stream.readBytes()//.toString(Charsets.UTF_8)
                stream.close()
                val responseObj = PnPLParser.getJsonObj(strData, true)
                val devStatus = PnPLParser.extractPnPLDeviceStatus(responseObj)
                if (devStatus != null) {
                    val compList = devStatus.comp_list
                    for (c in compList){
                        for (cn in c.cont_list){
                            if (cn.cont_info != null) {
                                sendPnPLSetProperty(c.comp_name, cn.cont_name, cn.cont_info!!)
                                /*if (cn.cont_info is ArrayList<*>) {
                                    if (cn.cont_enum_pos != null) {
                                        sendPnPLSetProperty(c.comp_name, cn.cont_name, cn.cont_enum_pos!!)
                                    }
                                } else {
                                    sendPnPLSetProperty(c.comp_name, cn.cont_name, cn.cont_info!!)
                                }*/
                            } else {
                                if (cn.sub_cont_list != null){
                                    for (scn in cn.sub_cont_list!!){
                                        if (scn.cont_info != null) {
                                            sendPnPLSetProperty(c.comp_name, cn.cont_name, scn.cont_name, cn.cont_info!!)
                                            /*if (scn.cont_info is ArrayList<*>) {
                                                if (scn.cont_enum_pos != null) {
                                                    sendPnPLSetProperty(c.comp_name, cn.cont_name, scn.cont_name, scn.cont_enum_pos!!)
                                                }
                                            } else {
                                                sendPnPLSetProperty(c.comp_name, cn.cont_name, scn.cont_name, cn.cont_info!!)
                                            }*/
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _error.postValue(IOConfError.ImpossibleReadFile)
                }
                _isConfLoading.postValue(false)
                sendPnPLGetDeviceStatus()

                /*val res = loadDeviceModelFromFile(strData)
                if (!res){
                    _error.postValue(IOConfError.ImpossibleReadFile)
                }*/

            }catch (e: FileNotFoundException){
                e.printStackTrace()
                _error.postValue(IOConfError.FileNotFound)
            }catch (e: IOError){
                e.printStackTrace()
                _error.postValue(IOConfError.ImpossibleReadFile)
            }
        }
    }

    fun sendPnPLGetDeviceStatus(){
        mPnPLConfigFeature?.apply {
            sendPnPLGetDeviceStatusCmd(PnPLGetDeviceStatusCmd())
        }
    }

    fun sendPnPLGetComponentStatus(component_name:String){
        mPnPLConfigFeature?.apply {
            sendPnPLGetComponentStatusCmd(PnPLGetComponentStatusCmd(component_name))
        }
    }

    fun sendPnPLSetProperty(component_name:String, property_name:String, property_value:Any) {
        mPnPLConfigFeature?.apply {
            var propCmd = PnPLSetProperty(property_name, property_value)
            sendPnPLSetPropertyCmd(PnPLSetPropertyCmd(component_name, listOf(propCmd)))
            var prop = mSensorCurrentConfig.find{it.component.comp_name == component_name}?.component?.cont_list?.find{it.cont_name == property_name }
            if (prop?.cont_schema!!.contains("enum")){
                prop.cont_enum_pos = property_value as Int
            }
            else{
                prop.cont_info = property_value
            }
        }
    }

    fun sendPnPLSetProperty(component_name:String, property_name:String, sub_property_name:String, property_value:Any) {
        mPnPLConfigFeature?.apply {
            var propCmd = PnPLSetProperty(property_name, PnPLSetProperty(sub_property_name, property_value))
            sendPnPLSetPropertyCmd(PnPLSetPropertyCmd(component_name, listOf(propCmd)))
            var prop = mSensorCurrentConfig.find{it.component.comp_name == component_name}?.component?.cont_list?.find{it.cont_name == property_name }?.sub_cont_list?.find { it.cont_name == sub_property_name }
            if (prop?.cont_schema!!.contains("enum")){
                prop.cont_enum_pos = property_value as Int
            }
            else{
                prop.cont_info = property_value
            }
        }
    }

    fun sendPnPLCommandCmd(compName: String, commandName: String, requestName: String, commandFields: Map<String, Any>){
        mPnPLConfigFeature?.sendPnPLCommandCmd(compName, commandName, requestName, commandFields)
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
                val sensors = mSensorCurrentConfig.map {it.component}
                val jsonStr = PnPLParser.getJsonFromComponents(sensors)
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

    private fun setCurrentConfAsDefault() {
        Log.d("HSD2ConfigViewModel","send Save Config Command (overwrite default config)")
        mPnPLConfigFeature?.sendPnPLCommandCmd("log_controller","save_config")
    }

    fun isSDCardMounted(): Boolean {
        return isSDCardInserted
    }

    fun saveConfiguration(saveSettings: SaveSettings){
        if(saveSettings.setAsDefault){
            setCurrentConfAsDefault()
        }
        if(saveSettings.storeLocalCopy){
            requestFileLocation.postValue(true)
        }else{
            _savedConfiguration.postValue(mSensorCurrentConfig.map { it.component })
        }
    }

    fun enableNotificationFromNode(node: Node){
        mPnPLConfigFeature=node.getFeature(FeaturePnPL::class.java)
        mPnPLConfigFeature?.apply {
            addFeatureListener(mPnPLListener)
            enableNotification()
            _isConfLoading.postValue(true)
        }
    }

    fun disableNotificationFromNode(node: Node){
        node.getFeature(FeatureHSDataLogConfig::class.java)?.apply {
            removeFeatureListener(mPnPLListener)
            disableNotification()
        }
    }

    fun collapseSensor(selected: PnPLComponentViewData) {
        Log.d("HSD2ConfigViewModel","select to: ${selected.component.comp_display_name}")
        val newSensor = selected.copy(isCollapsed = true)
        updateSensorConfig(newSensor)
    }

    fun expandSensor(selected: PnPLComponentViewData) {
        Log.d("HSD2ConfigViewModel","select to: ${selected.component.comp_display_name}")
        val newSensor = selected.copy(isCollapsed = false)
        updateSensorConfig(newSensor)
    }

    private fun updateSensorConfig(newSensor: PnPLComponentViewData) {
        val sensorComponent = mSensorCurrentConfig.find{ it.component.comp_name == newSensor.component.comp_name }
        val sId = mSensorCurrentConfig.indexOf(sensorComponent)
        mSensorCurrentConfig.removeAt(sId)
        mSensorCurrentConfig.add(sId,newSensor)
        _sensorCompList.postValue(mSensorCurrentConfig.toList())
    }

    fun updateMLCUCFLoadedStatus(status:Boolean) {
        val compName = "ism330dhcx_mlc"
        val propName = "ucf_status"
        mPnPLConfigFeature?.sendPnPLSetPropertyCmd(
            PnPLSetPropertyCmd(compName, listOf(PnPLSetProperty(propName, status)))
        )
        _sensorCompList.postValue(mSensorCurrentConfig.toList())
    }
}