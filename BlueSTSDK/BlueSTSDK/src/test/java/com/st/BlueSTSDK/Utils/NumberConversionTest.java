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

import org.junit.Assert;
import org.junit.Test;


public class NumberConversionTest {

    @Test
    public void testByteToUInt8() throws Exception {
        short number = NumberConversion.byteToUInt8(new byte[]{0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.byteToUInt8(new byte[]{14});
        Assert.assertEquals(14, number);

        number = NumberConversion.byteToUInt8(new byte[]{(byte) 0x7F});
        Assert.assertEquals(127, number);

        number = NumberConversion.byteToUInt8(new byte[]{(byte) 0xFF});
        Assert.assertEquals(255, number);
    }

    @Test
    public void testInt16ToBytesLE() {

        byte[] b = NumberConversion.LittleEndian.int16ToBytes((short) 0x0000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int16ToBytes((short) 0x1000); //MIN_VALUE
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x10}, b);

        b = NumberConversion.LittleEndian.int16ToBytes((short) 0xFFFF); //-1
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, b);

        b = NumberConversion.LittleEndian.int16ToBytes((short) 0x00FF);
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int16ToBytes((short) 0x7FFF); //MAX VALUE
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x7F}, b);
    }

    @Test
    public void testInt16ToBytesBE() {

        byte[] b = NumberConversion.BigEndian.int16ToBytes((short) 0x0000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int16ToBytes((short) 0x1000); //MIN_VALUE
        Assert.assertArrayEquals(new byte[]{(byte) 0x10, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int16ToBytes((short) 0xFFFF); //-1
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, b);

        b = NumberConversion.BigEndian.int16ToBytes((short) 0x00FF);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0xFF}, b);

        b = NumberConversion.BigEndian.int16ToBytes((short) 0x7FFF); //MAX VALUE
        Assert.assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF}, b);
    }


    @Test
    public void bytesToUInt16LE() {

        int number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0x7F});
        Assert.assertEquals(32512, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(65280, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0xFF, (byte) 0x7F});
        Assert.assertEquals(32767, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(65535, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0x7F, (byte) 0x00});
        Assert.assertEquals(127, number);

        number = NumberConversion.LittleEndian.bytesToUInt16(new byte[]{(byte) 0xFF, (byte) 0x00});
        Assert.assertEquals(255, number);
    }

    @Test
    public void bytesToUInt16BE() {

        int number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0x7F, (byte) 0x00});
        Assert.assertEquals(32512, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0xFF, (byte) 0x00});
        Assert.assertEquals(65280, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0x7F, (byte) 0xFF});
        Assert.assertEquals(32767, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(65535, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0x7F});
        Assert.assertEquals(127, number);

        number = NumberConversion.BigEndian.bytesToUInt16(new byte[]{(byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(255, number);
    }

    @Test
    public void bytesToInt16LE() {

        short number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(-1, number);

        number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0xFF, (byte) 0x7F});
        Assert.assertEquals(32767, number);

        number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0x00, (byte) 0x80});
        Assert.assertEquals(-32768, number);

        number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0xFF, (byte) 0x00});
        Assert.assertEquals(255, number);

        number = NumberConversion.LittleEndian.bytesToInt16(new byte[]{(byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(-256, number);
    }

    @Test
    public void bytesToInt16BE() {

        short number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(-1, number);

        number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0x7F, (byte) 0xFF});
        Assert.assertEquals(32767, number);

        number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0x80, (byte) 0x00});
        Assert.assertEquals(-32768, number);

        number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(255, number);

        number = NumberConversion.BigEndian.bytesToInt16(new byte[]{(byte) 0xFF, (byte) 0x00});
        Assert.assertEquals(-256, number);
    }

    @Test
    public void testInt32ToBytesLE() {

        byte[] b = NumberConversion.LittleEndian.int32ToBytes(0x00000000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int32ToBytes(0xFF000000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF}, b);

        b = NumberConversion.LittleEndian.int32ToBytes(0x00FF0000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int32ToBytes(0x0000FF00);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int32ToBytes(0x000000FF);
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.LittleEndian.int32ToBytes(0x12345678);
        Assert.assertArrayEquals(new byte[]{(byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12}, b);
    }

    @Test
    public void testInt32ToBytesBE() {

        byte[] b = NumberConversion.BigEndian.int32ToBytes(0x00000000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int32ToBytes(0x000000FF);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF}, b);

        b = NumberConversion.BigEndian.int32ToBytes(0x0000FF00);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int32ToBytes(0x00FF0000);
        Assert.assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int32ToBytes(0xFF000000);
        Assert.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00}, b);

        b = NumberConversion.BigEndian.int32ToBytes(0x12345678);
        Assert.assertArrayEquals(new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78}, b);
    }

    @Test
    public void bytesToUInt32LE() {

        long number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(255, number);

        number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(4294901760L, number);

        number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});
        Assert.assertEquals(2147483648L, number);

        number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F});
        Assert.assertEquals(2147483647L, number);

        number = NumberConversion.LittleEndian.bytesToUInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(4294967295L, number);

    }

    @Test
    public void bytesToUInt32BE() {

        long number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(255, number);

        number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(4294901760L, number);

        number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(2147483648L, number);

        number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(2147483647L, number);

        number = NumberConversion.BigEndian.bytesToUInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(4294967295L, number);

    }

    @Test
    public void bytesToInt32LE() {

        int number = NumberConversion.LittleEndian.bytesToInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.LittleEndian.bytesToInt32(
                new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(255, number);

        number = NumberConversion.LittleEndian.bytesToInt32(
                new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(-256, number);

        number = NumberConversion.LittleEndian.bytesToInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});
        Assert.assertEquals(-2147483648, number);

        number = NumberConversion.LittleEndian.bytesToInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F});
        Assert.assertEquals(2147483647, number);
    }

    @Test
    public void bytesToInt32BE() {

        int number = NumberConversion.BigEndian.bytesToInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0, number);

        number = NumberConversion.BigEndian.bytesToInt32(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF});
        Assert.assertEquals(255, number);

        number = NumberConversion.BigEndian.bytesToInt32(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00});
        Assert.assertEquals(-256, number);

        number = NumberConversion.BigEndian.bytesToInt32(
                new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(-2147483648, number);

        number = NumberConversion.BigEndian.bytesToInt32(
                new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        Assert.assertEquals(2147483647, number);
    }

    private static final float FLOAT_DELTA=1e-6f;

    @Test
    public void bytesToFloatBE(){

        float number = NumberConversion.BigEndian.bytesToFloat(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0.0f, number, FLOAT_DELTA);

        //+1 = 0x3F800000
        number = NumberConversion.BigEndian.bytesToFloat(
                new byte[]{(byte) 0x3F, (byte) 0x80, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(1f, number,FLOAT_DELTA);

        //-1 = 0xBF800000
        number = NumberConversion.BigEndian.bytesToFloat(
                new byte[]{(byte) 0xBF, (byte) 0x80, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(-1f, number,FLOAT_DELTA);

        //3.14159265359 = 0x40490FDB
        number = NumberConversion.BigEndian.bytesToFloat(
                new byte[]{(byte) 0x40, (byte) 0x49, (byte) 0x0F, (byte) 0xDB});
        Assert.assertEquals(3.14159265359, number,FLOAT_DELTA);

        //12345.6789 = 0x4640E6B7
        number = NumberConversion.BigEndian.bytesToFloat(
                new byte[]{(byte) 0x46, (byte) 0x40, (byte) 0xE6, (byte) 0xB7});
        Assert.assertEquals(12345.6789f, number,FLOAT_DELTA);

    }

    @Test
    public void bytesToFloatLE(){

        float number = NumberConversion.LittleEndian.bytesToFloat(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        Assert.assertEquals(0.0f, number, FLOAT_DELTA);

        number = NumberConversion.LittleEndian.bytesToFloat(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x3F});
        Assert.assertEquals(1.0f, number,FLOAT_DELTA);

        number = NumberConversion.LittleEndian.bytesToFloat(
                new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0xBF});
        Assert.assertEquals(-1.0f, number,FLOAT_DELTA);

        //3.14159265359 = 0x40490FDB
        number = NumberConversion.LittleEndian.bytesToFloat(
                new byte[]{(byte) 0xDB, (byte) 0x0F, (byte) 0x49, (byte) 0x40});
        Assert.assertEquals(3.14159265359, number,FLOAT_DELTA);

    }
}
 

