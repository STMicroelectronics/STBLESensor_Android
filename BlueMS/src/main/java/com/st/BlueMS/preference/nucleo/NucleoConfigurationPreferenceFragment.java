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
package com.st.BlueMS.preference.nucleo;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.R.layout.simple_spinner_item;

public class NucleoConfigurationPreferenceFragment extends PreferenceFragmentWithNode {

    private static final String CHANGE_NAME_PREF  = "DEVICE_LOCAL_NAME";
    private static final String SYNC_TIME_PREF    = "DEVICE_SYNC_TIME";
    private static final String SYNC_WIFI_PREF    = "DEVICE_WIFI_SEND_CRED";

    private static String selectedSecurity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_nucleo_configuration);
    }


    private void disableAllPreferences(){
        PreferenceManager prefs = getPreferenceManager();
        prefs.findPreference(CHANGE_NAME_PREF).setEnabled(false);
        prefs.findPreference(SYNC_TIME_PREF).setEnabled(false);
        prefs.findPreference(SYNC_WIFI_PREF).setEnabled(false);
    }

    private void setUpSetNamePreference(String currentName, final NucleoConsole console){
        EditTextPreference changeName = (EditTextPreference) getPreferenceManager().findPreference(CHANGE_NAME_PREF);
        changeName.setText(currentName);
        changeName.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue != null) {
                try {
                    console.setName((String) newValue);
                }catch (IllegalArgumentException e) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.pref_local_name_errorTitle)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                    return false;
                }
            }
            return true;
        });
    }

    private void setUpSyncTimePreference(final NucleoConsole console){
        getPreferenceManager().findPreference(SYNC_TIME_PREF)
                .setOnPreferenceClickListener(preference -> {
                    console.setDateAndTime(new Date());
                    Toast.makeText(getActivity(), R.string.pref_sync_time_sent,Toast.LENGTH_SHORT).show();
                    return false;
                });
    }

    private void setUpSyncWifiPreference(final NucleoConsole console) {
        getPreferenceManager().findPreference(SYNC_WIFI_PREF)
                .setOnPreferenceClickListener(preference -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    // Get the layout inflater
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    // Inflate and set the layout for the dialog
                    // Pass null as the parent view because its going in the dialog layout
                    View v = inflater.inflate(R.layout.wifi_credentials,null);


                    List<String>  securityTypeList =  Arrays.asList("OPEN", "WEP", "WPA", "WPA2","WPA/WPA2");
                    Spinner securitySpinner=  v.findViewById(R.id.wifi_security);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), simple_spinner_item, securityTypeList);
                    securitySpinner.setAdapter(dataAdapter);

                    securitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedSecurity = dataAdapter.getItem(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedSecurity = dataAdapter.getItem(0);
                        }
                    });

                    builder.setView(v);

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                    builder.setPositiveButton("Send to Node", (dialog, which) -> {
                                    console.SetWifiCred(((TextView) v.findViewById(R.id.wifi_ssid)).getText().toString(),
                                                        ((TextView) v.findViewById(R.id.wifi_password)).getText().toString(),
                                                        selectedSecurity);
                                    dialog.dismiss();
                                });
                    builder.create();
                    builder.show();
                    return false;
                });
    }

    @Override
    protected void onNodeIsAvailable(Node node) {
        // Disable if there is not the Debug Console
        if(node.getDebug()==null) {
            disableAllPreferences();
            return;
        }
        final NucleoConsole console = new NucleoConsole(node.getDebug());
        setUpSetNamePreference(node.getName(),console);
        setUpSyncTimePreference(console);
        setUpSyncWifiPreference(console);
    }
}
