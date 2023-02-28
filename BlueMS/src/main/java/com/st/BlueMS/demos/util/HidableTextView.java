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

package com.st.BlueMS.demos.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/** Text view that change the text color when clicked. the text color change from theme color to
 * transparent and viceversa -> the text become invisible.
 * default colour is transparent*/
public class HidableTextView extends TextView {

    /** true if the it is visible */
    private boolean mIsVisible;
    /** theme text color */
    private ColorStateList mOrigColor;

    /**
     * add the listener that will change the text color and save the current text color that will
     * be restored when the user click on the text
     */
    private void setChangeVisibilityListener(){
        setBackgroundColor(Color.TRANSPARENT);
        mIsVisible=false;
        mOrigColor = getTextColors();
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsVisible=!mIsVisible;
                changeVisibility();
            }//onClick
        });
        changeVisibility();
    }//setChangeVisibilityListener

    /**
     * switch the color from mOrigColor to transparent
     */
    private void changeVisibility() {
        if (mIsVisible)
            setTextColor(mOrigColor);
        else
            setTextColor(Color.TRANSPARENT);
    }

    public HidableTextView(Context context) {
        super(context);
        setChangeVisibilityListener();
    }

    public HidableTextView(Context context, AttributeSet attrs){
        super(context,attrs);
        setChangeVisibilityListener();
    }

    public HidableTextView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        setChangeVisibilityListener();
    }

    @TargetApi(21)
    public HidableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context,attrs,defStyleAttr,defStyleRes);
        setChangeVisibilityListener();
    }

    /**
     * store the current status of the view, if is transparent or not
     * @return object where we store the internal state
     */
    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        return new SavedState(superState,mIsVisible);
    }//onSaveInstanceState

    /**
     * restore the previus state of the view -> if it is transparent or not
     * @param state object where we have stored the data
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        mIsVisible = ss.getIsVisible();
        changeVisibility();
    }//onRestoreInstanceState

    /**
     * class used for store the internal state
     * see http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
     *
     */
    static class SavedState extends BaseSavedState {
        private boolean mIsVisible;

        SavedState(Parcelable superState,boolean isVisible) {
            super(superState);
            mIsVisible=isVisible;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mIsVisible = in.readByte() != 0;
        }

        boolean getIsVisible(){
            return mIsVisible;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mIsVisible ? 1 : 0));
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }//SaveState
}
