package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.provisionApplication;

import android.app.Dialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lucadruda.iotcentral.service.Application;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication.ApplicationViewModel;
import com.st.blesensor.cloud.proprietary.R;

public class ProvisionApplicationDialogFragment extends AppCompatDialogFragment {

    private static final long APP_REFRES_DELAY_MS = 1000;
    private Handler uiThread = new Handler(Looper.getMainLooper());

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d =  super.onCreateDialog(savedInstanceState);
        d.setTitle(R.string.azure_iotCentral_provisioning_title);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_application_provisioning,container,false);
    }

    private ApplicationViewModel applicationViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationViewModel = ViewModelProviders.of(requireActivity()).get(ApplicationViewModel.class);
        Application provisioningApplication = applicationViewModel.getProvisioning().getValue();
        applicationViewModel.getApplications().observe(getViewLifecycleOwner(), applications -> {
            if(applications == null){
                applicationViewModel.loadApps();
                return;
            }
            Application foundApp = containsAppWithName(applications,provisioningApplication);
            if(foundApp!=null){
                applicationViewModel.selectApplication(foundApp);
                dismiss();
            }else{
                uiThread.postDelayed(() -> applicationViewModel.loadApps(true),APP_REFRES_DELAY_MS);
            }
        });
    }

    private static @Nullable Application containsAppWithName(Application[] applications, Application appToSearch){
        String name = appToSearch.getName();
        for(Application app : applications){
            if(app.getName().equals(name)){
                return app;
            }
        }
        return null;
    }

}
