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
package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRSelector;

import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngineFactory;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;

import java.util.ArrayList;
import java.util.List;

public class AsrSelectorPresenter implements AsrSelectorContract.Presenter {

    private AsrSelectorContract.View mView;
    private String mSelectedEngine;

    AsrSelectorPresenter(AsrSelectorContract.View view){
        mView = view;
        mSelectedEngine = null;
    }

    private List<String> getEngineNames(){
        ArrayList<String> names = new ArrayList<>(ASREngineFactory.SUPPORTED_ENGINE.length);
        for (ASREngine.ASREngineDescription desc: ASREngineFactory.SUPPORTED_ENGINE){
            names.add(desc.getName());
        }
        return names;
    }

    @Override
    public void onDisplay() {
        List<String> names = getEngineNames();
        mView.displayAsrServicesListSelector(names);
    }

    @Override
    public void onServiceSelect(String serviceName) {
        mSelectedEngine =serviceName;
        ASREngine.ASREngineDescription desc = ASREngineFactory.getDescriptionFromName(serviceName);
        if(desc==null) // it is alwasy !=null since we use the string from getEngineNames
            return;
        @ASRLanguage.Language int[] supported = desc.getSupportedLanguage();
        if(supported.length==1) //if no choose is possible
            onLanguageSelect(supported[0]);
        else
            mView.displayLanguageSelector(desc.getSupportedLanguage());
    }

    @Override
    public void onLanguageSelect(int language) {
        mView.notifySelection(mSelectedEngine,language);
        mView.dismiss();
    }

}
