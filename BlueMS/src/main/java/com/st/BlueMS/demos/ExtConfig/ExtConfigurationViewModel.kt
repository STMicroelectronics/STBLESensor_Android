package com.st.BlueMS.demos.ExtConfig

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.ExtConfiguration.BanksStatus
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SubSensorDescriptor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SubSensorStatus
import com.st.BlueSTSDK.Node
import com.st.STWINBoard_Gui.Utils.SensorViewData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ExtConfigurationViewModel : ViewModel() {

    var context: Context?=null

    private val _commandlist_answer = MutableLiveData<String?>(null)
    val commandlist_answer: LiveData<String?>
        get() = _commandlist_answer

    private val _help_answer = MutableLiveData<String?>(null)
    val help_answer: LiveData<String?>
        get() = _help_answer

    private val _info_answer = MutableLiveData<String?>(null)
    val info_answer: LiveData<String?>
        get() = _info_answer

    private val _error_answer = MutableLiveData<String?>(null)
    val error_answer: LiveData<String?>
        get() = _error_answer

    private val _uid_answer = MutableLiveData<String?>(null)
    val uid_answer: LiveData<String?>
        get() = _uid_answer

    private val _powerstatus_answer = MutableLiveData<String?>(null)
    val powerstatus_answer: LiveData<String?>
        get() = _powerstatus_answer

    private val _versionfw_answer = MutableLiveData<String?>(null)
    val versionfw_answer: LiveData<String?>
        get() = _versionfw_answer

    private val _read_banks_status_answer = MutableLiveData<BanksStatus?>(null)
    val read_banks_status_answer: LiveData<BanksStatus?>
        get() = _read_banks_status_answer

    private val _certificate_answer = MutableLiveData<String?>(null)
    val certificate_answer: LiveData<String?>
        get() = _certificate_answer

    private val _customcommandlist_answer = MutableLiveData<List<CustomCommand>?>(null)
    val customcommandlist_answer: LiveData<List<CustomCommand>?>
        get() = _customcommandlist_answer

    private var mCurrentConfig = mutableListOf<SensorViewData>()
    private val _readSensorsConfig_answer = MutableLiveData(mCurrentConfig.toList())
    val readSensorsConfig_answer:LiveData<List<SensorViewData>>
        get() = _readSensorsConfig_answer


    private val _fw_uri_path = MutableLiveData<String?>(null)
    val fw_uri_path: LiveData<String?>
        get() = _fw_uri_path

    companion object {
        const val TAG = "ExtConfViewModel"
    }

    private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

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

                //Try to retrieve the Error
                answer = FeatureExtConfiguration.resultCommandError(responseObj)
                if (answer != null) {
                    _error_answer.postValue(answer)
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

                //Try to retrieve the Fw Version
                answer = FeatureExtConfiguration.resultCommandVersionFw(responseObj)
                if (answer != null) {
                    _versionfw_answer.postValue(answer)
                }

                //Try to retrieve the Flash Banks Status
                val bankStatus = FeatureExtConfiguration.resultCommandReadBanksStatus(responseObj)
                if(bankStatus != null) {
                    _read_banks_status_answer.postValue(bankStatus)
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

                //Try to retrieve the Sensors Configuration from the Node
                val listSensor = FeatureExtConfiguration.resultReadSensorCommand(responseObj)
                if (listSensor!=null) {
                    val newConfiguration: MutableList<SensorViewData> = mutableListOf<SensorViewData>()
                    newConfiguration.addAll(listSensor.map { it.toSensorViewData() })
                    if (mCurrentConfig != newConfiguration) {
                        Log.e("TAG", "DeviceConfig changed!")
                        if (mCurrentConfig.size > 0) {
                            val collapsedMap = mutableMapOf<String, Boolean>()
                            for (s in mCurrentConfig) {
                                collapsedMap[s.sensor.name] = s.isCollapsed
                            }
                            for (s in newConfiguration) {
                                s.isCollapsed = collapsedMap[s.sensor.name]!!
                            }
                        }
                        mCurrentConfig = newConfiguration
                        _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
                    }
                }
            }
        }
    }

    fun getMLCSensorID(subSensorDescriptorList:List<SubSensorDescriptor>):Int {
        for (ssd in subSensorDescriptorList){
            if (ssd.sensorType == SensorType.MLC){
                return ssd.id
            }
        }
        return -1
    }

    fun Sensor.toSensorViewData(): SensorViewData {
        val mlcId = getMLCSensorID(this.sensorDescriptor.subSensorDescriptors)
        if (mlcId != -1) {
            return SensorViewData(
                    sensor = this,
                    isCollapsed = true,
                    hasLockedParams = this.sensorStatus.subSensorStatusList[mlcId].isActive && this.sensorStatus.subSensorStatusList[mlcId].ucfLoaded
            )
        }
        return SensorViewData(
                sensor = this,
                isCollapsed = true,
                hasLockedParams = false
        )
    }

    private fun updateSensorConfig(newSensor: SensorViewData) {
        mCurrentConfig[newSensor.sensor.id] = newSensor
        _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
    }

    fun collapseSensor(selected: SensorViewData) {
        Log.d("ConfigViewMode","select to: ${selected.sensor.name}")
        val newSensor = selected.copy(isCollapsed = true)
        updateSensorConfig(newSensor)
    }

    fun expandSensor(selected: SensorViewData) {
        Log.d("ConfigViewMode","select to: ${selected.sensor.name}")
        val newSensor = selected.copy(isCollapsed = false)
        updateSensorConfig(newSensor)
    }

    private fun List<SensorViewData>.getSensorWithId(id:Int):SensorViewData? = find { it.sensor.id == id }

    private fun getSubSensorStatus(sensorId:Int,subSensorId:Int): SubSensorStatus?{
        return mCurrentConfig.getSensorWithId(sensorId)
                ?.sensor?.getSubSensorStatusForId(subSensorId)
    }

    fun changeODRValue(sensor: Sensor, subSensor: SubSensorDescriptor, newOdrValue: Double) {
        Log.d("ConfigVM","onSubSensorODRChange ${sensor.id} -> ${subSensor.id} -> $newOdrValue")
        val paramList = listOf(ODRParam(subSensor.id,newOdrValue))
        val ssODRCmd = HSDSetSensorCmd( sensor.id, paramList)
        mFeature?.writeCommandSetArgumentJSON(FeatureExtConfiguration.SET_SENSORS,ssODRCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.odr = newOdrValue
        _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
    }

    fun changeFullScale(sensor: Sensor, subSensor: SubSensorDescriptor, newFSValue: Double) {
        Log.d("ConfigVM","onSubSensorFSChange ${sensor.id} -> ${subSensor.id} -> $newFSValue")
        val paramList = listOf(FSParam(subSensor.id,newFSValue))
        val ssFSCmd = HSDSetSensorCmd(sensor.id, paramList)
        mFeature?.writeCommandSetArgumentJSON(FeatureExtConfiguration.SET_SENSORS,ssFSCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.fs = newFSValue
        _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
    }

    fun changeSampleForTimeStamp(sensor: Sensor, subSensor: SubSensorDescriptor, newSampleValue: Int) {
        Log.d("ConfigVM","onSubSensorSampleChange ${sensor.id} -> ${subSensor.id} -> $newSampleValue")
        val paramList = listOf(SamplePerTSParam(subSensor.id,newSampleValue))
        val ssSamplePerTSCmd = HSDSetSensorCmd(sensor.id, paramList)
        mFeature?.writeCommandSetArgumentJSON(FeatureExtConfiguration.SET_SENSORS,ssSamplePerTSCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.samplesPerTs = newSampleValue
        _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
    }

    fun changeEnableState(sensor: Sensor, subSensor: SubSensorDescriptor, newState: Boolean, paramsLocked: Boolean) {
        Log.d("ConfigVM","onSubSensorEnableChange ${sensor.id} -> ${subSensor.id} -> $newState")
        val paramList = listOf(IsActiveParam(subSensor.id,newState))
        val ssIsActiveCmd = HSDSetSensorCmd(sensor.id, paramList)
        mFeature?.writeCommandSetArgumentJSON(FeatureExtConfiguration.SET_SENSORS,ssIsActiveCmd)
        getSubSensorStatus(sensor.id,subSensor.id)?.isActive = newState
        if (subSensor.sensorType == SensorType.MLC) {

//            if(getSubSensorStatus(sensor.id,subSensor.id)?.isActive==true) {
//                getSubSensorStatus(sensor.id,subSensor.id)?.isActive=false;
//                if(this.context!=null) {
//                    Toast.makeText(this.context, "MLC could not be Activated in this section", Toast.LENGTH_SHORT).show()
//                }
//            }

            val newSensorStatus = mCurrentConfig[sensor.id].sensor.sensorStatus.copy(paramsLocked = paramsLocked)
            val newSensor = mCurrentConfig[sensor.id].sensor.copy(sensorStatus = newSensorStatus)
            val newSensorViewData = mCurrentConfig[sensor.id].copy(hasLockedParams = paramsLocked, sensor = newSensor)
            updateSensorConfig(newSensorViewData)
        }
        else {
            _readSensorsConfig_answer.postValue(mCurrentConfig.toList())
        }
    }

    fun changeMLCLockedParams(newState: Boolean){
        Log.d("ConfigVM","refreshDevice")
        var mlcSId = -1
        var mlcSsId = -1
        for(sc in mCurrentConfig){
            for(sd in sc.sensor.sensorDescriptor.subSensorDescriptors){
                if (sd.sensorType == SensorType.MLC){
                    mlcSId = sc.sensor.id
                    mlcSsId = sd.id
                }
            }
        }
        val newSubSensorStatusList: MutableList<SubSensorStatus> = mutableListOf()
        for (i in mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList.indices)
            newSubSensorStatusList += if (i == mlcSsId)
                mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList[i].copy(ucfLoaded = newState)
            else
                mCurrentConfig[mlcSId].sensor.sensorStatus.subSensorStatusList[i].copy()
        val newSensorStatus = mCurrentConfig[mlcSId].sensor.sensorStatus.copy(subSensorStatusList = newSubSensorStatusList)
        val newSensor = mCurrentConfig[mlcSId].sensor.copy(sensorStatus = newSensorStatus)
        val newSensorViewData = mCurrentConfig[mlcSId].copy(sensor = newSensor)
        updateSensorConfig(newSensorViewData)
        return
    }


    fun timeToString(date: Date): String {
        val timeStr = timeFormat.format(date)
        return String.format(Locale.getDefault(), "%s", timeStr)
    }

    /**
     * Convert the range Sunday=1 ... saturday=7 to monday =1 ... sunday=7
     * @param dayOfTheWeek dat og the week, 1 = sunday, 2 = monday ... 7= saturday
     * @return change the week where moday is the first day of the week
     */
    fun toMondayFirst(dayOfTheWeek: Int): Int {
        return if (dayOfTheWeek == Calendar.SUNDAY) 7 else dayOfTheWeek - 1
    }

    /**
     * get the day of the week, in a specifc date
     * @param d date to query
     * @return 1 = monday .. 7 = sunday
     */
    fun getDayOfTheWeek(d: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = d
        return toMondayFirst(cal[Calendar.DAY_OF_WEEK])
    }

    fun dateToString(date: Date): String {
        val dateStr = dateFormat.format(date)
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

    fun banksSwap() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.BANKS_SWAP)
    }

    fun readBanksStatus() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.BANKS_STATUS)
    }

    fun readCustomCommands() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)
    }

    fun readSensors() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_SENSORS)
    }

    // API for writing the command with argument
    fun setPIN(newPIN: Int) {
        mFeature?.writeCommandSetArgumentNumber(FeatureExtConfiguration.CHANGE_PIN,newPIN);
    }

    fun setName(name: String) {
        mFeature?.writeCommandSetArgumentString(FeatureExtConfiguration.SET_NAME,name.padEnd(7));
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

    fun setSensorsDone() {
        _readSensorsConfig_answer.postValue(null)
        mCurrentConfig.clear()
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

    fun customCommandReceived() {
        _customcommandlist_answer.postValue(null)
    }

    fun infoReceived() {
        _info_answer.postValue(null)
    }

    fun errorReceived() {
        _error_answer.postValue(null)
    }

    fun helpReceived() {
        _help_answer.postValue(null)
    }
    fun uidReceived() {
        _uid_answer.postValue(null)
    }
    fun powerStatusReceived() {
        _powerstatus_answer.postValue(null)
    }

    fun versionFwReceived() {
        _versionfw_answer.postValue(null)
    }

    fun readBanksStatusReceived() {
        _read_banks_status_answer.postValue(null)
    }

    fun certificateReceived() {
        _certificate_answer.postValue(null)
    }

    fun fwUriPath(uri: String) {
        _fw_uri_path.postValue(uri)
    }

    fun fwUriPathReceived(){
        _fw_uri_path.postValue(null)
    }

    fun setRetrievedCertificate(certificate: String?) {
         requestedCertificate = certificate
    }

    fun getRetrievedCertiticate(): String ? {
        return requestedCertificate
    }

    fun setMcuId(mcuId: String?) {
        mcuid = mcuId
    }
    fun getMcuId(): String ? {
        return mcuid
    }
}