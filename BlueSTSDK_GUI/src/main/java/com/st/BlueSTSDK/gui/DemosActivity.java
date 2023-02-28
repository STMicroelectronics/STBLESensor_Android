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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.Log.FeatureLogLogCat;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.fwDataBase.db.BoardFotaType;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusController;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusView;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that will show the available demos, each demo is a fragment that extend
 * {@link com.st.BlueSTSDK.gui.demos.DemoFragment}
 * <p>
 * The activity will required that the node is already connected or in a connecting state
 * </p>
 */
public abstract class DemosActivity extends LogFeatureActivity implements NodeContainer,
        NavigationView.OnNavigationItemSelectedListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final static String NODE_TAG_ARG = DemosActivity.class.getCanonicalName() + "" +
            ".NODE_TAG";
    private final static String CONNECTION_OPT_ARG = DemosActivity.class.getCanonicalName() + "" +
            ".CONNECTION_OPT_ARG";

    private final static String DEBUG_CONSOLE = DemosActivity.class.getCanonicalName() + "" +
            ".DEBUG_CONSOLE";

    private final static String CURRENT_DEMO = DemosActivity.class.getCanonicalName() + "" +
            ".CURRENT_DEMO";


    public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

    protected abstract Class<? extends DemoFragment>[] getAllDemos();

    /**
     * create an intent for start this activity
     *
     * @param c    context used for create the intent
     * @param node node to use for the demo
     * @return intent for start a demo activity that use the node as data source
     */
    protected static Intent getStartIntent(Context c, @NonNull Node node) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i,node,ConnectionOption.buildDefault());
        return i;
    }//getStartIntent

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param resetCache true if you want to reload the service and characteristics from the device
     * @return intent for start a demo activity that use the node as data source
     */
    @Deprecated
    public static Intent getStartIntent(Context c, @NonNull Node node, boolean resetCache) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i,node,ConnectionOption.builder().resetCache(resetCache).build());
        return i;
    }//getStartIntent

    public static Intent getStartIntent(Context c, @NonNull Node node,ConnectionOption option) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i,node,option);
        return i;
    }//getStartIntent

    @Deprecated
    protected static void setIntentParameters(Intent i, @NonNull Node node, boolean resetCache){
        i.putExtra(NODE_TAG_ARG,node.getTag());
        i.putExtra(CONNECTION_OPT_ARG,ConnectionOption.builder().resetCache(resetCache).build());
    }

    protected static void setIntentParameters(Intent i, @NonNull Node node, ConnectionOption option){
        i.putExtra(NODE_TAG_ARG,node.getTag());
        i.putExtra(CONNECTION_OPT_ARG,option);
    }

    /*
     * widget that will contain all the demo fragment
     */
    private ViewPager2 mPager;
    private int mPrevSelectedPage = 0;
    //private int mMaxPageSelectable= 0;
