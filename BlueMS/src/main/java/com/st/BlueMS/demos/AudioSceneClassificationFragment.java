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

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAudioSceneClassification;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Demo showing the output of the Audio scene classification NN library.
 */
@DemoDescriptionAnnotation(name="Audio Classification",
        requareAll = {FeatureAudioSceneClassification.class},
        iconRes = R.drawable.ic_bluetooth_audio)
public class AudioSceneClassificationFragment extends DemoFragment {

    /**
     * filter for convert a color image in a gray scale one
     */
    private static ColorMatrixColorFilter sToGrayScale;

    static{
        ColorMatrix temp = new ColorMatrix();
        temp.setSaturation(0.0f);
        sToGrayScale = new ColorMatrixColorFilter(temp);
    }//static initializer


    private ImageView mIndoorImage;

    private ImageView mOutdoorImage;

    private ImageView mInVeicleImage;

    private ImageView mCurrentImage;

    private Animation mScaleDownAnim;

    /**
     * feature where we will read the position
     */
    private Feature mAudioRecognition;

    //on update
    /**
     * listener that will animate selected image
     */
    private Feature.FeatureListener mAudioRecognitionListener = (f, sample) -> {
        final FeatureAudioSceneClassification.Scene scene = FeatureAudioSceneClassification.getScene(sample);
        if(scene == FeatureAudioSceneClassification.Scene.ERROR ||
                scene == FeatureAudioSceneClassification.Scene.UNKNOWN)
            return;
        //run
        updateGui(() -> animateScene(scene));

    };

    public AudioSceneClassificationFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mAudioRecognition = node.getFeature(FeatureAudioSceneClassification.class);
        if (mAudioRecognition != null) {
            mAudioRecognition.addFeatureListener(mAudioRecognitionListener);
            node.enableNotification(mAudioRecognition);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mAudioRecognition != null) {
            mAudioRecognition.removeFeatureListener(mAudioRecognitionListener);
            node.disableNotification(mAudioRecognition);
        }//if
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_audio_scene_recognition, container, false);

        mIndoorImage = root.findViewById(R.id.audioScene_indoor);
        mIndoorImage.setColorFilter(sToGrayScale);
        mOutdoorImage = root.findViewById(R.id.audioScene_outdoor);
        mOutdoorImage.setColorFilter(sToGrayScale);
        mInVeicleImage = root.findViewById(R.id.audioScene_invehicle);
        mInVeicleImage.setColorFilter(sToGrayScale);

        mScaleDownAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_down);

        mCurrentImage=null;

        return root;
    }


    /**
     * return the image link with the scene
     * @param scene scene detected by the node
     * @return image to select
     */
    private @Nullable
    ImageView getSceneImage(FeatureAudioSceneClassification.Scene scene){
        switch(scene) {
            case INDOOR:
                return mIndoorImage;
            case OUTDOOR:
                return mOutdoorImage;
            case IN_VEICLE:
                return mInVeicleImage;
            default:
                return null;
        }//switch
    }//getSceneImage

    /**
     * animate the image link with the gesture
     * @param scene scene to animate
     */
    @UiThread
    private void animateScene(FeatureAudioSceneClassification.Scene scene){

        ImageView gestureImage = getSceneImage(scene);
        if(mCurrentImage!=null){
            mCurrentImage.setColorFilter(sToGrayScale);
        }//if
        mCurrentImage=gestureImage;
        if(mCurrentImage!=null){
            mCurrentImage.setColorFilter(null);
            mCurrentImage.startAnimation(mScaleDownAnim);
        }//if
    } //animateScene

}
