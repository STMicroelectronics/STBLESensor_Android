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
package com.st.BlueMS.demos.Level;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureEulerAngle;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(
        iconRes = R.drawable.level_demo_icon,
        name = "Level",
        requareAll = {FeatureEulerAngle.class})
public class LevelDemoFragment extends DemoFragment {

    private static final float DEEP = 10.0f;

    private Feature mEulerAngle;

    private TextView mOffsetAngleText;
    private TextView mFullAngleText;
    private ImageView mTarget1;
    private ImageView mTarget2;
    private ImageView mLinearTarget;

    private Feature.FeatureListener onAngleUpdate = (f, sample) -> {
        float yaw = FeatureEulerAngle.getYaw(sample);
        float pitch = FeatureEulerAngle.getPitch(sample);
        float roll = FeatureEulerAngle.getRoll(sample);
        float offset = Math.max(Math.abs(pitch),Math.abs(roll));
        if(offset>50.0f){
            if(Math.abs(pitch)>Math.abs(roll))
                showLinearLevel(roll);
            else
                showLinearLevel(pitch);
        }else{
            showPlanarLevel(offset,yaw-90);
        }

        String angleStr = getResources().getString(R.string.level_full_angle_format,yaw,pitch,roll);
        updateGui(() -> mFullAngleText.setText(angleStr));
    };

    private void showLinearLevel(float angle){
        String angleStr = getResources().getString(R.string.level_offset_angle_format,angle);
        updateGui(()->{
            mOffsetAngleText.setText(angleStr);
            mLinearTarget.setRotation(angle);

            mTarget1.setVisibility(View.GONE);
            mTarget2.setVisibility(View.GONE);
            mLinearTarget.setVisibility(View.VISIBLE);
        });
    }

    private void showPlanarLevel(float planarOffset, float orientationOffset){
        float deltaY = (float) (DEEP*planarOffset*Math.sin(Math.toRadians(orientationOffset)));
        float deltaX = (float) (DEEP*planarOffset*Math.cos(Math.toRadians(orientationOffset)));
        String angleStr = getResources().getString(R.string.level_offset_angle_format,planarOffset);
        updateGui(()->{
            mTarget1.setTranslationX(deltaX);
            mTarget1.setTranslationY(deltaY);
            mTarget2.setTranslationX(-deltaX);
            mTarget2.setTranslationY(-deltaY);
            mOffsetAngleText.setText(angleStr);
            mTarget1.setVisibility(View.VISIBLE);
            mTarget2.setVisibility(View.VISIBLE);
            mLinearTarget.setVisibility(View.GONE);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level_demo, container, false);

        mOffsetAngleText = root.findViewById(R.id.level_offsetAngle);
        mFullAngleText = root.findViewById(R.id.level_fullAngle);
        mTarget1 = root.findViewById(R.id.level_offset1);
        mTarget2 = root.findViewById(R.id.level_offset2);
        mLinearTarget = root.findViewById(R.id.level_linearOffset);

        return root;
    }
/*
    private SensorManager sensorManager;
    private Sensor rotation;

    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float yaw = sensorEvent.values[0];
            float pitch = sensorEvent.values[1];
            float roll = sensorEvent.values[2];
            Feature.Sample s = new Feature.Sample(sensorEvent.timestamp,new Number[]{yaw,pitch,roll},new Field[]{});
            onAngleUpdate.onUpdate(mEulerAngle,s);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
*/

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

/*
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(rotation!=null){
            sensorManager.registerListener(sensorListener,rotation,SensorManager.SENSOR_DELAY_UI);
        }
*/
        mEulerAngle = node.getFeature(FeatureEulerAngle.class);
        if(mEulerAngle != null) {
            mEulerAngle.addFeatureListener(onAngleUpdate);
            mEulerAngle.enableNotification();
        }

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
  /*      if(rotation!=null){
            sensorManager.unregisterListener(sensorListener);
        }
*/
        if(mEulerAngle != null){
            mEulerAngle.removeFeatureListener(onAngleUpdate);
            mEulerAngle.disableNotification();
        }
    }
}
