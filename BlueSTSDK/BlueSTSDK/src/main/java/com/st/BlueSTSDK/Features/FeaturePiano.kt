package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node

class FeaturePiano  constructor(n: Node) :
    Feature(FEATURE_NAME, n, arrayOf(FEATURE_PIANO),false) {

    fun writeCommand(Command: ByteArray?) {
        parentNode.writeFeatureData(this, Command)
    }


    /**
     * extract the Information from Extended Configuration Feature
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (20) and data extracted (the audio information, the 40 shorts array)
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult? {
        val results = arrayOfNulls<Number>(2)
        results[0] = data[dataOffset]
        results[1] = data[dataOffset+1]
        return ExtractResult(Sample(timestamp, results, fieldsDesc), 2)
    }

    companion object {
        private const val FEATURE_NAME = "Piano"
        private const val FEATURE_DATA_NAME = "Sample"
        private val FEATURE_PIANO = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)

        const val START_SOUND:Byte = 0x01
        const val STOP_SOUND:Byte = 0x00
    }
}