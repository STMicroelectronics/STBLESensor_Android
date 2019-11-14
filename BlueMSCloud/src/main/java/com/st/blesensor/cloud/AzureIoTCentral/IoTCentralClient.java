/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.blesensor.cloud.AzureIoTCentral;

import androidx.annotation.Nullable;
import android.util.Log;


import com.github.lucadruda.iotc.device.Command;
import com.github.lucadruda.iotc.device.ILogger;
import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.IoTCCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_EVENTS;
import com.github.lucadruda.iotc.device.enums.IOTC_LOGGING;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class IoTCentralClient implements CloudIotClientConnectionFactory.CloutIotClient {
    private static final String TAG = "IoTCentralClient";

    private ExecutorService mBackgroundThread = Executors.newFixedThreadPool(1);
    private IoTCClient client;
    private boolean isConnected = false;

    private IoTCCallback logCallback = new IoTCCallback() {
        @Override
        public void Exec(Object result) {
            Log.d(TAG,"Log Result: "+result);
        }
    };

            //(error, message) -> Log.d(TAG,"Error: "+error+" message: "+message);

    IoTCentralClient(String scopeId, String deviceId, String masterKey){
        client = new IoTCClient(deviceId,scopeId, IOTC_CONNECT.SYMM_KEY,masterKey, new AndroidLogger());
    }

    void connect(CloudIotClientConnectionFactory.ConnectionListener callback){
        mBackgroundThread.submit(()-> {
            try {
                client.Connect();
                isConnected=true;
                callback.onSuccess();
            } catch (IoTCentralException e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
            /* client.Connect((error, result) -> {
                if(result.equals(IotHubConnectionStatus.CONNECTED.toString())) {
                    isConnected=true;
                    callback.onSuccess();
                }else if (error !=null){
                    callback.onFailure(new Exception(error));
                }

            });*/
        });
    }

    void disconnect(){
        isConnected=false;
        mBackgroundThread.submit(() -> {
            try {
                client.Disconnect(logCallback);
            } catch (IoTCentralException e) {
                e.printStackTrace();
            }
        });
    }

    void destroy(){
        mBackgroundThread.shutdown();
    }

    boolean isConnected(){
        return isConnected;
    }


    void publish(String obj){
        Log.d(TAG,"Publish: "+obj);
        mBackgroundThread.submit(
                () -> client.SendTelemetry(obj,null)
        );
    }

    private static final String FW_UPGRADE_CMD = "updFirmware";
    private static final String FW_UPGRADE_URL = "url";
    private static final String FW_UPGRADE_RESPONSE = "Fw url received";

    private static Map<String,Object> prepareResponse(String commandName,String respContent){
        Map<String, Object> responseMap = new HashMap<>();
        HashMap<String, String> valueMap = new HashMap<>();
        valueMap.put("value", respContent);
        responseMap.put(commandName, valueMap);
        return responseMap;
    }

    private static @Nullable String extractFwUpgradeUrl(Command commandData) {
        try {
            String commandName = commandData.getName();
            if (commandName.equals(FW_UPGRADE_CMD)) {
                JSONObject payload = new JSONObject(commandData.getPayload());
                return payload.getString(FW_UPGRADE_URL);
            }
        } catch (JSONException e) {
            Log.d(TAG,"Invalid command data: "+e.getMessage());
        }
        return null;
    }

    void onFwUpgradeCommand(CloudIotClientConnectionFactory.FwUpgradeAvailableCallback callback){
        client.on(IOTC_EVENTS.Command, new IoTCCallback() {
            @Override
            public void Exec(Object result) {
                Log.d(TAG,"Result:"+result);
                if (!(result instanceof Command))
                    return;
                Command commandData = (Command)result;
                String newFwUrl = extractFwUpgradeUrl(commandData);
                if(newFwUrl !=null) {
                    callback.onFwUpgradeAvailable(newFwUrl);
                    Map<String, Object> response = prepareResponse(FW_UPGRADE_CMD, FW_UPGRADE_RESPONSE);
                    try {
                        client.SendProperty(response, null);
                    } catch (IoTCentralException e) {
                        e.printStackTrace();
                    }//try-catch
                }//if
            }//Exec
        });
    }

    class AndroidLogger implements ILogger {

        @Override
        public void Log(String s) {
            Log.i(TAG,s);
        }

        @Override
        public void SetLevel(IOTC_LOGGING iotc_logging) { }
    }

}
