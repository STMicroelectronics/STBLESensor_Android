package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.IBMWatson;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

class WatsonARSKey {

    private static final String DEFAULT_ENDPOINT = "https://stream.watsonplatform.net/speech-to-text/api";
    private static final String PREF_NAME = WatsonARSKey.class.getCanonicalName();
    private static final String PREF_USERNAME = PREF_NAME+".ASR_USERNAME";
    private static final String PREF_PASSWORD = PREF_NAME+".ASR_PASSWORD";
    private static final String PREF_ENDPIINT = PREF_NAME+".ASR_ENDPOINT";

    final String name;
    final String password;
    final String endpoint;

    static @Nullable
    WatsonARSKey loadKey(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String user = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        String endpoint = pref.getString(PREF_ENDPIINT, DEFAULT_ENDPOINT);
        try {
            return new WatsonARSKey(endpoint,user,password);
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


    WatsonARSKey(String endpoint,String name, String password) throws IllegalArgumentException{
       this.name = sanitizeLoginString(name);
       this.password = sanitizeLoginString(password);
       this.endpoint = sanitizeEndpoint(endpoint);

    }

    public void store(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        pref.edit()
                .putString(PREF_USERNAME,name)
                .putString(PREF_PASSWORD,password)
                .putString(PREF_ENDPIINT, endpoint)
                .apply();
    }

}
