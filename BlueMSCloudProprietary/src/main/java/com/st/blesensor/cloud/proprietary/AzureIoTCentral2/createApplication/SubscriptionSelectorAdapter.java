package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.types.Subscription;

public class SubscriptionSelectorAdapter extends SimpleArrayAdapter<Subscription> {

    public SubscriptionSelectorAdapter(@NonNull Context context, @NonNull Subscription[] objects) {
        super(context, objects);
    }

    @Override
    protected void bindObject(TextView view, Subscription obj) {
        view.setText(obj.getDisplayName());
    }
}
