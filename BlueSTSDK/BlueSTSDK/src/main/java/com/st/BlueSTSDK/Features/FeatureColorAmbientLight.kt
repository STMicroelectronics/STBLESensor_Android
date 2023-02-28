package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureColorAmbientLight constructor(n: Node) :
    Feature(FEATURE_NAME_LUX, n, arrayOf(LUX_FIELD, CCT_FIELD, UV_INDEX_FIELD)) {

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        //val temp: Array<Field> = arrayOf(LUX_FIELD, CCT_FIELD, UV_INDEX_FIELD)

        //mDataDesc = temp

        val results = arrayOfNulls<Number>(3)
        results[0] = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset)
        results[1] = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 4)
        results[2] = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 6)
        return ExtractResult(Sample(timestamp, results, fieldsDesc), 8)
    }



    companion object {
        private const val FEATURE_NAME_LUX = "Color Ambient Light"
        private const val FEATURE_DATA_NAME_LUX = "Lux"
        private const val FEATURE_UNIT_LUX = "Lux"
        const val DATA_MAX_LUX: Int = 400000
        const val DATA_MIN_LUX: Int = 0
        private const val FEATURE_DATA_NAME_UV_INDEX = "UV Index"
        const val DATA_MAX_UV_INDEX: Short = 12
        const val DATA_MIN_UV_INDEX: Short = 0


        private const val FEATURE_DATA_NAME_CCT = "Correlated Color Temperature"
        private const val FEATURE_UNIT_CCT = "K"
        const val DATA_MAX_CCT: Short = 20000
        const val DATA_MIN_CCT: Short = 0

        private val  LUX_FIELD = Field(FEATURE_DATA_NAME_LUX, FEATURE_UNIT_LUX, Field.Type.UInt32, DATA_MAX_LUX, DATA_MIN_LUX)
        private val  CCT_FIELD = Field(FEATURE_DATA_NAME_CCT, FEATURE_UNIT_CCT, Field.Type.UInt16, DATA_MAX_CCT, DATA_MIN_CCT)
        private val  UV_INDEX_FIELD = Field(FEATURE_DATA_NAME_UV_INDEX, null, Field.Type.UInt16, DATA_MAX_UV_INDEX, DATA_MIN_UV_INDEX)

        /**
         * Return the Lux
         * @param sample data sample
         * @return
         */
        fun getLuxValue(sample: Sample?): Int {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[0] != null)
                        return sample.data[0].toInt()
            }
            return 0
        }

        /**
         * Return the CCT
         * @param sample data sample
         * @return
         */
        fun getCCTValue(sample: Sample?): Short {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[1] != null)
                        return sample.data[1].toShort()
            }
            return 0
        }

        /**
         * Return the UV Index
         * @param sample data sample
         * @return
         */
        fun getUVIndexValue(sample: Sample?): Short {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[2] != null)
                        return sample.data[2].toShort()
            }
            return 0
        }
    }

}