package com.st.blesensor.cloud.IBMWatson;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckRegularExpression;
import com.st.blesensor.cloud.R;
import com.st.blesensor.cloud.util.MqttClientUtil;

import static com.st.blesensor.cloud.IBMWatson.IBMWatsonUtil.VALID_NAME_CHARACTER;

public class IBMWatsonQuickStartConfigFragment  extends Fragment {

    private EditText mDeviceIdText;
    private Node.Type mNodeType;
    private String mDevideID=null;

    public IBMWatsonQuickStartConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.cloud_config_bluemx_quickstart, container, false);

        mDeviceIdText = root.findViewById(R.id.blueMXQuick_deviceId);
        TextInputLayout deviceIdLayout = root.findViewById(R.id.blueMXQuick_deviceIdWrapper);
        mDeviceIdText.addTextChangedListener(
                new CheckNotEmpty(deviceIdLayout,R.string.cloudLog_watson_deviceIdError));
        mDeviceIdText.addTextChangedListener(
                new CheckRegularExpression(deviceIdLayout,R.string.cloudLog_watson_invalidCharacterError,VALID_NAME_CHARACTER));

        mDeviceIdText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing.
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Do nothing.
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mDevideID = mDeviceIdText.getText().toString();
                    }
            });
        return root;
    }

    public Node.Type getNodeType() {
        return mNodeType;
    }

    public @Nullable String getDeviceID() {
        return mDevideID;
    }

    public void setNode(@Nullable Node n) {
        if(n==null){
            mNodeType = Node.Type.GENERIC;
            return;
        }//else
        mDevideID = MqttClientUtil.getDefaultCloudDeviceName(n);
        mDeviceIdText.setText(mDevideID);
        mNodeType=n.getType();
    }
}
