package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import androidx.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.Field;
import com.st.blesensor.cloud.util.SubSamplingFeatureListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

class AzureIoTCentralFeatureListener2 extends SubSamplingFeatureListener {
/*
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

    private IoTCentralClient2 mClient;

    AzureIoTCentralFeatureListener2(IoTCentralClient2 client,long minUpdateInterval){
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

    private @Nullable
    JSONObject createContosoJson(Feature f, Feature.Sample sample) throws JSONException {
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
*/

    public static boolean isSupportedFeature(Feature f){
        return SUPPORTED_FEATURES.contains(f.getClass());
    }

    private static final List<Class<? extends Feature>> SUPPORTED_FEATURES = Arrays.asList(
            FeatureAcceleration.class,
            FeatureGyroscope.class,
            FeatureHumidity.class,
            FeatureMagnetometer.class,
            FeaturePressure.class,
            FeatureTemperature.class,
            FeatureFFTAmplitude.class,
            FeatureMicLevel.class
    );

    private IoTCentralClient2 mClient;

    AzureIoTCentralFeatureListener2(IoTCentralClient2 client, long minUpdateInterval){
        super(minUpdateInterval);
        mClient = client;
    }

    public static final String TIMESTAMP = "timestamp";

    private static void addScalarField(JSONObject obj,String prefix, Field field, Number value)
            throws JSONException {

        String fieldName = prefix+field.getName();
        switch (field.getType()){
            case Float:
                obj.put(fieldName,value.floatValue());
                break;
            case Int64:
            case UInt32:
                obj.put(fieldName,value.longValue());
                break;
            case Int32:
            case UInt16:
                obj.put(fieldName,value.intValue());
                break;
            case Int16:
            case UInt8:
                obj.put(fieldName,value.shortValue());
                break;
            case Int8:
                obj.put(fieldName,value.byteValue());
                break;
            case ByteArray: //is not a scalar type!
                break;
        }//switch
    }//addScalarField

    public static JSONObject serialize(String featureName,Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(TIMESTAMP,sample.timestamp);
        int nField = sample.data.length;
        if(nField>1) {
            for (int i = 0; i < nField; i++) {
                Field field = sample.dataDesc[i];
                addScalarField(obj, featureName+"_", field, sample.data[i]);
            }
        }
        else {
            addScalarField(obj,"", sample.dataDesc[0], sample.data[0]);
        }
        return obj;
    }

    private void sendGenericFeature(Feature f, Feature.Sample sample){
        try {
            JSONObject obj = serialize(f.getName(),sample);
            if(mClient.isConnected()){
                mClient.publish(obj.toString());
            }
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(getClass().getName(), "Error Logging the sample: " +
                    sample + "\nError:" + e.getMessage());
        }
    }

    private void sendFFTAmplitudeFeature(Feature.Sample sample){
        String componentName[] = {"X","Y","Z"};
        if(!FeatureFFTAmplitude.isComplete(sample))
            return;
        List<float[]> data = FeatureFFTAmplitude.getComponents(sample);
        float freqStep = FeatureFFTAmplitude.getFreqStep(sample);
        int nSample = FeatureFFTAmplitude.getNSample(sample);
        try {
        for (int i = 0; i <nSample; i++) {
            JSONObject obj = new JSONObject();
            obj.put("f",freqStep*i);
            for (int j = 0; j < componentName.length; j++) {
                obj.put("FFT_"+componentName[j],data.get(j)[i]);
            }
            if(mClient.isConnected()){
                mClient.publish(obj.toString());
            }

        }
        } catch (JSONException e) {
            Log.e(getClass().getName(), "Error Logging the sample: " +
                    sample + "\nError:" + e.getMessage());
        }
    }

    @Override
    public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
        if( f instanceof FeatureFFTAmplitude) {
            sendFFTAmplitudeFeature(sample);
        }else {
            super.onUpdate(f,sample);
        }
    }

    @Override
    public void onNewDataUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
        sendGenericFeature(f, sample);
    }


}
