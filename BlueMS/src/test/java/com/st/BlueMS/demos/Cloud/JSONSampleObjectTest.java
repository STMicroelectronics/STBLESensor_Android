/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueMS.demos.cloud;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JSONSampleObjectTest {

    private Feature.Sample mSample;
    private JSONObject mObj;


    private static final double EQUAL_PRECISION=1e-5;
    private static final String FLOAT_FILED_NAME="Float";
    private static final float FLOAT_FILED_VALUE=(float)Math.PI;

    private static final String INT64_FILED_NAME="INT64";
    private static final long INT64_FILED_VALUE=Long.MIN_VALUE;

    private static final String UINT32_FILED_NAME="UINT32";
    private static final long UINT32_FILED_VALUE=(1L<<32)-1;

    private static final String INT32_FILED_NAME="INT32";
    private static final long INT32_FILED_VALUE=Integer.MIN_VALUE;

    private static final String UINT16_FILED_NAME="UINT16";
    private static final long UINT16_FILED_VALUE=(1L<<16)-1;

    private static final String INT16_FILED_NAME="INT16";
    private static final long INT16_FILED_VALUE=Short.MIN_VALUE;

    private static final String UINT8_FILED_NAME="UINT8";
    private static final long UINT8_FILED_VALUE=(1L<<8)-1;

    private static final String INT8_FILED_NAME="INT8";
    private static final long INT8_FILED_VALUE=Byte.MIN_VALUE;

    private static final String BYTE_ARRAY_FILED_NAME="BYTE_ARRAY";
    private static final Byte BYTE_ARRAY_FILED_VALUE[]=new Byte[]{1,2,3,4};


    @Before
    public void createComplexJSONObject() throws JSONException {
        mSample = new Feature.Sample(Long.MAX_VALUE,
                new Number[]{
                        FLOAT_FILED_VALUE,
                        INT64_FILED_VALUE,
                        UINT32_FILED_VALUE,
                        INT32_FILED_VALUE,
                        UINT16_FILED_VALUE,
                        INT16_FILED_VALUE,
                        UINT8_FILED_VALUE,
                        INT8_FILED_VALUE
                },
                new Field[]{
                    new Field(FLOAT_FILED_NAME,null, Field.Type.Float,0,0),
                    new Field(INT64_FILED_NAME,null, Field.Type.Int64,0,0),
                    new Field(UINT32_FILED_NAME,null, Field.Type.UInt32,0,0),
                    new Field(INT32_FILED_NAME,null, Field.Type.Int32,0,0),
                    new Field(UINT16_FILED_NAME,null, Field.Type.UInt16,0,0),
                    new Field(INT16_FILED_NAME,null, Field.Type.Int16,0,0),
                    new Field(UINT8_FILED_NAME,null, Field.Type.UInt8,0,0),
                    new Field(INT8_FILED_NAME,null, Field.Type.Int8,0,0)
                });
        mObj = JSONSampleSerializer.serialize(mSample);
    }

    @Test
    public void jsonHasFieldTimestamp() throws JSONException {
        Assert.assertTrue(mObj.has(JSONSampleSerializer.TIMESTAMP));
        Assert.assertEquals(mSample.timestamp,mObj.getLong(JSONSampleSerializer.TIMESTAMP));
    }

    @Test
    public void eachFieldHasAJsonObject(){
        Assert.assertEquals(mSample.dataDesc.length+1,mObj.length());
    }

    @Test
    public void floatFieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(FLOAT_FILED_NAME));
        Assert.assertEquals(FLOAT_FILED_VALUE,mObj.getDouble(FLOAT_FILED_NAME),EQUAL_PRECISION);
    }

    @Test
    public void Int64FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(INT64_FILED_NAME));
        Assert.assertEquals(INT64_FILED_VALUE,mObj.getLong(INT64_FILED_NAME));
    }

    @Test
    public void UInt32FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(UINT32_FILED_NAME));
        Assert.assertEquals(UINT32_FILED_VALUE,mObj.getLong(UINT32_FILED_NAME));
    }

    @Test
    public void Int32FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(INT32_FILED_NAME));
        Assert.assertEquals(INT32_FILED_VALUE,mObj.getInt(INT32_FILED_NAME));
    }

    @Test
    public void UInt16FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(UINT16_FILED_NAME));
        Assert.assertEquals(UINT16_FILED_VALUE,mObj.getInt(UINT16_FILED_NAME));
    }

    @Test
    public void Int16FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(INT16_FILED_NAME));
        Assert.assertEquals(INT16_FILED_VALUE,mObj.getInt(INT16_FILED_NAME));
    }

    @Test
    public void UInt8FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(UINT8_FILED_NAME));
        Assert.assertEquals(UINT8_FILED_VALUE,mObj.getInt(UINT8_FILED_NAME));
    }

    @Test
    public void Int8FieldIsPresent() throws JSONException {
        Assert.assertTrue(mObj.has(INT8_FILED_NAME));
        Assert.assertEquals(INT8_FILED_VALUE,mObj.getInt(INT8_FILED_NAME));
    }

    @Test
    public void ByteArrayFieldIsPresent() throws JSONException {

        Feature.Sample arraySample = new Feature.Sample(Long.MAX_VALUE,
                        BYTE_ARRAY_FILED_VALUE,
                new Field[]{
                        new Field(BYTE_ARRAY_FILED_NAME,null, Field.Type.ByteArray,0,0),
                });
        JSONObject obj = JSONSampleSerializer.serialize(arraySample);

        Assert.assertTrue(obj.has(BYTE_ARRAY_FILED_NAME));
        JSONArray dataArray = obj.getJSONArray(BYTE_ARRAY_FILED_NAME);

        Assert.assertEquals(BYTE_ARRAY_FILED_VALUE.length,dataArray.length());
        for(int i=0;i<BYTE_ARRAY_FILED_VALUE.length;i++){
            byte temp = (byte)dataArray.getInt(i);
            Assert.assertEquals(BYTE_ARRAY_FILED_VALUE[i],Byte.valueOf(temp));
        }
    }

    @Test
    public void ByteFieldAndByteArrayFieldIsPresent() throws JSONException {

        Feature.Sample arraySample = new Feature.Sample(Long.MAX_VALUE,
                BYTE_ARRAY_FILED_VALUE,
                new Field[]{
                        new Field(INT8_FILED_NAME,null, Field.Type.Int8,0,0),
                        new Field(BYTE_ARRAY_FILED_NAME,null, Field.Type.ByteArray,0,0),
                });
        JSONObject obj = JSONSampleSerializer.serialize(arraySample);

        Assert.assertTrue(obj.has(INT8_FILED_NAME));
        Assert.assertEquals((int)BYTE_ARRAY_FILED_VALUE[0],obj.getInt(INT8_FILED_NAME));

        Assert.assertTrue(obj.has(BYTE_ARRAY_FILED_NAME));

        JSONArray dataArray = obj.getJSONArray(BYTE_ARRAY_FILED_NAME);

        Assert.assertEquals(BYTE_ARRAY_FILED_VALUE.length-1,dataArray.length());
        for(int i=0;i<BYTE_ARRAY_FILED_VALUE.length-1;i++){
            byte temp = (byte)dataArray.getInt(i);
            Assert.assertEquals(BYTE_ARRAY_FILED_VALUE[i+1],Byte.valueOf(temp));
        }
    }

}
