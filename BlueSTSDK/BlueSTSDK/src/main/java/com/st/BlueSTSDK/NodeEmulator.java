/******************************************************************************
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

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Features.emul.FeatureRandomAcceleration;
import com.st.BlueSTSDK.Features.emul.FeatureRandomAccelerationEvent;
import com.st.BlueSTSDK.Features.emul.FeatureRandomActivityRecognition;
import com.st.BlueSTSDK.Features.emul.FeatureRandomBattery;
import com.st.BlueSTSDK.Features.emul.FeatureRandomCarryPosition;
import com.st.BlueSTSDK.Features.emul.FeatureRandomFreeFall;
import com.st.BlueSTSDK.Features.emul.FeatureRandomGyroscope;
import com.st.BlueSTSDK.Features.emul.FeatureRandomHumidity;
import com.st.BlueSTSDK.Features.emul.FeatureRandomLuminosity;
import com.st.BlueSTSDK.Features.emul.FeatureRandomMagnetometer;
import com.st.BlueSTSDK.Features.emul.FeatureRandomMemsGesture;
import com.st.BlueSTSDK.Features.emul.FeatureRandomMemsSensorFusion;
import com.st.BlueSTSDK.Features.emul.FeatureRandomMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.emul.FeatureRandomMicLevel;
import com.st.BlueSTSDK.Features.emul.FeatureRandomPedometer;
import com.st.BlueSTSDK.Features.emul.FeatureRandomPressure;
import com.st.BlueSTSDK.Features.emul.FeatureRandomProximity;
import com.st.BlueSTSDK.Features.emul.FeatureRandomProximityGesture;
import com.st.BlueSTSDK.Features.emul.FeatureRandomTemperature;
import com.st.BlueSTSDK.Features.emul.standardCharacateristics.FeatureRandomHeartRate;
import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.advertise.InvalidBleAdvertiseFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** This class represents a node that don't used a ble connection for extract the data but generate
 * fake data.
 * <p>
 *     The feature exported by this node must implement the interface {@link com.st.BlueSTSDK.NodeEmulator.EmulableFeature}.
 * </p>
 * <p>
 *     The class will generate a new feature value each 1000ms
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 * */
public class NodeEmulator extends Node {

    /**
     * list of class that implement the interface {@link com.st.BlueSTSDK.NodeEmulator.EmulableFeature}
     *  and that will be exported by this node
     */
    @SuppressWarnings("uncheked")
    public final static Class<? extends Feature> DEFAULT_EMULATED_FEATURE[] = new
            Class[]{
            FeatureRandomLuminosity.class,
            FeatureRandomMemsSensorFusion.class,
            FeatureRandomMemsSensorFusionCompact.class,
            FeatureRandomPressure.class,
            FeatureRandomProximity.class,
            FeatureRandomTemperature.class,
            FeatureRandomAcceleration.class,
            FeatureRandomGyroscope.class,
            FeatureRandomHumidity.class,
            FeatureRandomMagnetometer.class,
            FeatureRandomHumidity.class,
            FeatureRandomMicLevel.class,
            FeatureRandomProximityGesture.class,
            FeatureRandomMemsGesture.class,
            FeatureRandomPedometer.class,
            FeatureRandomAccelerationEvent.class,
            FeatureRandomBattery.class,
            FeatureRandomFreeFall.class,
            FeatureRandomActivityRecognition.class,
            FeatureRandomCarryPosition.class,
            FeatureRandomHeartRate.class
    };

    /**
     * object used write/read debug message to/from the ble device,
     * null if the device doesn't export the debug service   */
    private Debug mDebugConsole;

    /**
     * object used write/read Registers  to/from the ble device,
     * null if the device doesn't export the debug service   */
    private ConfigControl mConfigControl;

    /**
     * static value of the Emulator node available
     */
    private static int mEmulatorLastId = 0;

    /**
     * id of the Emulator Node
     */
    private int mEmulatorId;

    /**
     * ms to wait before declare a node as lost
     */
    private static long NODE_LOST_TIMEOUT_MS = 2000;

