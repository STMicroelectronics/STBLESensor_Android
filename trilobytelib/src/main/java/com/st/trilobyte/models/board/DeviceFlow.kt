package com.st.trilobyte.models.board

import com.google.gson.GsonBuilder
import com.st.trilobyte.helper.FlowHelper
import com.st.trilobyte.models.Flow
import com.st.trilobyte.models.Sensor
import com.st.trilobyte.models.board.serializer.BoardFlowSerializer
import com.st.trilobyte.models.board.serializer.BoardSensorSerializer
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class DeviceFlow private constructor(
        var version: Int = 0,
        var sensors: MutableList<Sensor> = ArrayList(),
        var flows: MutableList<Flow> = ArrayList()) : Serializable {

    companion object {

        fun getBoardStream(flows: List<Flow>): String {
            val deviceFlows = mutableListOf<DeviceFlow>()
            flows.forEach { deviceFlows.add(transform(it)) }

            return GsonBuilder()
                    .registerTypeAdapter(Flow::class.java, BoardFlowSerializer)
                    .registerTypeAdapter(Sensor::class.java, BoardSensorSerializer)
                    .create()
                    .toJson(deviceFlows)
        }

        fun transform(flow: Flow): DeviceFlow {

            val out = DeviceFlow()
            out.version = flow.version
            FlowHelper.extractAllSensorsFromCompositeFlow(flow, out.sensors)
            FlowHelper.extractAllFlowsFromCompositeFlow(flow, out.flows)

            out.apply {
                mergeSensorConfigs()
                orderSensors()
                orderFlows()
            }

            return out
        }
    }

    private fun mergeSensorConfigs() {

        val mergedMap = HashMap<String, Sensor>()
        for (sensor in sensors) {
            if (!mergedMap.containsKey(sensor.id)) {
                mergedMap[sensor.id] = sensor
            }
        }

        sensors = ArrayList(mergedMap.values)
    }

    private fun orderSensors() {
        sensors.sortWith(Comparator { (id), (id2) -> id.compareTo(id2) })
    }

    private fun orderFlows() {
        flows.sortWith(Comparator { flow1, flow2 -> FlowHelper.getCompositeInputFlowCount(flow2) - FlowHelper.getCompositeInputFlowCount(flow1) })
    }
}
