package com.st.BlueMS.demos

import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.Feature

/** VieModel used for Saving some configurations for all the Demos
 * that need to save few values.
 * For Avoiding the necessity to creates too much little ViewModels,
 * one for each of them
 */
class SupportViewModel  : ViewModel() {
    // For Environmental Sensor Demo Fragment
    private var mTemperatureOffset = 0.0f
    private var mTemperatureCelsius = true;

    private var mPressureOffset    = 0.0f
    private var mHumidityOffset    = 0.0f

    fun set_TemperatureOffset(offset: Float) {
        mTemperatureOffset=offset
    }

    fun get_TemperatureOffset() : Float {
        return mTemperatureOffset
    }

    fun set_TemperatureCelsius(status: Boolean) {
        mTemperatureCelsius = status
    }

    fun get_TemperatureCelsius() : Boolean {
        return mTemperatureCelsius
    }

    fun set_PressureOffset(offset: Float) {
        mPressureOffset=offset
    }

    fun get_PressureOffset() : Float {
        return mPressureOffset
    }

    fun set_HumidityOffset(offset: Float) {
        mHumidityOffset=offset
    }

    fun get_HumidityOffset() : Float {
        return mHumidityOffset
    }

    // For Time Of Flight Multi object Demo Fragment
    private var mPresenceDemo=false

    fun get_PresenceDemo(): Boolean {
        return mPresenceDemo
    }

    fun set_PresenceDemo(presenceEnable: Boolean) {
        mPresenceDemo = presenceEnable
    }

    //for GNSS Demo Fragment
    private var mLocationData = LocationData(0.0f,0.0f)

    private var mPositionOnMap = false;

    fun set_LocationData(value: LocationData) {
        mLocationData = value
    }

    fun get_LocationData() : LocationData {
        return mLocationData
    }

    fun set_PositionOnMap(value: Boolean) {
        mPositionOnMap = value
    }

    fun get_PositionOnMap(): Boolean {
        return mPositionOnMap
    }

    //For Cloud Demo
    private var mDefaultCloudUpdateInterval = 5000
    fun set_DefaultCloudUpdateInterval (value: Int) { mDefaultCloudUpdateInterval =value}
    fun get_DefaultCloudUpdateInterval() = mDefaultCloudUpdateInterval

    private var mSelectedCloudDemo = 0
    fun set_SelectedCloudDemo(value : Int) {mSelectedCloudDemo=value}
    fun get_SelectedCloudDemo() = mSelectedCloudDemo

    private var mCloudUpLoadButtonVisible = false
    fun set_CloudUploadButtonVisible(value: Boolean) { mCloudUpLoadButtonVisible=value}
    fun get_CloudUploadButtonVisible() = mCloudUpLoadButtonVisible

    private var mCloudUploadButtonNumSample = 0;
    fun set_CloudUploadButtonNumSample(value: Int) {mCloudUploadButtonNumSample = value}
    fun get_CloudUploadButtonNumSample() = mCloudUploadButtonNumSample

    private var mUID: String?=null
    fun set_mUID(value: String) { mUID = value }
    fun get_mUID() = mUID

    private var mRunningFwVersion: String?=null
    fun set_RunningFwVersion(value: String) { mRunningFwVersion=value}
    fun get_RunningFwVersion() = mRunningFwVersion

    //For BinaryContent Demo
    private var mMaxPayloadSize =20
    //private var mFeature: Feature?=null
    fun set_MaxPayLoadSize(value: Int) {mMaxPayloadSize=value}
    fun get_MaxPayLoadSize() = mMaxPayloadSize
}

//Data Class Used for GNSS Demo
data class LocationData(
    @SerializedName("latitude")
    val latitude: Float,
    @SerializedName("longitude")
    val longitude: Float)