/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueSTSDK.gui.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.st.BlueSTSDK.gui.R;

public class SDWritePermissionUtil {

    public interface OnWritePermissionAcquiredCallback{
        void onWritePermissionAcquired();
    }

    private static final int RESULT_WRITE_REQUEST = 2;


    private final @NonNull Context mCtx;
    private final @Nullable FragmentActivity mActivity;
    private final @Nullable Fragment mFragment;
    private final @NonNull View mRootView;
    private OnWritePermissionAcquiredCallback mCallback;

    /**
     *
     * @param src fragment that will trigger the open of the file selector
     * @param rootView view where show the shankbar with the request/errors
     */
    public SDWritePermissionUtil(@NonNull Fragment src,@NonNull View rootView) {
        this.mFragment = src;
        this.mCtx = src.requireContext();
        this.mRootView = rootView;
        this.mActivity=null;
    }

    public SDWritePermissionUtil(@NonNull FragmentActivity src,@NonNull View rootView) {
        this.mFragment = null;
        mActivity = src;
        this.mRootView = rootView;
        mCtx = mActivity;
    }


    private Activity requireActivity(){
        if(mFragment!=null)
            return mFragment.requireActivity();
        if(mActivity!=null)
            return mActivity;
        throw new IllegalStateException("Fragment or activity must be != null");
    }

    private void requestPermissions(String permission[],int requestCode){
        if(mFragment!=null)
            mFragment.requestPermissions(permission,requestCode);
        else if (mActivity!=null)
            ActivityCompat.requestPermissions(mActivity,permission,requestCode);
        else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    public void acquireWritePermission(OnWritePermissionAcquiredCallback callback){
        mCallback = callback;
        if(checkWriteSDPermission()){
            mCallback.onWritePermissionAcquired();
        }
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    public boolean checkWriteSDPermission(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return true;
        } else {
            if (ContextCompat.checkSelfPermission(mCtx,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //onClick
                    Snackbar.make(mRootView, R.string.SDWritePermission_rationale,
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, view -> requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    RESULT_WRITE_REQUEST)).show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            RESULT_WRITE_REQUEST);
                }//if-else
                return false;
            } else
                return true;
        }
    }


    /**
     * function to call in the fragment onRequestPermissionsResult if the permission is grantend open
     *  the file choorser to select the file
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_WRITE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCallback.onWritePermissionAcquired();
                } else {
                    Snackbar.make(mRootView, R.string.SDWritePermission_notGranted,
                            Snackbar.LENGTH_SHORT).show();

                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult


}

