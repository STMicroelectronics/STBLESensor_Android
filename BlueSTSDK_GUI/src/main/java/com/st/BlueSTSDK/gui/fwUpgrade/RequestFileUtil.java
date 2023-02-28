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
package com.st.BlueSTSDK.gui.fwUpgrade;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.st.BlueSTSDK.gui.R;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * Helper class to open a file selector to select a file
 */
public class RequestFileUtil {

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;
    private static final int RESULT_READ_ACCESS = 2;


    private final @NonNull Context mCtx;
    private final @Nullable FragmentActivity mActivity;
    private final @Nullable Fragment mFragment;
    private @NonNull View mRootView;

    ActivityResultLauncher<String> requestPermissionLauncher;

    /**
     *
     * @param src fragment that will trigger the open of the file selector
     */
    //public RequestFileUtil(@NonNull Fragment src,@NonNull View rootView) {
        public RequestFileUtil(@NonNull Fragment src) {
        this.mFragment = src;
        this.mCtx = src.requireContext();
        //this.mRootView = rootView;

            requestPermissionLauncher =
                    mFragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            // Permission is granted. Continue the action or workflow in your
                            startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
                        } else {
                            // Explain to the user that the feature is unavailable because the
                            // feature requires a permission that the user has denied.
                            Snackbar.make(mRootView, R.string.FwUpgrade_permissionDenied,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
        this.mActivity=null;
    }

    //public RequestFileUtil(@NonNull FragmentActivity src,@NonNull View rootView) {
            public RequestFileUtil(@NonNull FragmentActivity src) {
        this.mFragment = null;
        mActivity = src;
        //this.mRootView = rootView;
                requestPermissionLauncher =
                        mActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                // Permission is granted. Continue the action or workflow in your
                                startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
                            } else {
                                // Explain to the user that the feature is unavailable because the
                                // feature requires a permission that the user has denied.
                                Snackbar.make(mRootView, R.string.FwUpgrade_permissionDenied,
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
        mCtx = mActivity;
    }



    private void startActivityForResult(Intent intent,int requestCode){
        if(mFragment!=null)
            mFragment.startActivityForResult(intent,requestCode);
        else if (mActivity!=null)
            mActivity.startActivityForResult(intent,requestCode);
        else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    private Activity requireActivity(){
        if(mFragment!=null)
            return mFragment.requireActivity();
        if(mActivity!=null)
            return mActivity;
        throw new IllegalStateException("Fragment or activity must be != null");
    }


    private void requestPermissions(String permission[],int requestCode){
        if(mFragment!=null) {
            requestPermissionLauncher.launch(permission[0]);
        } else if (mActivity!=null) {
            requestPermissionLauncher.launch(permission[0]);
        }  else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    /**
     * check the permission and open the file selector
     */
    public void openFileSelector(){
        if(checkReadSDPermission()) {
            startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
        }
    }

    private Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmware file");
    }


    /**
     * extract the file name
     * @param context context to use to retrive the file info
     * @param uri uri with the file name to query
     * @return name o the file inside the uri
     */
    public static @Nullable String getFileName(@NonNull Context context, @Nullable Uri uri) {
        if(uri == null)
            return null;
        String scheme = uri.getScheme();
        if(scheme == null)
            return null;

        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        }
        if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                return fileName;
            }
        }
        return null;
    }


    /**
     * extract the file size
     * @param context context to use to retrive the file info
     * @param uri uri with the file name to query
     * @return file dimension
     */
    public static long getFileSize(@NonNull Context context, @Nullable Uri uri) {
        if(uri == null)
            return 0;
        String scheme = uri.getScheme();
        if(scheme == null)
            return 0;

        if (scheme.equals("file")) {
            File f = new File(uri.getPath());
            return f.length();
        }

        if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));

                cursor.close();
                return  Long.parseLong(fileSize,10);
            }
        }
        return 0;
    }


    /**
     * to call in the fragment onActivityResult if the request is correct will return the uri, null
     *  otherwise
     * @param requestCode
     * @param resultCode
     * @param data
     * @return selected file or null
     */
    public @Nullable Uri onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                return data.getData();
            }
        }
        return null;
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    public boolean checkReadSDPermission(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return true;
        } else {
            if (ContextCompat.checkSelfPermission(mCtx,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //onClick
                    Snackbar.make(mRootView, R.string.FwUpgrade_readSDRationale,
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, view -> requestPermissions(
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RESULT_READ_ACCESS)).show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RESULT_READ_ACCESS);
                }//if-else

                return false;
            } else {
                return true;
            }
        }
    }

    public void setRootView(View mRootView) {
        this.mRootView = mRootView;
    }
}
