package com.st.BlueMS.demos.ActivityRecognition;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureActivity;

/**
 * View to display the output of the GMP Activity recognition algorithm
 */
public class HumanActivityRecognitionGMPView extends ActivityView {

    public HumanActivityRecognitionGMPView(Context context) {
        super(context);
        init(context);
    }

    public HumanActivityRecognitionGMPView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HumanActivityRecognitionGMPView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * image to select for the stationary activity
     */
    private ImageView mStationaryImage;

    /**
     * image to select for the walking activity
     */
    private ImageView mWalkingImage;

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

   @Nullable
    ImageView getSelectedImage(@NonNull FeatureActivity.ActivityType status){
        switch(status){
            case STATIONARY:
                return mStationaryImage;
            case WALKING:
                return mWalkingImage;
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
        inflate(context, R.layout.view_activity_gmp,this);

        //extract all the image and set the alpha
        mStationaryImage = findViewById(R.id.activity_gmp_stationaryImage);
        mWalkingImage =  findViewById(R.id.activity_gmp_walkingImage);
        mJoggingImage =  findViewById(R.id.activity_gmp_joggingImage);
        mBikingImage =  findViewById(R.id.activity_gmp_bikingImage);
        mDrivingImage =  findViewById(R.id.activity_gmp_drivingImage);

        deselectAllImages();
    }

}
