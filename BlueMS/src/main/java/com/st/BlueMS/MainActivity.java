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

package com.st.BlueMS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.AboutActivity;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;
import com.st.trilobyte.ui.DashboardActivity;
import com.st.trilobyte.ui.TrilobyteActivity;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.ArrayList;

/**
 * Entry point activity, it show a full screen image for 2s and than the button for start the
 * scan or go in the privacy_policy/help activity
 */
public class MainActivity extends com.st.BlueSTSDK.gui.MainActivity {

    private static final String PRIVACY_PAGE_URL = "http://www.st.com/content/st_com/en/common/privacy-policy.html";
    private static final String ABOUT_PAGE_URL = "file:///android_asset/about.html";

    private static final String CONF_PREFERENCE = MainActivity.class.getCanonicalName();
    private static final String DARK_MODE_DECISION = CONF_PREFERENCE+".DARK_MODE_DECISION";

    private TextView themeText=null;
    private TextView betaText;

    protected View buildContentView(ViewGroup parent) {

        Log.i("Etna","Allocation from MainActivity");

        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.activity_main,parent);

        TextView versionText = content.findViewById(R.id.main_versionText);
        TextView appText = content.findViewById(R.id.main_appNameText);

        betaText = content.findViewById(R.id.main_appNameTextBeta);

        themeText = content.findViewById(R.id.main_changeTheme);

