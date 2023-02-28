package com.st.blesensor.cloud.AzureIoTCentralPnP.AzurePnPHelperFunction;

import com.google.gson.Gson;
import androidx.annotation.NonNull;

import java.util.Map;
import static java.util.Collections.singletonMap;

/*
 A helper class for formatting command requests and properties as per plug and play convention.
 */
public class PnpHelper {

    private static final String MODEL_ID = "modelId";

    private static final Gson gson = new Gson();

    /**
     * Create the DPS payload to provision a device as plug and play.
     * For more information on device provisioning service and plug and play compatibility,
     * and PnP device certification, see https://docs.microsoft.com/en-us/azure/iot-pnp/howto-certify-device
     * The DPS payload should be in the format:
     * {
     *     "modelId": "dtmi:com:example:modelName;1"
     * }
     * For information on DTDL, see https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md
     * @param modelId The Id of the model the device adheres to for properties, telemetry, and commands.
     * @return The DPS payload to provision a device as plug and play.
     */
    public static String createDpsPayload(@NonNull String modelId) {
        Map<String, String> payload = singletonMap(MODEL_ID, modelId);
        return gson.toJson(payload);
    }
}