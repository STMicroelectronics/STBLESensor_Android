/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueMS.demos.COSensor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureCOSensor;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;


@DemoDescriptionAnnotation(name = "CO Sensor",iconRes = R.drawable.co_sensor_icon,
        requareAll = {FeatureCOSensor.class})
public class COSensorDemoFragment extends BaseDemoFragment implements
        SetSensitivityDialogFragment.SetSensitivityDialogFragmentCallback {

    private static final String SET_SENSITIVITY_DIALOG_TAG=COSensorDemoFragment.class.getCanonicalName() + ".TAG";

    private TextView mCOValueText;
    private float mCurrentSensitivity;

    private FeatureCOSensor.FeatureCOSensorListener mFeatureListener = new FeatureCOSensor.FeatureCOSensorListener(){

        @Override
        public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            float gasConcentration = FeatureCOSensor.getGasPresence(sample);
            updateGui(()-> mCOValueText.setText(getString(R.string.coSensor_numberFormat,gasConcentration)));
        }

        @Override
        public void onSensorSensitivityRead(@NonNull FeatureCOSensor feature, float sensitivity) {
            mCurrentSensitivity = sensitivity;
        }
    };

    private FeatureCOSensor mCOSensor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_co_sensor, container, false);
        mCOValueText = root.findViewById(R.id.coSensor_sensorValue);

        TextView dataUnit = root.findViewById(R.id.coSensor_valueUnit);
        dataUnit.setText(FeatureCOSensor.FEATURE_UNIT);
        return root;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_co_sensor_demo, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.coSensor_menu_setSensitivity) {
            displaySetSensitivityDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mCOSensor = node.getFeature(FeatureCOSensor.class);

        if(mCOSensor!=null){
            mCOSensor.addFeatureListener(mFeatureListener);
            node.enableNotification(mCOSensor);
            mCOSensor.requestSensitivity();
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mCOSensor!=null){
            mCOSensor.removeFeatureListener(mFeatureListener);
            node.disableNotification(mCOSensor);
        }
    }

    private void displaySetSensitivityDialog(){
        DialogFragment dialog = SetSensitivityDialogFragment.newInstance(mCurrentSensitivity);
        dialog.show(getChildFragmentManager(),SET_SENSITIVITY_DIALOG_TAG);
    }

    @Override
    public void onNewSensitivity(float sensitivity) {
        if(mCOSensor!=null){
            mCOSensor.setSensorSensitivity(sensitivity);
        }
    }
}
