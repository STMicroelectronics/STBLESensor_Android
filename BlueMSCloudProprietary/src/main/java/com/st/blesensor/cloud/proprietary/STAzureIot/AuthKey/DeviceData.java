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

package com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


public class DeviceData {

    private static final String DEVICE_ADDRESS_VALIDATE ="[0-9A-F]{12}";
    private static final String MCU_ID_VALIDATE ="[0-9A-F]{24}_[0-9]{3}";

    private static String sanitizeDeviceAdr(String address){
        String sanitizedAddress = address.toUpperCase().replace(":","");
        if(sanitizedAddress.matches(DEVICE_ADDRESS_VALIDATE))
            return sanitizedAddress;
        throw new IllegalArgumentException("deviceAdr is not valid, must match "+DEVICE_ADDRESS_VALIDATE);
    }

    private static long[] strToInt(String mcuId) {
        long[] intMcuId = new long[3];

        for(int i = 0; i < 3; ++i) {
            intMcuId[i] = Long.parseLong(mcuId.substring(i*8, (i + 1) *8), 16);
        }

        return intMcuId;
    }

    private static String sanitizeMcuId(String mcuId){

        String sanitizedId = mcuId.toUpperCase();
        if(!sanitizedId.matches(MCU_ID_VALIDATE))
            throw new IllegalArgumentException("mcuId is not valid, must match "+MCU_ID_VALIDATE);
        String[] splitMcuId = mcuId.split("_");
        mcuId=splitMcuId[0];

        long mcuIdInt[] = strToInt(mcuId);
        //TODO: remove the +n, added to debug
        return String.format("%010d%010d%010d",mcuIdInt[0]+2,mcuIdInt[1]+3,mcuIdInt[2]+4);
    }

    private String mcuId=null;
    private String deviceMacAddress=null;

    public DeviceData(String mcuId, String deviceMacAddress) {
        this(deviceMacAddress);
        this.mcuId = sanitizeMcuId(mcuId);

    }

    public DeviceData(String deviceMacAddress){
        this.deviceMacAddress = sanitizeDeviceAdr(deviceMacAddress);
    }

    public String getMcuId() {
        return mcuId;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public static class RequestConverter extends Converter.Factory{

        public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {

            if(type != DeviceData.class)
                return null;

            //we want convert a DeviceData
            return new RequestConverterImp();
        }

    }

    private static class RequestConverterImp implements Converter<DeviceData,RequestBody>{
        @Override
        public RequestBody convert(DeviceData value) throws IOException {
            //board type 5 = bluems app
            return RequestBody.create(MediaType.parse("application/json"),
                    "{\"id\":\""+value.getMcuId()+"+"+value.getDeviceMacAddress()+"\", \"boardType\":5}");
        }
    }

}
