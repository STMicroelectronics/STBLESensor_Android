package com.st.trilobyte.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.FlowCategory;
import com.st.trilobyte.services.Session;
import com.st.trilobyte.ui.fragment.CustomFlowsFragment;
import com.st.trilobyte.ui.fragment.ExampleCategoriesFlowsFragment;
import com.st.trilobyte.ui.fragment.ExampleFlowsFragment;
import com.st.trilobyte.ui.fragment.MoreSheetDialogFragment;
import com.st.trilobyte.ui.fragment.NavigationDelegate;
import com.st.trilobyte.ui.fragment.SensorsFragment;
import com.st.trilobyte.ui.fragment.StFragment;

import java.util.List;

public class DashboardActivity extends TrilobyteActivity {

    private final static String TAG = DashboardActivity.class.getSimpleName();

    public final static int FLOW_UPDATED_STATUS_CODE = 200;

    public final static String FINISH_ACTIVITY_NODE_ADDRESS = DashboardActivity.class.getCanonicalName()+"NODE_ADDRESS";

    private final static int WRITE_PERMISSION_REQ_CODE = 11;

    private static final String EXTRA_BOARD_TYPE = DashboardActivity.class.getCanonicalName()+".EXTRA_BOARD_TYPE";

    private BottomNavigationView mNavigationView;

    private FragmentManager mFragmentManager;

    private Node.Type mBoard;

    private final NavigationDelegate flowListener = new NavigationDelegate() {

        @Override
        public void showExpertMode() {
            DashboardActivity.this.showExpertMode();
        }

        @Override
        public void showFlowCategory(FlowCategory category) {
            showExamples(category);
        }

        @Override
        public void uploadFlows(List<Flow> flows) {
            sendToBoard(flows);
        }
    };

    public static Intent getStartDashboardActivityIntent(Context c, @NonNull Node.Type board) {
        Intent i = new Intent(c,DashboardActivity.class);
        i.putExtra(EXTRA_BOARD_TYPE,board);
        return i;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = getIntent();
        mBoard = (Node.Type) startIntent.getSerializableExtra(EXTRA_BOARD_TYPE);

        setContentView(R.layout.activity_dashboard);

        mFragmentManager = getSupportFragmentManager();

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                clearFragmentBackStack();

                int i = menuItem.getItemId();
                if (i == R.id.action_sensor) {
                    showSensorsView();
                } else if (i == R.id.action_flow) {
                    showFlowCategories();
                } else if (i == R.id.action_more) {
                    showMore();
                    return false;
                }

                return true;
            }
        });

        mNavigationView.setSelectedItemId(R.id.action_flow);
    }

    private void showMore() {
        DialogFragment moreDialogFragment = MoreSheetDialogFragment.getInstance(mBoard,null);
        moreDialogFragment.show(mFragmentManager, null);
    }

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void clearFragmentBackStack() {
        while (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
        }
    }

    private void sendToBoard(List<Flow> flows) {
        Session.INSTANCE.setSession(flows, null);
        Intent intent = UploadFlowActivity.Companion.provideIntent(this,mBoard);
        startActivity(intent);
    }

    @Override
    protected void onFinishBroadcastReceived(final Intent intent) {
        detachFinishBroadcastReceiver();
        //for saving the Intent Extra for passing the Node address to the caller
        setResult(FLOW_UPDATED_STATUS_CODE,intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQ_CODE) {
            if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                DialogHelper.showDialog(this, getString(R.string.error_cannot_write_external_memory), null);
                return;
            }

            showExpertMode();
        }
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof StFragment) {
            StFragment stFragment = (StFragment) currentFragment;
            if (stFragment.onBackPressed()) {
                mFragmentManager.popBackStack();
                return;
            }
        }

        super.onBackPressed();
    }

    // listeners

    private void showSensorsView() {

        Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SensorsFragment) {
            return;
        }

        switchFragment(SensorsFragment.getInstance(mBoard), false);
    }

    private void showFlowCategories() {

        Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ExampleCategoriesFlowsFragment) {
            return;
        }

        switchFragment(ExampleCategoriesFlowsFragment.getInstance(flowListener,mBoard), false);
    }

    private void showExamples(FlowCategory category) {
        switchFragment(ExampleFlowsFragment.getInstance(category, flowListener,mBoard), true);
    }

    private void showExpertMode() {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQ_CODE);
                return;
            }
        }

        switchFragment(CustomFlowsFragment.getInstance(flowListener,mBoard), true);
    }
}
