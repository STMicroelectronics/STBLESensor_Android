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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;

/**
 * this is headless fragment that is used for store a connected node. this fragment will not be
 * destroyed when an activity is destroyed for change its configuration -> using this fragment you
 * avoid to connect/disconnect multiple times in a short time..
 *
 * this class will start the connection with the node inside the onCreate and close it inside the
 * onDestroy.
 * If you move to another activity that will use the same node you can avoid to disconnect calling
 * the method {@link NodeContainerFragment#keepConnectionOpen(boolean, boolean)}
 */
@Deprecated
public class NodeContainerFragment extends Fragment implements NodeContainer {
    private final static String TAG = NodeContainerFragment.class.getCanonicalName();
    /**
     * string used for store our data in the fragment args
     */
    final static String NODE_TAG=NodeContainerFragment.class.getCanonicalName()+".NODE_TAG";
    final static String RESETCACHE_TAG=NodeContainerFragment.class.getCanonicalName()+".RESETCACHE_TAG";

    /**
     * prepare the arguments to pass to this fragment
     * @param node node that this fragment has to manage
     * @param resetCache true if you want to reload all the service and characteristics from the
     *                   device
     * @return bundle to pass as argument to a NodeContainerFragment
     */
    public static Bundle prepareArguments(Node node,boolean resetCache) {
        Bundle args = new Bundle();
        args.putString(NODE_TAG,node.getTag());
        args.putBoolean(RESETCACHE_TAG,resetCache);
        return args;
    }

    /**
     * prepare the arguments to pass to this fragment
     * @param node node that this fragment has to manage
     * @return bundle to pass as argument to a NodeContainerFragment
     */
    public static Bundle prepareArguments(Node node) {
        return prepareArguments(node,false);
    }

    /**
     * progress dialog to show when we wait that the node connection
     */
    private ProgressDialog mConnectionWait;

    /**
     * node handle by this class
     */
    private Node mNode=null;

    /**
     * true if the user ask to reset the device cache before open the connection
     */
    private boolean mResetNodeCache;
    /**
     * node state listener set by the user,
     */
    private Node.NodeStateListener mUserNodeStateListener=null;

    /**
     * true if the user ask to skip the disconnect when the fragment is destroyed
     */
    private boolean userAskToKeepConnection =false;

    /**
     * node listener that will manage the dialog + pass the data to the user listener if it is set
     *
     */
    private Node.NodeStateListener mNodeStateListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(@NonNull final Node node, @NonNull Node.State newState, @NonNull Node.State prevState) {
            final Activity activity = NodeContainerFragment.this.getActivity();
            //we connect -> hide the dialog
            if ((newState == Node.State.Connected) && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //close the progress dialog
                        mConnectionWait.dismiss();
                    }
                });
            //error state -> show a toast message and start a new connection
            } else if ((newState == Node.State.Unreachable ||
                    newState == Node.State.Dead ||
                    newState == Node.State.Lost) && activity != null) {
                final String msg;
                switch (newState) {
                    case Dead:
                        msg = String.format(getResources().getString(R.string.progressDialogConnMsgDeadNodeError),
                                node.getName());
                        break;
                    case Unreachable:
                        msg = String.format(getResources().getString(R.string.progressDialogConnMsgUnreachableNodeError),
                                node.getName());
                        break;
                    case Lost:
                    default:
                        msg = String.format(getResources().getString(R.string
                                        .progressDialogConnMsgLostNodeError),
                                node.getName());
                        break;
                }//switch
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mConnectionWait.isShowing())
                            mConnectionWait.show(); //show the dialog and set the listener for hide it
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();

                        mNode.connect(getActivity(), ConnectionOption.builder()
                                .resetCache(mResetNodeCache)
                                .build());
                    }
                });
            }
            if(mUserNodeStateListener!=null)
                mUserNodeStateListener.onStateChange(node,newState,prevState);
        }//onStateChange
    };


    /**
     * prepare the progress dialog tho be shown setting the title and the message
     * @param nodeName name of the node that we will use
     */
    private void setUpProgressDialog(String nodeName){
        mConnectionWait = new ProgressDialog(getActivity(),ProgressDialog.STYLE_SPINNER);
        mConnectionWait.setTitle(R.string.progressDialogConnTitle);
        mConnectionWait.setMessage(String.format(getResources().getString(R.string
                        .progressDialogConnMsg),
                nodeName));
    }//setUpProgressDialog

    /**
     * return the node handle by this fragment
     * @return return the node handle by this fragment
     */
    public @Nullable Node getNode(){return mNode;}

    /**
     * add a user defined state listener for the node handle by this class.
     * this method is usefull because it can be called before the node is created/conneteed -> it
     * can intercept the connection event/process
     * @param listener used defined node state listener
     */
    public void setNodeStateListener(Node.NodeStateListener listener){
        mUserNodeStateListener=listener;
    }


    @TargetApi(23)
    @Override public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        //if (Build.VERSION.SDK_INT >= 23) {
        super.onAttach(context);
        onAttachToContext(context);
        //}
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    private void onAttachToContext(Context context) {
        String nodeTag = getArguments().getString(NODE_TAG);
        mResetNodeCache = getArguments().getBoolean(RESETCACHE_TAG,false);
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);
    }

    /**
     * set this fragment as retain state + retrive the node from the manager
     * @param savedInstanceState
     */
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(mNode!=null)
            setUpProgressDialog(mNode.getName());
    }

    /**
     * if not already connected, show the dialog and stat the connection with the node
     */
    @Override
    public void onResume(){
        super.onResume();
        if(mNode!=null) {
            Node.State state = mNode.getState();
            //avoid to start the connection if we are already doing a connection
            if (state != Node.State.Connected && state != Node.State.Connecting) {
                mConnectionWait.show(); //show the dialog and set the listener for hide it
                mNode.connect(getActivity(), mResetNodeCache);
                mResetNodeCache = false; //reset the cache only the first time that we connect
            }//if
            mNode.addNodeStateListener(mNodeStateListener);
        }//if !=null
    }


    /**
     * if we are still connection hide the progress dialog
     */
    @Override
    public void onPause(){
        //dismiss the dialog if we are showing it
        if(mConnectionWait!=null && mConnectionWait.isShowing()){
            mConnectionWait.dismiss();
        }//if

        mNode.removeNodeStateListener(mNodeStateListener);

        super.onPause();
    }

    public void keepConnectionOpen(boolean doIt, boolean showNotification){
        userAskToKeepConnection =doIt;
    }

    /**
     * if the user did do explicity disconnect the node
     */
    @Override
    public void onDestroy(){

        if(mNode!=null && mNode.isConnected()){
            if(!userAskToKeepConnection) {
                mNode.disconnect();
            }
        }//if

        super.onDestroy();
    }

}

