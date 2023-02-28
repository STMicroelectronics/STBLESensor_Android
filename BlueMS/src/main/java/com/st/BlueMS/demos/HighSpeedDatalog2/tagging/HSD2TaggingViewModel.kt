package com.st.BlueMS.demos.HighSpeedDatalog2.tagging

import android.util.Log
import com.st.BlueMS.R
import com.st.BlueMS.demos.HighSpeedDataLog.tagging.AnnotationViewData
import com.st.BlueMS.demos.HighSpeedDataLog.tagging.HSDTaggingViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.PnPL.*

import com.st.BlueSTSDK.Node

internal class HSD2TaggingViewModel: HSDTaggingViewModel(){

    private var mPnPLConfigFeature: FeaturePnPL? = null

    private val tagListener = Feature.FeatureListener { _, sample ->

        val componentStatus = FeaturePnPL.getPnPLComponentStatus(sample)
        if(componentStatus?.comp_name == "log_controller"){
            Log.e("HSD2TaggingViewModel","--> log_controller status received")
            val isLogging = componentStatus.cont_list.find{it.cont_name == "log_status"}?.cont_info as Boolean
            if(_isLogging.value != isLogging){
                _isLogging.postValue(isLogging)
            }
            val isSDCardInserted = componentStatus.cont_list.find{it.cont_name == "sd_mounted"}?.cont_info as Boolean
            _isSDCardInserted.postValue(isSDCardInserted)
        }

        //Tag Classes (tags_info)
        if(componentStatus?.comp_name == "tags_info") {
            Log.e("HSD2TaggingViewModel","--> tags_info status received")
            mAnnotationViewDataList.clear()
            componentStatus.cont_list.forEach { content ->
                if (content.cont_name.contains("sw_tag") || content.cont_name.contains("hw_tag")) {
                    val tagClassId = content.cont_name.filter { it.isDigit() }.toInt()
                    val tagClass = content.toAnnotationViewData(tagClassId)
                    mAnnotationViewDataList.add(tagClass)
                }
            }
            _annotation.postValue(mAnnotationViewDataList.toList())
        }

        if (_isLogging.value == null || _isLogging.value == false) {
            _areAllSelected.postValue(true)
        } else {
            _areAllSelected.postValue(false)
        }
    }

    private fun PnPLContent.toAnnotationViewData(id:Int): AnnotationViewData {
        val isLogging = (this@HSD2TaggingViewModel.isLogging.value ?: false)
        return AnnotationViewData(
            id = id,
            label = this.sub_cont_list!!.find { it.cont_name == "label"}!!.cont_info as String,
            pinDesc = this.cont_name,
            tagType = if(this.cont_name.contains("sw_tag")) R.string.annotationView_swType else R.string.annotationView_hwType,
            isSelected = this.sub_cont_list!!.find { it.cont_name == "enabled"}!!.cont_info as Boolean,
            userCanEditLabel = !isLogging,
            userCanSelect = !isLogging)
    }

    override fun enableNotification(node: Node){
        mPnPLConfigFeature = node.getFeature(FeaturePnPL::class.java)
        mPnPLConfigFeature?.apply {
            addFeatureListener(tagListener)
            enableNotification()
        }
    }

    override fun disableNotification(node:Node){
        mPnPLConfigFeature?.apply {
            removeFeatureListener(tagListener)
            disableNotification()
        }
    }

    fun sendPnPLGetDeviceStatus(){
        mPnPLConfigFeature?.apply {
            sendPnPLGetDeviceStatusCmd(PnPLGetDeviceStatusCmd())
        }
    }

    fun sendPnPLGetComponentStatus(compName: String){
        mPnPLConfigFeature?.apply {
            sendPnPLGetComponentStatusCmd(PnPLGetComponentStatusCmd(compName))
        }
    }

    private fun startLog(acquisitionName: String, acquisitionDesc: String) {
        disableLabelEditing()
        _isLogging.postValue(true)
        val setPropCmd = PnPLSetPropertyCmd("acquisition_info",
            listOf(PnPLSetProperty("name",acquisitionName), PnPLSetProperty("description",acquisitionDesc)
        ))
        mPnPLConfigFeature?.sendPnPLSetPropertyCmd(setPropCmd)
        mPnPLConfigFeature?.sendPnPLCommandCmd("log_controller","start_log", mapOf("interface" to 0))
    }

    private fun stopLog(){
        enableLabelEditing()
        _isLogging.postValue(false)
        mPnPLConfigFeature?.sendPnPLCommandCmd("log_controller","stop_log")
    }

    override fun changeTagLabel(selected: AnnotationViewData, label: String) {
        val newAnnotation = selected.copy(label = label)
        updateAnnotation(newAnnotation)
        val setTagLabelCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("label", label))
        mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagLabelCmd)))
    }

    override fun selectAnnotation(selected: AnnotationViewData) {
        Log.d("TagViewMode","select to: ${selected.label}")
        val newAnnotation = selected.copy(isSelected = true)
        updateAnnotation(newAnnotation)
        if (selected.tagType == R.string.annotationView_swType) {
            if(_isLogging.value == true){
                val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("status", true))
                mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
            } else {
                val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("enabled", true))
                mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
            }
        }
        else {
            val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("enabled", true))
            mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
        }
    }

    override fun deSelectAnnotation(selected: AnnotationViewData) {
        Log.d("TagViewMode","deSelect to: ${selected.label}")
        val newAnnotation = selected.copy(isSelected = false)
        updateAnnotation(newAnnotation)
        if(selected.tagType == R.string.annotationView_swType) {
            if(_isLogging.value == true){
                val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("status", false))
                mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
            } else {
                val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("enabled", false))
                mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
            }
        }
        else {
            val setTagStatusCmd = PnPLSetProperty(selected.pinDesc!!, PnPLSetProperty("enabled", false))
            mPnPLConfigFeature?.sendPnPLSetPropertyCmd(PnPLSetPropertyCmd("tags_info", listOf(setTagStatusCmd)))
        }
    }

    override fun onStartStopLogPressed(acquisitionName: String, acquisitionDesc: String) {
        if(_isSDCardInserted.value == true){
            if(_isLogging.value==true){
                stopLog()
            }else{
                startLog(acquisitionName,acquisitionDesc)
            }
        }
    }
}