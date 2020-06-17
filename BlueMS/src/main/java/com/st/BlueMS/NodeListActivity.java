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

package com.st.BlueMS;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.st.BlueNRG.fwUpgrade.BlueNRGAdvertiseFilter;
import com.st.BlueNRG.fwUpgrade.feature.BlueNRGOTASupport;
import com.st.BlueSTSDK.Features.standardCharacteristics.StdCharToFeatureMap;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;
import com.st.STM32WB.fwUpgrade.FwUpgradeSTM32WBActivity;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;

import java.util.List;

/**
 * Activity that show the list of device found by the manager
 */
public class NodeListActivity extends com.st.BlueSTSDK.gui.NodeListActivity {

    @Override
    public boolean displayNode(@NonNull Node n) {
        return true;
    }

    @Override
    protected List<AdvertiseFilter> buildAdvertiseFilter() {
        List<AdvertiseFilter> defaultList =  super.buildAdvertiseFilter();
        defaultList.add(new BlueNRGAdvertiseFilter());
        return defaultList;
    }

    @Override
    public void onNodeSelected(@NonNull Node n) {

        ConnectionOption.ConnectionOptionBuilder optionsBuilder = ConnectionOption.builder()
                .resetCache(clearCacheIsSelected())
                .enableAutoConnect(false)
                .setFeatureMap(STM32OTASupport.getOTAFeatures())
                .setFeatureMap(BlueNRGOTASupport.getOTAFeatures())
                .setFeatureMap(new StdCharToFeatureMap());

        if(Peer2PeerDemoConfiguration.isValidDeviceNode(n)){
            optionsBuilder.setFeatureMap(Peer2PeerDemoConfiguration.getCharacteristicMapping());
        }

        ConnectionOption options = optionsBuilder.build();

        //disable the ble server for sensor tile box, to improve stability for the fw upgrade.
        // it is not clear why.. and probably is not the root cause
        if(n.getType() != Node.Type.SENSOR_TILE_BOX) {
            try {
                n.enableNodeServer(BLENodeDefines.FeatureCharacteristics.getDefaultExportedFeature());
            } catch (IllegalStateException e) {
                Toast.makeText(this, R.string.nodeList_serverNotStarted, Toast.LENGTH_SHORT).show();
            }
        }



        if(n.getAdvertiseInfo() instanceof BlueNRGAdvertiseFilter.BlueNRGAdvertiseInfo)
            startActivity(FwUpgradeActivity.getStartIntent(this, n,false,options));
        else if(n.getType()== Node.Type.STEVAL_WESU1)
            startActivity(DemosActivityWesu.getStartIntent(this,n,options));
        else if (STM32OTASupport.isOTANode(n)) {
            startActivity(FwUpgradeSTM32WBActivity.getStartIntent(this, n, null, null, null));
        }else if (n.getType() == Node.Type.SENSOR_TILE_BOX){
            displayPinWarningsAndConnect(n,options);
        }else {
            startActivity(DemosActivity.getStartIntent(this, n, options));
        }

    }


    private void displayPinWarningsAndConnect(Node node, ConnectionOption options){
        if(stBoxPinDialogNeedToBeShown()){
            displayPinWarnings(node,options);
        }else{
            startActivity(DemosActivity.getStartIntent(this, node, options));
        }
    }

    private static final String STBOX_PIN_DIALOG_SHOWN = NodeListActivity.class.getCanonicalName()+".STBOX_PIN_DIALOG_SHOWN";
    private static final String STBOX_PIN_DIALOG_SHOWN_TAG = NodeListActivity.class.getCanonicalName()+".STBOX_PIN_DIALOG_SHOWN_TAG";

    private boolean stBoxPinDialogNeedToBeShown(){
        final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        return !prefs.contains(STBOX_PIN_DIALOG_SHOWN);
    }

    private void displayPinWarnings(Node node, ConnectionOption options) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string.nodeList_stbox_pinTitle,R.string.nodeList_stbox_pinDesc);
        dialog.show(getSupportFragmentManager(),STBOX_PIN_DIALOG_SHOWN_TAG);
        dialog.setOnclickListener((dialogInterface, i) -> {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(STBOX_PIN_DIALOG_SHOWN,true)
                    .apply();
            startActivity(DemosActivity.getStartIntent(dialog.getContext(), node, options));
        });
    }

}//NodeListActivity
