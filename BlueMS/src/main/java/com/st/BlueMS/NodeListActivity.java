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


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.st.BlueNRG.fwUpgrade.BlueNRGAdvertiseFilter;
import com.st.BlueNRG.fwUpgrade.feature.BlueNRGOTASupport;
import com.st.BlueSTSDK.Features.standardCharacteristics.StdCharToFeatureMap;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.gui.AboutActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.RetrieveNodeVersion;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;
import com.st.STM32WB.fwUpgrade.FwUpgradeSTM32WBActivity;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;
import com.st.trilobyte.ui.DashboardActivity;
import com.st.utility.databases.associatedBoard.AssociatedBoard;
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.util.Objects;

/**
 * Activity that show the list of device found by the manager
 */
public class NodeListActivity extends com.st.BlueSTSDK.gui.NodeListActivity {

    long myDownloadId = -1;

    Node node = null;
    String incomingNodeAddress = null;

    private class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == myDownloadId) {
                if (node != null) {
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                    boolean completedSuccessful = false;
                    {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(myDownloadId);
                        Cursor c = manager.query(query);

                        if (c != null) {
                            if (c.moveToFirst()) {
                                int statusColumnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                int downloadManagerDownloadStatus = c.getInt(statusColumnIndex);

                                if (DownloadManager.STATUS_SUCCESSFUL == downloadManagerDownloadStatus) {
                                    completedSuccessful = true;
                                } else if (DownloadManager.STATUS_FAILED == downloadManagerDownloadStatus) {
                                    completedSuccessful = false;
                                }
                            }
                            c.close();
                        }
                    }

                    if (completedSuccessful) {
                        Uri uri = manager.getUriForDownloadedFile(myDownloadId);

                        if (uri != null) {
                            ContentResolver contResolver = context.getContentResolver();
                            InputStream inputStream = null;
                            try {
                                inputStream = contResolver.openInputStream(uri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            if (inputStream != null) {
                                String dtmi_model = null;
                                try {
                                    dtmi_model = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                node.setDTDLModel(dtmi_model);
                            }
                        }

                        myDownloadId = -1;
                        ConnectionToNode(node);

                    } else {
                        myDownloadId = -1;
//                        NodeListActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {

                        String CONF_PREFERENCE = com.st.BlueSTSDK.gui.NodeListActivity.class.getCanonicalName();
                        String DTDL_CUSTOM_ENTRY = CONF_PREFERENCE + ".DTDL_CUSTOM_ENTRY";

                        final SharedPreferences prefs = getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE);
                        String dtdl_model = prefs.getString(DTDL_CUSTOM_ENTRY, null);

                        if (dtdl_model == null) {
                            Toast.makeText(NodeListActivity.this, "Not able to download DTDL model",
                                    Toast.LENGTH_SHORT).show();
                            ConnectionToNode(node);
                        } else {
                            new AlertDialog.Builder(NodeListActivity.this)
                                    .setTitle("DTDL non found")
                                    .setMessage("Not able to download the DTDL model\nDo you want to use the custom one?")
                                    .setNegativeButton("No",
                                            (dialog, whichButton) -> {
                                                ConnectionToNode(node);
                                                dialog.dismiss();
                                            }
                                    )
                                    .setNeutralButton("Cancel",
                                            (dialog, whichButton) -> {
                                                dialog.dismiss();
                                            }
                                    )
                                    .setPositiveButton("Ok",
                                            (dialog, whichButton) -> {
                                                node.setDTDLModel(dtdl_model);
                                                ConnectionToNode(node);
                                                dialog.dismiss();
                                            }
                                    )
                                    .create()
                                    .show();
                        }
//                        }});
                    }
                }
            }
        }
    }

    MyReceiver broadCastReceiver = new MyReceiver();

    private Manager.ManagerListener mManagerListener = new Manager.ManagerListener() {


        @Override
        public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {

        }

        @Override
        public void onNodeDiscovered(@NonNull Manager m, @NonNull Node node) {
            if (incomingNodeAddress != null) {
                if (incomingNodeAddress.equals(node.getTag())) {
                    //Node Found! Connect to it
                    //if (Objects.equals(node.getTag(), incomingNode.getTag())) {
                    m.removeListener(this);
                    m.stopDiscovery();
                    ConnectionToNode(node);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startIntent = getIntent();
        String nodeTag = startIntent.getStringExtra(DashboardActivity.FINISH_ACTIVITY_NODE_ADDRESS);
        Node n = nodeTag != null ? Manager.getSharedInstance().getNodeWithTag(nodeTag) : null;
        if (n != null) {
            incomingNodeAddress = n.getTag();
            //Add the listener for searching the node
            Manager manager = Manager.getSharedInstance();
            if (manager != null) {
                manager.addListener(mManagerListener);
            } else {
                onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        this.registerReceiver(broadCastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadCastReceiver);
    }

    @Override
    public boolean displayNode(@NonNull Node n) {
        return true;
    }

    @Override
    protected List<AdvertiseFilter> buildAdvertiseFilter() {
        List<AdvertiseFilter> defaultList = super.buildAdvertiseFilter();
        defaultList.add(new BlueNRGAdvertiseFilter());
        return defaultList;
    }

    private void setUpNodeAnalytics(Node node, RetrieveNodeVersion retrieveNodeVersion) {
        if (Peer2PeerDemoConfiguration.isValidNode(node)) {
            FwVersion version = new FwVersionBoard("STM32Cube_FW_WB", "STM32WBxx",
                    0, 0, 0);
        }

        if (node.getAdvertiseInfo().getProtocolVersion() == 2) {
            //If the board has a BlueST-SDK Version 2 protocol

            byte[] optBytes = NumberConversion.BigEndian.uint32ToBytes(node.getAdvertiseOptionBytes());
            short optBytesUnsigned[] = new short[4];

            ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(getApplicationContext());

            optBytesUnsigned[0] = NumberConversion.byteToUInt8(optBytes, 0);
            optBytesUnsigned[1] = NumberConversion.byteToUInt8(optBytes, 1);
            optBytesUnsigned[2] = NumberConversion.byteToUInt8(optBytes, 2);
            optBytesUnsigned[3] = NumberConversion.byteToUInt8(optBytes, 3);

            BoardFirmware fw_details = firmwareDB.getFwDetailsNode((short) (node.getTypeId() & 0xFF),
                    optBytesUnsigned[0],
                    optBytesUnsigned[1]);
        }

    }

    @Override
    public void onNodeSelected(@NonNull Node n) {

        Toast.makeText(NodeListActivity.this, "Connecting", Toast.LENGTH_SHORT).show();
        node = n;

        BoardFirmware fwBoard = n.getFwDetails();
        if (fwBoard != null) {
            String dtmi = fwBoard.getDtmi();
            if (dtmi != null) {
                /* we need to download the dtmi */
                String uri_dtmi;
                if (dtmi.contains("dtmi:stmicroelectronics")) {
                    uri_dtmi = "https://devicemodels.azure.com/" + dtmi.replace(':', '/').replace(';', '-') + ".expanded.json";
                } else {
                    final SharedPreferences prefs = getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE);
                    boolean betaFunctionalities = prefs.getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES, false);
                    if(betaFunctionalities) {
                        uri_dtmi = BuildConfig.DTDL_DB_BASE_URL_BETA + dtmi.replace(':', '/').replace(';', '-') + ".expanded.json";
                    } else {
                        uri_dtmi = BuildConfig.DTDL_DB_BASE_URL + dtmi.replace(':', '/').replace(';', '-') + ".expanded.json";
                    }
                }
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri_dtmi));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                String nameFile = uri_dtmi.substring(uri_dtmi.lastIndexOf("/") + 1);
                request.setTitle(nameFile);
                request.setAllowedOverMetered(true);
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
                myDownloadId = manager.enqueue(request);
                this.getApplicationContext().registerReceiver(broadCastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                manageDownloadProcess();
            } else {
                ConnectionToNode(n);
            }
        } else {
            ConnectionToNode(n);
        }
    }

    /* Remove the request to download the DTDL after 3 seconds */
    private void manageDownloadProcess() {
        DownloadManager manager = (DownloadManager) this.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED);

        final Cursor cursor = manager.query(query.setFilterById(myDownloadId));
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int downloadManagerDownloadStatus = cursor.getInt(statusColumnIndex);
                        switch (downloadManagerDownloadStatus) {
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_PAUSED: {
                                if (myDownloadId != -1) {
                                    manager.remove(myDownloadId);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }, 3000);//do this after 3 sec
    }

    private void ConnectionToNode(@NonNull Node n) {

        ConnectionOption.ConnectionOptionBuilder optionsBuilder = ConnectionOption.builder()
                .resetCache(clearCacheIsSelected())
                .enableAutoConnect(keepConnectionOpenIsSelected())
                //.enableAutoConnect(false)
                .setFeatureMap(STM32OTASupport.getOTAFeatures())
                .setFeatureMap(BlueNRGOTASupport.getOTAFeatures())
                .setFeatureMap(new StdCharToFeatureMap());

        if (Peer2PeerDemoConfiguration.isValidDeviceNode(n)) {
            optionsBuilder.setFeatureMap(Peer2PeerDemoConfiguration.getCharacteristicMapping());
        }

        ConnectionOption options = optionsBuilder.build();

        //disable the ble server for sensor tile box, to improve stability for the fw upgrade.
        // it is not clear why.. and probably is not the root cause
        if (n.getType() != Node.Type.SENSOR_TILE_BOX) {
            try {
                n.enableNodeServer(BLENodeDefines.FeatureCharacteristics.getDefaultExportedFeature());
            } catch (IllegalStateException e) {
                Toast.makeText(this, R.string.nodeList_serverNotStarted, Toast.LENGTH_SHORT).show();
            }
        }

        if (n.getAdvertiseInfo() instanceof BlueNRGAdvertiseFilter.BlueNRGAdvertiseInfo)
            startActivity(FwUpgradeActivity.getStartIntent(this, n, false, options));
        else if (n.getType() == Node.Type.STEVAL_WESU1)
            startActivity(DemosActivityWesu.getStartIntent(this, n, options));
        else if (STM32OTASupport.isOTANode(n, getApplicationContext())) {
            startActivity(FwUpgradeSTM32WBActivity.getStartIntent(this, n, null, null, null));
        } else if ((n.getType() == Node.Type.SENSOR_TILE_BOX) || (n.getType() == Node.Type.SENSOR_TILE_BOX_PRO)) {
            displayPinWarningsAndConnect(n, options);
        } else {
            startActivity(DemosActivity.getStartIntent(this, n, options));
        }
    }

    @Override
    public void onNodeAdded(Node mItem, ImageView mNodeAddedIcon) {
        ReadAssociatedBoardDataBase associatedDB = new ReadAssociatedBoardDataBase(getApplicationContext());
        AssociatedBoard associatedBoard = associatedDB.getBoardDetailsWithMAC(mItem.getTag());

        if (associatedBoard != null) {
            associatedDB.removeWithMAC(mItem.getTag());
            mNodeAddedIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_not_favorite));
        } else {
            ArrayList<AssociatedBoard> associatedBoardNew = new ArrayList<>();
            associatedBoardNew.add(new AssociatedBoard(mItem.getTag(), mItem.getName(), AssociatedBoard.ConnectivityType.ble, null, null, null, false, null));
            associatedDB.add(associatedBoardNew);
            mNodeAddedIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite));
        }
    }

    private void displayPinWarningsAndConnect(Node node, ConnectionOption options) {
        if (stBoxPinDialogNeedToBeShown()) {
            displayPinWarnings(node, options);
        } else {
            startActivity(DemosActivity.getStartIntent(this, node, options));
        }
    }

    private static final String STBOX_PIN_DIALOG_SHOWN = NodeListActivity.class.getCanonicalName() + ".STBOX_PIN_DIALOG_SHOWN";
    private static final String STBOX_PIN_DIALOG_SHOWN_TAG = NodeListActivity.class.getCanonicalName() + ".STBOX_PIN_DIALOG_SHOWN_TAG";

    private boolean stBoxPinDialogNeedToBeShown() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return !prefs.contains(STBOX_PIN_DIALOG_SHOWN);
    }

    private void displayPinWarnings(Node node, ConnectionOption options) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string.nodeList_stbox_pinTitle, R.string.nodeList_stbox_pinDesc);
        dialog.show(getSupportFragmentManager(), STBOX_PIN_DIALOG_SHOWN_TAG);
        dialog.setOnclickListener((dialogInterface, i) -> {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(STBOX_PIN_DIALOG_SHOWN, true)
                    .apply();
            startActivity(DemosActivity.getStartIntent(dialog.getContext(), node, options));
        });
    }

}//NodeListActivity
