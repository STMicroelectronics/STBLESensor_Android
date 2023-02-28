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
package com.st.BlueSTSDK.Features.standardCharacteristics;

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * Feature that manage the Heart rate data as defined by the bluetooth specification
 * @see <a href="https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer
 * .aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml">Heart Rate Measurement Specs</a>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureHeartRate extends DeviceTimestampFeature {

    private static final String FEATURE_NAME = "Heart Rate";

    public static int HEART_RATE_INDEX=0;
    protected static final Field HEART_RATE_FIELD = new Field(
            "Heart Rate Measurement","bpm", Field.Type.UInt16,0,1<<16);

    public static int ENERGY_EXPENDED_INDEX=1;
    protected static final Field ENERGY_EXPENDED_FIELD = new Field(
            "Energy Expended","kJ", Field.Type.UInt16,0,1<<16);

    public static int RR_INTERVAL_INDEX=2;
    protected static final Field RR_INTERVAL_FIELD = new Field(
            "RR-Interval","s", Field.Type.Float,0,Float.MAX_VALUE);


    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeatureHeartRate(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                HEART_RATE_FIELD,
                ENERGY_EXPENDED_FIELD,
                RR_INTERVAL_FIELD
        });
    }

    /**
     * extract the hart rate from the sample
     * @param s sample
     * @return heart rate or a negative number if not present
     */
    public static int getHeartRate(Sample s) {
        if(s!=null)
            if (s.data.length > HEART_RATE_INDEX)
                if (s.data[HEART_RATE_INDEX] != null)
                    return s.data[HEART_RATE_INDEX].intValue();
        //else
        return -1;
    }//getHeartRate

    /**
     * extract the energy extended field from the sample,
     * @param s sample
     * @return energy extended or a negative number if not present
     */
    public static int getEnergyExtended(Sample s) {
        if(s!=null)
            if (s.data.length > ENERGY_EXPENDED_INDEX)
                if (s.data[ENERGY_EXPENDED_INDEX] != null)
                    return s.data[ENERGY_EXPENDED_INDEX].intValue();
        //else
        return -1;
    }//getEnergyExtended

    /**
     * extract the rr interval field from the sample
     * @param s sample
     * @return rr interval or nan if not present
     */
    public static float getRRInterval(Sample s) {
        if(s!=null)
            if (s.data.length > RR_INTERVAL_INDEX)
                if (s.data[RR_INTERVAL_INDEX] != null)
                    return s.data[RR_INTERVAL_INDEX].floatValue();
        //else
        return Float.NaN;
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");

        int heartRate, energyExpended;
        float rrInterval;
        int offset = dataOffset;

        byte flags = data[offset++];
        if (has8BitHeartRate(flags)) {
            heartRate = data[offset++];
        }else {
            heartRate = NumberConversion.LittleEndian.bytesToUInt16(data, offset);
            offset+=2;
        }

        if(hasEnergyExpended(flags)){
            energyExpended = NumberConversion.LittleEndian.bytesToUInt16(data,offset);
            offset+=2;
        }else{
            energyExpended=-1;
        }

        if(hasRRInterval(flags)){
            rrInterval = NumberConversion.LittleEndian.bytesToUInt16(data,offset)/1024.0f;
            offset+=2;
        }else{
            rrInterval=Float.NaN;
        }

         return new ExtractResult(
                 new Sample(timestamp,new Number[]{heartRate,energyExpended,rrInterval},
                         getFieldsDesc()),
                 offset-dataOffset);
    }

    private static boolean has8BitHeartRate(byte flags){
        return (flags & 0x01) == 0;
    }

    private static boolean hasEnergyExpended(byte flags){
        return (flags & 0x08)!=0;
    }

    private static boolean hasRRInterval(byte flags){
        return (flags & 0x10)!=0;
    }
}
