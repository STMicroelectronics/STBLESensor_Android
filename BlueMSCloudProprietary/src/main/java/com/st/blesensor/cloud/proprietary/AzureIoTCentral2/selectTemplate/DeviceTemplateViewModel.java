package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectTemplate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.IoTCentralServiceClient;

public class DeviceTemplateViewModel extends ViewModel {

    private static final String SUPPORTED_TEMPLATE_PREFIX_NAME = "SensorTile.Box";
    private IoTCentralServiceClient mClient;

    private MutableLiveData<DeviceTemplate[]> mTemplates = new MutableLiveData<>();
    private MutableLiveData<DeviceTemplate> mSelectedTemplate = new MutableLiveData<>();

    DeviceTemplateViewModel(IoTCentralServiceClient client){
        mClient = client;
    }

    public LiveData<DeviceTemplate[]> getDeviceTemplates(){
        return  mTemplates;
    }
    public LiveData<DeviceTemplate> getSelectedDeviceTemplate(){
        return  mSelectedTemplate;
    }

    public void loadTemplatesForApp(Application app){
        loadTemplatesForApp(app,false);
    }

    public void loadTemplatesForApp(Application app,boolean force){
        if(force || !hasValidData()) {
            mClient.getDeviceTemplateForApp(app, deviceTemplates -> {
                if (deviceTemplates == null) {
                    mTemplates.postValue(null);
                    return;
                }//else
                DeviceTemplate searchTemplate = findTemplateWithNamePrefix(deviceTemplates, SUPPORTED_TEMPLATE_PREFIX_NAME);
                //if we found our template select it otherwise let the use choose
                if (searchTemplate != null) {
                    mSelectedTemplate.postValue(searchTemplate);
                } else {
                    mTemplates.postValue(deviceTemplates);
                }
            });
        }
    }

    private boolean hasValidData() {
        DeviceTemplate[] data = mTemplates.getValue();
        return data != null && data.length != 0;
    }

    private @Nullable
    DeviceTemplate findTemplateWithNamePrefix(DeviceTemplate[] templates, @NonNull String prefix){
        for (DeviceTemplate template : templates){
            if(template.getName().startsWith(prefix)){
                return template;
            }
        }
        return null;
    }

    public void selectDeviceTemplate(@NonNull DeviceTemplate app) {
        mSelectedTemplate.postValue(app);
    }

    public static class Factory implements ViewModelProvider.Factory{

        private IoTCentralServiceClient mClient;

        public Factory(IoTCentralServiceClient client){
            mClient = client;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DeviceTemplateViewModel(mClient);
        }
    }

}
