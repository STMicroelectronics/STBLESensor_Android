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
package com.st.STM32WB.p2pDemo;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

/**
 * Activity that is containing the demo for the STM32WB board
 */
public class DemoSTM32WBActivity extends com.st.BlueSTSDK.gui.DemosActivity{

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param option    parameters to use during the connection
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, ConnectionOption option) {
        Intent i = new Intent(c, DemoSTM32WBActivity.class);
        setIntentParameters(i, node, option);
        return i;
    }//getStartIntent

    /**
     * list of demo to show when connecting to a server node (the remote node)
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment> SERVER_DEMOS[] = new Class[]{
            LedButtonControlFragment.class,
    };

    /**
     * list of demo to show when connecting to a router node (the central node)
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment> ROUTER_DEMOS[] = new Class[]{
           LedButtonNetworkControlFragment.class,
    };


    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends DemoFragment>[] getAllDemos() {
        Node n = getNode();
        if(n.getTypeId() == Peer2PeerDemoConfiguration.WB_ROUTER_NODE_ID){
            return ROUTER_DEMOS;
        }else
            return SERVER_DEMOS;

    }

    @Override
    protected boolean enableFwUploading() {
        return false;
    }
}
