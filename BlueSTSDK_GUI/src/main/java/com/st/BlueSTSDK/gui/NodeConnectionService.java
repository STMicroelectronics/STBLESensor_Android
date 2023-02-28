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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;

import java.util.HashSet;
import java.util.Set;

/**
 * Service that connect a node, in this way the node connection is bounded with the live cycle of the
 * service
 */
public class NodeConnectionService extends Service {

    private static final String DISCONNECT_ACTION = NodeConnectionService.class.getName() + ".DISCONNECT";
    private static final String DISCONNECT_ALL_ACTION = NodeConnectionService.class.getName() + ".DISCONNECT_ALL";
    private static final String CONNECT_ACTION = NodeConnectionService.class.getName() + ".CONNECT";
    private static final String NODE_TAG_ARG = NodeConnectionService.class.getName() + ".NODE_TAG";
    private static final String CONNECTION_PARAM_ARG = NodeConnectionService.class.getName() + ".CONNECTION_PARAM_ARG";

    private static final int STOP_SERVICE = 1;
    private static final String CONNECTION_NOTIFICATION_CHANNEL = "ConnectionNotification";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * start the service asking to connect with the node
     * @param c context used for start the service
     * @param n node to connect
     */
    static public void connect(Context c, Node n){
        connect(c,n,null);
    }

    /**
     * start the service asking to connect with the node
     * @param c context used for start the service
     * @param n node to connect
     * @param resetCache true to try reset the ble uuid for the node
     * @deprecated use {{@link #connect(Context, Node, ConnectionOption)}}
     */
    @Deprecated
    static public void connect(Context c, Node n, boolean resetCache ){
        Log.d("Service","connect" + n.getName());
        connect(c,n,ConnectionOption.builder().resetCache(resetCache).build());
    }

