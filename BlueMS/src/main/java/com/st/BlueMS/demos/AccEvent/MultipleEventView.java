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
import android.content.Context;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.support.v7.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.gui.util.RepeatAnimator;


/**
 * view used to display multiple event at the same time
 */
public class MultipleEventView extends GridLayout implements EventView {

    public MultipleEventView(Context context) {
        super(context);
        init(context);
    }

    public MultipleEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MultipleEventView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * image view that will show the current chip orientation
     */
    private ImageView mOrientationIcon;

    private RepeatAnimator mTapAnim;
    private RepeatAnimator mFreeFallAnim;
    private RepeatAnimator mWakeUpAnim;
    private RepeatAnimator mTiltAnim;

    /////pedometer data///

    /**
     * format to use for print hte pedometer data
     */
    private String mStepCountTextFormat;
    private RepeatAnimator mPedometerAnim;
    private TextView mPedometerText;
    private int mNSteps;
    /////////////////////

    /**
     * create an shake animation that will animate the specific image
     * @param context context where the animation will live
     * @param target view where apply the animation
     * @return animation
     */
    private static RepeatAnimator createNewEventAnimation(Context context, ImageView target){
        AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.shake);

        RepeatAnimator temp = new RepeatAnimator(anim, 5);
        temp.setTarget(target);
        return temp;
    }

    private void init(Context context){
        inflate(context, R.layout.view_acc_event_multiple,this);

        mStepCountTextFormat = getResources().getString(R.string.stepCounterStringFormat);

        mOrientationIcon = (ImageView) findViewById(R.id.accEvent_multiple_orientationIcon);

        ImageView pedometerIcon = (ImageView) findViewById(R.id.accEvent_multiple_pedometerIcon);
        mPedometerAnim = createNewEventAnimation(context, pedometerIcon);
        mPedometerText = (TextView) findViewById(R.id.accEvent_multiple_pedometerText);

        ImageView tapIcon = (ImageView) findViewById(R.id.accEvent_multiple_tapIcon);
        mTapAnim = createNewEventAnimation(context, tapIcon);

        ImageView freeFallIcon = (ImageView) findViewById(R.id.accEvent_multiple_freeFallIcon);
        mFreeFallAnim = createNewEventAnimation(context, freeFallIcon);

        ImageView wakeUpIcon = (ImageView) findViewById(R.id.accEvent_multiple_wakeUpIcon);
        mWakeUpAnim = createNewEventAnimation(context, wakeUpIcon);

        ImageView tiltIcon = (ImageView) findViewById(R.id.accEvent_multiple_tiltIcon);
        mTiltAnim = createNewEventAnimation(context, tiltIcon);

    }

    /**
     * utility function that check if an event is present in an event set
     * @param eventSet bitmask with multiple events
     * @param event event to detect
     * @return true if the event is present in the eventSet
     */
    private static boolean hasEvent(@FeatureAccelerationEvent.AccelerationEvent int eventSet,
                                    @FeatureAccelerationEvent.AccelerationEvent int event){
        return (eventSet & event) != 0;
    }

    @Override
    public void displayEvent(@FeatureAccelerationEvent.AccelerationEvent int event, int data) {
        if(FeatureAccelerationEvent.hasOrientationEvent(event))
            mOrientationIcon.setImageResource(EventIconUtil.getEventIcon(
                    FeatureAccelerationEvent.extractOrientationEvent(event)));
        if(hasEvent(event,FeatureAccelerationEvent.FREE_FALL)){
            mFreeFallAnim.start();
        }
        if(hasEvent(event,FeatureAccelerationEvent.SINGLE_TAP) ||
                hasEvent(event,FeatureAccelerationEvent.SINGLE_TAP) ) {
            mTapAnim.start();
        }
        if(hasEvent(event,FeatureAccelerationEvent.FREE_FALL)){
            mFreeFallAnim.start();
        }
        if(hasEvent(event,FeatureAccelerationEvent.WAKE_UP)){
            mWakeUpAnim.start();
        }
        if(hasEvent(event,FeatureAccelerationEvent.TILT)){
            mTiltAnim.start();
        }
        if(hasEvent(event,FeatureAccelerationEvent.PEDOMETER)){
            updatePedometer(data);
        }
    }

    private void updatePedometer(int nSteps) {
        if(nSteps==mNSteps)
            return;
        mNSteps=nSteps;
        mPedometerAnim.start();
        updatePedometerString(nSteps);

    }

    private void updatePedometerString(int nSteps) {
        mPedometerText.setText(String.format(mStepCountTextFormat,nSteps));
    }


    ////////////////////////////// STORE VIEW STATE ///////////////////////////////////

    /**
     * store the current status of the view, if is transparent or not
     * @return object where we store the internal state
     */
    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        return new MultipleEventView.SavedState(superState,mNSteps);
    }//onSaveInstanceState

    /**
     * restore the previus state of the view -> if it is transparent or not
     * @param state object where we have stored the data
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if(!(state instanceof MultipleEventView.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        MultipleEventView.SavedState ss = (MultipleEventView.SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        updatePedometerString(ss.getNSteps());

    }//onRestoreInstanceState


    /**
     * class used for store the internal state
     * see http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
     *
     */
    static class SavedState extends BaseSavedState {
        private int mNSteps;

        SavedState(Parcelable superState,int nSteps) {
            super(superState);
            mNSteps =nSteps;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mNSteps = in.readInt();
        }

        int getNSteps(){
            return mNSteps;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mNSteps);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<MultipleEventView.SavedState> CREATOR =
            new Parcelable.Creator<MultipleEventView.SavedState>() {
                public MultipleEventView.SavedState createFromParcel(Parcel in) {
                    return new MultipleEventView.SavedState(in);
                }
                public MultipleEventView.SavedState[] newArray(int size) {
                    return new MultipleEventView.SavedState[size];
                }
            };
    }//SaveState

    ////////////////////////// END STORE VIEW STATE ///////////////////////////////////

    @Override
    public void enableEvent(FeatureAccelerationEvent.DetectableEvent eventType) {

    }

}
