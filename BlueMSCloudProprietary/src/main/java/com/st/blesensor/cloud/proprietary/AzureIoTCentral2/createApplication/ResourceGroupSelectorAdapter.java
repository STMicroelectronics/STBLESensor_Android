package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.types.ResourceGroup;

public class ResourceGroupSelectorAdapter extends SimpleArrayAdapter<ResourceGroup> {

    public ResourceGroupSelectorAdapter(@NonNull Context context, @NonNull ResourceGroup[] objects) {
        super(context, objects);
    }

    @Override
    protected void bindObject(TextView view, ResourceGroup obj) {
        view.setText(obj.getName());
    }
}
