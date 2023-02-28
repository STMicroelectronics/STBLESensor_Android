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
package com.st.BlueSTSDK.gui.preferences;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.util.ConnectProgressDialog;

/**
 * Preference Fragment to use for set preference inside the node, since this fragment will keep a
 * connection with the node.
 */
public abstract class PreferenceFragmentWithNode extends PreferenceFragment {

    private final static String NODE_TAG = PreferenceFragmentWithNode.class.getCanonicalName()
            + ".NODE_TAG";

    private final static String KEEP_CONNECTION_OPEN = PreferenceFragmentWithNode.class.getCanonicalName() +
            ".KEEP_CONNECTION_OPEN";

    private boolean mKeepConnectionOpen;

    private ConnectProgressDialog mConnectionProgressDialog;

    protected Node mNode;

    public static Bundle addStartArgs(@Nullable Bundle args, @NonNull String nodeTag, boolean keepConnectionOpen) {
        if (args == null)
            args = new Bundle();
        args.putString(NODE_TAG, nodeTag);
        args.putBoolean(KEEP_CONNECTION_OPEN, keepConnectionOpen);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        String nodeTag;
        if (savedInstanceState == null) {
            nodeTag = args.getString(NODE_TAG);
            mKeepConnectionOpen = args.getBoolean(KEEP_CONNECTION_OPEN, false);
        } else {
            nodeTag = savedInstanceState.getString(NODE_TAG);
            mKeepConnectionOpen = savedInstanceState.getBoolean(KEEP_CONNECTION_OPEN, false);
        }
        // recover the node
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);

    }//onCreate


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NODE_TAG, mNode.getTag());
        outState.putBoolean(KEEP_CONNECTION_OPEN, mKeepConnectionOpen);
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity context = getActivity();
        //if the node is connected -> this frame is recreated
        if (mNode == null) {
            getActivity().onBackPressed(); // go to the previous activity
            return;
        }
        keepConnectionOpen(true, true);
        mConnectionProgressDialog = new ConnectProgressDialog(context, mNode.getName());
        mNode.addNodeStateListener(mConnectionProgressDialog);
        if (!mNode.isConnected()) {
            mNode.addNodeStateListener(new Node.NodeStateListener() {
                @Override
                public void onStateChange(@NonNull Node node, @NonNull Node.State newState, @NonNull Node.State prevState) {
                    if(newState== Node.State.Connected) {
                        onNodeIsAvailable(node);
                        node.removeNodeStateListener(this);
                    }
                }
            });
            NodeConnectionService.connect(context, mNode);
        }else
            onNodeIsAvailable(mNode);
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity context = getActivity();
        if(mNode!=null) {
            mNode.removeNodeStateListener(mConnectionProgressDialog);
            if (!mKeepConnectionOpen) {
                NodeConnectionService.disconnect(context, mNode);
            }
        }
    }

    public void keepConnectionOpen(boolean keepOpen, boolean showNotification) {
        mKeepConnectionOpen = keepOpen;
    }

    protected abstract void onNodeIsAvailable(Node node);

}