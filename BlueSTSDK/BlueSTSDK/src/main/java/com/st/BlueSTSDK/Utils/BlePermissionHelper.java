/*
 *  Copyright (c) 2018  STMicroelectronics – All rights reserved
 *  The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 *  - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 *  - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 *  - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *  OF SUCH DAMAGE.
 */

package com.st.BlueSTSDK.Utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;
import com.st.BlueSTSDK.R;

public class BlePermissionHelper{
    /**
     * request id for the activity that will ask to the user to enable the bt
     */
    public static final int REQUEST_ENABLE_BT = 1;
    /**
     * request id for grant the location permission
     */
    public static final int REQUEST_LOCATION_ACCESS = 2;
    /**
     * request id for grant the Bluetooth Scan permission
     */
    public static final int REQUEST_BLUETOOTH_SCAN = 3;

    /**
     * request id for grant the Bluetooth Connect permission
     */
    public static final int REQUEST_BLUETOOTH_CONNECT = 4;

    private final @NonNull Context mCtx;
    private final @Nullable
    FragmentActivity mActivity;
    private final @Nullable
    Fragment mFragment;

    /**
     * @param src fragment that will trigger the open of the file selector
     */
    public BlePermissionHelper(@NonNull Fragment src) {
        this.mFragment = src;
        this.mCtx = src.requireContext();
        this.mActivity=null;
    }

    public BlePermissionHelper(@NonNull FragmentActivity src) {
        this.mFragment = null;
        mActivity = src;
        mCtx = mActivity;
    }


    private Activity requireActivity(){
        if(mFragment!=null)
            return mFragment.requireActivity();
        if(mActivity!=null)
            return mActivity;
        throw new IllegalStateException("Fragment or activity must be != null");
    }

    private void requestPermissions(String[] permission, int requestCode){
        if(mFragment!=null)
            mFragment.requestPermissions(permission,requestCode);
        else if (mActivity!=null)
            ActivityCompat.requestPermissions(mActivity,permission,requestCode);
        else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    private void startActivityForResult(Intent request,int requestCode){
        if(mFragment!=null)
            mFragment.startActivityForResult(request,requestCode);
        else if (mActivity!=null)
            mActivity.startActivityForResult(request,requestCode);
        else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    /**
     * check that the bluetooth is enabled
     * @return true if the bluetooth is enable false if we ask to the user to enable it
     */
    private boolean enableBluetoothAdapter(){
        final BluetoothManager bluetoothManager =
                ContextCompat.getSystemService(mCtx,BluetoothManager.class);
        if(bluetoothManager==null)
            throw new IllegalStateException("Bluetooth adapter is needed by this app!");
        //the adapter is !=null since we request in the manifest to have the bt capability
        final BluetoothAdapter btAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT );
            return false;
        }else
            return true;
    }//enableBluetoothAdapter

