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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.snackbar.Snackbar;
import com.st.BlueSTSDK.Features.PnPL.PnPLComponent;
import com.st.BlueSTSDK.Features.PnPL.PnPLParser;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NodeScanActivity;
import com.st.utility.databases.associatedBoard.AssociatedBoard;
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase;
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import kotlin.Pair;


/**
 * Activity that will show the list of discovered nodes
 */
public abstract class NodeListActivity extends NodeScanActivity implements NodeRecyclerViewAdapter
        .OnNodeSelectedListener, NodeRecyclerViewAdapter.FilterNode, View.OnClickListener{

    static final String CONF_PREFERENCE = NodeListActivity.class.getCanonicalName();
    static final String DTDL_CUSTOM_ENTRY = CONF_PREFERENCE+".DTDL_CUSTOM_ENTRY";

    private final static String TAG = NodeListActivity.class.getCanonicalName();

    private Manager.ManagerListener mUpdateDiscoverGui = new Manager.ManagerListener() {

        /**
         * call the stopNodeDiscovery for update the gui state
         * @param m manager that start/stop the process
         * @param enabled true if a new discovery start, false otherwise
         */
        @Override
        public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {
            Log.d(TAG, "onDiscoveryChange " + enabled);
            if (!enabled)
                //run
                runOnUiThread(() -> stopNodeDiscovery());
        }//onDiscoveryChange

        /**
         * Hide the SwipeRefreshLayout refresh after that we discover the first node
         * @param m manager that discover the node
         * @param node new node discovered
         */
        @Override
        public void onNodeDiscovered(@NonNull Manager m, Node node) {
            Log.d(TAG, "onNodeDiscovered " + node.getTag());
            runOnUiThread(() -> mSwipeLayout.setRefreshing(false));
        }//onNodeDiscovered
    };

    /**
     * number of millisecond that we spend looking for a new node
     */
    private final static int SCAN_TIME_MS = 10 * 1000; //10sec

    /**
     * adapter used for build the view that will contain the node
     */
    public NodeRecyclerViewAdapter mAdapter;
    /**
     * true if the user request to clear the device handler cache after the connection
     */
    private boolean mClearDeviceCache = true;

    /**
     * true if the user request to Keep the Connection open when the device lose the connection
     */
    private boolean mKeepConnection = false;

    /**
     * SwipeLayout used for refresh the list when the user pull down the fragment
     */
    private SwipeRefreshLayout mSwipeLayout;

    /**
     * Bottom Navigation toolbar
     */
    private BottomAppBar mToolbar;

    /**
     * button used for start/stop the discovery
     */
    private FloatingActionButton mStartStopButton;

    /**
     * Variable for enabling or not the Search of New Board
     */
    private boolean mSearchNotFavoriteBoard;

    /**
     * Animation for start/stop discovery button
     */
    private Animation animRotateButton;


    /**
     * class that manage the node discovery
     */
    private Manager mManager;

    /**
     * clear the adapter and the manager list of nodes
     */
    private void resetNodeList(){
        mManager.resetDiscovery();
        mManager.removeNodes();
        mAdapter.clear();
        //some nodes can survive if they are bounded with the device
        //mAdapter.addAll(mManager.getNodes());
    }

    /**
     * Return the adapter view used for display the node
     * you can overwrite this method for use a custom adapter.
     * @return adapter view for the node.
     */
    protected NodeRecyclerViewAdapter getNodeAdapter(ReadBoardFirmwareDataBase firmwareDB,ReadAssociatedBoardDataBase associatedDB,boolean showNewBoard){
        return new NodeRecyclerViewAdapter(mManager.getNodes(),this,this,firmwareDB,associatedDB,showNewBoard);
    }

    private int requestJson=-1;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    if(uri!=null) {
                        if(requestJson==1) {
                            requestJson=-1;
                            Log.i("Add Fw DB Entry from ", "uri=" + uri);
                            ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(getApplicationContext());
                            Pair<Integer, String> boardsAdded = firmwareDB.readLocalFileDb(getApplicationContext(), uri);
                            if(boardsAdded.component2()==null) {
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Added " + boardsAdded.component1() + " Custom Fw Models", Snackbar.LENGTH_SHORT).show();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(getWindow().getDecorView().getRootView().getContext()).create();
                                alertDialog.setTitle("Error custom Fw Db Entry");
                                alertDialog.setMessage(boardsAdded.component2());
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                                        (dialog, which) -> dialog.dismiss());
                                alertDialog.show();
                            }
                        } else if(requestJson==2) {
                            requestJson=-1;
                            ContentResolver contResolver = NodeListActivity.this.getContentResolver();
                            InputStream inputStream = null;
                            try {
                                inputStream = contResolver.openInputStream(uri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            if (inputStream != null) {
                                String dtmi_model = null;
                                try {
                                    dtmi_model = readFile(inputStream);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                /* Check Validity of PnPL model */
                                if(dtmi_model!=null)
                                {
                                    List<PnPLComponent> components =null;
                                    try {
                                        components = PnPLParser.getPnPLComponentList(dtmi_model);
                                    } catch (Exception e) {
                                        Log.e("PnPL","not correct DTDL");
                                    }
                                    if(components!=null) {
                                            getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE).edit()
                                                    .putString(DTDL_CUSTOM_ENTRY,dtmi_model)
                                                    .apply();
                                            Snackbar.make(getWindow().getDecorView().getRootView(), "Added Custom DTDL Model", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(getWindow().getDecorView().getRootView(), "Not Valid Custom DTDL Model!", Snackbar.LENGTH_SHORT).show();
                                    }
                                }

//                                if(dtmi_model!=null) {
//                                    getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE).edit()
//                                            .putString(DTDL_CUSTOM_ENTRY,dtmi_model)
//                                            .apply();
//                                    Snackbar.make(getWindow().getDecorView().getRootView(), "Added Custom DTDL Model", Snackbar.LENGTH_SHORT).show();
//                                }
                            }
                        }
                    }
                }
            });

    public static String readFile(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }

        return sb.toString();
    }

    /**
     * set the manager and and ask to draw the menu
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mManager = Manager.getSharedInstance();

        ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(getApplicationContext());

        ReadAssociatedBoardDataBase associatedDB = new ReadAssociatedBoardDataBase(getApplicationContext());

        List<AssociatedBoard> listFavoriteBoards = associatedDB.getAssociatedBoards();

        mSearchNotFavoriteBoard = listFavoriteBoards.isEmpty();

        mAdapter = getNodeAdapter(firmwareDB,associatedDB,mSearchNotFavoriteBoard);

        setContentView(R.layout.activity_node_list);

        // Set the adapter
        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(mAdapter);

        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        int nCol =getResources().getInteger(R.integer.nNodeListColum);
        if(nCol!=1){
            recyclerView.setLayoutManager(new GridLayoutManager(this,nCol));
        }

        mSwipeLayout = findViewById(R.id.swiperRefreshDeviceList);
        //find the bottom Toolbar
        mToolbar = findViewById(R.id.bottomAppBar);
        //Inflate the Menu
        mToolbar.inflateMenu(R.menu.activity_node_list);
        //Set the Icon for Favorite/Not Favorite Menu item
        setFavoriteMenuItem(mToolbar.getMenu().findItem(R.id.action_show_favorite));

        //Set the Debug/Release Catalog Fw String
        setCatalogTypeMenuItem(mToolbar.getMenu().findItem(R.id.action_reset_db_entry));

        //Handle the Menu Item Click
        mToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_show_favorite) {
                mSearchNotFavoriteBoard = !mSearchNotFavoriteBoard;
                setFavoriteMenuItem(item);
                //Reset the list of board
                //resetNodeList();
                mAdapter.setBoardToShow(mSearchNotFavoriteBoard);
                //Reset the list of board
                resetNodeList();
                if (mManager.isDiscovering()) {
                    stopNodeDiscovery();
                }
                startNodeDiscovery();
            } else if (itemId == R.id.action_clear_list) {
                resetNodeList();
            } else if (itemId == R.id.menu_clear_device_cache) {
                changeDeviceCacheStatus(item);
            } else if (itemId == R.id.menu_keep_connection_open) {
                changeKeepConnectionStatus(item);
            } else if (itemId== R.id.action_add_db_entry) {
                requestJson=1;
                mGetContent.launch("application/json");
            } else if (itemId== R.id.action_add_dtdl_entry) {
                requestJson=2;
                mGetContent.launch("application/json");
            } else if (itemId== R.id.action_reset_db_entry) {
                if(getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE).getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES,false)) {
                    firmwareDB.readDbBeta();
                } else {
                    firmwareDB.readDb();
                }
                //Reset the list of board
                resetNodeList();
                if (mManager.isDiscovering()) {
                    stopNodeDiscovery();
                }
                startNodeDiscovery();
            }
            return true;
        });

        //onRefresh
        mSwipeLayout.setOnRefreshListener(() -> {
            resetNodeList();
            startNodeDiscovery();
        });

        //set refreshing color
        mSwipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.swipeColor_background));
        mSwipeLayout.setColorSchemeResources(R.color.swipeColor_1, R.color.swipeColor_2,
                R.color.swipeColor_3, R.color.swipeColor_4);

        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mStartStopButton = findViewById(R.id.fab_search_button);
        if(mStartStopButton!=null) {
            mStartStopButton.setOnClickListener(this);
        }

        //Set the Default Value
        mAdapter.setBoardToShow(mSearchNotFavoriteBoard);

        animRotateButton = AnimationUtils.loadAnimation(this,R.anim.fab_rotate);

    }

    private void setFavoriteMenuItem(MenuItem item) {
        if(mSearchNotFavoriteBoard) {
            item.setIcon(R.drawable.ic_not_favorite);
        } else {
            item.setIcon(R.drawable.ic_favorite);
        }
    }

    private void setCatalogTypeMenuItem(MenuItem item) {
        if(getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE).getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES,false)) {
           item.setTitle(R.string.ResetBetaDbEntryMenu);
        } else {
            item.setTitle(R.string.ResetDbEntryMenu);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //disconnect all the already discovered device
        NodeConnectionService.disconnectAllNodes(this);
    }

    /**
     * disconnect all the node and connect our adapter with the node manager for update the list
     * with new discover nodes and start the node discovery
     */
    @Override
    protected void onResume() {
        resetNodeList();
        startNodeDiscovery();
        super.onResume();
    }//onListViewIsDisplayed

    /**
     * stop the discovery and remove all the lister that we attach to the manager
     */
    @Override
    protected void onPause() {
        //remove the listener add by this class
        stopNodeDiscovery();
        super.onPause();
    }


    /** change the menu item name */
    private void changeDeviceCacheStatus(MenuItem item){
        mClearDeviceCache=!mClearDeviceCache;
        if(mClearDeviceCache)
            item.setTitle(R.string.ClearDeviceCacheMenuEnabled);
        else
            item.setTitle(R.string.ClearDeviceCacheMenu);
    }

    private void changeKeepConnectionStatus(MenuItem item){
        mKeepConnection=!mKeepConnection;
        if(mKeepConnection) {
            item.setTitle(R.string.KeepConnectionOpenMenu);
        } else {
            item.setTitle(R.string.KeepConnectionOpenMenuEnabled);
        }
    }

    /**
     * method start a discovery and update the gui for the new state
     */
    private void startNodeDiscovery() {
        setRefreshing(mSwipeLayout, true);
        //add the listener that will hide the progress indicator when the first device is discovered
        mManager.addListener(mUpdateDiscoverGui);
        //disconnect all the already discovered device
        NodeConnectionService.disconnectAllNodes(this);
        //add as listener for the new nodes
        mManager.addListener(mAdapter);
        super.startNodeDiscovery(SCAN_TIME_MS);
        mStartStopButton.setImageResource(R.drawable.ic_close_24dp);
        mStartStopButton.startAnimation(animRotateButton);
        //mManager.addVirtualNode();
    }

    /**
     * method that stop the discovery and update the gui state
     */
    @Override
    public void stopNodeDiscovery() {
        super.stopNodeDiscovery();
        mManager.removeListener(mUpdateDiscoverGui);
        mManager.removeListener(mAdapter);
        mStartStopButton.setImageResource(R.drawable.ic_search_24dp);
        mStartStopButton.startAnimation(animRotateButton);
        setRefreshing(mSwipeLayout, false);
    }

    public static void setRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean isRefreshing) {
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(isRefreshing));
    }

    public void onClick(View view) {
        if(mManager.isDiscovering()){
            stopNodeDiscovery();
        }else{
            startNodeDiscovery();
        }
    }

    protected boolean clearCacheIsSelected(){
        return mClearDeviceCache;
    }

    protected boolean keepConnectionOpenIsSelected(){
        return mKeepConnection;
    }

}
