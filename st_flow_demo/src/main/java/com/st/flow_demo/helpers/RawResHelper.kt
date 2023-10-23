package com.st.flow_demo.helpers

import android.content.Context
import android.util.Log
import com.st.blue_sdk.models.Boards
import com.st.flow_demo.R
import com.st.flow_demo.models.Flow
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.flow_demo.models.Function
import com.st.flow_demo.models.Output
import com.st.flow_demo.models.SensorFilter
import kotlinx.serialization.json.Json
import java.util.Scanner

private fun readRawFile(context: Context, fileId: Int): String {
    val inputStream = context.resources.openRawResource(fileId)
    return Scanner(inputStream).useDelimiter("\\A").next()
}

internal fun getSensorList(context: Context, board: Boards.Model): List<Sensor> {
    val text = readRawFile(context, R.raw.sensors)

    val sensors: ArrayList<Sensor> = arrayListOf()

    try {
        sensors.addAll(Json.decodeFromString<List<Sensor>>(text))
    } catch (e: Exception) {
        Log.d("getSensorList", e.stackTraceToString())
    }
    return sensors.filter { it.board_compatibility.contains(board.name) }
}

fun getSensorFilterList(context: Context, board: Boards.Model): List<SensorFilter>? {
    val text = readRawFile(context, R.raw.filters)

    val filters =
        try {
            Json.decodeFromString<List<SensorFilter>>(text)
        } catch (e: Exception) {
            Log.d("getSensorFilterList", e.stackTraceToString())
            null
        }
    return filters?.filter { it.board_compatibility.contains(board.name) }
}

internal fun getFunctionList(context: Context, board: Boards.Model): List<Function> {
    val text = readRawFile(context, R.raw.functions)

    val functions: ArrayList<Function> = arrayListOf()

    try {
        functions.addAll(Json.decodeFromString<List<Function>>(text))
    } catch (e: Exception) {
        Log.d("getFunctionList", e.stackTraceToString())
    }

    return functions.filter { it.board_compatibility.contains(board.name) }
}

internal fun getOutputList(context: Context, board: Boards.Model): List<Output> {
    val text = readRawFile(context, R.raw.output)

    val outputs: ArrayList<Output> = arrayListOf()

    try {
        outputs.addAll(Json.decodeFromString<List<Output>>(text))
    } catch (e: Exception) {
        Log.d("getOutputList", e.stackTraceToString())
    }
    return outputs.filter { it.board_compatibility?.contains(board.name) ?: false }
}

internal fun getExpFlowList(context: Context, board: Boards.Model): List<Flow> {
    val text = readRawFile(context, R.raw.exp_flows)

    val expFlow: ArrayList<Flow> = arrayListOf()
    try {
        expFlow.addAll(Json.decodeFromString<List<Flow>>(text))
    } catch (e: Exception) {
        Log.d("getExpFlowList", e.stackTraceToString())
    }
    return expFlow.filter { it.board_compatibility.contains(board.name) }
}

internal fun getCounterFlowList(context: Context, board: Boards.Model): List<Flow> {
    val text = readRawFile(context, R.raw.counter_flows)

    val countFlow: ArrayList<Flow> = arrayListOf()

    try {
        countFlow.addAll(Json.decodeFromString<List<Flow>>(text))
    } catch (e: Exception) {
        Log.d("getCounterFlowList", e.stackTraceToString())
    }
    return countFlow.filter { it.board_compatibility.contains(board.name) }
}