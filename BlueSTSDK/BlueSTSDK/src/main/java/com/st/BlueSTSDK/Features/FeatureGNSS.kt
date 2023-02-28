package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureGNSS constructor(n: Node) :
    Feature(
        FEATURE_NAME_GNSS, n, arrayOf(
            LATITUDE_FIELD, LONGITUDE_FIELD, ALTITUDE_FIELD,
            NSAT_FIELD,
            SIGQUALITY_FIELD
        )) {

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {

        val results = arrayOfNulls<Number>(5)
        results[0] = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset)
        results[1] = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 4)
        results[2] = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 8)
        results[3] = data[dataOffset+12]
        results[4] = data[dataOffset+13]
        return ExtractResult(Sample(timestamp, results, fieldsDesc), 14)
    }



    companion object {
        private const val FEATURE_NAME_GNSS = "Global Navigation Satellite System"
        private const val FEATURE_DATA_NAME_LATITUDE = "Latitude"
        private const val FEATURE_UNIT_LATITUDE = "Lat"
        const val DATA_MAX_LATITUDE: Int =  900000000
        const val DATA_MIN_LATITUDE: Int = -900000000
        private const val FEATURE_DATA_NAME_LONGITUDE = "Longitude"
        private const val FEATURE_UNIT_LONGITUDE = "Lon"
        const val DATA_MAX_LONGITUDE: Int =  1800000000
        const val DATA_MIN_LONGITUDE: Int = -1800000000

        private const val FEATURE_DATA_NAME_ALTITUDE = "Altitude"
        private const val FEATURE_UNIT_ALTITUDE = "Meter"
        const val DATA_MAX_ALTITUDE: Int = Int.MAX_VALUE
        const val DATA_MIN_ALTITUDE: Int = 0

        private const val FEATURE_DATA_NAME_NSAT = "Satellites Number"
        private const val FEATURE_UNIT_NSAT = "Num"

        private const val FEATURE_DATA_NAME_SIGQUALITY = "Signal Quality"
        private const val FEATURE_UNIT_SIGQUALITY = "dB-Hz"


        private val LATITUDE_FIELD = Field(FEATURE_DATA_NAME_LATITUDE, FEATURE_UNIT_LATITUDE, Field.Type.Int32, DATA_MAX_LATITUDE, DATA_MIN_LATITUDE)
        private val LONGITUDE_FIELD = Field(FEATURE_DATA_NAME_LONGITUDE, FEATURE_UNIT_LONGITUDE, Field.Type.Int32, DATA_MAX_LONGITUDE, DATA_MIN_LONGITUDE)
        private val ALTITUDE_FIELD = Field(FEATURE_DATA_NAME_ALTITUDE, FEATURE_UNIT_ALTITUDE, Field.Type.Int32, DATA_MAX_ALTITUDE, DATA_MIN_ALTITUDE)
        private val NSAT_FIELD = Field(
            FEATURE_DATA_NAME_NSAT,
            FEATURE_UNIT_NSAT,Field.Type.UInt8,255,0)
        private val SIGQUALITY_FIELD = Field(
            FEATURE_DATA_NAME_SIGQUALITY,
            FEATURE_UNIT_SIGQUALITY,Field.Type.UInt8,255,0)

        /**
         * Return the Latitude value
         * @param sample data sample
         * @return
         */
        fun getLatitudeValue(sample: Sample?): Float? {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[0] != null)
                        return sample.data[0].toFloat()/(1e7f)
            }
            return null
        }

        /**
         * Return the Longitude value
         * @param sample data sample
         * @return
         */
        fun getLongitudeValue(sample: Sample?): Float? {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[1] != null)
                        return sample.data[1].toFloat()/(1e7f)
            }
            return null
        }

        /**
         * Return the Altitude value
         * @param sample data sample
         * @return
         */
        fun getAltitudeValue(sample: Sample?): Float? {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[2] != null)
                        return sample.data[2].toFloat()/(1e3f)
            }
            return null
        }

        /**
         * Return the Satellites Number value
         * @param sample data sample
         * @return
         */
        fun getNSatValue(sample: Sample?): Int? {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[3] != null)
                        return convertByteToInt(sample.data[3].toByte())
            }
            return null
        }

        /**
         * Return the Signal quality value
         * @param sample data sample
         * @return
         */
        fun getSigQualityValue(sample: Sample?): Int? {
            if (sample != null)  {
                if (sample.data.isNotEmpty())
                    if (sample.data[4] != null)
                        return convertByteToInt(sample.data[4].toByte())
            }
            return null
        }

        private fun convertByteToInt(o: Byte): Int = when {
            (o.toInt() < 0) -> 255 + o.toInt() + 1
            else -> o.toInt()
        }
    }

}