package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class IoTCentralRegisterDeviceViewModel extends ViewModel {

    enum State {
        AZURE_LOGIN_REQUEST,
        AZURE_LOGIN_SUCCESS,
        AZURE_APP_SELECTED,
        AZURE_APP_LOGIN,
        AZURE_TEMPLATE_SELECTED,
        AZURE_DEVICE_CREATED,
        Azure_DEVICE_KNOWED
    }

    MutableLiveData<State> currentStatus = new MutableLiveData<>();
    MutableLiveData<Boolean> showIndefiniteProgressBar = new MutableLiveData<>();
    MutableLiveData<Boolean> showSelectApplication = new MutableLiveData<>();
    MutableLiveData<Boolean> showAppLogin = new MutableLiveData<>();
    MutableLiveData<Boolean> showSelectedTemplate = new MutableLiveData<>();

    private Observer<State> stateObserver = state -> {
        if(state == null)
            return;
        switch (state){
            case AZURE_LOGIN_REQUEST:
                showIndefiniteProgressBar.postValue(true);
                break;
            case AZURE_LOGIN_SUCCESS:
                showIndefiniteProgressBar.postValue(false);
                showSelectApplication.postValue(true);
                break;
            case AZURE_APP_SELECTED:
                showSelectApplication.postValue(false);
                showAppLogin.postValue(true);
                break;
            case AZURE_APP_LOGIN:
                showAppLogin.postValue(false);
                showSelectedTemplate.postValue(true);
                break;
            case AZURE_TEMPLATE_SELECTED:
                showAppLogin.postValue(false);
                showSelectedTemplate.postValue(false);
                showIndefiniteProgressBar.postValue(true);
                break;
            case AZURE_DEVICE_CREATED:
                showIndefiniteProgressBar.postValue(false);
                break;
        }

    };

    public IoTCentralRegisterDeviceViewModel(){
        currentStatus.observeForever(stateObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        currentStatus.removeObserver(stateObserver);
    }
}
