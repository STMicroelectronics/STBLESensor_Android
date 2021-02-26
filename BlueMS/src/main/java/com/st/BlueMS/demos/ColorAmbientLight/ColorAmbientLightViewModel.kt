package com.st.BlueMS.demos.ColorAmbientLight

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureColorAmbientLight
import com.st.BlueSTSDK.Node

class ColorAmbientLightViewModel : ViewModel() {

    var mFeature : FeatureColorAmbientLight? =null

    private val _lux_value = MutableLiveData<Int?>(null)
    val lux_value: LiveData<Int?>
        get() = _lux_value

    private val _cct_value = MutableLiveData<Short?>(null)
    val cct_value: LiveData<Short?>
        get() = _cct_value

    private val _uv_index = MutableLiveData<Short?>(null)
    val uv_index: LiveData<Short?>
        get() = _uv_index

    companion object {
        const val TAG = "ColorAmbLightViewModel"
    }

    private val featureListener = Feature.FeatureListener { _, sample ->
        //Log.i(TAG, "sample received")
        // Post the new values to Fragment
        _lux_value.postValue(FeatureColorAmbientLight.getLuxValue(sample))
        _cct_value.postValue(FeatureColorAmbientLight.getCCTValue(sample))
        _uv_index.postValue(FeatureColorAmbientLight.getUVIndexValue(sample))

    }

    fun enableNotification(node: Node){
        mFeature = node.getFeature(FeatureColorAmbientLight::class.java) ?: null
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    fun disableNotification(node: Node){
        node.getFeature(FeatureColorAmbientLight::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }

    fun getMinValueLux() : Int {
        return FeatureColorAmbientLight.DATA_MIN_LUX
    }

    fun getMaxValueLux() : Int {
        return FeatureColorAmbientLight.DATA_MAX_LUX
    }

    fun getMinValueCCT() : Short {
        return FeatureColorAmbientLight.DATA_MIN_CCT
    }

    fun getMaxValueCCT() : Short {
        return FeatureColorAmbientLight.DATA_MAX_CCT
    }

    fun getMinValueUVIndex() : Short {
        return FeatureColorAmbientLight.DATA_MIN_UV_INDEX
    }

    fun getMaxValueUVIndex() : Short {
        return FeatureColorAmbientLight.DATA_MAX_UV_INDEX
    }
}