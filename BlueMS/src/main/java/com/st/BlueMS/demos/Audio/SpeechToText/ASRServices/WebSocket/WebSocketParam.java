package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.WebSocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/*
 * Class containing the parameters needed to open a websocket connection
 *
 */
public class WebSocketParam {

    private static final String PREF_NAME = WebSocketParam.class.getCanonicalName();
    private static final String PREF_ENDPOINT = PREF_NAME+".ASR_ENDPOINT";
    private static final String PREF_AUTH_USER = PREF_NAME+".ASR_AUTH_USER";
    private static final String PREF_AUTH_PASS = PREF_NAME+".ASR_AUTH_PASS";


    /**
     * url where find the websocket server
     */
    final @NonNull String endpoint;
    /**
     * user name needed to open the websocket connection, if null the autorization is not send
     */
    final @Nullable String user;
    final @Nullable String password;

    static @Nullable
    WebSocketParam loadParam(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String endpoint = pref.getString(PREF_ENDPOINT, null);
        String user = pref.getString(PREF_AUTH_USER, null);
        String password = pref.getString(PREF_AUTH_PASS, null);
        try {
            return new WebSocketParam(endpoint,user,password);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    /**
     * validate the parameters and store it inside the class
     * @param webSocketEndpoint websocket url
     * @param user username to present to the websocket
     * @param password user password
     * @throws IllegalArgumentException if the websocketEnptont is null or it doesn't start with ws://
     *  or wss://
     *
     * set the user to null if the authentication is not necessary
     */
    WebSocketParam(@Nullable String webSocketEndpoint, @Nullable String user,
                   @Nullable String password) throws IllegalArgumentException{
        if(webSocketEndpoint == null)
            throw new IllegalArgumentException("Endpoint can not be null");

        if(!webSocketEndpoint.startsWith("ws://") && !webSocketEndpoint.startsWith("wss://")){
            throw new IllegalArgumentException("Endpoint is not a valid WebSocket protocol");
        }
        this.endpoint = webSocketEndpoint;
        this.user = user;
        this.password = password;
    }

    public void store(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        pref.edit()
                .putString(PREF_ENDPOINT, endpoint)
                .putString(PREF_AUTH_USER,user)
                .putString(PREF_AUTH_PASS,password)
                .apply();
    }

}