//    private ViewPager2.OnPageChangeCallback mUpdateActivityTitle = new ViewPager2.OnPageChangeCallback() {
//
//        @Override
//        public void onPageSelected(int position) {
//            if(mMaxPageSelectable!=0) {
//                //!=0 because at the beginning mMaxPageSelectable==0
//                if (position >= (mMaxPageSelectable - 1)) {
//                    // Limit the maximum selectable page
//                    position = mMaxPageSelectable - 1;
//                    mPager.setCurrentItem(position);
//                }
//            }
//            mPrevSelectedPage=position;
//            DemosTabAdapter adapter = (DemosTabAdapter) mPager.getAdapter();
//            setTitle(adapter.getPageTitle(position));
//        }
//
//    };

    private ViewPager2.OnPageChangeCallback mUpdateActivityTitle = new ViewPager2.OnPageChangeCallback() {

        @Override
        public void onPageSelected(int position) {
            mPager.setCurrentItem(position,false);
            mPrevSelectedPage=position;
            DemosTabAdapter adapter = (DemosTabAdapter) mPager.getAdapter();

            CharSequence title = adapter.getPageTitle(position);
            if(adapter.requireChangeName(position)) {
                Node node = getNode();
                if(node!=null) {
                    BoardFirmware boardFw = node.getFwDetails();
                    if (boardFw != null) {
                        //title = boardFw.getFw_name() + " " + adapter.getPageTitle(position);
                        title = boardFw.getFw_name();
                    }
                }
            }

            setTitle(title);
        }

    };

    //layout with the demo and demo menu
    private DrawerLayout mDrawerLayout;

    //button for show the demo menu
    private ActionBarDrawerToggle mDrawerToggle;

    //demo menu
    private NavigationView mNavigationTab;

    /**
     * text view that will show the debug message
     */
    private TextView mConsoleText;
    /**
     * scrollview attached to the console text
     */
    private ScrollView mConsoleView;

    private View mDemosListView;
    private RecyclerView recyclerViewDemos;
    private ListOfDemoAdapter adapterListOfDemo;

    private ConnectionStatusView mConnectionView;
    private boolean mKeepConnectionOpen;
    private Node mNode;
    private ConnectionOption mConnectionOption;

    /**
     * true if we are showing the debug console
     */
    private boolean mShowDebugConsole = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set default settings for the logging
        PreferenceManager.setDefaultValues(this, R.xml.pref_logging, false);

        setContentView(R.layout.activity_demos);

        mConnectionView = findViewById(R.id.demoConnectionStatus);

        mDrawerLayout = findViewById(R.id.demoDrawerLayout);
        mNavigationTab = findViewById(R.id.demoNavigationView);

        mConsoleText = findViewById(R.id.consoleText);
        mConsoleView = findViewById(R.id.consoleView);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            mNode = Manager.getSharedInstance().getNodeWithTag(i.getStringExtra(NODE_TAG_ARG));
            mConnectionOption = i.getParcelableExtra(CONNECTION_OPT_ARG);
            if(mConnectionOption==null)
                mConnectionOption=ConnectionOption.buildDefault();
            mShowDebugConsole = i.getBooleanExtra(DEBUG_CONSOLE, false);
        } else {
            mNode = Manager.getSharedInstance().getNodeWithTag(savedInstanceState.getString(NODE_TAG_ARG));
            mShowDebugConsole = savedInstanceState.getBoolean(DEBUG_CONSOLE);
            mPrevSelectedPage = savedInstanceState.getInt(CURRENT_DEMO,0);
        }//if-else

        mPager = findViewById(R.id.pager);

        mPager.setPageTransformer(new ZoomOutPageTransformer());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.showDemoList, R
                .string.closeDemoList);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDemosListView = findViewById(R.id.demosListLayout);

    }//onCreate

    public void enableUserInput(boolean enable) {
        mPager.setUserInputEnabled(enable);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(DEBUG_CONSOLE, mShowDebugConsole);
        savedInstanceState.putInt(CURRENT_DEMO, mPager.getCurrentItem());
        savedInstanceState.putString(NODE_TAG_ARG,mNode.getTag());
    }

    protected void reloadDemoList(){
        buildDemoAdapter(getNode());
    }

    //Show the list of demos
    protected void ShowDemosList() {
        //Set the listener for hiding the List of Demo
        //mDemosListView.setOnClickListener(view -> view.setVisibility(View.GONE));
        //Make it visible
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDemosListView.setVisibility(View.VISIBLE);
    }

    private void buildDemoAdapter(Node node){
        mPager.registerOnPageChangeCallback(mUpdateActivityTitle);
        final DemosTabAdapter adapter=new DemosTabAdapter(node, getAllDemos(), this);
        int nDemo = adapter.getItemCount();
        int i;

        ImageView mHeaderBoardIcon;
        TextView mHeaderBoardName;
        TextView mHeaderBoardId;
        ImageButton mHeaderDisconnecting;
        List<DemoClass> listOfDemo = new ArrayList<DemoClass>();

        mPager.setAdapter(adapter);

        if(mPrevSelectedPage<nDemo){
            mPager.setCurrentItem(mPrevSelectedPage,false);
        }

        mUpdateActivityTitle.onPageSelected(mPager.getCurrentItem());

        Menu navigationMenu = mNavigationTab.getMenu();
        //remove the old items
        navigationMenu.clear();

        int headersCount = mNavigationTab.getHeaderCount();
        View headerView;

        if(headersCount<=0) {
            /* If there is not a valid Header... Create a new one */
            mNavigationTab.inflateHeaderView(R.layout.header_navigation_drawer_activity_demos);
        }
        /* Take the Header View */
        headerView = mNavigationTab.getHeaderView(0);

        //We must fill the Header here, otherwise the items are not allocated
        mHeaderBoardIcon   = headerView.findViewById(R.id.headerNodeBoardIcon);
        mHeaderBoardIcon.setImageDrawable(AppCompatResources.getDrawable(this,NodeGui.getRealBoardTypeImage(mNode.getType())));
        @DrawableRes int boardImageRes = NodeGui.getRealBoardTypeImage(mNode.getType());
        Glide.with(this)
                .load(boardImageRes)
                .fitCenter()
                .into(mHeaderBoardIcon);//imageView

        mHeaderBoardName = headerView.findViewById(R.id.headerNodeBoardName);
        mHeaderBoardName.setText(mNode.getName());
        mHeaderBoardId = headerView.findViewById(R.id.headerNodeBoardId);
        mHeaderBoardId.setText(mNode.getTag());
        //Button for Disconnecting from the node
        mHeaderDisconnecting = headerView.findViewById(R.id.headerDisconnecting);
        mHeaderDisconnecting.setOnClickListener(view -> {
            //disconnecting from the node
            finish();
        });


        HashMap<String,SubMenu> mapSubMenus = new HashMap<>();

        //Loop inside all the demos
        for(i = 0; i < nDemo; i++) {
            String[] demoCategory = adapter.demoCategory(i);
            int category;
            for(category=0; category<demoCategory.length; category++) {
                String categoryName = demoCategory[category];
                boolean alreadyPresentCategory = mapSubMenus.containsKey(categoryName);
                MenuItem temp;

                CharSequence title = adapter.getPageTitle(i);
                if(adapter.requireChangeName(i)) {
                    BoardFirmware boardFw= node.getFwDetails();
                    if(boardFw!=null) {
                        //title = boardFw.getFw_name() +" "+adapter.getPageTitle(i);
                        title = boardFw.getFw_name();
                    } else {
                        title = adapter.getPageTitle(i);
                    }
                } else {
                    title = adapter.getPageTitle(i);
                }
                if(alreadyPresentCategory) {
                    //the SubMenu was already present
                    SubMenu subMenu = mapSubMenus.get(demoCategory[category]);
                    temp = subMenu.add(title);
                } else {
                    //we need to create a new Submenu
                    SubMenu newSubMenu     = navigationMenu.addSubMenu(categoryName);
                    temp = newSubMenu.add(title);
                    //Add the new entry on HashMap
                    mapSubMenus.put(categoryName,newSubMenu);
                }
                listOfDemo.add(new DemoClass(title.toString(),adapter.getDemoIconRes(i),i,adapter.DemoIsEnabled(i)));
                temp.setIcon(adapter.getDemoIconRes(i));
                temp.setEnabled(adapter.DemoIsEnabled(i));
            }
        }

        mNavigationTab.setNavigationItemSelectedListener(this);


        //RecyclerView for the list of Demo
        recyclerViewDemos = findViewById(R.id.ListOfDemosRecyclerView);
        //this lambda make the jump to the right demo
        adapterListOfDemo = new ListOfDemoAdapter(position -> {
            mPager.setCurrentItem(position,false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDemosListView.setVisibility(View.GONE);
        });
        //Set the adapter for the RecyclerView
        recyclerViewDemos.setAdapter(adapterListOfDemo);
        //Update the Demo List
        adapterListOfDemo.updateDemoList(listOfDemo);

        //if only a demo is available hide the left menu
        if (adapter.getItemCount() == 1) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.syncState();
            mDemosListView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mShowDebugConsole = savedInstanceState.getBoolean(DEBUG_CONSOLE);
        mPager.setCurrentItem(savedInstanceState.getInt(CURRENT_DEMO, 0),false);
    }


    /*
     * this listener will run only the first time we connect, and remove himself when the node
     * connects
     */
    private Node.NodeStateListener mBuildDemoAdapterOnConnection = new Node.NodeStateListener() {
        @Override
        public void onStateChange(@NonNull final Node node, @NonNull Node.State newState, @NonNull Node.State
                prevState) {
            if(newState==Node.State.Connected) {
                runOnUiThread(() -> {
                    invalidateOptionsMenu();
                    buildDemoAdapter(node);
                    showConsoleOutput(mShowDebugConsole);
                });
                node.removeNodeStateListener(this);
            }//if
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //if the node is connected -> this frame is recreated
        if (mNode==null){
            onBackPressed(); // go to the previous activity
            return;
        }
        keepConnectionOpen(true,true);
        ConnectionStatusController mConnectionStatusController = new ConnectionStatusController(mConnectionView, mNode);
        getLifecycle().addObserver(mConnectionStatusController);

        if(!mNode.isConnected()){
            mNode.addNodeStateListener(mBuildDemoAdapterOnConnection);
            NodeConnectionService.connect(this,mNode, mConnectionOption);
        }else{
            buildDemoAdapter(mNode);
            showConsoleOutput(mShowDebugConsole);
        }//if-else
    }

    @Override
    protected void onPause() {
        if(mNode!=null) {
            //remove the listener in case we go on pause before finish the connection
            mNode.removeNodeStateListener(mBuildDemoAdapterOnConnection);
            if (mShowDebugConsole) {
                Debug debug = mNode.getDebug();
                //remove the listener
                if (debug != null)
                    debug.removeDebugOutputListener(mDebugListener);
            }//if
        }//if !=null
        super.onPause();
    }

    @Override
    protected void onStop(){
        if(mNode!=null){
            if(!mKeepConnectionOpen){
                NodeConnectionService.disconnect(this,mNode);
            }
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.acitivity_demos, menu);

        if (mShowDebugConsole) {
            menu.findItem(R.id.showDebugConsole).setTitle(R.string.hideDebugConsole);
        } else {
            menu.findItem(R.id.showDebugConsole).setTitle(R.string.showDebugConsole);
        }//if-else

        //hide debug stuff if not available
        if(mNode!=null) {
            Debug debug = mNode.getDebug();
            if (debug == null) {
                menu.findItem(R.id.openDebugConsole).setVisible(false);
                menu.findItem(R.id.showDebugConsole).setVisible(false);
            }

            if(!STM32OTASupport.isOTANode(mNode,getApplicationContext())) {
                if(mNode.getProtocolVersion()==1) {
                    //check if we can build a fw upgrade, if not hide the button
                    FwUpgradeConsole fwUpgradeConsole = FwUpgradeConsole.getFwUpgradeConsole(getApplicationContext(), mNode, null);
                    if (!enableFwUploading() || fwUpgradeConsole == null) {
                        menu.findItem(R.id.menu_start_fw_upgrade).setVisible(false);
                    }
                } else {
                    BoardFirmware fw_model =  mNode.getFwDetails();
                    if(fw_model!=null) {
                        if(fw_model.getFota().getType()!=null) {
                            if (fw_model.getFota().getType() != BoardFotaType.no) {
                                //Should be ok... but check also that the DebugConsole is present...
                                FwUpgradeConsole fwUpgradeConsole = FwUpgradeConsole.getFwUpgradeConsole(getApplicationContext(), mNode, null);
                                if (!enableFwUploading() || fwUpgradeConsole == null) {
                                    menu.findItem(R.id.menu_start_fw_upgrade).setVisible(false);
                                }
                            } else {
                                menu.findItem(R.id.menu_start_fw_upgrade).setVisible(false);
                            }
                        } else {
                            menu.findItem(R.id.menu_start_fw_upgrade).setVisible(false);
                        }
                    }
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * @return true if you want enable the fw uploading using the debug console, false otherwise
     */
    protected abstract boolean enableFwUploading();

    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed() {

        //check if the current fragment has a back stack and unroll that one before close the activity
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            FragmentManager fm = f.getChildFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                return;
            }
        }

        if(mPager!=null) {
            if(mPager.getAdapter()!=null) {
                //If we have more than one Demo
                if (mPager.getAdapter().getItemCount() > 1) {
                    //Check if the List of demo is visible or not
                    if (mDemosListView.getVisibility() == View.GONE) {
                        ShowDemosList();
                        //mPager.setCurrentItem(0);
                        return;
                    }
                }
            }
        }
        //else
        keepConnectionOpen(false, false);
        super.onBackPressed();
    }

    /**
     * get the node used for this demos
     *
     * @return the node that we will use for this demos
     */
    public Node getNode() {
        return mNode;
    }

    @Override
    public void keepConnectionOpen(boolean keepOpen, boolean showNotification){
        mKeepConnectionOpen = keepOpen;
    }

    /**
     * create a logger in function of the preference selected by the users
     * <p>the default logger is a logCat logger</p>
     *
     * @return logger to use for for dump the features data
     */
    @Override
    protected Feature.FeatureLoggerListener getLogger() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String logType = sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_STORE, "LogCat");
        String dumpPath = getLogDirectory();
        switch (logType) {
            case "LogCat":
                return new FeatureLogLogCat();
            case "DB":
                return new FeatureLogDB(this,dumpPath,getNodesToLog());
            case "File":
                return new FeatureLogCSVFile(dumpPath,getNodesToLog());
            default:
                return null;
        }//switch
    }//getFeatureLogger

    protected List<Node> getNodesToLog(){
        return Collections.singletonList(mNode);
    }

    /**
     * listener that will show the debug message on the textView
     */
    private Debug.DebugOutputListener mDebugListener = new Debug.DebugOutputListener() {
        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull final String message) {
            DemosActivity.this.runOnUiThread(() -> {
                mConsoleText.append(message);
                mConsoleView.fullScroll(View.FOCUS_DOWN);
            });
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull final String message) {
            DemosActivity.this.runOnUiThread(() -> {
                mConsoleText.append(message);
                mConsoleView.fullScroll(View.FOCUS_DOWN);
            });
        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {
        }
    };

    /**
     * show/hide the debug text view and start the debug logging
     *
     * @param enable true if we have to show/enable false for hide/disable
     */
    private void showConsoleOutput(boolean enable) {
        if(mNode==null)
            return;

        Debug debug = mNode.getDebug();
        if (enable) {
            if (debug == null) {
                Toast.makeText(this, R.string.debugNotAvailable, Toast.LENGTH_SHORT).show();
                return;
            }//else
            mConsoleView.setVisibility(View.VISIBLE);
            debug.addDebugOutputListener(mDebugListener);
        } else {
            mConsoleView.setVisibility(View.GONE);
            if (debug!=null) debug.removeDebugOutputListener(mDebugListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

//        if(id== R.id.disconnecting) {
//            //close current activity
//            //disconnecting from the node
//            finish();
//            return true;
//        }

        if (id == R.id.settings) {
            startSettingsActivity(this, mNode);
            return true;
        }

        if(id == R.id.openDebugConsole){
            startDebugConsoleActivity(this, mNode);
            return true;
        }

        if (id == R.id.showDebugConsole) {
            mShowDebugConsole = !mShowDebugConsole;
            showConsoleOutput(mShowDebugConsole);
            invalidateOptionsMenu();
            return true;
        }
        if(id == R.id.menu_start_fw_upgrade){
            if(mShowDebugConsole)
                showConsoleOutput(false);
            startFwUpgradeActivity(this,mNode);
            return true;
        }

        if(id==android.R.id.home)
            keepConnectionOpen(false,false);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        CharSequence titleMenuItem = menuItem.getTitle();
        DemosTabAdapter adapter = (DemosTabAdapter) mPager.getAdapter();
        if(adapter==null)
            return false;
        int nDemo = adapter.getItemCount();
        Node node = getNode();

        for (int i = 0; i < nDemo; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if(adapter.requireChangeName(i)) {
                if(node!=null) {
                    BoardFirmware boardFw = node.getFwDetails();
                    if (boardFw != null) {
                        //title = boardFw.getFw_name() + " " + adapter.getPageTitle(i);
                        title = boardFw.getFw_name();
                    }
                }
            }


            //if (adapter.getPageTitle(i).equals(titleMenuItem)) {
            if(title.equals(titleMenuItem)) {
                mPager.setCurrentItem(i,false);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }//if
        }//for
        return false;
    }//onNavigationItemSelected

    /**
     * start an activity that will show the debug console
     * @param c context used for create the intent
     * @param n node where send the message
     */
    protected void startDebugConsoleActivity(Context c,Node n){
        keepConnectionOpen(true,false);
        Intent i = DebugConsoleActivity.getStartIntent(c,n);
        startActivity(i);
    }

    /**
     * start an activity where the use can change the settings
     * @param c context used for create the intent
     * @param n node that will be configured
     */
    public void startSettingsActivity(Context c,Node n){
        keepConnectionOpen(true,false);
        Intent i = SettingsActivityWithNode.getStartIntent(c,n,true);
        startActivity(i);
    }

    protected void startFwUpgradeActivity(Context context, Node node) {
        keepConnectionOpen(true,false);
        Intent i = FwUpgradeActivity.getStartIntent(context,node,true);
        startActivity(i);
    }

    /**
     * adapter that contains all the demos to show. The demos are a subset of {@code
     * DemosActivity.ALL_DEMOS}
     */
    private static class DemosTabAdapter extends FragmentStateAdapter {

        /**
         * demos that will be displayed to the user
         */
        private ArrayList<Class<? extends DemoFragment>> mDemos = new
                ArrayList<>();

        private ArrayList<Boolean> mDemosEnableFlag = new ArrayList<Boolean>();

        /**
         * tell if the demo will show something if we run it
         * @param demoClass demo that we want show, it must be annotated with the annotation
         * {@link DemoDescriptionAnnotation}
         * @param node node where we will extract the information
         * @return true the node has the needed features for the demo
         */
        private boolean demoIsWorking(Class<? extends DemoFragment> demoClass, Node node) {
            DemoDescriptionAnnotation desc =
                    demoClass.getAnnotation(DemoDescriptionAnnotation.class);
            if (desc == null) //we don't have information -> let it pass
                return true;

            //Check if the demo need the DTDL loaded
            if(desc.requireDTDLLoaded()) {
                if(node.getDTDLModel()==null) {
                    //if we don't have a valid DTDL model
                    return false;
                }
            }

            //check that we have all the feature in the require All field
            //return false if one feature is missing
            Class<? extends Feature>[] requireAll = desc.requireAll();
            for (Class<? extends Feature> f : requireAll) {
                if (node.getFeature(f) == null) {
                    return false;
                }
            }//for

            //Check if there are some Features that exclude the requireAll category
            Class<? extends Feature>[] notRequireOneOf = desc.notRequireOneOf();
            for (Class<? extends Feature> f : notRequireOneOf) {
                if (node.getFeature(f) != null) {
                    return false;
                }
            }//for

            //check that we have all the feature in the require One field
            //return true if we have almost one feature
            Class<? extends Feature>[] requireOneOf = desc.requireOneOf();
            for (Class<? extends Feature> f : requireOneOf) {
                if (node.getFeature(f)  != null) {
                    return true;
                }
            }//for

            // Check that we have at least one Feature
            if(desc.requireAny()) {
                if(node.getFeatures().isEmpty()) {
                    return false;
                } else {
                    return true;
                }
            }

            //return true if we don't have constrains
            return requireOneOf.length == 0;
        }//demoIsWorking

        DemosTabAdapter(@NonNull Node node, Class<? extends
                DemoFragment>[] demos, FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            // Adding the available demos for the current node
            for (Class<? extends DemoFragment> demo : demos ) {
                if (demoIsWorking(demo, node)) {
                    mDemos.add(demo);
                    mDemosEnableFlag.add(true);
                }
            }//for

        }//

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Class<? extends DemoFragment> frag = mDemos.get(position);
            try {
                return frag.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return mDemos.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mDemos.size();
        }

        public CharSequence getPageTitle(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).name();
        }

        public boolean requireChangeName(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).requireChangeName();
        }

        @DrawableRes
        int getDemoIconRes(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).iconRes();
        }
        public Boolean DemoIsEnabled(int position) {
            return mDemosEnableFlag.get(position);
        }

        public String[] demoCategory(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).demoCategory();
        }
    }
}
