package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.loginApplication;

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
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.ConnectionViewModel;
import com.st.blesensor.cloud.proprietary.R;

public class LoginApplicationDialogFragment extends AppCompatDialogFragment {

    private static final long APP_REFRES_DELAY_MS = 1000;
    private Handler uiThread = new Handler(Looper.getMainLooper());

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d =  super.onCreateDialog(savedInstanceState);
        d.setTitle(R.string.azure_iotCentral_app_login_title);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_application_login,container,false);
    }

    private ConnectionViewModel connectionViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
