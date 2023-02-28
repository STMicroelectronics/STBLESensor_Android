package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion

class FeatureStepperMotor constructor(n: Node) :
    Feature(FEATURE_NAME_STEPPER, n, arrayOf(STEPPER_FIELD)) {

    //This Feature must be tested
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {

        val results = arrayOfNulls<Number>(1)
        results[0] = NumberConversion.byteToUInt8(data,dataOffset)
        return ExtractResult(Sample(timestamp, results, fieldsDesc), 1)
    }

    enum class StepperMotorState {
        INACTIVE, // Motor not running.
        RUNNING   // Motor running.
    }

    enum class StepperMotorCommand {
        MOTOR_STOP_RUNNING_WITHOUT_TORQUE, // Stops running with HiZ.
        MOTOR_STOP_RUNNING_WITH_TORQUE,    // Stops running with torque applied.
        MOTOR_RUN_FORWARD,                 // Runs forward indefinitely.
        MOTOR_RUN_BACKWARD,                // Runs backward indefinitely.
        MOTOR_MOVE_STEPS_FORWARD,          // Moves steps forward.
        MOTOR_MOVE_STEPS_BACKWARD          // Moves steps backward.
    }

    companion object {
        private const val FEATURE_NAME_STEPPER = "Stepper Motor"
        private const val FEATURE_DATA_NAME_STEPPER = "StepMotor"
        private const val FEATURE_UNIT_STEPPER = "State"
        var DATA_MAX_STEPPER: Int = StepperMotorState.RUNNING.ordinal
        var DATA_MIN_STEPPER: Int = StepperMotorState.INACTIVE.ordinal


        private val STEPPER_FIELD = Field(
            FEATURE_DATA_NAME_STEPPER,
            FEATURE_UNIT_STEPPER,
            Field.Type.UInt8,
            DATA_MAX_STEPPER,
            DATA_MIN_STEPPER
        )

        /**
         * Return the Stepper Motor State
         * @param sample data sample
         * @return
         */
        fun getStateValue(sample: Sample?): StepperMotorState? {
            if (sample != null) {
                if (sample.data.isNotEmpty())
                    if (sample.data[0] != null)
                        return StepperMotorState.values()[sample.data[0].toInt()]
            }
            return null
        }

        /**
         * Write a command to Stepper Motor
         * @param f Feature
         * @param command Command to Stepper Motor
         * @param steps Steps number
         * @return
         */
        fun writeCommand(f: Feature, command: StepperMotorCommand, steps: UInt=0U) {
            val data:ByteArray = byteArrayOf()

            data.plus(command.ordinal.toByte())
            if((command==StepperMotorCommand.MOTOR_MOVE_STEPS_BACKWARD) || (command==StepperMotorCommand.MOTOR_MOVE_STEPS_FORWARD)) {
                //this must be tested...
                data.plus(NumberConversion.BigEndian.uint32ToBytes(steps.toLong()));
            }
            f.parentNode.writeFeatureData(f,data)
        }

        /**
         * Read the State from Stepper Motor
         * @param f Feature
         */
        fun readState(f: Feature) {
            f.parentNode.readFeature(f)
        }
    }
}