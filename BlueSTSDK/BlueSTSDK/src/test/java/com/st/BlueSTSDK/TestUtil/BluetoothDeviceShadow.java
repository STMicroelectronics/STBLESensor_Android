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

package com.st.BlueSTSDK.TestUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.Nullable;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.spy;

/***
 * class that shadow the BleuthoothDevice class, and permit to create a fake gatt connection
 * and set a ble service that the client will see
 */
@Implements(BluetoothDevice.class)
public class BluetoothDeviceShadow extends ShadowBluetoothDevice {

    /**
     * list of services availabe in this fake device*/
    private List<BluetoothGattService> mServices = new ArrayList<>();

    /** connection open with the remote device*/
    private BluetoothGatt mGattConnection;


    /***
     * add a service to the fake device
     * @param newService service that the device will export
     */
    public void addService(BluetoothGattService newService){
        mServices.add(newService);
    }

    /**
     * return the connection that we have open with this device, the object is spy with the
     * mockito framework
     * @return connection with the device or null if not present
     */
    public @Nullable BluetoothGatt getGattConnection(){
        return mGattConnection;
    }

    /**
     * open a connection with the device the returning connection is a shadow object that can be
     * queue with the mockito framework
     * @param c
     * @param b
     * @param callback
     * @return
     */
    @Implementation
    public BluetoothGatt connectGatt(Context c,boolean b,BluetoothGattCallback callback){
        mGattConnection = spy(Shadow.newInstanceOf(BluetoothGatt.class));
        BluetoothGattShadow shadowGatt = Shadow.extract(mGattConnection);
        shadowGatt.setGattCallBack(callback);
        shadowGatt.setServices(mServices);
        mGattConnection.connect();
        return mGattConnection;
    }


}
