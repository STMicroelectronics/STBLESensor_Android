package com.st.blesensor.cloud.AzureIoTCentralPnP.AzurePnPHelperFunction;

import android.util.Log;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.provisioning.device.AdditionalData;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class AzurePnpInterface {
    private AzureCloudDevice selectedDevice;
    private static final String globalEndpoint = "global.azure-devices-provisioning.net";
    private static final String TAG="AzurePnpInterface";

    // Plug and play features are available over MQTT, MQTT_WS, AMQPS, and AMQPS_WS.
    private static final ProvisioningDeviceClientTransportProtocol provisioningProtocol = ProvisioningDeviceClientTransportProtocol.MQTT;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000; // in milli seconds

    private static DeviceClient deviceClient;

    public AzurePnpInterface(AzureCloudDevice device) {
        selectedDevice = device;
    }

    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageIotHubEventCallback implements IotHubEventCallback {

        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
            Message msg = (Message) callbackContext;
            Log.d(TAG,"Telemetry - Response from IoT Hub: message Id={"+msg.getMessageId()+"}, status={"+responseStatus.name()+"}");
        }
    }

    public void sendMessageWithCallback(@NotNull Message message) {
        deviceClient.sendEventAsync(message, new MessageIotHubEventCallback(), message);
    }

    public void sendMessageWithoutCallback(@NotNull Message message) {
        deviceClient.sendEventAsync(message, null, message);
    }

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                Log.d(TAG,"Received unknown context");
            }
        }
    }

    public Boolean initializeAndProvisionDevice() throws ProvisioningDeviceClientException, IOException, URISyntaxException, InterruptedException {

        Boolean validateDevice = false;
        if(selectedDevice!=null) {
            if(selectedDevice.getCredentials()!=null) {
                if((selectedDevice.getCredentials().getIdScope()!=null) && (selectedDevice.getCredentials().getSymmetricKey()!=null)) {
                    if (selectedDevice.getCredentials().getSymmetricKey().getPrimaryKey() != null) {
                        validateDevice=true;
                    }
                }
            }
            if(validateDevice) {
                validateDevice= selectedDevice.getTemplateModel() != null;
            }
        }

        //If the device was not valid... return false
        if(!validateDevice) {
            return false;
        }

        SecurityProviderSymmetricKey securityClientSymmetricKey = new SecurityProviderSymmetricKey(
                selectedDevice.getCredentials().getSymmetricKey().getPrimaryKey().getBytes(StandardCharsets.UTF_8),
                selectedDevice.getId());
        ProvisioningDeviceClient provisioningDeviceClient;
        ProvisioningStatus provisioningStatus = new ProvisioningStatus();

        provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, selectedDevice.getCredentials().getIdScope(), provisioningProtocol, securityClientSymmetricKey);

        AdditionalData additionalData = new AdditionalData();
        String modelId = selectedDevice.getTemplateModel().getCapabilityModel().getId();
        additionalData.setProvisioningPayload(PnpHelper.createDpsPayload(modelId));

        provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus, additionalData);

        while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED)
            {
                provisioningStatus.exception.printStackTrace();
                Log.d(TAG,"Registration error, bailing out");
                break;
            }
            Log.d(TAG,"Waiting for Provisioning Service to register");
            Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
        }

        ClientOptions options = new ClientOptions();
        options.setModelId(modelId);

        if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
            Log.d(TAG,"IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
            Log.d(TAG,"Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

            String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
            String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();

             Log.d(TAG,"Opening the device client.");
            deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityClientSymmetricKey, IotHubClientProtocol.MQTT, options);
            deviceClient.open();
        }
        return true;
    }

    public void closeConnection() throws IOException {
        deviceClient.closeNow();
    }
}
