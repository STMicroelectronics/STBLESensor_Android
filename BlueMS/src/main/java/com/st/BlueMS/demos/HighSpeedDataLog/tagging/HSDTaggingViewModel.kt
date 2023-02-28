package com.st.BlueMS.demos.HighSpeedDataLog.tagging

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueMS.R
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Tag
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.TagHW
import com.st.BlueSTSDK.Node
import java.text.SimpleDateFormat
import java.util.*

internal open class HSDTaggingViewModel :ViewModel(){

    private val tagListener = Feature.FeatureListener { _, sample ->

        val deviceStatus = FeatureHSDataLogConfig.getDeviceStatus(sample)
        if(deviceStatus != null){
            if(deviceStatus.isSDCardInserted != null)
                _isSDCardInserted.postValue(deviceStatus.isSDCardInserted)
            if(deviceStatus.isSDLogging != null){
                _isLogging.postValue(deviceStatus.isSDLogging)
                if(deviceStatus.isSDLogging == true)
                    mConfigFeature?.sendGetCmd(HSDGetDeviceInfoCmd())
            }

            //ask the tag list after we have the log status to avoid sending multiple commands
            if(mAnnotationViewDataList.isEmpty())
                mConfigFeature?.sendGetCmd(HSDGetTagConfigCmd())
        }

        val annotation = FeatureHSDataLogConfig.getDeviceTagConfig(sample) ?: return@FeatureListener

        mAnnotationViewDataList.clear()
        mAnnotationViewDataList.addAll(annotation.softwareTags.map { it.toAnnotationViewData() })
        mAnnotationViewDataList.addAll(annotation.hardwareTags.map { it.toAnnotationViewData() })
        _annotation.postValue(mAnnotationViewDataList.toList())

        if (_isLogging.value == null || _isLogging.value == false) {
            _areAllSelected.postValue(true)
        } else {
            _areAllSelected.postValue(false)
        }
    }

    private fun Tag.toAnnotationViewData(): AnnotationViewData {
        val isLogging = (this@HSDTaggingViewModel.isLogging.value ?: false)
        return if (this is TagHW) {
            AnnotationViewData(id = this.id,
                    label = this.label,
                    pinDesc = this.pinDesc,
                    tagType = R.string.annotationView_hwType,
                    isSelected = this.isEnabled,
                    userCanEditLabel = !isLogging,
                    userCanSelect = !isLogging)
        } else {
            AnnotationViewData(id = this.id,
                    label = this.label,
                    pinDesc = null,
                    tagType = R.string.annotationView_swType,
                    isSelected = this.isEnabled,
                    userCanEditLabel = !isLogging,
                    userCanSelect = true)
        }
    }

    private var mConfigFeature: FeatureHSDataLogConfig? = null

    val mAnnotationViewDataList = mutableListOf<AnnotationViewData>()
    val _annotation = MutableLiveData(mAnnotationViewDataList.toList())
    val annotations:LiveData<List<AnnotationViewData>>
        get() = _annotation

    val _isLogging = MutableLiveData(false)
    val isLogging:LiveData<Boolean>
        get() = _isLogging

    val _isSDCardInserted = MutableLiveData(false)
    val isSDCardInserted:LiveData<Boolean>
        get() = _isSDCardInserted

    val _areAllSelected = MutableLiveData(false)
    val areAllSelected:LiveData<Boolean>
        get() = _areAllSelected

    open fun enableNotification(node: Node){
        mConfigFeature = node.getFeature(FeatureHSDataLogConfig::class.java)
        mConfigFeature?.apply {
            addFeatureListener(tagListener)
            enableNotification()
            sendGetCmd(HSDGetLogStatusCmd())
        }
        val lastSample = mConfigFeature?.sample
        if(lastSample!=null){
            tagListener.onUpdate(mConfigFeature!!,lastSample)
        }
    }

    open fun disableNotification(node:Node){
        mConfigFeature?.apply {
            removeFeatureListener(tagListener)
            disableNotification()

        }
    }

    private fun startLog(acquisitionName: String, acquisitionDesc: String) {
        disableLabelEditing()
        _isLogging.postValue(true)
        mConfigFeature?.sendSetCmd(HSDSetAcquisitionInfoCmd(acquisitionName,acquisitionDesc))

        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

        mConfigFeature?.sendControlCmd(HSDStartLoggingCmd(date))
    }

