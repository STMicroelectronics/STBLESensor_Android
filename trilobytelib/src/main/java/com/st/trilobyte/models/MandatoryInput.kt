package com.st.trilobyte.models

import java.io.Serializable

data class MandatoryInput(
        var sensors: List<String> = ArrayList(),
        var functions: List<Int> = ArrayList()
) : Serializable