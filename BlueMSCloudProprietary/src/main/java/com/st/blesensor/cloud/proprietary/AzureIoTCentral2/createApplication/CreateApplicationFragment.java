package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.app.Dialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.templates.ContosoTemplate;
import com.github.lucadruda.iotcentral.service.templates.IoTCTemplate;
import com.github.lucadruda.iotcentral.service.types.ResourceGroup;
import com.github.lucadruda.iotcentral.service.types.Subscription;
import com.github.lucadruda.iotcentral.service.types.Tenant;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.IWindowComponent;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.ConnectionViewModel;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.Constant;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.IoTCentralResourceManagementClient;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication.ApplicationViewModel;
import com.st.blesensor.cloud.proprietary.R;


public class CreateApplicationFragment extends AppCompatDialogFragment {

    private static final IoTCTemplate APP_TEMPLATE = new ContosoTemplate();

    private static final Region[] IOTC_REGIONS = new Region[]{
            Region.US_WEST,
            Region.US_EAST,
            Region.EUROPE_NORTH,
            Region.EUROPE_WEST
    };

    private ConnectionViewModel mConnection;
    private CreateApplicationViewModel mCreateApplication;
    private AuthenticationContext mAuthContext;

    private void createResourceManagerClient(){
        mCreateApplication.isLoading.postValue(true);
        AuthenticationResult authData = mConnection.serviceAuthenticationResult.getValue();
        if(authData!=null) {
            String userId = authData.getUserInfo().getUserId();
            mAuthContext = new AuthenticationContext(requireContext(), Constant.AUTHORITY, true);
            mAuthContext.acquireTokenSilentAsync(Constant.RM_TOKEN_AUDIENCE,Constant.CLIENT_ID,userId,new AuthenticationCallback<AuthenticationResult>(){

                @Override
                public void onSuccess(AuthenticationResult result) {
                    mConnection.setResourceManagementAuthentication(result);
                    mConnection.resourceIotClientService.observe(CreateApplicationFragment.this.getViewLifecycleOwner(), new Observer<IoTCentralResourceManagementClient>() {
                        @Override
                        public void onChanged(@Nullable IoTCentralResourceManagementClient ioTCentralResourceManagementClient) {
                            if (ioTCentralResourceManagementClient == null) {
                                return;
                            }
                            loadTenant(ioTCentralResourceManagementClient);
                            mConnection.resourceIotClientService.removeObserver(this);
                        }
                    });
                }

                @Override
                public void onError(Exception exc) {

                }
            });
        }
    }
    private static final String REDIRECT_URI = "http://localhost";

    private void createTenantResourceManagerClient(Tenant tenant){
        mCreateApplication.isLoading.postValue(true);
        AuthenticationResult authData = mConnection.serviceAuthenticationResult.getValue();
        if(authData!=null) {
            String userId = authData.getUserInfo().getUserId();
            mAuthContext =  new AuthenticationContext(requireContext(), Constant.AUTHORITY_BASE + tenant.getTenantId(),true);
            mAuthContext.acquireTokenSilentAsync(Constant.RM_TOKEN_AUDIENCE,Constant.CLIENT_ID,userId,new AuthenticationCallback<AuthenticationResult>(){

                @Override
                public void onSuccess(AuthenticationResult result) {
                    mCreateApplication.isLoading.postValue(false);
                    mConnection.setResourceManagementAuthentication(result);
                    mConnection.resourceIotClientService.observe(CreateApplicationFragment.this.getViewLifecycleOwner(), new Observer<IoTCentralResourceManagementClient>() {
                        @Override
                        public void onChanged(@Nullable IoTCentralResourceManagementClient ioTCentralResourceManagementClient) {
                            if (ioTCentralResourceManagementClient == null) {
                                return;
                            }
                            loadSubscription(ioTCentralResourceManagementClient);
                            mConnection.resourceIotClientService.removeObserver(this);
                        }
                    });
                }

                @Override
                public void onError(Exception exc) {
                    if(exc instanceof AuthenticationException){
                        ADALError code = ((AuthenticationException) exc).getCode();
                        //requeire the login windonw it the token is not valid
                        if(code == ADALError.AUTH_REFRESH_FAILED_PROMPT_NOT_ALLOWED){
                            //reddirect uri is nullable, but it is need other it show an login error
                            mAuthContext.acquireToken(wrapThis(), Constant.RM_TOKEN_AUDIENCE, Constant.CLIENT_ID, REDIRECT_URI,
                                    null,PromptBehavior.Auto,null, this);
                        }
                    }

                }
            });
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.createApp_title);
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthContext.onActivityResult(requestCode,resultCode,data);
    }

