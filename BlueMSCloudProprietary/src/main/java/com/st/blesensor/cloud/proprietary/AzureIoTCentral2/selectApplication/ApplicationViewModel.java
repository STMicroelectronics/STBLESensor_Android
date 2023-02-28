package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.templates.ContosoTemplate;
import com.github.lucadruda.iotcentral.service.templates.IoTCTemplate;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.IoTCentralServiceClient;

import java.util.ArrayList;

public class ApplicationViewModel extends ViewModel {

    private static IoTCTemplate SUPPORTED_APP_TEMPLATE = new ContosoTemplate();

    private IoTCentralServiceClient mClient;

    private MutableLiveData<Application[]> mApps = new MutableLiveData<>();
    private MutableLiveData<Application> mSelectedApp = new MutableLiveData<>();
    private MutableLiveData<Application> mProvisioningApp = new MutableLiveData<>();

    ApplicationViewModel(@NonNull IoTCentralServiceClient client){
        mClient = client;
    }

    public LiveData<Application[]> getApplications(){
        return  mApps;
    }
    public LiveData<Application> getSelectedApplication(){
        return  mSelectedApp;
    }
    public LiveData<Application> getProvisioning(){
        return  mProvisioningApp;
    }

    public void loadApps(){
        loadApps(false);
    }

    public void loadApps(boolean force){
        if(force || !hasValidData())
        mClient.getApplications(applications ->{
            if(applications!=null) {
                Application[] supportedApp =
                        filterApplicationWithTemplate(applications, SUPPORTED_APP_TEMPLATE);
                mApps.postValue(supportedApp);
            }else{
                mApps.postValue(null);
            }//if-else
        });
    }

    private boolean hasValidData() {
        Application[] data = mApps.getValue();
        return data != null && data.length != 0;
    }

    private static Application[] filterApplicationWithTemplate(@NonNull Application[] apps,
                                                               @NonNull IoTCTemplate requestTemplate){
        ArrayList<Application> correctApp = new ArrayList<>();
        for ( Application app : apps){
            //if template info is not present, let the use choose it
            if(app.getTemplate() == null){
                correctApp.add(app);
            }else {
                if (app.getTemplate().id().equals(requestTemplate.id())) {
                    correctApp.add(app);
                }
            }
        }
        return correctApp.toArray(new Application[0]);
    }

    public void selectApplication(@NonNull Application app) {
        mSelectedApp.postValue(app);
    }

    public void setProvisioningApplication(Application app) {
        mProvisioningApp.postValue(app);
    }

    public static class Factory implements ViewModelProvider.Factory{

        private IoTCentralServiceClient mClient;

        public Factory(IoTCentralServiceClient client){
            mClient = client;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ApplicationViewModel(mClient);
        }
    }


}
