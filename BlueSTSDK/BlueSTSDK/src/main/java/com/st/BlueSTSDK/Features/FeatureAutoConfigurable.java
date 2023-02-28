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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extend the feature adding the possibility to be configured sending a command message to the
 * node.
 * <p>
 * We can start an auto configuration process that will start an algorithm that will run on the
 * remote device that will extract the information needed for the configuration.
 * </p>
 * <p>
 * The user will be informed of the status of this process with a callback on the method
 * {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#notifyAutoConfigurationStatus(int)}
 * when the percentage of the process receive the 100% the configuration is finished.
 * </p>
 * <p>
 * When the configuration finish the user will receive a callback to the method
 * {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#notifyConfigurationFinished(int)}
 * with the  final configuration goodness or a negative value if there was an error during the
 * configuration
 * </p>
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public abstract class FeatureAutoConfigurable extends Feature {

    /**
     * value used for start the configuration procedure
     */
    protected final byte FEATURE_COMMAND_START_CONFIGURATION = 0x00;
    /**
     * value used for stop the configuration procedure
     */
    protected final byte FEATURE_COMMAND_STOP_CONFIGURATION = 0x01;
    /**
     * value used for ask the current configuration status/goodness
     */
    protected final byte FEATURE_COMMAND_GET_CONFIGURATION_STATUS = (byte) 0xFF;

    /**
     * tell if the feature is configured, in the node side.
     * if it is false it you have to call {@link FeatureAutoConfigurable#startAutoConfiguration()} before
     * have correct values
     * default value: true
     */
    private @Nullable Boolean mIsConfigured;

    private CalibrationSensorTileBox mStBoxCalib;

    /**
     * @param name     name of the feature
     * @param n        node that will update this feature
     * @param dataDesc description of the data that belong to this feature
     */
    public FeatureAutoConfigurable(String name, Node n, Field dataDesc[]) {
        super(name, n, dataDesc);
        mIsConfigured = null;
        //This must be changed for .box-Pro
        if((n.getType() == Node.Type.SENSOR_TILE_BOX) || (n.getType() == Node.Type.SENSOR_TILE_BOX_PRO)){
            mStBoxCalib = new CalibrationSensorTileBox(n.getDebug());
        }
    }//

    /**
     * call the method {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#onAutoConfigurationStarting(FeatureAutoConfigurable)}
     * for each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     */
    protected void notifyAutoConfigurationStart() {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureAutoConfigurationListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureAutoConfigurationListener) listener)
                                .onAutoConfigurationStarting(FeatureAutoConfigurable.this);
                    }//run
                });
        }//for
    }//notifyUpdate

    /**
     * call the method {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#onAutoConfigurationStatusChanged(FeatureAutoConfigurable, int)}
     * for each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     *
     * @param status status of the initialization process, 0 = not started 100=completed
     */
    protected void notifyAutoConfigurationStatus(final int status) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureAutoConfigurationListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureAutoConfigurationListener) listener)
                                .onAutoConfigurationStatusChanged(FeatureAutoConfigurable.this, status);
                    }//run
                });
        }//for
    }//notifyUpdate

    /**
     * call the method {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#onConfigurationFinished(FeatureAutoConfigurable, int)}
     * for each listener that subscribe to this feature.
     * <p> each call will be run in a different thread</p>
     * @param status last status of the calibration process, 0 = not started 100=completed
     */
    protected void notifyConfigurationFinished(final int status) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureAutoConfigurationListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureAutoConfigurationListener) listener)
                                .onConfigurationFinished(FeatureAutoConfigurable.this, status);
                    }//run
                });
        }//for
    }//notifyUpdate

    /**
     * start a configuration process
     *
     * @param data raw data to use for initialize this feature
     * @return true if the request is correctly send
     * <p> the data array can have length 0, in this case no data payload will be send to the
     * feature characteristics</p>
     */
    protected boolean startConfiguration(byte[] data) {
        setConfigurationStatus(false);
        boolean messageSend = sendCommand(
                FEATURE_COMMAND_START_CONFIGURATION, data);
        if (!messageSend && mStBoxCalib != null){
            mStBoxCalib.startCalibration();
            messageSend = true;
        }
        if (messageSend)
            notifyAutoConfigurationStart();
        return messageSend;
    }

    /**
     * this method will parse the response to a stopAutoConfiguration or configuration status
     * commands.
     * those commands contains a status, that is -1 if there is an error,
     * or a percentage that represent the calibration goodness
     */
    @Override
    protected void parseCommandResponse(int timeStamp, byte commandType, byte[] data) {
        byte status = data[0];
        if (commandType == FEATURE_COMMAND_STOP_CONFIGURATION) {
            notifyConfigurationFinished(status);
            if (status == 100)
                setConfigurationStatus(true);
        } else if (commandType == FEATURE_COMMAND_GET_CONFIGURATION_STATUS) {
            notifyAutoConfigurationStatus(status);
            if (status == 100) {
                setConfigurationStatus(true);
                notifyConfigurationFinished(status);
            } else if (status == 0)
                setConfigurationStatus(false);
        } else
            super.parseCommandResponse(timeStamp, commandType, data);
    }

    /**
     * start a initialization process, that doesn't require data
     *
     * @return true if the request is correctly send
     */
    public boolean startAutoConfiguration() {
        return startConfiguration(new byte[]{});
    }

    /**
     * Stop the auto configuration procedure
     * <p>the node will notify to the user that the procedure stops with a call back on the
     * method {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#onConfigurationFinished(FeatureAutoConfigurable, int)}</p>
     *
     * @return true if the request is correctly send to the node
     */
    public boolean stopAutoConfiguration() {
        return sendCommand(FEATURE_COMMAND_STOP_CONFIGURATION, new byte[]{});
    }//stop calibration

    /**
     * Request to know the current status of the auto configuration procedure
     * <p>the status will be notify by a callback to
     * {@link com.st.BlueSTSDK.Features.FeatureAutoConfigurable.FeatureAutoConfigurationListener#notifyAutoConfigurationStatus(int)}</p>
     *
     * @return true if the request is correctly send to the node
     */
    public boolean requestAutoConfigurationStatus() {
        if(mStBoxCalib!=null){
            mStBoxCalib.getCalibrationStatus();
            return true;
        }
        return sendCommand(FEATURE_COMMAND_GET_CONFIGURATION_STATUS, new byte[]{});
    }

    /**
     * change the configuration status
     *
     * @param status new configuration status
     */
    protected void setConfigurationStatus(boolean status) {
        mIsConfigured = status;
    }

    /**
     * True if a feature is configured
     *
     * @return true if the feature is configured, false otherwise
     */
    public @Nullable Boolean isConfigured() {
        return mIsConfigured;
    }

    /**
     * extend the {@link com.st.BlueSTSDK.Feature.FeatureListener} with the callback used for
     * notify the configuration status
     */
    public interface FeatureAutoConfigurationListener extends Feature.FeatureListener {

        /**
         * The feature has start a process for self configuration
         *
         * @param f feature that start the configuration process
         */
        void onAutoConfigurationStarting(FeatureAutoConfigurable f);

        /**
         * Notify that the process of configuration has a new state
         *
         * @param f      feature that change its configuration status
         * @param status new calibration status when reach 100 the configuration is completed
         */
        void onAutoConfigurationStatusChanged(FeatureAutoConfigurable f, int status);

        /**
         * The configuration process is done or stopped by the user
         *
         * @param f      feature that finish its configuration process
         * @param status final configuration status: -1 if there was an error,
         *               otherwise is a % with the configuration goodness
         */
        void onConfigurationFinished(FeatureAutoConfigurable f, int status);

    }//FeatureListener

    private class CalibrationSensorTileBox implements Debug.DebugOutputListener{

        private Debug mConsole;
        private final Pattern STATUS_PARSER = Pattern.compile("magnCalibStatus (\\d+)");

        public CalibrationSensorTileBox(Debug console){
            mConsole = console;

        }

        void startCalibration(){

            mConsole.addDebugOutputListener(this);
            mConsole.write("startMagnCalib");
        }

        void getCalibrationStatus(){
            mConsole.write("getMagnCalibStatus");
        }

        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {
            Matcher matcher = STATUS_PARSER.matcher(message);
            if (!matcher.matches())
                return;
            mConsole.removeDebugOutputListener(this);
            byte calibStatus = Byte.parseByte(matcher.group(1));
            parseCommandResponse(0,FEATURE_COMMAND_GET_CONFIGURATION_STATUS,
                    new byte[]{calibStatus});
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) {

        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {

        }
    }

}//FeatureConfigurable
