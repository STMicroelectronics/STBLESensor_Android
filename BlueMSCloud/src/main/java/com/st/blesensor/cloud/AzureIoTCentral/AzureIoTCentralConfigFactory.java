package com.st.blesensor.cloud.AzureIoTCentral;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.blesensor.cloud.R;

public class AzureIoTCentralConfigFactory implements CloudIotClientConfigurationFactory {
    private static final String CLOUD_NAME= "Azure IoT Central";

    private static final String CONF_PREFERENCE = AzureIoTCentralConfigFactory.class.getCanonicalName();
    private static final String SCOPE_ID_KEY = CONF_PREFERENCE+".SCOPE_ID_KEY";
    private static final String MASTER_KEY = CONF_PREFERENCE+".NOTE_TYPE_KEY";
    private static final String DEVICE_ID_KEY = CONF_PREFERENCE+".DEVICE_ID_KEY";

    private static final Uri CREATE_APP_PAGE = Uri.parse("https://azure.microsoft.com/en-us/services/iot-central/");

    private EditText mScopeIdText;
    private EditText mMasterKeyText;
    private EditText mDeviceIdText;

    /**
     * if present load the organization key and the auth key,device type and device id from the app
     *  preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mScopeIdText.setText(pref.getString(SCOPE_ID_KEY,null));
        mMasterKeyText.setText(pref.getString(MASTER_KEY,null));
        mDeviceIdText.setText(pref.getString(DEVICE_ID_KEY,null));
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(SCOPE_ID_KEY,mScopeIdText.getText().toString())
                .putString(MASTER_KEY,mMasterKeyText.getText().toString())
                .putString(DEVICE_ID_KEY,mDeviceIdText.getText().toString())
                .apply();
    }

    private static SharedPreferences getShearedPreference(Context c){
        return c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE);
    }

    private void openIoTCentralSite(Context c){
            try {
                c.startActivity(new Intent(Intent.ACTION_VIEW, CREATE_APP_PAGE));
            }catch (ActivityNotFoundException e) {
                Log.d("IoTCentral",e.getMessage());
            }
    }

    @Override
    public void attachParameterConfiguration(Context c, ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View v = inflater.inflate(R.layout.cloud_config_azure_iotcentral,root);

        TextInputLayout scopeIdLayout = v.findViewById(R.id.azure_iotCentral_scopeIdWrapper);
        mScopeIdText = scopeIdLayout.getEditText();
        mScopeIdText.addTextChangedListener(
                new CheckNotEmpty(scopeIdLayout,R.string.azure_iotCentral_scopeIdEmptyError));

        TextInputLayout masterKeyTextLayout = v.findViewById(R.id.azure_iotCentral_masterKeyWrapper);
        mMasterKeyText = masterKeyTextLayout.getEditText();
        mMasterKeyText.addTextChangedListener(
                new CheckNotEmpty(masterKeyTextLayout,R.string.azure_iotCentral_masterKeyEmptyError));

        TextInputLayout deviceIdTextLayout = v.findViewById(R.id.azure_iotCentral_deviceIdWrapper);
        mDeviceIdText = deviceIdTextLayout.getEditText();
        mDeviceIdText.addTextChangedListener(
                new CheckNotEmpty(deviceIdTextLayout,R.string.azure_iotCentral_deviceIdEmptyError));

        v.findViewById(R.id.azure_iotCentral_createAppButton).setOnClickListener(
                view -> openIoTCentralSite(c)
        );

        loadFromPreferences(getShearedPreference(c));
    }

    @Override
    public void loadDefaultParameters(@Nullable Node n) {

    }

    @Override
    public String getName() {
        return CLOUD_NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory() throws IllegalArgumentException {
        storeToPreference(getShearedPreference(mDeviceIdText.getContext()));
        return new AzureIotCentralFactory(mScopeIdText.getText().toString(),
                mDeviceIdText.getText().toString(),
                mMasterKeyText.getText().toString());
    }
}
