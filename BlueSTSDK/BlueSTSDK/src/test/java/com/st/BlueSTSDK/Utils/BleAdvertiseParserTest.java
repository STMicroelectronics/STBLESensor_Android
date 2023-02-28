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
package com.st.BlueSTSDK.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.BuildConfig;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.advertise.BleAdvertiseInfo;
import com.st.BlueSTSDK.Utils.advertise.InvalidBleAdvertiseFormat;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,manifest = "src/main/AndroidManifest.xml", sdk = 23)
public class BleAdvertiseParserTest {

    private static @Nullable
    BleAdvertiseInfo parse(byte data[]){
        return new BlueSTSDKAdvertiseFilter().filter(data);
    }

    @Test
    public void testTransmissionPower(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,0x40 /* 64dB? */, // Transmission Power
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(64,info.getTxPower());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,(byte)0x7F /* 127? */, // Trasmission Power
        };
        info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(127,info.getTxPower());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,(byte)0x80 /* -128 */, // Trasmission Power
        };
        info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(-128,info.getTxPower());
    }

    @Test
    public void testBoardName(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x09,'c'
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals("c",info.getName());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                6,0x09,'h','e','l','l','o'
        };
        info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals("hello",info.getName());
    }

    @Test
    public void testProtocolVersion(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x00,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(1,info.getProtocolVersion());
    }

    @Test
    public void testWrongProtocolVersion(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0xFF,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNull(info);
    }

    @Test
    public void testBoardAddress(){
        byte advertise[] = new byte[]{
                13,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00,(byte)0xEF, (byte)0xBE, (byte)0x00, (byte)0xAD, (byte)0xDE, (byte)0x02
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals("EF:BE:00:AD:DE:02",info.getAddress());
    }

    @Test
    public void testBoardAddressAbsent(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertNull(info.getAddress());
    }

    @Test
    public void testVendorField(){
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(0,info.getFeatureMap());
        Assert.assertEquals(Node.Type.NUCLEO,info.getBoardType());
        Assert.assertEquals((byte)0x80,info.getDeviceId());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(0xFF0000,info.getFeatureMap());
        Assert.assertEquals( Node.Type.GENERIC,info.getBoardType());
        Assert.assertEquals(0x00,info.getDeviceId());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x01, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        info = parse(advertise);
        Assert.assertNotNull(info);
        Assert.assertEquals(0xFFFF0000,info.getFeatureMap());
        Assert.assertEquals(Node.Type.STEVAL_WESU1,info.getBoardType() );
        Assert.assertEquals(0x01,info.getDeviceId());

    }

    @Test
    public void testVendorFieldSmallSize() {
        byte[] advertise = new byte[]{
                5,(byte) 0xFF, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD,
                0x1B, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD //fake datas
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNull(info);
    }

    @Test
    public void testVendorFieldBigSize() {
        byte[] advertise = new byte[]{
                7, (byte)0xFF, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD, (byte) 0xDE,
                (byte) 0x02,(byte) 0x02
        };
        BleAdvertiseInfo info = parse(advertise);
        Assert.assertNull(info);
    }

    @Test
    public void testEmptyAdvertise(){
        BleAdvertiseInfo info = parse(new byte[]{});
        Assert.assertNull(info);
    }

    @Test
    public void testInvalidNodeType() {
        byte advertise[]  = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x10, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        BleAdvertiseInfo info = parse(new byte[]{});
        Assert.assertNull(info);
    }

    @Test
    public void testNoVendorSpecific() {
        byte advertise[]  = new byte[]{
                //vendor specific is 0xff we have 0xfe
                7,(byte)0xFE,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00,
                6,0x09,'h','e','l','l','o'
        };
        BleAdvertiseInfo info = parse(new byte[]{});
        Assert.assertNull(info);
    }

}