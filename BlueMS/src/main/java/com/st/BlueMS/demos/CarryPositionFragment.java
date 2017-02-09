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
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureCarryPosition;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Carry Position",requareAll = {FeatureCarryPosition.class},
        iconRes = R.drawable.carry_demo_icon)
public class CarryPositionFragment extends DemoFragment {

    private static final String LAST_VALUE = CarryPositionFragment.class.getCanonicalName() +
            ".PositionValue";

    /**
     * alpha to use for the selected image
     */
    private static final float SELECTED_ALPHA = 1.0f;

    /**
     * alpha to use for the other images
     */
    private static final float DEFAULT_ALPHA = 0.3f;

    /**
     * feature where we will read the position
     */
    private Feature mCarryPositionRecognition;

    /**
     * image to select for the unknown position
     */
    //private ImageView mUnknownImage;

    /**
     * image to select for on desk position
     */
    private ImageView mOnDeskImage;

    /**
     * image to select for the in hand position
     */
    private ImageView mInHandImage;

    /**
     * image to select for the near head position
     */
    private ImageView mNearHeadImage;

    /**
     * image to select for the shirt pocket position
     */
    private ImageView mShirtPocketImage;

    /**
     * image to select for the trousers pocket position
     */
    private ImageView mTrousersPocketImage;

    /**
     * image to select for the trousers pocket position
     */
    private ImageView mArmSwingImage;

    /**
     * currently selected image
     */
    private ImageView mSelectedImage=null;

    /**
     * listener that will change the alpha to the selected image
     */
    private Feature.FeatureListener mActivityListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            final FeatureCarryPosition.Position pos = FeatureCarryPosition.getPosition(sample);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    selectNewPosition(pos);
                }//run
            });

        }//on update
    };

    public CarryPositionFragment() {
        // Required empty public constructor
    }

    private void selectNewPosition(FeatureCarryPosition.Position pos){
        //restore the alpha for the old activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(DEFAULT_ALPHA);

        //find the new activity
        mSelectedImage = getSelectedImage(pos);

        //set the alpha for the new activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(SELECTED_ALPHA);
    }

    /**
     * map an position type to the linked imageview
     * @param position current position
     * @return image view associated to the status activity or null if is an invalid status
     */
    private @Nullable ImageView getSelectedImage(FeatureCarryPosition.Position position){
        switch(position){
            case ON_DESK:
                return mOnDeskImage;
            case IN_HAND:
                return mInHandImage;
            case NEAR_HEAD:
                return mNearHeadImage;
            case SHIRT_POCKET:
                return mShirtPocketImage;
            case TROUSERS_POCKET:
                return mTrousersPocketImage;
            case ARM_SWING:
                return mArmSwingImage;
            case UNKNOWN:
            case ERROR:
                return null;
        }//switch
        return null;
    }//getSelectedImage

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_carry_position, container, false);

        //extract all the image and set the alpha
        //mUnknownImage = (ImageView) root.findViewById(R.id.unknownImage);
        //mUnknownImage.setAlpha(DEFAULT_ALPHA);
        mOnDeskImage = (ImageView) root.findViewById(R.id.onDeskImage);
        mOnDeskImage.setAlpha(DEFAULT_ALPHA);
        mInHandImage = (ImageView) root.findViewById(R.id.inHandImage);
        mInHandImage.setAlpha(DEFAULT_ALPHA);
        mNearHeadImage = (ImageView) root.findViewById(R.id.nearHeadImage);
        mNearHeadImage.setAlpha(DEFAULT_ALPHA);
        mShirtPocketImage = (ImageView) root.findViewById(R.id.shirtPocketImage);
        mShirtPocketImage.setAlpha(DEFAULT_ALPHA);
        mTrousersPocketImage = (ImageView) root.findViewById(R.id.trousersPocketImage);
        mTrousersPocketImage.setAlpha(DEFAULT_ALPHA);
        mArmSwingImage = (ImageView) root.findViewById(R.id.armSwingImage);
        mArmSwingImage.setAlpha(DEFAULT_ALPHA);

        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey(LAST_VALUE)) {
                FeatureCarryPosition.Position lastPos = (FeatureCarryPosition.Position)
                        savedInstanceState.getSerializable(LAST_VALUE);
                selectNewPosition(lastPos);
            }// if contains key
        }// if !=null

        return root;
    }//onCreateView


    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mCarryPositionRecognition = node.getFeature(FeatureCarryPosition.class);
        if (mCarryPositionRecognition != null) {
            mCarryPositionRecognition.addFeatureListener(mActivityListener);
            node.enableNotification(mCarryPositionRecognition);
            showActivityToast(R.string.carryPositionStarted);
            //we have a notification only if the state change -> we force a read for have the
            //initial state
            node.readFeature(mCarryPositionRecognition);
        } else {
            //showActivityToast(R.string.humidityNotFound);
        }//if-else
    }//enableNeededNotification

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mCarryPositionRecognition != null) {
            mCarryPositionRecognition.removeFeatureListener(mActivityListener);
            node.disableNotification(mCarryPositionRecognition);
        }//if
    }//disableNeedNotification

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mCarryPositionRecognition != null) {
            Feature.Sample sample = mCarryPositionRecognition.getSample();
            if(sample!=null) {
                FeatureCarryPosition.Position lastPos =FeatureCarryPosition.getPosition(sample);
                savedInstanceState.putSerializable(LAST_VALUE, lastPos);
            }
        }//if

    }//onSaveInstanceState

}
