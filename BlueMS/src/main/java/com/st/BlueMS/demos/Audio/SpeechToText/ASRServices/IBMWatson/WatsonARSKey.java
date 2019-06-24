/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.IBMWatson;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

class WatsonARSKey {

    private static final String DEFAULT_ENDPOINT = "https://stream.watsonplatform.net/speech-to-text/api";
    private static final String PREF_NAME = WatsonARSKey.class.getCanonicalName();
    private static final String PREF_API_KEY = PREF_NAME+".ASR_USERNAME";
    private static final String PREF_ENDPIINT = PREF_NAME+".ASR_ENDPOINT";

    final String apiKey;
    final String endpoint;

    static @Nullable
    WatsonARSKey loadKey(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String apiKey = pref.getString(PREF_API_KEY, null);
        String endpoint = pref.getString(PREF_ENDPIINT, DEFAULT_ENDPOINT);
        try {
            return new WatsonARSKey(endpoint,apiKey);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    private static @Nullable String sanitizeLoginString(@Nullable String str){
        if(str==null)
            return null;
        String temp = str.trim();
        if(temp.isEmpty())
            return null;
        else
            return temp;
    }

    private static String sanitizeEndpoint(@Nullable String str) throws IllegalArgumentException{
        if(str == null || str.trim().isEmpty())
            return DEFAULT_ENDPOINT;
        str = str.trim();
        //convert websocket to https since the sdk require an https endpoint
        if(str.startsWith("wss://")){
            str = str.replaceFirst("wss://","https://");
        }
        //check that the enpoint has the https protocol
        if(!str.startsWith("https://")){
            throw new IllegalArgumentException("Endpoint must be an HTTPS URL");
        }
        //check if the string is a valid url
        try {
            new URL(str);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed url:"+e.getLocalizedMessage());
        }
        return str;
    }


    WatsonARSKey(String endpoint,String apiKey) throws IllegalArgumentException{
       this.apiKey = sanitizeLoginString(apiKey);
       this.endpoint = sanitizeEndpoint(endpoint);

    }

    public void store(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        pref.edit()
                .putString(PREF_API_KEY, apiKey)
                .putString(PREF_ENDPIINT, endpoint)
                .apply();
    }

}
