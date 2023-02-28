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

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Utils.BLENodeDefines;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class used for write and read from the debug console
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Debug {

    /**
     * node that will send the data to this class
     */
    private final Node mNode;
    /**
     * characteristics used for send/read stdin/out
     */
    private final BluetoothGattCharacteristic mTermChar;
    /**
     * characteristics used for read the stdErr
     */
    private final BluetoothGattCharacteristic mErrChar;

    /**
     * thread used for call the user listener
     */
    private Handler mNotifyThread;

    /**
     * class where the notify that we receive a new data
     */
    private final CopyOnWriteArrayList<DebugOutputListener> mListener = new CopyOnWriteArrayList<>();

    private static final Charset CHARSET = Charset.forName("ISO-8859-1"); //ASCII

    /**
     * Max size of string to sent in the input char
     */
    public static final int DEFAULT_MAX_STRING_SIZE_TO_SENT = 20;
    private  int mMaxStringSizeToSent= DEFAULT_MAX_STRING_SIZE_TO_SENT;
    private void initHandler(){
        HandlerThread temp = new HandlerThread(Debug.class.getCanonicalName());
        //if you send a lot of data through the debug interface, you need this for avoid delay that
        //can trigger a timeout
        temp.setPriority(Thread.MAX_PRIORITY);
        temp.start();
        mNotifyThread= new Handler(temp.getLooper());
    }//initHandler

    /**
     * @param n          node that will send the data
     * @param termChar   characteristic used for write/notify the stdin/out
     * @param errChar    characteristic used used for notify the stderr
     */
    Debug(Node n, BluetoothGattCharacteristic termChar,
          BluetoothGattCharacteristic errChar) {
        mNode = n;
        mTermChar = termChar;
        mErrChar = errChar;
        initHandler();
    }//Debug

    /**
     * write a message to the stdIn, the message can be split in more ble writes
     * the string will be converted in a byte array using the default charset configuration
     *
     * @param message message to send
     * @return number of char sent in Terminal standard characteristic
     */
    public int write(String message) {
        return write(stringToByte(message));
    }

    public void changeMaxStringSizeToSent(int maxStringSize) {
        Log.i("DebugConsole","changeMaxStringSizeToSent="+maxStringSize);
        mMaxStringSizeToSent = maxStringSize;
    }

    public void setDefaultMaxStringSizeToSent() {
        Log.i("DebugConsole","setDefaultMaxStringSizeToSent");
        mMaxStringSizeToSent= DEFAULT_MAX_STRING_SIZE_TO_SENT;
    }

    /**
     * write an array of byte into the stdIn. the array can be split in more ble write
     *
     * @param data array to write
     * @return number of byte sent
     */
    public int write(byte[] data){
        return write(data,0,data.length);
    }

    /**
     * Write an array of byte into the stdInt, the array can be split in more ble write
     * @param data array to write
     * @param offset offset in the array where start read data
     * @param byteToSend number of byte to send
     * @return number of byte sent
     */
    public int write(byte[] data,int offset, int byteToSend){
        int byteSend=offset;
        //write the message with chunk of mMaxStringSizeToSent bytes
        while((byteToSend-byteSend) > mMaxStringSizeToSent){
            mNode.enqueueCharacteristicsWrite(mTermChar,
                    Arrays.copyOfRange(data,byteSend, byteSend + mMaxStringSizeToSent));
            byteSend+=mMaxStringSizeToSent;
        }//while

        //send the remaining data
        if(byteSend!=byteToSend){
            mNode.enqueueCharacteristicsWrite(mTermChar,
                    Arrays.copyOfRange(data,byteSend,byteToSend));
        }//if
        return byteToSend;
    }

    public void addDebugOutputListener(@Nullable DebugOutputListener listener){
        if(listener==null)
            return;
        mListener.addIfAbsent(listener);
        if(mListener.size()==1){
            mNode.changeNotificationStatus(mTermChar, true);
            mNode.changeNotificationStatus(mErrChar, true);
        }
    }

    public void removeDebugOutputListener(@Nullable DebugOutputListener listener){
        if(listener==null)
            return;
        mListener.remove(listener);
        if(mListener.isEmpty()){
            mNode.changeNotificationStatus(mTermChar, false);
            mNode.changeNotificationStatus(mErrChar, false);
        }
    }

//    /**
//     * set the output listener, only one listener can be set in this class
//     * <p>
//     *     If the listener is null we stop the notification
//     * </p>
//     *
//     * @param listener class with the callback when something appear in the debug console
//     */
//    @Deprecated
//    public void setDebugOutputListener(@NonNull DebugOutputListener listener) {
//        addDebugOutputListener(listener);
//    }

    public static String byteToString(byte[] value){
        return new String(value, CHARSET);
    }

    public static byte[] stringToByte(String str){
        return str.getBytes(CHARSET);
    }

    /**
     * the node had received an update on this characteristics, if it is a debug characteristic we
     * sent its data to the listener
     *
     * @param characteristic characteristic that has been updated
     */
    void receiveCharacteristicsUpdate(final BluetoothGattCharacteristic characteristic) {
        if (mListener.isEmpty())
            return;
        UUID charUuid = characteristic.getUuid();
        final String msg = byteToString(characteristic.getValue());
        if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_STDERR_UUID)) {
           // mListener.onStdErrReceived(Debug.this, characteristic.getStringValue(0));
            mNotifyThread.post(() -> {
            for(DebugOutputListener listener : mListener)
                listener.onStdErrReceived(Debug.this, msg);
            });

        } else if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID)) {
            //mListener.onStdOutReceived(Debug.this, characteristic.getStringValue(0));
            mNotifyThread.post(() -> {
            for(DebugOutputListener listener : mListener)
                listener.onStdOutReceived(Debug.this, msg);
            });
        }//if-else-if
    }//receiveCharacteristicsUpdate

    /**
     * the node had finish to write a characteristics
     *
     * @param characteristic characteristic that has been write
     * @param status         true if the write end correctly, false otherwise
     */
    void receiveCharacteristicsWriteUpdate(final BluetoothGattCharacteristic characteristic,
                                           final byte[] data,
                                           final boolean status) {
        if (mListener.isEmpty())
            return;
        UUID charUuid = characteristic.getUuid();

        if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID)) {
            final String str = byteToString(data);
            if(str.length()>mMaxStringSizeToSent) {
                mNotifyThread.post(() -> {
                final String msg = str.substring(0,mMaxStringSizeToSent);
                for(DebugOutputListener listener : mListener)
                    listener.onStdInSent(Debug.this, msg, status);
                });
            }else {
                mNotifyThread.post(() -> {
                for(DebugOutputListener listener : mListener)
                    listener.onStdInSent(Debug.this, str, status);
                });
            }//if-else
        }//if
    }//receiveCharacteristicsWriteUpdate

    /**
     * get the node that write/listen in this debug console
     *
     * @return get the node that write on this debug console
     */
    public Node getNode() {
        return mNode;
    }//getNode

    /**
     * Interface used for notify to the user the console activity
     * The data received/send from/to the node are encoded with ISO-8859-1 charset.
     * @author STMicroelectronics - Central Labs.
     */
    public interface DebugOutputListener {

        /**
         * a new message appear on the standard output
         *
         * @param debug   object that send the message
         * @param message message that someone write in the debug console
         */
        void onStdOutReceived(@NonNull Debug debug,@NonNull String message);

        /**
         * a new message appear on the standard error
         *
         * @param debug   object that send the message
         * @param message message that someone write in the error console
         */
        void onStdErrReceived(@NonNull Debug debug,@NonNull String message);

        /**
         * call when a message is send to the debug console
         *
         * @param debug       object that received the message
         * @param message     message that someone write in the debug console
         * @param writeResult true if the message is correctly send
         */
        void onStdInSent(@NonNull Debug debug,@NonNull String message, boolean writeResult);

    }//DebugOutputListener

}//Debug
