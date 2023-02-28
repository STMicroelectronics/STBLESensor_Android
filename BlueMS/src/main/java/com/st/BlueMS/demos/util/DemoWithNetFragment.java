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

package com.st.BlueMS.demos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * DemoFragment that will notify if the mobile has an internet connection
 */
public abstract class DemoWithNetFragment extends BaseDemoFragment {

    /**
     * tell if the mobile has an internet connection
     * @return true if the system has an internet connection
     */
    protected boolean isOnline() {
        Context ctx = getActivity();
        if(ctx==null)
            return false;
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        return connMgr != null &&
                connMgr.getActiveNetworkInfo() != null &&
                connMgr.getActiveNetworkInfo().isAvailable() &&
                connMgr.getActiveNetworkInfo().isConnected();
    }

    private class OnConnectionStatusChange extends  BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
                onSystemLostConnectivity();
            }else{
                onSystemHasConnectivity();
            }
        }//onReceive
    }
    private BroadcastReceiver mUpdateConnectionStatus=null;


    @Override
    public void onResume() {
        super.onResume();
        if(mUpdateConnectionStatus==null)
            mUpdateConnectionStatus = new OnConnectionStatusChange();
        getActivity().registerReceiver(mUpdateConnectionStatus, new IntentFilter(android.net
                .ConnectivityManager.CONNECTIVITY_ACTION));
        if(isOnline())
            onSystemHasConnectivity();
        else
            onSystemLostConnectivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        Context c = getActivity();
        if(mUpdateConnectionStatus!=null && c!=null)
            c.unregisterReceiver(mUpdateConnectionStatus);
        mUpdateConnectionStatus=null;
    }

    /**
     * method called when the system has internet connection
     */
    protected void onSystemHasConnectivity(){}

    /**
     * method called when the system lost its internet connection
     */
    protected void onSystemLostConnectivity(){}

}
