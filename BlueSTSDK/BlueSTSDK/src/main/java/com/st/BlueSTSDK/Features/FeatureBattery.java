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
package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * Feature that will contain the battery data, this is not the standard battery since will
 * contain more information.
 *
 * The information exported by this class are:
 * <ul>
 *     <li>Battery Level, % of remaining charge with one decimal position</li>
 *     <li>Voltage, battery voltage, in Volt with 3 decimal position</li>
 *     <li>Current, electric current that is used by the board, with one decimal position in mA</li>
 *     <li>Status, tell if the battery is charging, discharging or in low battery </li>
 * </ul>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.1
 */
public class FeatureBattery extends Feature {

    /** feature name */
    public static final String FEATURE_NAME = "Battery";
    /** unit of the data exported by this feature */
    public static final String[] FEATURE_UNIT = {"%", "V", "mA", null};
    /** name of the data exported by this feature */
    public static final String[] FEATURE_DATA_NAME = {"Level", "Voltage", "Current", "Status"};
    /** maximum value for the feature data */
    public static final short[] DATA_MAX = {100, 10, 10, 0xFF};
    /** minimum value for the feature data */
    public static final short[] DATA_MIN = {0, -10, -10, 0};

    /** index where you can find the percentage value/description */
    public static final int PERCENTAGE_INDEX = 0;
    /** index where you can find the voltage value/description */
    public static final int VOLTAGE_INDEX = 1;
    /** index where you can find the current value/description */
    public static final int CURRENT_INDEX = 2;
    /** index where you can find the status value/description */
    public static final int STATUS_INDEX = 3;

    private static final byte COMMAND_GET_BATTERY_CAPACITY = 0x01;
    private static final byte COMMAND_GET_MAX_ASSORBED_CURRENT = 0X02;
    private static final short UNKOWN_CURRENT_VALUE = Short.MIN_VALUE;

