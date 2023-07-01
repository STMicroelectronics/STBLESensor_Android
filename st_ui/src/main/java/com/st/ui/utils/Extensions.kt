package com.st.ui.utils

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum

fun LoremIpsum.asString(): String = this.values.joinToString()

val KeyboardOptions.Companion.Number
    get() = Default.copy(
        imeAction = ImeAction.Send,
        keyboardType = KeyboardType.Number
    )
