package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.lucadruda.iotcentral.service.types.ResourceGroup;
import com.github.lucadruda.iotcentral.service.types.Subscription;
import com.github.lucadruda.iotcentral.service.types.Tenant;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class CreateApplicationViewModel extends ViewModel {

    MutableLiveData<Tenant[]> tenants = new MutableLiveData<>();
    MutableLiveData<Tenant> selectedTenant = new MutableLiveData<>();
    MutableLiveData<Subscription> selectedSubscription = new MutableLiveData<>();
    MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    MutableLiveData<Region> selectedRegion = new MutableLiveData<>();
    MutableLiveData<Subscription[]> subscriptions = new MutableLiveData<>();
    MutableLiveData<ResourceGroup[]> resourceGroup = new MutableLiveData<>();
    MutableLiveData<ResourceGroup> selecteResourceGroup = new MutableLiveData<>();

}