    /**
     * check that the location service is enabled
     * @return true if the location service is enabled, false if we ask to the user to do it
     */
    private boolean enableLocationService(){
        final LocationManager lm =  ContextCompat.getSystemService(mCtx,LocationManager.class);
        if(lm == null)
            throw new IllegalStateException("Location manager adapter is needed by this app!");
        boolean providerEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) |
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!providerEnabled) {
            Resources res = mCtx.getResources();
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
            dialog.setMessage(res.getString(R.string.EnablePositionService));
            dialog.setPositiveButton(res.getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            mCtx.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            paramDialogInterface.cancel();
                        }
                    });
            dialog.setNegativeButton(res.getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            paramDialogInterface.cancel();
                            Toast.makeText(mCtx,
                                    R.string.LocationNotGranted,
                                    Toast.LENGTH_SHORT).show();
                        }//onClick
                    });
                    dialog.show();
        }//if
        return providerEnabled;
    }//enableLocationService

    /**
     * check to have the permission needed for start a bluetooth scanning
     * @return true if we have ti false if we ask for it
     */
    private boolean checkBlePermissionLocation(){
        if (ContextCompat.checkSelfPermission(mCtx,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
                dialog.setTitle("Permission required");
                dialog.setMessage(mCtx.getString(R.string.LocationCoarseRationale));
                dialog.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_ACCESS);
                            }
                        });
                dialog.show();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_ACCESS);
            }//if-else
            return false;
        }
        return  true;
    }//checkBlePermissionLocation

    private boolean checkBlePermissionScan(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(mCtx,
                    Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.BLUETOOTH_SCAN)) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
                    dialog.setTitle("Permission required");
                    dialog.setMessage(mCtx.getString(R.string.BluetoothScanRationale));
                    dialog.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.S)
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    requestPermissions(
                                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                            REQUEST_BLUETOOTH_SCAN);
                                }
                            });
                    dialog.show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissions(
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_BLUETOOTH_SCAN);
                }//if-else
                return false;
            }
        }
        return  true;
    }//checkBlePermissionScan


    private boolean checkPermissionConnection(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // only for Android API Bigger that 31
            if (ContextCompat.checkSelfPermission(mCtx,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.BLUETOOTH_CONNECT)) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
                    dialog.setTitle("Permission required");
                    dialog.setMessage(mCtx.getString(R.string.BluetoothConnectRationale));
                    dialog.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.S)
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    requestPermissions(
                                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                            REQUEST_BLUETOOTH_CONNECT);
                                }
                            });
                    dialog.show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissions(
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_BLUETOOTH_CONNECT);
                }//if-else
                return false;
            }
        }
        return  true;
    }//checkBlePermission


    /**
     * check to have the permission and the service enabled needed for stat a bluetooth scanning
     * @return true if we have all the requirements, false if we ask for something
     */
    public boolean checkAdapterAndPermission(){

        final BluetoothManager bluetoothManager =
                ContextCompat.getSystemService(mCtx,BluetoothManager.class);
        final BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
        if(btAdapter!=null) {
            if (checkPermissionConnection()) {
                if (enableBluetoothAdapter()) {
                    if (enableLocationService())
                        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkBlePermissionLocation()) {
                            return checkBlePermissionScan();
                        }
                }//if
            }
        }
        return false;
    }//checkAdapterAndPermission

    /**
     * to be called to check if the user has switched on the ble adapter
     * @param requestCode
     * @param resultCode
     * @param data
     * @return null if the request code is not a bleEnable, true/false if the the user enable the ble
     */
    public @Nullable Boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            return resultCode == Activity.RESULT_OK;
        }//if request
        return null;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0 )
            return;

        if(requestCode == REQUEST_LOCATION_ACCESS){
            // If request is cancelled, the result arrays are empty.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have the permission to start the scan again
                Toast.makeText(mCtx, R.string.LocationGranted, Toast.LENGTH_SHORT).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),permissions[0])) {
                // User selected the Never Ask Again Option
                Toast.makeText(mCtx, R.string.LocationNotGranted, Toast.LENGTH_SHORT).show();
                mActivity.finish();
            } else {
                Toast.makeText(mCtx, R.string.LocationNotGranted, Toast.LENGTH_SHORT).show();
            }//if-else
        } else if(requestCode == REQUEST_BLUETOOTH_SCAN){
            // If request is cancelled, the result arrays are empty.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have the permission to start the scan again
                Toast.makeText(mCtx, R.string.BluetoothScanGranted, Toast.LENGTH_SHORT).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),permissions[0])) {
                // User selected the Never Ask Again Option
                Toast.makeText(mCtx, R.string.BluetoothScanNotGranted, Toast.LENGTH_SHORT).show();
                mActivity.finish();
            } else {
                Toast.makeText(mCtx, R.string.BluetoothScanNotGranted, Toast.LENGTH_SHORT).show();
            }//if-else
        } else if(requestCode == REQUEST_BLUETOOTH_CONNECT){
            // If request is cancelled, the result arrays are empty.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have the permission to start the scan again
                Toast.makeText(mCtx, R.string.BluetoothConnectGranted, Toast.LENGTH_SHORT).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),permissions[0])) {
                // User selected the Never Ask Again Option
                Toast.makeText(mCtx, R.string.BluetoothConnectNotGranted, Toast.LENGTH_SHORT).show();
                mActivity.finish();
            } else {
                Toast.makeText(mCtx, R.string.BluetoothConnectNotGranted, Toast.LENGTH_SHORT).show();
            }//if-else
        }
    }//onRequestPermissionsResult
}