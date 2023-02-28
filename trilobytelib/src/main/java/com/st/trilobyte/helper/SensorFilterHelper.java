package com.st.trilobyte.helper;

import android.content.Context;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.models.Filter;
import com.st.trilobyte.models.FilterHolder;
import com.st.trilobyte.models.PowerMode;
import com.st.trilobyte.models.SensorFilter;

import java.util.List;

public class SensorFilterHelper {

    public static boolean hasFilter(Context context, String sensorId, Node.Type board) {

        List<SensorFilter> filters = RawResHelperKt.getSensorFilterList(context,board);

        if (filters != null) {
            for (SensorFilter sensorFilter : filters) {
                if (sensorFilter.getSensorId().equals(sensorId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static SensorFilter getSensorFiltersBySensorId(Context context, String sensorId, Node.Type board) {

        List<SensorFilter> filters = RawResHelperKt.getSensorFilterList(context,board);

        if (filters != null) {
            for (SensorFilter sensorFilter : filters) {
                if (sensorFilter.getSensorId().equals(sensorId)) {
                    return sensorFilter;
                }
            }
        }

        return null;
    }

    public static Filter getAvailableFilter(SensorFilter sensorFilter, PowerMode.Mode powerMode, double odr) {

        if (powerMode == PowerMode.Mode.NONE) {
            return sensorFilter.getValues().get(0).getFilters().get(0);
        }

        for (FilterHolder filterHolder : sensorFilter.getValues()) {
            if (filterHolder.getPowerModes().contains(powerMode)) {
                for (Filter filter : filterHolder.getFilters()) {
                    if (filter.getOdrs().contains(odr)) {
                        return filter;
                    }
                }
            }
        }

        return null;
    }
}
