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
package com.st.BlueSTSDK;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.WorkerThread;
import android.util.Log;
import android.util.SparseArray;

import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.BlueSTSDKAdvertiseFilter;
import com.st.BlueSTSDK.Utils.BtAdapterNotFound;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.Utils.advertise.InvalidBleAdvertiseFormat;
import com.st.BlueSTSDK.Utils.InvalidFeatureBitMaskException;
import com.st.BlueSTSDK.Utils.advertise.LeScanCallback;
import com.st.BlueSTSDK.Utils.advertise.ScanCallbackBridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton class that manage the discovery of new BLE nodes
 * <p>
 * The ManagerListener method will be called asynchronous inside a different thread.
 * It is safe to remove the listener form inside a callback
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Manager {
    /**
     * pool of thread used for notify to the listeners that the feature have new data
     */
    private static final ExecutorService sThreadPool = Executors.newCachedThreadPool();

    /**
     * map the device byte with the featureMask/Class array
     */
    private static final Map<Byte, SparseArray<Class<? extends Feature>>> sFeatureMapDecoder =
            new HashMap<>();
    static{
        sFeatureMapDecoder.put((byte) 0x81, BLENodeDefines.FeatureCharacteristics
                .Nucleo_Remote_Features);
        sFeatureMapDecoder.put((byte) 0x06, BLENodeDefines.FeatureCharacteristics
                .SENSOR_TILE_BOX_MASK_TO_FEATURE);
    }


    public static List<AdvertiseFilter> buildDefaultAdvertiseList(){
        List<AdvertiseFilter> list = new ArrayList<>(1);
        list.add(new BlueSTSDKAdvertiseFilter());
        return list;
    }

    /**
     * singleton instance of the manager
     */
    private static Manager sInstance = new Manager();
    /**
     * handler used for stop the scan process after the timeout expire
     */
    private Handler mHandler = new Handler();

    /**
     * system ble adapter
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * list of discovered nodes
     */
    final private ArrayList<Node> mDiscoverNode = new ArrayList<>();

    /**
     * object to use for notify the events
     */
    private CopyOnWriteArrayList<ManagerListener> mListeners = new CopyOnWriteArrayList<>();

    /**
     * scanning state
     */
    private boolean mIsScanning = false;

    /**
     * class that will stop a scanning process
     */
    private Runnable mStopScanning = new Runnable() {
        @Override
        public void run() {
            stopDiscovery();
        }
    };

    /**
     * debug class used for track the node state evolution, it just print the node status
     */
    private Node.NodeStateListener mDebugNodeStatus = new Node.NodeStateListener() {
        @Override
        public void onStateChange(@NonNull Node node, @NonNull Node.State newState, @NonNull Node.State prevState) {
            Log.d("NodeStateListener", node.getName() + " " + prevState + "->" + newState);
        }
    };


    /**
     * for each ManagerListener, call the onNodeDiscovered notify the newNode discovery
     * @param newNode new node discovered
     */
    private void notifyNewNodeDiscovered(final Node newNode) {
        for (final ManagerListener listener : mListeners) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onNodeDiscovered(Manager.this, newNode);
                }//run
            });
        }//for
    }//notifyNewNodeDiscovered

    /**
     * class that contains the ble callback
     */
    private BluetoothAdapter.LeScanCallback mScanCallBack_pre21;

    /**
     * ble Callback calss to use for api 21+ it fallback to pre 21 api.
     */
    private ScanCallbackBridge mScanCallBack_post21;

    /**
     * build the manager retrieving the system BluetoothAdapter.
     *
     * @throws BtAdapterNotFound if the device has not a bluetooth adapter.
     */
    private Manager() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBtAdapter == null) {
//            throw new BtAdapterNotFound("Your Device hasn't bluetooth capability");
//        }
    }//Manager

    /**
     * return a singleton instance of the manager
     *
     * @return an instance of Manager
     */
    public static synchronized Manager getSharedInstance() {
        if(sInstance==null)
            sInstance = new Manager();
        return sInstance;
    }

    /**
     * start a discovery process
     * <p>
     * The scanning will run until you don't call {@link Manager#stopDiscovery()}
     * </p>
     *
     * @return true if the process is started, false if it is already running
     */
    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN})
    public boolean startDiscovery() {

        return startDiscovery(-1);
    }//startDiscovery;

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN})
    public boolean startDiscovery(int timeoutMs){
        return startDiscovery(timeoutMs,buildDefaultAdvertiseList());
    }

    /**
     * notify to each listener that the discovery process change its status
     *
     * @param status true the discovery start, false the discovery stop
     */
    private void notifyDiscoveryChange(final boolean status) {
        mIsScanning = status;
        for (final ManagerListener listener : mListeners) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onDiscoveryChange(Manager.this, status);
                }//run
            });
        }//for
    }//notifyDiscoveryChange

    /**
     * start a discovery process, that will automatically stop after <code>timeoutMs</code> milliseconds
     *
     * @param timeoutMs time to wait before stop the discovery process.
     * @return true if the process is started, false if a discovery is already running
     */
    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN})
    public boolean startDiscovery(int timeoutMs,List<AdvertiseFilter> filters) {
        mScanCallBack_pre21 = new LeScanCallback(this,filters);
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            if (mIsScanning)
                return false;
            notifyDiscoveryChange(true);
            startBleScan();
            if (timeoutMs > 0) {
                //stop scan after timeoutMs
                mHandler.postDelayed(mStopScanning, timeoutMs);
            }// if timeout
            return true;
        }//if adapter

        return false;
    }//startDiscovery


    /**
     * start the ble scanning, this method will call the correct method to avoid the deprecated api
     */
    private void startBleScan(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            startBleScan_post21();
        }else
            startBleScan_pre21();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startBleScan_post21() {
        mScanCallBack_post21 = new ScanCallbackBridge(mScanCallBack_pre21);
        if(mBtAdapter!=null && mBtAdapter.getBluetoothLeScanner()!=null) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            List<ScanFilter> noFilter = Collections.emptyList();
            mBtAdapter.getBluetoothLeScanner().startScan(noFilter,settings,mScanCallBack_post21);

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void startBleScan_pre21() {
        mBtAdapter.startLeScan(mScanCallBack_pre21);
    }

    /**
     * stop the ble scanning, this method will call the correct method to avoid the deprecated api
     */
    private void stopBleScan(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            stopBleScan_post21();
        }else
            stopBleScan_pre21();
    }

    /**
     * tell if some nodes can needs to be disconnected
     * @return true if one node is in connected or connecting state
     */
    public boolean hasConnectedNodes(){
        for (Node node : mDiscoverNode){
            Node.State state = node.getState();
            if(state == Node.State.Connected || state == Node.State.Connecting ){
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void stopBleScan_pre21() {
        mBtAdapter.stopLeScan(mScanCallBack_pre21);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopBleScan_post21() {
        if(mBtAdapter!=null && mBtAdapter.getBluetoothLeScanner()!=null) {
            mBtAdapter.getBluetoothLeScanner().stopScan(mScanCallBack_post21);
        }
    }

    public boolean isMPhy2Supported(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mBtAdapter.isLe2MPhySupported();
        }
        return false;
    }

    /**
     * Add a fake Node to the list
     *
     */
    public void addVirtualNode() {
        try {
            addNode(new NodeEmulator());
        }catch(InvalidBleAdvertiseFormat e){
            //never throw from a NodeEmulator
        }//try-catch
    }//addVirtualNode


    /**
     * insert a node into this manager, the addition will be notify to all the listener
     * @param newNode node to add
     * @return true if the node is added, false if a node with the same tag is present
     */
    public boolean addNode(final Node newNode){
        synchronized (mDiscoverNode) {
            String newTag = newNode.getTag();
            Node n = this.getNodeWithTag(newTag);
            if(n!=null){
                //copy the last info to the old node
                n.upDateAdvertising(newNode.getAdvertiseInfo());
                n.isAlive(newNode.getLastRssi());
                return false;
            }else {
                newNode.addNodeStateListener(mDebugNodeStatus);
                mDiscoverNode.add(newNode);
            }
        }//synchronized
        notifyNewNodeDiscovered(newNode);
        return true;
    }//addNode

    /**
     * stop a discovery process
     *
     * @return true if the discovery is stopped, false if there are no running discovery process
     */
    public boolean stopDiscovery() {
        if (mBtAdapter != null && mIsScanning) {
            //remove the timeout
            mHandler.removeCallbacks(mStopScanning);
            //stop the scan
            stopBleScan();
            //notify to the user
            notifyDiscoveryChange(false);
            return true;
        }//if
        return false;
    }//stopDiscovery

    /**
     * @return true if the manager is scanning for find new nodes, false otherwise
     */
    public boolean isDiscovering() {
        return mIsScanning;
    }//isDiscovering

    /**
     * remove all the nodes that aren't bounded with this device
     */
    public void removeNodesExceptBounded(){
        synchronized (mDiscoverNode){
            Collection<Node> removeMe = new ArrayList<>(mDiscoverNode.size());
            for(Node n: mDiscoverNode){
                if(!n.isBounded() && !n.isConnected()){
                    removeMe.add(n);
                }//if
            }//for
            mDiscoverNode.removeAll(removeMe);
        }//synchronized
    }//removeNodes

    /**
     * remove all the nodes (also nodes bounded with this device)
     */
    public void removeNodes(){
        synchronized (mDiscoverNode){
            Collection<Node> removeMe = new ArrayList<>(mDiscoverNode.size());
            removeMe.addAll(mDiscoverNode);
            mDiscoverNode.removeAll(removeMe);
        }
    }

    /**
     * stop the discovery process and remove all the already discovered nodes. the node that are
     * bounded with the device will be kept in the list
     */
    public void resetDiscovery() {
        if (isDiscovering()) {
            stopDiscovery();
        }//if
        removeNodes();
    }//resetDiscovery

    /**
     * add a listener for the event send by the manager
     *
     * @param listener class where notify the manager events
     */
    public void addListener(ManagerListener listener) {
        if (listener != null) {
            mListeners.addIfAbsent(listener);
        }//if
    }//addListener

    /**
     * remove the listener
     *
     * @param listener listener to remove
     */
    public void removeListener(ManagerListener listener) {
        mListeners.remove(listener);
    }

    /**
     * if present return the node with that specific tag
     *
     * @param tag unique string that identify a node
     * @return the node with that tag or null if is not present on the list of the discovered node
     */
    public @Nullable Node getNodeWithTag(@NonNull String tag) {
        synchronized (mDiscoverNode) {
            for (Node n : mDiscoverNode) {
                if (n.getTag().equals(tag))
                    return n;
            }//for
        } //synchronized
        return null;
    }//getNodeWithTag

    /**
     * if present it return a node with that name
     * <p>Note: the name is not unique, we will return the fist node that match the name</p>
     * <p>Note: the match is case sensitive</p>
     *
     * @param name name of the device
     * @return the device with that name or null otherwise
     */
    public @Nullable Node getNodeWithName(String name) {
        synchronized (mDiscoverNode) {
            for (Node n : mDiscoverNode) {
                if (n.getName().equals(name))
                    return n;
            }//for
        }// synchronized
        return null;
    }//getNodeWithName

    /**
     * get the list of all the discovered node
     * <p>Node: The list is unmodifiable, calling add will throw an exception</p>
     *
     * @return the list of all the discovered node until that time
     */
    public List<Node> getNodes() {
        synchronized (mDiscoverNode) {
            return java.util.Collections.unmodifiableList(mDiscoverNode);
        } //synchronized
    }//getNodes


    /**
     * Interface used by this class for notify that a new node is discovered or that the scanning
     * start/stop
     * <p>Note: the Manager is a singleton, we pass to the method for be consistent to the other
     * function in this sdk</p>
     * @author STMicroelectronics - Central Labs.
     */
    public interface ManagerListener {

        /**
         * This method is call when a discovery process start or stop
         *
         * @param m       manager that start/stop the process
         * @param enabled true if a new discovery start, false otherwise
         */
        @WorkerThread
        void onDiscoveryChange(@NonNull Manager m, boolean enabled);

        /**
         * This method is call when the manager discover a new node
         *
         * @param m    manager that discover the node
         * @param node new node discovered
         */
        @WorkerThread
        void onNodeDiscovered(@NonNull Manager m,@NonNull Node node);
    }//ManagerListener

    /**
     * register a new device id or add feature to an already defined device
     * <p>the change will affect only the node discover after this call</p>
     * @param deviceId device type that will use the feature, it can be a new device id
     * @param features array of feature that we will add, the index of the feature will be used
     *                 as feature mask. the feature mask must have only one bit to 1
     * @throws InvalidFeatureBitMaskException throw when a feature is a position that is not a
     * power of 2
     */
    public static void addFeatureToNode(byte deviceId,SparseArray<Class<? extends Feature>> features)
            throws InvalidFeatureBitMaskException {
        SparseArray<Class<? extends Feature>> updateMe;
        if(!sFeatureMapDecoder.containsKey(deviceId)){
            updateMe = BLENodeDefines.FeatureCharacteristics.DEFAULT_MASK_TO_FEATURE.clone();
            sFeatureMapDecoder.put(deviceId,updateMe);
        }else{
            updateMe = sFeatureMapDecoder.get(deviceId);
        }//if-else

        SparseArray<Class<? extends Feature>> addMe = features.clone();

        long mask=1;
        //we test all the 32bit of the feature mask
        for(int i=0; i<32; i++ ){
            Class<? extends Feature> featureClass = addMe.get((int)mask);
            if (featureClass != null) {
                updateMe.append((int) mask, featureClass);
                addMe.remove((int)mask);
            }
            mask=mask<<1;
        }//for

        if(addMe.size()!=0)
            throw new InvalidFeatureBitMaskException("Not all elements have a single bit in " +
                    "as key");
    }

    /**
     * get a copy Features available for the deviceId
     * @param deviceId device type that will use the feature, it can be a new device id
     * @return null if the deviceId is not registered else the list of available feature for that
     * deviceId
     */
    public static @NonNull SparseArray<Class<? extends Feature>> getNodeFeatures(byte deviceId){
        SparseArray<Class<? extends Feature>> featureList;

        if(sFeatureMapDecoder.containsKey(deviceId)) {
            featureList = sFeatureMapDecoder.get(deviceId).clone();
        }else{
            featureList = BLENodeDefines.FeatureCharacteristics.DEFAULT_MASK_TO_FEATURE.clone();
        }

        return featureList;
    }

}//Manager
