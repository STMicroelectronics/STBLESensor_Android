package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRSelector;

import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngineFactory;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRLanguage;

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
