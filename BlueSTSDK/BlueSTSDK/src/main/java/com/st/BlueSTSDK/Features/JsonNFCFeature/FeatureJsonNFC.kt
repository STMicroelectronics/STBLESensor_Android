package com.st.BlueSTSDK.Features.JsonNFCFeature

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.STL2TransportProtocol
import java.net.URL

class FeatureJsonNFC  constructor(n: Node) :
    Feature(FEATURE_NAME, n, arrayOf(FEATURE_JSON_CONFIG),false) {

    private var mSTL2TransportDecoder = STL2TransportProtocol()

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val commandString = mSTL2TransportDecoder.decapsulate(data)

        if(commandString!=null) {
            Log.i("FeatureJsonNFC", String(commandString))
            val responseObj = JsonNFCParser.getJsonObj(commandString)

            return ExtractResult(FeatureJsonCommandSample(responseObj), data.size)
        }
        return ExtractResult(null, data.size)
    }

    fun writeCommandWithoutArgument(commandName: String) {
        sendWrite(mSTL2TransportDecoder.encapsulate( Gson().toJson(JsonWriteCommand(commandName))),null)
    }

    fun writeCommandWithArgument(command: JsonWriteCommand) {
        sendWrite(mSTL2TransportDecoder.encapsulate( Gson().toJson(command)),null)
    }

    private fun sendWrite(bytesToSend: ByteArray, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > 20) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + 20))
            byteSend += 20
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size), onSendComplete)
        } //if
    }

    companion object {
        private const val FEATURE_NAME = "JsonNFC"
        private const val FEATURE_DATA_NAME = "Json NFC"
        private val FEATURE_JSON_CONFIG = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)

        const val ReadModes = "ReadModes"
        const val NFCWifi = "NFCWiFi"
        const val NFCVCard = "NFCVCard"
        const val NFCURL = "NFCURL"
        const val NFCText = "GenericText"


        val WifiEncrString = mapOf("NONE" to 1, "WEP" to 2, "TKIP" to 4, "AES" to 8)

        /*
         NDEF_WIFI_ENCRYPTION_NONE = 0x0001, /**< WPS No Encryption (set to 0 for Android native support / should be 1) */
         NDEF_WIFI_ENCRYPTION_WEP  = 0x0002, /**< WPS Encryption based on WEP  */
         NDEF_WIFI_ENCRYPTION_TKIP = 0x0004, /**< WPS Encryption based on TKIP  */
         NDEF_WIFI_ENCRYPTION_AES  = 0x0008 /**< WPS Encryption based on AES  */
        */

        val WifiAuthString = mapOf("NONE" to 1, "WPAPSK" to 2, "SHARED" to 4, "WPA" to 8, "WPA2" to 16, "WPA2PSK" to 32)

        /*
         NDEF_WIFI_AUTHENTICATION_NONE     = 0x0001, /**< WPS No Authentication (set to 0 for Android native support / should be 1)  */
         NDEF_WIFI_AUTHENTICATION_WPAPSK   = 0x0002, /**< WPS Authentication based on WPAPSK  */
         NDEF_WIFI_AUTHENTICATION_SHARED   = 0x0004, /**< WPS Authentication based on ??  */
         NDEF_WIFI_AUTHENTICATION_WPA      = 0x0008, /**< WPS Authentication based on WPA  */
         NDEF_WIFI_AUTHENTICATION_WPA2     = 0x0010, /**< WPS Authentication based on WPA2  */
         NDEF_WIFI_AUTHENTICATION_WPA2PSK  = 0x0020 /**< WPS Authentication based on WPA2PSK  */
        */

        val UrlTypeString = mapOf("http://www." to 1, "https://www." to 2)


        fun resultCommandReadModes(responseObj: JsonObject?): String? {
            return JsonNFCParser.extractAnswer(responseObj)
        }
    }

    class FeatureJsonCommandSample(val command: JsonObject?) :
        Sample(emptyArray(), arrayOf(FEATURE_JSON_CONFIG))
}

data class JsonReadModes(
    @SerializedName("Answer")
    val Answer: String?,
)

data class JsonWriteCommand(
    @SerializedName("Command")
    val Command: String?=null,
    @SerializedName("GenericText")
    val GenericText: String?=null,
    @SerializedName("NFCWiFi")
    val NFCWiFi: JsonWIFI?=null,
    @SerializedName("NFCVCard")
    var NFCVCard: JsonVCard?=null,
    @SerializedName("NFCURL")
    var NFCURL: String?=null
)

data class JsonWIFI(
    @SerializedName("NetworkSSID")
    val NetworkSSID: String?=null,
    @SerializedName("NetworkKey")
    val NetworkKey: String?=null,
    @SerializedName("AuthenticationType")
    val AuthenticationType: Int=0,
    @SerializedName("EncryptionType")
    val EncryptionType: Int=0
)

data class JsonVCard(
    @SerializedName("Name")
    var Name: String?=null,
    @SerializedName("FormattedName")
    var FormattedName: String?=null,
    @SerializedName("Title")
    var Title: String?=null,
    @SerializedName("Org")
    var Org: String?=null,
    @SerializedName("HomeAddress")
    var HomeAddress: String?=null,
    @SerializedName("WorkAddress")
    var WorkAddress: String?=null,
    @SerializedName("Address")
    var Address: String?=null,
    @SerializedName("HomeTel")
    var HomeTel: String?=null,
    @SerializedName("WorkTel")
    var WorkTel: String?=null,
    @SerializedName("CellTel")
    var CellTel: String?=null,
    @SerializedName("HomeEmail")
    var HomeEmail: String?=null,
    @SerializedName("WorkEmail")
    var WorkEmail: String?=null,
    @SerializedName("Url")
    var Url: String?=null
)

