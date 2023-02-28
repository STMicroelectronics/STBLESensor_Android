package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureNEAIAnomalyDetection  constructor(n: Node) :
        Feature(FEATURE_NAME, n, FIELDS,true) {

    private val stopCommand: Int = 0x00
    private val learningCommand: Int = 0x01
    private val detectionCommand: Int = 0x02
    private val resetKnowledgeCommand: Int = 0xFF

    fun writeStopCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(stopCommand.toByte()))
    }

    fun writeLearningCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(learningCommand.toByte()))
    }

    fun writeDetectionCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(detectionCommand.toByte()))
    }

    fun writeResetKnowledgeCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(resetKnowledgeCommand.toByte()))
    }

    /** Enum containing the possible result of the PHASE Field */
    enum class PhaseType {
        /** idle */
        IDLE,

        /** learning */
        LEARNING,

        /** detection */
        DETECTION,

        /** idle (Trained) */
        IDLE_TRAINED,

        /** busy */
        BUSY,

        /** null */
        NULL
    }

    /** Enum containing the possible result of the STATE Field */
    enum class StateType {
        /** ok */
        OK,

        /** initNotCalled */
        INIT_NOT_CALLED,

        /** boardError */
        BOARD_ERROR,

        /** knowledgeError */
        KNOWLEDGE_ERROR,

        /** notEnoughLearning */
        NOT_ENOUGH_LEARNING,

        /** minimalLearningDone */
        MINIMAL_LEARNING_DONE,

        /** unknownError */
        UNKNOWN_ERROR,

        /** null */
        NULL
    }

    /** Enum containing the possible result of the STATUS Field */
    enum class StatusType {
        /** normal */
        NORMAL,

        /** anomaly */
        ANOMALY,

        /** null */
        NULL
    }

    /**
     * extract the Information from NEAI Anomaly Detection Feature
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val results = arrayOfNulls<Number>(5)
        results[0] = NumberConversion.byteToUInt8(data, dataOffset+2)
        results[1] = NumberConversion.byteToUInt8(data, dataOffset+3)
        results[2] = NumberConversion.byteToUInt8(data, dataOffset+4)
        results[3] = NumberConversion.byteToUInt8(data, dataOffset+5)
        results[4] = NumberConversion.byteToUInt8(data, dataOffset+6)

        return ExtractResult(Sample(timestamp, results, fieldsDesc), 7)
    }

    companion object {

        private const val FEATURE_NAME = "NEAI AD"

        private val FIELDS = arrayOf(
                Field("Phase", null, Field.Type.UInt8, 3, 0),
                Field("State", null, Field.Type.UInt8, 128, 0),
                Field("Phase Progress", "%", Field.Type.UInt8, 100, 0),
                Field("Status", null, Field.Type.UInt8, 1, 0),
                Field("Similarity", null, Field.Type.UInt8, 100, 0)
        )

        /**
         * extract the PHASE from a NEAI (AD) message
         * @param sample data read from the node
         * @return type of PHASE detected by the node
         */
        fun getPhaseValue(sample: Sample): PhaseType {
            if (hasValidIndex(sample, 0)) {
                return when (sample.data[0].toByte().toInt()) {
                    0x00 -> PhaseType.IDLE
                    0x01 -> PhaseType.LEARNING
                    0x02 -> PhaseType.DETECTION
                    0x03 -> PhaseType.IDLE_TRAINED
                    0x04 -> PhaseType.BUSY
                    else -> PhaseType.NULL
                }
            }
            return PhaseType.NULL
        }

        /**
         * extract the STATE from a NEAI (AD) message
         * @param sample data read from the node
         * @return type of STATE detected by the node
         */
        fun getStateValue(sample: Sample): StateType {
            if (hasValidIndex(sample, 1)) {
                return when (sample.data[1].toInt()) {
                    0x00 -> StateType.OK
                    0x7B -> StateType.INIT_NOT_CALLED
                    0x7C -> StateType.BOARD_ERROR
                    0x7D -> StateType.KNOWLEDGE_ERROR
                    0x7E -> StateType.NOT_ENOUGH_LEARNING
                    0x7F -> StateType.MINIMAL_LEARNING_DONE
                    0x80 -> StateType.UNKNOWN_ERROR
                    else -> StateType.NULL
                }
            }
            return StateType.NULL
        }

        /**
         * extract the PHASE_PROGRESS from a NEAI (AD) message
         * @param sample data read from the node
         * @return type of PHASE_PROGRESS detected by the node
         */
        fun getPhaseProgressValue(sample: Sample): Int {
            if (hasValidIndex(sample, 2)) {
                return sample.data[2].toByte().toInt()
            }
            return 0
        }

        /**
         * extract the STATUS from a NEAI (AD) message
         * @param sample data read from the node
         * @return type of STATUS detected by the node
         */
        fun getStatusValue(sample: Sample): StatusType {
            if (hasValidIndex(sample, 3)) {
                return when (sample.data[3].toByte().toInt()) {
                    0x00 -> StatusType.NORMAL
                    0x01 -> StatusType.ANOMALY
                    else -> StatusType.NULL
                }
            }
            return StatusType.NULL
        }

        /**
         * extract the SIMILARITY from a NEAI (AD) message
         * @param sample data read from the node
         * @return type of SIMILARITY detected by the node
         */
        fun getSimilarityValue(sample: Sample): Int {
            if (hasValidIndex(sample, 4)) {
                return sample.data[4].toByte().toInt()
            }
            return 0
        }
    }
}