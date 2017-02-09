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

package com.st.BlueMS.demos.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.Suppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;


import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import junit.framework.Assert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/*
@RunWith(AndroidJUnit4.class)
public class FeatureListViewAdapterTest {

    static public class SimpleActivity extends Activity{

        public RecyclerView list;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            list = new RecyclerView(this);
            setContentView(list);
        }


        public void setAdapter(final RecyclerView.Adapter adapter){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    list.setAdapter(adapter);
                }
            });

        }
    }


    static public class DummyFeature extends Feature{

        public DummyFeature() {
            super(null, null, new Field[]{});
        }

        @Override
        protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
            return new ExtractResult(null,0);
        }
    }

    @Rule
    public ActivityTestRule<FeatureListViewAdapterTest.SimpleActivity> mActivityRule =
            new ActivityTestRule<>(FeatureListViewAdapterTest.SimpleActivity.class);


    private static Feature createFakeFeature(String name,boolean enable){

        Feature f = mock(DummyFeature.class);
        when(f.getName()).thenReturn(name);
        when(f.isEnabled()).thenReturn(enable);

        return f;
    }

    @Test
    @Suppress
    public void displayAllEnabledFeatures(){
        RecyclerView list = mActivityRule.getActivity().list;
        List<Feature> featureList = Arrays.asList(
                createFakeFeature("Featrure1",true),
                createFakeFeature("Featrure2",true),
                createFakeFeature("Featrure3",true)
        );
        FeatureListViewAdapter adapter = new FeatureListViewAdapter(featureList,null);
        mActivityRule.getActivity().setAdapter(adapter);

        Assert.assertEquals(featureList.size(),list.getChildCount());

        //onView(isAssignableFrom(RecyclerView.class)).perform(RecyclerViewActions.actionOnItemPosition());

    }


    public void defaultFeatureStateIsDisabled(){
        RecyclerView list = mActivityRule.getActivity().list;
        List<Feature> featureList = Arrays.asList(
                createFakeFeature("Featrure1",true),
                createFakeFeature("Featrure2",true),
                createFakeFeature("Featrure3",true)
        );
        FeatureListViewAdapter adapter = new FeatureListViewAdapter(featureList,null);
        mActivityRule.getActivity().setAdapter(adapter);

        ViewInteraction recyclerView = onView(isAssignableFrom(RecyclerView.class));

        for(int i =0 ; i<featureList.size();i++){
            onView(recyclerView)
        }
    }


}
*/