package com.st.flow_demo.helpers

import com.st.flow_demo.uploader.DeviceFlow
import com.st.flow_demo.uploader.DeviceIfStatementFlow
import com.st.flow_demo.models.Flow
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.flow_demo.models.Function
import com.st.flow_demo.models.Output
import com.st.blue_sdk.board_catalog.models.SensorConfiguration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun isCompositeFlow(flow: Flow): Boolean {
    return flow.flows.isNotEmpty()
}

fun getFlowFunctionInputs(currentFlow: Flow, inputs: MutableList<String>) {
    for (parent in currentFlow.flows) {
        if (parent.functions.isEmpty()) {
            getFlowFunctionInputs(parent, inputs)
        } else {
            val lastFunction = parent.functions.last()
            inputs.add(lastFunction.id)
        }
    }
}

fun getFlowSensorInputs(currentFlow: Flow, inputs: MutableList<String>) {
    for (sensor in currentFlow.sensors) {
        if(!inputs.contains(sensor.id)) {
            inputs.add(sensor.id)
        }
    }

    for (parent in currentFlow.flows) {
        if (parent.functions.isEmpty()) {
            getFlowSensorInputs(parent, inputs)
        }
    }
}

fun gzip(content: String): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
    return bos.toByteArray()
}

fun ungzip(content: ByteArray): String =
    GZIPInputStream(content.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }


fun serializeForSendingFlows(flows: List<DeviceFlow>): String {
    val format = Json

    return format.encodeToString(flows)
}

fun serializeForSendingIfFlows(flow : DeviceIfStatementFlow): String {
    val format = Json

    return format.encodeToString(flow)
}


fun serializePrettyPintFlow(flow: Flow): String {
    val format = Json { prettyPrint = true }

    return format.encodeToString(flow)
}


fun extractAllSensorsFromCompositeFlow(flow: Flow, sensors: MutableList<Sensor>) {
    sensors.addAll(flow.sensors)
    for (parent in flow.flows) {
        extractAllSensorsFromCompositeFlow(parent, sensors)
    }
}

//fun extractAllFlowsFromCompositeFlow(flow: Flow, flows: MutableList<Flow>) {
//    flows.add(flow)
//    for (parent in flow.flows) {
//        extractAllFlowsFromCompositeFlow(parent, flows)
//    }
//}


fun searchSensorConfigurationInFlow(flow: Flow, sensorId: String?): SensorConfiguration? {
    val sensors: MutableList<Sensor> = ArrayList()
    extractAllSensorsFromCompositeFlow(flow, sensors)
    for (i in sensors.indices.reversed()) {
        val sensor: Sensor = sensors[i]
        if (sensor.id == sensorId) {
            return sensor.configuration
        }
    }
    return null
}

fun getCompositeInputFlowCount(flow: Flow): Int {
    return flow.flows.size
}

fun filterByFunction(flows: List<Flow>, function: Function): List<Flow>? {
    val filtered: MutableList<Flow> = ArrayList()
    for (flow in flows) {
        val functions: List<Function> = flow.functions
        val sensors: List<Sensor> = flow.sensors
        if (functions.isNotEmpty()) {
            if (function.inputs.contains(functions.last().id)) {
                filtered.add(flow)
                continue
            }
        }
        if (sensors.isNotEmpty()) {
            for (sensor in sensors) {
                if (function.inputs.contains(sensor.id)) {
                    filtered.add(flow)
                    continue
                }
            }
        }
    }
    return filtered
}

fun containsFunction(flow: Flow, funId: String?): Boolean {
    for (function in flow.functions) {
        if (function.id == funId) {
            return true
        }
    }
    return false
}

fun canBeUsedAsExp(flow: Flow): Boolean {
    return if (flow.outputs.size == 1) {
        flow.outputs[0].id == Output.OUTPUT_EXP_ID
    } else false
}

fun isAlsoExp(flow: Flow): Boolean {
    return flow.functions.any{ it.outputs.any{ out -> out == Output.OUTPUT_EXP_ID}}
}

fun findFlowById(flows: List<Flow>, id: String?): Flow? {
    for (flow in flows) {
        if (flow.id == id) {
            return flow
        }
    }
    return null
}