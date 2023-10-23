package com.st.flow_demo.models

data class MlcFsmDecisionTreeOutput(
    var enabled: Boolean=false,
    var number: Int,
    var name: String,
    var mlcFsmLabels: List<MlcFsmLabelEntry>
)
