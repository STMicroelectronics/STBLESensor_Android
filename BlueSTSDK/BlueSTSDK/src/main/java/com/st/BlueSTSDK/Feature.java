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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.st.BlueSTSDK.Features.Field;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Contains the data and the description of the data exported by the node
 * <p>
 * When you have a new sensor in the node you have to extend this class and implement the
 * method {@link Feature#extractData(long, byte[], int)}  that will extract the information from the raw
 * data arrived from the node. This class will manage the notification and listener subscrition.
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public abstract class Feature {

    /**
     * pool of thread used for notify to the listeners that the feature have new data
     */
    protected static final ExecutorService sThreadPool = Executors.newCachedThreadPool();
    //protected static final ExecutorService sThreadPool = Executors.newFixedThreadPool(4);

    /**
     * list of listener for the feature change.
     * <p> is a thread safe list, so a listener can subscribe itself from a callback </p>
     * @see com.st.BlueSTSDK.Feature.FeatureListener
     */
    protected final CopyOnWriteArrayList<FeatureListener> mFeatureListener = new CopyOnWriteArrayList<>();
    /**
     * list of listener for logging the received data
     * <p> is a thread safe list, so a listener can subscribe itself from a callback </p>
     * @see com.st.BlueSTSDK.Feature.FeatureLoggerListener
     */
    private final CopyOnWriteArrayList<FeatureLoggerListener> mFeatureLogger = new CopyOnWriteArrayList<>();
    /**
     * read/write lock used for avoid that someone read the feature data while an update is
     * running
     */
    private final ReentrantReadWriteLock mRwLock = new ReentrantReadWriteLock();
    /**
     * lock to acquire before change the data
     */
    protected final ReentrantReadWriteLock.WriteLock mWriteLock = mRwLock.writeLock();
    /**
     * local time of the last update, automatically update from the default update method
     */
    protected Date mLastUpdate;

    /**
     * last data received from the node
     */
    protected Sample mLastSample;

    /**
     * node that will update this feature
     */
    protected Node mParent;
    /**
     * feature name
     */
    private String mName;
    /**
     * if the node export this feature, its possible that a node has a feature but doesn't
     * export it
     */
    private boolean mIsEnabled;

    /**
     * This variable says if the features must be included on not on Textual/Cloud Demo
     */
    private boolean isDataNotifyFeature;

    /**
     * array of feature field that describe the feature data with the name,
     * unit and min/max value for each feature field
     */
    protected Field[] mDataDesc;

    /**
     * check if the sample has valid data in the index position
     * @param s sample to test
     * @param index index to test
     * @return true if sample is not null and has a non null value into the index position
     */
    protected static boolean hasValidIndex(Sample s, int index){
        return (index >=0 && (s != null) && (s.data.length > index) && (s.data[index]!=null));
    }

    /**
     * extract a float number from the sample
     * @param s sample that contains the data
     * @param i data index
     * @return value or nan if the value is not present
     */
    protected static float getFloatFromIndex(Sample s, int i){
        if(hasValidIndex(s,i)){
            return s.data[i].floatValue();
        }
        return Float.NaN;
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param name     name of the feature
     * @param n        node that will update this feature
     * @param dataDesc description of the data that belong to this feature
     */
    public Feature(@NonNull String name,@NonNull Node n,@NonNull Field dataDesc[]) {
        mName = name;
        mParent = n;
        mIsEnabled = false;
        mDataDesc = dataDesc;
        isDataNotifyFeature = true;
    }//

    public Feature(@NonNull String name,@NonNull Node n,@NonNull Field dataDesc[],@NonNull boolean isDataFeature) {
        mName = name;
        mParent = n;
        mIsEnabled = false;
        mDataDesc = dataDesc;
        isDataNotifyFeature =isDataFeature;
    }

    /**
     * add a new listener for the update of this feature
     *
     * @param listener listener class
     */
    public void addFeatureListener(FeatureListener listener) {
        if (listener != null)
            mFeatureListener.addIfAbsent(listener);
    }//addFeatureListener

    /**
     * remove a listener for the update of this feature
     *
     * @param listener listener to remove
     */
    public void removeFeatureListener(FeatureListener listener) {
        mFeatureListener.remove(listener);
    }

    /**
     * add a new object where log the update of this feature
     *
     * @param logger new logger to update when the feature has an update
     */
    public void addFeatureLoggerListener(FeatureLoggerListener logger) {
        if (logger != null)
            mFeatureLogger.addIfAbsent(logger);
    }

    /**
     * remove a logger listener
     *
     * @param logger listener to remove
     */
    public void removeFeatureLoggerListener(FeatureLoggerListener logger) {
        mFeatureLogger.remove(logger);
    }

    /**
     * date of the last update
     *
     * @return date of the last update
     */
    public Date getLastUpdate() {
        return mLastUpdate; //no need to lock since we are just returning a reference
    }

    /**
     * get the feature name
     *
     * @return feature name
     */
    public String getName() {
        return mName;
    }

    /**
     * node that will update this feature
     *
     * @return node that will update this feature
     */
    public Node getParentNode() {
        return mParent;
    }

    /**
     * true if the node export the data of this feature
     * <p> a Node can export a feature in the advertise without having the equivalent
     * characteristics</p>
     *
     * @return true if the node export the data of this feature, false otherwise
     */
    public boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     *  true if the feature streams data in notification
     * @return true if the Feature streams data
     */
    public boolean isDataNotifyFeature() {return isDataNotifyFeature;}

    /**
     * Get the description of the data field of this feature.
     * <p>From the returned data you can know the name and units of the corrispondig data in the
     * array returned by the getFieldData </p>
     *
     * @return data description of the feature
     */
    public @NonNull Field[] getFieldsDesc() {
        return mDataDesc;
    }//getFieldsDesc

    /**
     * return the last timestamp and the data received from the device
     * @return last data received from the feature or null
     */
    public @Nullable Sample getSample(){
        if(mLastSample!=null)
            //no need to lock since we are just returning a reference
            return new Sample(mLastSample);
        else
            return null;
    }

    /**
     * change the status of this feature
     *
     * @param enable new feature status
     */
    void setEnable(boolean enable) {
        mIsEnabled = enable;
    }//setEnable

    public void enableNotification(){
        mParent.enableNotification(this);
    }

    public void read(){
        mParent.readFeature(this);
    }

    public void disableNotification(){
        mParent.disableNotification(this);
    }

    /**
     * call the method {@link com.st.BlueSTSDK.Feature.FeatureListener#onUpdate(Feature,
     * Feature.Sample)} for each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     * <p>
     * If you extend the method overwrite the method {@link Feature#update_priv(long, byte[], int)}
     * you have to call this method for notify to the user the new sample
     * </p>
     * @param sample new data that we have to notify to the listener
     */
    protected void notifyUpdate(final @Nullable Sample sample) {
        if(sample==null)
            return;
        for (final FeatureListener listener : mFeatureListener) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate(Feature.this,sample);
                }//run
            });
        }//for
    }//notifyUpdate

    /**
     * notify to all the logger that we parse new package
     * <p> each {@link com.st.BlueSTSDK.Feature.FeatureLoggerListener#logFeatureUpdate(byte[], Sample)}
     * will run in a different thread </p>
     * <p>
     * if you overwrite the method {@link com.st.BlueSTSDK.Feature#update_priv(long, byte[],
     * int)} you have to call this method after that you update the data,
     * if you want that your feature can be logged
     * </p>
     *
     * @param rawData raw data that we have used for extract the feature field, can be null
     * @param sample sample that we have to log
     */
    protected void logFeatureUpdate(final byte[] rawData, @Nullable final Sample sample) {
        for (final FeatureLoggerListener listener : mFeatureLogger) {
            sThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.logFeatureUpdate(Feature.this, rawData, sample);
                }//run
            });
        }//for
    }//logRawData

    /**
     * update the feature internal data and notify the update to the listener
     * <p>
     * if you overwrite this method, remember to update the the timestamp,
     * the lastUpdate value and to acquire the write lock, when you finish you should to call
     * the  {@link Feature#notifyUpdate(Sample)} and {@link Feature#logFeatureUpdate(byte[], Sample)}
     * for notify the update to the user </p>
     * <p> This method will be called by a package protected method called by the node,
     * in this way we can permit only to the node to call the update method,
     * without export this functionality to the final user</p>
     *
     * @param timeStamp  package timestamp
     * @param data       array of data where we will extract  the data
     * @param dataOffset data offset fo the data array where we have to read
     * @return number of read byte
     */
    protected int update_priv(long timeStamp, byte[] data, int dataOffset) {
        //acquire the write permission
        Sample newSample=null; //keep a reference for the notification
        mWriteLock.lock(); // made the update atomic
            mLastUpdate = new Date();
            ExtractResult res = extractData(timeStamp, data, dataOffset);
            if(res.newSample!=null) {
                newSample = mLastSample = res.newSample;
            }
        mWriteLock.unlock();

        if(mIsEnabled) {
            //notify to all the listener that the new data arrived
            notifyUpdate(newSample);

            //pass to the log only the byte that we have read
            logFeatureUpdate(java.util.Arrays.copyOfRange(data, dataOffset, dataOffset + res.nReadByte),
                    newSample);
        }
        return res.nReadByte;
    }//update

    /**
     * this method is called by the node when receive new data from this feature
     * @param timeStamp  package timestamp
     * @param data       array of data where we will extract  the data
     * @param dataOffset data offset fo the data array where we have to read
     * @return number of read byte
     */
    int update(long timeStamp,@NonNull byte[] data, int dataOffset) {
        return update_priv(timeStamp,data,dataOffset);
    }

    /**
     * parse the command response data
     * @param timeStamp device time stamp of when the response was send
     * @param commandType id of the request that the feature did
     * @param data data attached to the response
     * <p> it is implemented as an empty method, it is an optional abstract method</p>
     */
    protected void parseCommandResponse(int timeStamp,byte commandType,byte[] data){}


    /**
     * notify to the feature that the node receive a response from a command send from this feature
     * @param timeStamp device time stamp of when the response was send
     * @param commandType id of the request that the feature did
     * @param data data attached to the response
     */
    void commandResponseReceived(int timeStamp,byte commandType, byte[] data){
        parseCommandResponse(timeStamp,commandType,data);
    }

    /**
     * class used for return the data and the number of bytes read
     */
    protected class ExtractResult{
        /** number of read bytes */
        final int nReadByte;
        /** data extracted from the byte stream */
        final @Nullable Sample newSample;

        /**
         * create a new object
         * @param newSample data extracted
         * @param nReadByte number of byte used for extract the data
         */
        public ExtractResult(@Nullable Sample newSample,int nReadByte){
            this.nReadByte=nReadByte;
            this.newSample=newSample;
        }//ExtractResult

        /**
         * number of read bytes
         * @return number of bytes used for extract the data
         */
        public int getReadBytes() {
            return nReadByte;
        }//getReadBytes

        /**
         * data extracted by the feature
         * @return data extracted by the raw bytes stream
         */
        public @Nullable Sample getNewSample(){
            return newSample;
        }
    }//ExtractResult

    /**
     * extract the Feature data from a raw byte stream
     *<p>
     * You have to parse the data inside the {@code data} field skipping the first {@code
     * dataOffset} byte. </p>
     *<p>
     * This method have to exact the data create a {@link com.st.BlueSTSDK.Feature
     * .Sample} and return a Extract result containing it</p>
     *<p>
     * The method that call this one will manage the lock acquisition/release and to
     * notify to the user the new sample</p>
     *
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte and data extracted
     */
    abstract protected ExtractResult extractData(long timestamp,@NonNull byte[] data, int dataOffset);

    /**
     * in case the corresponding characteristics has the write permission you can send some data
     * to the feature
     * @param data raw data to write
     * @return true if the write command is send correctly, false otherwise
     */
    protected boolean writeData(@NonNull byte[] data) {
        return mParent.writeFeatureData(this, data);
    }//writeData

    /**
     * write the data into the corresponding characteristics and call the callback when the system
     * do it
     * @param data data to write
     * @param onWriteComplete callback to do when the write data is done
     * @return true if the write command is send correctly, false otherwise
     */
    protected boolean writeData(@NonNull byte[] data, @Nullable Runnable onWriteComplete) {
        return mParent.writeFeatureData(this, data, onWriteComplete);
    }//writeData

    /**
     * this method can be used for send data to the command characteristic,
     * for the extended feature use the {@link Feature#writeData(byte[], Runnable)}  method
     *
     * @param commandType integer that identify the command to execute
     * @param data array of data to send as command parameters, can be an empty array
     * @return true if the node has the command characteristic
     */
    protected boolean sendCommand(byte commandType,@NonNull byte[] data){
        return mParent.sendCommandMessage(this,commandType,data);
    }


    /**
     * print the last data received by this feature
     * @return string with the feature data
     */
    @Override
    public String toString(){
        //create the string with the feature data
        Sample sample = mLastSample; //keep a reference for be secure to be thread safe
        if(sample==null)
            return mName+":\n\tNo Data";
        //else
        if(sample.data.length==0){
            return mName+":\n\tNo Data";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mName).append(":\n\tTimestamp: ").append(sample.timestamp).append('\n');
        Number data[] = sample.data;
        for (int i = 0; i < data.length-1; i++) {
            sb.append('\t').append(mDataDesc[i].getName())
                    .append(": ").append(data[i]).append('\n');
        }//for
        sb.append('\t').append(mDataDesc[data.length-1].getName())
                .append(": ").append(data[data.length-1]);

        return sb.toString();
    }

    /**
     * print the last data received by this feature including only the one that we want to plot
     * @return string with the feature data
     */
    public String toStringToPlot(){
        //create the string with the feature data
        Sample sample = mLastSample; //keep a reference for be secure to be thread safe
        if(sample==null)
            return mName+": No Data";
        //else
        if(sample.data.length==0){
            return mName+": No Data";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("TS: ").append(sample.timestamp).append(' ');
        Number data[] = sample.data;
        for (int i = 0; i < data.length-1; i++) {
            if(sample.dataDesc[i].getPlotIt()) {
                sb.append(mDataDesc[i].getName())
                        .append(": ").append(data[i]).append(' ');
            }
        }//for
        if(sample.dataDesc[data.length - 1].getPlotIt()) {
            sb.append(mDataDesc[data.length - 1].getName())
                    .append(": ").append(data[data.length - 1]);
        }

        return sb.toString();
    }

    /**
     * Interface used for notify that the feature updates its data
     *
     * @author STMicroelectronics - Central Labs.
     */
    public interface FeatureListener {

        /**
         * this method is called when the feature update its internal value
         *
         * @param f feature that has received an update
         * @param sample new data received from the feature
         */
        @WorkerThread
        void onUpdate(@NonNull Feature f,@NonNull Sample sample);

    }//FeatureListener

    /**
     * This class permit to dump the feature data, both in raw format (the same that that we
     * receive from the node) and after that we parse it
     *
     * @author STMicroelectronics - Central Labs.
     */
    public interface FeatureLoggerListener {

        /**
         *Call when we have to log some data
         *
         * <p> the rawData array can be null if we extract multiple feature data that are notify
         * to the user in different moment. in that case the only in the first call we will have
         * a rawData!=null </p>
         *
         * @param feature feature that has updated
         * @param rawData raw data that used for update the feature
         * @param sample data extracted by the feature
         *
         */
        @WorkerThread
        void logFeatureUpdate(@NonNull Feature feature,
                              @NonNull byte[] rawData,
                              @Nullable Sample sample);
    }

    /**
     * Class that contains the last data from the node
     */
    public static class Sample{

        /**
         * Data time stamp send with the data
         */
        public final long timestamp;

        /**
         * feature data
         */
        public final Number[] data;

        /**
         * description of the data field
         */
        public final Field[] dataDesc;

        /**
         * Unix timestamp of the moment the notification arrive at the device
         */
        public final long notificationTime;

        /**
         * @param timestamp data timestamp
         * @param data feature data
         * @param dataDesc description for each field data
         */
        public Sample(long timestamp,@NonNull Number[] data, @NonNull Field[] dataDesc) {
            this.timestamp = timestamp;
            this.data = data;
            this.dataDesc=dataDesc;
            notificationTime = System.currentTimeMillis();
        }

        public Sample(@NonNull Number[] data,@NonNull Field[] dataDesc) {
            this(0,data,dataDesc);
        }

        /**
         * create a copy of the class, it is used for avoid that the user change the internal data
         * @param copyMe object to copy
         */
        public Sample(@NonNull Sample copyMe) {
            this.timestamp =copyMe.timestamp;
            this.data = copyMe.data.clone();
            this.notificationTime =copyMe.notificationTime;
            this.dataDesc = copyMe.dataDesc;
        }

        @Override
        public boolean equals(Object o) {
            if(o==null)
                return false;
            if(o instanceof Sample){
                Sample s = (Sample)o;
                return s.timestamp == timestamp  && Arrays.equals(data,s.data);
            }//if
            return false;
        }

        @Override
        public String toString(){
            return "Timestamp: "+timestamp +" Data: "+Arrays.toString(data);
        }
    }
}
