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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioBuffer;

import java.net.MalformedURLException;
import java.util.Locale;

/**
 * Class which defines an ASR Engine that uses Google Speech API
 */
public class GoogleASREngine implements ASREngine {

    private static final String ENGINE_NAME="Google";
    private static final @ASRLanguage.Language int[] SUPPORTED_LANGUAGES = {
            ASRLanguage.Language.ENGLISH_UK,
            ASRLanguage.Language.ITALIAN ,
            ASRLanguage.Language.FRENCH, ASRLanguage.Language.SPANISH,
            ASRLanguage.Language.GERMAN, ASRLanguage.Language.PORTUGUESE,
            ASRLanguage.Language.KOREAN};


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
                return new GoogleASREngine(context, ASRLanguage.getLocale(language));
            return null;
        }
    };

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
    public DialogFragmentDismissCallback getAuthKeyDialog() {
        return new GoogleASRAuthKeyDialog();
    }

    @Override
    public boolean sendASRRequest(AudioBuffer audio, ASRRequestCallback callback) {
        if(mAsrService!=null) {
            mAsrService.sendRequest(audio, callback);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean hasContinuousRecognizer() {
        return false;
    }

    @Override
    public void startListener(@NonNull ASRConnectionCallback callback) {
        //empty
        callback.onEngineStart();
    }

    @Override
    public void stopListener(@NonNull ASRConnectionCallback callback) {
        //empty
        callback.onEngineStop();
    }

    @Override
    public void destroyListener() {
        //empty
    }

    @Override
    public ASREngineDescription getDescription() {
        return DESCRIPTION;
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
