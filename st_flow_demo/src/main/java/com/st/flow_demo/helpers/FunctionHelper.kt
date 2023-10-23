package com.st.flow_demo.helpers

import com.st.flow_demo.R
import com.st.flow_demo.models.Flow
import com.st.flow_demo.models.Function
import com.st.blue_sdk.board_catalog.models.Sensor

fun getFunctionIconResourceByName(name: String) = when (name) {
    "ic_function" -> R.drawable.ic_function
    else -> R.drawable.ic_task_alt
}

fun findFunctionById(functions: List<Function>, id: String?): Function? {
    for (function in functions) {
        if (function.id == id) {
            return function
        }
    }
    return null
}

fun filterFunctionsByMandatoryInputs(
    mutableListFunctions: MutableList<Function>,
    listOfSensorId: List<String>
) {
    //val listOfSensorId = sensors.map { sensor -> sensor.id }

    //mutableListFunctions.removeIf { !hasFunctionMandatoryInput(it, listOfSensorId) }
    for (i in mutableListFunctions.indices.reversed()) {
        val function: Function = mutableListFunctions[i]
        if (!hasFunctionMandatoryInput(function, listOfSensorId)) {
            mutableListFunctions.remove(function)
        }
    }
}


private fun hasFunctionMandatoryInput(function: Function, listOfSensorId: List<String>): Boolean {

    if (function.mandatoryInputs.isEmpty()) {
        return true
    }
    for (mandatoryInputs in function.mandatoryInputs) {
        if (listOfSensorId.containsAll(mandatoryInputs)) {
            return true
        }
    }
    return false
}

fun filterFunctionsByInputs(mutableListFunctions: MutableList<Function>, listOfSensorId: List<String>) {
   // val listOfSensorId = sensors.map { sensor -> sensor.id }

    mutableListFunctions.removeIf { function ->
        var hasInput = false
        for (sensorId in listOfSensorId) {
            if(function.inputs.contains(sensorId)) {
                hasInput= true
                break
            }
        }
        !hasInput
    }
}

fun filterFunctionsByRepeatCount(mutableListFunctions: MutableList<Function>, currentFlow: Flow) {

    if (currentFlow.functions.isNotEmpty()) {
        val lastFunction = currentFlow.functions.last()
        filterFunctionByRepeatCount(currentFlow,lastFunction,0,mutableListFunctions)
    } else {
          currentFlow.flows.forEach { parentFlow ->
              filterFunctionsByRepeatCount(mutableListFunctions,parentFlow)
          }
    }

    return
}

private fun filterFunctionByRepeatCount(
    currentFlow: Flow,
    lastFunction: Function,
    repeatCount: Int,
    mutableListFunctions: MutableList<Function>
) {
    var repeatCountLocal = repeatCount
    if (currentFlow.functions.isNotEmpty()) {
        var scanParentsFlow = true
        for (i in currentFlow.functions.size - 1 downTo 0) {
            val function: Function = currentFlow.functions[i]
            if (function.id != lastFunction.id) {
                scanParentsFlow = false
                break
            }
            repeatCountLocal += 1
        }
        if (scanParentsFlow && isCompositeFlow(currentFlow)) {
            for (parentFlow in currentFlow.flows) {
                filterFunctionByRepeatCount(
                    parentFlow,
                    lastFunction,
                    repeatCountLocal,
                    mutableListFunctions
                )
            }
        } else {
            for (availableFunction in mutableListFunctions) {
                if (availableFunction.id == lastFunction.id) {
                    if (availableFunction.maxRepeatCount != null) {
                        if (repeatCountLocal >= availableFunction.maxRepeatCount) {
                            mutableListFunctions.remove(availableFunction)
                            break
                        }
                    }
                }
            }
        }
    } else {
        for (parentFlow in currentFlow.flows) {
            filterFunctionByRepeatCount(parentFlow, lastFunction, repeatCount, mutableListFunctions)
        }
    }
}

fun hasFunctionAmbiguousInputs(selectedFunction: Function,listOfSensorId: List<String>) : Boolean {
    var counter=0
    for(inputId in listOfSensorId) {
        if(selectedFunction.inputs.contains(inputId)) {
            counter++
        }
    }
    return if(selectedFunction.maxRepeatCount!=null) {
        counter > selectedFunction.maxRepeatCount
    } else {
        false
    }
}

//fun getDescriptionMessageForSensor(id:String):Int?{
//    return when(id){
//        "S1" -> R.string.thresholdDesc_temperature
//        "S2" -> R.string.thresholdDesc_humidity
//        "S3" -> R.string.thresholdDesc_pressure
//        "S4" -> R.string.thresholdDesc_acceleration
//        "S5" -> R.string.thresholdDesc_acceleration
//        "S6" -> R.string.thresholdDesc_gyroscope
//        "S7" -> R.string.thresholdDesc_acceleration
//        "S8" -> R.string.thresholdDesc_magnetometer
//        "S10" -> R.string.thresholdDesc_timer
//        else -> null
//    }
//}
