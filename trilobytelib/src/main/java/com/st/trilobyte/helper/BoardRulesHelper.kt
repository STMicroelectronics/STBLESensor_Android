package com.st.trilobyte.helper

import android.app.Activity
import android.content.DialogInterface
import com.st.trilobyte.R
import com.st.trilobyte.models.*
import com.st.trilobyte.models.Function


private fun validateSensorsConfigurations(flows: List<Flow>): Pair<Boolean, Sensor?> {
    val expandedFlows = mutableListOf<Flow>()
    flows.forEach { FlowHelper.extractAllFlowsFromCompositeFlow(it, expandedFlows) }

    val sensorMap = mutableMapOf<String, MutableList<Sensor>>()
    for (flow in expandedFlows) {
        for (sensor in flow.sensors) {
            if (!sensorMap.containsKey(sensor.id)) {
                sensorMap[sensor.id] = mutableListOf()
            }
            sensorMap[sensor.id]!!.add(sensor)
        }
    }

    for (sensorId in sensorMap.keys) {
        val sensorList = sensorMap[sensorId]
        val refConfiguration = sensorList!![0].configuration

        for (sensor in sensorList) {
            if (sensor.configuration != refConfiguration) {
                return Pair(false, sensor)
            }
        }
    }

    return Pair(true, null)
}

/**
 * @return true if the number of flows composed by (only) sensors is <= 1
 */
private fun validateOnlySensorFlowCount(flows: List<Flow>): Boolean {
    val expandedFlows = mutableListOf<Flow>()
    flows.forEach { FlowHelper.extractAllFlowsFromCompositeFlow(it, expandedFlows) }
    return expandedFlows.filter { flow -> flow.sensors.isNotEmpty() && flow.functions.isEmpty() }.toList().size <= 1
}

private fun validateOdrForBleOutput(flows: List<Flow>): Pair<Boolean, Sensor?> {
    val expandedFlows = mutableListOf<Flow>()
    flows.forEach { FlowHelper.extractAllFlowsFromCompositeFlow(it, expandedFlows) }

    val bleFlows = expandedFlows.asSequence().filter { it.hasBtStreamAsOutput() }.toList()
    for (flow in bleFlows) {

        if (FlowHelper.containsFunction(flow, FFT_FUNCTION_ID)) {
            continue
        }

        flow.sensors.forEach { s ->
            if (s.bleMaxOdr != null) {
                s.configuration.odr?.let {
                    if (it > s.bleMaxOdr!!) {
                        return Pair(false, s)
                    }
                }
            }
        }
    }

    return Pair(true, null)
}

private fun validateFunctionCount(flows: List<Flow>, functionId: String): Boolean {

    val expandedFlows = mutableListOf<Flow>()
    flows.forEach { FlowHelper.extractAllFlowsFromCompositeFlow(it, expandedFlows) }

    var count = 0
    expandedFlows.forEach { flow ->
        count += flow.functions.filter { function -> function.id == functionId }.size
    }

    return count <= 1
}

internal fun validateFlows(activity: Activity, flows: List<Flow>, functions: List<Function>,
                  clickListener: DialogInterface.OnClickListener? = null): Boolean {

    if (!validateOnlySensorFlowCount(flows)) {
        DialogHelper.showDialog(activity, activity.getString(R.string.error_cannot_upload_flow_multiple_sensors_only_flows), clickListener)
        return false
    }

    val (hasValidConfiguration, sensor) = validateSensorsConfigurations(flows)
    if (!hasValidConfiguration) {
        DialogHelper.showDialog(activity, String.format(activity.getString(R.string.error_cannot_upload_flow_sensor_configuration_misleading),
                sensor?.description), clickListener)
        return false
    }

    val funIds = listOf(FFT_FUNCTION_ID,
            SENSOR_FUSION_QUATERNION_FUNCTION_ID, SENSOR_FUSION_EULER_FUNCTION_ID,
            SENSOR_FUSION_PEDOMETER_FUNCTION_ID, HARD_IRON_COMP_FUNCTION_ID)
    for (funId in funIds) {
        if (!validateFunctionCount(flows, funId)) {
            val description = FunctionHelper.findFunctionById(functions, funId)!!.description
            DialogHelper.showDialog(activity, String.format(activity.getString(R.string.error_cannot_upload_flow_function_exceed_count), description), clickListener)
            return false
        }
    }

    val (hasValidOdrConfiguration, _sensor) = validateOdrForBleOutput(flows)
    if (!hasValidOdrConfiguration) {
        DialogHelper.showDialog(activity, String.format(activity.getString(R.string.error_cannot_upload_flow_sensor_odr_exceeded),
                _sensor?.description), clickListener)
        return false
    }

    return true
}