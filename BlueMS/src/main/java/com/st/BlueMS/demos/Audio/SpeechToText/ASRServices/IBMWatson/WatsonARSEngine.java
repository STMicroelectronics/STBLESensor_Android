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
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.GetModelOptions;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.speech_to_text.v1.websocket.RecognizeCallback;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioBuffer;
import com.st.BlueMS.demos.Audio.Utils.AudioConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class WatsonARSEngine implements ASREngine {

    private static final String ENGINE_NAME="IBM Watson";

    private static final String DEFAULT_LANGUAGE_MODEL = GetModelOptions.ModelId.EN_GB_BROADBANDMODEL;

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

    private static String getLanguageModel(@ASRLanguage.Language int lang){
        if(lang== ASRLanguage.Language.ENGLISH_UK)
            return GetModelOptions.ModelId.EN_GB_BROADBANDMODEL;
        if(lang == ASRLanguage.Language.ENGLISH_US)
            return GetModelOptions.ModelId.EN_US_BROADBANDMODEL;
        return DEFAULT_LANGUAGE_MODEL;
    }

    private Context mContext;
    private boolean mIsAsrEnabled;
    private PipedInputStream mWebSocketPipe;
    private PipedOutputStream mAppPipe;
    private @ASRLanguage.Language int mModel;
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
        mModel = language;
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


    @Override
    public boolean sendASRRequest(AudioBuffer audio, ASRRequestCallback callback) {
       mServiceCallback.setRequestCallback(callback);
        try {
            if(mServiceCallback.isConnect()) {
                if(audio.getSamplingRate() != 16000)
                    AudioConverter.upSamplingSignalToLE(audio.getData(), mAppPipe);
                else
                    AudioConverter.toLEByteStream(audio.getData(),mAppPipe);
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
        SpeechToText service = buildService();
        new WatsonConnectionTask(service,mModel,mServiceCallback).execute(mWebSocketPipe);
    }



    private static class WatsonConnectionTask extends AsyncTask<InputStream,Void,Void>{

        private static RecognizeOptions getRecognizeOptions(InputStream inputStream, @ASRLanguage.Language int model){
            return new RecognizeOptions.Builder()
                    .interimResults(true)
                    .audio(inputStream)
                    //.inactivityTimeout(2000)
                    .inactivityTimeout(-1)//inactivity timeout to +inf
                    .contentType(HttpMediaType.createAudioRaw(16000)+"; endianness=little-endian")
                    .model(getLanguageModel(model))
                    .build();
        }

        private @ASRLanguage.Language int mModel;
        private SpeechToText mService;
        private RecognizeCallback mServiceCallback;

        WatsonConnectionTask(SpeechToText service,@ASRLanguage.Language int model,RecognizeCallback serviceCallback){
            mService = service;
            mModel = model;
            mServiceCallback = serviceCallback;
        }

        @Override
        protected Void doInBackground(InputStream... inputStreams) {
            InputStream inputStream = inputStreams[0];
            mService.recognizeUsingWebSocket(
                    getRecognizeOptions(inputStream,mModel),
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
        public void onTranscription(SpeechRecognitionResults speechResults) {
            if(speechResults == null || speechResults.getResults()==null ||
                    speechResults.getResults().size()==0)
                return;

            SpeechRecognitionResult res  = speechResults.getResults().get(0);
            if(res.isXFinal()){
                String text = res.getAlternatives().get(0).getTranscript();
                if(mRequestCallback!=null){
                    mRequestCallback.onAsrResponse(text);
                }
            }

        }

        @Override
        public void onConnected() {
            //mSetupCallback.onEngineStart();
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

    private @Nullable SpeechToText buildService() {
        WatsonARSKey asrKey = WatsonARSKey.loadKey(mContext);
        if(asrKey == null)
            return null;
        IamAuthenticator auth = new IamAuthenticator(asrKey.apiKey);
        SpeechToText service = new SpeechToText(auth);
        if(asrKey.endpoint != null)
            service.setServiceUrl(asrKey.endpoint);
        return service;
    }



    private void enableASR() {
        WatsonARSKey mAsrKey = WatsonARSKey.loadKey(mContext);
        mIsAsrEnabled = mAsrKey != null;
    }
}
