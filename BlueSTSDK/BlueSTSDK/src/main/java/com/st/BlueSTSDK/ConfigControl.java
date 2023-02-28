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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that manage the Config characteristics
 * <p>
 * The class provides the read and write functions
 * to access the registers throws the {@link com.st.BlueSTSDK.Config.Command} class
 * </p>
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class ConfigControl {

    public interface ConfigControlListener {
        void onRegisterReadResult(ConfigControl control,Command cmd, int error);
        void onRegisterWriteResult(ConfigControl control,Command cmd, int error);
        void onRequestResult(ConfigControl control,Command cmd, boolean success);
    }

    /**
     * pool of thread used for notify to the listeners that the feature have new data
     */
    protected static final ExecutorService sThreadPool = Executors.newCachedThreadPool();

    /**
     * node that will send the data to this class
     */
    private final Node mNode;
    /**
     * characteristics used for send/read Config register
     */
    private final BluetoothGattCharacteristic mRegChar;
    /**
     * connection that can be used for send data to the remote device
     */
    private final BluetoothGatt mConnection;
    /**
     * list of listener for the feature change.
     * <p> is a thread safe list, so a listener can subscribe itself from a callback </p>
     * @see com.st.BlueSTSDK.Feature.FeatureListener
     */
    protected final CopyOnWriteArrayList<ConfigControlListener> mConfigControlListener = new CopyOnWriteArrayList<>();

    /**
     *
     * @param node node that will be configurate
     * @param regChar characteristics used for access to the register
     * @param connection connection with the node
     */
    public ConfigControl(Node node, BluetoothGattCharacteristic regChar, BluetoothGatt connection) {
        this.mNode = node;
        this.mRegChar = regChar;
        this.mConnection = connection;
    }

    /**
     * add a new listener for the update of this feature
     *
     * @param listener listener class
     */
    public void addConfigListener(ConfigControlListener listener) {

        if (mConfigControlListener.isEmpty()) {
            if (mNode != null && mRegChar != null)
                mNode.changeNotificationStatus(mRegChar, true);
        }
        if (listener != null)
            mConfigControlListener.addIfAbsent(listener);

    }//addFeatureListener

    /**
     * remove a listener for the update of this feature
     *
     * @param listener listener to remove
     */
    public void removeConfigListener(ConfigControlListener listener) {
        mConfigControlListener.remove(listener);

        if (mConfigControlListener.isEmpty()) {
            if (mNode != null && mRegChar != null)
                mNode.changeNotificationStatus(mRegChar, false);
        }
    }

    /**
     * call the method onRegisterReadResult or  onRegisterWriteResult for
     * each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     * <p>
     * if you extend the method update you have to call this method after that you update the data
     * </p>
     * @param characteristic byte read from the register
     */
    void characteristicsUpdate(BluetoothGattCharacteristic characteristic) {
        byte [] dataReg = characteristic.getValue();
        if(dataReg==null)
            return;
        final Command cmd = new Command( dataReg);
        final boolean readOperation = Register.isReadOperation(dataReg);
        final int error = Register.getError(dataReg);

        for (final ConfigControlListener listener : mConfigControlListener) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (readOperation)
                        listener.onRegisterReadResult(ConfigControl.this,cmd, error);
                    else
                        listener.onRegisterWriteResult(ConfigControl.this,cmd, error);
                }//run
            });
        }//for
    }//characteristicsUpdate

    /**
     * call the method onRequestResult for
     * each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     * <p>
     * if you extend the method update you have to call this method after that you update the data
     * </p>
     * @param characteristic that contains the data command sent to the device
     * @param success true if the wrote command is send correctly
     */
     void characteristicsWriteUpdate(BluetoothGattCharacteristic characteristic,byte dataReg[], final boolean success) {
         if(dataReg==null)
             return;
        final Command cmd = new Command(dataReg);
        for (final ConfigControlListener listener : mConfigControlListener) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onRequestResult(ConfigControl.this,cmd, success);
                }//run
            });
        }//for
    }//characteristicsWriteUpdate

    /**
     *
     * @param cmd read command
     */
    public void read(Command cmd ) {
        if (mRegChar != null && mConnection != null && cmd!=null) {
            mNode.enqueueCharacteristicsWrite(mRegChar,cmd.ToReadPacket());
        }
    }

    /**
     *
     * @param cmd write command
     */
    public void write(Command cmd ) {
        if (mRegChar != null && mConnection != null && cmd!=null) {
            mNode.enqueueCharacteristicsWrite(mRegChar,cmd.ToWritePacket());
        }
    }
}