    private IWindowComponent wrapThis(){
        return CreateApplicationFragment.this::startActivityForResult;
    }

    private void loadSubscription(IoTCentralResourceManagementClient client) {
        client.getSubscription(data -> {
            mCreateApplication.subscriptions.postValue(data);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mConnection = ViewModelProviders.of(requireActivity()).get(ConnectionViewModel.class);
        mCreateApplication = ViewModelProviders.of(CreateApplicationFragment.this)
                .get(CreateApplicationViewModel.class);
        if(mCreateApplication.tenants.getValue()==null) {
            loadData(mConnection);
        }

    }

    private void loadData(ConnectionViewModel mConnection) {
        IoTCentralResourceManagementClient client = mConnection.resourceIotClientService.getValue();
        if(client == null){
            createResourceManagerClient();
        }else {
            loadTenant(client);
        }
    }


    private void loadTenant(IoTCentralResourceManagementClient client){
        mCreateApplication.isLoading.postValue(true);
        client.getUserTenant(data ->{
            mCreateApplication.tenants.postValue(data);
            mCreateApplication.isLoading.postValue(false);
        });
    }

    private Spinner mTenantSelector;
    private Spinner mSubscriptionSelector;
    private Spinner mResourceGroupSelector;
    private TextView mAppNAme;
    private ProgressBar mLoadingBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_application,container,false);
        setUpTenantSelector(root);
        setUpSubscriptionSelector(root);
        setUpResourceGroupSelector(root);
        setUpRegionSelector(root);
        setUpLoadingBar(root);

        mAppNAme = root.findViewById(R.id.createApp_nameText);

        root.findViewById(R.id.createApp_createButton).setOnClickListener(view -> createNewApplication());
        return root;
    }

