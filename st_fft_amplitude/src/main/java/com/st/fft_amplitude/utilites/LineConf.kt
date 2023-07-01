package com.st.fft_amplitude.utilites

import android.graphics.Color

data class LineConf(val color: Int, val name: String) {
    companion object {
        val LINES = arrayOf(
            LineConf(Color.RED, "X"),
            LineConf(Color.BLUE, "Y"),
            LineConf(Color.GREEN, "Z")
        )
    }
}
