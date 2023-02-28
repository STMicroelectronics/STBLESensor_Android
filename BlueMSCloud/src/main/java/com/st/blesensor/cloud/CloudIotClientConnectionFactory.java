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

package com.st.blesensor.cloud;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.TextView;

import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Feature.FeatureListener;
import com.st.BlueSTSDK.Node;

/**
 * Interface used for open a connection to a mqtt broker
 */
public interface CloudIotClientConnectionFactory {

    /**
     * empty interface to hide the real client object that can be different for different
     * provider
     */
    interface CloutIotClient {
        //TODO move the connect,disconnect,isConnect methods here?
    }

    /**
     * callback trigger when the cloud send a firmware update message
     */
    interface FwUpgradeAvailableCallback{

        /**
         * a new firmware is available online
         * @param fwUrl url where find the new firmare
         */
        void onFwUpgradeAvailable(String fwUrl);
    }


    /**
     * callback done when the connection is done
     */
    interface ConnectionListener{

        /**
         * method call when the connection is done correctly
         */
        void onSuccess();

        /**
         * method call when the connection fails
         * @param exception error happen during the connection
         */
        void onFailure(Throwable exception);
    }

    /**
     * Callback called when we have a new Data Sample
     */
    interface NewSampleListener {
        /**
         * This is called when we have a new data to send to Cloud
         */
        void onNewSampleReady (int numSamples);
    }

    /**
     * create a mqtt mqtt
     * @param ctx context to use for crete the mqtt
     * @return object to use for open the connection
     */
    @Nullable CloutIotClient createClient(@NonNull Context ctx);

    /**
     * Open the mqtt connection
     * @param ctx context to use for open the connection
     * @param client mqtt to open
     * @param connectionListener callback to do when the connection is ready
     * @return operation id
     * @throws Exception if some error happen during the connection handshake
     */
    boolean connect(@NonNull Context ctx, @NonNull CloutIotClient client,
                    @NonNull ConnectionListener connectionListener) throws Exception;

    default void newSampleReady(@Nullable NewSampleListener newSampleListener) {

    }

    /**
     * listener that will send the data to the cloud service
     * @param broker mqtt to use for sned the mqtt message
     * @param minUpdateIntervalMs send a cloud update only after minUpdateIntervalMs milliseconds
     * @return listener to use in a feature for load the data to the cloud
     */
    @Nullable FeatureListener getFeatureListener(@NonNull CloutIotClient broker,long minUpdateIntervalMs);

     default void setNewSampleListener(@NonNull CloutIotClient broker, @NonNull NewSampleListener newSampleListener) {
    }

    /**
     * close the connection with the cloud
     * @param client connection to close
     * @throws Exception error happen during the connection
     */
    void disconnect(@NonNull CloutIotClient client) throws Exception;

    /**
     *  Function for enabling or not the upload button
     * @return
     */
    default boolean showUploadButton() {
        return false;
    };

    /**
     * upload the data to cloud
     * @param activityResultRegistry
     * @param client connection to close
     */
    default void upload(ActivityResultRegistry activityResultRegistry, @NonNull Activity act, @NonNull Context ctx, @NonNull CloutIotClient client) {

    }

    /**
     * free the connection resources
     * @param client client to free
     */
    void destroy(@NonNull CloutIotClient client);

    /**
     * function for enabling the text view for visualizing the data that we are sending to the cloud
     * @return true/false -> enable/disable
     */
    default boolean showSendingData() {
        return false;
    }

    /**
     * Function for passing the TextViews for Displaying the Sample value
     * @param featureName TextView for displaying the Feature Name
     * @param featureDetail TextView for displaying the Feature Detail
     */
    default void setTextViewsForDataSample(TextView featureName, TextView featureDetail) {}

    /**
     * tell if the client is connected
     * @param client connection to check
     * @return true if the client is connected with the cloud
     */
    boolean isConnected(@NonNull CloutIotClient client);

    /**
     * return the url page where see the uploaded data
     * @return page to visit for see the data or null if it is not available
     */
    @Nullable Uri getDataPage();

    /**
     * tell if a particular feature is supported by the cloud service
     * @param f feature to test
     * @return true if the service support the data upload from the feature, false otherwise
     */
    boolean supportFeature(@NonNull Feature f);

    /**
     * set up the connection to accept a fw upgrade message
     * @param node node to upgrade
     * @param cloudConnection connection used to send the message
     * @param callback function to call when a new fw is available
     * @return true if the function is supported, false otherwise
     */
    boolean enableCloudFwUpgrade(@NonNull Node node,@NonNull CloutIotClient cloudConnection, @NonNull FwUpgradeAvailableCallback callback);



}
