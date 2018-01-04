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

package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices;

import android.content.Context;
import android.support.annotation.IntDef;

import com.st.BlueMS.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Locale;

/**
 * ASR supported languages.
 */
public abstract class ASRLanguage {

    @IntDef({Language.ENGLISH_US,Language.ENGLISH_UK,Language.ITALIAN,
            Language.CHINESE,Language.FRENCH,
            Language.SPANISH,Language.GERMAN,
            Language.PORTUGUESE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Language {
        int ENGLISH_US = 0;
        int ENGLISH_UK = 1;
        int ITALIAN = 2;
        int CHINESE = 3;
        int FRENCH = 4;
        int SPANISH = 5;
        int GERMAN = 6;
        int PORTUGUESE = 7;
    }

    private static final int[] languages = {
            Language.ENGLISH_US,Language.ENGLISH_UK,
            Language.ITALIAN,Language.CHINESE,
            Language.FRENCH,Language.SPANISH,
            Language.GERMAN,Language.PORTUGUESE};

    /**
     * Provides the {@code lang} corresponding Locale Object.
     * @param lang a ASRLanguage language.
     * @return the corresponding Locale Object to the {@link ASRLanguage} provided as a parameter.
     */
    public static Locale getLocale(@ASRLanguage.Language int lang){
        switch(lang){
            case Language.ENGLISH_UK:
                return new Locale(Locale.ENGLISH.getLanguage(),Locale.UK.getCountry());
            case Language.ENGLISH_US:
                return new Locale(Locale.ENGLISH.getLanguage(),Locale.US.getCountry());
            case Language.ITALIAN:
                return new Locale(Locale.ITALIAN.getLanguage(),Locale.ITALY.getCountry());
            case Language.CHINESE:
                return new Locale(Locale.CHINESE.getLanguage(),Locale.CHINA.getCountry());
            case Language.FRENCH:
                return new Locale(Locale.FRENCH.getLanguage(),Locale.FRANCE.getCountry());
            case Language.SPANISH:
                return new Locale("es","ES");
            case Language.GERMAN:
                return new Locale(Locale.GERMAN.getLanguage(),Locale.GERMANY.getCountry());
            case Language.PORTUGUESE:
                return new Locale("pr","PR");
        }
        return null;
    }

    /**
     * Provides the {@code lang} corresponding resource string.
     * @param context current active context.
     * @param lang an ASRLanguage status.
     * @return the corresponding String to the {@link ASRLanguage} provided as a parameter.
     */
    public static String getLanguage(Context context, @ASRLanguage.Language int lang){
        switch (lang) {
            case Language.ENGLISH_US:
                return context.getResources().getString(R.string.blueVoice_langEnglish_us);
            case Language.ENGLISH_UK:
                return context.getResources().getString(R.string.blueVoice_langEnglish_uk);
            case Language.ITALIAN:
                return context.getResources().getString(R.string.blueVoice_langItalian);
            case Language.CHINESE:
                return context.getResources().getString(R.string.blueVoice_langChinese);
            case Language.FRENCH:
                return context.getResources().getString(R.string.blueVoice_langFrench);
            case Language.SPANISH:
                return context.getResources().getString(R.string.blueVoice_langSpanish);
            case Language.GERMAN:
                return context.getResources().getString(R.string.blueVoice_langGerman);
            case Language.PORTUGUESE:
                return context.getResources().getString(R.string.blueVoice_langPortuguese);
        }
        return null;
    }

    public static boolean isSupportedLanguage(@Language int[] supported, @Language int searchMe){
        for (@Language int lang : supported){
            if(lang == searchMe)
                return true;
        }
        return false;
    }

    /**
     * Returns the list of the available languages for the ASR.
     * @param context current active context.
     * @return a String array which contains all the String resources associated with
     * all the available languages.
     */
    public static String[] getSupportedLanguages(Context context){
        ArrayList<String> supportedLanguages = new ArrayList<>(languages.length);
        for (@ASRLanguage.Language int l:languages) {
            supportedLanguages.add(getLanguage(context,l));
        }
        return supportedLanguages.toArray(new String[languages.length]);
    }
}