        //Apply the user defined theme
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final SharedPreferences prefs = getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE);
            int NightMode = prefs.getInt(DARK_MODE_DECISION, 2);
            changeNightMode(NightMode);
        }

        themeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogThemeSelection();
            }
        });

        //show the version using the data in the manifest
        String version=null;
        int verCode=0;
        CharSequence appName=null;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            verCode = pInfo.versionCode;
            appName = getPackageManager().getApplicationLabel(pInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        if(versionText!=null){
            if(version!=null){
                versionText.append(version);
                versionText.append(" ("+verCode+")");
            }else
                versionText.setText("");
        }

        if(appText!=null && appName!=null){
            appText.setText(appName);
        }

        content.findViewById(R.id.main_aboutButton)
                .setOnClickListener(this::startAboutActivity);
        content.findViewById(R.id.main_searchButton)
                .setOnClickListener(this::startScanBleActivity);
        content.findViewById(R.id.main_flowButton)
                .setOnClickListener(this::startFlowActivity);
        return content;
    }

    void showDialogThemeSelection() {
        ArrayList<String> themes = new ArrayList<String>();
        themes.add("Light Theme");
        themes.add("Dark Theme");

        //Remove the System theme on Device<Q
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q) {
            themes.add("System Theme");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Context context = this;
        builder.setTitle("Select the Application Theme")
                .setItems(themes.toArray(new String[themes.size()]), (dialog, which) -> {
                    //Save the User Decision
                    getSharedPreferences(CONF_PREFERENCE, Context.MODE_PRIVATE).edit()
                            .putInt(DARK_MODE_DECISION,which)
                            .apply();
                    //Apply the User Decision
                    changeNightMode(which);
                })
                .create()
                .show();
    }

    //Change the Night Mode only if's necessary
    private void changeNightMode(int nightMode) {
        //Take the current darkMode
        int currentDarkMode = AppCompatDelegate.getDefaultNightMode();
        switch (nightMode) {
            case 0:
                if (currentDarkMode != AppCompatDelegate.MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                themeText.setText(R.string.change_to_dark_theme);
                break;
            case 1:
                if (currentDarkMode != AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                themeText.setText(R.string.change_to_ligth_theme);
                break;
            case 2:
                if (currentDarkMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                themeText.setText(R.string.choose_app_theme);
                break;
        }
    }

    @Override
    public void startScanBleActivity(View view) {
        startActivity(new Intent(this, NodeListActivity.class));
    }

    public void directConnectToNode(String nodeAddress) {
        Intent intent = new Intent(this, NodeListActivity.class);
        intent.putExtra(DashboardActivity.FINISH_ACTIVITY_NODE_ADDRESS,nodeAddress);
        startActivity(intent);
    }

    @Override
    public void startAboutActivity(View view) {
        URL privacyPage = getPrivacyPolicyUrl();
        Intent licenseIntent = new Intent(this, OssLicensesMenuActivity.class);
        AboutActivity.startActivityWithAboutPage(this,ABOUT_PAGE_URL,privacyPage,licenseIntent);
    }

    private final static int START_FLOW_REQ_CODE = 100;


    private void startCreateNewAppActivity(Node.Type board){
        startActivityForResult(DashboardActivity.getStartDashboardActivityIntent(this,board), START_FLOW_REQ_CODE);
    }


    private void startFlowActivity(final View view) {
        if(newAppWarningsDialogNeedsToBeShown()) {
            displayNewAppWarnings();
        }else {
            selectSensorTileBoxBoardType();
        }
    }

    private void selectSensorTileBoxBoardType() {
        // Beta Enabled/Not Enabled
        final SharedPreferences prefs = getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE);
        boolean betaFunctionalities = prefs.getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES, false);

        if(betaFunctionalities) {
            //The SensorTile.Box-Pro is not yet public
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.STRoundedDialog);
            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.dialog_select_sensortilebox_board, null);
            CardView selectSensorTileBox = dialogView.findViewById(R.id.select_sensortilebox_board);
            CardView selectSensorTileBoxPro = dialogView.findViewById(R.id.select_sensortilebox_pro_board);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            selectSensorTileBox.setOnClickListener(v -> {
                dialog.dismiss();
                Node.Type board = Node.Type.SENSOR_TILE_BOX;
                startCreateNewAppActivity(board);
            });

            selectSensorTileBoxPro.setOnClickListener(v -> {
                dialog.dismiss();
                Node.Type board = Node.Type.SENSOR_TILE_BOX_PRO;
                startCreateNewAppActivity(board);
            });
        } else {
            startCreateNewAppActivity(Node.Type.SENSOR_TILE_BOX);
        }
    }

    private static final String NEW_APP_DIALOG_SHOWN = MainActivity.class.getCanonicalName()+".NEW_APP_DIALOG_SHOWN";
    private static final String NEW_APP_DIALOG_SHOWN_TAG = MainActivity.class.getCanonicalName()+".NEW_APP_DIALOG_SHOWN_TAG";

    private boolean newAppWarningsDialogNeedsToBeShown(){
        final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        return !prefs.contains(NEW_APP_DIALOG_SHOWN);
    }

    private void displayNewAppWarnings() {
        SimpleFragmentDialog dialog;
        final SharedPreferences prefs = getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE);
        boolean betaFunctionalities = prefs.getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES, false);

        if(betaFunctionalities) {
            dialog = SimpleFragmentDialog.newInstance(R.string.new_app_dialog_title,R.string.new_app_dialog_content_beta);
        } else {
            dialog = SimpleFragmentDialog.newInstance(R.string.new_app_dialog_title,R.string.new_app_dialog_content);
        }

            dialog.show(getSupportFragmentManager(),NEW_APP_DIALOG_SHOWN_TAG);
            dialog.setOnclickListener((dialogInterface, i) -> {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(NEW_APP_DIALOG_SHOWN,true)
                        .apply();
                selectSensorTileBoxBoardType();
            });
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_FLOW_REQ_CODE) {
            if (resultCode == DashboardActivity.FLOW_UPDATED_STATUS_CODE) {
                //startScanBleActivity(null);
                String nodeAddress = data.getStringExtra(DashboardActivity.FINISH_ACTIVITY_NODE_ADDRESS);
                if(nodeAddress==null) {
                    startScanBleActivity(null);
                } else {
                    directConnectToNode(nodeAddress);
                }
            }
        }
    }

    @Override
    public URL getPrivacyPolicyUrl(){
        try {
            return new URL(PRIVACY_PAGE_URL);
        } catch (MalformedURLException e) {
            return null;
        }//try-catch
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE);
        if(prefs.getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES, false)) {
            betaText.setVisibility(View.VISIBLE);
        } else {
            betaText.setVisibility(View.GONE);
        }
    }

}
