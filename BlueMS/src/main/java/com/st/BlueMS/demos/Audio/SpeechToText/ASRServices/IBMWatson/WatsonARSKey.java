package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.IBMWatson;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private static @NonNull
    String sanitizeLoginString(@Nullable String str){
        if(str==null)
            throw new IllegalArgumentException("Missing Api Key");
        String temp = str.trim();
        if(temp.isEmpty())
            throw new IllegalArgumentException("Invalid empty Api Key");
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
