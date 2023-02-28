package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.IWindowComponent;
import com.microsoft.aad.adal.PromptBehavior;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.loginApplication.LoginApplicationDialogFragment;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.provisionApplication.ProvisionApplicationDialogFragment;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication.ApplicationSelectorFragment;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication.ApplicationViewModel;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectTemplate.DeviceTemplateSelectorFragment;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectTemplate.DeviceTemplateViewModel;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage.DeviceConnectionSettings;
import com.st.blesensor.cloud.proprietary.R;

import static com.st.blesensor.cloud.proprietary.AzureIoTCentral2.IoTCentralRegisterDeviceViewModel.State;

public class IoTCentralLoginFragment extends Fragment {

    private static final String TAG = "IoTCentralLoginFragment";
    private static final String SELECT_APP_TAG = IoTCentralLoginFragment.class.getCanonicalName()+".SELECT_APP_TAG";
    private static final String LOGIN_APP_TAG = IoTCentralLoginFragment.class.getCanonicalName()+".LOGIN_APP_TAG";
    private static final String SELECT_TEMPLATE_TAG = IoTCentralLoginFragment.class.getCanonicalName()+".SELECT_TEMPLATE_TAG";
    private static final String PROVISIONING_APP_TAG = IoTCentralLoginFragment.class.getCanonicalName()+".PROVISIONING_APP_TAG";
    private static final String REDIRECT_URI = "http://localhost";

    private AuthenticationContext mAuthContext;
    private ApplicationViewModel mApplicationViewModel;
    private DeviceTemplateViewModel mTemplatesViewModel;
    private ConnectionViewModel mConnectionViewModel;
    private IoTCentralRegisterDeviceViewModel mRegisterDeviceStatus;

    private String mDeviceMac;
    private String mDeviceName;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private StoredConnectionViewModel mStoredConnectionParam;
    private @Nullable DeviceConnectionSettings mKnowSettings = null;

