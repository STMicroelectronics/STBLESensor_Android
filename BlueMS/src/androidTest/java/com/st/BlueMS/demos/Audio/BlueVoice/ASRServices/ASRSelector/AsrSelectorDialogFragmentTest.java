package com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRSelector;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.st.BlueMS.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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