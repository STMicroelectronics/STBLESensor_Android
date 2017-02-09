/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueMS.demos;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.DemosActivity;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TestCloudLogFragment {
    private static final String NODE_TAG="TAG";

    private static final List<Feature> FEATURE_LIST = Arrays.asList(
            getFakeFeature("feature1"),
            getFakeFeature("feature2")
    );

    private static Feature getFakeFeature(String name){
        Feature f = mock(Feature.class);
        when(f.getName()).thenReturn(name);
        return f;
    }


    private static  Node getFakeNode(){
        Node node = mock(Node.class);

        when(node.getTag()).thenReturn(NODE_TAG);
        when(node.getFeatures()).thenReturn(FEATURE_LIST);
        return node;
    }


    static public class SimpleActivity extends DemosActivity {

        @Override
        @SuppressWarnings("unchecked")
        protected Class<? extends DemoFragment>[] getAllDemos() {
            return new Class[]{CloudLogFragment.class};
        }

        @Override
        protected boolean enableFwUploading() {
            return false;
        }

        @Override
        protected boolean enableLicenseManager() {
            return false;
        }

        @Override
        public Node getNode() {
            return getFakeNode();
        }
    }


    @Rule
    public ActivityTestRule<SimpleActivity> mActivityRule =
            new ActivityTestRule<>(SimpleActivity.class);


    @Test
    public void defaultDeviceIdIsNodeTag(){
        String deviceIdName =
                CloudLogFragment.getDefaultCloudDeviceName(mActivityRule.getActivity().getNode());
        onView(withId(R.id.deviceId)).check(matches(withText(deviceIdName)));
    }

}

