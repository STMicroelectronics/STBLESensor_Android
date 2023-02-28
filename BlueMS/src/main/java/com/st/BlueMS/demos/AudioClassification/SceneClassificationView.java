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
package com.st.BlueMS.demos.AudioClassification;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureAudioClassification.AudioClass;

/**
 * View to display the output of the GMP Activity recognition algorithm
 */
public class SceneClassificationView extends AudioView {

    public SceneClassificationView(Context context) {
        super(context);
        init(context);
    }

    public SceneClassificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SceneClassificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * image to select for the indoor scene
     */
    private ImageView mIndoorImage;

    /**
     * image to select for the outdoor scene
     */
    private ImageView mOutdoorImage;

    /**
     * image to select for the in-vehicle scene
     */
    private ImageView mInVehicleImage;

   @Nullable
    ImageView getSelectedImage(@NonNull AudioClass status){
        switch(status){
            case INDOOR:
                return mIndoorImage;
            case OUTDOOR:
                return mOutdoorImage;
            case IN_VEHICLE:
                return mInVehicleImage;
            default:
                return null;
        }//switch
    }//getSelectedImage

    private void init(Context context){
        inflate(context, R.layout.view_audio_scene_classification,this);

        //extract all the image and set the alpha
        mIndoorImage = findViewById(R.id.audio_scene_classification_indoorImage);
        mOutdoorImage =  findViewById(R.id.audio_scene_classification_outdoorImage);
        mInVehicleImage =  findViewById(R.id.audio_scene_classification_invehicleImage);

        deselectAllImages();
    }
}
