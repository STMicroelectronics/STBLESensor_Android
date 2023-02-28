package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureNEAIClassClassification constructor(n: Node) :
    Feature(FEATURE_NAME, n, FIELDS, true) {

    fun writeStopClassificationCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(STOP_CLASSIFICATION.toByte()))
    }

    fun writeStartClassificationCommand() {
        parentNode.writeFeatureData(this, byteArrayOf(START_CLASSIFICATION.toByte()))
    }


    /** Enum containing the possible result of the PHASE Field */
    enum class PhaseType {
        /** idle */
        IDLE,

        /** classification */
        CLASSIFICATION,

        /** busy */
        BUSY,

        /** null */
        NULL
    }

    /** Enum containing the possible result of the Mode Field */
    enum class ModeType {
        /** idle */
        ONE_CLASS,

        /** classification */
        N_CLASS,

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

    /**
     * extract the Information from NEAI Anomaly Detection Feature
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {

        if(data.size-dataOffset<4) {
            //Minimal number of necessary bytes
            throw IllegalArgumentException("Not enough bytes")
        } else {
            if(data.size-dataOffset==4) {
                // We are in Idle phase
                val results = arrayOfNulls<Number>(2)
                //Mode: 1-Class/N-Class
                results[0] = NumberConversion.byteToUInt8(data, dataOffset + 2 )
                //Phase
                results[1] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 1)
                return ExtractResult(Sample(timestamp, results, fieldsDesc), 2+2)
            } else {
                val mode = NumberConversion.byteToUInt8(data, dataOffset + 2 )

                when (mode.toInt()) {
                    0x01 -> {
                        //1-Class

                        if(data.size-dataOffset!=6) {
                            throw IllegalArgumentException("Wrong number of bytes (${data.size-dataOffset}) for 1-Class")
                        }

                        val results = arrayOfNulls<Number>(6)
                        results[0] = mode
                        //Phase
                        results[1] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 1)
                        //State
                        results[2] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 2)
                        //Class Major
                        results[3] = 1
                        //Class Number
                        results[4] = 1
                        //Class 1 outlier (0->1)
                        results[5] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3)

                        return ExtractResult(Sample(timestamp, results, fieldsDesc), 2+4)

                    }
                    0x02 -> {
                        //N-Class

                        if(data.size-dataOffset==6) {
                            //Unknown Class
                            val results = arrayOfNulls<Number>(5)
                            //Mode
                            results[0] = mode
                            //Phase
                            results[1] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 1)
                            //State
                            results[2] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 2)
                            //Class Major
                            val classMajor = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3)

                            if(classMajor.toInt()!=0) {
                                throw IllegalArgumentException("Unknown Case not Valid ${classMajor.toInt()} !=0")
                            }
                            results[3] = classMajor
                            //Class Number
                            results[4] = 1
                            return ExtractResult(Sample(timestamp, results, fieldsDesc), 2+4)
                        } else {
                           //Inference
                            val numClasses: Int = data.size-dataOffset-6
                            if(numClasses>N_MAX_CLASS_NUMBER) {
                                throw IllegalArgumentException("Too many Classes $numClasses")
                            }

                            val results = arrayOfNulls<Number>(numClasses+5)
                            //Mode
                            results[0] = mode
                            //Phase
                            results[1] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 1)
                            //State
                            results[2] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 2)
                            //Class Major
                            results[3] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3)
                            //Number of Classes
                            results[4] = numClasses

                            for (i in 0 until numClasses) {
                                results[5+i] = NumberConversion.byteToUInt8(data, dataOffset + 2 + 4 + i)
                            }
                            return ExtractResult(Sample(timestamp, results, fieldsDesc), 2+4+numClasses)
                        }
                    }
                    else -> throw IllegalArgumentException("Mode Type not recognized")
                }
            }
        }
    }

    companion object {

        private const val FEATURE_NAME = "NEAI Classification"
        private const val N_MAX_CLASS_NUMBER = 8

        private const val STOP_CLASSIFICATION: Int = 0x00
        private const val START_CLASSIFICATION: Int = 0x01

        const val CLASS_PROB_ESCAPE_CODE: Int = 0xFF


        private val FIELDS = arrayOf(
            Field("Mode", null, Field.Type.UInt8, 2, 0),
            Field("Phase", null, Field.Type.UInt8, 1, 0),
            Field("State", null, Field.Type.UInt8, 128, 0),
            Field("Class Major Prob", null, Field.Type.UInt8, 8, 0),
            Field("ClassesNumber", null, Field.Type.UInt8, 8, 2),
            Field("Class 1 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 2 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 3 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 4 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 5 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 6 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 7 Probability", "%", Field.Type.UInt8, 100, 0),
            Field("Class 8 Probability", "%", Field.Type.UInt8, 100, 0)
        )

        /**
         * extract the Mode from a NEAI (nClass) message
         * @param sample data read from the node
         * @return type of Mode detected by the node
         */
        fun getModeValue(sample: Sample): ModeType {
            if (hasValidIndex(sample, 0)) {
                return when (sample.data[0].toByte().toInt()) {
                    0x01 -> ModeType.ONE_CLASS
                    0x02 -> ModeType.N_CLASS
                    else -> ModeType.NULL
                }
            }
            return ModeType.NULL
        }

        /**
         * extract the PHASE from a NEAI (nClass) message
         * @param sample data read from the node
         * @return type of PHASE detected by the node
         */
        fun getPhaseValue(sample: Sample): PhaseType {
            if (hasValidIndex(sample, 1)) {
                return when (sample.data[1].toByte().toInt()) {
                    0x00 -> PhaseType.IDLE
                    0x01 -> PhaseType.CLASSIFICATION
                    0x02 -> PhaseType.BUSY
                    else -> PhaseType.NULL
                }
            }
            return PhaseType.NULL
        }

        /**
         * extract the STATE from a NEAI (nClass) message
         * @param sample data read from the node
         * @return type of STATE detected by the node
         */
        fun getStateValue(sample: Sample): StateType {
            if (hasValidIndex(sample, 2)) {
                return when (sample.data[2].toInt()) {
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
         * extract the Most Probable Class from a NEAI (nClass) message
         * @param sample data read from the node
         * @return Class Number with bigger Probablity
         */
        fun getMostProbableClass(sample: Sample): Int {
            if (hasValidIndex(sample, 3)) {
                return sample.data[3].toInt()
            }
            return 0
        }

        /**
         * extract the Classes Number from a NEAI (nClass) message
         * @param sample data read from the node
         * @return Number of Classes
         */
        fun getClassNumber(sample: Sample): Int {
            if (hasValidIndex(sample, 4)) {
                return sample.data[4].toInt()
            }
            return 0
        }

        /**
         * extract the Probability for a Class  from a NEAI (nClass) message
         * @param sample data read from the node
         * @return Class Number with bigger Probablity
         */
        fun getClassProbability(sample: Sample, num: Int): Int {
            if (hasValidIndex(sample, num + 5)) {
                return sample.data[num + 5].toInt()
            }
            return 0
        }
    }
}