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
package com.st.BlueSTSDK.gui;

import androidx.appcompat.app.AlertDialog;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity that ask to the use witch feature enable and log
 */
public abstract class AskWhatFeatureLogActivity extends LogFeatureActivity {

    //TODO: use save instance for keep it after screen rotation
    //set of feature that this class is logging
    private final Set<String> mFeatureLogSet = new HashSet<>();

    @Override
    public void startLogging(){

        List<String> availableFeatures = getAvailableFeatures();
        final String[] featureToLogList = new String[availableFeatures.size()];
        if (availableFeatures.size() > 0) {
            availableFeatures.toArray(featureToLogList);
            //build and show the MultiChoice dialog
            new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_select_log_features_24dp)
                .setCancelable(false)
                .setMultiChoiceItems(featureToLogList, getEnabledFeature(featureToLogList),
                        (dialogInterface, i, b) -> {
                            if (b)
                                mFeatureLogSet.add(featureToLogList[i]);
                            else
                                mFeatureLogSet.remove(featureToLogList[i]);
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    for (Node n : getNodesToLog())
                        enableLoggingNodeNotification(n);
                    AskWhatFeatureLogActivity.super.startLogging();
                }).create().show();
        }

    }

    private List<String> getAvailableFeatures(){
        List<String> valuesList = new ArrayList<>();
        for(Node n :getNodesToLog())
            for (Feature f : n.getFeatures()){
                if (!valuesList.contains(f.getName()))
                    valuesList.add(f.getName());
            }// for feature
        return  valuesList;
    }//getAvailableFeatures

    private boolean[] getEnabledFeature(String [] featureNames){
        boolean[] valuesChekedList = new boolean[featureNames.length];
        for (int i =0; i<valuesChekedList.length; i++) {
            valuesChekedList[i]= false;
            for (Node n : getNodesToLog())
                for (Feature f : n.getFeatures()) {
                    if (featureNames[i].compareTo(f.getName()) == 0){
                        if(n.isEnableNotification(f)) {
                            valuesChekedList[i] = true;
                            mFeatureLogSet.add(featureNames[i]);
                        }
                    }
                }
        }
        invalidateOptionsMenu();

        return  valuesChekedList;
    }

    private Node.NodeStateListener mStateListener = (node, newState, prevState) -> {
        if (newState == Node.State.Connected) {
            enableLoggingNodeNotification(node);
        }
    };

    private void enableLoggingNodeNotification(Node node){
        for (Feature f : node.getFeatures()) {
            if (mFeatureLogSet.contains(f.getName()))
                node.enableNotification(f);
        }//for
    }

    @Override
    protected void stopLogging(Node n) {
        super.stopLogging(n);
        n.removeNodeStateListener(mStateListener);
        mFeatureLogSet.clear();
    }

    @Override
    protected void startLogging(Node n) {
        super.startLogging(n);
        n.addNodeStateListener(mStateListener);
    }
}
