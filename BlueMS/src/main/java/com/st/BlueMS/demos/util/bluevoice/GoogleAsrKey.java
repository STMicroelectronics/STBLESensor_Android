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

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class GoogleAsrKey  {
    private static final int KEY_LENGTH=39;
    private static final String PREF_KEY = GoogleAsrKey.class.getCanonicalName()+".ASR_KEY";

    private String key;

    public static @Nullable GoogleAsrKey loadKey(SharedPreferences pref) {
        String key = pref.getString(PREF_KEY, null);
        if (key != null)
            try {
                return new GoogleAsrKey(key);
            } catch (IllegalArgumentException e){
                return null;
            }
        else
            return null;

    }

    private static void checkValidKey(String key) throws IllegalArgumentException{
        if(key.length()!=KEY_LENGTH)
            throw new IllegalArgumentException("Illegal key length");
    }

    public GoogleAsrKey(CharSequence k) throws IllegalArgumentException{
        String temp = k.toString().trim();
        checkValidKey(temp);
        key=temp;
    }

    public String getKey(){
        return key;
    }

    public void store(SharedPreferences pref){
        pref.edit()
                .putString(PREF_KEY,key)
                .apply();
    }


}
