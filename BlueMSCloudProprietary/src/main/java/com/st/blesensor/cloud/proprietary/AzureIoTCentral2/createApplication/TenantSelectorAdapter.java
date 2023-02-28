package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.types.Tenant;

public class TenantSelectorAdapter  extends SimpleArrayAdapter<Tenant> {

    public TenantSelectorAdapter(@NonNull Context context, @NonNull Tenant[] objects) {
        super(context, objects);
    }

    @Override
    protected void bindObject(TextView view, Tenant obj) {
        view.setText(obj.getDisplayName());
    }
}
