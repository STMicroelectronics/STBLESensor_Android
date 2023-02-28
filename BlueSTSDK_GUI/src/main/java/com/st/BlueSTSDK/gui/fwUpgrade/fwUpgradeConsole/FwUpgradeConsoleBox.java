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
package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.STM32Crc32;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.Checksum;

/**
 * Implement the FwUpgradeConsole for a board running the BlueMs firmware.
 * In this case the protocol is:
 * mobile:upgrade[Ble|Fw]+length+fileCrc
 * node:fileCrc
 * mobile: file data, the file is spited in message of 16bytes
 * node: when all the byte are write return 1 if the crc is ok, -1 otherwise
 */
public class FwUpgradeConsoleBox extends FwUpgradeConsole {

    /**
     *to avoid to stress the BLE Stack the message are send each 13ms that corrisponding to a connection
     * inteval of 12.5 ms.
     */
    private static final int FW_PACKAGE_DELAY_MS = 13; // connection interval 12.5
    //every time there is a fail we decease the number of block to send

    static private final byte[] UPLOAD_BOARD_FW={'u','p','g','r','a','d','e','F','w'};
    static private final byte[] UPLOAD_BLE_FW={'u','p','g','r','a','d','e','B','l','e'};

    static private final String ACK_MSG="\u0001";

    /**
     * the Stm32 L4 can write only 8bytes at time, so sending a multiple of 8 simplify the fw code
     */
    static private final int MAX_MSG_SIZE=16;

    /**
     * if all the messages are not send in 1s an error is fired
     */
    static private final int LOST_MSG_TIMEOUT_MS=1000;

    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    /**
     * console where send the command
     */
    private Debug mConsole;

    /**
     * class that manage the file upload
     */
    private class UploadFileProtocol implements  Debug.DebugOutputListener{

        /**
         * since the traffic is high, we use a bigger timeout for give time to the system to notify
         * that sent a message
         */
        static private final int FW_UPLOAD_MSG_TIMEOUT_MS=4*LOST_MSG_TIMEOUT_MS;

        /**
         * file that we are uploading
         */
        private FwFileDescriptor mFile;

        /**
         * buffer where we are reading the file
         */
        private byte[] mFileData;

        /**
         * number of byte send to the node
         */
        private long mByteSend;

        /**
         * total number of byte to send
         */
        private long mByteToSend;

        /**
         * file crc
         */
        private long mCrc;

        /**
         * true if the handshake with the node is finished
         */
        private boolean mNodeReadyToReceiveFile;

        /**
         * counter of package that are sent
         */
        private long mNPackageReceived;

        private long nSentPackage;

        /**
         * size of the last package send
         */
        private byte[] mLastPackageSend = new byte[MAX_MSG_SIZE+4];

        static private final int NOTIFY_EACH_PACKAGE = 10;

        private Runnable mNextPackageSentTask;

        /**
         * if the timeout is rise, fire an error of type
         * {@link FwUpgradeCallback#ERROR_TRANSMISSION}
         */
        private Runnable onTimeout = () -> {
            onLoadFail(FwUpgradeCallback.ERROR_TRANSMISSION);
            if(mNextPackageSentTask!=null){
                mTimeout.removeCallbacks(mNextPackageSentTask);
            }
        };


        /**
         * Notify to the used that an error happen
         * @param errorCode type of error
         */
        private void onLoadFail(@FwUpgradeCallback.UpgradeErrorType int errorCode){
            if(mCallback!=null)
                mCallback.onLoadFwError(FwUpgradeConsoleBox.this,mFile,errorCode);
            setConsoleListener(null);
        }

        /**
         * notify to the user that the upload is correctly finished
         */
        private void onLoadComplete(){
            if(mCallback!=null)
                mCallback.onLoadFwComplete(FwUpgradeConsoleBox.this,mFile);
            setConsoleListener(null);
        }

