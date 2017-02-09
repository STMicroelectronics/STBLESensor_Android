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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.lang.annotation.Inherited;

/**
 * fragment that show the 6 possible activity and change the alpha of the selected one
 */
@DemoDescriptionAnnotation(name="Activity Recognition", requareAll = {FeatureActivity.class},
    iconRes = R.drawable.activity_demo_icon)
public class ActivityRecognitionFragment extends DemoFragment {

    private static final String LAST_VALUE = ActivityRecognitionFragment.class.getCanonicalName()
            +".ActivityValue";

    /**
     * alpha to use for the selected image
     */
    private static final float SELECTED_ALPHA = 1.0f;

    /**
     * alpha to use for the other images
     */
    private static final float DEFAULT_ALPHA = 0.3f;

    /**
     * feature where we will read the activity type
     */
    private Feature mActivityRecognition;

    /**
     * image to select for the stationary activity
     */
    private ImageView mStationaryImage;

    /**
     * image to select for the walking activity
     */
    private ImageView mWalkingImage;

    /**
     * image to select for the fast walking activity
     */
    private ImageView mFastWalkingImage;

    /**
     * image to select for the jogging activity
     */
    private ImageView mJoggingImage;

    /**
     * image to select for the biking activity
     */
    private ImageView mBikingImage;

    /**
     * image to select for the driving activity
     */
    private ImageView mDrivingImage;

    /**
     * currently selected image
     */
    private ImageView mSelectedImage=null;

    /**
     * map an activity type to the linked imageview
     * @param status current activity
     * @return image view associated to the status activity or null if is an invalid status
     */
    private @Nullable ImageView getSelectedImage(FeatureActivity.ActivityType status){
        switch(status){
            case STATIONARY:
                return mStationaryImage;
            case WALKING:
                return mWalkingImage;
            case FASTWALKING:
                return mFastWalkingImage;
            case JOGGING:
                return mJoggingImage;
            case BIKING:
                return mBikingImage;
            case DRIVING:
                return mDrivingImage;
            case ERROR:
            case NO_ACTIVITY:
                return null;
        }//switch
        return null;
    }//getSelectedImage

    private void selectNewActivity(FeatureActivity.ActivityType activity){
        if(activity == FeatureActivity.ActivityType.NO_ACTIVITY ||
                activity== FeatureActivity.ActivityType.ERROR)
            return;

        //restore the alpha for the old activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(DEFAULT_ALPHA);

        //find the new activity
        mSelectedImage = getSelectedImage(activity);

        //set the alpha for the new activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(SELECTED_ALPHA);
    }

    /**
     * listener that will change the alpha to the selected image
     */
    private Feature.FeatureListener mActivityListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            final FeatureActivity.ActivityType status = FeatureActivity.getActivityStatus(sample);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    selectNewActivity(status);
                }//run
            });

        }//on update
    };

    public ActivityRecognitionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_activity_recognition, container, false);

        //extract all the image and set the alpha
        mStationaryImage = (ImageView) root.findViewById(R.id.stationaryImage);
        mStationaryImage.setAlpha(DEFAULT_ALPHA);
        mWalkingImage = (ImageView) root.findViewById(R.id.walkingImage);
        mWalkingImage.setAlpha(DEFAULT_ALPHA);
        mFastWalkingImage = (ImageView) root.findViewById(R.id.fastWalkImage);
        mFastWalkingImage.setAlpha(DEFAULT_ALPHA);
        mJoggingImage = (ImageView) root.findViewById(R.id.joggingImage);
        mJoggingImage.setAlpha(DEFAULT_ALPHA);
        mBikingImage = (ImageView) root.findViewById(R.id.bikingImage);
        mBikingImage.setAlpha(DEFAULT_ALPHA);
        mDrivingImage = (ImageView) root.findViewById(R.id.drivingImage);
        mDrivingImage.setAlpha(DEFAULT_ALPHA);

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(LAST_VALUE)) {
                FeatureActivity.ActivityType lastActivity = (FeatureActivity.ActivityType)
                        savedInstanceState.getSerializable(LAST_VALUE);
                selectNewActivity(lastActivity);
            }// if contains key
        }// if !=null

        return root;
    }


    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mActivityRecognition = node.getFeature(FeatureActivity.class);
        if (mActivityRecognition != null) {
            mActivityRecognition.addFeatureListener(mActivityListener);
            node.enableNotification(mActivityRecognition);
            //we have a notification only if the state change -> we force a read for have the
            //initial state
            node.readFeature(mActivityRecognition);
            showActivityToast(R.string.activityRecognitionStarted);
        } else {
            //showActivityToast(R.string.humidityNotFound);
        }//if-else
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mActivityRecognition != null) {
            mActivityRecognition.removeFeatureListener(mActivityListener);
            node.disableNotification(mActivityRecognition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mActivityRecognition != null) {
            Feature.Sample sample = mActivityRecognition.getSample();
            if(sample!=null) {
                FeatureActivity.ActivityType lastActivity = FeatureActivity.getActivityStatus(sample);
                savedInstanceState.putSerializable(LAST_VALUE, lastActivity);
            }
        }//if

    }//onSaveInstanceState
}
