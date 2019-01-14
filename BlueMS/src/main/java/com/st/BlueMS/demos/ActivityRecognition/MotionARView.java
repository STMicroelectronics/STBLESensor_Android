package com.st.BlueMS.demos.ActivityRecognition;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureActivity;

/**
 * view to display the output of the MotionAR activity recognition library
 */
public class MotionARView extends ActivityView {

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

    public MotionARView(Context context) {
        super(context);
        init(context);
    }

    public MotionARView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MotionARView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    /**
     * map an activity type to the linked imageview
     * @param status current activity
     * @return image view associated to the status activity or null if is an invalid status
     */
    @Nullable
    ImageView getSelectedImage(@NonNull FeatureActivity.ActivityType status){
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
            default:
                return null;
        }//switch
    }//getSelectedImage


    private void init(Context context){
        inflate(context, R.layout.view_actvity_motion_ar,this);

        //extract all the image and set the alpha
        mStationaryImage = findViewById(R.id.activity_ar_stationaryImage);
        mWalkingImage =  findViewById(R.id.activity_ar_walkingImage);
        mFastWalkingImage = findViewById(R.id.activity_ar_fastWalkImage);
        mJoggingImage =  findViewById(R.id.activity_ar_joggingImage);
        mBikingImage =  findViewById(R.id.activity_ar_bikingImage);
        mDrivingImage =  findViewById(R.id.activity_ar_drivingImage);

        deselectAllImages();
    }

}
