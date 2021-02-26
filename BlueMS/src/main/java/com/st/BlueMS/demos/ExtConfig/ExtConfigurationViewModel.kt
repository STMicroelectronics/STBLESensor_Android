package com.st.BlueMS.demos.ExtConfig

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings
import com.st.BlueSTSDK.Node
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ExtConfigurationViewModel : ViewModel() {

    private val _commandlist_answer = MutableLiveData<String?>(null)
    val commandlist_answer: LiveData<String?>
        get() = _commandlist_answer

    private val _help_answer = MutableLiveData<String?>(null)
    val help_answer: LiveData<String?>
        get() = _help_answer

    private val _info_answer = MutableLiveData<String?>(null)
    val info_answer: LiveData<String?>
        get() = _info_answer

    private val _uid_answer = MutableLiveData<String?>(null)
    val uid_answer: LiveData<String?>
        get() = _uid_answer

    private val _powerstatus_answer = MutableLiveData<String?>(null)
    val powerstatus_answer: LiveData<String?>
        get() = _powerstatus_answer

    private val _versionfw_answer = MutableLiveData<String?>(null)
    val versionfw_answer: LiveData<String?>
        get() = _versionfw_answer

    private val _certificate_answer = MutableLiveData<String?>(null)
    val certificate_answer: LiveData<String?>
        get() = _certificate_answer

    private val _customcommandlist_answer = MutableLiveData<List<CustomCommand>?>(null)
    val customcommandlist_answer: LiveData<List<CustomCommand>?>
        get() = _customcommandlist_answer

    companion object {
        const val TAG = "ExtConfViewModel"
    }

    private val SET_DATE_FORMAT: DateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val SET_TIME_FORMAT: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    var mFeature : FeatureExtConfiguration? =null

    private var requestedCertificate : String? = null

    private var mcuid : String? = null

    fun enableNotification(node: Node){
        mFeature = node.getFeature(FeatureExtConfiguration::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
            writeCommandWithoutArgument(FeatureExtConfiguration.READ_COMMANDS)
        }
    }

    fun disableNotification(node: Node){
        node.getFeature(FeatureExtConfiguration::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }

        mFeature = null
    }

    private val featureListener = Feature.FeatureListener { _, sample ->
        Log.i(TAG, "sample received")
        if(sample is FeatureExtConfiguration.CommandSample) {
            val responseObj = sample.command
            if (responseObj != null) {
                //Try to retrieve the command list
                var answer = FeatureExtConfiguration.resultCommandList(responseObj)
                if (answer != null) {
                    _commandlist_answer.postValue(answer)
                }

                //Try to retrieve the help
                answer = FeatureExtConfiguration.resultCommandHelp(responseObj)
                if (answer != null) {
                    _help_answer.postValue(answer)
                }

                //Try to retrieve the Info
                answer = FeatureExtConfiguration.resultCommandInfo(responseObj)
                if (answer != null) {
                    _info_answer.postValue(answer)
                }

                //Try to retrieve the uid
                answer = FeatureExtConfiguration.resultCommandSTM32UID(responseObj)
                if (answer != null) {
                    _uid_answer.postValue(answer)
                }

                //Try to retrieve the PowerStatus
                answer = FeatureExtConfiguration.resultCommandPowerStatus(responseObj)
                if (answer != null) {
                    _powerstatus_answer.postValue(answer)
                }

                //Try to retrieve the VersionFW
                answer = FeatureExtConfiguration.resultCommandVersionFW(responseObj)
                if (answer != null) {
                    _versionfw_answer.postValue(answer)
                }

                //Try to retrieve the certificate
                answer = FeatureExtConfiguration.resultCommandCertificate(responseObj)
                if (answer != null) {
                    _certificate_answer.postValue(answer)
                }

                //Try to retrieve the Custom Commands List
                val listCommand = FeatureExtConfiguration.resultCustomCommandList(responseObj)
                if (listCommand != null) {
                    _customcommandlist_answer.postValue(listCommand)
                }
            }
        }
    }

    private  fun timeToString(date: Date): String {
        val timeStr = SET_TIME_FORMAT.format(date)
        return String.format(Locale.getDefault(), "%s", timeStr)
    }

    /**
     * Convert the range Sunday=1 ... saturday=7 to monday =1 ... sunday=7
     * @param dayOfTheWeek dat og the week, 1 = sunday, 2 = monday ... 7= saturday
     * @return change the week where moday is the first day of the week
     */
    private fun toMondayFirst(dayOfTheWeek: Int): Int {
        return if (dayOfTheWeek == Calendar.SUNDAY) 7 else dayOfTheWeek - 1
    }

    /**
     * get the day of the week, in a specifc date
     * @param d date to query
     * @return 1 = monday .. 7 = sunday
     */
    private fun getDayOfTheWeek(d: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = d
        return toMondayFirst(cal[Calendar.DAY_OF_WEEK])
    }

    private fun dateToString(date: Date): String {
        val dateStr = SET_DATE_FORMAT.format(date)
        val dayOfTheWeek = getDayOfTheWeek(date)
        return String.format(Locale.getDefault(), "%02d/%s", dayOfTheWeek, dateStr)
    }

    /** Command selection Methods **/
    // API for writing the command without argument
    fun readCert() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_CERTIFICATE)
    }

    fun readUid() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_UID)
    }


    fun readvFw() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_VERSION_FW)
    }


    fun readInfo() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_INFO)
    }


    fun readHelp() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_HELP)
    }


    fun readPowStatus() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_POWER_STATUS)
    }

    fun clearDB() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.CLEAR_DB)
    }


    fun setDFU() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.SET_DFU)
    }


    fun powerOff() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.POWER_OFF)
    }

    fun readCustomCommands() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)
    }

    // API for writing the command with argument
    fun setPIN(newPIN: Int) {
        mFeature?.writeCommandSetArgumentNumber(FeatureExtConfiguration.CHANGE_PIN,newPIN);
    }


    fun setName(name: String) {
        mFeature?.writeCommandSetArgumentString(FeatureExtConfiguration.SET_NAME,name);
    }

    fun setCert(certificate: String) {
       //Log.i("AWS Certificate","setCert "+ certificate?: "NULL???")
        mFeature?.writeCommandSetArgumentString(FeatureExtConfiguration.SET_CERTIFICATE,certificate);
    }

    fun setTime() {
        val date = Date()
        val string =timeToString(date)
        mFeature?.writeCommandSetArgumentString(FeatureExtConfiguration.SET_TIME,string)
    }

    fun setDate() {
        val date = Date()
        val string =dateToString(date)
        mFeature?.writeCommandSetArgumentString(FeatureExtConfiguration.SET_DATE,string)
    }

    fun setWiFi(wifi: WifSettings) {
        mFeature?.writeCommandSetArgumentJSON(FeatureExtConfiguration.SET_WIFI,wifi)
    }

    fun sendCustomCommandInteger(name: String, value: Int) {
        mFeature?.writeCommandSetArgumentNumber(name,value)
    }

    fun sendCustomCommandString(name: String, value: String) {
        mFeature?.writeCommandSetArgumentString(name,value)
    }

    fun sendCustomCommandVoid(name: String) {
        mFeature?.writeCommandWithoutArgument(name)
    }

    fun commandListReceived() {
        _commandlist_answer.postValue(null)
    }

//    fun customCommandListReceived() {
//        _customcommandlist_answer.postValue(null)
//    }

    fun infoReceived() {
        _info_answer.postValue(null)
    }
    fun helpReceived() {
        _help_answer.postValue(null)
    }
    fun uidReceived() {
        _uid_answer.postValue(null)
    }
    fun powerstatusReceived() {
        _powerstatus_answer.postValue(null)
    }

    fun versionfwReceived() {
        _versionfw_answer.postValue(null)
    }

    fun certificateReceived() {
        _certificate_answer.postValue(null)
    }

    fun setRetrivedCertificate(certificate: String?) {
         requestedCertificate = certificate
    }

    fun getRetrivedCertiticate(): String ? {
        return requestedCertificate
    }

    fun setMCUID(mcuId: String?) {
        mcuid = mcuId
    }
    fun getMCUID(): String ? {
        return mcuid
    }
}