package com.st.trilobyte.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import com.st.trilobyte.R;
import com.st.trilobyte.helper.DeviceHelperKt;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.PeripheralListener;

public class TrilobyteActivity extends AppCompatActivity {

    public final static String FINISH_ACTIVITY_ACTION = "com.st.trilobyte.intent.action.CLOSE";

    private final static int REQUEST_DANGEROUS_PERMISSIONS = 10;

    private final static int REQUEST_ENABLE_BT = 1000;

    private final static int REQUEST_ENABLE_GPS = 1001;

    private PeripheralListener mPeripheralListener;

    private BroadcastReceiver mFlowUploadedBroacastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            onFinishBroadcastReceived(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(FINISH_ACTIVITY_ACTION);
        manager.registerReceiver(mFlowUploadedBroacastReceiver, intentFilter);
    }



    private void enableGPS() {
        //check GPS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_DANGEROUS_PERMISSIONS);
            return;
        }

        if (!DeviceHelperKt.hasGPSEnabled(this)) {
            DialogHelper.showDialog(this, getString(R.string.warn_need_gps_ble_scan), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(gpsOptionsIntent, REQUEST_ENABLE_GPS);
                }
            });
            return;
        }

        enableBluetooth();
    }

    protected void enableBluetooth() {
        if (!DeviceHelperKt.isBluetoothEnabled(this)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (mPeripheralListener != null) {
            mPeripheralListener.onPeripheralready();
        }
    }

    protected void onFinishBroadcastReceived(Intent intent) {
        detachFinishBroadcastReceiver();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_DANGEROUS_PERMISSIONS) {
            for (final int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (mPeripheralListener != null) {
                        mPeripheralListener.onPeripheralError();
                    }
                    return;
                }
            }

            enableGPS();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_GPS) {

            if (!DeviceHelperKt.hasGPSEnabled(this)) {
                if (mPeripheralListener != null) {
                    mPeripheralListener.onPeripheralError();
                }
                return;
            }

            enableBluetooth();

        } else if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                if (mPeripheralListener != null) {
                    mPeripheralListener.onPeripheralError();
                }
                return;
            }

            if (mPeripheralListener != null) {
                mPeripheralListener.onPeripheralready();
            }
        }
    }

    protected void detachFinishBroadcastReceiver() {
        if (mFlowUploadedBroacastReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mFlowUploadedBroacastReceiver);
            mFlowUploadedBroacastReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        detachFinishBroadcastReceiver();
        super.onDestroy();
    }
}
