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

package com.st.BlueMS.demos.util.bluevoice;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AsrResponseParser {

    private static final String RESULT= "result";
    private static final String RESULT_INDEX= "result_index";
    private static final String TRANSCRIPT= "transcript";
    private static final String CONFIDENCE= "confidence";
    private static final String ALTERNATIVE= "alternative";

    private double mConfidence=1.0;
    private String mTranscript=null;


    private static String extractJsonString(InputStream in) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(in, "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);
        String s;
        StringBuilder resultContent = new StringBuilder();
        br.readLine();
        while ((s = br.readLine()) != null) {
            resultContent.append(s);
        }
        return resultContent.toString();
    }

    AsrResponseParser(InputStream response) throws JSONException {
        String json = null;
        try {
            json = extractJsonString(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseJson(json);
    }

    private void parseJson(String json) throws JSONException {
        JSONObject jsonResponse = new JSONObject(json);
        JSONArray jsonArrayResults = jsonResponse.getJSONArray(RESULT);
        int respIndex = jsonResponse.getInt(RESULT_INDEX);
        JSONObject jsonObjectAlternative = jsonArrayResults.getJSONObject(0);
        JSONArray jsonArrayAlternative = jsonObjectAlternative.getJSONArray(ALTERNATIVE);

        JSONObject bestResponse = jsonArrayAlternative.getJSONObject(respIndex);
        if(bestResponse.has(CONFIDENCE))
            mConfidence = bestResponse.getDouble(CONFIDENCE);
        if(bestResponse.has(TRANSCRIPT))
            mTranscript = bestResponse.getString(TRANSCRIPT);

    }

    @Nullable String getTranscript(){
        return mTranscript;
    }

    double getConfidence(){
        return mConfidence;
    }


}
