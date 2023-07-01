package com.st.fft_amplitude.utilites

enum class WindowType {
    RECTANGULAR,
    HANNING,
    HAMMING,
    FLAT_TOP;

    companion object {
        fun fromByte(value: Byte) = when (value.toInt()) {
            0x00 -> WindowType.RECTANGULAR
            0x01 -> WindowType.HANNING
            0x02 -> WindowType.HAMMING
            0x03 -> WindowType.FLAT_TOP
            else -> WindowType.RECTANGULAR
        }
    }
}