    /**
     * start the service asking to connect with the node
     * @param c context used for start the service
     * @param n node to connect
     * @param option connection options, if no parameters present the default one will be used
     */
    static public void connect(Context c, Node n,@Nullable ConnectionOption option ){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(CONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        if(option==null)
            option = ConnectionOption.builder().build();
        i.putExtra(CONNECTION_PARAM_ARG,option);
        ContextCompat.startForegroundService(c,i);
    }

    /**
     * build the intent that will ask to disconnect the node
     * @param c context used for crate the intent
     * @param n node to disconnect
     * @return intent that will disconnect the node
     */
    private static Intent buildDisconnectIntent(Context c,Node n){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(DISCONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        return i;
    }

    /**
     * build the intent that will ask to disconnect the node
     * @param c context used for crate the intent
     * @return intent that will disconnect the node
     */
    private static Intent buildDisconnectAllIntent(Context c){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(DISCONNECT_ALL_ACTION);
        return i;
    }

    /**
     * ask to the service to disconnect the node
     * @param c context used for crate the intent
     * @param n node to disconnect
     */
    static public void disconnect(Context c, Node n){
        if(n.isConnected())
            ContextCompat.startForegroundService(c,buildDisconnectIntent(c,n));
    }

    /**
     * ask to the service to disconnect the node
     * @param c context used for crate the intent
     */
    static public void disconnectAllNodes(Context c){
        if(Manager.getSharedInstance().hasConnectedNodes())
            ContextCompat.startForegroundService(c,buildDisconnectAllIntent(c));
    }


    /**
     * set of node managed by this service
     */
    private Set<Node> mConnectedNodes = new HashSet<>();

    /**
     * if the node enter in a disconnected state try to connect again
     */
    private Node.NodeStateListener mStateListener = (node, newState, prevState) -> {
        ConnectionOption option = node.getConnectionOption();
        if ((newState == Node.State.Unreachable ||
            newState == Node.State.Dead ||
            newState == Node.State.Lost ) &&
                mConnectedNodes.contains(node)) {
                // if the autoConnect if on the system will connect automatically
                if(!option.enableAutoConnect()) {
                    Log.d("NodeConnectionService","re connect" + node.getTag());
                    /* Try To Reconnect */
                    node.connect(NodeConnectionService.this, option);
                } else {
                    node.disconnect();
                }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            removeConnectionNotification();
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Log.d("ConnectionService","id: "+startId+" action:"+action);
        if(CONNECT_ACTION.equals(action)){
            connect(startId,intent);
        }else if (DISCONNECT_ACTION.equals(action)) {
            disconnect(startId,intent);
        }else if (DISCONNECT_ALL_ACTION.equals(action)){
            disconnectAll(startId);
        }

        return START_STICKY;
    }

    private void disconnectAll(int startId) {
        startForeground(startId,buildDisconnectNotification());
        disconnectAll();
    }

    /**
     * if present remove the connection notification
     */
    private void removeConnectionNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectAll();
    }

    @Override
    public void onTaskRemoved (Intent rootIntent){
        disconnectAll();
        stopSelf(); // stop the service
    }

    private PendingIntent getDisconnectPendingIntent(Node n){
        Intent stopServiceIntent = buildDisconnectIntent(this,n);
        return PendingIntent.getService(this, STOP_SERVICE, stopServiceIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * create the action to visualize into the notification
     * @param disconnectIntent itent to exec when the user click on the notificaiton
     * @return action that will disconnect the node
     */
    private NotificationCompat.Action buildDisconnectAction(PendingIntent disconnectIntent){
        return new NotificationCompat.Action.Builder(
                android.R.drawable.ic_delete,
                getString(R.string.NodeConn_disconnect),disconnectIntent).build();
    }//buildDisconnectAction

    /**
     * get the logo to display in the notificaiton, if present it will use the app logo, otherwise the
     * ic_dialog_alert icon
     * @return icon to use in the notificaiton
     */
    private @DrawableRes int getResourceLogo(){
        String packageName = getPackageName();
        @DrawableRes int logo=R.drawable.ic_warning_24dp;
        try {
            final ApplicationInfo applicationInfo=getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if(applicationInfo.logo!=0)
                logo = applicationInfo.logo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return logo;
    }

    private String createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            String desc = getString(R.string.NodeConn_channelDescription);
            String name = getString(R.string.NodeConn_channelName);
            NotificationChannel channel = new NotificationChannel(CONNECTION_NOTIFICATION_CHANNEL,
                    name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(desc);
            if(manager!=null)
                manager.createNotificationChannel(channel);
        }

        return CONNECTION_NOTIFICATION_CHANNEL;
    }

    private Notification buildConnectionNotification(Node n){
        if (n==null || !mConnectedNodes.contains(n) )
            return null;
        @DrawableRes int notificationIcon = getResourceLogo();
        PendingIntent disconnectNode = getDisconnectPendingIntent(n);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this,createNotificationChannel())
                        .setContentTitle(getString(R.string.NodeConn_nodeConnectedTitile))
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                        .setDeleteIntent(disconnectNode)
                        .addAction(buildDisconnectAction(disconnectNode))
                        .setContentText(getString(R.string.NodeConn_nodeIsConnected,n.getName()));
        notificationBuilder.setSmallIcon(notificationIcon);

        return notificationBuilder.build();
    }

    private Notification buildDisconnectNotification(){
        @DrawableRes int notificationIcon = getResourceLogo();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this,createNotificationChannel())
                        .setContentTitle(getString(R.string.NodeConn_nodeDisconnectionTitile))
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                        .setContentText(getString(R.string.NodeConn_nodeDisconnectionDesc));
        notificationBuilder.setSmallIcon(notificationIcon);

        return notificationBuilder.build();
    }

    /**
     * start the connection with the node
     * @param intent node to connect
     */
    private void connect(int startId,Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        Log.d("NodeConnectionService","connect " + tag);
        ConnectionOption options = intent.getParcelableExtra(CONNECTION_PARAM_ARG);
        Node n = Manager.getSharedInstance().getNodeWithTag(tag);
        if(n!=null)
            if(!mConnectedNodes.contains(n)) {
                mConnectedNodes.add(n);
                n.addNodeStateListener(mStateListener);
                n.connect(this,options);
                startForeground(startId,buildConnectionNotification(n));
            }
    }

    /**
     * get the connected node manage by the service or null if there are no nodes with that tag
     * that area manage by the service
     * @param tag node that to search
     * @return the node with that tag manage by the service or null
     */
    private @Nullable Node findConnectedNodeWithTag(String tag){
        for(Node n: mConnectedNodes){
            if(n.getTag().equals(tag))
                return n;
        }//for
        return null;
    }

    private void disconnectAll(){
        //disconnect all the nodes and remove the notification
        for(Node n : mConnectedNodes){
            Log.d("ConnectionService", "disconnectAll:"+n.getTag()+"mConnectedNodes:"+mConnectedNodes);
            n.removeNodeStateListener(mStateListener);
            n.disconnect();
        }
        mConnectedNodes.clear();
        removeConnectionNotification();
        stopSelf();
    }

    /**
     * disconnect the node
     * @param startId
     * @param intent node to disconnect
     */
    private void disconnect(int startId,Intent intent) {
        startForeground(startId,buildDisconnectNotification());
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        Log.d("NodeConnectionService","disconnect " + tag+" mConnectedNodes:"+mConnectedNodes);

        Node n = findConnectedNodeWithTag(tag);
        if(n==null){
            if(mConnectedNodes.size()==0){
                removeConnectionNotification();
                stopSelf();
            }//if
            return;
        }

        mConnectedNodes.remove(n);
        n.removeNodeStateListener(mStateListener);
        n.disconnect();

        if(mConnectedNodes.size()==0){
            removeConnectionNotification();
            stopSelf();
        }//if

    }

}