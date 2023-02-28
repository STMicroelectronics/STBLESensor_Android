package com.st.BlueMS.demos.Textual

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureGenPurpose
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Utils.NumberConversion
import com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic

class GenericTextualViewModel : ViewModel() {

    private var mSelectedFeature: Feature? = null
    private var mSelectedBleCharDesc: BleCharacteristic? = null

    private val _sample_data = MutableLiveData<String?>(null)
    val sample_data: LiveData<String?>
        get() = _sample_data

    private val featureListener = Feature.FeatureListener { _, sample ->
        when (mSelectedFeature) {
            is FeatureGenPurpose -> {
                if (mSelectedBleCharDesc != null) {
                    //We need to parse the data
                    if (mSelectedBleCharDesc!!.format_notify != null) {
                        var offset = 0
                        val sampleValues = StringBuilder()
                        sampleValues.append("TS =${sample.timestamp}:\n")
                        for (field in mSelectedBleCharDesc!!.format_notify!!) {

                            //we Skip the timestamp
                            if (field.name != "timestamp") {
                                val data = FeatureGenPurpose.getRawData(sample)
                                var value: Float? = null
                                when (field.type) {
                                    Field.Type.Float -> {
                                        value =
                                            NumberConversion.LittleEndian.bytesToFloat(data, offset)
                                    }
                                    Field.Type.Int64 -> {
                                        sampleValues.append(" Int64 not supported.. skip sample\n")
                                    }
                                    Field.Type.UInt32 -> {
                                        value = NumberConversion.LittleEndian.bytesToUInt32(
                                            data,
                                            offset
                                        ).toFloat()
                                    }
                                    Field.Type.Int32 -> {
                                        value =
                                            NumberConversion.LittleEndian.bytesToInt32(data, offset)
                                                .toFloat()
                                    }
                                    Field.Type.UInt16 -> {
                                        value = NumberConversion.LittleEndian.bytesToUInt16(
                                            data,
                                            offset
                                        ).toFloat()
                                    }
                                    Field.Type.Int16 -> {
                                        value =
                                            NumberConversion.LittleEndian.bytesToInt16(data, offset)
                                                .toFloat()
                                    }
                                    Field.Type.UInt8 -> {
                                        value = NumberConversion.byteToUInt8(data, offset).toFloat()
                                    }
                                    Field.Type.Int8 -> {
                                        value = data[offset].toFloat()

                                    }
                                    Field.Type.ByteArray -> {
                                        sampleValues.append(" ByteArray not supported.. skip sample\n")
                                    }
                                    null -> {
                                        sampleValues.append(" type not present.. skip sample\n")
                                    }
                                    else -> {
                                        sampleValues.append(" type not supported.. skip sample\n")
                                    }
                                }
                                if (value != null) {
                                    value *= field.scalefactor
                                    value += field.offset

                                    sampleValues.append(" ${field.name} = $value")

                                    field.unit.let { sampleValues.append(" [${field.unit}]")}

                                    if((field.min!=null) || (field.max!=null)){
                                        sampleValues.append(" <")
                                        field.min?.let { sampleValues.append("${field.min}") }

                                        sampleValues.append("...")

                                        field.max?.let { sampleValues.append("${field.max}") }

                                        sampleValues.append(">")
                                    }

                                    sampleValues.append("\n")
                                }
                                //Move to next sample
                                offset += field.length
                            }
                        }
                        _sample_data.postValue(sampleValues.toString())
                    } else {
                        _sample_data.postValue("TS =${sample.timestamp}: Sample=${sample.data.size}\n\n")
                    }
                } else {
                    _sample_data.postValue("TS =${sample.timestamp}: Sample=${sample.data.size}\n\n")
                }
            }
            else -> {
                //if (sample.data.size == sample.dataDesc.size) {
                    val sampleValues = StringBuilder()
                    sampleValues.append("TS =${sample.timestamp}:\n")
                    for (index in sample.data.indices) {
                        sampleValues.append(" Sample = ${sample.data[index]} ${sample.dataDesc[index].name}\n")
                    }
                    sampleValues.append("\n")
                    _sample_data.postValue(sampleValues.toString())
//                } else {
//                    Log.w("GenericTextualDemo", "Sample and dataDesc have different dimension")
//                }
            }
        }
    }

    fun setSelectedFeature(f: Feature?, desc: BleCharacteristic?) {
        mSelectedFeature = f
        mSelectedBleCharDesc = desc
    }

    fun getSelectedFeature() = mSelectedFeature

    fun enableFeatureNotification() {
        mSelectedFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    fun disableFeatureNotification() {
        mSelectedFeature?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
    }

    fun clearLastSampleData() {
        _sample_data.postValue(null)
    }
}