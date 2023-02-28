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
package com.st.BlueMS.demos.aiDataLog.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.widget.ArrayAdapter;

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
