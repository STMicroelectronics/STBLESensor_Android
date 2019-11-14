package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRSelector;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.st.BlueMS.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AsrSelectorDialogFragmentTest {

    public static class FakeActivity extends Activity{

        AsrSelectorDialogFragment dialog;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
            super.onCreate(savedInstanceState, persistentState);
            dialog = new AsrSelectorDialogFragment();
            dialog.show(getFragmentManager(),"dialog");
        }

    }


    @Rule
    public ActivityTestRule<FakeActivity> mActivityTestRule =
            new ActivityTestRule<>(FakeActivity.class,true,false);

    private AsrSelectorDialogFragment mFragment;

    @Before
    public void extractFragment(){
        mFragment = mActivityTestRule.getActivity().dialog;
    }


    @Test
    public void cancellDismissTheDialog(){
        onView(withId(R.id.asrSelector_cancel)).perform(click());
        assertFalse(mFragment.isDetached());
    }
}