package com.st.trilobyte.helper

import com.st.trilobyte.models.Flow
import com.st.trilobyte.models.Output
import com.st.trilobyte.models.acquisitionTimeMin

private fun Flow.hasOutput(outputType:String):Boolean{
    return this.outputs.firstOrNull{ it.id == outputType } != null
}

fun Flow.hasBtStreamAsOutput():Boolean{
    return hasOutput(Output.OUTPUT_STREAM_TO_BT)
}

fun Flow.hasSDStreamAsOutput():Boolean{
    return hasOutput(Output.OUTPUT_STREAM_TO_SD)
}

fun Flow.durationMin():Double?{
    val maxTime = this.sensors.mapNotNull { it.configuration.acquisitionTimeMin}.maxOrNull()
    if(maxTime != null){
        return if(maxTime > 0.0){
            maxTime
        }else{
            null
        }
    }
    return maxTime
}

fun List<Flow>.hasBtStreamAsOutput():Boolean{
    return this.any { it.hasBtStreamAsOutput() }
}

fun List<Flow>.maxDurationMin():Double?{
    return this.mapNotNull { it.durationMin() }.maxOrNull()
}

fun List<Flow>.hasSDStreamAsOutput():Boolean{
    return this.any { it.hasSDStreamAsOutput() }
}