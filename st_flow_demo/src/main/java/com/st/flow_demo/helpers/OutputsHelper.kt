package com.st.flow_demo.helpers

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.st.flow_demo.R
import com.st.flow_demo.models.Flow
import com.st.flow_demo.models.Output
import com.st.blue_sdk.board_catalog.models.Sensor

fun getOutputIconResourceByName(name: String) = when (name) {
    "ic_bluetooth" -> R.drawable.ic_bluetooth
    "ic_sdcard" -> R.drawable.ic_sdcard
    "ic_input" -> R.drawable.ic_input
    "ic_usb" -> R.drawable.ic_usb
    "ic_multi" -> R.drawable.ic_multi_output
    "ic_expr" -> R.drawable.ic_code
    else -> R.drawable.ic_output
}

fun findOutputById(outputs: List<Output>, id: String?): Output? {
    for (output in outputs) {
        if (output.id == id) {
            return output
        }
    }
    return null
}

fun getAvailableOutputs(flow : Flow, outputs: MutableList<Set<String>>) {
    if (flow.functions.isNotEmpty()) {
        val lastFunction = flow.functions.last()
        val out: Set<String> = HashSet<String>(lastFunction.outputs)
        outputs.add(out)
    } else {
        flow.sensors.forEach { sensor ->
            outputs.add(HashSet<String>(sensor.outputs))
        }
        flow.flows.forEach { getAvailableOutputs(it,outputs) }
    }
}

fun hasOutputAmbiguousInputs(selectedOutputs: List<Output>, context: Context): String? {
    if (selectedOutputs.size <= 1) {
        return null
    }
    var onceLogic = false
    var onceHw = false
    var asInput = false
    var asExp = false
    for (output in selectedOutputs) {
        onceHw = onceHw || !output.isLogic
        onceLogic = onceLogic || output.isLogic
        asInput = asInput || output.id == Output.OUTPUT_AS_INPUT_ID
        asExp = asExp || output.id == Output.OUTPUT_EXP_ID
        if (asInput && asExp) {
            return context.getString(R.string.error_cannot_set_two_logical_output)
        }
        if (onceHw && onceLogic) {
            return context.getString(R.string.error_select_outputs_to_save)
        }
    }
    return null
}

fun checkIfRemoveBluetooth(outputs: List<Output>, sensor: Sensor): List<Output>{
    val tmp = outputs.toMutableList()
    tmp.removeIf { output ->
        if (output.id == "O3") {
            if(sensor.configuration!=null) {
                if ((sensor.configuration!!.odr != null) && (sensor.bleMaxOdr != null)) {
                    sensor.configuration!!.odr!! > sensor.bleMaxOdr!!
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }
    return tmp
}