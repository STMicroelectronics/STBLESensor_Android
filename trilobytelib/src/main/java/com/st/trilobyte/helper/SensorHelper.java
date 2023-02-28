package com.st.trilobyte.helper;

import android.content.Context;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.PowerMode;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorHelper {

    public static String getSensorPropertiesDescription(Context context, Sensor sensor,Node.Type board) {

        List<String> properties = new ArrayList<>();

        if (sensor.getPowerModes() != null && !sensor.getPowerModes().isEmpty()) {
            PowerMode pm = sensor.getPowerModes().get(0);

            if (sensor.getPowerModes().size() > 1) {
                properties.add(context.getString(R.string.power_mode));
            }

            if (pm.getOdrs() != null) {
                properties.add(context.getString(R.string.odr));
            }
        }

        if (SensorFilterHelper.hasFilter(context, sensor.getId(),board)) {
            properties.add(context.getString(R.string.filter));
        }

        if (sensor.getFullScales() != null) {
            properties.add(context.getString(R.string.full_scale));
        }

        if (sensor.getSamplingFrequencies() != null) {
            properties.add(context.getString(R.string.sampling_frequencies));
        }

        return formatSensorProperties(properties);
    }

    private static String formatSensorProperties(List<String> properties) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < properties.size(); i++) {
            sb.append(properties.get(i));
            if (i != properties.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    public static PowerMode getPowerModeBySelectedMode(Sensor sensor, PowerMode.Mode mode) {

        if (sensor.getPowerModes() != null) {
            for (PowerMode powerMode : sensor.getPowerModes()) {
                if (powerMode.getMode() == mode)
                    return powerMode;
            }

        }

        return null;
    }

    public static Sensor findSensorById(List<Sensor> sensors, String id) {

        for (final Sensor sensor : sensors) {
            if (sensor.getId().equals(id)) {
                return sensor;
            }
        }

        return null;
    }
}
