package com.st.trilobyte.helper

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.Node
import com.st.trilobyte.models.Flow
import com.st.trilobyte.models.Property
import com.st.trilobyte.models.deserializer.PropertyDeserializer
import com.st.trilobyte.models.serializer.PropertySerializer
import java.io.*
import java.util.*


private const val TRILOBYTE_DATA_FOLDER = "SensorTile.box"

private const val EXAMPLES_FLOW_FOLDER = "examples"

private const val FLOW_FILE_EXTENSION = ".json"

fun saveFlow(flow: Flow, listener: SaveListener) {

    if (!isExternalStorageWritable()) {
        listener.onError()
        return
    }

    val dataDir = getStorageDir()
    val dataFile = File(getFileName(dataDir, flow))

    val gsonInstance = GsonBuilder()
            .registerTypeAdapter(Property::class.java, PropertySerializer)
            .create()

    if (!dataDir.exists()) {
        listener.onError()
        return
    }

    try {
        PrintWriter(dataFile).use { writer -> writer.println(gsonInstance.toJson(flow)) }
    } catch (e: Exception) {
        e.printStackTrace()
        listener.onError()
    }

    listener.onSuccess()
}

fun hasFlowConflictingName(flowName: String): Boolean {

    val dataDir = getStorageDir()
    if (dataDir.exists() && dataDir.isDirectory) {
        return dataDir.list()?.contains(flowName + FLOW_FILE_EXTENSION) ?: return false

    }
    return false
}

fun loadExampleFlows(context: Context, board: Node.Type): List<Flow> {

    val flows = mutableListOf<Flow>()

    val assetManager = context.assets
    val examples: Array<String>?
    try {
        examples = assetManager.list(EXAMPLES_FLOW_FOLDER)
    } catch (e: IOException) {
        return flows
    }

    examples?.let {
        for (example in examples) {
            try {
                val stream = assetManager.open("$EXAMPLES_FLOW_FOLDER/$example")
                val flow = parseFlowFile(stream)
                if (flow != null) {
                    if(flow.board_compatibility.contains(board.name)) {
                        flows.add(flow)
                    } else if ((flow.board_compatibility.size==0) && (board==Node.Type.SENSOR_TILE_BOX)) {
                        flow.board_compatibility.add(board.name)
                        flows.add(flow)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    return flows
}

fun filterFlowsByCategory(flows: List<Flow>, category: String?): List<Flow> {

    if (category.isNullOrEmpty())
        return flows

    return flows.filter { flow -> flow.category == category }
}

fun loadSavedFlows(board: Node.Type): List<Flow> {

    val flows = mutableListOf<Flow>()

    val dataDir = getStorageDir()
    if (dataDir.exists() && dataDir.isDirectory) {
        val files = dataDir.listFiles() ?: return flows
        for (file in files) {
            try {
                val flow = parseFlowFile(FileInputStream(file))
                if (flow != null && flow.version == Flow.FLOW_VERSION && flow.board_compatibility.contains(board.name)) {
                    flow.file = file
                    flows.add(flow)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    return flows
}

private fun getStorageDir(): File {
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), TRILOBYTE_DATA_FOLDER)
    if (!file.exists()) {
        file.mkdirs()
    }
    return file
}

private fun isExternalStorageWritable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state
}

private fun getFileName(dataDir: File, flow: Flow): String {
    return dataDir.absolutePath + "/" + flow.description + FLOW_FILE_EXTENSION
}

private fun parseFlowFile(stream: InputStream): Flow? {
    val propertyListType = object : TypeToken<List<Property<*>>>() {}.type
    val gson = GsonBuilder()
            .registerTypeAdapter(propertyListType, PropertyDeserializer)
            .create()

    val text = Scanner(stream).useDelimiter("\\A").next()
    return gson.fromJson<Flow>(text, object : TypeToken<Flow>() {}.type)
}

// interface
interface SaveListener {
    fun onSuccess()

    fun onError()
}