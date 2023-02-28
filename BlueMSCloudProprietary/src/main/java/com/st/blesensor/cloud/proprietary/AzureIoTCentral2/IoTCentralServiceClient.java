package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoTCentralServiceClient {

    private static long COMMAND_RETRY_DELAY_MS = 4000;
    //private static String TAG = "IoTCentralServiceClient";

    private ExecutorService mBackgroundThread = Executors.newFixedThreadPool(1);
    private DataClient mDataClient;


    IoTCentralServiceClient(DataClient client){
        mDataClient = client;
    }

    public void getApplications(ListApplicationCallback callback){
        mBackgroundThread.execute(() -> {
            try {
                Application[] apps = mDataClient.listApps();
                callback.onResult(apps);
            } catch (DataException e) {
                e.printStackTrace();
                callback.onResult(null);
            }
        });
    }


    public void getDeviceTemplateForApp(@NonNull Application application , ListDeviceTemplateCallback callback){
        mBackgroundThread.execute(() -> {
            try {
                callback.onResult(mDataClient.listTemplates(application.getId()));
            } catch (DataException e) {
                e.printStackTrace();
                callback.onResult(null);
            }

        });
    }

    private @Nullable Device findDeviceWithName(String name, Device[] devices){
        for (Device d : devices){
            if(d.getName().equals(name)){
                return d;
            }
        }
        return null;
    }

    void createDevice(Application app, DeviceTemplate template, String deviceName, DeviceCreationCallback callback) {
        mBackgroundThread.execute(() -> {
            try {
                Device[] deviceList = mDataClient.listDevices(app.getId(),template.getId());
                Device device = findDeviceWithName(deviceName,deviceList);
                if(device == null){
                    device = mDataClient.createDevice(app.getId(),deviceName,template.getId());
                }
                callback.onCreate(device);
            } catch (DataException e) {
                e.printStackTrace();
                callback.onCreate(null);
            }
        });
    }


    void getCredentialForApp(Application app, ApplicationCredentialCallback callback){
        mBackgroundThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DeviceCredentials credentials = mDataClient.getCredentials(app.getId());
                    callback.onReceivedCredentialForApp(app,credentials);
                } catch (Exception e) {
                    String message = e.getMessage();
                    //{"error":{"appId":"2b9a6fae-0aae-4767-acec-c2f2ddfd024d","code":"503.072.010.004",
                    // "message":"Application still provisioning: 2b9a6fae-0aae-4767-acec-c2f2ddfd024d"}}
                    if(message.contains("error") && message.contains("provisioning")){
                        try {
                            Thread.sleep(COMMAND_RETRY_DELAY_MS);
                        } catch (InterruptedException interruptExc) { }
                        mBackgroundThread.submit(this);
                    }
                    e.printStackTrace();
                    callback.onReceivedCredentialForApp(app,null);
                }
            }
        });
    }

    public interface ListApplicationCallback{
        void onResult(@Nullable Application[] applications);
    }

    public interface ListDeviceTemplateCallback{
        void onResult(@Nullable DeviceTemplate[] templates);
    }

    public interface DeviceCreationCallback{
        void onCreate(@Nullable Device device);
    }

    public interface ApplicationCredentialCallback{
        void onReceivedCredentialForApp(@NonNull Application app, @Nullable DeviceCredentials credentials);
    }

}
