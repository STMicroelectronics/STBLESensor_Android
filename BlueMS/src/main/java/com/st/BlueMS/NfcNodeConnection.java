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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BleAdvertiseParser;
import com.st.BlueSTSDK.Utils.InvalidBleAdvertiseFormat;
import com.st.BlueSTSDK.Utils.NodeScanActivity;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.Utils.SearchSpecificNode;

import java.util.Arrays;

/**
 * activity called when a nfc tag is detected, it analyse the content and search for the node,
 * after the search it start the demo activity
 * the tag have to contains a NdeMessage with the playload with :
 * - 6 bytes that contains the mac address of the board, in this case a  scan process will
 * trigger for retrieve the advertise package
 * - the advertise package that contains the mac address, in this case the node is create and
 * added to the manager without scanning
 */
public class NfcNodeConnection extends NodeScanActivity {
    private static final int SEARCH_NODE_TIMEOUT_MS=2000;
    private static byte[] sPairingPin;

    private static byte[] pinToByte(int pin){
        if(pin > 999999 || pin <0)
            throw new IllegalArgumentException("Pin must have at maximum 6 numbers");

        byte pinByte[] = new byte[6];
        int factor = 1;
        for(int i=0;i<6;i++){
            byte digit =(byte) ((pin/factor)%10); //extract the digit in position i or 0
            pinByte[5-i]=(byte) ('0'+digit); //convert digit to ascii
            factor*=10;
        }//for i
        return pinByte;
    }//pinToByte

    /**
     * call when we receive a android.bluetooth.device.action.PAIRING_REQUEST -> we are create a
     * bound
     */
    public static class InsertPairPin extends BroadcastReceiver {
        private static final int SET_PIN_DELAY_MS=1000;

        private void onPairingRequest(Context context,Intent intent){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //check that is one of our device
            if(Manager.getSharedInstance().getNodeWithTag(device.getAddress())==null)
                return;
            //check that we have a pin
            if(sPairingPin!=null) {
                //add this delay help to avoid ble internal stack errors
                //Log.d("InsertPin","setPin "+Arrays.toString(sPairingPin));
                device.setPin(sPairingPin);
                //sPairingPin=null; //invalidate the pin for the next pairing
                //device.createBond(); // is not necessary we already doing the pairing
            }//if
        }

        private void onBoundStateChange(Context context,Intent intent){

            int boundState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.BOND_NONE);
            Log.d("InsertPin", "onBoundStateChange State:" + boundState);
            //android lollipop show 2 notification -> we delete the pin only when we have finish
            // to bound
            if(boundState != BluetoothDevice.BOND_BONDING)
                sPairingPin=null; //the pairing is done -> remove the pin
            Log.d("InsertPin", "onBoundStateChange "+ Arrays.toString(sPairingPin));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    onPairingRequest(context,intent);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    onBoundStateChange(context,intent);
                    break;
            }//switch
        }//onReceives
    }

    /** show a toast message and start the NodeListActivity
     *
     * @param msgId message to display in the toast message
     */
    private void startNodeListActivityAndFinish( @StringRes int msgId){
        Toast.makeText(NfcNodeConnection.this,msgId,Toast.LENGTH_LONG).show();
        startActivity(new Intent(NfcNodeConnection.this, NodeListActivity.class));
        finish();
    }

    /**
     * true if a node read with the nfc is still connected -> we avoid to read the tag again
     */
    private static boolean sIsDemoRunning;
    /**
     * node mac address
     */
    private String mNodeTag=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if the previous node is connected stop the activity
        if(sIsDemoRunning)
            finish();
        setContentView(R.layout.activity_nfc_node_connection);

        Intent nfcIntent = getIntent();

        //retrieve the nfc message
        final Parcelable[] ndefMessages = nfcIntent.getParcelableArrayExtra
                (NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg =((NdefMessage) ndefMessages[0]);

        byte messagePayload[] = (msg.getRecords()[0]).getPayload();

        if (messagePayload.length>13){
            int pin = NumberConversion.LittleEndian.bytesToInt32(messagePayload,
                    messagePayload.length-4);
            sPairingPin=pinToByte(pin);
            try {
                mNodeTag = new BleAdvertiseParser(messagePayload).getAddress();
                Log.d("InsertPin", "nodeTag: "+mNodeTag + "Pin: "+pin);
            } catch (InvalidBleAdvertiseFormat invalidBleAdvertiseFormat) {
                startNodeListActivityAndFinish(R.string.invalidNfc);
            }
        }else {
            startNodeListActivityAndFinish(R.string.invalidNfc);
        }//if-else
    }//onCreate


    @Override
    protected void onResume() {
        super.onResume();
        if(mNodeTag!=null) { // start search the node
            sIsDemoRunning =true; //avoid to create other activity
            new SearchNodeTask(this, SEARCH_NODE_TIMEOUT_MS).execute(mNodeTag);
        }
    }


    private class SearchNodeTask extends SearchSpecificNode{
        public SearchNodeTask(NodeScanActivity activity, int searchTimeMs){
            super(activity,searchTimeMs);
        }

        /**
         * when we the task finish to run we start the demo or we go back on the nodeList activity
         * @param result node found or null if we did found anything
         */
        protected void onPostExecute(Node result) {
            if(result==null) {
                sIsDemoRunning=false; //permit to create other activity
                startNodeListActivityAndFinish(R.string.nodeNotFound);
            }else {
                NfcNodeConnection.this.startActivity(
                        com.st.BlueMS.DemosActivity.getStartIntent(NfcNodeConnection.this, result));
                mNodeTag=null;
                result.addNodeStateListener(new Node.NodeStateListener() {
                    @Override
                    public void onStateChange(Node node, Node.State newState, Node.State prevState) {
                        if(newState== Node.State.Disconnecting){
                            sIsDemoRunning =false; //we we disconnect we re enable nfc + remove
                            // the listener
                            node.removeNodeStateListener(this);
                        }

                    }
                });
                finish();
            }//if-else
        }//onPostExecute

    }//SearchNode

}
