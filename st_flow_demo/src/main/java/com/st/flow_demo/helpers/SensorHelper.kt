package com.st.flow_demo.helpers

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.st.blue_sdk.models.Boards
import com.st.flow_demo.R
import com.st.flow_demo.models.MlcFsmDecisionTreeOutput
import com.st.flow_demo.models.MlcFsmLabelEntry
import com.st.blue_sdk.board_catalog.models.PowerMode
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.blue_sdk.board_catalog.models.SensorConfiguration
import com.st.blue_sdk.models.JsonMLCFormat
import com.st.flow_demo.models.SensorFilter
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader

fun getSensorIconResourceByName(name: String) = when (name) {
    "ic_accelerometer" -> com.st.ui.R.drawable.sensor_type_accelerometer
    "ic_humidity" -> com.st.ui.R.drawable.sensor_type_humidity
    "ic_inemo" -> R.drawable.ic_inemo
    "ic_magnetometer" -> com.st.ui.R.drawable.sensor_type_compass
    "ic_microphone" -> com.st.ui.R.drawable.sensor_type_microphone
    "ic_nfc_sensor" -> R.drawable.ic_nfc_sensor
    "ic_pressure" -> com.st.ui.R.drawable.sensor_type_pressure
    "ic_qvar_sensor" -> R.drawable.ic_qvar_sensor
    "ic_rtc" -> R.drawable.ic_rtc
    "ic_termometer" -> com.st.ui.R.drawable.sensor_type_temperature
    "ic_infrared" -> R.drawable.ic_infrared
    else -> R.drawable.ic_sensor
}


fun hasFilter(context: Context, sensorId: String, board: Boards.Model): Boolean {
    val filters: List<SensorFilter>? = getSensorFilterList(context, board)
    if (filters != null) {
        for (sensorFilter in filters) {
            if (sensorFilter.sensorId.compareTo(sensorId) == 0) {
                return true
            }
        }
    }
    return false
}

fun getSensorPropertiesDescription(context: Context, sensor: Sensor, board: Boards.Model): String {
    val properties: MutableList<String> = ArrayList()
    if (!sensor.powerModes.isNullOrEmpty()) {
        val pm: PowerMode = sensor.powerModes!![0]
        if (sensor.powerModes!!.size > 1) {
            properties.add(context.getString(R.string.power_mode))
        }
        if (pm.odrs.isNotEmpty()) {
            properties.add(context.getString(R.string.odr))
        }
    }
    if (hasFilter(context, sensor.id, board)) {
        properties.add(context.getString(R.string.filter))
    }
    if (sensor.fullScales != null) {
        properties.add(context.getString(R.string.full_scale))
    }
    if (sensor.samplingFrequencies != null) {
        properties.add(context.getString(R.string.sampling_frequencies))
    }
    return formatSensorProperties(properties)
}

private fun formatSensorProperties(properties: List<String>): String {
    val sb = StringBuilder()
    for (i in properties.indices) {
        sb.append(properties[i])
        if (i != properties.size - 1) {
            sb.append(", ")
        }
    }
    return sb.toString()
}

fun findSensorById(sensors: List<Sensor>, id: String?): Sensor? {
    for (sensor in sensors) {
        if (sensor.id == id) {
            return sensor
        }
    }
    return null
}

fun bothMLCAndFSMArePresent(sensorsSelected: List<Sensor>): Boolean {
    // Sensors that cannot be enabled together
    var mlcSelected = false
    var fsmSelected = false

    if (sensorsSelected.firstOrNull { sensor -> sensor.id == "S12" } != null) {
        mlcSelected = true
    }

    if (sensorsSelected.firstOrNull { sensor -> sensor.id == "S13" } != null) {
        fsmSelected = true
    }

    return mlcSelected && fsmSelected
}

fun multipleAccelerometerAreSelected(sensorsSelected: List<Sensor>): Boolean {
    val nSelected = sensorsSelected.filter { sensor -> sensor.id == "S5" }.size
    return nSelected > 1
}

fun getSensorFiltersBySensorId(
    context: Context,
    sensorId: String,
    board: Boards.Model
): SensorFilter? {
    val filters: List<SensorFilter>? = getSensorFilterList(context, board)
    if (filters != null) {
        for (sensorFilter in filters) {
            if (sensorFilter.sensorId == sensorId) {
                return sensorFilter
            }
        }
    }
    return null
}

