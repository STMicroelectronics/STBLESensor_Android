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

/**
 * Feature that contains a switch status, each bit can be mapped in a single single switch.
 * So a feature can handle up to 8 switch.
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureSwitch extends Feature {

    public static final String FEATURE_NAME = "Switch";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "Status";
    public static final short DATA_MAX = 256;
    public static final short DATA_MIN = 0;

    protected static final Field SWITCH_FILED =
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8, DATA_MAX, DATA_MIN);

    /**
     * build a led status feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureSwitch(Node n) {
        super(FEATURE_NAME, n, new Field[]{SWITCH_FILED});
    }

    protected FeatureSwitch(String name, Node n, Field data[]) {
        super(name,n,data);
        if(data[0]!= SWITCH_FILED){
            throw new IllegalArgumentException("First data[0] must be FeatureSwitch" +
                    ".SWITCH_FILED");
        }//if
    }


    /**
     * extract the led status, each bit is a led status
     * @param sample sensor raw data
     * @return led status data, or 0 if the sample is not valid
     */
    public static byte getSwitchStatus(Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[0] != null)
                    return sample.data[0].byteValue();
        //else
        return 0;
    }

    private static final byte[] EMPTY_COMMAND_DATA = new byte[0];

    /**
     * change the led status to the new one
     * @param newStatus new led status
     * @return true if the command is correctly send
     */
    public boolean changeSwitchStatus(byte newStatus){
        return sendCommand(newStatus,EMPTY_COMMAND_DATA);
    }

    /**
     * extract the temperature data from the node raw data, in this care it read a int16 value
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (2) and data extracted (the temperature information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp,byte[] data, int dataOffset) {
        if (data.length - dataOffset < 1)
            throw new IllegalArgumentException("There are no byte available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                data[dataOffset]
        },getFieldsDesc());
        return new ExtractResult(temp,1);
    }//update

    @Override
    public String toString(){
        //keep a reference for avoid partial update
        Sample sample = mLastSample;
        if(sample==null)
            return super.toString();
        Number data[] = sample.data;
        Field dataDesc[] = getFieldsDesc();
        return String.format(FEATURE_NAME+":\n\tTimestamp: %d\n\t%s: %s",sample.timestamp,
                dataDesc[0].getName(),data[0].byteValue()==0 ? "Off" : "On");
    }
}
