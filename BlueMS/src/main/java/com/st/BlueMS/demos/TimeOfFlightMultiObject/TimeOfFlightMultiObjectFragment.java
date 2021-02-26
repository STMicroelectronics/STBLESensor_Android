package com.st.BlueMS.demos.TimeOfFlightMultiObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureToFMultiObject;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Show the Time-of-Flight Multi Objects demo
 */

@DemoDescriptionAnnotation(name="ToF Objects Detection",
        iconRes=R.drawable.tof_multi_object_demo_icon,
       requareAll = {FeatureToFMultiObject.class})
public class TimeOfFlightMultiObjectFragment extends BaseDemoFragment {

    private static final String CONF_PREFIX_KEY = TimeOfFlightMultiObjectFragment.class.getCanonicalName();
    private static final String ALGORITHM_SELECTION_DIALOG_TAG = CONF_PREFIX_KEY +".ALGORITHM_SELECTED_DIALOG_TAG";

    private FeatureToFMultiObject mTimeOfMultiObjectFeature;
    private Boolean mPresenceDemo = false;

    /**
     * for Switching between presence/object detection
     */
    private Switch mObjSwitch;

    /**
     * Presence Section
     */
    private CardView mPresenceCard;
    private ImageView mPresenceImage;
    private TextView mPresenceText;

    /**
     * Objects detection section
     */
    private CardView mCard_0;
    private ImageView mObjImg_0;
    private TextView mObjText_0;

    /**
     * Object 1
     */
    private TextView mObjText_1;
    private ProgressBar mObjProg_1;
    private CardView mCard_1;

    /**
     * Object 2
     */
    private TextView mObjText_2;
    private ProgressBar mObjProg_2;
    private CardView mCard_2;

    /**
     * Object 3
     */
    private TextView mObjText_3;
    private ProgressBar mObjProg_3;
    private CardView mCard_3;

    /**
     * Object 4
     */
    private TextView mObjText_4;
    private ProgressBar mObjProg_4;
    private CardView mCard_4;

    /**
     * Function for changing the image when we detect one object
     * @param obj_view image type to change
     */
    private void obj_found(ImageView obj_view) {
        obj_view.setImageResource(R.drawable.tof_obj_found);
    }

    /**
     * Function for changing the image when we don't detect one object
     * @param obj_view image type to change
     */
    private void obj_not_found(ImageView obj_view) {
        obj_view.setImageResource(R.drawable.tof_obj_search);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_tof_multi_objects, container, false);

        mObjSwitch = root.findViewById(R.id.Obj_Switch);

        mPresenceCard = root.findViewById(R.id.Obj_presence_Card);
        mPresenceImage = root.findViewById(R.id.Obj_presence_Image);
        mPresenceText = root.findViewById(R.id.Obj_presence_Text);

        mCard_0    = root.findViewById(R.id.Obj_0_Card);
        mObjImg_0 = root.findViewById(R.id.Obj_0_Image);
        mObjText_0 = root.findViewById(R.id.Obj_0_Text);

        mObjText_1 = root.findViewById(R.id.Obj_1_Text);
        mObjProg_1 = root.findViewById(R.id.Obj_1_progressBar);
        mCard_1    = root.findViewById(R.id.Obj_1_Card);

        mObjText_2 = root.findViewById(R.id.Obj_2_Text);
        mObjProg_2 = root.findViewById(R.id.Obj_2_progressBar);
        mCard_2    = root.findViewById(R.id.Obj_2_Card);

        mObjText_3 = root.findViewById(R.id.Obj_3_Text);
        mObjProg_3 = root.findViewById(R.id.Obj_3_progressBar);
        mCard_3    = root.findViewById(R.id.Obj_3_Card);

        mObjText_4 = root.findViewById(R.id.Obj_4_Text);
        mObjProg_4 = root.findViewById(R.id.Obj_4_progressBar);
        mCard_4    = root.findViewById(R.id.Obj_4_Card);

        mObjSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPresenceDemo = true;
                if(mTimeOfMultiObjectFeature !=null) {
                    mTimeOfMultiObjectFeature.enablePresenceRecognition(mTimeOfMultiObjectFeature);
                }
            } else {
                mPresenceDemo = false;
                if(mTimeOfMultiObjectFeature !=null) {
                    mTimeOfMultiObjectFeature.disablePresenceRecognition(mTimeOfMultiObjectFeature);
                }
            }
        });

        if(savedInstanceState!=null) {
            if (savedInstanceState.getBoolean(ALGORITHM_SELECTION_DIALOG_TAG)) {
                mObjSwitch.setChecked(true);
            } else {
                mObjSwitch.setChecked(false);
            }
        }

        return root;
    }

    private Feature.FeatureListener mToFMultiObjListener = (f, sample) -> {

        updateGui(() -> {
            mCard_1.setVisibility(View.INVISIBLE);
            mCard_2.setVisibility(View.INVISIBLE);
            mCard_3.setVisibility(View.INVISIBLE);
            mCard_4.setVisibility(View.INVISIBLE);

            if(mPresenceDemo==false) {
                mCard_0.setVisibility(View.VISIBLE);
                mPresenceCard.setVisibility(View.INVISIBLE);

                int mNumObj0 = FeatureToFMultiObject.getNumObjects(sample);
                final String valueStr0 = FeatureToFMultiObject.getNumObjectsToString(sample);

                if (mNumObj0 != 0) {
                    obj_found(mObjImg_0);
                } else {
                    obj_not_found(mObjImg_0);
                }
                mObjText_0.setText(valueStr0);

                int distance1 = FeatureToFMultiObject.getDistance(sample, 0);
                int distance2 = FeatureToFMultiObject.getDistance(sample, 1);
                int distance3 = FeatureToFMultiObject.getDistance(sample, 2);
                int distance4 = FeatureToFMultiObject.getDistance(sample, 3);

                if (distance1 != 0) {
                    final String valueStr1 = FeatureToFMultiObject.getDistanceToString(sample, 0);
                    mObjText_1.setText(valueStr1);
                    mObjProg_1.setProgress(distance1);
                    mCard_1.setVisibility(View.VISIBLE);
                }
                if (distance2 != 0) {
                    final String valueStr2 = FeatureToFMultiObject.getDistanceToString(sample, 1);
                    mObjText_2.setText(valueStr2);
                    mObjProg_2.setProgress(distance2);
                    mCard_2.setVisibility(View.VISIBLE);
                }
                if (distance3 != 0) {
                    final String valueStr3 = FeatureToFMultiObject.getDistanceToString(sample, 2);
                    mObjText_3.setText(valueStr3);
                    mObjProg_3.setProgress(distance3);
                    mCard_3.setVisibility(View.VISIBLE);

                }
                if (distance4 != 0) {
                    final String valueStr4 = FeatureToFMultiObject.getDistanceToString(sample, 3);
                    mObjText_4.setText(valueStr4);
                    mObjProg_4.setProgress(distance4);
                    mCard_4.setVisibility(View.VISIBLE);
                }
            } else {
                int numPresence = FeatureToFMultiObject.getNumPresence(sample);
                final String valueStr = FeatureToFMultiObject.getNumPresenceToString(sample);
                mPresenceText.setText(valueStr);
                if(numPresence!=0) {
                    mPresenceImage.setImageResource(R.drawable.tof_presence);
                } else {
                    mPresenceImage.setImageResource(R.drawable.tof_not_presence);
                }
                mCard_0.setVisibility(View.INVISIBLE);
                mPresenceCard.setVisibility(View.VISIBLE);
            }
        });
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ALGORITHM_SELECTION_DIALOG_TAG,mObjSwitch.isChecked());
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mTimeOfMultiObjectFeature = node.getFeature(FeatureToFMultiObject.class);
        if(mTimeOfMultiObjectFeature==null)
            return;

        mTimeOfMultiObjectFeature.addFeatureListener(mToFMultiObjListener);
        node.enableNotification(mTimeOfMultiObjectFeature);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mTimeOfMultiObjectFeature==null)
            return;

        mTimeOfMultiObjectFeature.removeFeatureListener(mToFMultiObjListener);
        node.disableNotification(mTimeOfMultiObjectFeature);
    }
}
