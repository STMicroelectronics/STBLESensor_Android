package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureGestureNavigation constructor(n: Node) :
    Feature(FEATURE_NAME, n, FIELDS, true) {

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val results = arrayOfNulls<Number>(2)
        results[0] = NumberConversion.byteToUInt8(data, dataOffset)
        results[1] = NumberConversion.byteToUInt8(data, dataOffset+1)
        return ExtractResult(Sample(timestamp, results, fieldsDesc), 2)
    }

    enum class NavigationGesture(val value: Int) {
        UNDEFINED(0),
        SWYPE_LEFT_TO_RIGHT(1),
        SWYPE_RIGHT_TO_LEFT(2),
        SWYPE_UP_TO_DOWN(3),
        SWYPE_DOWN_TO_UP(4),
        SINGLE_PRESS(5),
        DOUBLE_PRESS(6),
        TRIPLE_PRESS(7),
        LONG_PRESS(8),
        ERROR(9);
        companion object {
            fun fromInt(value: Int) = values().first { it.value == value}
        }
    }

    enum class NavigationButton(val value: Int) {
        UNDEFINED(0),
        LEFT(1),
        RIGHT(2),
        UP(3),
        DOWN(4),
        ERROR(5);
        companion object {
            fun fromInt(value: Int) = values().first { it.value == value}
        }
    }

    companion object {
        private const val FEATURE_NAME = "Navigation"
        private val FIELDS = arrayOf(
            Field("Gesture", null, Field.Type.UInt8, 9, 0),
            Field("Button", null, Field.Type.UInt8, 5, 0),
        )

        /**
         * extract the Navigation Gesture from a sensor sample
         * @param sample data read from the node
         * @return type of Navigation Gesture detected by the node
         */
        fun getNavigationGesture(sample: Sample): NavigationGesture {
            if (hasValidIndex(sample, 0)) {
                return NavigationGesture.fromInt(sample.data[0].toByte().toInt())
            }
            return NavigationGesture.ERROR
        }

        /**
         * extract the Navigation Button from a sensor sample
         * @param sample data read from the node
         * @return type of Navigation Button detected by the node
         */
        fun getNavigationButton(sample: Sample): NavigationButton {
            if (hasValidIndex(sample, 1)) {
                return NavigationButton.fromInt(sample.data[1].toByte().toInt())
            }
            return NavigationButton.ERROR
        }
    }
}