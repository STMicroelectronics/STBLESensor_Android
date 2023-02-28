/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Example;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NodeScanActivity;

/**
 * This activity will show a list of device that are supported by the sdk
 */
public class ScanActivity extends NodeScanActivity implements AbsListView.OnItemClickListener {

    /**
     * number of millisecond that we spend looking for a new node
     */
    private final static int SCAN_TIME_MS = 10 * 1000; //10sec

    /**
     * adapter that will build the gui for each discovered node
     */
    private NodeArrayAdapter mAdapter;

    /**
     * listener that will change button gui when the discover stop
     */
    private Manager.ManagerListener mUpdateDiscoverGui = new Manager.ManagerListener() {

        /**
         * call the stopNodeDiscovery for update the gui state
         * @param m manager that start/stop the process
         * @param enabled true if a new discovery start, false otherwise
         */
        @Override
        public void onDiscoveryChange(Manager m, boolean enabled) {
            if (!enabled)
                ScanActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopNodeDiscovery();
                    }//run
                });
        }//onDiscoveryChange

        @Override
        public void onNodeDiscovered(Manager m, Node node) {
        }//onNodeDiscovered
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        AbsListView listView = (AbsListView) findViewById(R.id.nodeListView);
        //create the adapter and set it to the list view
        mAdapter = new NodeArrayAdapter(this);
        listView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

        //add the already discovered nodes
        mAdapter.addAll(mManager.getNodes());

    }

    /**
     * clear the adapter and the manager list of nodes
     */
    private void resetNodeList() {
        mManager.resetDiscovery();
        mAdapter.clear();
        //some nodes can survive if they are bounded with the device
        mAdapter.addAll(mManager.getNodes());
    }//resetNodeList

    /**
     * check that the bluetooth is enabled and register the lister to the manager
     */
    @Override
    protected void onStart() {
        super.onStart();

        //add the listener that will hide the progress indicator when the first device is discovered
        mManager.addListener(mUpdateDiscoverGui);
        //disconnect all the already discovered device
        mAdapter.disconnectAllNodes();
        //add as listener for the new nodes
        mManager.addListener(mAdapter);
        resetNodeList();
        startNodeDiscovery();
    }//onStart

    /**
     * stop the discovery and remove all the lister that we attach to the manager
     */
    @Override
    protected void onStop() {
        if (mManager.isDiscovering())
            mManager.stopDiscovery();
        //remove the listener add by this class
        mManager.removeListener(mUpdateDiscoverGui);
        mManager.removeListener(mAdapter);
        super.onStop();
    }//onPause

    /**
     * build the menu, it show the start/stop button in function of the manager state (if it is
     * scanning or not )
     *
     * @param menu menu where add the items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);

        boolean isScanning = (mManager != null) && mManager.isDiscovering();
        menu.findItem(R.id.menu_stop_scan).setVisible(isScanning);
        menu.findItem(R.id.menu_start_scan).setVisible(!isScanning);

        return true;
    }

    /**
     * called when the user select a menu item
     *
     * @param item item selected, it will remove the discovered nodes and start a new scan or
     *             stop the scanning
     * @return true if the item is handle by this method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_start_scan) {
            resetNodeList();
            startNodeDiscovery();
            return true;
        }//else
        if (id == R.id.menu_stop_scan) {
            stopNodeDiscovery();
            return true;
        }//else
        return super.onOptionsItemSelected(item);

    }//onOptionsItemSelected

    /**
     * method start a discovery and update the gui for the new state
     */
    public void startNodeDiscovery() {
        super.startNodeDiscovery(SCAN_TIME_MS);
        invalidateOptionsMenu(); //ask to redraw the menu for change the menu icon
    }

    /**
     * method that stop the discovery and update the gui state
     */
    public void stopNodeDiscovery() {
        super.stopNodeDiscovery();
        invalidateOptionsMenu();//ask to redraw the menu for change the menu icon
    }

    /**
     * when a node is selected we start to connect with that node and show the demo activity
     *
     * @param parent   adapter where the click is item
     * @param view     item clicked
     * @param position position clicked
     * @param id       item id clicked
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Node n = mAdapter.getItem(position);
        if(n==null)
            return;

        Intent i = FeatureListActivity.getStartIntent(this, n);
        startActivity(i);
    }//onItemClick

}//ScanActivity
