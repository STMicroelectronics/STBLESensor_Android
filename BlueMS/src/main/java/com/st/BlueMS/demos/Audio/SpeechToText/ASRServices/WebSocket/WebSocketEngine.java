package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.WebSocket;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioBuffer;
import com.st.BlueMS.demos.Audio.Utils.AudioConverter;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * This engine will send the audio to a websocket.
 * the audio is send with as raw 16bit little endian @ 8kHz
 * the websocket authentication parameters are send inside the AUTHORIZATION header part of open request.
 *
 * a simple websocket server can be done using nodejs + ws lib example:
 * <code>
 * const WebSocket = require('ws');
 * const fullScale = require('fullScale');
 *
 * const wss = new WebSocket.Server({
 *      verifyClient: function(info) {
 *          authorisation format:"Basic base64(user:pass)"
 *          var base64Auth = info.req.headers['authorization'].split(" ")[1];
 *          var buf = Buffer.from(base64Auth, 'base64');
 *          console.log("Auth: "+buf);
 *          return true;
 *      }, port: 8080 });
 *
 * wss.on('connection', function connection(ws) {
 *  var wstream = fullScale.createWriteStream('audio.raw');
 *  ws.on('message', function incoming(message) {
 *      wstream.write(message);
 *      //ws.send("received message");
 *  });
 *
 *  ws.on('close', function close(reason){
 *      console.log("close: "+reason)
 *  });
 * });
 * </code>
 */
public class WebSocketEngine implements ASREngine {

    private static final String ENGINE_NAME="Generic WebSocket";

    public static ASREngineDescription DESCRIPTION = new ASREngineDescription() {
        @Override
        public String getName() {
            return ENGINE_NAME;
        }

        @Override @ASRLanguage.Language
        public int[] getSupportedLanguage() {
            return new int[]{ASRLanguage.Language.UNKNOWN};
        }

        @Nullable
        @Override
        public ASREngine build(@NonNull Context context, @ASRLanguage.Language int language) {
            if(language == ASRLanguage.Language.UNKNOWN)
                return new WebSocketEngine(context,language);
            else
                return null;
        }
    };

    private void enableASR() {
        WebSocketParam mAsrKey = WebSocketParam.loadParam(mContext);
        mIsAsrEnabled = mAsrKey != null;
    }

    private Context mContext;
    private boolean mIsAsrEnabled;

    private ASRConnectionCallback mConnectionCallBack;
    private ASRRequestCallback mRequestCallback;
    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            Log.d("WebSocket","open:"+response.toString());
            if(response.code()==101)
                mConnectionCallBack.onEngineStart();
            mIsSocketConnected = true;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.d("WebSocket","text:"+text);
            if(mRequestCallback!=null)
                mRequestCallback.onAsrResponse(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d("WebSocket","onClose");
            super.onClosed(webSocket, code, reason);
            mIsSocketConnected = false;
            mConnectionCallBack.onEngineStop();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            mIsSocketConnected = false;
            super.onFailure(webSocket, t, response);
            Log.d("WebSocket",t.toString());
            mConnectionCallBack.onEngineFail(t);
        }
    };

    private WebSocket mConnection;
    private boolean mIsSocketConnected;

    private WebSocketEngine(Context ctx, @ASRLanguage.Language int language){
        mContext = ctx;
        mIsAsrEnabled = false;
    }


    @Override
    public boolean sendASRRequest(AudioBuffer audio, ASRRequestCallback callback) {
        mRequestCallback = callback;
        ByteString data = ByteString.of(AudioConverter.toLEByteArray(audio.getData()));
        if(mIsSocketConnected)
            return mConnection.send(data);
        return false;
    }

    @Override
    public void startListener(@NonNull ASRConnectionCallback callback) {
        mConnectionCallBack = callback;

        WebSocketParam param = WebSocketParam.loadParam(mContext);
        if(param == null){
            callback.onEngineFail(new IllegalArgumentException("Connection parameters not present"));
            return;
        }
        Request.Builder builder = new Request.Builder().url(param.endpoint);

        setAuthentication(builder,param);
        //setDefaultHeaders(builder);

        OkHttpClient client = configureHttpClient();

        mIsSocketConnected = false;
        mConnection = client.newWebSocket(builder.build(),mWebSocketListener);

    }

    private void setAuthentication(Request.Builder builder,WebSocketParam param) {
        if(param.user!=null && param.password!=null)
            builder.addHeader("Authorization", Credentials.basic(param.user,param.password));
    }

    private OkHttpClient configureHttpClient(){
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(90, TimeUnit.SECONDS);

        return builder.build();
    }

    @Override
    public void stopListener(@NonNull ASRConnectionCallback callback) {
        mConnectionCallBack = callback;
        if(mConnection !=null)
            mConnection.close(1000,null);
    }

    @Override
    public void destroyListener() {
    }

    @Override
    public boolean needAuthKey() {
        return true;
    }

    @Nullable
    @Override
    public DialogFragmentDismissCallback getAuthKeyDialog() {
        return new WebSocketAuthDialog();
    }

    @Override
    public ASREngineDescription getDescription() {
        return WebSocketEngine.DESCRIPTION;
    }

    @Override
    public boolean hasLoadedAuthKey() {
        enableASR();
        return mIsAsrEnabled;
    }

    @Override
    public boolean hasContinuousRecognizer() {
        return true;
    }
}
