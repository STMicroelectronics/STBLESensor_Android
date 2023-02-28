package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class AzureRegionSelectorAdapter extends SimpleArrayAdapter<Region> {

    public AzureRegionSelectorAdapter(@NonNull Context context, @NonNull Region[] objects) {
        super(context, objects);
    }

    @Override
    protected void bindObject(TextView view, Region obj) {
        view.setText(obj.label());
    }
}