    /**
     * create a feature Battery
     * @param n node where the feature will read the data
     */
    public FeatureBattery(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[PERCENTAGE_INDEX], FEATURE_UNIT[PERCENTAGE_INDEX],
                        Field.Type.Float, DATA_MAX[PERCENTAGE_INDEX], DATA_MIN[PERCENTAGE_INDEX]),
                new Field(FEATURE_DATA_NAME[VOLTAGE_INDEX], FEATURE_UNIT[VOLTAGE_INDEX],
                        Field.Type.Float, DATA_MAX[VOLTAGE_INDEX], DATA_MIN[VOLTAGE_INDEX]),
                new Field(FEATURE_DATA_NAME[CURRENT_INDEX], FEATURE_UNIT[CURRENT_INDEX],
                        Field.Type.Float, DATA_MAX[CURRENT_INDEX], DATA_MIN[CURRENT_INDEX]),
                new Field(FEATURE_DATA_NAME[STATUS_INDEX], FEATURE_UNIT[STATUS_INDEX],
                        Field.Type.UInt8, DATA_MAX[STATUS_INDEX], DATA_MIN[STATUS_INDEX]),
        });
    }//FeatureBattery

    /**
     * extract the battery level from the data exported by this feature
     * @param s data exported by this feature
     * @return percentage of charge inside the battery, or nan if the data are not valid
     */
    public static float getBatteryLevel(Sample s) {
        if(hasValidIndex(s,PERCENTAGE_INDEX))
            return s.data[PERCENTAGE_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getBatteryLevel

    /**
     * extract the battery voltage from the data exported by this feature
     * @param s data exported by this feature
     * @return battery voltage , or nan if the data are not valid
     */
    public static float getVoltage(Sample s) {
        if(hasValidIndex(s,VOLTAGE_INDEX))
            return s.data[VOLTAGE_INDEX].floatValue();
            //else
        return Float.NaN;
    }//getVoltage

    /**
     * extract the current used by the system from the data exported by this feature
     * @param s data exported by this feature
     * @return current used by the system , or nan if the data are not valid
     */
    public static float getCurrent(Sample s) {
        if(hasValidIndex(s,CURRENT_INDEX))
            return s.data[CURRENT_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getCurrent

    /**
     * extract the battery status from the data exported by this feature
     * @param s data exported by this feature
     * @return battery status , or Error if data are not valid
     */
    public static BatteryStatus getBatteryStatus(Sample s) {
        if(!hasValidIndex(s,STATUS_INDEX))
            return BatteryStatus.Error;

        int status = s.data[STATUS_INDEX].byteValue();

        switch (status) {
            case 0x00:
                return BatteryStatus.LowBattery;
            case 0x01:
                return BatteryStatus.Discharging;
            case 0x02:
                return BatteryStatus.PluggedNotCharging;
            case 0x03:
                return BatteryStatus.Charging;
            case 0x04:
                return BatteryStatus.Unknown;
            case 0xFF:
            default:
                return BatteryStatus.Error;
        }//switch
    }//getBatteryStatus


    /**
     * the most significative bit in the status tell us if the current has an high resolution or not
     * @param status battery status
     * @return true if the  MSB is 1 -> we use high precision current, false otherwise
     */
    private static boolean hasHeightResolutionCurrent(short status){
        return (status & 0x80)!=0;
    }

    /**
     * when all the bit of the current are 1, it means unknown value
     * @param status current value extracted from the notification
     * @return true if the current is unknown
     */
    private static boolean hasUnknownCurrent(short status){
        return status == UNKOWN_CURRENT_VALUE;
    }

    /***
     * remove the most MSB for extract only the battery status value
     * @param status battery status
     * @return the status with the MSB set to 0
     */
    private static byte getBatteryStatus(short status){
        return (byte)(status & 0x7F);
    }

    /**
     * convert the current value from the notification
     * @param currentValue current value extracted from the notificaiton
     * @return nan if the current is unknow, otherwise the current used by the device
     */
    private float extractCurrentValue(short currentValue,boolean hightResolution){
        if(hasUnknownCurrent(currentValue))
            return Float.NaN;
        if(hightResolution)
            return (float)currentValue*0.1f; // current/10
        else
            return currentValue;
    }

    /**
     * scale the battery charge percentage
     * @param rawPercentage raw percentage read from the notification
     * @return float percentage clamped between 0 and 100
     */
    private float extractPercentage(short rawPercentage){
        float percentage = rawPercentage/10.0f;
        return Math.max(0.0f, Math.min(100.0f, percentage));
    }

    /**
     * extract the battery information from 7 byte
     * @param data array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (7) and data extracted (the battery information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 7)
            throw new IllegalArgumentException("There are no 7 bytes available to read");

        short tempStatus = NumberConversion.byteToUInt8(data,dataOffset + 6);
        short tempCurrent = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4);
        float current =extractCurrentValue(tempCurrent,hasHeightResolutionCurrent(tempStatus));

        Sample temp = new Sample(timestamp,new Number[]{
                extractPercentage(NumberConversion.LittleEndian.bytesToInt16(data,dataOffset)),
                NumberConversion.LittleEndian.bytesToInt16(data,dataOffset + 2) / 1000.0f,
                current,
                getBatteryStatus(tempStatus)
        },getFieldsDesc());

        return new ExtractResult(temp,7);
    }

    /**
     * Send the command used for read the board battery capacity. The value will be notified with the
     * callback {@link FeatureBattery.FeatureBatteryListener#onCapacityRead(FeatureBattery, int)}
     * @return true if the command is correctly sent
     */
    public boolean readBatteryCapacity(){
        return sendCommand(COMMAND_GET_BATTERY_CAPACITY,new byte[]{});
    }

    /**
     * Send the command used for read the biggest current assorbed by the system.
     * The value will be notified with the callback
     * {@link FeatureBattery.FeatureBatteryListener#onMaxAssorbedCurrentRead(FeatureBattery, float)}
     * @return true if the command is correctly sent
     */
    public boolean readMaxAbsorbedCurrent(){
        return sendCommand(COMMAND_GET_MAX_ASSORBED_CURRENT,new byte[]{});
    }

    /**
     * Notify to all the listener of type FeatureBatteryListener, that the battery capacity was read
     * @param batteryCapacity battery capacity read from the board
     */
    private void notifyBatteryCapacity(final int batteryCapacity) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureBatteryListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureBatteryListener) listener).onCapacityRead(FeatureBattery.this,
                                batteryCapacity);
                    }//run
                });
        }//for
    }//notifyUpdate

    /**
     * Notify to all the listener of type FeatureBatteryListener, that the max current was read
     * @param current max current assorbed by the system
     */
    private void notifyMaxAbsorbedCurrent(final float current) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureBatteryListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureBatteryListener) listener).onMaxAssorbedCurrentRead(FeatureBattery.this,
                                current);
                    }//run
                });
        }//for
    }//notifyUpdate


    /**
     * callback called when we receve an answer to a command
     * @param timeStamp device time stamp of when the response was send
     * @param commandType id of the request that the feature did
     * @param data data attached to the response
     */
    @Override
    protected void parseCommandResponse(int timeStamp, byte commandType, byte[] data) {
        if(commandType == COMMAND_GET_BATTERY_CAPACITY){
            int capacity = NumberConversion.LittleEndian.bytesToUInt16(data);
            notifyBatteryCapacity(capacity);
            return;
        }

        if(commandType == COMMAND_GET_MAX_ASSORBED_CURRENT){
            float current = NumberConversion.LittleEndian.bytesToInt16(data)/10.0f;
            notifyMaxAbsorbedCurrent(current);
            return;
        }
        //else
        super.parseCommandResponse(timeStamp,commandType,data);
    }


    /**
     * Possible battery status
     */
    public enum BatteryStatus {
        /** low battery, when the battery capacity is below a threshold defined by the fw
         * programmer */
        LowBattery,
        /** the battery is discharging (the current is negative) */
        Discharging,
        /** the battery is fully charge and the cable is plugged */
        PluggedNotCharging,
        /** the battery is charging (current is positive */
        Charging,
        /** unknown status */
        Unknown,
        /** internal error or not valid status */
        Error
    }

    /**
     * Listener interface that include the callback for the battery commands
     */
    public interface FeatureBatteryListener extends FeatureListener{

        /***
         * Called when the battery capacity is read
         * @param featureBattery feature where the data was read
         * @param batteryCapacity battery capacity in mAh
         */
        void onCapacityRead(FeatureBattery featureBattery, int batteryCapacity);

        /**
         * Called when the max assorbed current is read
         * @param featureBattery fature where the data was read
         * @param current max current assorbed by the system, in mA
         */
        void onMaxAssorbedCurrentRead(FeatureBattery featureBattery, float current);

    }

}
