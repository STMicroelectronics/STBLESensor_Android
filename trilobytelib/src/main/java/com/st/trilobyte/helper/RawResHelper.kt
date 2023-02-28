package com.st.trilobyte.helper

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.models.*
import com.st.trilobyte.models.Function
import com.st.trilobyte.models.deserializer.PropertyDeserializer
import java.util.*

private fun readRawFile(context: Context, fileId: Int): String {
    val inputStream = context.resources.openRawResource(fileId)
    return Scanner(inputStream).useDelimiter("\\A").next()
}

internal fun getSensorList(context: Context, board: Node.Type): List<Sensor>? {
    val json = readRawFile(context, R.raw.sensors)
    val sensors: List<Sensor>? = Gson().fromJson<List<Sensor>>(json, object : TypeToken<List<Sensor>>() {}.type)
    return sensors?.filter {  it.board_compatibility.contains(board.name)}
}

internal fun getSensorFilterList(context: Context, board: Node.Type): List<SensorFilter>? {
    val json = readRawFile(context, R.raw.filters)
    val filters: List<SensorFilter>? = Gson().fromJson<List<SensorFilter>>(json, object : TypeToken<List<SensorFilter>>() {}.type)
    return filters?.filter { it.board_compatibility.contains(board.name) }
}

internal fun getFunctionList(context: Context, board: Node.Type): List<Function>? {
    val json = readRawFile(context, R.raw.functions)
    val propertyListType = object : TypeToken<List<Property<*>>>() {}.type
    val gson = GsonBuilder().registerTypeAdapter(propertyListType, PropertyDeserializer).create()
    val functions: List<Function>? = gson.fromJson<List<Function>>(json, object : TypeToken<List<Function>>() {}.type)
    return  functions?.filter { it.board_compatibility.contains(board.name) }
}

internal fun getOutputList(context: Context, board: Node.Type): List<Output>? {
    val json = readRawFile(context, R.raw.output)
    val propertyListType = object : TypeToken<List<Property<*>>>() {}.type
    val gson = GsonBuilder().registerTypeAdapter(propertyListType, PropertyDeserializer).create()
    val outputs: List<Output>? = gson.fromJson<List<Output>>(json, object : TypeToken<List<Output>>() {}.type)
    return outputs?.filter { it.board_compatibility.contains(board.name)  }
}

internal fun getExpFlowList(context: Context, board: Node.Type): List<Flow>? {
    val expFlowsJson = readRawFile(context, R.raw.exp_flows)
    val propertyListType = object : TypeToken<List<Property<*>>>() {}.type
    val gson = GsonBuilder().registerTypeAdapter(propertyListType, PropertyDeserializer).create()
    val flows: List<Flow>? = gson.fromJson<List<Flow>>(expFlowsJson, object : TypeToken<List<Flow>>() {}.type)
    return flows?.filter { it.board_compatibility.contains(board.name) }
}

internal fun getCounterFlowList(context: Context, board: Node.Type): List<Flow>? {
    val expFlowsJson = readRawFile(context, R.raw.counter_flows)
    val propertyListType = object : TypeToken<List<Property<*>>>() {}.type
    val gson = GsonBuilder().registerTypeAdapter(propertyListType, PropertyDeserializer).create()
    val flows: List<Flow>? = gson.fromJson<List<Flow>>(expFlowsJson, object : TypeToken<List<Flow>>() {}.type)
    return flows?.filter{it.board_compatibility.contains(board.name)}
}