package com.st.blesensor.cloud.GenericMqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckRegularExpression;
import com.st.blesensor.cloud.R;

import java.util.regex.Pattern;

public class GenericMqttFragment extends Fragment {
    /*
     * pattern to match a an broker url
     */
    private static final Pattern MQTT_URL = Pattern.compile("(ssl|tcp)://[^\\s]+");

    private static final String CONF_PREFERENCE = GenericMqttFragment.class.getCanonicalName();
    private static final String BROKER_URL_KEY = CONF_PREFERENCE+".BROKER_URL_KEY";
    private static final String USER_KEY = CONF_PREFERENCE+".USER_KEY";
    private static final String PASSWD_KEY = CONF_PREFERENCE+".PASSWD_KEY";
    private static final String PORT_KEY = CONF_PREFERENCE+".PORT_KEY";
    private static final String CLIENT_ID_KEY = CONF_PREFERENCE+".CLIENT_ID_KEY";;

    private EditText mBrokerUrlText;
    private EditText mUserNameText;
    private EditText mPasswordText;
    private EditText mPortText;
    private EditText mClientIdText;

    private String mBrokerUrlString=null;
    private String mUserNameTString=null;
    private String mPassWdString=null;
    private String mPortString=null;
    private String mClientIdString=null;

    /**
     * if present load the previous connection data from the app preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mBrokerUrlString = pref.getString(BROKER_URL_KEY,"");
        if(mBrokerUrlString!=null) {
            mBrokerUrlText.setText(mBrokerUrlString);
        }

        mUserNameTString = pref.getString(USER_KEY,"");
        if(mUserNameTString!=null) {
            mUserNameText.setText(mUserNameTString);
        }

        mPortString = pref.getString(PORT_KEY,"");
        if(mPortString!=null) {
            mPortText.setText(mPortString);
        }


        mPassWdString = pref.getString(PASSWD_KEY,"");
        if(mPassWdString!=null) {
            mPasswordText.setText(mPassWdString);
        }

        mClientIdString = pref.getString(CLIENT_ID_KEY,"");
        if(mClientIdString!=null) {
            mClientIdText.setText(mClientIdString);
        }
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(BROKER_URL_KEY, mBrokerUrlText.getText().toString())
                .putString(USER_KEY,mUserNameText.getText().toString())
                .putString(PASSWD_KEY,mPasswordText.getText().toString())
                .putString(PORT_KEY,mPortText.getText().toString())
                .putString(CLIENT_ID_KEY,mClientIdText.getText().toString())
                .apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cloud_config_generic_mqtt,container, false);

        TextInputLayout brokerUrlLayout = v.findViewById(R.id.genericMqtt_brokerUrlWrapper);
        mBrokerUrlText = brokerUrlLayout.getEditText();
        mBrokerUrlText.addTextChangedListener(
                new CheckRegularExpression(brokerUrlLayout,R.string.cloudLog_genericMqtt_brokerUrlError,MQTT_URL));

        TextInputLayout userLayout = v.findViewById(R.id.genericMqtt_UserWrapper);
        mUserNameText = userLayout.getEditText();

        TextInputLayout passwordLayout = v.findViewById(R.id.genericMqtt_passwordWrapper);
        mPasswordText = passwordLayout.getEditText();


        TextInputLayout clientIdLayout = v.findViewById(R.id.genericMqtt_clientIdWrapper);
        mClientIdText = clientIdLayout.getEditText();

        TextInputLayout portLayout = v.findViewById(R.id.genericMqtt_portWrapper);
        mPortText = portLayout.getEditText();
        mPortText.addTextChangedListener(new CheckNumberRange(portLayout,R.string.cloudLog_genericMqtt_portError,
                0,1<<16));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFromPreferences(requireContext().getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE));
    }

    @Override
    public void onStop() {
        super.onStop();
        storeToPreference(getActivity().getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public @Nullable String getBrokerUrl() {
        return mBrokerUrlText.getText().toString();
    }

    public @Nullable String getUserName() {
        return mUserNameText.getText().toString();
    }

    public @Nullable String getPort() {
        return mPortText.getText().toString();
    }

    public @Nullable String getClientId() {
        return mClientIdText.getText().toString();
    }

    public @Nullable String getPassWd() {
        return mPasswordText.getText().toString();
    }

    public void setClientId(String clientId) {
        if(mClientIdText==null) {
            mClientIdString = clientId;
        }else if(mClientIdText.getText().toString().isEmpty()){
            mClientIdText.setText(mClientIdString);
        }
    }
}
