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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * shadow object that will intercept the call send to the remote device
 * all the call return a success and will only call the corresponding callback
 */
@Implements(BluetoothGatt.class)
public class BluetoothGattShadow {

    private static long CALLBACK_DELAY_MS=100;

    @RealObject private BluetoothGatt bluetoothGatt;

    /**
     * list of service present in the device
     */
    private List<BluetoothGattService> mServices = new ArrayList<>();

    /**
     * callback object set from the user
     */
    private BluetoothGattCallback userCallBack;

    /**
     * set the call bakc object
     * @param callback
     */
    void setGattCallBack(BluetoothGattCallback callback){
        userCallBack=callback;
    }

    @Implementation
    public void disconnect(){
        if(userCallBack!=null)
            Robolectric.getBackgroundThreadScheduler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userCallBack.onConnectionStateChange(bluetoothGatt,BluetoothGatt.GATT_SUCCESS,
                            BluetoothProfile.STATE_DISCONNECTED);
                }
            },CALLBACK_DELAY_MS);
    }

    @Implementation
    public  boolean connect(){
        if(userCallBack!=null) {
            Robolectric.getBackgroundThreadScheduler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userCallBack.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS,
                            BluetoothProfile.STATE_CONNECTED);
                }
            },CALLBACK_DELAY_MS);
            return true;
        }
        return false;
    }

    @Implementation
    public boolean discoverServices(){
        if(userCallBack!=null) {
            Robolectric.getBackgroundThreadScheduler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userCallBack.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS);
                }},CALLBACK_DELAY_MS);
            return true;
        }else{
            return false;
        }
    }

    public void setServices(List<BluetoothGattService> services){
        mServices = services;
    }

    @Implementation
    public List<BluetoothGattService> getServices(){
        return mServices;
    }

    @Implementation
    public boolean readRemoteRssi(){
        if(userCallBack!=null) {
            userCallBack.onReadRemoteRssi(bluetoothGatt, new Random().nextInt(), BluetoothGatt.GATT_SUCCESS);
            return true;
        }else
            return false;

    }

    /**
     * generate 20byte of random data, this is used as data field for the characteristics read
     * @return array of 20 random bytes
     */
    private byte[] genRandomData(){
        byte fakeData[] = new byte[20];
        new Random().nextBytes(fakeData);
        return fakeData;
    }

    /**
     * generate 20byte of random data and notify it to the user
     * @param characteristic characteristics that we will read
     * @return
     */
    @Implementation
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(userCallBack!=null) {
            characteristic.setValue(genRandomData());
            userCallBack.onCharacteristicRead(bluetoothGatt, characteristic, BluetoothGatt.GATT_SUCCESS);
            return true;
        }else
            return true;

    }


    /**
     * do a characteristic read and notify it with a onCharacteristicChanged
     * @param characteristic
     * @param enable
     * @return
     */
    @Implementation
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                 boolean enable){
        if(userCallBack!=null) {
            if (enable) {
                characteristic.setValue(genRandomData());
                userCallBack.onCharacteristicChanged(bluetoothGatt, characteristic);
            }//if
            return true;
        }
        return false;

    }

    @Implementation
    public  boolean writeDescriptor(BluetoothGattDescriptor desc){
        userCallBack.onDescriptorWrite(bluetoothGatt,desc,BluetoothGatt.GATT_SUCCESS);
        return true;
    }

    @Implementation
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic){
        userCallBack.onCharacteristicWrite(bluetoothGatt, characteristic, BluetoothGatt.GATT_SUCCESS);
        return true;
    }

    @Implementation
    public void close(){

    }


}
