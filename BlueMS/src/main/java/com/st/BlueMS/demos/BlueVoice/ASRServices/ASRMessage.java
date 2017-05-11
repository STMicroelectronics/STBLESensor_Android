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

package com.st.BlueMS.demos.BlueVoice.ASRServices;

import android.content.Context;
import android.support.annotation.IntDef;

import com.st.BlueMS.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ASR Response Messages
 */
public abstract class ASRMessage {

    @IntDef({NO_ERROR,IO_CONNECTION_ERROR,
            RESPONSE_ERROR,REQUEST_FAILED,NOT_RECOGNIZED,NETWORK_PROBLEM,LOW_CONFIDENCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    public static final int NO_ERROR = 0;
    public static final int IO_CONNECTION_ERROR = 1;
    public static final int RESPONSE_ERROR = 2;
    public static final int REQUEST_FAILED = 3;
    public static final int NOT_RECOGNIZED = 4;
    public static final int NETWORK_PROBLEM = 5;
    public static final int LOW_CONFIDENCE =6;

    /**
     * Provides the {@code status} corresponding resource string.
     * @param context current active context.
     * @param status an ASRMessage status.
     * @return the corresponding String to the state provided as a parameter.
     */
    public static String getMessage(Context context, @ASRMessage.Status int status){
        switch(status){
            case NO_ERROR:
                return "";
            case IO_CONNECTION_ERROR:
                return context.getResources().getString(R.string.blueVoice_ioError);
            case RESPONSE_ERROR:
                return context.getResources().getString(R.string.blueVoice_responseError);
            case REQUEST_FAILED:
                return context.getResources().getString(R.string.blueVoice_requestFailed);
            case NOT_RECOGNIZED:
                return context.getResources().getString(R.string.blueVoice_notRecognized);
            case NETWORK_PROBLEM:
                return context.getResources().getString(R.string.blueVoice_networkError);
            case LOW_CONFIDENCE:
                return context.getString(R.string.blueVoice_errorLowConfidence);
            default:
                return null;
        }
    }
}