    public IoTCentralLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.cloud_config_azure_iotcentral2, container, false);
        mProgressBar = root.findViewById(R.id.azure_iotCentral_registrationProgress);
        mProgressText = root.findViewById(R.id.azure_iotCentral_registrationStatusText);
        root.findViewById(R.id.azure_iotCentral_registerDevice).setOnClickListener(
                view -> {
                    mKnowSettings=null;
                    doAzureLogin();
                }
        );
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mConnectionViewModel = ViewModelProviders.of(requireActivity()).get(ConnectionViewModel.class);
        mRegisterDeviceStatus = ViewModelProviders.of(requireActivity()).get(IoTCentralRegisterDeviceViewModel.class);
        mStoredConnectionParam = ViewModelProviders.of(this).get(StoredConnectionViewModel.class);
        attachStoredConnectionParameters(mStoredConnectionParam);
        attachRegisterDeviceStatus(mRegisterDeviceStatus);
        attachConnectionViewModel(mConnectionViewModel);
    }

    private void attachStoredConnectionParameters(StoredConnectionViewModel viewModel){
        viewModel.loadSettings(mDeviceMac,mDeviceName).observe(getViewLifecycleOwner(), settings -> {
            if(settings != null){
                mRegisterDeviceStatus.currentStatus.postValue(State.Azure_DEVICE_KNOWED);
                mKnowSettings = settings;
            }
        });
    }

    private void attachConnectionViewModel(ConnectionViewModel viewModel) {
        viewModel.iotClientService.observe(getViewLifecycleOwner(), ioTCentralServiceClient -> {
            if(ioTCentralServiceClient==null)
                return;
            mApplicationViewModel = ViewModelProviders.of(requireActivity(),
                    new ApplicationViewModel.Factory(ioTCentralServiceClient))
                    .get(ApplicationViewModel.class);
            attachApplicationViewModel(mApplicationViewModel);
            mTemplatesViewModel = ViewModelProviders.of(requireActivity(),
                    new DeviceTemplateViewModel.Factory(ioTCentralServiceClient))
                    .get(DeviceTemplateViewModel.class);
            attachDeviceTemplateViewModel(mTemplatesViewModel);
            if(mRegisterDeviceStatus.currentStatus.getValue() == State.AZURE_LOGIN_REQUEST) {
                mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_LOGIN_SUCCESS);
            }
        });

        viewModel.device.observe(getViewLifecycleOwner(), device -> {
            if(viewModel.hasValidDevice())
                mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_DEVICE_CREATED);
        });

        viewModel.applicationCredential.observe(getViewLifecycleOwner(), deviceCredentials -> {
            if(deviceCredentials!=null && !viewModel.hasValidDevice()){
                mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_APP_LOGIN);
            }
        });
    }

    private void doAzureLogin(){
        mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_LOGIN_REQUEST);
        mAuthContext = new AuthenticationContext(requireContext(), Constant.AUTHORITY, true);
        mAuthContext.acquireToken(wrapThis(), Constant.IOTC_TOKEN_AUDIENCE,
                Constant.CLIENT_ID, REDIRECT_URI,null, PromptBehavior.Auto,null,
                getAuthInteractiveCallback());
    }

    private void attachRegisterDeviceStatus(IoTCentralRegisterDeviceViewModel viewModel){

        viewModel.currentStatus.observe(getViewLifecycleOwner(),state -> {
            if(state == null){
                return;
            }
            showStatusText(state);
        });

        viewModel.showIndefiniteProgressBar.observe(getViewLifecycleOwner(), showProgressBar -> {
            if(showProgressBar ==null)
                return;
            if(showProgressBar){
                mProgressBar.setVisibility(View.VISIBLE);
            }else{
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        viewModel.showSelectApplication.observe(getViewLifecycleOwner(),showSelectApplication -> {
            if(showSelectApplication == null)
                return;
            if(showSelectApplication){
                Fragment appSelector = new ApplicationSelectorFragment();
                addFragmentWithTag(appSelector,SELECT_APP_TAG);
            }else{
                removeFragmentWithTag(SELECT_APP_TAG);
            }
        });

        viewModel.showAppLogin.observe(getViewLifecycleOwner(),showAppLoginProgress -> {
            if(showAppLoginProgress == null)
                return;
            if(showAppLoginProgress){
                Fragment appSelector = new LoginApplicationDialogFragment();
                addFragmentWithTag(appSelector,LOGIN_APP_TAG);
            }else{
                removeFragmentWithTag(LOGIN_APP_TAG);
            }
        });

        viewModel.showSelectedTemplate.observe(getViewLifecycleOwner(),showSelectTemplate -> {
            if(showSelectTemplate == null)
                return;
            Log.d("Azure","showSelectTemplate: "+showSelectTemplate);
            if(showSelectTemplate){
                Fragment deviceSelector = new DeviceTemplateSelectorFragment();
                addFragmentWithTag(deviceSelector,SELECT_TEMPLATE_TAG);
            }else{
                removeFragmentWithTag(SELECT_TEMPLATE_TAG);
            }
        });
    }

    private void showStatusText(State currentState){
        Log.d("Azure","Status: "+currentState);
        switch (currentState){
            case AZURE_LOGIN_REQUEST:
                mProgressText.setText(R.string.azure_iotCentral_loginRequest);
                break;
            case AZURE_LOGIN_SUCCESS:
                mProgressText.setText(R.string.azure_iotCentral_selectApplication);
                break;
            case AZURE_APP_SELECTED:
                mProgressText.setText(R.string.azure_iotCentral_selectTemplate);
                break;
            case AZURE_APP_LOGIN:
                mProgressText.setText(R.string.azure_iotCentral_loginApplication);
                break;
            case AZURE_TEMPLATE_SELECTED:
                mProgressText.setText(R.string.azure_iotCentral_createDevice);
                break;
            case AZURE_DEVICE_CREATED:
                mProgressText.setText(R.string.azure_iotCentral_creationComplete);
                break;
            case Azure_DEVICE_KNOWED:
                mProgressText.setText(R.string.azure_iotCentral_knowDevice);
                break;
        }
    }

    private void attachApplicationViewModel(ApplicationViewModel applicationViewModel){
        applicationViewModel.getSelectedApplication().observe(getViewLifecycleOwner(),application -> {
            if(application == null)
                return;
            mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_APP_SELECTED);
            mConnectionViewModel.loadApplicationCredential(application);
        });
        applicationViewModel.getProvisioning().observe(getViewLifecycleOwner(),application -> {
            if(application == null){
                return;
            }
            Fragment provisioningApp = new ProvisionApplicationDialogFragment();
            addFragmentWithTag(provisioningApp,PROVISIONING_APP_TAG);
        });
    }

    private void removeFragmentWithTag(String tag){
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if(fragment!=null) {
            fm.beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private void addFragmentWithTag(Fragment fragment, String tag){
        FragmentManager fm = getChildFragmentManager();
        Fragment oldFragment = fm.findFragmentByTag(tag);
        FragmentTransaction transaction = fm.beginTransaction();
        if(oldFragment!=null){
            transaction.remove(oldFragment);
        }
        transaction.add(fragment,tag);
        transaction.commit();
    }

    private void attachDeviceTemplateViewModel(DeviceTemplateViewModel viewModel) {

        viewModel.getSelectedDeviceTemplate().observe(getViewLifecycleOwner(), template -> {
            Application app = mApplicationViewModel.getSelectedApplication().getValue();
            if(app==null || template==null || mDeviceName == null){
                return;
            }
            mRegisterDeviceStatus.currentStatus.postValue(State.AZURE_TEMPLATE_SELECTED);
            mConnectionViewModel.createDevice(app,template,mDeviceName);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthContext.onActivityResult(requestCode,resultCode,data);
    }

    private IWindowComponent wrapThis(){
        return IoTCentralLoginFragment.this::startActivityForResult;
    }


    private AuthenticationCallback<AuthenticationResult> getAuthInteractiveCallback() {
        return new AuthenticationCallback<AuthenticationResult>() {


            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
/*
                Log.d(TAG, "Authority: " + authenticationResult.getAuthority());
                Log.d(TAG, "Resource: " + authenticationResult.getIsMultiResourceRefreshToken());
                Log.d(TAG, "Token: " + authenticationResult.getAccessToken());
                Log.d(TAG, "RToken: " + authenticationResult.getRefreshToken());
*/
                mConnectionViewModel.serviceAuthenticationResult.postValue(authenticationResult);
            }

            @Override
            public void onError(Exception exception) {
                if (exception == null) {
                    return;
                }

                /* Failed to acquireToken */
                String msg = "Authentication failed: " + exception.toString();
                if (exception instanceof AuthenticationException) {
                    ADALError error = ((AuthenticationException) exception).getCode();
                    if (error == ADALError.AUTH_FAILED_CANCELLED) {
                        msg = "The user cancelled the authorization request";
                    } else if (error == ADALError.AUTH_FAILED_NO_TOKEN) {
                        // In this case ADAL has found a token in cache but failed to retrieve it.
                        // Retry interactive with Prompt.Always to ensure we do an interactive sign in
                        return;
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        msg = "Device is in doze mode or the app is in standby mode";
                    }
                }
                Log.d(TAG, "Error: " + msg);
            }
        };
    }

    public void setDeviceInfo(@NonNull String deviceMac,@NonNull String deviceName) {
        mDeviceName = deviceName;
        mDeviceMac = deviceMac;
    }

    public @Nullable
    AzureIotCentralFactory2.ConnectionParameters getConnectionParam(){
        if(mKnowSettings!=null){
            return new AzureIotCentralFactory2.ConnectionParameters(mKnowSettings.appName,
                    new IoTCentralClient2.Parameters(mKnowSettings.scopeId,
                            mKnowSettings.appKey,mKnowSettings.deviceId));
        }//else
        DeviceCredentials appCredential = mConnectionViewModel.applicationCredential.getValue();
        Device device = mConnectionViewModel.device.getValue();
        Application app = mApplicationViewModel != null ? mApplicationViewModel.getSelectedApplication().getValue() : null;
        if(appCredential == null && app != null){ //if the credential are not valid try to reload id for the next execution
            mConnectionViewModel.loadApplicationCredential(app);
        }
        if(app == null || appCredential == null || device == null || device.getDeviceId() == null){
            return null;
        }else{
            IoTCentralClient2.Parameters param = new IoTCentralClient2.Parameters(
                    appCredential.getIdScope(),
                    appCredential.getPrimaryKey(),
                    device.getDeviceId());

            AzureIotCentralFactory2.ConnectionParameters connectionParameters =
                    new AzureIotCentralFactory2.ConnectionParameters(app.getSubdomain(),param);
            mStoredConnectionParam.storeSettings(mDeviceMac,mDeviceName,connectionParameters);
            return  connectionParameters;
        }
    }

}
