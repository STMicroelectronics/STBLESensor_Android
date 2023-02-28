/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Config.STWeSU;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Utils.FwVersion;

/**
 * This class help to get list of Registers available for The BlueST devices
 * <p>
 *     It define the Register list available for the device
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class RegisterDefines {

    /**
     * This enum contains the registers available for the STWeSU Device
     */
    public enum RegistersName{
        /*Mandatory registers*/
        FW_VER(new Register(0x00, 1, Register.Access.R, Register.Target.BOTH)),
        LED_CONFIG(new Register(0x02, 1, Register.Access.RW, Register.Target.BOTH)),
        BLE_LOC_NAME(new Register(0x03, 8, Register.Access.RW, Register.Target.PERSISTENT)),
        BLE_PUB_ADDR(new Register(0x0B, 3, Register.Access.RW, Register.Target.PERSISTENT)),
        BLE_ADDR_TYPE(new Register(0x0E, 1, Register.Access.RW, Register.Target.PERSISTENT)),

        BATTERY_LEVEL(new Register(0x03, 1, Register.Access.R, Register.Target.SESSION)),
        BATTERY_VOLTAGE(new Register(0x04, 2, Register.Access.R, Register.Target.SESSION)),
        CURRENT(new Register(0x06, 2, Register.Access.R, Register.Target.SESSION)),
        PWRMNG_STATUS(new Register(0x08, 1, Register.Access.R, Register.Target.SESSION)),

        /*optional generic*/
        RADIO_TXPWR_CONFIG(new Register(0x20, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        TIMER_FREQ(new Register(0x21, 1, Register.Access.RW, Register.Target.BOTH)),
        PWR_MODE_CONFIG(new Register(0x22, 1, Register.Access.RW, Register.Target.BOTH)),

        GROUP_A_FEATURE_CTRLS_0001(new Register(0x24, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0002(new Register(0x25, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0004(new Register(0x26, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0008(new Register(0x27, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0010(new Register(0x28, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0020(new Register(0x29, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0040(new Register(0x2A, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0080(new Register(0x2B, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0100(new Register(0x2C, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0200(new Register(0x2D, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0400(new Register(0x2E, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_0800(new Register(0x2F, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_1000(new Register(0x30, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_2000(new Register(0x31, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_4000(new Register(0x32, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_FEATURE_CTRLS_8000(new Register(0x33, 1, Register.Access.RW, Register.Target.BOTH)),

        GROUP_B_FEATURE_CTRLS_0001(new Register(0x35, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0002(new Register(0x36, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0004(new Register(0x37, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0008(new Register(0x38, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0010(new Register(0x39, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0020(new Register(0x3A, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0040(new Register(0x3B, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0080(new Register(0x3C, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0100(new Register(0x3D, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0200(new Register(0x3E, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0400(new Register(0x3F, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_0800(new Register(0x40, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_1000(new Register(0x41, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_2000(new Register(0x42, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_4000(new Register(0x43, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURE_CTRLS_8000(new Register(0x44, 1, Register.Access.RW, Register.Target.BOTH)),

        BLE_DEBUG_CONFIG(new Register(0x45, 1, Register.Access.RW, Register.Target.BOTH)),
        USART_DEBUG_CONFIG(new Register(0x46, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_A_CALIBRATION_MAP(new Register(0x47, 1, Register.Access.R, Register.Target.PERSISTENT)),
        GROUP_B_CALIBRATION_MAP(new Register(0x48, 1, Register.Access.R, Register.Target.PERSISTENT)),
        GROUP_A_FEATURES_MAP(new Register(0x49, 1, Register.Access.RW, Register.Target.BOTH)),
        GROUP_B_FEATURES_MAP(new Register(0x4A, 1, Register.Access.RW, Register.Target.BOTH)),
        BLUENRG_INFO(new Register(0x4C, 2, Register.Access.R, Register.Target.SESSION)),

        MAGNETOMETER_CALIBRATION_START(new Register(0x60, 1, Register.Access.RW, Register.Target.PERSISTENT)),

        ACCELEROMETER_CONFIG_FS(new Register(0x74, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        ACCELEROMETER_CONFIG_ODR(new Register(0x75, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        GYROSCOPE_CONFIG_FS(new Register(0x76, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        GYROSCOPE_CONFIG_ODR(new Register(0x77, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        MAGNETOMETER_CONFIG_FS(new Register(0x78, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        MAGNETOMETER_CONFIG_ODR(new Register(0x79, 1, Register.Access.RW, Register.Target.PERSISTENT)),
        ACC_EVENT_CONFIG(new Register(0x7A, 1, Register.Access.RW, Register.Target.BOTH)),
        PRESSURE_CONFIG_ODR(new Register(0x7B, 1, Register.Access.RW, Register.Target.PERSISTENT)),

        MOTION_FX_CALIBRATION_LIC_STATUS(new Register(0x8C, 2, Register.Access.R, Register.Target.SESSION)),
        MOTION_AR_VALUE_LIC_STATUS(new Register(0x8D, 2, Register.Access.R, Register.Target.SESSION)),
        MOTION_CP_VALUE_LIC_STATUS(new Register(0x8E, 2, Register.Access.R, Register.Target.SESSION)),

        RTC_DATE_TIME(new Register(0x90, 4, Register.Access.RW, Register.Target.SESSION)),

        DFU_REBOOT(new Register(0xF0, 1, Register.Access.W, Register.Target.SESSION)),
        POWER_OFF(new Register(0xF2, 1, Register.Access.W, Register.Target.SESSION));

        private Register mapRegister;

        RegistersName(Register r){
            mapRegister=r;
        }

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. In this case, the enum constant's name is returned.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            String strRet = super.toString();
            strRet = strRet.replace("_", " ")
                    .replace("CTRLS", "");

            return  strRet;
        }

        public Register getRegister(){
            return mapRegister;
        }
    }

    /**
     * Returns the request register from the map.
     *
     * @param address register address to find, it must match exactly
     * @param target  register target to find
     * @return the relative register with the specified address and target,
     * the target if a register match the criteria else it return null
     */
    public static @Nullable Register lookUpFromAddress(int address, Register.Target target){
        for (RegistersName rName: RegistersName.values()) {
            Register r = rName.getRegister();
            if (r.getAddress() == address && ((r.getTarget() == target) || (r.getTarget() == Register.Target.BOTH)) ) {
                return r;
            }
        }
        return  null;
    }

    /**
     * Returns the request register name from the map
     *
     * @param address register address to find, it must match exactly
     * @param target  register target to find
     * @return the relative register name with the specified address and target,
     * the target if a register match the criteria else it return null
     */
    public static @Nullable RegistersName lookUpRegisterNameFromAddress(int address, Register
            .Target target){
        for (RegistersName rName: RegistersName.values()) {
            Register r = rName.getRegister();
            if (r.getAddress() == address && ((r.getTarget() == target) || (r.getTarget() == Register.Target.BOTH)) ) {
                return rName;
            }
        }
        return  null;
    }

    /**
     * This enum contains the power off mode values for the PowerOff register for STWeSU devices
     */
    public enum PowerOffModes{
        STAND_BY_BLE(253),
        STAND_BY(254),
        REBOOT(252),
        REBOOT_WITH_DEFAULT(251),
        SHUTDOWN(250);

        private int offModeValue;

        /**
         * Returns a int value that represents the relative register value of that modes
         *
         * @return the int value of the power off mode.
         */
        public int getValue(){return offModeValue; }

        PowerOffModes(int nVal){offModeValue = nVal;}
    }


    public static class FwVersionWesu extends FwVersion{
        public FwVersionWesu(Command fwVersionAnswer){
            super(
                    ((fwVersionAnswer.getData()[1] >> 4) & 0x0F),
                    ((fwVersionAnswer.getData()[1]) & 0x0F),
                    (fwVersionAnswer.getData()[0] & 0xFF)
            );
        }
    }

}
