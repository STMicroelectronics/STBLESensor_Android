package com.st.blesensor.cloud.AzureIoTCentral;

import android.support.annotation.Nullable;
import android.util.Log;

import com.st.blesensor.cloud.util.SubSamplingFeatureListener;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class AzureIoTCentralFeatureListener extends SubSamplingFeatureListener {

    private static final List<Class<? extends Feature>> SUPPORTED_FEATURES = Arrays.asList(
            FeatureAcceleration.class,
            FeatureGyroscope.class,
            FeatureHumidity.class,
            FeatureMagnetometer.class,
            FeaturePressure.class,
            FeatureTemperature.class
    );

    public static boolean isSupportedFeature(Feature f){
        return SUPPORTED_FEATURES.contains(f.getClass());
    }

    private IoTCentralClient mClient;

    AzureIoTCentralFeatureListener(IoTCentralClient client,long minUpdateInterval){
        super(minUpdateInterval);
        mClient = client;
    }

    private JSONObject createAccelerometerJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("accelerometerX",FeatureAcceleration.getAccX(sample));
        obj.put("accelerometerY",FeatureAcceleration.getAccY(sample));
        obj.put("accelerometerZ",FeatureAcceleration.getAccZ(sample));
        return obj;
    }

    private JSONObject createGyroscopeJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("gyroscopeX",FeatureGyroscope.getGyroX(sample));
        obj.put("gyroscopeY",FeatureGyroscope.getGyroY(sample));
        obj.put("gyroscopeZ",FeatureGyroscope.getGyroZ(sample));
        return obj;
    }

    private JSONObject createMagnetometerJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("magnetometerX",FeatureMagnetometer.getMagX(sample));
        obj.put("magnetometerY",FeatureMagnetometer.getMagY(sample));
        obj.put("magnetometerZ",FeatureMagnetometer.getMagZ(sample));
        return obj;
    }

    private JSONObject createTemperatureJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("temp",FeatureTemperature.getTemperature(sample));
        return  obj;
    }

    private JSONObject createPressureJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("pressure",FeaturePressure.getPressure(sample));
        return  obj;
    }

    private JSONObject createHumidityJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("humidity",FeatureHumidity.getHumidity(sample));
        return  obj;
    }

    private @Nullable JSONObject createContosoJson(Feature f, Feature.Sample sample) throws JSONException {
        if (f instanceof  FeatureAcceleration){
            return createAccelerometerJson(sample);
        }else if ( f instanceof  FeatureGyroscope){
            return createGyroscopeJson(sample);
        }else if (f instanceof  FeatureMagnetometer){
            return createMagnetometerJson(sample);
        }else if (f instanceof  FeatureTemperature){
            return createTemperatureJson(sample);
        }else if (f instanceof FeaturePressure){
            return createPressureJson(sample);
        }else if ( f instanceof FeatureHumidity){
            return createHumidityJson(sample);
        }
        return null;
    }

    @Override
    public void onNewDataUpdate(Feature f, Feature.Sample sample) {
        try {
            JSONObject obj = createContosoJson(f,sample);
            if(obj!=null && mClient.isConnected()){
                mClient.publish(obj.toString());
            }
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(getClass().getName(), "Error Logging the sample: " +
                    sample + "\nError:" + e.getMessage());
        }
    }
}
