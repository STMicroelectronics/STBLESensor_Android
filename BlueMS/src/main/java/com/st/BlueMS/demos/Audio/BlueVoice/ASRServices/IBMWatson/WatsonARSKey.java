package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.IBMWatson;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

class WatsonARSKey {
    private static final String PREF_NAME = WatsonARSKey.class.getCanonicalName();
    private static final String PREF_USERNAME = PREF_NAME+".ASR_USERNAME";
    private static final String PREF_PASSWORD = PREF_NAME+".ASR_PASSWORD";

    final String name;
    final String password;

    static @Nullable
    WatsonARSKey loadKey(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String user = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        try {
            return new WatsonARSKey(user,password);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    WatsonARSKey(String name, String password) throws IllegalArgumentException{
        if(name == null)
            throw new IllegalArgumentException("Name can not be null");
        this.name = name.trim();
        if(this.name.isEmpty())
            throw new IllegalArgumentException("Name can not be empty");


        if(password == null)
            throw new IllegalArgumentException("Password can not be null");
        this.password = password.trim();
        if(this.password.isEmpty())
            throw new IllegalArgumentException("Password can not be empty");
    }

    public void store(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        pref.edit()
                .putString(PREF_USERNAME,name)
                .putString(PREF_PASSWORD,password)
                .apply();
    }

}
