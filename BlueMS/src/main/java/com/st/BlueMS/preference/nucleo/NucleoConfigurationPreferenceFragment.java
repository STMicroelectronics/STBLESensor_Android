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

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;

import java.util.Date;

public class NucleoConfigurationPreferenceFragment extends PreferenceFragmentWithNode {

    private static final String CHANGE_NAME_PREF = "DEVICE_LOCAL_NAME";
    private static final String SYNC_TIME_PREF = "DEVICE_SYNC_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_nucleo_configuration);
    }


    private void disableAllPreferences(){
        PreferenceManager prefs = getPreferenceManager();
        prefs.findPreference(CHANGE_NAME_PREF).setEnabled(false);
        prefs.findPreference(SYNC_TIME_PREF).setEnabled(false);
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

    @Override
    protected void onNodeIsAvailable(Node node) {
        if(node.getDebug()==null) {
            disableAllPreferences();
            return;
        }
        //else
        final NucleoConsole console = new NucleoConsole(node.getDebug());
        setUpSetNamePreference(node.getName(),console);
        setUpSyncTimePreference(console);
    }
}
