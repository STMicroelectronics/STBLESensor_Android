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
package com.st.BlueSTSDK.Utils;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;

import java.util.List;

/**
 * Extend this activity if you need to start a device scan using the {@link Manager}.
 * This class will check that the user has the bluetooth enabled and for api &gt; 23 it check that
 * the user granted the location and enable the location service
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class NodeScanActivity extends AppCompatActivity {
    private final static String SCAN_TIMEOUT = NodeScanActivity.class.getCanonicalName() + "" +
            ".SCAN_TIMEOUT";


    /**
     * class used for manage the ble adapter and that will keep the list of the discovered device
     */
    protected Manager mManager;

    private BlePermissionHelper mPermissionHelper;

    /**
     * !=0 if we have a start scanning request pending
     */
    private int mLastTimeOut=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManager = Manager.getSharedInstance();

        mPermissionHelper = new BlePermissionHelper(this);
    }//onCreate


    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SCAN_TIMEOUT, mLastTimeOut);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLastTimeOut=savedInstanceState.getInt(SCAN_TIMEOUT, 0);
    }

    /**
     * build a list of object used to filter the node to display in the activity
     * @return a filter to show the node with the advertise format defined by the BlueSTSDK specs
     */
    protected List<AdvertiseFilter> buildAdvertiseFilter(){
        return Manager.buildDefaultAdvertiseList();
    }

    /**
     * method start a discovery and update the gui for the new state
     * @param timeoutMs time to wait before stop the discovery
     */
    public void startNodeDiscovery(final int timeoutMs) {
        mLastTimeOut=timeoutMs;
        if(mPermissionHelper.checkAdapterAndPermission()) {
            mManager.startDiscovery(mLastTimeOut, buildAdvertiseFilter());
            mLastTimeOut = 0;
        }
    }

    /**
     * method that stop the discovery and update the gui state
     */
    public void stopNodeDiscovery() {
        mManager.stopDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }//onRequestPermissionsResult
}

