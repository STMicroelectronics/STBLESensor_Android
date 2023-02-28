package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureSTREDL constructor(n: Node) : Feature(FEATURE_NAME,n, Array(N_OUTPUT_REGISTER) { i -> Field(
        FEATURE_DATA_NAME+i,null,Field.Type.UInt8, FEATURE_DATA_MIN, FEATURE_DATA_MAX)}) {


    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult? {
        require(data.size - dataOffset >= N_OUTPUT_REGISTER) { "There are no $N_OUTPUT_REGISTER byte available to read" }
        val output = arrayOfNulls<Number>(N_OUTPUT_REGISTER)
        for (i in 0 until N_OUTPUT_REGISTER) {
            output[i] = NumberConversion.byteToUInt8(data, dataOffset + i)
        }
        val temp = Sample(
            timestamp, output,
            fieldsDesc
        )
        return ExtractResult(temp, N_OUTPUT_REGISTER)
    }

    companion object {
        private const val N_OUTPUT_REGISTER = 8
        const val FEATURE_NAME = "STREDL "
        private const val FEATURE_DATA_NAME = "Register_"

        private const val FEATURE_DATA_MIN = 0
        private const val FEATURE_DATA_MAX = 255

        fun getRegisterStatus(sample: Sample): ShortArray {
            val data = sample.data
            val outData = ShortArray(data.size)
            for (i in data.indices) {
                outData[i] = data[i].toShort()
            }
            return outData
        }

        fun getRegisterStatus(sample: Sample, registerIndex: Int): Short {
            return if (hasValidIndex(sample, registerIndex)) {
                sample.data[registerIndex].toShort()
            } else Short.MAX_VALUE
        }
    }
}