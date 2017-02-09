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

import android.os.AsyncTask;
import android.support.annotation.IntDef;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsrAsyncRequest {

    private static final int IO_TIMEOUT_MS =10000;
    private static final String ASR_URL ="https://www.google.com/speech-api/v2/recognize" +
                    "?xjerr=1&client=chromium&lang=en-US&key=";

    @IntDef({NO_ERROR,IO_CONNECTION_ERROR,
            RESPONSE_ERROR,REQUEST_FAILED,NOT_RECOGNIZED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    public static final int NO_ERROR = 0;
    public static final int IO_CONNECTION_ERROR = 1;
    public static final int RESPONSE_ERROR = 2;
    public static final int REQUEST_FAILED = 3;
    public static final int NOT_RECOGNIZED = 4;
    private static final float MIN_CONFIDENCE = 0.75f;

    public interface AsrAsyncRequestCallback{
        /**
         * called when the request is send
         */
        void onRequestSend();

        /**
         * called when we have a response
         * @param status response status
         * @param response sound converted to text
         */
        void onRequestRespond(@Status int status, String response);
    }

    private GoogleAsrKey mKey;

    private AsrAsyncRequestCallback mCallback;

    public AsrAsyncRequest(GoogleAsrKey key, AsrAsyncRequestCallback callback){
        mKey=key;
        mCallback=callback;
    }

    public void sendRequest(AudioBuffer record){
       new UploadMessageTask().execute(record);
    }

    private class UploadMessageTask extends AsyncTask<AudioBuffer,Void,
            UploadMessageTask.AsrResult> {

        class AsrResult{
            @AsrAsyncRequest.Status int status;
            String result;

            AsrResult( @AsrAsyncRequest.Status int status,  String result){
                this.status = status;
                this.result=result;
            }

            AsrResult( @AsrAsyncRequest.Status int status){
                this(status,null);
            }
        }

        private HttpURLConnection createUrlRequest(AudioBuffer buffer)
                throws IOException {
            HttpURLConnection con = (HttpURLConnection) new URL(ASR_URL+mKey.getKey()).openConnection();
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
            AsrResponseParser parser;
            try {
                parser = new AsrResponseParser(con.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return new AsrResult(IO_CONNECTION_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
                return new AsrResult(NOT_RECOGNIZED);
            }

            if(parser.getTranscript()==null || parser.getConfidence()<MIN_CONFIDENCE)
                return new AsrResult(NOT_RECOGNIZED);
            else
                return new AsrResult(NO_ERROR,parser.getTranscript());

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
                    return new AsrResult(RESPONSE_ERROR);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new AsrResult(IO_CONNECTION_ERROR);
            }
        }

        @Override
        protected void onPostExecute(AsrResult s) {
            mCallback.onRequestRespond(s.status,s.result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mCallback.onRequestSend();
        }
    }

}
