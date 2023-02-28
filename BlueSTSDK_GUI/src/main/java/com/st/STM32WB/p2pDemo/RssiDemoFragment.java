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

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.gui.R;


import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

/**
 * Demo fragment that will periodically (every 2 seconds) request and display the rssi value
 */
public abstract class RssiDemoFragment extends DemoFragment implements Node.BleConnectionParamUpdateListener {

    /**
     * rssi update period, in milliseconds
     */
    private static final long RSSI_UPDATE_PERIOD_MS=2000;

    /**
     * thread where post the rssi update
     */
    private Handler mUpdateRssiRequestQueue;

    /**
     * text view where display the rssi value
     */
    private TextView mRssiText;

    /**
     * task to request the rssi update
     */
    private Runnable mAskNewRssi = new Runnable() {
        @Override
        public void run() {
            Node n = getNode();
            if(n!=null){
                n.readRssi();
                mUpdateRssiRequestQueue.postDelayed(mAskNewRssi,RSSI_UPDATE_PERIOD_MS);
            }
        }//run
    };

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        HandlerThread looper = new HandlerThread(RssiDemoFragment.class.getName()+".UpdateRssi");
        looper.start();
        mUpdateRssiRequestQueue = new Handler(looper.getLooper());
    }

    /**
     * get the text view id where upgrade the rssi value
     * @return view id of a text view
     */
    protected abstract @IdRes int getRssiLabelId();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRssiText = view.findViewById(getRssiLabelId());
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        node.addBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.post(mAskNewRssi);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        node.removeBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.removeCallbacks(mAskNewRssi);

    }

    @Override
    public void onRSSIChanged(@NonNull Node node, final int newRSSIValue) {
        if(isDetached()) //avoid to update the gui if the fragment is detached
            return;
        updateGui(() -> mRssiText.setText(getString(R.string.stm32wb_rssiFormat,newRSSIValue)));
    }//onRSSIChanged

    @Override
    public void onMtuChange(@NonNull Node node, int newMtu) { }
}
