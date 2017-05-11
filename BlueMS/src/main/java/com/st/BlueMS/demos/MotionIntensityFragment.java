package com.st.BlueMS.demos;

import android.animation.AnimatorInflater;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Motion Intensity", requareAll = {FeatureMotionIntensity.class},
        iconRes = com.st.BlueMS.R.drawable.activity_demo_icon)
public class MotionIntensityFragment extends DemoFragment {

    private static final String LAST_VALUE = MotionIntensityFragment.class.getCanonicalName()
            +".MotionValue";

    private Feature mMotionIntensityFeature;

    private String mIntensityValueFormat;
    private ImageView mIntensityNeedle;
    private TypedArray mNeedleOffset;
    private TextView mIntensityValue;

    private ValueAnimator mRotationAnim;

    private byte mLastValue =0;
    /**
     * rotate the indicator needle and set the text
     * @param value intensity value to show
     */
    private void setGuiForIntensityValue(byte value){
        if(value== mLastValue)
            return;
        mLastValue =value;
        final float rotationOffset = mNeedleOffset.getFloat(value,0.0f);
        final String valueStr = String.format(mIntensityValueFormat,value);
        /*
        updateGui(()->{
            mIntensityValue.setText(valueStr);

            if(mRotationAnim.isRunning())
                mRotationAnim.pause();

            float currentPosition = mIntensityNeedle.getRotation();
            mRotationAnim.setFloatValues(currentPosition,rotationOffset);
            mRotationAnim.start();

        });
        */
        updateGui(new Runnable() {
            @Override
            public void run() {
                mIntensityValue.setText(valueStr);

                if(mRotationAnim.isRunning())
                    mRotationAnim.pause();

                float currentPosition = mIntensityNeedle.getRotation();
                mRotationAnim.setFloatValues(currentPosition,rotationOffset);
                mRotationAnim.start();
            }
        });
    }


    private Feature.FeatureListener mMotionListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            byte value = FeatureMotionIntensity.getMotionIntensity(sample);
            setGuiForIntensityValue(value);
        }
    };

    public MotionIntensityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_motion_intensity, container, false);

        mIntensityValue = (TextView) root.findViewById(R.id.motionId_intensityValue);
        mIntensityNeedle = (ImageView) root.findViewById(R.id.motionId_needleImage);

        Resources res = root.getResources();
        mNeedleOffset = res.obtainTypedArray(R.array.motionId_angleOffset);
        mIntensityValueFormat = res.getString(R.string.motionId_valueTextFormat);

        mRotationAnim = (ValueAnimator) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.needle_rotation);
        mRotationAnim.setTarget(mIntensityNeedle);


        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(LAST_VALUE)) {
                setGuiForIntensityValue(savedInstanceState.getByte(LAST_VALUE));
            }// if contains key
        }// if !=null

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mMotionIntensityFeature = node.getFeature(FeatureMotionIntensity.class);
        if(mMotionIntensityFeature==null)
            return;

        mMotionIntensityFeature.addFeatureListener(mMotionListener);
        node.enableNotification(mMotionIntensityFeature);

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mMotionIntensityFeature==null)
            return;

        mMotionIntensityFeature.removeFeatureListener(mMotionListener);
        node.disableNotification(mMotionIntensityFeature);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putByte(LAST_VALUE, mLastValue);
    }//onSaveInstanceState

}
