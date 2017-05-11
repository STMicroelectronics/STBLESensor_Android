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

package com.st.BlueMS.demos.Cloud.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.st.BlueMS.demos.Cloud.MqttClientConfigurationFactory;

import java.util.List;

/**
 * Adapter that show a list MqttClientConfigurationFactory, for each factory the name will be displayed
 */
public class MqttClientConfigAdapter extends ArrayAdapter<MqttClientConfigurationFactory> {

    /**
     * create an adapter
     * @param c context to use for load the data
     * @param data list of factory that the adapter will manage
     */
    public MqttClientConfigAdapter(Context c, List<MqttClientConfigurationFactory> data) {
        super(c, android.R.layout.simple_list_item_1, data);
    }

    //the spinner will use this value for show the possible value that can be selected
    @Override
    public View getDropDownView(int position, View v, ViewGroup parent) {
        return getView(position, v, parent);
    }

    /**
     * create a view for a specific MqttClient
     *
     * @param position client position
     * @param v        if not null we can use this view instead of create create a new one
     * @param parent   view where the class will insert the view that we create
     * @return view that represent the client
     */
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }//else

        MqttClientConfigurationFactory item = getItem(position);
        if (item != null) ((TextView) v).setText(item.getName());

        return v;
    }
}
