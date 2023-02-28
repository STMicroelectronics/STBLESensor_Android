package com.st.trilobyte.models

import java.io.Serializable

data class FilterHolder(val id: Int,
                        val powerModes: List<PowerMode.Mode> = listOf(),
                        val filters: List<Filter> = listOf()) : Serializable