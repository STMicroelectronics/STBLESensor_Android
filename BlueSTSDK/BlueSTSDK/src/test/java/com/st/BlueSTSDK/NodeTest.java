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

package com.st.BlueSTSDK;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.SparseArray;

import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.TestUtil.BluetoothDeviceShadow;
import com.st.BlueSTSDK.TestUtil.BluetoothGattShadow;
import com.st.BlueSTSDK.TestUtil.MyTestRunner;
import com.st.BlueSTSDK.TestUtil.TestUtil;
import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.advertise.InvalidBleAdvertiseFormat;
import com.st.BlueSTSDK.Utils.InvalidFeatureBitMaskException;

import junit.framework.Assert;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class,manifest = "src/main/AndroidManifest.xml", sdk = 23,
    shadows = {BluetoothGattShadow.class, BluetoothDeviceShadow.class})
public class NodeTest {

    private Node createNode(BluetoothDevice device){

        try {
            return  new Node(device, 10,
                    new byte[]{0x07, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte)
                    0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}
            );
        } catch (InvalidBleAdvertiseFormat invalidBleAdvertiseFormat) {
            Assert.fail("Impossible create a node");
            return null;
        }

    }

    private BluetoothGattCharacteristic createReadNotifyChar(UUID uuid){
        BluetoothGattCharacteristic temp = new BluetoothGattCharacteristic(uuid,
                BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ|
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        temp.addDescriptor(new BluetoothGattDescriptor(UUID.fromString
                ("00002902-0000-1000-8000-00805f9b34fb"),
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        return temp;
    }

    @Test
    public void testUpdateRssi(){

        Node.BleConnectionParamUpdateListener listener = mock(Node
                .BleConnectionParamUpdateListener.class);

        Node node = createNode(null);

        node.addBleConnectionParamListener(listener);

        int rssi = new Random().nextInt();

        node.updateRssi(rssi);

        TestUtil.execAllAsyncTask();

        verify(listener).onRSSIChanged(node, rssi);
    }


    @Test
    public void connectEmptyNode(){

        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        Node node = createNode(device);

        Assert.assertEquals(Node.State.Idle, node.getState());
        node.connect(RuntimeEnvironment.application);

        TestUtil.execAllAsyncTask();

        verify(device).connectGatt(eq(RuntimeEnvironment.application), eq(false),
                any(BluetoothGattCallback.class));
        Assert.assertEquals(Node.State.Dead, node.getState());
    }

    @Test
    public void connectNodeWithDebug(){

        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        BluetoothDeviceShadow shadowDevice = Shadow.extract(device);

        BluetoothGattService debugService = new BluetoothGattService(BLENodeDefines.Services
                .Debug.DEBUG_SERVICE_UUID,BluetoothGattService.SERVICE_TYPE_PRIMARY);
        debugService.addCharacteristic(
                new BluetoothGattCharacteristic(BLENodeDefines.Services.Debug.DEBUG_STDERR_UUID,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                                .PROPERTY_NOTIFY));
        debugService.addCharacteristic(
                new BluetoothGattCharacteristic(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                                .PROPERTY_NOTIFY)
        );

        shadowDevice.addService(debugService);

        Node node = createNode(device);
        Assert.assertEquals(Node.State.Idle, node.getState());
        node.connect(RuntimeEnvironment.application);

        TestUtil.execAllAsyncTask();

        verify(device).connectGatt(eq(RuntimeEnvironment.application), eq(false),
                any(BluetoothGattCallback.class));
        Assert.assertEquals(Node.State.Connected, node.getState());
        Assert.assertTrue(node.getDebug()!=null);
    }

    public static class FakeFeature extends Feature{

        public FakeFeature(Node n) {
            super(FakeFeature.class.getName(), n, new Field[]{});
        }

        @Override
        public boolean isEnabled(){
            return true;
        }

        @Override
        protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
            return new ExtractResult(new Sample(timestamp,new Number[]{}, new Field[]{}),0);
        }

        public void execAllTask(){
            try {
                sThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void enableDisableNotificationForFeature(){
        SparseArray <Class <? extends Feature> > temp = new SparseArray<>();
        temp.append(0x01,FakeFeature.class);
        try {
            Manager.addFeatureToNode((byte)0x00,temp);
        } catch (InvalidFeatureBitMaskException e) {
            Assert.fail("Impossible add the FakeFeature");
        }

        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        BluetoothDeviceShadow shadowDevice = Shadow.extract(device);
        BluetoothGattService dataService = new BluetoothGattService(
                UUID.randomUUID(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        dataService.addCharacteristic(createReadNotifyChar(
                UUID.fromString("000000001-"+BLENodeDefines.FeatureCharacteristics.BASE_FEATURE_COMMON_UUID)
        ));
        shadowDevice.addService(dataService);
        Node n = createNode(device);
        n.connect(RuntimeEnvironment.application);

        TestUtil.execAllAsyncTask();

        Feature f = n.getFeature(FakeFeature.class);
        Assert.assertFalse(n.isEnableNotification(f));
        Assert.assertTrue(n.enableNotification(f));
        Assert.assertTrue(n.isEnableNotification(f));
        Assert.assertTrue(n.disableNotification(f));
        Assert.assertFalse(n.isEnableNotification(f));
    }
/*
    @Test
    public void readFeature(){
        SparseArray <Class <? extends Feature> > temp = new SparseArray<>();
        temp.append(0x01,FakeFeature.class);
        try {
            Manager.addFeatureToNode((byte)0x00,temp);
        } catch (InvalidFeatureBitMaskException e) {
            Assert.fail("Impossible add the FakeFeature");
        }

        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        BluetoothDeviceShadow shadowDevice = Shadow.extract(device);
        BluetoothGattService dataService = new BluetoothGattService(
                UUID.randomUUID(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic dataChar = createReadNotifyChar(
                UUID.fromString("000000001-" + BLENodeDefines.FeatureCharacteristics.BASE_FEATURE_COMMON_UUID)
        );
        dataService.addCharacteristic(dataChar);
        shadowDevice.addService(dataService);
        Node n = createNode(device);
        n.connect(RuntimeEnvironment.application);

        TestUtil.execAllAsyncTask();

        BluetoothGatt gatt = shadowDevice.getGattConnection();
        Feature.FeatureListener emptyListener = mock(Feature.FeatureListener.class);
        FakeFeature f = n.getFeature(FakeFeature.class);
        Assert.assertTrue(f!=null);
        f.addFeatureListener(emptyListener);
        n.readFeature(f);

        TestUtil.execAllAsyncTask();
        f.execAllTask();

        verify(gatt).readCharacteristic(dataChar);
        verify(emptyListener).onUpdate(eq(f), any(Feature.Sample.class));
    }
*/
    @Test
    public void updateFeature(){
        SparseArray <Class <? extends Feature> > temp = new SparseArray<>();
        temp.append(0x01,FakeFeature.class);
        try {
            Manager.addFeatureToNode((byte)0x00,temp);
        } catch (InvalidFeatureBitMaskException e) {
            Assert.fail("Impossible add the FakeFeature");
        }

        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        BluetoothDeviceShadow shadowDevice = Shadow.extract(device);
        BluetoothGattService dataService = new BluetoothGattService(
                UUID.randomUUID(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic dataChar = createReadNotifyChar(
                UUID.fromString("000000001-" + BLENodeDefines.FeatureCharacteristics.BASE_FEATURE_COMMON_UUID)
        );
        dataService.addCharacteristic(dataChar);
        shadowDevice.addService(dataService);
        Node n = createNode(device);
        n.connect(RuntimeEnvironment.application);

        TestUtil.execAllAsyncTask();

        BluetoothGatt gatt = shadowDevice.getGattConnection();
        Feature.FeatureListener emptyListener = mock(Feature.FeatureListener.class);
        FakeFeature f = n.getFeature(FakeFeature.class);
        Assert.assertTrue(f != null);
        f.addFeatureListener(emptyListener);
        Assert.assertTrue(n.enableNotification(f));

        TestUtil.execAllAsyncTask();
        f.execAllTask();

        verify(gatt).setCharacteristicNotification(dataChar, true);
        verify(emptyListener).onUpdate(eq(f), any(Feature.Sample.class));

        Assert.assertTrue(n.disableNotification(f));
        verify(gatt).setCharacteristicNotification(dataChar, false);
    }

/*

    @Test
    public void disconnect(){
        BluetoothDevice device = spy(Shadow.newInstanceOf(BluetoothDevice.class));
        BluetoothDeviceShadow shadowDevice = (BluetoothDeviceShadow)ShadowExtractor.extract(device);

        BluetoothGattService dataService = new BluetoothGattService(
                UUID.fromString("00000000-0001-11e1-9ab4-0002a5d5c51b"),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        dataService.addCharacteristic(createReadNotifyChar(
                UUID.fromString("000000001-" + BLENodeDefines.FeatureCharacteristics.BASE_COMMON_FEATURE_UUID)
        ));
        shadowDevice.addService(dataService);
        Node node = createNode(device);
        Assert.assertEquals(Node.State.Idle, node.getState());
        node.connect(RuntimeEnvironment.application);

        BluetoothGatt gatt = shadowDevice.getGattConnection();

        TestUtil.execAllAsyncTask();

        Assert.assertEquals(Node.State.Connected, node.getState());
        node.disconnect();

        TestUtil.execAllAsyncTask();

        verify(gatt,timeout(10000)).disconnect();
        verify(gatt,timeout(10000)).close();

        Assert.assertEquals(Node.State.Idle, node.getState());

    }
*/

    @Test
    public void getProtocolVersion(){
        Assert.assertEquals(1,createNode(null).getProtocolVersion());
    }
}
