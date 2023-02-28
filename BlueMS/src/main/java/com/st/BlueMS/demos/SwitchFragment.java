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

package com.st.BlueMS.demos;

import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Demo used to switch on and off a led in the node
 */
@DemoDescriptionAnnotation(name = "Switch",
        iconRes = R.drawable.switch_demo_icon,
        demoCategory = {"Control"},
        requireAll ={FeatureSwitch.class})
public class SwitchFragment extends BaseDemoFragment {

    private final static @DrawableRes int SWITCH_ON =R.drawable.switch_on;
    private final static @DrawableRes int SWITCH_OFF =R.drawable.switch_off;

    private FeatureSwitch mSwitchFeature;
    private ImageView mSwitchImage;
    private TextView mSwitchText;

    private Feature.FeatureListener mStatusChanged = new Feature.FeatureListener() {
        @Override
        public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            final @DrawableRes int newImage;
            if(FeatureSwitch.getSwitchStatus(sample)==0){
                newImage = SWITCH_OFF;
            }else
                newImage = SWITCH_ON;
            SwitchFragment.this.updateGui(() -> mSwitchImage.setImageResource(newImage));
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_switch, container, false);

        mSwitchImage = root.findViewById(R.id.switch_image);
        mSwitchText = root.findViewById(R.id.switch_title);

        return root;
    }

    private void setSwitchText(Node.Type nodeType){
        if((nodeType == Node.Type.SENSOR_TILE_BOX) || (nodeType == Node.Type.SENSOR_TILE_BOX_PRO)){
            mSwitchText.setText(R.string.switch_eventDescription);
        }else{
            mSwitchText.setText(R.string.switch_onOffDescription);
        }
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        setSwitchText(node.getType());
        mSwitchFeature = node.getFeature(FeatureSwitch.class);
        if (mSwitchFeature != null) {
            mSwitchFeature.addFeatureListener(mStatusChanged);
            //onClick
            mSwitchImage.setOnClickListener(view -> {
                byte currentStatus = FeatureSwitch.getSwitchStatus(mSwitchFeature.getSample());
                if(currentStatus==0)
                    //switch on the first led
                    mSwitchFeature.changeSwitchStatus((byte) 0x01);
                else
                    //switch off all
                    mSwitchFeature.changeSwitchStatus((byte) 0x00);
            });
            node.enableNotification(mSwitchFeature);
        }//if
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mSwitchFeature != null) {
            mSwitchFeature.removeFeatureListener(mStatusChanged);
            mSwitchImage.setOnClickListener(null);
            node.disableNotification(mSwitchFeature);
        }//if
    }
}