    /**
     * thread where wait the timeout
     */
    private Handler mHandler;  //NOTE: since the handler is crate in a thread,
    // is better check that the class is not null in the case we try to use it before the thread
    // got exec
    /**
     * task to run when the timeout expire, it will set the node status as lost
     */
    private Runnable mSetNodeLost = new Runnable() {
        @Override
        public void run() {
            if (getState() == State.Idle) {
                updateNodeStatus(State.Lost);
            }//if
        }//run
    };

    /**
     * create a thread where run the handler that will contain the timeout for understand if the
     * node go out of scope
     */
    private void initHandler() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler(); //create the handler
                //put the first item
                mHandler.postDelayed(mSetNodeLost, NODE_LOST_TIMEOUT_MS);
                Looper.loop(); //start exec the item in the queue
            }//run
        }).start(); //run the thread
    }//initHandler

    /**
     * list of all the feature that are available in the advertise
     */
    private ArrayList<Feature> mAvailableFeature;


    /**
     * build the feature exported by this node
     * @param emulFeature list of class that extend feature, we also check that the class
     *                    implements the interface {@link com.st.BlueSTSDK.NodeEmulator.EmulableFeature}
     */
    private void buildAvailableFeatures(Class<? extends Feature> emulFeature[]) {
        mAvailableFeature = new ArrayList<>();

        for (Class<? extends Feature> c : emulFeature) {
            //if c implement EmulableFeature
            if (!EmulableFeature.class.isAssignableFrom(c))
                continue;
            Feature f = buildFeatureFromClass(c);
            f.setEnable(true);
            mAvailableFeature.add(f);
        }//for

    }//buildAvailableFeatures

    /** global timestamp for the node, it will be incremented each time a new data is created */
    private AtomicInteger mTimestamp = new AtomicInteger(0);

    /**
     *
     * @param emulFeature list of feature that this node will emulate. The class must extend
     *                    feature and implement {@link com.st.BlueSTSDK.NodeEmulator.EmulableFeature}
     * @throws InvalidBleAdvertiseFormat this eception is not throw since the advertise is a fake
     * one
     */
    public NodeEmulator(Class<? extends Feature> emulFeature[]) throws InvalidBleAdvertiseFormat {
        //byte{size=5+1,type=vendor_data,data={v1.0,GENERIC board + 4x 0xFF } }
        super(null, 10, new byte[]{0x07, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF});
        initHandler();
        buildAvailableFeatures(emulFeature);
        mEmulatorId = ++mEmulatorLastId;
        mDebugConsole = null;
        mConfigControl = new ConfigControlEmul();
    }

    /**
     * create a emulate node that emulate the feature in the array {@link #DEFAULT_EMULATED_FEATURE}
     * @throws InvalidBleAdvertiseFormat never throw
     */
    public NodeEmulator() throws InvalidBleAdvertiseFormat {
        this(DEFAULT_EMULATED_FEATURE);
    }

    @Override
    public void connect(Context c, boolean unused) {
        updateNodeStatus(State.Connecting);
        updateNodeStatus(State.Connected);
    }//connect


    @Override
    public void disconnect() {
        updateNodeStatus(State.Disconnecting);
        updateNodeStatus(State.Idle);

    }

    @Override
    public Type getType() {
        return Type.GENERIC;
    }//getType

    @Override
    public String getTag() {
        return "Virtual Tag " + String.format("%02X:",mEmulatorId);
    }//getTag

    @Override
    public String getName() {
        return "VirtualNodeName " +mEmulatorId;
    }//getName

    @Override
    public List<Feature> getFeatures() {
        return java.util.Collections.unmodifiableList(mAvailableFeature);
    }

    /**
     * search for a specific feature
     * @param type type of feature that we want search
     * @param <T> class that will want search
     * @return the feature of type {@code type} exported by this node, or null if not present
     */
    @SuppressWarnings("unchecked")
    public @Nullable
    <T extends Feature> T getFeature(Class<T> type){
        for (Feature f : mAvailableFeature) {
            if (type.isInstance(f)) {
                return (T) f;
            }
        }// for list
        return null;
    }//getFeature

    @Override
    public boolean readFeature(Feature feature) {
        if (feature instanceof EmulableFeature) {
            feature.update(mTimestamp.incrementAndGet(), ((EmulableFeature) feature).generateFakeData(), 0);
            return true;
        } else
            return false;
    }//readFeature

    /**
     * each feature enabled for the notification will have its task that will generate new datas
     */
    private Map<Feature, UpdateTask> mNotifyFeature = Collections.synchronizedMap(
            new HashMap<Feature, UpdateTask>());
    /** generate a new data each second */
    final static int NOTIFICATION_DELAY_MS = 1000;

    private class UpdateTask<T extends Feature & EmulableFeature> implements Runnable {

        /** object that will generate the data */
        private T mFeature;

        /** the user can set this variable to false for stop the execution of this task*/
        public volatile boolean mUpdate;

        /** build a new task, it will generate a new data as soon as possible */
        UpdateTask(T f) {
            mFeature = f;
            mUpdate = true;
            mHandler.post(this);
        }

        @Override
        public void run() {
            if (mUpdate){
                mFeature.update(mTimestamp.incrementAndGet(), mFeature.generateFakeData(), 0);
                mHandler.postDelayed(this, NOTIFICATION_DELAY_MS);
            }
        }//run
    }

    @Override
    public boolean disableNotification(Feature feature) {

        UpdateTask task = mNotifyFeature.remove(feature);
        if (task != null) {
            task.mUpdate = false;
            return true;
        }
        return false;
    }//disableNotification

    @Override
    public boolean enableNotification(Feature feature) {
        if (mNotifyFeature.containsKey(feature))
            return false;
        //else
        if (feature instanceof EmulableFeature) {
            mNotifyFeature.put(feature, new UpdateTask(feature));
            return true;
        } else
            return false;
    }//enableNotification

    @Override
    public boolean isEnableNotification(Feature feature) {
        return mNotifyFeature.containsKey(feature);
    }

    @Override
    public int getTxPowerLevel() {
        return 10;
    }//getTxPowerLevel

    @Override
    public void readRssi() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRssi((int) (Math.random() * 100));
            }
        }, NOTIFICATION_DELAY_MS / 2);
    }

    @Override
    void isAlive(int rssi) {
        //remove the set lost task
        if (mHandler != null) mHandler.removeCallbacks(mSetNodeLost);
        updateRssi(rssi);
        //start a new set lost task
        if (mHandler != null) mHandler.postDelayed(mSetNodeLost, NODE_LOST_TIMEOUT_MS);

    }//isAlive

    @Override
    public Debug getDebug() {
        return null;
    }

    @Override
    public ConfigControl getConfigRegister() {
        return mConfigControl;
    }

    @Override
    boolean isBounded(){
        return false;
    }

    /**
     * Interface that the emulated feature have to implement
     */
    public interface EmulableFeature {
        /**
         * generate the feature data
         * @return array of byte with the data that will be parsed as generated by the sensor
         */
        byte[] generateFakeData();
    }

    /**
     * class that emulate the presence of register
     */
    private class ConfigControlEmul extends ConfigControl{
        public ConfigControlEmul()
        {
            super(null, null, null);


            mRegPersistent[0]=258; //Version
            mRegPersistent[1]=258; //Version - copy
            mRegPersistent[2]=0; //LED Config
            mRegPersistent[3]=0x4142; //BLE LOC NAME
            mRegPersistent[4]=0x4344; //BLE LOC NAME
            mRegPersistent[5]=0x4546; //BLE LOC NAME
            mRegPersistent[6]=0x4748; //BLE LOC NAME
            mRegPersistent[7]=0x494A; //BLE LOC NAME
            mRegPersistent[8]=0x4B4C; //BLE LOC NAME
            mRegPersistent[9]=0x4D4E; //BLE LOC NAME
            mRegPersistent[10]=0x4F50; //BLE LOC NAME
            mRegPersistent[11]=0x1123; //BLE PUB ADDRESS
            mRegPersistent[12]=0x2234; //BLE PUB ADDRESS
            mRegPersistent[13]=0x3345; //BLE PUB ADDRESS
            mRegPersistent[14]=0x4456; //BLE PUB ADDRESS
            mRegPersistent[15]=0x5567; //BLE PUB ADDRESS
            mRegPersistent[16]=0x6678; //BLE PUB ADDRESS
            mRegPersistent[17]=0x77AB; //BLE PUB ADDRESS
            mRegPersistent[18]=0x7CDE; //BLE PUB ADDRESS

            for (int i = 19; i < mRegPersistent.length ; i++) {
                mRegPersistent[i] = (short)((Math.random() * 65535));
            }

            System.arraycopy(mRegPersistent, 0, mRegSession, 0, mRegSession.length);

            //Session custom regs
            mRegSession[3] = (short)((Math.random() * 1000)); //Battery level
            mRegSession[4] = (short)((Math.random() * 1000)); //Battery voltage
            mRegSession[5] = (short)((Math.random() * 1000)); //Battery voltage
            mRegSession[6] = (short)((Math.random() * 1000)); //Battery voltage
            mRegSession[7] = (short)((Math.random() * 1000)); //Battery voltage
            mRegSession[8] = (short)((Math.random() * 3)); //PowerManagerStatus

        }

        short [] mRegPersistent = new short[256];
        short [] mRegSession = new short[mRegPersistent.length];

        @Override
        public void read(Command cmd ) {
            Register r =cmd.getRegister();
            Register.Target t = cmd.getTarget();

            if (r.getAccess() == Register.Access.R || r.getAccess() == Register.Access.RW ) {
                byte arr[] = new byte[4+r.getSize() *2];
                System.arraycopy(r.ToReadPacket(t), 0, arr, 0, 4);
                for (int i = 0; i<r.getSize(); i++) {
                    if (t == Register.Target.PERSISTENT) {
                        arr[4 + (2 * i)] = (byte)(((mRegPersistent[r.getAddress() + i])>>8 ) & 0xFF);
                        arr[4 + (2 * i) + 1] = (byte)(((mRegPersistent[r.getAddress() + i]) ) & 0xFF);
                    } else { // Session
                        arr[4 + (2 * i)] = (byte)(((mRegSession[r.getAddress() + i])>>8 ) & 0xFF);
                        arr[4 + (2 * i) + 1] = (byte)(((mRegSession[r.getAddress() + i]) ) & 0xFF);
                    }
                }
                BluetoothGattCharacteristic virtualChar = new BluetoothGattCharacteristic(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID, 0 , 0);
                virtualChar.setValue(arr);
                characteristicsUpdate(virtualChar);
            }
        }

        @Override
        public void write(Command cmd ) {
            Register r =cmd.getRegister();
            Register.Target t = cmd.getTarget();
            if (r.getAccess() == Register.Access.W || r.getAccess() == Register.Access.RW ) {
                byte [] arr = cmd.getData();
                if (arr.length <= r.getSize() * 2 && (arr.length % 2 == 0)) {
                    for (int i = 0;  i < arr.length; i+=2) {
                        //byte[] arr = cmd.getData();
                        short value = (short)(((arr[ i] << 8) +  (arr[ i + 1] & 0xFF)) & 0xFFFF);
                        if (t == Register.Target.PERSISTENT) {
                            mRegPersistent[r.getAddress() + (i >> 1)] = value;
                        } else { // Session
                            mRegSession[r.getAddress() + (i >> 1)] = value;
                        }
                    }
                    BluetoothGattCharacteristic virtualChar = new BluetoothGattCharacteristic(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID, 0 , 0);
                    virtualChar.setValue(r.ToWritePacket(cmd.getTarget(), arr));
                    characteristicsUpdate(virtualChar);
                }
                else
                {
                    byte [] arrNotify = r.ToWritePacket(cmd.getTarget(), arr);
                    arrNotify[2] = 1; //Error specific
                    BluetoothGattCharacteristic virtualChar = new BluetoothGattCharacteristic(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID, 0 , 0);
                    virtualChar.setValue(arrNotify);
                    characteristicsUpdate(virtualChar);
                }
            }//if
        }//write
    }//ConfigControlEmul
}