    private fun stopLog(){
        enableLabelEditing()
        _isLogging.postValue(false)

        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

        mConfigFeature?.sendControlCmd(HSDStopLoggingCmd(date))
    }

    fun enableLabelEditing() {
        val nextStatus = mAnnotationViewDataList.map {annotation ->
            if(annotation.tagType == R.string.annotationView_hwType){
                annotation.copy(userCanSelect = true,userCanEditLabel = true)
            }else{
                annotation.copy(userCanSelect = true,userCanEditLabel = true)
            }
        }
        mAnnotationViewDataList.clear()
        mAnnotationViewDataList.addAll(nextStatus)
        _annotation.postValue(nextStatus)
    }

    fun disableLabelEditing() {
        val filteredTags = mAnnotationViewDataList.filter { it.isSelected }
        val nextStatus = filteredTags.map {annotation ->
            if(annotation.tagType == R.string.annotationView_hwType){
                annotation.copy(userCanSelect = false,userCanEditLabel = false)
            }else{
                annotation.copy(userCanSelect = true,userCanEditLabel = false, isSelected = false)
            }
        }
        mAnnotationViewDataList.clear()
        mAnnotationViewDataList.addAll(nextStatus)
        _annotation.postValue(nextStatus)
    }

    open fun onStartStopLogPressed(acquisitionName: String, acquisitionDesc: String) {
        if(_isSDCardInserted.value == true){
            if(_isLogging.value==true){
                stopLog()
            }else{
                startLog(acquisitionName,acquisitionDesc)
            }
        }
    }

    fun removeAnnotation(annotation: AnnotationViewData) {
        val annotationIndex = mAnnotationViewDataList.indexOfFirst { it.id == annotation.id }
        if(annotationIndex == -1)
            return
        mAnnotationViewDataList.removeAt(annotationIndex)
        _annotation.postValue(mAnnotationViewDataList.toList())
    }

    fun addNewTag() {
        val newId = mAnnotationViewDataList.maxByOrNull { it.id }?.id?.inc() ?: 0
        val newTag = AnnotationViewData(newId,"Tag $newId",null,R.string.annotationView_swType,false,true,userCanSelect = true)
        mAnnotationViewDataList.add(0,newTag)
        _annotation.postValue(mAnnotationViewDataList.toList())
    }

    open fun changeTagLabel(selected: AnnotationViewData, label: String) {
        val newAnnotation = selected.copy(label = label)
        updateAnnotation(newAnnotation)
        if(selected.tagType == R.string.annotationView_hwType){
            mConfigFeature?.sendSetCmd(HSDSetHWTagLabelCmd(selected.id,label))
        }else{
            mConfigFeature?.sendSetCmd(HSDSetSWTagLabelCmd(selected.id,label))
        }
    }

    fun updateAnnotation(newAnnotation: AnnotationViewData) {
        val annotationIndex = mAnnotationViewDataList.indexOfFirst {
            it.id == newAnnotation.id  && it.tagType == newAnnotation.tagType
        }
        mAnnotationViewDataList[annotationIndex] = newAnnotation
        _annotation.postValue(mAnnotationViewDataList.toList())
    }

    open fun selectAnnotation(selected: AnnotationViewData) {
        Log.d("TagViewMode","select to: ${selected.label}")
        val newAnnotation = selected.copy(isSelected = true)
        updateAnnotation(newAnnotation)
        if (selected.tagType == R.string.annotationView_swType) {
            if(_isLogging.value == true)
                mConfigFeature?.sendSetCmd(HSDSetSWTagCmd(selected.id, true))
        }
        else
            mConfigFeature?.sendSetCmd(HSDSetHWTagCmd(selected.id, true))
    }

    open fun deSelectAnnotation(selected: AnnotationViewData) {
        Log.d("TagViewMode","deSelect to: ${selected.label}")
        val newAnnotation = selected.copy(isSelected = false)
        updateAnnotation(newAnnotation)
        if(selected.tagType == R.string.annotationView_swType) {
            if(_isLogging.value == true)
                mConfigFeature?.sendSetCmd(HSDSetSWTagCmd(selected.id, false))
        }
        else
            mConfigFeature?.sendSetCmd(HSDSetHWTagCmd(selected.id,false))
    }

}