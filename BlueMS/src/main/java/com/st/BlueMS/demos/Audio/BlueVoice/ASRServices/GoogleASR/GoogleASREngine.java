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

package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.GoogleASR;

import android.app.DialogFragment;
import android.content.Context;

import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.BlueVoice.util.AudioBuffer;

import java.net.MalformedURLException;
import java.util.Locale;

/**
 * Class which defines an ASR Engine that uses Google Speech API
 */
public class GoogleASREngine implements ASREngine {

    private static final String ENGINE_NAME="Google";

    private Context mContext;
    private boolean mIsAsrEnabled = false;
    private GoogleASRAsyncRequest mAsrService;
    private Locale mLanguage;

    public GoogleASREngine(Context context, Locale language) throws IllegalArgumentException {
        mLanguage = language;
        mContext = context;
        enableASR();
    }

    @Override
    public boolean needAuthKey() {
        return true;
    }

    @Override
    public boolean hasLoadedAuthKey() {
        enableASR();
        return mIsAsrEnabled;
    }

    @Override
    public DialogFragment getAuthKeyDialog() {
        return new GoogleASRAuthKeyDialog();
    }

    @Override
    public boolean sendASRRequest(AudioBuffer audio, ASRRequestCallback callback) {

        mAsrService.sendRequest(audio,callback);
        return true;
    }

    @Override
    public boolean hasContinuousRecognizer() {
        return false;
    }

    @Override
    public void startListener() {
        //empty
    }

    @Override
    public void stopListener() {
        //empty
    }

    @Override
    public void destroyListener() {
        //empty
    }

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    private void enableASR() {
        GoogleASRKey mAsrKey = GoogleASRKey.loadKey(mContext);
        mIsAsrEnabled = mAsrKey != null;
        if(mIsAsrEnabled) {
            try {
                mAsrService = new GoogleASRAsyncRequest(mAsrKey, mLanguage);
            } catch (MalformedURLException e) {
                mIsAsrEnabled = false;
            }
        }
    }


}
