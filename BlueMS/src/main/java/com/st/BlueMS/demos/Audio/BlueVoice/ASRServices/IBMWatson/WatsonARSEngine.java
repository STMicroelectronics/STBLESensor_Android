package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.IBMWatson;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.BlueVoice.util.AudioBuffer;
import com.st.BlueMS.demos.Audio.BlueVoice.util.DialogFragmentDismissCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class WatsonARSEngine implements ASREngine {

    private static final String ENGINE_URL = "https://stream.watsonplatform.net/speech-to-text/api";
    private static final String ENGINE_NAME="IBM Watson";
    private static final SpeechModel DEFAULT_LANGUAGE_MODEL = SpeechModel.EN_GB_BROADBANDMODEL;

    private static final @ASRLanguage.Language int[] SUPPORTED_LANGUAGES = {
            ASRLanguage.Language.ENGLISH_US,
            ASRLanguage.Language.ENGLISH_UK};


    public static ASREngineDescription DESCRIPTION = new ASREngineDescription() {
        @Override
        public String getName() {
            return ENGINE_NAME;
        }

        @Override
        public int[] getSupportedLanguage() {
            return SUPPORTED_LANGUAGES;
        }

        @Nullable
        @Override
        public ASREngine build(@NonNull Context context, int language) {
            if(ASRLanguage.isSupportedLanguage(SUPPORTED_LANGUAGES,language))
                return new WatsonARSEngine(context,language);
            return null;
        }
    };

    private static SpeechModel getLanguageModel(@ASRLanguage.Language int lang){
        if(lang== ASRLanguage.Language.ENGLISH_UK)
            return SpeechModel.EN_GB_BROADBANDMODEL;
        if(lang == ASRLanguage.Language.ENGLISH_US)
            return SpeechModel.EN_US_BROADBANDMODEL;
        return DEFAULT_LANGUAGE_MODEL;
    }

    private Context mContext;
    private boolean mIsAsrEnabled;
    private SpeechToText mService;
    private PipedInputStream mWebSocketPipe;
    private PipedOutputStream mAppPipe;
    private SpeechModel mModel;
    private WatsonServiceCallback mServiceCallback;

    private void initAudioStream() throws IOException {
        mAppPipe = new PipedOutputStream();
        mWebSocketPipe = new PipedInputStream(mAppPipe);
    }

    private void closeAudioStream() throws IOException {
        if(mAppPipe!=null)
            mAppPipe.close();
        if(mWebSocketPipe!=null)
            mWebSocketPipe.close();
    }

    private WatsonARSEngine(Context context, @ASRLanguage.Language int language){
        mContext = context;
        mIsAsrEnabled = false;
        mService = new SpeechToText();
        mModel = getLanguageModel(language);
    }

    @Override
    public boolean needAuthKey() {
        return true;
    }

    @Nullable
    @Override
    public DialogFragmentDismissCallback getAuthKeyDialog() {
        return new WatsonASRAuthDialog();
    }

    private static void writeUpSamplingDataToOutStream(short[] data, OutputStream out) throws IOException {
        for (short val : data){

            out.write(val & 0x00FF);
            out.write(val >> 8);
            out.write(val & 0x00FF);
            out.write(val >> 8);
        }
    }

    @Override
    public boolean sendASRRequest(AudioBuffer audio, ASRRequestCallback callback) {
       mServiceCallback.setRequestCallback(callback);
        try {
            if(mServiceCallback.isConnect()) {
                writeUpSamplingDataToOutStream(audio.getData(), mAppPipe);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasContinuousRecognizer() {
        return true;
    }

    @Override
    public void startListener(@NonNull ASRConnectionCallback callback) {
        try {
            initAudioStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mServiceCallback = new WatsonServiceCallback(callback);

        new WatsonConnectionTask(mService,mModel,mServiceCallback).execute(mWebSocketPipe);
    }

    private static class WatsonConnectionTask extends AsyncTask<InputStream,Void,Void>{

        private static RecognizeOptions getRecognizeOptions(SpeechModel model){
            Log.d("WatsonConnectionTask", "Model: "+model.getName());
            return new RecognizeOptions.Builder()
                    .interimResults(true)
                    //.inactivityTimeout(2000)
                    .inactivityTimeout(-1)//inactivity timeout to +inf
                    .contentType(HttpMediaType.createAudioRaw(16000)+"; endianness=little-endian")
                    .model(model.getName())
                    .build();
        }

        private SpeechModel mModel;
        private SpeechToText mService;
        private RecognizeCallback mServiceCallback;

        WatsonConnectionTask(SpeechToText service,SpeechModel model,RecognizeCallback serviceCallback){
            mService = service;
            mModel = model;
            mServiceCallback = serviceCallback;
        }

        @Override
        protected Void doInBackground(InputStream... inputStreams) {
            InputStream inputStream = inputStreams[0];
            mService.recognizeUsingWebSocket(inputStream,
                    getRecognizeOptions(mModel),
                    mServiceCallback);
            return null;
        }

    }


    private static class WatsonServiceCallback extends BaseRecognizeCallback {

        private @NonNull
        ASRConnectionCallback mSetupCallback;
        private ASRRequestCallback mRequestCallback;

        private boolean isConnect=false;

        WatsonServiceCallback(@NonNull ASRConnectionCallback setupCallback){
            mSetupCallback = setupCallback;
        }

        void setRequestCallback(ASRRequestCallback requestCallback){
            mRequestCallback = requestCallback;
        }

        @Override
        public void onTranscription(SpeechResults speechResults) {
            if(speechResults == null ||
                    speechResults.getResults()==null ||
                    speechResults.getResults().size()==0){
                return;
            }

            Transcript transcript = speechResults.getResults().get(0);
            if(transcript.isFinal()){
                String text = transcript.getAlternatives().get(0).getTranscript();
                if(mRequestCallback!=null) {
                    mRequestCallback.onAsrResponse(text);
                }
            }

        }

        @Override
        public void onConnected() {
            mSetupCallback.onEngineStart();
        }

        @Override
        public void onError(Exception e) {
            isConnect=false;
            mSetupCallback.onEngineFail(e);
            e.printStackTrace();
        }

        @Override
        public void onDisconnected(){
            isConnect=false;
            mSetupCallback.onEngineStop();
        }


        @Override
        public void onListening() {
            isConnect=true;
            mSetupCallback.onEngineStart();
        }


        public boolean isConnect() {
            return isConnect;
        }
    }

    @Override
    public void stopListener(@NonNull ASRConnectionCallback callback) {
        try {
            closeAudioStream();
            callback.onEngineStop();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onEngineFail(e);
        }

    }

    @Override
    public void destroyListener() {
    }

    @Override
    public ASREngineDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean hasLoadedAuthKey() {
        enableASR();
        return mIsAsrEnabled;
    }



    private void enableASR() {
        WatsonARSKey mAsrKey = WatsonARSKey.loadKey(mContext);
        if(mAsrKey!=null) {
            mIsAsrEnabled = true;
            mService.setUsernameAndPassword(mAsrKey.name, mAsrKey.password);
            mService.setEndPoint(ENGINE_URL);
        }else{
            mIsAsrEnabled=false;
        }
    }
}
