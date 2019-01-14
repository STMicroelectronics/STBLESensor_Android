package com.st.BlueMS.demos.Level;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureEulerAngle;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(iconRes = R.drawable.level_demo_icon,name = "Level", requareAll = {FeatureEulerAngle.class})
public class LevelDemoFragment extends DemoFragment {

    private Feature mEulerAngle;

    private TextView mAngleText;
    private ImageView mLevel;

    private Feature.FeatureListener onAngleUpdate = (f, sample) -> {
        float angle = FeatureEulerAngle.getPitch(sample);

        updateGui(()->{
            String angleStr = getResources().getString(R.string.level_angle_format,angle);
            mAngleText.setText(angleStr);
            mLevel.setRotation(-angle);
        });
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level_demo, container, false);

        mAngleText = root.findViewById(R.id.level_angle);
        mLevel = root.findViewById(R.id.level_image);

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mEulerAngle = node.getFeature(FeatureEulerAngle.class);
        if(mEulerAngle != null) {
            mEulerAngle.addFeatureListener(onAngleUpdate);
            mEulerAngle.enableNotification();
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mEulerAngle != null){
            mEulerAngle.removeFeatureListener(onAngleUpdate);
            mEulerAngle.disableNotification();
        }
    }
}
