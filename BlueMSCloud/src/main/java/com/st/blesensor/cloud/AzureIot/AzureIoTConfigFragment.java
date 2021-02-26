package com.st.blesensor.cloud.AzureIot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.gui.util.InputChecker.InputChecker;
import com.st.blesensor.cloud.AzureIot.util.ConnectionParameters;
import com.st.blesensor.cloud.R;

public class AzureIoTConfigFragment extends Fragment {
    private static final String CONF_PREFERENCE = AzureIoTConfigFragment.class.getCanonicalName();
    private static final String CONNECTION_STRING_KEY = CONF_PREFERENCE+".CONPOINT";

    private EditText mConnectionStringText;

    public AzureIoTConfigFragment() {
        // Required empty public constructor
    }

    /**
     * if present load the previous connection data from the app preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mConnectionStringText.setText(pref.getString(CONNECTION_STRING_KEY,""));
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(CONNECTION_STRING_KEY, mConnectionStringText.getText().toString());
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.cloud_config_azure, container, false);
        mConnectionStringText = root.findViewById(R.id.azure_connectionString);
        TextInputLayout connectionStringLayout = root.findViewById(R.id.azure_connectionStringWrapper);
        mConnectionStringText.addTextChangedListener(
                new AzureIoTConfigFragment.ConnectionStringChecker(connectionStringLayout,
                        R.string.cloudLog_azure_connectionStringError));

        if(savedInstanceState!=null){
            restoreConnectionStringState(savedInstanceState);
        }

        return root;
    }

    public @Nullable
    String getConnectionString(){
        return mConnectionStringText.getText().toString();
    }

    /**
     * class used to check that if the user input is a valid connection string or not
     */
    private static class ConnectionStringChecker extends InputChecker {

        ConnectionStringChecker(TextInputLayout textInputLayout,
                                @StringRes int errorMessageId) {
            super(textInputLayout, errorMessageId);
        }

        @Override
        protected boolean validate(String input) {
            return ConnectionParameters.hasValidFormat(input);
        }

    }//ConnectionStringChecker

    private void restoreConnectionStringState(@NonNull Bundle savedState){
        String ConnectionString = savedState.getString(CONNECTION_STRING_KEY);
        if(ConnectionString!=null){
            mConnectionStringText.setText(ConnectionString);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CONNECTION_STRING_KEY,mConnectionStringText.getText().toString());
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
}
