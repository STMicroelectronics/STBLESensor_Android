package com.st.BlueMS.demos.ActivityRecognition;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.st.BlueSTSDK.Features.FeatureActivity.ActivityType;


/**
 * common class between all the ones that display different activity icons
 */
abstract class ActivityView extends ConstraintLayout{

    /**
     * alpha to use for the selected image
     */
    private static final float SELECTED_ALPHA = 1.0f;

    /**
     * alpha to use for the other images
     */
    private static final float DEFAULT_ALPHA = 0.3f;

    /**
     * currently selected image
     */
    private ImageView mSelectedImage=null;

    public ActivityView(Context context) {
        super(context);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActivityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * utility method to deselect all the demo icons
     */
    protected void deselectAllImages(){
        for(ActivityType type : ActivityType.values()){
            ImageView image = getSelectedImage(type);
            if(image!=null)
                image.setAlpha(DEFAULT_ALPHA);
        }
    }

    /**
     * map an activity type to the linked imageview
     * @param status current activity
     * @return image view associated to the status activity or null if is an invalid status
     */
    abstract @Nullable
    ImageView getSelectedImage(@NonNull ActivityType status);


    /***
     * select a specific images
     * @param type activity image to select
     */
    public void setActivity(@Nullable ActivityType type) {
        //restore the alpha for the old activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(DEFAULT_ALPHA);

        //find the new activity
        if(type!=null)
            mSelectedImage = getSelectedImage(type);

        //set the alpha for the new activity
        if(mSelectedImage !=null)
            mSelectedImage.setAlpha(SELECTED_ALPHA);
    }

}
