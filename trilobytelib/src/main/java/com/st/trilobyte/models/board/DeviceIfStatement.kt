package com.st.trilobyte.models.board

import com.google.gson.GsonBuilder
import com.st.trilobyte.models.Flow
import com.st.trilobyte.models.Sensor
import com.st.trilobyte.models.board.serializer.BoardFlowSerializer
import com.st.trilobyte.models.board.serializer.BoardSensorSerializer
import java.io.Serializable

data class DeviceIfStatement private constructor(val expression: DeviceFlow, val statements: List<DeviceFlow>) : Serializable {

    companion object {

        fun getBoardStream(exp: Flow, stats: List<Flow>): String {

            val expression = DeviceFlow.transform(exp)

            val statements = mutableListOf<DeviceFlow>()
            stats.forEach { statements.add(DeviceFlow.transform(it)) }

            return GsonBuilder()
                    .registerTypeAdapter(Flow::class.java, BoardFlowSerializer)
                    .registerTypeAdapter(Sensor::class.java, BoardSensorSerializer)
                    .create()
                    .toJson(DeviceIfStatement(expression, statements))
        }
    }
}