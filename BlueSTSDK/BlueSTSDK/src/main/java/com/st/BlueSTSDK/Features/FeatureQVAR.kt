package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureQVAR constructor(n: Node) :
        Feature(FEATURE_NAME_QVAR, n, arrayOf(QVAR_FIELD, FLAG_FIELD, DQVAR_FIELD, PARAM_FIELD,NUM_FIELD)) {

    //TimeStamp (2); Qvar (4); Flag (1) ,dQvar (4);Parameter (4);

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        //val temp: Array<Field> = arrayOf(QVAR_FIELD, FLAG_FIELD, DQVAR_FIELD, PARAM_FIELD)

        //mDataDesc = temp

        val numberOfFields =
            when(data.size - dataOffset){
                4->  1
                5->  2
                9 ->  3
                13 ->   4
                else -> {
                    0
                }
            }

        if(numberOfFields!=0) {
            //We allocate always 4 results... this is a tmp workaround for plot Demo
            // if we want to avoid to display all the Feature elements
            val results = arrayOfNulls<Number>(5)
            //val results = arrayOfNulls<Number>(4)
            var numBytes = 4;
            results[0] = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset)
            if (numberOfFields > 1) {
                //Read the Flag Value
                results[1] = data[dataOffset + 4];
                numBytes += 1
                if (numberOfFields > 2) {
                    //Read the DQVAR value
                    numBytes += 4
                    results[2] = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 5)
                    if (numberOfFields == 4) {
                        //Read the PARAM Value
                        numBytes += 4
                        results[3] = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 9)
                    }
                }
            }

            //Filling the empty Fields
            // this is a tmp workaround for plot Demo
            // if we want to avoid to display all the Feature elements
            if(numberOfFields!=4) {
                for (i in numberOfFields..3) {
                    results[i] = 0
                }
            }
            results[4]= numberOfFields;
            return ExtractResult(Sample(timestamp, results, fieldsDesc), numBytes)
        }

        return ExtractResult(null, 0)
    }



    companion object {
        private const val FEATURE_NAME_QVAR = "Electric Charge Variation"

        private const val FEATURE_DATA_NAME_QVAR = "QVAR"
        private const val FEATURE_UNIT_QVAR = "LSB"
        private val DATA_MAX_QVAR_DQVAR_: Number = Int.MAX_VALUE
        private val  DATA_MIN_QVAR_DQVAR: Number = Int.MIN_VALUE

        private const val FEATURE_DATA_NAME_FLAG = "Flag"
        private const val FEATURE_UNIT_FLAG = "NotDefined"
        private val DATA_MAX_FLAG: Number = 255
        private val DATA_MIN_FLAG: Number = 0

        private const val FEATURE_DATA_NAME_DQVAR = "DQVAR"
        private const val FEATURE_UNIT_DQVAR = "LSB"


        private const val FEATURE_DATA_NAME_PARAM = "Parameter"
        private const val FEATURE_UNIT_PARAM = "NotDefined"
        private val DATA_MAX_PARAM: Number = 1L shl 32 - 1
        private val DATA_MIN_PARAM: Number = 0


        private val QVAR_FIELD = Field(FEATURE_DATA_NAME_QVAR, FEATURE_UNIT_QVAR, Field.Type.Int32, DATA_MAX_QVAR_DQVAR_, DATA_MIN_QVAR_DQVAR,true)
        private val FLAG_FIELD = Field(FEATURE_DATA_NAME_FLAG, FEATURE_UNIT_FLAG, Field.Type.UInt8, DATA_MAX_FLAG, DATA_MIN_FLAG,false)
        private val DQVAR_FIELD = Field(FEATURE_DATA_NAME_DQVAR, FEATURE_UNIT_DQVAR, Field.Type.Int32, DATA_MAX_QVAR_DQVAR_, DATA_MIN_QVAR_DQVAR,true)
        private val PARAM_FIELD = Field(FEATURE_DATA_NAME_PARAM, FEATURE_UNIT_PARAM, Field.Type.UInt32, DATA_MAX_PARAM, DATA_MIN_PARAM,false)
        private val NUM_FIELD = Field("Num Fields", "#", Field.Type.UInt8, 4, 0,false)

        /**
         * Return the QVAR
         * @param sample data sample
         * @return
         */
        fun getQVARValue(sample: Sample?): Long? {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if(sample.data[0]!=null)
                        return sample.data[0].toLong()
            }
            return null
        }

        /**
         * Return the Flag
         * @param sample data sample
         * @return
         */
        fun getFlagValue(sample: Sample?): Byte? {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if (sample.data.size>=2)
                        if(sample.data[1]!=null)
                            return sample.data[1].toByte()
            }
            return null
        }

        /**
         * Return the DQVAR
         * @param sample data sample
         * @return
         */
        fun getDQVARValue(sample: Sample?): Long? {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if (sample.data.size>=3)
                        if(sample.data[2]!=null)
                            return sample.data[2].toLong()
            }
            return null
        }

        /**
         * Return the PARAM
         * @param sample data sample
         * @return
         */
        fun getParamValue(sample: Sample?): Long? {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if (sample.data.size==4)
                        if(sample.data[3]!=null)
                            return sample.data[3].toLong()
            }
            return null
        }

        /**
         * Return the Number of fields
         * @param sample data sample
         * @return
         */
        fun getNumFields(sample: Sample?): Int {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if (sample.data.size==5)
                        if(sample.data[4]!=null)
                            return sample.data[4].toInt()
            }
            return 0
        }
    }
}