package com.st.predicted_maintenance.utilities

import com.st.blue_sdk.features.extended.predictive.Status

data class ViewStatus(
    val xStatus: Status? = null,
    val yStatus: Status? = null,
    val zStatus: Status? = null,
    val x: Point? = null,
    val y: Point? = null,
    val z: Point? = null
)