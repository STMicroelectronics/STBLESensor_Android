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

package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.GoogleASR;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRMessage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioBuffer;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Send a request to the Google Automatic Speech Recognition service
 * the request is handle in a background service {@link UploadMessageTask}
 */
class GoogleASRAsyncRequest {

    private static final int IO_TIMEOUT_MS =10000;
    private static final float MIN_CONFIDENCE = 0.75f;

    private static final String ASR_URL_FORMAT ="https://www.google.com/speech-api/v2/recognize" +
            "?xjerr=1&mqtt=chromium&lang=%s&key=%s";

    private URL mServiceUrl;

    GoogleASRAsyncRequest(GoogleASRKey key, @Nullable Locale language) throws MalformedURLException {

        if(language==null) {
            language = Locale.ENGLISH;
        }
        mServiceUrl = getRequestUrl(key,language);
    }

    private static URL getRequestUrl(GoogleASRKey key , Locale language) throws MalformedURLException {
        return new URL(String.format(language,ASR_URL_FORMAT,language.toString(),key.getKey()));
    }

    void sendRequest(AudioBuffer record, ASRRequestCallback callback){
        new UploadMessageTask(callback).execute(record);
    }

    /**
     * Background Async task which manage an ASR request
     */
    private class UploadMessageTask extends AsyncTask<AudioBuffer,Void,
            UploadMessageTask.AsrResult> {

        private ASRRequestCallback mCallback;

        class AsrResult{
            @ASRMessage.Status
            int status;
            String result;

            AsrResult(@ASRMessage.Status int status, String result){
                this.status = status;
                this.result=result;
            }

            AsrResult( @ASRMessage.Status int status){
                this(status,null);
            }
        }

        UploadMessageTask(ASRRequestCallback callback){
            mCallback =callback;
        }

        private HttpURLConnection createUrlRequest(AudioBuffer buffer)
                throws IOException {
            HttpURLConnection con = (HttpURLConnection) mServiceUrl.openConnection();
            con.setDefaultUseCaches(false);
            con.setConnectTimeout(IO_TIMEOUT_MS);
            con.setReadTimeout(IO_TIMEOUT_MS);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "audio/l16; rate="+buffer.getSamplingRate());
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setChunkedStreamingMode(0);
            con.setInstanceFollowRedirects(true);

            return con;
        }

        private void appendData(HttpURLConnection con, AudioBuffer buffer) throws IOException {
            OutputStream os = con.getOutputStream();
            buffer.writeLittleEndianTo(os);
            os.close();
        }

        private boolean hasCorrectResponse(HttpURLConnection conn) throws IOException {
            return conn.getResponseCode()==HttpURLConnection.HTTP_OK;
        }

        private AsrResult extractBestAnswer(HttpURLConnection con){
            GoogleASRResponseParser parser;
            try {
                parser = new GoogleASRResponseParser(con.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return new AsrResult(ASRMessage.IO_CONNECTION_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
                return new AsrResult(ASRMessage.NOT_RECOGNIZED);
            }

            if(parser.getTranscript()==null)
                return new AsrResult(ASRMessage.NOT_RECOGNIZED);
            else if (parser.getConfidence()<MIN_CONFIDENCE)
                return new AsrResult(ASRMessage.LOW_CONFIDENCE);
            else
                return new AsrResult(ASRMessage.NO_ERROR,parser.getTranscript());
        }

        @Override
        protected AsrResult doInBackground(AudioBuffer... params) {
            AudioBuffer buffer = params[0];
            HttpURLConnection con;
            try {
                con = createUrlRequest(buffer);
                appendData(con, buffer);
                con.connect();
                if(hasCorrectResponse(con)){
                    return extractBestAnswer(con);
                }else{
                    return new AsrResult(ASRMessage.RESPONSE_ERROR);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new AsrResult(ASRMessage.IO_CONNECTION_ERROR);
            }
        }

        @Override
        protected void onPostExecute(AsrResult s) {
            mCallback.onAsrResponseError(s.status);
            mCallback.onAsrResponse(s.result);

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mCallback.onAsrRequestSend();
        }
    }
}
