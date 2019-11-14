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

package com.st.BlueMS.demos.PredictiveMaintenance;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;

import static com.st.BlueSTSDK.Features.predictive.FeaturePredictiveSpeedStatus.*;

class PredictiveStatusView extends CardView {

    static class ViewStatus{

        static class Point{
            final float freq;
            final float value;

            Point(float freq, float value) {
                this.freq = freq;
                this.value = value;
            }

            Point(float value) {
                this(Float.NaN,value);
            }
        }

        final Status xStatus;
        final Status yStatus;
        final Status zStatus;
        final @Nullable Point x;
        final @Nullable Point y;
        final @Nullable Point z;

        ViewStatus(Status xStatus, Status yStatus, Status zStatus,
                   @Nullable Point x, @Nullable Point y, @Nullable Point z) {
            this.xStatus = xStatus;
            this.yStatus = yStatus;
            this.zStatus = zStatus;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        ViewStatus(Status xStatus, Status yStatus, Status zStatus) {
            this(xStatus,yStatus,zStatus,null,null,null);
        }

    }

    private String mValueFormat;

    private TextView mXStatusText;
    private TextView mXFreqText;
    private TextView mXValueText;
    private ImageView mXStatusImage;

    private TextView mYStatusText;
    private TextView mYFreqText;
    private TextView mYValueText;
    private ImageView mYStatusImage;

    private TextView mZStatusText;
    private TextView mZFreqText;
    private TextView mZValueText;
    private ImageView mZStatusImage;


    public PredictiveStatusView(@NonNull Context context) {
        super(context);
        init(context,null,0);
    }

    public PredictiveStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs, 0);
    }

    public PredictiveStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs, defStyleAttr);
    }

    private void setCardTitle(Context context,@Nullable AttributeSet attrs, int defStyleAttr){
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.PredictiveStatusView,defStyleAttr,0);

        String title = a.getString(R.styleable.PredictiveStatusView_title);
        TextView titleView = findViewById(R.id.predictive_title);
        if(title==null){
            titleView.setVisibility(GONE);
        }else{
            titleView.setText(title);
        }

        a.recycle();
    }

    private String loadValueFormat(Context context,@Nullable AttributeSet attrs , int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.PredictiveStatusView,defStyleAttr,0);
        String format = a.getString(R.styleable.PredictiveStatusView_valueFormat);
        a.recycle();

        if(format!=null) {
            return format;
        }
        //else
        return context.getString(R.string.predictive_statusView_defaultValueFormat);
    }

    private void init(Context context,@Nullable AttributeSet attrs, int defStyleAttr){
        inflate(context, R.layout.view_preditive_status,this);

        setCardTitle(context,attrs,defStyleAttr);

        mValueFormat = loadValueFormat(context,attrs,defStyleAttr);

        mXStatusText = findViewById(R.id.predictive_xStatus);
        mXFreqText = findViewById(R.id.predictive_xFreq);
        mXValueText = findViewById(R.id.predictive_xValue);
        mXStatusImage = findViewById(R.id.predictive_xStatusImage);

        mYStatusText = findViewById(R.id.predictive_yStatus);
        mYFreqText = findViewById(R.id.predictive_yFreq);
        mYValueText = findViewById(R.id.predictive_yValue);
        mYStatusImage = findViewById(R.id.predictive_yStatusImage);

        mZStatusText = findViewById(R.id.predictive_zStatus);
        mZFreqText = findViewById(R.id.predictive_zFreq);
        mZValueText = findViewById(R.id.predictive_zValue);
        mZStatusImage = findViewById(R.id.predictive_zStatusImage);


    }



    private String getStatusString(Status s){
        Resources res = getResources();
        switch (s){
            case GOOD:
                return res.getString(R.string.predictive_statusView_good);
            case WARNING:
                return res.getString(R.string.predictive_statusView_warning);
            case BAD:
                return res.getString(R.string.predictive_statusView_bad);
            default:
                return res.getString(R.string.predictive_statusView_unknown);
        }
    }

    private @DrawableRes int getStatusImage(Status s){
        switch (s){
            case GOOD:
                return R.drawable.predictive_status_good;
            case WARNING:
                return R.drawable.predictive_status_warnings;
            case BAD:
                return R.drawable.predictive_status_bad;
            default:
                return R.drawable.predictive_status_unknown;
        }
    }

    private void updateAxisStatus(@StringRes int statusFormat, TextView statusText, ImageView statusImage, Status newStatus){
        CharSequence text = getResources().getString(statusFormat,getStatusString(newStatus));
        statusText.setText(text);
        statusImage.setImageResource(getStatusImage(newStatus));
    }

    private void updateFrequency(TextView freqText, float value){
        if(Float.isNaN(value)){
            freqText.setVisibility(GONE);
        }else{
            freqText.setVisibility(VISIBLE);
            String text = getResources().getString(R.string.predictive_statusView_freqFormat,value);
            freqText.setText(text);
        }
    }

    private void updateValue(TextView freqText, float value){
        if(Float.isNaN(value)){
            freqText.setVisibility(GONE);
        }else{
            freqText.setVisibility(VISIBLE);
            String text = String.format(mValueFormat,value);
            freqText.setText(text);
        }
    }

    void updateStatus(ViewStatus newStatus){
        updateAxisStatus(R.string.predictive_statusView_xStatusFormat,mXStatusText,mXStatusImage,newStatus.xStatus);
        if(newStatus.x!=null) {
            updateFrequency(mXFreqText, newStatus.x.freq);
            updateValue(mXValueText, newStatus.x.value);
        }

        updateAxisStatus(R.string.predictive_statusView_yStatusFormat,mYStatusText,mYStatusImage,newStatus.yStatus);
        if(newStatus.y!=null) {
            updateFrequency(mYFreqText, newStatus.y.freq);
            updateValue(mYValueText, newStatus.y.value);
        }

        updateAxisStatus(R.string.predictive_statusView_zStatusFormat,mZStatusText,mZStatusImage,newStatus.zStatus);
        if(newStatus.z!=null) {
            updateFrequency(mZFreqText, newStatus.z.freq);
            updateValue(mZValueText, newStatus.z.value);
        }
    }

}
