package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.annotation.Nullable;

import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.ResourceGroup;
import com.github.lucadruda.iotcentral.service.types.Subscription;
import com.github.lucadruda.iotcentral.service.types.Tenant;
import com.microsoft.aad.adal.AuthenticationResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoTCentralResourceManagementClient {

    private static String TAG = "IoTCentralServiceClient";

    private final static ExecutorService sBackgroundThread = Executors.newFixedThreadPool(1);
    private ARMClient mResourceClient;

    //need the build method because the ARMClient constructor do network calls
    public static void build(AuthenticationResult auth, BuildCallback callback){
        sBackgroundThread.submit(() -> {
            ARMClient dataClient = null;
            try {
                dataClient = new ARMClient(auth.getAccessToken());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if(dataClient!=null) {
                callback.onResult(new IoTCentralResourceManagementClient(dataClient));
            }else{
                callback.onResult(null);
            }
        });

    }

    IoTCentralResourceManagementClient(ARMClient resourceClient){
        mResourceClient = resourceClient;
    }

    public void getUserTenant(ArrayDataCallback<Tenant> callback){
        sBackgroundThread.submit(() -> {
            try {
                Tenant data[] = mResourceClient.listTenants();
                callback.onResult(data);
            } catch (DataException e) {
                e.printStackTrace();
                callback.onResult(null);
            }
        });
    }

    public void getSubscription(ArrayDataCallback<Subscription> callback) {
        sBackgroundThread.submit(() -> {
            try {
                Subscription data[] = mResourceClient.listSubscriptions();
                callback.onResult(data);
            } catch (DataException e) {
                e.printStackTrace();
                callback.onResult(null);
            }
        });
    }

    public void getResourceGroupForSubscription(String subscriptionId, ArrayDataCallback<ResourceGroup> callback){
        sBackgroundThread.submit(() -> {
            try{
                ResourceGroup data[] = mResourceClient.listResourceGroups(subscriptionId);
                callback.onResult(data);
            }catch (DataException e){
                e.printStackTrace();
                callback.onResult(null);
            }
        });
    }

    public void createAppForResource(Application newApp, ResourceGroup appResourceGroup, CreateApplicationCallback callback) {
        sBackgroundThread.submit( () -> {
            mResourceClient.setResourceGroup(appResourceGroup.getName());
            try {
                mResourceClient.createApplication(newApp);
                callback.onSuccess();
            } catch (IOException e) {
                e.printStackTrace();
                callback.onError(e);
            }
        });
    }


    public interface ArrayDataCallback<T>{
        void onResult(@Nullable T data[]);
    }


    public interface BuildCallback{
        void onResult(@Nullable IoTCentralResourceManagementClient client);
    }

    public interface CreateApplicationCallback{
        void onSuccess();
        void onError(Throwable e);
    }


}
