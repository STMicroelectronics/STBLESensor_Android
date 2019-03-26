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

package com.st.blesensor.cloud.util;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class used for serialize a sample to a json object the format is:
 * { timestamp: xxx
 *   filedName0: fieldValue0
 *   ...
 *   filedName0: fieldValue0
 * }
 */
public class JSONSampleSerializer{

    public static final String TIMESTAMP = "timestamp";

    /**
     * put a scalar field to the root object
     * @param obj root object
     * @param field filed to add
     * @param value value to add
     * @throws JSONException
     */
    private static void addScalarField(JSONObject obj, Field field, Number value)
            throws JSONException {
        String fieldName = field.getName();
        switch (field.getType()){
            case Float:
                obj.put(fieldName,value.floatValue());
                break;
            case Int64:
            case UInt32:
                obj.put(fieldName,value.longValue());
                break;
            case Int32:
            case UInt16:
                obj.put(fieldName,value.intValue());
                break;
            case Int16:
            case UInt8:
                obj.put(fieldName,value.shortValue());
                break;
            case Int8:
                obj.put(fieldName,value.byteValue());
                break;
            case ByteArray: //is not a scalar type!
                break;
        }//switch
    }//addScalarField

    /**
     * add an array of value to the object
     * @param obj root object
     * @param field name of the filed
     * @param data data to add
     * @param startIndex index where find the fist valid data
     * @throws JSONException
     */
    private static void addArrayField(JSONObject obj,Field field,Number data[], int startIndex)
            throws JSONException {
        String fieldName = field.getName();
        for(int i=startIndex;i<data.length;i++){
            obj.accumulate(fieldName,data[i].byteValue());
        }
    }


    /**
     * serialize a sample object in a json object
     * @param sample object to serialize
     * @return equivalent json object
     * @throws JSONException
     */
    public static JSONObject serialize(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(TIMESTAMP,sample.timestamp);
        int nField = sample.data.length;
        for(int i=0;i<nField;i++){
            Field field = sample.dataDesc[i];
            if(field.getType() == Field.Type.ByteArray){
                addArrayField(obj,field,sample.data,i);
                break;
            }else
                addScalarField(obj,field,sample.data[i]);
        }
        return obj;
    }

    //needed for the junit tests
    private JSONSampleSerializer(){}

}
