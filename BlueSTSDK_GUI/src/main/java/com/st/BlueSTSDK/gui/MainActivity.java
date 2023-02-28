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
package com.st.BlueSTSDK.gui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import java.net.URL;

/**
 * Main activity, it will show the ST logo and a button for the about page and one for start the ble
 * scanning
 */
public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * The number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    private static final String SPLASH_SCREEN_WAS_SHOWN = MainActivity.class.getCanonicalName()+"" +
            ".SplashWasShown";
    private static final String PRIVACY_DIALOG_SHOWN = MainActivity.class.getCanonicalName()+".PRIVACY_DIALOG_SHOWN";
    private static final String PRIVACY_DIALOG_SHOWN_TAG = MainActivity.class.getCanonicalName()+".PRIVACY_DIALOG_SHOWN";


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private final Handler mHideHandler = new Handler();

    private View mControlsView;
    private final Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);

        }
    };


    /**
     * build the view to display after the splash screen
     * this method is called during the on create, if you need a different home view overwrite it
     * @param parent view to use as parent during the layout inflation
     * @return view to display after the splash screen
     */
    protected View buildContentView(ViewGroup parent) {
        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.view_main_default_content,parent,true);

        TextView versionText = content.findViewById(R.id.bluestsdk_main_versionText);
        TextView appText = content.findViewById(R.id.bluestsdk_main_appNameText);
        //show the version using the data in the manifest
        String version=null;
        CharSequence appName=null;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            appName = getPackageManager().getApplicationLabel(pInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        if(versionText!=null){
            if(version!=null){
                versionText.append(version);
            }else
                versionText.setText("");
        }

        if(appText!=null && appName!=null){
            appText.setText(appName);
        }

        content.findViewById(R.id.bluestsdk_main_aboutButton).setOnClickListener(this::startAboutActivity);
        content.findViewById(R.id.bluestsdk_main_searchButton).setOnClickListener(this::startScanBleActivity);
        return content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluestsdk_gui_main);
        ViewGroup frame = findViewById(R.id.bluestsdk_main_content_view);

        mControlsView = buildContentView(frame);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        boolean splashWasShown=false;
        if(savedInstanceState!=null){
            splashWasShown = savedInstanceState.getBoolean(SPLASH_SCREEN_WAS_SHOWN,false);
        }//if

        // Trigger the initial showSplashScreen() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if(!splashWasShown) {
            showSplashScreen();
            delayedShowContent(AUTO_HIDE_DELAY_MILLIS);
        }else{
            mControlsView.setVisibility(View.VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(SPLASH_SCREEN_WAS_SHOWN, true);
    }

    private void showSplashScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowContentRunnable);
    }

    /**
     * Schedules a call to showContent() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedShowContent(int delayMillis) {
        mHideHandler.removeCallbacks(mShowContentRunnable);
        mHideHandler.postDelayed(mShowContentRunnable, delayMillis);
    }

    /**
     * function called when the start ble scan button is pressed
     * @param view view pressed
     */
    public void startScanBleActivity(View view){}

    /**
     * function called when the about button is pressed
     * @param view view pressed
     */
    public void startAboutActivity(View view){}

    /**
     * tell witch file is containing the privacy policy, the file content will be shown in the dialog
     * @return raw resource id with the privacy policy
     */
    public URL getPrivacyPolicyUrl(){ return null;}

    private static void setDialogShown(final SharedPreferences prefs, boolean showNextTime){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PRIVACY_DIALOG_SHOWN, showNextTime);
        editor.apply();
    }

    private boolean showPrivacyDialog(){
        final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        return !prefs.contains(PRIVACY_DIALOG_SHOWN) && getPrivacyPolicyUrl()!=null;
    }

    private void displayPrivacyDialog(){
        URL page = getPrivacyPolicyUrl();
        if(page!=null)
            PrivacyDialog.newInstance(page).show(getSupportFragmentManager(),PRIVACY_DIALOG_SHOWN_TAG);

    }

    private boolean privacyDialogIsCurrentlyDisplayed(){
        return getSupportFragmentManager().findFragmentByTag(PRIVACY_DIALOG_SHOWN_TAG)!=null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(showPrivacyDialog() && !privacyDialogIsCurrentlyDisplayed()) {
            displayPrivacyDialog();
        }
    }

    public static class PrivacyDialog extends DialogFragment {

        private static final String PRIVACY_URL_EXTRA = PrivacyDialog.class.getCanonicalName()+".PRIVACY_URL_EXTRA";

        public static DialogFragment newInstance(@NonNull URL privacyPage){
            Bundle param = new Bundle();
            param.putSerializable(PRIVACY_URL_EXTRA,privacyPage);

            DialogFragment dialog = new PrivacyDialog();
            dialog.setArguments(param);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final String urlPage = getArguments().getSerializable(PRIVACY_URL_EXTRA).toString();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());

            dialogBuilder.setTitle(R.string.privacyDialog_title);
            dialogBuilder.setMessage(R.string.privacyDialog_message);
            dialogBuilder.setPositiveButton(R.string.privacyDialog_button, (dialogInterface, i) -> {
                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                openUrl.setData(Uri.parse(urlPage));
                startActivity(openUrl);
                setDialogShown(prefs,false);
            });
            dialogBuilder.setNeutralButton(android.R.string.ok,
                    (dialogInterface, i) -> setDialogShown(prefs,false));
            dialogBuilder.setCancelable(false);

            return dialogBuilder.create();

        }
    }


}
