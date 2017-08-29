package com.st.BlueMS.demos;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.demos.util.CalibrationDialogFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAutoConfigurable;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Compass", requareAll = {FeatureCompass.class},
        iconRes = R.drawable.compass_demo_icon)
public class CompassFragment extends DemoFragment implements CalibrationDialogFragment.CalibrationDialogCallback {

    private static final String CALIBRATION_DIALOG_TAG = CompassFragment.class.getCanonicalName()+"dialog";
    private FeatureCompass mCompassFeature;
    private ImageView mCompassNeedle;
    private TextView mCompassAngle;
    private TextView mCompassDirection;
    private ImageButton mCalibButton;

    private String mAngleFormat;
    private String mOrientationFormat;
    private String[] mOrientation;

    private boolean mShowCalibrateDialog=false;


    private String getOrientationName(float angle){
        int nOrientation = mOrientation.length;

        float section = 360.0f/nOrientation;
        angle = angle - (section/2) + 360.0f;
        int index = (int)(angle/section)+1;

        return mOrientation[index % nOrientation];

    }

    private FeatureAutoConfigurable.FeatureAutoConfigurationListener mCommpassUpdate =
            new FeatureAutoConfigurable.FeatureAutoConfigurationListener() {


        @Override
        public void onAutoConfigurationStarting(FeatureAutoConfigurable f) {
        }

        @Override
        public void onAutoConfigurationStatusChanged(final FeatureAutoConfigurable f, int status) {
            updateGui(new Runnable() {
                @Override
                public void run() {
                    CompassFragment.this.setCalibButtonState(f.isConfigured());
                }
            });
        }

        @Override
        public void onConfigurationFinished(FeatureAutoConfigurable f, int status) {
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            final float angle = FeatureCompass.getCompass(sample);
            final String angleStr = String.format(mAngleFormat,angle);
            final String orientationStr = String.format(mOrientationFormat,getOrientationName(angle));
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mCompassNeedle.setRotation(angle);
                    mCompassAngle.setText(angleStr);
                    mCompassDirection.setText(orientationStr);
                }
            });
        }
    };


    public CompassFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mCompassFeature = node.getFeature(FeatureCompass.class);
        if(mCompassFeature==null)
            return;
        mCompassFeature.addFeatureListener(mCommpassUpdate);
        node.enableNotification(mCompassFeature);
        //not needed since the board send us the status when we connect
        //and this command doesn't work it return an uncalibrate state also if it is calibrated
        //mCompassFeature.requestAutoConfigurationStatus();
        if(!mCompassFeature.isConfigured()){
            mCalibButton.callOnClick();
        }

        setCalibButtonState(mCompassFeature.isConfigured());
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mCompassFeature==null)
            return;

        mCompassFeature.removeFeatureListener(mCommpassUpdate);
        node.disableNotification(mCompassFeature);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_compass_demo, container, false);

        mCompassNeedle = (ImageView) root.findViewById(R.id.compass_needle);
        mCompassAngle = (TextView) root.findViewById(R.id.compass_angle);
        mCompassDirection = (TextView) root.findViewById(R.id.compass_direction);

        mCalibButton = (ImageButton) root.findViewById(R.id.compass_calibButton);
        mCalibButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mShowCalibrateDialog){
                    DialogFragment dialog = new CalibrationDialogFragment();
                    dialog.setTargetFragment(CompassFragment.this,0);
                    dialog.show(getFragmentManager(),CALIBRATION_DIALOG_TAG);
                    mShowCalibrateDialog=true;
                }else
                    onStartCalibrationClicked();
            }
        });

        Resources res = root.getResources();

        mOrientation = res.getStringArray(R.array.compass_orientation);
        mAngleFormat = res.getString(R.string.compass_angle_format);
        mOrientationFormat = res.getString(R.string.compass_orientation_format);

        return root;
    }

    private void setCalibButtonState(boolean newState){
        @DrawableRes int imgId = newState ? R.drawable.calibrated : R.drawable.uncalibrated;
        mCalibButton.setImageResource(imgId);
    }

    @Override
    public void onStartCalibrationClicked() {
        if (mCompassFeature != null) {
            mCompassFeature.startAutoConfiguration();
            setCalibButtonState(mCompassFeature.isConfigured());
        }//if
    }
}
