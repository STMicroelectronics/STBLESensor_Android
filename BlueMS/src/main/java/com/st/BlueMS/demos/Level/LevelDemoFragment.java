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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureEulerAngle;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.Arrays;
import java.util.List;

@DemoDescriptionAnnotation(
        iconRes = R.drawable.level_demo_icon,
        name = "Level",
        demoCategory = {"Inertial Sensors"},
        requireAll = {FeatureEulerAngle.class})
public class LevelDemoFragment extends DemoFragment {

    private Feature mEulerAngle;

    private ImageView mTarget1;
    private ImageView mTarget2;
    private View mLineRoll;
    private View mLinePitch;

    private TextView mTextYaw;
    private TextView mTextPitch;
    private TextView mTextRoll;

    private Button mButtonSetZero;
    private Button mButtonResetZero;

    private int widthImage;

    private Spinner mSpinner;

    List<String> spinnerPossibility = Arrays.asList("Pitch/Roll", "Pitch", "Roll");
    int currentSpinnerPosition = 0;

    private float mZeroPitch = 0f;
    private float mZeroRoll = 0f;

    private float mPitch = 0f;
    private float mRoll = 0f;

    private Feature.FeatureListener onAngleUpdate = (f, sample) -> {
        float yaw = FeatureEulerAngle.getYaw(sample);
        mPitch = FeatureEulerAngle.getPitch(sample);
        mRoll = FeatureEulerAngle.getRoll(sample);

        showPlanarLevel(mPitch - mZeroPitch, mRoll - mZeroRoll, yaw);
    };

    private void showPlanarLevel(float pitch, float roll, float yaw) {
        float deltaY;
        float deltaX;

        switch (currentSpinnerPosition) {
            case 0:
                deltaY = (float) (widthImage * Math.sin(Math.toRadians(roll)));
                deltaX = (float) (widthImage * Math.sin(Math.toRadians(pitch)));
                break;
            case 1:
                deltaY = 0f;
                deltaX = (float) (widthImage * Math.sin(Math.toRadians(pitch)));
                break;
            case 2:
            default:
                deltaY = (float) (widthImage * Math.sin(Math.toRadians(roll)));
                deltaX = 0f;
                break;
        }

        updateGui(() -> {
            mTarget1.setTranslationX(deltaX);
            mTarget1.setTranslationY(deltaY);
            mLineRoll.setRotation(roll);
            mLinePitch.setRotation(pitch);

            mTextPitch.setText(String.format("%1$3.2f°", pitch));
            mTextYaw.setText(String.format("%1$3.2f°", yaw));
            mTextRoll.setText(String.format("%1$3.2f°", roll));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level_demo, container, false);

        mTarget1 = root.findViewById(R.id.level_offset1);
        mTarget2 = root.findViewById(R.id.level_offset2);
        mLineRoll = root.findViewById(R.id.line_roll);
        mLinePitch = root.findViewById(R.id.line_pitch);

        mTextYaw = root.findViewById(R.id.val_yaw);
        mTextRoll = root.findViewById(R.id.val_roll);
        mTextPitch = root.findViewById(R.id.val_pitch);

        //Circle radius
        widthImage = mTarget2.getDrawable().getIntrinsicWidth() / 2;

        mButtonResetZero = root.findViewById(R.id.level_button_reset_zero);
        mButtonResetZero.setOnClickListener(v -> {
            mZeroPitch = 0f;
            mZeroRoll = 0f;
        });

        mButtonSetZero = root.findViewById(R.id.level_button_set_zero);
        mButtonSetZero.setOnClickListener(v -> {
            mZeroPitch = mPitch;
            mZeroRoll = mRoll;
        });

        mSpinner = root.findViewById(R.id.level_spinner);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerPossibility);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpinnerPosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mTextYaw.setOnClickListener(view -> {
            if (mTextYaw.getAlpha() == 0f) {
                mTextYaw.setAlpha(1f);
            } else {
                mTextYaw.setAlpha(0f);
            }
        });

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        mEulerAngle = node.getFeature(FeatureEulerAngle.class);
        if (mEulerAngle != null) {
            mEulerAngle.addFeatureListener(onAngleUpdate);
            mEulerAngle.enableNotification();
        }

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mEulerAngle != null) {
            mEulerAngle.removeFeatureListener(onAngleUpdate);
            mEulerAngle.disableNotification();
        }
    }
}
