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
 * Feature that contains the sound angle of arrival.
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureDirectionOfArrival extends Feature {

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "Direction of Arrival";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "\u00B0"; //degree angle
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Angle";
    /**
     * max angle
     */
    public static final short DATA_MAX = 360;
    /**
     * min angle
     */
    public static final short DATA_MIN = 0;

    //NOTE SL - sensitivity ////////////////////////////////////////////////////////////////////////
    /** Source Localization command Type*/
    private static final byte SL_COMMAND_SENSITIVITY = (byte)0xCC;
    /** Disable Source Localization command*/
    private static final byte COMMAND_SENS_LOW[] = {0x00};
    /** Enable Source Localization command*/
    private static final byte COMMAND_SENS_HIGH[] = {0x01};
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureDirectionOfArrival(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt16,
                                DATA_MAX, DATA_MIN),
                });
    }//FeatureMicLevel

    /**
     * Get audio direction
     *
     * @param s sample from the sensor
     * @return direction of arrival of the sound
     */
    public static short getSoundAngle(Feature.Sample s) {
        if(hasValidIndex(s,0))
            return s.data[0].shortValue();
        //else
        return Short.MIN_VALUE;
    }//getAccY

    /**
     * the number of microphone is not fixed so this function will read all the available data
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return data sample and the number of read bytes
     */
    @Override
    protected Feature.ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length-dataOffset < 2)
            throw new IllegalArgumentException("There are no more than 2 byte available to read");

        short dataShort = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset);

        dataShort = normalizeAngle(dataShort);

        Sample temp = new Sample(timestamp,new Number[]{dataShort},getFieldsDesc());
        return new ExtractResult(temp,2);
    }//extractData


    /**
     * add or subtract 360 to obtain an angle in the range 0 360
     * @param angle angle to normalize
     * @return angle between 0 and 360
     */
    private static short normalizeAngle(short angle){
        while (angle<0){
            angle+=360;
        }
        while (angle>360){
            angle-=360;
        }

        return angle;
    }

    /**
     * enable/disable high sensitivity mode
     * @param newStatus new sensitivity status
     * @return true if the command is correctly send
     */
    public boolean enableLowSensitivity(boolean newStatus){
        if(newStatus)
            return sendCommand(SL_COMMAND_SENSITIVITY,COMMAND_SENS_LOW);
        else
            return sendCommand(SL_COMMAND_SENSITIVITY,COMMAND_SENS_HIGH);
    }
}
