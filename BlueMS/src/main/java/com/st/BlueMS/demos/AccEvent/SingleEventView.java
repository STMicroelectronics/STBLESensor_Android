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

package com.st.BlueMS.demos.AccEvent;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.AccelerationEvent;
import com.st.BlueSTSDK.gui.util.RepeatAnimator;


public class SingleEventView extends LinearLayout implements EventView {

    public SingleEventView(Context context) {
        super(context);
        init(context);
    }

    public SingleEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SingleEventView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SingleEventView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private @DrawableRes int mCurrentIconId;
    private ImageView mEventIcon;
    private TextView mEventText;
    private RepeatAnimator mShakeImage;

    /**
     * string were write the number of steps
     */
    private String mStepCountTextFormat;


    private void changeIcon(@DrawableRes int icon){
        if(icon==mCurrentIconId)
            return;
        mEventIcon.setImageResource(icon);
        mCurrentIconId=icon;
    }

    private void init(Context context){
        inflate(context, R.layout.view_acc_event_single,this);
        mEventIcon = (ImageView) findViewById(R.id.accEvent_singleEventIcon);
        mEventText = (TextView) findViewById(R.id.accEvent_singleEventLabel);
        AnimatorSet shakeImage = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.shake);

        mShakeImage = new RepeatAnimator(shakeImage, 5);
        mShakeImage.setTarget(mEventIcon);

        mStepCountTextFormat = getResources().getString(R.string.stepCounterStringFormat);
    }

    @Override
    public void enableEvent(FeatureAccelerationEvent.DetectableEvent eventType) {
        changeIcon( EventIconUtil.getDefaultIcon(eventType));
        mEventText.setText(null);
    }

    @Override
    public void displayEvent(@AccelerationEvent int event, int data) {
        @DrawableRes int newIcon = EventIconUtil.getEventIcon(event);
        if(newIcon!=mCurrentIconId) {
            changeIcon(newIcon);
        }else if(!FeatureAccelerationEvent.hasOrientationEvent(event))
            mShakeImage.start();

        if(event==FeatureAccelerationEvent.PEDOMETER && data>=0){
            mEventText.setText(String.format(mStepCountTextFormat,data));
        }
    }


    /**
     * store the current status of the view, if is transparent or not
     * @return object where we store the internal state
     */
    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        return new SingleEventView.SavedState(superState,mCurrentIconId);
    }//onSaveInstanceState

    /**
     * restore the previus state of the view -> if it is transparent or not
     * @param state object where we have stored the data
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if(!(state instanceof SingleEventView.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SingleEventView.SavedState ss = (SingleEventView.SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        changeIcon(ss.getIcon());

    }//onRestoreInstanceState


    /**
     * class used for store the internal state
     * see http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
     *
     */
    static class SavedState extends BaseSavedState {
        private @DrawableRes int mIconId;

        SavedState(Parcelable superState,@DrawableRes int iconId) {
            super(superState);
            mIconId=iconId;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mIconId = in.readInt();
        }

        @DrawableRes int getIcon(){
            return mIconId;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mIconId);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SingleEventView.SavedState> CREATOR =
                new Parcelable.Creator<SingleEventView.SavedState>() {
                    public SingleEventView.SavedState createFromParcel(Parcel in) {
                        return new SingleEventView.SavedState(in);
                    }
                    public SingleEventView.SavedState[] newArray(int size) {
                        return new SingleEventView.SavedState[size];
                    }
                };
    }//SaveState

}
