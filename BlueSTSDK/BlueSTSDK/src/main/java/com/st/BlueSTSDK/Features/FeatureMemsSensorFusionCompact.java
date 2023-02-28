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
package com.st.BlueSTSDK.Features;

import android.os.Handler;
import android.os.HandlerThread;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Date;

/**
 * This feature manage the quaternion data in the compact way, in this package the quaternion
 * component are encoded using a int16 with  5 decimal values, and sent in group of 2-4 quaternions
 * <p>
 *     the quaternion are normalized, so the sum of the square is 1
 *     we assume to receive one package each <code>QUATERNION_DELAY_MS</code>,
 *     to the user we will notify one quaternion each <code>QUATERNION_DELAY_MS</code>/#quaternion
 * </p>
 * <p>The AutoConfigure process will acquire the magnetometer data for calibrate the magnetometer
 * sensors </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureMemsSensorFusionCompact extends FeatureMemsSensorFusion {

    public static final String FEATURE_NAME = "MEMS Sensor Fusion (Compact)";

    /**
     * since the package will contain more than one quaternion, the class assume that it will
     * arrive a package each <code>QUATERNION_DELAY_MS</code> ms and it will send out the quaternion each
     * <code>QUATERNION_DELAY_MS</code>/#quaternion
     */
    public static final long QUATERNION_DELAY_MS = 30;
    /**
     * the data have 5 decimal position, we divide all by this factor
     */
    protected static final float SCALE_FACTOR = 10000.0f;

    /** thread used for send out the notification of the new quaternion */
    private Handler mDelayNotifier;

    /**
     * start a thread and attach to it an Handler
     */
    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread(FEATURE_NAME);
        handlerThread.start();
        mDelayNotifier = new Handler(handlerThread.getLooper());
    }//initHandler

    /**
     * build a MemsSensorFusionCompact feature
     * @param node node that will send data to this feature
     */
    public FeatureMemsSensorFusionCompact(Node node) {
        super(FEATURE_NAME, node);
        initHandler();
    }//FeatureMemsSensorFusionCompact


    /**
     * Extract the quaternion from the raw data
     * <p>
     *     This method will consume all available data reading a quaternion each 6 bytes
     * </p>
     * @param data array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return the object contains only the number of read bytes since the data are manage
     * directly by this class
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp,byte[] data, int dataOffset) {
        if (data.length - dataOffset < 6)
            throw new IllegalArgumentException("There are less than 6 bytes available to read");

        int nQuat = (data.length - dataOffset) / 6;
        long quatDelay = QUATERNION_DELAY_MS / nQuat;
        final byte parsedData[] = java.util.Arrays.copyOfRange(data, dataOffset,
                dataOffset + 6*nQuat);
        for (int i = 0; i < nQuat; i++) {
            float qi = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 0) / SCALE_FACTOR;
            float qj = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2) / SCALE_FACTOR;
            float qk = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4) / SCALE_FACTOR;
            if(i==0)
                notifySample(timestamp,qi,qj,qk, getQs(qi, qj, qk),parsedData);
            else
                mDelayNotifier.postDelayed(new DelayUpdate(timestamp, qi, qj, qk, getQs(qi, qj,
                        qk)), i * quatDelay);
            dataOffset += 6;
        }//for
        return new ExtractResult(null,nQuat * 6);

    }

    /**
     * update the feature with the new data
     * @param timeStamp message timestamp
     * @param data message data payload
     * @param dataOffset offset where start to read the data
     * @return number of read byte
     */
    @Override
    protected int update_priv(long timeStamp, byte[] data, int dataOffset) {
        /*this function is like the Feature ones but doesn't notify the data since all quaternion
         will be enque in the handler, in this way we minimize the possibility to notify the quaternion in
        a different order respect the arrival order */
        mWriteLock.lock();
            mLastUpdate = new Date();
            int nReadByte = extractData(timeStamp, data, dataOffset).getReadBytes();
        mWriteLock.unlock();

        return nReadByte;
    }//

    private void notifySample(long timestamp, float qi,float qj,float qk,float qs,byte rawData[]){
        Sample newSample;
        mWriteLock.lock();
            mLastSample = new Sample(timestamp,new Number[]{qi,qj,qk,qs},getFieldsDesc());
            newSample = mLastSample;
        mWriteLock.unlock();
        notifyUpdate(newSample);
        logFeatureUpdate(rawData,newSample);
    }

    /**
     * runnable that update the feature internal data
     */
    private class DelayUpdate implements Runnable {

        /** quaternion that will be notify */
        private long mTimestamp;
        private float mQi, mQj, mQk, mQs;
        /**
         * create the runnable that will update the feature quaternion component
         * @param qi new qi component
         * @param qj new qj component
         * @param qk new qk component
         * @param qs new qs component
         */
        public DelayUpdate(long timestamp,float qi, float qj, float qk, float qs) {
            mTimestamp = timestamp;
            mQi = qi;
            mQj = qj;
            mQk = qk;
            mQs = qs;
        }//DelayUpdate

        /**
         * acquire the write lock and change the quaternion value,
         * at the end notify to the listener the change
         */
        @Override
        public void run() {
           notifySample(mTimestamp,mQi,mQj,mQk,mQs,null);
        }
    }
}