package com.st.BlueMS.demos.aiDataLog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.ArrayAdapter;

import java.util.Arrays;

public class RangeSpinnerAdapter extends ArrayAdapter<String>{

    private float[] values;

    private static float[] createArrayValue(float min, float max, float step){
        int size = (int)Math.ceil((max-min)/step)+1; // +1 to have the max included
        float[] values = new float[size];
        float value = min;
        for (int i = 0; i < size; i++) {
            values[i] = value;
            value+=step;
        }
        return values;
    }

    private static String[] createStringArray(float[] data, String format){
        String[] strings = new String[data.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = String.format(format,data[i]);
        }
        return strings;
    }

    public RangeSpinnerAdapter(@NonNull Context context, @StringRes int valueFormat,
                               float min, float max, float step) {
        super(context, android.R.layout.simple_spinner_item);
        values = createArrayValue(min,max,step);
        String dataFormat = context.getString(valueFormat);
        addAll(createStringArray(values,dataFormat));
    }

    public float getValue(int position){
        return values[position];
    }

    /**
     * return the index of the nearest element in the list
     * @param value value to search
     * @return index to the neartest element in the list
     */
    public int getPosition(float value){
        float distance = Math.abs(value-values[0]);
        int index = 0;
        for (int i = 1; i < values.length; i++) {
            float tempDistance = Math.abs(value-values[i]);
            if(tempDistance<distance){
                distance = tempDistance;
                index = i;
            }
        }
        return index;
    }
}
