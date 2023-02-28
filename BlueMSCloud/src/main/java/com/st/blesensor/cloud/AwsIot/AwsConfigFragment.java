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

package com.st.blesensor.cloud.AwsIot;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.st.blesensor.cloud.R;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckRegularExpression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Fragment used to configure the aws iot cloud connection
 * a simple view is not enough because to select the file a onActivityRetrun is needed
 */
public class AwsConfigFragment extends Fragment {

    private static final String CONF_PREFERENCE = AwsConfigFragment.class.getCanonicalName();
    private static final String ENDPOINT_KEY = CONF_PREFERENCE+".ENDPOINT";
    private static final String CLIENT_ID_KEY = CONF_PREFERENCE+".DEVICE_ID";
    private static final String PRIVATE_KEY_URI_KEY = CONF_PREFERENCE+".PRIVATE_KEY_URI";
    private static final String CERTIFICATE_URI_KEY = CONF_PREFERENCE+".CERTIFICATE_URI";
    private static final int SELECT_PRIVATEKEY_FILE = 2;
    private static final int SELECT_CERTIFICATE_FILE = 1;
    private static final Pattern ENPOINT_MATCHER =
            Pattern.compile("([-_\\w]*)\\.iot\\.([-_\\w]*)\\.amazonaws\\.com");

    private EditText mEndpointText;
    private EditText mClientIdText;
    private Button mSelectCertificate;
    private Button mSelectPrivateKey;

    private String mClientId=null;

    //location where the certificate file is
    private Uri mCertificateFile;
    //location where the private key file is
    private Uri mPrivateKeyFile;


    public AwsConfigFragment() {
        // Required empty public constructor
    }


    /**
     * if present load the previous connection data from the app preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mEndpointText.setText(pref.getString(ENDPOINT_KEY,""));
        mClientId = pref.getString(CLIENT_ID_KEY,null);
        if(mClientId!=null)
            mClientIdText.setText(mClientId);

    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(ENDPOINT_KEY, mEndpointText.getText().toString());
        editor.putString(CLIENT_ID_KEY, mClientIdText.getText().toString());
        editor.apply();
    }

    /*create an intent to open a new activity to select a file*/
    private static Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_aws_config, container, false);
        TextInputLayout deviceIdLayout = root.findViewById(R.id.aws_endpointWrapper);
        mEndpointText = deviceIdLayout.getEditText();
        mEndpointText.addTextChangedListener(
                new CheckRegularExpression(deviceIdLayout,
                        R.string.cloudLog_aws_endpoint_invalid_value,ENPOINT_MATCHER));

        TextInputLayout clientIdLayout = root.findViewById(R.id.aws_clientIdWrapper);
        mClientIdText = clientIdLayout.getEditText();
        mClientIdText.addTextChangedListener(
                new CheckNotEmpty(clientIdLayout,R.string.cloudLog_aws_clientId_error));

        mSelectCertificate =root.findViewById(R.id.aws_certificate_button);
        mSelectCertificate.setOnClickListener(view ->
                startActivityForResult(getFileSelectIntent(), SELECT_CERTIFICATE_FILE));

        mSelectPrivateKey = root.findViewById(R.id.aws_privatekey_button);
        mSelectPrivateKey.setOnClickListener(view ->
                startActivityForResult(getFileSelectIntent(), SELECT_PRIVATEKEY_FILE));
        if(savedInstanceState!=null){
            restoreFileState(savedInstanceState);
        }
        if(mCertificateFile!=null){
            setCertificateFileUri(mCertificateFile);
        }
        if(mPrivateKeyFile!=null){
            setPrivateKeyFileUri(mPrivateKeyFile);
        }
        return root;
    }

    private void restoreFileState(@NonNull Bundle savedState){
        Uri privateKeyFile = savedState.getParcelable(PRIVATE_KEY_URI_KEY);
        if(privateKeyFile!=null){
            setPrivateKeyFileUri(privateKeyFile);
        }

        Uri certificateKeyFile = savedState.getParcelable(CERTIFICATE_URI_KEY);
        if(certificateKeyFile!=null){
            setCertificateFileUri(certificateKeyFile);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPrivateKeyFile!=null){
            outState.putParcelable(PRIVATE_KEY_URI_KEY,mPrivateKeyFile);
        }
        if(mCertificateFile!=null){
            outState.putParcelable(CERTIFICATE_URI_KEY,mCertificateFile);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFromPreferences(requireContext().getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE));
    }

    /*get the file name from an uri
    * or null if the uri shame is unknown*/
    private @Nullable String getFileName(Uri uri) {
        Context c = requireContext();
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        }
        if (scheme.equals("content")) {
            Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                return fileName;
           }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode!= Activity.RESULT_OK || data==null)
            return;

        Uri file = data.getData();
        if (file==null)
            return;

        switch (requestCode){
            case SELECT_CERTIFICATE_FILE:
                setCertificateFileUri(file);
                break;
            case SELECT_PRIVATEKEY_FILE:
                setPrivateKeyFileUri(file);
                break;
        }
    }

    private void setCertificateFileUri(Uri fileUri){
        String fileName = getFileName(fileUri);
        mCertificateFile = fileUri;
        if(fileName!=null)
            mSelectCertificate.setText(fileName);
        else
            mSelectCertificate.setText(R.string.cloudLog_aws_select_file);
    }

    private void setPrivateKeyFileUri(Uri fileUri){
        String fileName = getFileName(fileUri);
        mPrivateKeyFile = fileUri;
        if(fileName!=null)
            mSelectPrivateKey.setText(fileName);
        else
            mSelectPrivateKey.setText(R.string.cloudLog_aws_select_file);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mClientId !=null){
            mClientIdText.setText(mClientId);
            mClientId=null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        storeToPreference(getActivity().getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE));
    }


    public @Nullable String getEndpoint(){
        String endpoint = mEndpointText.getText().toString();
        Matcher match = ENPOINT_MATCHER.matcher(endpoint);
        if(match.matches())
            return endpoint;
        else
            return null;
    }

    public @Nullable Uri getPrivateKeyFile() {
        return mPrivateKeyFile;
    }

    public @Nullable Uri getCertificateFile() {
        return mCertificateFile;
    }

    public @Nullable String getClientId(){return mClientIdText.getText().toString();}

    public void setClientId(String clientId){
        if(mClientIdText==null) {
            mClientId = clientId;
        }else if(mClientIdText.getText().toString().isEmpty()){
            mClientIdText.setText(mClientId);
        }
    }
}
