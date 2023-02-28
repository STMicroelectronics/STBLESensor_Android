package com.st.blesensor.cloud.stazure.iotHub

import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property
import com.microsoft.azure.sdk.iot.device.Message
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.*
import com.st.blesensor.cloud.util.SubSamplingFeatureListener
import org.json.JSONObject

internal class STAzureFeatureListener(private val client: DeviceClient, updateRateMs: Long)
    : SubSamplingFeatureListener(updateRateMs) {

    companion object {
        fun isSupportedFeature(f: Feature): Boolean {
            return SUPPORTED_FEATURES.contains(f.javaClass)
        }

        private val SUPPORTED_FEATURES = listOf(FeatureTemperature::class.java,
                FeaturePressure::class.java,
                FeatureHumidity::class.java,
                FeatureAcceleration::class.java,
                FeatureMagnetometer::class.java,
                FeatureGyroscope::class.java)
    }


    init {
        val updateInterval = Property("TelemetryInterval",updateRateMs/1000)
        client.sendReportedProperties(setOf(updateInterval))
    }

    private fun prepareIotHubMessage(f: Feature,sample: Feature.Sample): Message?{
        return when(f){
            is FeatureTemperature -> buildTemperatureMessage(sample)
            is FeaturePressure -> buildPressureMessage(sample)
            is FeatureHumidity -> buildHumidityMessage(sample)
            is FeatureAcceleration -> buildAccelerationMessage(sample)
            is FeatureMagnetometer -> buildMagnetometerMessage(sample)
            is FeatureGyroscope -> buildGyroscopeMessage(sample)
            else -> null
        }
    }

    private fun buildGyroscopeMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("gyrX",FeatureGyroscope.getGyroX(sample))
            put("gyrY",FeatureGyroscope.getGyroY(sample))
            put("gyrZ",FeatureGyroscope.getGyroZ(sample))
        }
        return Message(obj.toString(0))
    }

    private fun buildMagnetometerMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("magX",FeatureMagnetometer.getMagX(sample))
            put("magY",FeatureMagnetometer.getMagY(sample))
            put("magZ",FeatureMagnetometer.getMagZ(sample))
        }
        return Message(obj.toString(0))
    }

    private fun buildAccelerationMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("accX",FeatureAcceleration.getAccX(sample))
            put("accY",FeatureAcceleration.getAccY(sample))
            put("accZ",FeatureAcceleration.getAccZ(sample))
        }
        return Message(obj.toString(0))
    }

    private fun buildTemperatureMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("Temperature",FeatureTemperature.getTemperature(sample))
        }
        return Message(obj.toString(0))
    }

    private fun buildPressureMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("Pressure",FeaturePressure.getPressure(sample))
        }
        return Message(obj.toString(0))
    }

    private fun buildHumidityMessage(sample: Feature.Sample): Message {
        val obj = JSONObject().apply {
            put("Humidity",FeatureHumidity.getHumidity(sample))
        }
        return Message(obj.toString(0))
    }

    override fun onNewDataUpdate(f: Feature, sample: Feature.Sample) {
        prepareIotHubMessage(f,sample)?.also {
            client.sendEventAsync(it,null,null)
        }
    }

}