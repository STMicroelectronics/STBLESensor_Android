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


import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.List;

/**
 * demo that show a text view for each feature, when the use click on the name we subscribe to
 * the feature notification and the update data will show in the text view. another click will
 * stop the notification
 */
@DemoDescriptionAnnotation(name="Debug",iconRes=android.R.drawable.ic_menu_search)
public class FeatureDebugFragment extends DemoFragment {

    /**
     * linear layout that will contain all the textView
     */
    private LinearLayout mTextViewList;

    public FeatureDebugFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feature_debug, container, false);
        mTextViewList = (LinearLayout) v.findViewById(R.id.featureListLayout);
        return v;
    }

    /**
     * now the node is created, check what feature are enable by the node and add the text
     * view to the layout
     *
     * @param savedInstanceState previous state (not used)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Node node = getNode();
        float textSize = getResources().getDimension(R.dimen.featureTextSize);
        if (node != null) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            List<Feature> features = node.getFeatures();
            for (Feature f : features) {
                if (f.isEnabled()) {
                    TextView text = new TextView(getActivity());
                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    text.setLayoutParams(layoutParams);
                    f.addFeatureListener(new GenericFragmentUpdate(f, text));
                    mTextViewList.addView(text);
                }//if
            }//for
        }//if
    }//onCreate

    /**
     * in this case it doesn't do anything since the notification are decided runtime by the user
     *
     * @param node node to use for enable the notification
     */
    @Override
    protected void enableNeededNotification(Node node) {
    }

    /**
     * iterate all the feature and disable all the feature that are in notification mode
     *
     * @param node node to use for disable the notification
     */
    @Override
    protected void disableNeedNotification(Node node) {
        List<Feature> features = node.getFeatures();
        for (Feature f : features) {
            if (node.isEnableNotification(f))
                node.disableNotification(f);
        }//for sTestFeature
    }//disableNeedNotification

    /**
     * class used for update the feature display data
     */
    private class GenericFragmentUpdate implements Feature.FeatureListener {

        /**
         * text view that will contain the data/name
         */
        final private TextView mTextView;

        /**
         * add a click listener to text for enable or disable the notification of f when the user
         * click the text
         *
         * @param f    feature associated to this text view
         * @param text text view that will show the name/values
         */
        public GenericFragmentUpdate(final Feature f, TextView text) {
            mTextView = text;

            /**
             * when we click on it we will enable/disable the notification
             */
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Node node = getNode();
                    if(node == null)
                        return;
                    if (node.isEnableNotification(f)) {
                        node.disableNotification(f);
                        mTextView.setText(f.getName());
                    } else
                        node.enableNotification(f);
                }
            });
            mTextView.setClickable(true);
            mTextView.setText(f.getName());
        }//GenericFragmentUpdate

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            final String featureDump = f.toString();
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(featureDump);
                }
            });
        }//onUpdate
    }//GenericFragmentUpdate
}