    private void setUpLoadingBar(View root) {
        mLoadingBar = root.findViewById(R.id.create_loadingProgress);
        mCreateApplication.isLoading.observe(getViewLifecycleOwner(),isLoading -> {
            if(isLoading == null || !isLoading){
                mLoadingBar.setVisibility(View.INVISIBLE);
            }else{
                mLoadingBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpRegionSelector(View root) {
        Spinner regionSelector = root.findViewById(R.id.createApp_regionSelector);
        regionSelector.setAdapter(new AzureRegionSelectorAdapter(requireContext(),IOTC_REGIONS));
        regionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Region selectedRegion = (Region) adapterView.getSelectedItem();
                mCreateApplication.selectedRegion.postValue(selectedRegion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void setUpResourceGroupSelector(View root) {
        mResourceGroupSelector = root.findViewById(R.id.createApp_resourceGroupSelector);
        mResourceGroupSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ResourceGroup group = (ResourceGroup) adapterView.getSelectedItem();
                mCreateApplication.selecteResourceGroup.postValue(group);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCreateApplication.resourceGroup.observe(getViewLifecycleOwner(),resourceGroups -> {
            if(resourceGroups == null)
                return;
            mResourceGroupSelector.setAdapter(new ResourceGroupSelectorAdapter(requireContext(),resourceGroups));
        });
    }

    private void setUpSubscriptionSelector(View root) {
        mSubscriptionSelector = root.findViewById(R.id.create_subscriptionSelector);
        mSubscriptionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Subscription selectedSub = (Subscription) adapterView.getSelectedItem();
                mCreateApplication.selectedSubscription.postValue(selectedSub);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCreateApplication.subscriptions.observe(getViewLifecycleOwner(), subscriptions -> {
            if(subscriptions==null)
                return;
            mSubscriptionSelector.setAdapter(new SubscriptionSelectorAdapter(requireContext(),subscriptions));
            selectSubscription(subscriptions);
        });
        mCreateApplication.selectedSubscription.observe(getViewLifecycleOwner(), subscription -> {
            if(subscription == null)
                return;
            loadResourceGroupForSubscription(subscription);
        });
    }

    private void setUpTenantSelector(View root) {
        mTenantSelector = root.findViewById(R.id.createApp_tenantSelector);
        mTenantSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Tenant selectedTenant = (Tenant) adapterView.getSelectedItem();
                mCreateApplication.selectedTenant.postValue(selectedTenant);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCreateApplication.tenants.observe(getViewLifecycleOwner(), tenants -> {
            if(tenants==null || tenants.length == 0){
                return;
            }
            mTenantSelector.setAdapter(new TenantSelectorAdapter(requireContext(),tenants));
            selectTenant(tenants);
        });
        mCreateApplication.selectedTenant.observe(getViewLifecycleOwner(), tenant -> {
            if(tenant==null)
                return;
            createTenantResourceManagerClient(tenant);
        });
    }

    private void createNewApplication() {

        IoTCentralResourceManagementClient client = mConnection.resourceIotClientService.getValue();
        if(client==null)
            return;
        Application app = new Application(mAppNAme.getText().toString(), mAppNAme.getText().toString(),
                mAppNAme.getText().toString(),
                mCreateApplication.selectedRegion.getValue().name(),
                APP_TEMPLATE);
        mCreateApplication.isLoading.postValue(true);
        client.createAppForResource(app, mCreateApplication.selecteResourceGroup.getValue(), new IoTCentralResourceManagementClient.CreateApplicationCallback() {
            @Override
            public void onSuccess() {
                Log.d("CrateApp","Success");
                mCreateApplication.isLoading.postValue(false);
                ApplicationViewModel viewModel = ViewModelProviders.of(requireActivity()).get(ApplicationViewModel.class);
                viewModel.setProvisioningApplication(app);

                dismiss();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("CrateApp","Fail "+e);
            }
        });
    }

    private void loadResourceGroupForSubscription(Subscription subscription) {
        IoTCentralResourceManagementClient client = mConnection.resourceIotClientService.getValue();
        if(client!=null){
            mCreateApplication.isLoading.postValue(true);
            client.getResourceGroupForSubscription(subscription.getSubscriptionId(),
                    data -> {
                        mCreateApplication.isLoading.postValue(false);
                        mCreateApplication.resourceGroup.postValue(data);
                        });
        }
    }

    private void selectTenant(Tenant[] tenants) {
        if(tenants.length == 1){
            mTenantSelector.setSelection(0);
        }else {
            Tenant selected =   mCreateApplication.selectedTenant.getValue();
            if (selected!=null){
                for (int i = 0; i < tenants.length ; i++) {
                    if(tenants[i].getTenantId().equals(selected.getTenantId())){
                        mTenantSelector.setSelection(i);
                        return;
                    }
                }
            }
        }
    }

    private void selectSubscription(Subscription[] subscriptions) {
        if(subscriptions.length == 1){
            mSubscriptionSelector.setSelection(0);
        }else {
            Subscription selected =   mCreateApplication.selectedSubscription.getValue();
            if (selected!=null){
                for (int i = 0; i < subscriptions.length ; i++) {
                    if(subscriptions[i].getTenantId().equals(selected.getTenantId())){
                        mSubscriptionSelector.setSelection(i);
                        return;
                    }
                }
            }
        }
    }
}
