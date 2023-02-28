package com.st.blesensor.cloud.AzureIoTCentralPnP

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException
import com.st.BlueSTSDK.Feature
import com.st.blesensor.cloud.AzureIoTCentralPnP.AzurePnPHelperFunction.PnpConvention
import com.st.blesensor.cloud.util.SubSamplingFeatureListener
import com.microsoft.azure.sdk.iot.device.Message
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudContentCapabilityModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap


class AzureIoTCentralPnPFeatureListener(
    val client: AzureIoTCentralPnPConnection,
    val updateRateMs: Long,
    val ctx: Context?
) : SubSamplingFeatureListener(updateRateMs) {

    private var capabilityModel: CloudContentCapabilityModel? = null
    private var mFeatureNameTextView: TextView?=null
    private var mFeatureDetailTextView: TextView?=null

    private val mFeatureMap = mutableMapOf<String, String?>()


    //Found the capability model of the current Device
    fun setCapabilityModel(currentDevice: AzureCloudDevice) {
        capabilityModel =
            currentDevice.templateModel?.capabilityModel?.contents?.firstOrNull { it.name == "std_comp" }
    }

    //Check if one Feature is present on the Capability Model
    fun isSupportFeature(f: Feature): Boolean {
        var isSupported = false

        if(ctx!=null) {

            val catalogListBleCharsDtmiName = ReadBoardFirmwareDataBase(ctx).getAllBleCharacteristics().filter { it.dtmi_name!=null }

            val featureName = f.name.lowercase().replace(" ", "_")


            //Check between catalog dtmi_name and Feature Name
            catalogListBleCharsDtmiName.forEach { bleChar ->
                if(bleChar.dtmi_name== featureName) {
                    val schema = capabilityModel?.schema?.contents?.firstOrNull { it.name.lowercase() == bleChar.dtmi_name?.lowercase() }
                    isSupported = schema !=null
                    mFeatureMap[featureName]= bleChar.dtmi_name?.lowercase()
                }
            }

            if(!isSupported) {
                //Check between uuids....
                val uuidFeature = f.parentNode.getCorrespondingUUID(f).toString()
                catalogListBleCharsDtmiName.forEach { bleChar ->
                    val uuid = bleChar.uuid
                    if(uuid==uuidFeature)  {
                        val schema = capabilityModel?.schema?.contents?.firstOrNull { it.name.lowercase() == bleChar.dtmi_name?.lowercase() }
                        isSupported = schema !=null
                        mFeatureMap[featureName]= bleChar.dtmi_name?.lowercase()
                    }
                }
            }
        }

        return isSupported
    }

    fun setTextViewsForDataSample(name: TextView?, detail: TextView?) {
        mFeatureNameTextView = name
        mFeatureDetailTextView = detail
    }

    override fun onNewDataUpdate(f: Feature, sample: Feature.Sample) {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: onNewDataUpdate")
        val componentName = "std_comp"

        val featureName = f.name.lowercase().replace(" ", "_")
        val schemaModel =
            capabilityModel?.schema?.contents?.firstOrNull { it.name.lowercase() == mFeatureMap[featureName] }
        if (schemaModel != null) {
            if (schemaModel.schema != null) {
                if (schemaModel.schema.type != null) {
                    var message: Message? = null

                    if (schemaModel.schema.type == "double") {
                        //Serial Value
                        val telemetryName = schemaModel.name
                        val currentValue: Double = sample.data[0].toDouble()
                        //if (telemetryName != null) {
                            message = PnpConvention.createIotHubMessageUtf8(
                                telemetryName,
                                currentValue,
                                componentName
                            )

                            //Update the Feature's Name Text View
                            val sendingName = "Sending ${telemetryName}:"

                            //Update the Feature's Detail Text View
                            val sampleValues = StringBuilder()
                            sampleValues.append(sample.data[0])
                            sampleValues.append(" ")

                            if(sample.dataDesc[0].unit!=null) {
                                sampleValues.append(sample.dataDesc[0].unit)
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                mFeatureNameTextView?.text = sendingName
                                mFeatureDetailTextView?.text = sampleValues
                            }
                        //}
                    } else {
                        //Object Value... for example:
                        //accelerometer": {
                        //  "a_y": -7,
                        //  "a_z": 988,
                        //  "a_x": -263
                        //}
                        val telemetryName = schemaModel.name
                        val obj: MutableMap<String, Double> = HashMap()
                        val sampleValues = StringBuilder()

                        if (schemaModel.schema.fields != null) {

                            //Sample data must have at least the same number of elements of the fields array
                            //if (sample.data.size >= schemaModel.schema.fields.size) {

                            for(index in 0 until minOf(sample.data.size,schemaModel.schema.fields.size)) {
                                obj[schemaModel.schema.fields[index]?.name!!] =
                                    sample.data[index].toDouble()

                                sampleValues.append(sample.dataDesc[index].name)
                                    .append("=")
                                    .append(sample.data[index].toString())

                                //For avoiding the null for sample type
                                if(sample.dataDesc[index].unit!=null) {
                                    sampleValues.append(sample.dataDesc[index].unit)
                                }

                                sampleValues.append(" ")
                            }
                            //}

                            //if (telemetryName != null) {
                                val payload = Collections.singletonMap<String, Any>(telemetryName, obj)
                                message = PnpConvention.createIotHubMessageUtf8(
                                    payload,
                                    componentName
                                )

                                //Update the Feature's Name Text View
                                val sendingName = "Sending ${telemetryName}:"
                                CoroutineScope(Dispatchers.Main).launch {
                                    mFeatureNameTextView?.text = sendingName

                                    //Update the Feature's Detail Text View
                                    mFeatureDetailTextView?.text = sampleValues
                                }
                            //}
                        }
                    }
                    if (message != null) {
                        client.mBackgroundThread.submit {
                            try {
                                client.mAzureCom?.sendMessageWithoutCallback(message)
                            } catch (e: IoTCentralException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
}