fun extractMLCLabels(mlcLabels: String): List<MlcFsmDecisionTreeOutput> {
    val outputList = mutableListOf<MlcFsmDecisionTreeOutput>()

    if (mlcLabels.contains("<MLC") && mlcLabels.contains("_SRC>")) {
        val myDataRow = mlcLabels.split(";".toRegex())

        //for (stringFullDecTree in myDataRow) {
        for (treeIndex in 0 until myDataRow.size) {

            val stringFullDecTree = myDataRow[treeIndex]

            //For last ';'
            if (stringFullDecTree.length > 1) {
                val mlcFsmLabels = mutableListOf<MlcFsmLabelEntry>()
                val separated = stringFullDecTree.split(",".toRegex())

                // DecTree Number
                val decTreeNumber = Character.getNumericValue(separated[0][4])
                // DecTree Name
                val decTreeName = separated[0].substring(10)
                // DecTree Outputs/Labels
                for (index in 1 until separated.size) {
                    val singleDecision = separated[index]
                    val separated2 = singleDecision.split("=".toRegex())
                    mlcFsmLabels.add(
                        MlcFsmLabelEntry(
                            value = separated2[0].toInt(), label = separated2[1].substring(
                                1,
                                separated2[1].length - 1
                            )
                        )
                    )
                }

                outputList.add(
                    MlcFsmDecisionTreeOutput(
                        enabled = true,
                        number = decTreeNumber,
                        name = decTreeName,
                        mlcFsmLabels = mlcFsmLabels.toList()
                    )
                )
            }
        }
    }

    return outputList.toList()
}

fun generateMLCLabelsString(mlcLabels: List<MlcFsmDecisionTreeOutput>): String {
    val outputString = StringBuilder()
    mlcLabels.forEach {
        if (it.enabled) {
            outputString.append("<MLC${it.number}_SRC>${it.name}")
            it.mlcFsmLabels.forEach { it2 ->

                outputString.append(",${it2.value}='${it2.label}'")
            }
            outputString.append(";")
        }
    }

    return outputString.toString()
}


fun extractFSMLabels(fsmLabels: String): List<MlcFsmDecisionTreeOutput> {
    val outputList = mutableListOf<MlcFsmDecisionTreeOutput>()

    if (fsmLabels.contains("<FSM_OUTS") && fsmLabels.contains(">")) {
        val myDataRow = fsmLabels.split(";".toRegex())

        //for (stringFullDecTree in myDataRow) {
        for (treeIndex in 0 until myDataRow.size) {

            val stringFullDecTree = myDataRow[treeIndex]

            //For last ';'
            if (stringFullDecTree.length > 1) {
                val mlcFsmLabels = mutableListOf<MlcFsmLabelEntry>()
                val separated = stringFullDecTree.split(",".toRegex())

                // DecTree Number
                var decTreeNumber: Int
                // DecTree Name
                var decTreeName: String


                // Program Number / Name
                if (separated[0][10] in '0'..'9') {
                    decTreeNumber =
                        Character.getNumericValue(separated[0][9]) * 10 + Character.getNumericValue(
                            separated[0][10]
                        )
                    decTreeName = separated[0].substring(12)
                } else {
                    decTreeNumber = Character.getNumericValue(separated[0][9])
                    decTreeName = separated[0].substring(11)
                }
                decTreeNumber--


                // DecTree Outputs/Labels
                for (index in 1 until separated.size) {
                    val singleDecision = separated[index]
                    val separated2 = singleDecision.split("=".toRegex())
                    mlcFsmLabels.add(
                        MlcFsmLabelEntry(
                            value = separated2[0].toInt(), label = separated2[1].substring(
                                1,
                                separated2[1].length - 1
                            )
                        )
                    )
                }

                outputList.add(
                    MlcFsmDecisionTreeOutput(
                        enabled = true,
                        number = decTreeNumber,
                        name = decTreeName,
                        mlcFsmLabels = mlcFsmLabels.toList()
                    )
                )
            }
        }
    }

    return outputList.toList()
}


fun generateFSMLabelsString(fsmLabels: List<MlcFsmDecisionTreeOutput>): String {
    val outputString = StringBuilder()
    fsmLabels.forEach {
        if (it.enabled) {
            outputString.append("<FSM_OUTS${it.number + 1}>${it.name}")
            it.mlcFsmLabels.forEach { it2 ->

                outputString.append(",${it2.value}='${it2.label}'")
            }
            outputString.append(";")
        }
    }

    return outputString.toString()
}

