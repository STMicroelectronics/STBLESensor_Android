package com.st.flow_demo.helpers

import android.content.Context
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.board_catalog.models.CutOff
import com.st.flow_demo.models.Filter
import com.st.blue_sdk.board_catalog.models.FilterConfiguration
import com.st.blue_sdk.board_catalog.models.PowerMode
import com.st.blue_sdk.board_catalog.models.SensorConfiguration
import com.st.flow_demo.models.SensorFilter


fun fillFilterSectionByBoardSensorPowerMode(context: Context,
                                            sensorId: String,
                                            powerMode: PowerMode.Mode?,
                                            sensorConfiguration: SensorConfiguration,
                                            board: Boards.Model)  {

    val sensorFilter = getSensorFiltersBySensorId(context = context, sensorId = sensorId, board = board)

    sensorConfiguration.lowPassCutoffs = null
    sensorConfiguration.highPassCutoffs = null

    if(sensorFilter!=null) {
        val powerModeToUse =  powerMode ?: PowerMode.Mode.NONE
        val odrToUse = sensorConfiguration.odr ?: -1.0

        val filter: Filter? = getAvailableFilter(sensorFilter, powerModeToUse, odrToUse)

        if(filter==null) {
            if (sensorConfiguration.oneShotTime==null) {
                return
            }
        }

        val lowPassCutoffs: List<CutOff> = filter?.lowPass ?: sensorFilter.values[0].filters[0].lowPass

        val highPassCutoffs: List<CutOff>? =
            filter?.highPass?.ifEmpty {
                null
            }

        sensorConfiguration.lowPassCutoffs = lowPassCutoffs
        sensorConfiguration.highPassCutoffs = highPassCutoffs
    }
    return
}

fun getAvailableFilter(
    sensorFilter: SensorFilter,
    powerMode: PowerMode.Mode,
    odr: Double
): Filter? {
    if (powerMode === PowerMode.Mode.NONE) {
        return sensorFilter.values[0].filters[0]
    }
    for (filterHolder in sensorFilter.values) {
        if (filterHolder.powerModes.contains(powerMode)) {
            for (filter in filterHolder.filters) {
                if (filter.odrs.contains(odr)) {
                    return filter
                }
            }
        }
    }
    return null
}