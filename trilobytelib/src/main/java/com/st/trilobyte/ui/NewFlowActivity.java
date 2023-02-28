package com.st.trilobyte.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.ui.fragment.StFragment;
import com.st.trilobyte.ui.fragment.flow_builder.BuilderFragment;
import com.st.trilobyte.ui.fragment.flow_builder.FlowBuilderFragment;

public class NewFlowActivity extends AppCompatActivity {

    public final static String EXTRA_BOARD_TYPE = "extra-board_type";

    private FragmentManager mFragmentManager;

    private Flow mCurrentFlow;

    private Node.Type mBoard;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crate_flow);

        mFragmentManager = getSupportFragmentManager();

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle.containsKey(ViewFlowDetailActivity.EXTRA_FLOW)){
                mCurrentFlow = (Flow) bundle.getSerializable(ViewFlowDetailActivity.EXTRA_FLOW);
            }

            if(bundle.containsKey(EXTRA_BOARD_TYPE)){
                mBoard = (Node.Type) bundle.getSerializable(ViewFlowDetailActivity.EXTRA_BOARD_TYPE);
            }
        }

        if (mCurrentFlow == null) {
            mCurrentFlow = new Flow();
            mCurrentFlow.setBoard_compatibility(mBoard.name());
        }

        switchFragment(FlowBuilderFragment.getInstance(mBoard));
    }

    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public Flow getCurrentFlow() {
        return mCurrentFlow;
    }

    private void clearFragmentBackStack() {
        while (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
        }
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof BuilderFragment) {
            StFragment stFragment = (StFragment) currentFragment;
            if (stFragment.onBackPressed()) {

                if (mFragmentManager.getBackStackEntryCount() > 1) {
                    mFragmentManager.popBackStack();
                    return;
                }

                finish();
            }

            return;
        }

        super.onBackPressed();
    }
}
