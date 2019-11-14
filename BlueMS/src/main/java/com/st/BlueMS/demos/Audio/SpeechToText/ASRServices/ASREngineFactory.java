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

package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices;

import android.content.Context;
import androidx.annotation.Nullable;

import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.GoogleASR.GoogleASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.IBMWatson.WatsonARSEngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.WebSocket.WebSocketEngine;

/**
 * ASR Engine Factory class that returns a specific ASR Engine
 */
public class ASREngineFactory {

    /**
     * list of supported engine
     */
    public static final ASREngine.ASREngineDescription SUPPORTED_ENGINE[] = new ASREngine.ASREngineDescription[]{
            GoogleASREngine.DESCRIPTION,
            WatsonARSEngine.DESCRIPTION,
            WebSocketEngine.DESCRIPTION
    };


    /**
     * find the engine description for a specific engine name
     * @param name name to search
     * @return engine description with that specific name or null if not found
     */
    public static @Nullable ASREngine.ASREngineDescription getDescriptionFromName(String name){
        for (ASREngine.ASREngineDescription desc: SUPPORTED_ENGINE){
            if(name.equals(desc.getName()))
                return desc;
        }
        return null;
    }

    /**
     * build an ASR engine with a specific name and set up to recognize a specific language
     * @param context context where use the engine
     * @param engineName engine name
     * @param lang language to recognize
     * @return the engine or null if the engine name is not valid or the language is not supported
     */
    public static @Nullable
    ASREngine getASREngine(Context context, String engineName, @ASRLanguage.Language int lang){
        ASREngine.ASREngineDescription desc = getDescriptionFromName(engineName);
        if(desc == null)
            return  null;
        return  desc.build(context,lang);
    }
}