        private long computeCrc32(FwFileDescriptor file) throws FileNotFoundException {
            Checksum crc = new STM32Crc32();
            byte[] buffer = new byte[4];
            BufferedInputStream inputStream = new BufferedInputStream(file.openFile());
            //the file must be multiple of 32bit,
            long fileSize = file.getLength() - file.getLength()%4;
            try {
                for(long i=0;i<fileSize;i+=4){
                    if(inputStream.read(buffer)==buffer.length)
                        crc.update(buffer,0,buffer.length);
                }//for i
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return crc.getValue();
        }

        /**
         * merge the file size and crc for create the command that will start the upload on the
         * board
         * @param fwType firmware to update
         * @param fileSize number of file to send
         * @param fileCrc file crc
         * @return command to send to the board
         */
        private byte[] prepareLoadCommand(@FirmwareType int fwType, long fileSize, long
                fileCrc){
            byte[] command;
            int offset;
            if(fwType==FirmwareType.BLE_FW){
                offset = UPLOAD_BLE_FW.length;
                command =new byte[offset+8];
                System.arraycopy(UPLOAD_BLE_FW,0,command,0,offset);
            }else{
                offset = UPLOAD_BOARD_FW.length;
                command =new byte[offset+8];
                System.arraycopy(UPLOAD_BOARD_FW,0,command,0,offset);
            }
            byte[] temp = NumberConversion.LittleEndian.uint32ToBytes(fileSize);
            System.arraycopy(temp,0,command,offset,temp.length);
            offset+=temp.length;
            temp = NumberConversion.LittleEndian.uint32ToBytes(fileCrc);
            System.arraycopy(temp,0,command,offset,temp.length);

            return command;
        }

        /**
         * start to upload the file
         * @param fwType firmware that we are uploading
         * @param file file to upload
         */
        void loadFile(@FirmwareType int fwType,FwFileDescriptor file){

            mFile=file;
            mNodeReadyToReceiveFile =false;
            mByteToSend = file.getLength();
            nSentPackage = 0;
            try {
                mCrc = computeCrc32(file);
                InputStream fileStream = file.openFile();
                mFileData = new byte[(int)mByteToSend];
                if(mByteToSend!= fileStream.read(mFileData)){
                    onLoadFail(FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                }
            } catch (FileNotFoundException e) {
                onLoadFail(FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                return;
            }catch (IOException e){
                onLoadFail(FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                return;
            }

            mConsole.write(prepareLoadCommand(fwType,mByteToSend,mCrc));
        }

        /**
         * @param message message received from the node
         * @return true if the message contain the crc code that we have send
         */
        private boolean checkCrc(String message){
            byte[] rcvCrc = Debug.stringToByte(message);
            byte[] myCrc = NumberConversion.LittleEndian.uint32ToBytes(mCrc);
            return Arrays.equals(rcvCrc,myCrc);
        }

        boolean firtTime = true;

        /**
         * read the data from the file and send it to the node
         * @return true if the package is correctly sent
         */
        private void sendFwPackage(){
            mByteSend = nSentPackage * MAX_MSG_SIZE;
            int lastPackageSize = (int) Math.min(mByteToSend - mByteSend, MAX_MSG_SIZE);

            Log.d("fwUpgrade", "send: "+nSentPackage + " size: "+lastPackageSize + "sent: "+mByteSend+"/"+mByteToSend );
            if(lastPackageSize<0){
                return;

            }
            System.arraycopy(mFileData,(int)mByteSend,mLastPackageSend,0,lastPackageSize);

            byte[] packageId = NumberConversion.LittleEndian.uint32ToBytes(nSentPackage);
            System.arraycopy(packageId,0,mLastPackageSend,lastPackageSize,packageId.length);
            lastPackageSize += packageId.length;
            nSentPackage += 1;
            mConsole.write(mLastPackageSend, 0, lastPackageSize);

        }//sendFwPackage

        private boolean transferIsComplete(){
            return (mByteToSend-mByteSend) == 0;
        }

        /**
         * send a block of message, the function will stop at the first error
         */
        private void sendPackageBlock(){
            sendFwPackage();
            final long nPackageSent2 = mNPackageReceived;
            mNextPackageSentTask = () -> {
                if(transferIsComplete())
                    return;
                //if the message was sent
                if (mNPackageReceived > nPackageSent2) {
                    sendPackageBlock(); //send the next one
                } else { // wait a bit an try again
                    mTimeout.postDelayed(mNextPackageSentTask, FW_PACKAGE_DELAY_MS);
                }
            };
            mTimeout.postDelayed(mNextPackageSentTask, FW_PACKAGE_DELAY_MS);

        }//sendPackageBlock


        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {
            if(!mNodeReadyToReceiveFile){
                if(checkCrc(message)) {
                    mNodeReadyToReceiveFile = true;
                    mNPackageReceived=0;
                    //wait update of the connection interval
                    mTimeout.postDelayed(this::sendPackageBlock,500);

                }else
                    onLoadFail(FwUpgradeCallback.ERROR_TRANSMISSION);
            }else { //transfer complete
                mTimeout.removeCallbacks(onTimeout);
                if(message.equalsIgnoreCase(ACK_MSG))
                    onLoadComplete();
                else
                    onLoadFail(FwUpgradeCallback.ERROR_CORRUPTED_FILE);
            }
        }//onStdOutReceived

        /**
         * notify to the user that a block of data is correctly send and send a new one
         */
        private void notifyNodeReceivedFwMessage(){
            mNPackageReceived++;
            //if we finish to send all the message
            if(mNPackageReceived % NOTIFY_EACH_PACKAGE ==0){
                if(mCallback!=null)
                    mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleBox.this,mFile,
                            mByteToSend-mByteSend);
            }//if
        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {
            if(writeResult){
                if(mNodeReadyToReceiveFile){
                    //reset the timeout
                    mTimeout.removeCallbacks(onTimeout);
                    notifyNodeReceivedFwMessage();
                    mTimeout.postDelayed(onTimeout,FW_UPLOAD_MSG_TIMEOUT_MS);
                }
            }else{
                onLoadFail(FwUpgradeCallback.ERROR_TRANSMISSION);
            }
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) {
            byte[] msgData = Debug.stringToByte(message);
            if( msgData[0]!= 0x01){
                return;
            }
            mTimeout.removeCallbacks(onTimeout);
            final  long requestPackage = NumberConversion.LittleEndian.bytesToUInt32(msgData,1);
            Log.d("fwUpgrade","request: "+requestPackage);
            mTimeout.post(() ->{
                Log.d("fwUpgrade","Set request: "+requestPackage);
                nSentPackage = requestPackage+1;

            });


        }
    }

    /**
     * object used for manage the get board id command
     */
    private UploadFileProtocol mConsoleUpgradeFw = new UploadFileProtocol();

    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    /**
     * build a debug console without a callback
     * @param console console to use for send the command
     */
    FwUpgradeConsoleBox(Debug console){
        this(console,null);
    }

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    private FwUpgradeConsoleBox(Debug console, FwUpgradeCallback callback) {
        super(callback);
        mConsole = console;
        mTimeout = new Handler(Looper.getMainLooper());
        mBuffer = new StringBuilder();

    }

    /**
     * change the listener to use for receive the debug console message, null will close the
     * debug console
     *
     * @param listener object to use for notify the console messages
     */
    private void setConsoleListener(Debug.DebugOutputListener listener) {
        synchronized (this) {
            mConsole.removeDebugOutputListener(mCurrentListener);
            mConsole.addDebugOutputListener(listener);
            mCurrentListener = listener;
        }//synchronized
    }

    private boolean isWaitingAnswer() {
        return mCurrentListener != null;
    }


    /**
     *
     * @param fwType type of the firmware to load, only board fw is supported
     * @param fwFile file path
     * @param startingAddress not used the firmware will always be loaded in the address 0x0804000
     * @return true if the upload starts correctly
     */
    @Override
    public boolean loadFw(@FirmwareType int fwType,final FwFileDescriptor fwFile, long startingAddress) {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer

        setConsoleListener(mConsoleUpgradeFw);
        mConsoleUpgradeFw.loadFile(fwType,fwFile);
        return  true;
    }
}
