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
package com.st.STM32WB.fwUpgrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusController;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusView;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;
import com.st.STM32WB.fwUpgrade.searchOtaNode.SearchOtaNodeFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.uploadFwFile.UploadOtaFileFragment;

public class FwUpgradeSTM32WBActivity extends AppCompatActivity implements SearchOtaNodeFragment.OnOtaNodeSearchCallback {

    private static final String NODE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".NODE_PARAM";
    private static final String NODE_ADDRESS_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".NODE_ADDRESS_PARAM";
    private static final String FILE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".FILE_PARAM";
    private static final String ADDRESS_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".ADDRESS_PARAM";
    private static final String FW_TYPE = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".ASK_FW_TYPE";
    private static final String WB_TYPE = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".ASK_WB_TYPE";
    private static final String SEARCH_NODE_TAG = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".SEARCH_NODE_TAG";
    private static final String UPLOAD_NODE_TAG = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".UPLOAD_NODE_TAG";

    public static Intent getStartIntent(@NonNull Context context, @Nullable Node node, @Nullable Uri file,
                                        @Nullable Long address,@Nullable Integer fwType){
        Intent fwUpgradeActivity = new Intent(context, FwUpgradeSTM32WBActivity.class);

        if(node!=null){
            fwUpgradeActivity.putExtra(NODE_PARAM,node.getTag());
        }

        if(file!=null){
            fwUpgradeActivity.putExtra(FILE_PARAM,file);
        }

        if(address!=null) {
            fwUpgradeActivity.putExtra(ADDRESS_PARAM, address);
        }

        if(fwType!=null){
            fwUpgradeActivity.putExtra(FW_TYPE,fwType);
        }

        return fwUpgradeActivity;
    }

    public static Intent getStartIntent(@NonNull Context context, @NonNull String nodeAddress, @Nullable Uri file,
                                        @Nullable Long address,@Nullable Integer fwType, @Nullable Integer mWB_board){
        Intent fwUpgradeActivity = new Intent(context, FwUpgradeSTM32WBActivity.class);

        fwUpgradeActivity.putExtra(NODE_ADDRESS_PARAM,nodeAddress);

        if(file!=null){
            fwUpgradeActivity.putExtra(FILE_PARAM,file);
        }

        if(address!=null) {
            fwUpgradeActivity.putExtra(ADDRESS_PARAM, address);
        }

        if(fwType!=null){
            fwUpgradeActivity.putExtra(FW_TYPE,fwType);
        }

        if(mWB_board!=null) {
            fwUpgradeActivity.putExtra(WB_TYPE,mWB_board);
        }
        return fwUpgradeActivity;
    }

    private Node mNode;
    private ConnectionStatusView mConnectionStatus;

    private static @Nullable String getNodeTag(Intent startIntent,
                                               @Nullable Bundle salvedInstanceState){
        if(startIntent.hasExtra(NODE_PARAM)){
            return startIntent.getStringExtra(NODE_PARAM);
        }else{
            if(salvedInstanceState!=null)
                return salvedInstanceState.getString(NODE_PARAM);
            else
                return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade_stm32_wb);

        mConnectionStatus = findViewById(R.id.otaStm32_connectionStatus);

        Intent startIntent = getIntent();
        String nodeTag = getNodeTag(startIntent,savedInstanceState);
        Node n = nodeTag != null ? Manager.getSharedInstance().getNodeWithTag(nodeTag) : null;
        if(n==null){ //the node is not discovered
            String address = startIntent.getStringExtra(NODE_ADDRESS_PARAM);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.otaSTM32_content,SearchOtaNodeFragment.instantiate(address),SEARCH_NODE_TAG)
                    .commit();
        }else {
            onOtaNodeFound(n);
        }


    }

    private void showUploadFileFragment(@NonNull Node node){
        Intent startIntent = getIntent();
        FragmentManager fm = getSupportFragmentManager();
        //load the upload fragment if need
        if(fm.findFragmentByTag(UPLOAD_NODE_TAG)==null) {
            Uri file = startIntent.getParcelableExtra(FILE_PARAM);
            Long address = startIntent.hasExtra(ADDRESS_PARAM) ?
                    startIntent.getLongExtra(ADDRESS_PARAM, 0) : null;
            Integer fwType = startIntent.hasExtra(FW_TYPE) ?
                    startIntent.getIntExtra(FW_TYPE,FirmwareType.BOARD_FW) : null;
            Integer mWB_board = startIntent.hasExtra(WB_TYPE) ?
                    startIntent.getIntExtra(WB_TYPE,0) : null;
            UploadOtaFileFragment fragment = UploadOtaFileFragment.build(node, file, address,
                    true, fwType,mWB_board,true);

            FragmentTransaction transaction = fm.beginTransaction();
            if (fm.findFragmentByTag(SEARCH_NODE_TAG) != null)
                transaction.replace(R.id.otaSTM32_content, fragment,UPLOAD_NODE_TAG);
            else
                transaction.add(R.id.otaSTM32_content, fragment, UPLOAD_NODE_TAG);
            transaction.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mNode!=null){
            outState.putString(NODE_PARAM,mNode.getTag());
        }
    }

    @Override
    public void onOtaNodeFound(@NonNull Node node) {
        mNode = node;

        runOnUiThread(()->{
            ConnectionStatusController mConnectionStatusController = new ConnectionStatusController(mConnectionStatus, mNode);
            getLifecycle().addObserver(mConnectionStatusController);
        });

        ConnectionOption option = ConnectionOption.builder()
                //the node was probably connected with another name and char set so
                // it is better to reset the cache
                .resetCache(true)
                .setFeatureMap(STM32OTASupport.getOTAFeatures())
                .build();
        NodeConnectionService.connect(this,node,option);
        showUploadFileFragment(node);
    }

    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed() {
        disconnectNode();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button, we go back in the same task
            //for avoid to recreate the DemoActivity
            case android.R.id.home:
                disconnectNode();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected

    private void disconnectNode() {
        if(mNode!=null && mNode.isConnected()){
            NodeConnectionService.disconnect(this,mNode);
        }
    }

}