fun parseUcfFile(
    context: Context,
    uri: Uri,
    sensorConfiguration: SensorConfiguration,
    isMLC: Boolean,
    board: Boards.Model,
    sensorModel: String
): String? {
    var errorTest: String? = null
    var regConfig = ""
    var labels = ""
    var sensor_supported = false
    var stmc_page = false
    var mlc_enabled = false
    var fsm_enabled = false

    //Start Resetting the regConfig field
    sensorConfiguration.regConfig = ""

    val ucfFilename = context.contentResolver.query(uri, null, null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()

            cursor.getString(nameIndex)
        } ?: ""

    val fileExt = ucfFilename.split('.').last()

    if (fileExt == "ucf") {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        inputStream?.let { inStream ->
            val myReader = BufferedReader(InputStreamReader(inStream))
            var myDataRow: String?

            try {
                while (myReader.readLine().also { myDataRow = it } != null) {

                    // Check if it's the program for the right sensor
                    if (myDataRow!!.contains(sensorModel)) {
                        sensor_supported = true
                    }

                    if (sensor_supported) {
                        // MLC labels in ucf header
                        if (myDataRow!!.contains("<MLC") && myDataRow!!.contains("_SRC>")) {
                            myDataRow = myDataRow!!.substring(3)
                            labels += myDataRow
                            labels = "$labels;"
                        }
                        // FSM labels in ucf header
                        if (myDataRow!!.contains("<FSM_OUTS") && myDataRow!!.contains(">")) {
                            myDataRow = myDataRow!!.substring(3)
                            labels += myDataRow
                            labels = "$labels;"
                        }

                        // Valid ucf file row
                        if (myDataRow!!.contains("Ac")) {
                            val separated =
                                myDataRow!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            val regAddress = separated[1]
                            val regValue = separated[2]
                            regConfig += regAddress
                            regConfig += regValue
                            if (regAddress == "01" && regValue == "80") {
                                stmc_page = true
                            } else if (myDataRow!!.contains("01 00")) {
                                stmc_page = false
                            }
                            if (stmc_page) {
                                if (regAddress == "05") {
//                                var reg = regValue.toByte(16)
//                                reg = (reg.toInt() and 0x01).toByte()
//                                fsm_enabled = reg > 0
                                    var reg = regValue.toInt(16)
                                    reg = (reg and 0x01)
                                    fsm_enabled = reg != 0
                                }
                                if (regAddress == "05") {
//                                var reg = regValue.toByte(16)
//                                reg = (reg.toInt() and 0x10).toByte()
//                                mlc_enabled = reg > 0
                                    var reg = regValue.toInt(16)
                                    reg = (reg and 0x10)
                                    mlc_enabled = reg != 0
                                }
                            }
                        }
                    }
                }

                myReader.close()
            } catch (e: Exception) {
                sensor_supported = false
            }

            //Close the Input Stream
            inStream.close()
        }
    } else if (fileExt == "json") {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        inputStream?.let { inStream ->

            //Try before to decode like Json format
            val fileContent = inStream.readBytes().toString(Charsets.UTF_8)

            val jsonDec = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
            val jsonMLCFormat =
                try {
                    jsonDec.decodeFromString<JsonMLCFormat>(fileContent)
                } catch (e: Exception) {
                    Log.d("JsonMLCFormat", e.stackTraceToString())
                    null
                }

            if (jsonMLCFormat != null) {
                val mlcParsed =
                    jsonMLCFormat.toFlowSensorConfiguration(sensorName = sensorModel, isMLC = isMLC)
                if (mlcParsed != null) {
                    regConfig = mlcParsed.regConfig
                    labels = mlcParsed.labels
                    mlc_enabled = mlcParsed.mlcEnabled
                    fsm_enabled = mlcParsed.fsmEnabled
                    sensor_supported = true
                }
            }
            inStream.close()
        }
    }


    if (sensor_supported) {
//        val ucfFilename = context.contentResolver.query(uri, null, null, null, null)
//            ?.use { cursor ->
//                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                cursor.moveToFirst()
//
//                cursor.getString(nameIndex)
//            } ?: ""

        if (isMLC) {
            if (mlc_enabled) {
                sensorConfiguration.regConfig = regConfig
                sensorConfiguration.mlcLabels = labels
                sensorConfiguration.ucfFilename = ucfFilename
                Log.i("FSMMLC", "MLC=$labels")
            } else {
                errorTest = context.getString(R.string.ucf_file_mlc_disabled)
                sensorConfiguration.mlcLabels = ""
            }
        } else {
            if (fsm_enabled) {
                sensorConfiguration.regConfig = regConfig
                sensorConfiguration.fsmLabels = labels
                sensorConfiguration.ucfFilename = ucfFilename
                Log.i("FSMMLC", "FMS=$labels")
            } else {
                errorTest = context.getString(R.string.ucf_file_fsm_disabled)
                sensorConfiguration.fsmLabels = ""
            }
        }
    } else {
        errorTest = "The selected ucf file is not compatible with the $sensorModel device"
    }
    return errorTest
}