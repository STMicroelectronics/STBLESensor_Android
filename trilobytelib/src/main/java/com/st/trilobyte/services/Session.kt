package com.st.trilobyte.services

import com.st.trilobyte.models.Flow

object Session {

    var expression: Flow? = null

    var selectedFlows: List<Flow>? = null

    fun setSession(flows: List<Flow>, exp: Flow? = null) {
        clear()
        selectedFlows = flows
        expression = exp
    }

    private fun clear() {
        expression = null
        selectedFlows = null
    }
}