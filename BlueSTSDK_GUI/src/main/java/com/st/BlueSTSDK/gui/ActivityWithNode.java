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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusController;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusView;

/**
 * Activity that contains a Node. It will automatically display a dialog during the node connection
 * and try to reconnect the node if the connection get lost.
 */
public class ActivityWithNode extends AppCompatActivity implements NodeContainer {

    private final static String NODE_TAG = DebugConsoleActivity.class.getCanonicalName()
            + ".NODE_TAG";

    private final static String KEEP_CONNECTION_OPEN = ActivityWithNode.class.getCanonicalName() +
            ".KEEP_CONNECTION_OPEN";

    private final static String CONNECTION_OPTIONS = ActivityWithNode.class.getCanonicalName() + "" +
            ".CONNECTION_OPTIONS";


    private boolean mKeepConnectionOpen;

    protected Node mNode;
    private ConnectionOption mConnecitonOption;
    private ConnectionStatusView mConnectionStatusView;

    protected static Intent getStartIntent(Context c, @NonNull Class activity, @NonNull Node
            node, boolean keepConnectionOpen){
        return getStartIntent(c,activity,node,keepConnectionOpen,null);
    }

    protected static Node getNodeFromIntent(Intent intent){
        String nodeTag = intent.getStringExtra(NODE_TAG);
        return Manager.getSharedInstance().getNodeWithTag(nodeTag);
    }

    /**
     * create an intent for start the activity that will log the information from the node
     *
     * @param c    context used for create the intent
     * @param node note that will be used by the activity
     * @param keepConnectionOpen true if the node must remain connected also if this activity get stopped
     * @return intent for start this activity
     */
    protected static Intent getStartIntent(Context c, @NonNull Class activity, @NonNull Node
            node,boolean keepConnectionOpen,@Nullable ConnectionOption option) {
        Intent i = new Intent(c, activity);
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtra(KEEP_CONNECTION_OPEN, keepConnectionOpen);
        if(option!=null)
            i.putExtra(CONNECTION_OPTIONS,option);
        return i;
    }


    private void addConnectionProgressView(){
        ViewGroup rootView = findViewById(android.R.id.content);
        mConnectionStatusView = new ConnectionStatusView(this);
        rootView.addView(mConnectionStatusView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        // recover the node
        mNode = getNodeFromIntent(i);
        mKeepConnectionOpen = i.getBooleanExtra(KEEP_CONNECTION_OPEN,false);
        mConnecitonOption = i.getParcelableExtra(CONNECTION_OPTIONS);

    }//onCreate

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addConnectionProgressView();
    }

    /**
     * if we have to leave this activity, we force to keep the connection open, since we go back
     * in the {@link DemosActivity}
     */
    @Override
    public void onBackPressed() {
        keepConnectionOpen(true,false);
        super.onBackPressed();
    }//onBackPressed


    @Override
    protected void onResume() {
        super.onResume();
        //if the node is connected -> this frame is recreated
        if (mNode==null){
            onBackPressed(); // go to the previous activity
            return;
        }
        keepConnectionOpen(true,true);

        ConnectionStatusController mConnectionStatusController = new ConnectionStatusController(mConnectionStatusView, mNode);
        getLifecycle().addObserver(mConnectionStatusController);

        if(!mNode.isConnected()){
            NodeConnectionService.connect(this,mNode,mConnecitonOption);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mNode==null)
            return;

        if(!mKeepConnectionOpen){
            NodeConnectionService.disconnect(this,mNode);
        }
    }

    /**
     * call when the user press the back button on the menu bar, we are leaving this activity so
     * we keep the connection open since we are going int the {@link DemosActivity}
     *
     * @param item menu item clicked
     * @return true if the item is handle by this function
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button, we go back in the same task
            //for avoid to recreate the DemoActivity
            case android.R.id.home:
                keepConnectionOpen(true,false);
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected

    @Override
    public @Nullable Node getNode(){
        return mNode;
    }

    @Override
    public void keepConnectionOpen(boolean keepOpen,boolean showNotificaiton){
        mKeepConnectionOpen=keepOpen;
    }

}
