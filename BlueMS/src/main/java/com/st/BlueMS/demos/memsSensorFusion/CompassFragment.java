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

package com.st.BlueMS.demos.memsSensorFusion;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Features.FeatureEulerAngle;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationContract;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationPresenter;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationView;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import com.st.BlueMS.R;

@DemoDescriptionAnnotation(name="Compass", requareOneOf = {FeatureCompass.class,FeatureEulerAngle.class},
        iconRes = R.drawable.compass_demo_icon)
public class CompassFragment extends BaseDemoFragment {

    private FeatureEulerAngle mEulerAngle;
    private FeatureCompass mCompassFeature;
    private ImageView mCompassNeedle;
    private TextView mCompassAngle;
    private TextView mCompassDirection;
    private ImageButton mCalibButton;

    private CalibrationContract.Presenter mCalibPresenter = new CalibrationPresenter();
    private String mAngleFormat;
    private String mOrientationFormat;
    private String[] mOrientation;



    private String getOrientationName(float angle){
        int nOrientation = mOrientation.length;

        float section = 360.0f/nOrientation;
        angle = angle - (section/2) + 360.0f;
        int index = (int)(angle/section)+1;

        return mOrientation[index % nOrientation];

    }

    private void updateCompassAngle(float angle){

        final String angleStr = String.format(mAngleFormat,angle);
        final String orientationStr = String.format(mOrientationFormat,getOrientationName(angle));
        updateGui(() -> {
            mCompassNeedle.setRotation(angle);
            mCompassAngle.setText(angleStr);
            mCompassDirection.setText(orientationStr);
        });
    }

    private Feature.FeatureListener mCompassUpdate = (f, sample) -> {
        final float angle = FeatureCompass.getCompass(sample);
        updateCompassAngle(angle);
    };

    private Feature.FeatureListener mEulerUpdate = (f, sample) -> {
        final float angle = FeatureEulerAngle.getYaw(sample);
        updateCompassAngle(angle);
    };

    public CompassFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mCompassFeature = node.getFeature(FeatureCompass.class);
        if(mCompassFeature!=null) {
            mCompassFeature.addFeatureListener(mCompassUpdate);
            CalibrationContract.View calibView = new CalibrationView(getFragmentManager(), mCalibButton);
            mCalibPresenter.manage(calibView, mCompassFeature);
            mCompassFeature.enableNotification();
        }else {
            mEulerAngle = node.getFeature(FeatureEulerAngle.class);
            if(mEulerAngle != null) {
                CalibrationContract.View calibView = new CalibrationView(getFragmentManager(), mCalibButton);
                mCalibPresenter.manage(calibView, mEulerAngle);
                mEulerAngle.addFeatureListener(mEulerUpdate);
                mEulerAngle.enableNotification();
            }
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mCompassFeature!=null) {
            mCompassFeature.removeFeatureListener(mCompassUpdate);
            mCalibPresenter.unManageFeature();
            node.disableNotification(mCompassFeature);
        }else if(mEulerAngle != null){
            mEulerAngle.disableNotification();
            mEulerAngle.removeFeatureListener(mEulerUpdate);
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_compass_demo, container, false);

        mCompassNeedle = root.findViewById(R.id.compass_needle);
        mCompassAngle = root.findViewById(R.id.compass_angle);
        mCompassDirection = root.findViewById(R.id.compass_direction);

        mCalibButton = root.findViewById(R.id.compass_calibButton);
        mCalibButton.setOnClickListener(view -> mCalibPresenter.startCalibration());

        Resources res = root.getResources();

        mOrientation = res.getStringArray(R.array.compass_orientation);
        mAngleFormat = res.getString(R.string.compass_angle_format);
        mOrientationFormat = res.getString(R.string.compass_orientation_format);

        return root;
    }

}
