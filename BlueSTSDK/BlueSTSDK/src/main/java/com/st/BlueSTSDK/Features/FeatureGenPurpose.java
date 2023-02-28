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

import android.bluetooth.BluetoothGattCharacteristic;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

/**
 * It is a special feature that is used during the development and doesn't follow the sdk schema.
 * <ul>
 *     <li>the general purpose feature are not present in the advertise bitmask</li>
 *     <li>you can't send a command to a gp feature</li>
 *     <li>the length of the array returned by getFieldsDesc (it is always 1) can be different
 *     from the  length of the array returned from getFieldsData</li>
 * </ul>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureGenPurpose extends Feature {

    public static final String FEATURE_UNIT=null;
    public static final String FEATURE_DATA_NAME ="RawData";
    public static final byte DATA_MAX = 127;
    public static final byte DATA_MIN = -127;

    /**
     * characteristics that will export this data
     */
    private BluetoothGattCharacteristic mChar; //we can store the characteristics inside the
    // feature because we have one characteristics for each general purpose feature, for the
    // normal feature the data can be exported by multiple characteristics

    /**
     *
     * @param n node that export this data
     * @param characteristics characteristics that export this data
     */
    public FeatureGenPurpose(Node n, BluetoothGattCharacteristic characteristics){
        super("GenPurpose_"+characteristics.getUuid().toString().substring(0,Math.min(8,characteristics.getUuid().toString().length())),n,new Field[]{
                new Field(FEATURE_DATA_NAME,FEATURE_UNIT, Field.Type.Int8,DATA_MAX,DATA_MIN)
        });
        mChar=characteristics;
    }//FeatureGenPurpose

    /**
     * convert an array of Number in an array of byte
     * @param s array of data returned by the function getFeatureData
     * @return array of byte
     */
    public static byte[] getRawData(Sample s){
        if(s==null)
            return null;

        byte rawData[] = new byte[s.data.length];
        for(int i=0;i<s.data.length;i++){
            rawData[i]=s.data[i].byteValue();
        }//for i

        return rawData;
    }//getRawData

    /**
     * get the characteristics that is associated with this general purpose
     * @return characteristics to query for have update data
     */
    public BluetoothGattCharacteristic getFeatureChar(){
        return mChar;
    }

    @Override
    protected ExtractResult extractData(long timestamp,byte[] data, int offsetData) {

        Number dataObj[] = new Number[data.length-offsetData];
        for(int i=offsetData;i<data.length;i++){
            dataObj[i-offsetData]=data[i];
        }//for i

        return new ExtractResult(new Sample(timestamp,dataObj,getFieldsDesc()),
                data.length-offsetData);

    }//update

    @Override
    public String toString(){
        //create the string with the feature data
        StringBuilder sb = new StringBuilder();
        Sample sample = mLastSample;
        sb.append(getName()).append(":\n\tTimestamp: ").append(sample.timestamp).append('\n');
        sb.append('\t').append(FEATURE_DATA_NAME).append(": ");
        Number data[] = sample.data;
        for (Number n: data) {
            sb.append(String.format("%X ",n.byteValue()));
        }//for
        sb.append('\n');

        return sb.toString();
    }

}
