package com.st.flow_demo.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import com.st.blue_sdk.models.Boards
import com.st.flow_demo.models.Flow
import kotlinx.serialization.encodeToString
import java.io.IOException
import java.io.InputStream
import java.util.Scanner
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

const val CUSTOM_FLOW_FOLDER = "SensorTile.box"

private const val EXAMPLES_FLOW_FOLDER = "examples"
const val FLOW_FILE_TYPE = "application/json"
const val FLOW_FILE_EXTENSION = ".json"

fun loadExampleFlows(context: Context, board: Boards.Model): List<Flow> {

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
                Log.i("parseFlowFile", "$EXAMPLES_FLOW_FOLDER/$example")
                val flow = parseFlowFile(stream)
                if (flow != null) {
                    if (flow.board_compatibility.contains(board.name)) {
                        flows.add(flow)
                    } else if ((flow.board_compatibility.size == 0) && (board == Boards.Model.SENSOR_TILE_BOX)) {
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

fun parseFlowFile(stream: InputStream): Flow? {
    val text = Scanner(stream).useDelimiter("\\A").next()
    val json = Json

    val flow =
        try {
            json.decodeFromString<Flow>(text)
        } catch (e: Exception) {
            Log.d("parseFlowFile", e.stackTraceToString())
            null
        }

    return flow
}

fun saveFlow(context: Context, file: Uri?, flow: Flow): FlowSaveDeleteState {

    val json = Json
    val output =  json.encodeToString(flow).toByteArray()
    file?.let {
        try {
            val stream = context.contentResolver.openOutputStream(file)
            stream?.let {
                stream.write(output)
                stream.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return FlowSaveDeleteState.GENERIC_WRITING_ERROR
        } catch (e: SecurityException) {
            e.printStackTrace()
            return FlowSaveDeleteState.SECURITY_ERROR
        }
        return FlowSaveDeleteState.SAVED
    }
    return FlowSaveDeleteState.GENERIC_WRITING_ERROR
}

    enum class FlowSaveDeleteState {
        DEAD_BEEF,
        EXTERNAL_STORAGE_NOT_WRITABLE,
        DIRECTORY_NOT_EXIST,
        SECURITY_ERROR,
        GENERIC_WRITING_ERROR,
        SAVED,
        DELETED,
        ERROR_DELETING
    }