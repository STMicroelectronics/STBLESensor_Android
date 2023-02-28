package com.st.BlueMS.demos.PnPL

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.internal.LazilyParsedNumber
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.PnPL.*
import com.st.BlueSTSDK.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOError

class PnPLConfigViewModel : ViewModel() {
    var context: Context?=null

    private lateinit var mCompName4File:String
    private lateinit var mContName4File:String
    private lateinit var mReqName4File:String

    var isLogging = false

    private var mCurrentConfig = mutableListOf<PnPLComponentViewData>()
    private val _PnPLCompList = MutableLiveData(mCurrentConfig.toList())
    val compList:LiveData<List<PnPLComponentViewData>>
        get() = _PnPLCompList

    private val _error = MutableLiveData<String>(null)
    val error: LiveData<String?>
        get() = _error

    private val _isLoading = MutableLiveData(false)
    val isLoading:LiveData<Boolean>
        get() = _isLoading

    private var mPnPLConfigFeature: FeaturePnPL? = null
    private val mPnPLListener = Feature.FeatureListener { _: Feature, sample: Feature.Sample? ->
        if (sample == null)
            return@FeatureListener

        val pnplDevStatus = FeaturePnPL.getPnPLDeviceStatus(sample)
        val pnplComponentsStatus = pnplDevStatus?.comp_list

        if(pnplComponentsStatus!=null){
            for(c in mCurrentConfig){
                val newC: PnPLComponent? = pnplComponentsStatus.find {
                    it.comp_name == c.component.comp_name
                }
                if (newC != null) {
                    val currContList = (c.component.cont_list as ArrayList<PnPLContent>)
                    val newContList = (newC.cont_list as ArrayList<PnPLContent>)
                    for(cont in currContList){
                        val newCont: PnPLContent? = newContList.find {it.cont_name == cont.cont_name}
                        if (newCont != null) {
                            if(newCont.cont_name == cont.cont_name){
                                if (cont.cont_schema == "enum_int" || cont.cont_schema == "enum_string") {
//                                    val enumValues = (cont.cont_info as List<*>)
//                                    val selectedEnumValue = enumValues.find { (it as PnPLEnumValue).displayName.contains(newCont.cont_info.toString()) }
//                                    var enumPosition = 0
//                                    if (selectedEnumValue != null) {
//                                        enumPosition = enumValues.indexOf(selectedEnumValue)
//                                    }
//                                    cont.cont_enum_pos = enumPosition
                                      cont.cont_enum_pos = (newCont.cont_info as LazilyParsedNumber).toInt()
                                }
                                else if (cont.cont_schema == "object"){
                                    if(newCont.sub_cont_list != null) {
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
            _PnPLCompList.postValue(mCurrentConfig.toList())
        }
    }

    fun enableNotificationFromNode(node: Node){
        mPnPLConfigFeature=node.getFeature(FeaturePnPL::class.java)
        mPnPLConfigFeature?.apply {
            addFeatureListener(mPnPLListener)
            enableNotification()
        }
    }

    fun disableNotificationFromNode(node: Node){
        node.getFeature(FeaturePnPL::class.java)?.apply {
            removeFeatureListener(mPnPLListener)
            disableNotification()
        }
    }

    fun filterContent(cont_names: List<String>, comp_list: MutableList<PnPLComponent>){
        for (comp in comp_list){
            val contList = emptyList<PnPLContent>().toMutableList()
            for (cont in comp.cont_list){
                if (cont.cont_name in cont_names){
                    contList.add(cont)
                }
            }
            comp.cont_list = contList
        }
    }

    fun PnPLComponent.toPnPLComponentViewData(collapsed : Boolean=true): PnPLComponentViewData {
        return PnPLComponentViewData(
            component = this,
            isCollapsed = collapsed,
            hasLockedParams = false
        )
    }

    fun parseDeviceModel(strData: String, cont_names: List<String>?=null,collapsed : Boolean=true) {
        val newConfiguration = mutableListOf<PnPLComponentViewData>()

        var components = PnPLParser.getPnPLComponentsMap(strData)?.values
        if(components != null) {
            if(cont_names!=null) {
                //PnPLParser.filterComponentByName(cont_names,components.toMutableList())
                //Search the demos component
                val demosComponent = components.firstOrNull { it.comp_name == "applications_stblesensor" }
                if(demosComponent!=null) {
                    //If I had the demo components
                    val listOfComponentsForDemo = mutableListOf<String>()
                    for (comp in demosComponent.cont_list) {

                       if(comp.cont_name in cont_names) {
                           comp.sub_cont_list?.forEach { subComp ->
                               listOfComponentsForDemo.add(subComp.cont_name)
                           }
                       }
                    }
                    if(listOfComponentsForDemo.isNotEmpty()) {
                        components =PnPLParser.filterComponentByName(listOfComponentsForDemo,components.toMutableList())
                    }
                }
            }
            newConfiguration.addAll(components.map { it.toPnPLComponentViewData(collapsed) })
            mCurrentConfig = newConfiguration
            _PnPLCompList.postValue(mCurrentConfig)
        }
    }

    fun parseDeviceModelControl(strData: String,cont_names: List<String>,collapsed : Boolean=true) {
        val newConfiguration = mutableListOf<PnPLComponentViewData>()

        val components = PnPLParser.getPnPLComponentsMap(strData)?.values
        if(components != null) {
            val newComponents = PnPLParser.filterComponentByName(cont_names,components.toMutableList())
            newConfiguration.addAll(newComponents.map { it.toPnPLComponentViewData(collapsed) })
            mCurrentConfig = newConfiguration
            _PnPLCompList.postValue(mCurrentConfig)
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
            var prop = mCurrentConfig.find{it.component.comp_name == component_name}?.component?.cont_list?.find{it.cont_name == property_name }
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
            var prop = mCurrentConfig.find{it.component.comp_name == component_name}?.component?.cont_list?.find{it.cont_name == property_name }?.sub_cont_list?.find { it.cont_name == sub_property_name }
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

    fun sendPnPLCommandCmd(compName: String, commandName: String, commandFields: Map<String, Any>){
        mPnPLConfigFeature?.sendPnPLCommandCmd(compName, commandName, commandFields)
    }

    fun sendPnPLCommandCmd(compName: String, commandName: String){
        mPnPLConfigFeature?.sendPnPLCommandCmd(compName, commandName)
    }

    private fun updateComponentConfig(newSensor: PnPLComponentViewData) {
        val sensorComponent = mCurrentConfig.find{ it.component.comp_name == newSensor.component.comp_name }
        val sId = mCurrentConfig.indexOf(sensorComponent)
        mCurrentConfig.removeAt(sId)
        mCurrentConfig.add(sId,newSensor)
        _PnPLCompList.postValue(mCurrentConfig.toList())
    }

    fun setComponentToLoadFile(comp_name: String, cont_name: String, req_name: String){
        mCompName4File = comp_name
        mContName4File = cont_name
        mReqName4File = req_name
    }

    private fun isCommentLine(line:String):Boolean{
        return !line.startsWith("--")
    }
    private fun compressUCFString(ucfContent: String): String {
        val isSpace = "\\s+".toRegex()
        return ucfContent.lineSequence()
            .filter { isCommentLine(it) }
            .map { it.replace(isSpace, "").drop(2) }
            .joinToString("")
    }

    fun sendFileToSelectedComponent(file: Uri?, contentResolver: ContentResolver){
        if(file == null){
            _error.postValue("Invalid file")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val stream = contentResolver.openInputStream(file)
                if(stream==null){
                    _isLoading.postValue(false)
                    _error.postValue("Impossible to read file")
                    return@launch
                }
                val fileContent = stream.readBytes().toString(Charsets.UTF_8)
                stream.close()
                val fileExt = fileContent.split('.').last()
                var postProcFile = fileContent
                if (fileExt == "ucf"){
                    postProcFile = compressUCFString(fileContent)
                }
                mPnPLConfigFeature?.sendPnPLCommandCmd(
                    mCompName4File,
                    mContName4File,
                    mReqName4File,
                    mapOf("size" to postProcFile.length, "data" to postProcFile),
                    Runnable {
                        _isLoading.postValue(false)
                    }
                )
            }catch (e: FileNotFoundException){
                e.printStackTrace()
                _isLoading.postValue(false)
                _error.postValue("File not found")
            }catch (e: IOError){
                e.printStackTrace()
                _isLoading.postValue(false)
                _error.postValue("Impossible to read file")
            }
        }
    }

    fun collapseComponent(selected: PnPLComponentViewData) {
        Log.d("ConfigViewMode","select to: ${selected.component.comp_display_name}")
        val newComponent = selected.copy(isCollapsed = true)
        updateComponentConfig(newComponent)
    }

    fun expandComponent(selected: PnPLComponentViewData) {
        Log.d("ConfigViewMode","select to: ${selected.component.comp_display_name}")
        val newComponent = selected.copy(isCollapsed = false)
        updateComponentConfig(newComponent)
    }
}