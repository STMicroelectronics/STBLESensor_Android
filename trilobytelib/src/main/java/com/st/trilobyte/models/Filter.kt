package com.st.trilobyte.models

data class Filter(val odrs: List<Double> = listOf(),
                  val lowPass: List<CutOff> = listOf(),
                  val highPass: List<CutOff> = listOf())