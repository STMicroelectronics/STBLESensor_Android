package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;

import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;
import com.microsoft.aad.adal.AuthenticationResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ConnectionViewModel extends ViewModel {

    public final MutableLiveData<DeviceCredentials> applicationCredential = new MutableLiveData<>();
    public final MutableLiveData<Device> device = new MutableLiveData<>();
    public final MutableLiveData<AuthenticationResult> serviceAuthenticationResult = new MutableLiveData<>();

    public final MutableLiveData<IoTCentralServiceClient> iotClientService = new MutableLiveData<>();
    public final MutableLiveData<IoTCentralResourceManagementClient> resourceIotClientService = new MutableLiveData<>();
    public final MutableLiveData<AuthenticationResult> resourceManagementAuthenticationResult = new MutableLiveData<>();


    private Observer<AuthenticationResult> createIotClient = authenticationResult -> {
        if(authenticationResult == null)
            return;
        try {
            DataClient dataClient = new DataClient(authenticationResult.getAccessToken());
            iotClientService.setValue(new IoTCentralServiceClient(dataClient));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    };

    public boolean hasValidDevice(){
        Device device = this.device.getValue();
        return device!=null && device.getId()!=null;
    }

    private Observer<AuthenticationResult> createResourceClient = authenticationResult -> {
        if(authenticationResult == null)
            return;
        IoTCentralResourceManagementClient.build(authenticationResult, resourceIotClientService::postValue);
    };

    public ConnectionViewModel(){
        serviceAuthenticationResult.observeForever(createIotClient);
        resourceManagementAuthenticationResult.observeForever(createResourceClient);
    }


    public  void setResourceManagementAuthentication(AuthenticationResult authData){
        resourceIotClientService.setValue(null);
        resourceManagementAuthenticationResult.setValue(authData);
    }

    public void loadApplicationCredential(@NonNull Application app){
        IoTCentralServiceClient client = iotClientService.getValue();
        if(client == null) {
            applicationCredential.postValue(null);
            return;
        }//else
        client.getCredentialForApp(app, (app1, credentials) -> applicationCredential.postValue(credentials));

    }

    public void createDevice(@NonNull Application app, @NonNull DeviceTemplate template, @NonNull String deviceName){
        IoTCentralServiceClient client = iotClientService.getValue();
        if(client == null) {
            device.postValue(null);
            return;
        }//else
        client.createDevice(app, template, deviceName, device::postValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        serviceAuthenticationResult.removeObserver(createIotClient);
        resourceManagementAuthenticationResult.removeObserver(createResourceClient);
    }
}
