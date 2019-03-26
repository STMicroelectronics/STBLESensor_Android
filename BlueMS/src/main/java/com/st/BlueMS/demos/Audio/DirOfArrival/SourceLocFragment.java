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

package com.st.BlueMS.demos.Audio.DirOfArrival;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureDirectionOfArrival;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
@DemoDescriptionAnnotation(name="Source Localization",iconRes= R.drawable.source_localization_icon,
        requareAll = {FeatureDirectionOfArrival.class})
public class SourceLocFragment extends BaseDemoFragment {

    private ImageView mSLocNeedleImage;
    private TextView mSLocAngleText;
    private String mAngleFormat;
    private ImageView mBoardImage;
    private Switch sensSwitch;

    /////////////////////////////////////////// SOURCE LOC /////////////////////////////////////////

    FeatureDirectionOfArrival mDOAFeature;

    private final Feature.FeatureListener mDOAListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            final short directionOfArrivalAngle = FeatureDirectionOfArrival.getSoundAngle(sample);
            final String angleStr = String.format(mAngleFormat,directionOfArrivalAngle);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mSLocNeedleImage.setRotation(directionOfArrivalAngle);
                    mSLocAngleText.setText(angleStr);
                }
            });
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_source_loc, container, false);

        mSLocNeedleImage = (ImageView) rootView.findViewById(R.id.source_loc_needle);
        sensSwitch = (Switch) rootView.findViewById(R.id.sourceLoc_sensitivity_switch);
        setupSensitivitySwitch(sensSwitch);

        mSLocAngleText = (TextView) rootView.findViewById(R.id.sourceLoc_angle);

        mBoardImage = (ImageView) rootView.findViewById(R.id.sourceLoc_imageBackground);

        mAngleFormat = getResources().getString(R.string.source_loc_angle_format);

        Node n = getNode();
        if(n!=null)
            setBoardImage(n.getType());

        return rootView;
    }

    private void setupSensitivitySwitch(CompoundButton sensitivitySelector) {
        sensitivitySelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mDOAFeature!=null)
                    mDOAFeature.enableLowSensitivity(isChecked);
            }
        });
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        setBoardImage(node.getType());
        mDOAFeature = node.getFeature(FeatureDirectionOfArrival.class);
        if(mDOAFeature!=null){
            mDOAFeature.addFeatureListener(mDOAListener);
            node.enableNotification(mDOAFeature);
            if(sensSwitch.isChecked())
                sensSwitch.toggle();
            else
                mDOAFeature.enableLowSensitivity(false);
        }
    }//enableNeededNotification

    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mDOAFeature!=null){
            mDOAFeature.enableLowSensitivity(false);
            mDOAFeature.removeFeatureListener(mDOAListener);
            node.disableNotification(mDOAFeature);
        }
    }//disableNeedNotification

    private void setBoardImage(Node.Type nodeType) {
        //if already set return
        if(mBoardImage.getDrawable()!=null)
            return;
        switch(nodeType){
            case BLUE_COIN:
                mBoardImage.setImageResource(R.drawable.ic_board_bluecoin_bg);
                break;
            case NUCLEO:
                mBoardImage.setImageResource(R.drawable.ic_board_nucleo_bg);
                //rotate 90 degree right
                mBoardImage.setRotation(90);
                break;
            default:
                mBoardImage.setImageResource(R.drawable.mic_on);
                break;
        }
    }
}