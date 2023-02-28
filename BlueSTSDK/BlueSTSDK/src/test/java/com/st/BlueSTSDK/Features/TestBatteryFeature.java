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
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;

public class TestBatteryFeature {

    @Test
    public void testNullSampleBatteryLevel(){
        Assert.assertEquals(Float.NaN, FeatureBattery.getBatteryLevel(null), 0.0f);
    }

    @Test
    public void testInvalidSampleBatteryLevel(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureBattery.getBatteryLevel(s), 0.0f);
    }

    @Test
    public void testSampleBatteryLevel(){
        float x = 20.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureBattery.getBatteryLevel(s), 0.0f);
    }

    @Test
    public void testNullSampleVoltage(){
        Assert.assertEquals(Float.NaN,FeatureBattery.getVoltage(null),0.0f);
    }

    @Test
    public void testInvalidSampleVoltage(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0}, new Field[]{});
        Assert.assertEquals(Float.NaN,FeatureBattery.getVoltage(s),0.0f);
    }

    @Test
    public void testGetSampleVoltage(){
        float y = 5.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,y}, new Field[]{});
        Assert.assertEquals(y, FeatureBattery.getVoltage(s), 0.0f);
    }

    @Test
    public void testNullSampleCurrent(){
        Assert.assertEquals(Float.NaN,FeatureBattery.getCurrent(null),0.0f);
    }

    @Test
    public void testInvalidSampleCurrent(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0}, new Field[]{});
        Assert.assertEquals(Float.NaN,FeatureBattery.getCurrent(s),0.0f);
    }

    @Test
    public void testGetSampleCurrent(){
        float z = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,z}, new Field[]{});
        Assert.assertEquals(z, FeatureBattery.getCurrent(s), 0.0f);
    }

    @Test
    public void testNullSampleStatus(){
        Assert.assertEquals(FeatureBattery.BatteryStatus.Error,
                FeatureBattery.getBatteryStatus(null));
    }

    @Test
    public void testInvalidSampleStatus(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,0}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Error,
                FeatureBattery.getBatteryStatus(s));
    }

    @Test
    public void testGetSampleStatus(){
        int status = 0x03;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,0,status}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Charging,
                FeatureBattery.getBatteryStatus(s));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureBattery(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureBattery(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6,7}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureBattery(null);
        int offset = 0;
        byte data[] = new byte[offset+7];

        float batteryLevel=43.4f;
        float current=4.2f,voltage=6.0f;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(batteryLevel*10));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(current*1000));
        data[offset+2]=temp[0];
        data[offset+3]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(voltage*100));
        data[offset+4]=temp[0];
        data[offset+5]=temp[1];

        byte batteryStatus=0x01;
        data[offset+6]=batteryStatus;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);


        Assert.assertEquals(2, f.getSample().timestamp);
        Number extractData[] = f.getSample().data;
        Assert.assertEquals(batteryLevel, extractData[0].floatValue(),0.0f);
        Assert.assertEquals(current, extractData[1].floatValue(),0.0f);
        Assert.assertEquals(voltage*100, extractData[2].floatValue(), 0.0f); //is in millivolt
        Assert.assertEquals(batteryStatus, extractData[3].byteValue());

    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureBattery(null);
        int offset = 4;
        byte data[] = new byte[offset+7];

        float batteryLevel=56.4f;
        float current=1.2f,voltage=5.0f;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(batteryLevel*10));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(current*1000));
        data[offset+2]=temp[0];
        data[offset+3]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(voltage*100));
        data[offset+4]=temp[0];
        data[offset+5]=temp[1];

        byte batteryStatus=0x01;
        data[offset+6]=batteryStatus;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);


        Assert.assertEquals(2, f.getSample().timestamp);
        Number extractData[] = f.getSample().data;
        Assert.assertEquals(batteryLevel, extractData[0].floatValue(),0.0f);
        Assert.assertEquals(current, extractData[1].floatValue(),0.0f);
        Assert.assertEquals(voltage*100, extractData[2].floatValue(),0.0f); //is in millivolt
        Assert.assertEquals(batteryStatus, extractData[3].byteValue());

    }

    @Test
    public void testStatusValue(){

        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,0,0x00}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.LowBattery,
                FeatureBattery.getBatteryStatus(s));

        s = new Feature.Sample(100,new Number[]{0,0,0,0x01}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Discharging,
                FeatureBattery.getBatteryStatus(s));

        s = new Feature.Sample(100,new Number[]{0,0,0,0x02}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.PluggedNotCharging,
                FeatureBattery.getBatteryStatus(s));

        s = new Feature.Sample(100,new Number[]{0,0,0,0x03}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Charging,
                FeatureBattery.getBatteryStatus(s));

        s = new Feature.Sample(100,new Number[]{0,0,0,0xFF}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Error,
                FeatureBattery.getBatteryStatus(s));

        //other value are errors
        s = new Feature.Sample(100,new Number[]{0,0,0,0x12}, new Field[]{});
        Assert.assertEquals(FeatureBattery.BatteryStatus.Error,
                FeatureBattery.getBatteryStatus(s));
    }

}

