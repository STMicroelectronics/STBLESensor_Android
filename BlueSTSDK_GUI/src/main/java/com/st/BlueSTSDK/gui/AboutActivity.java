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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;

import com.st.BlueSTSDK.gui.thirdPartyLibLicense.LibLicense;
import com.st.BlueSTSDK.gui.thirdPartyLibLicense.LibLicenseActivity;

import java.net.URL;
import java.util.ArrayList;


/**
 * Activity to display with the app info
 */
public class AboutActivity extends AppCompatActivity {
    private static final String ABOUT_PAGE_URL = AboutActivity.class.getCanonicalName()+".ABOUT_PAGE_URL";
    private static final String PRIVACY_RES_ID = AboutActivity.class.getCanonicalName()+".PRIVACY_RES_ID";
    private static final String LIB_LICENSES_INFOS = AboutActivity.class.getCanonicalName()+".LIB_LICENSES_INFOS";
    private static final String OSS_LIB_LICENSES_INFOS = AboutActivity.class.getCanonicalName()+".OSS_LIB_LICENSES_INFOS";

    public static final String BETA_PREFERENCE = AboutActivity.class.getCanonicalName()+".BETA_PREFERENCE";
    public static final String ENABLE_BETA_FUNCTIONALITIES = AboutActivity.class.getCanonicalName()+".ENABLE_BETA_FUNCTIONALITIES";

    private TextView testCatalogDebugDBTextview;
    private TextView betaTextView;
    private int aboutDontUseClicks=0;

    private static void startActivityWithAboutPage(Context c,
                                                   @Nullable String aboutPageUrl,
                                                   @Nullable URL privacyUrl,
                                                   @Nullable ArrayList<LibLicense> usedLibrary,
                                                   @Nullable Intent usedLibraryActivity){
        Intent intent = new Intent(c,AboutActivity.class);
        intent.putExtra(ABOUT_PAGE_URL,aboutPageUrl);
        if(privacyUrl!=null)
            intent.putExtra(PRIVACY_RES_ID,privacyUrl);
        if(usedLibrary!=null)
            intent.putExtra(LIB_LICENSES_INFOS,usedLibrary);
        if(usedLibraryActivity!=null)
            intent.putExtra(OSS_LIB_LICENSES_INFOS,usedLibraryActivity);
        c.startActivity(intent);
    }

    /**
     *  display the activity that will show the about page
     * @param c context where start the activity
     * @param aboutPageUrl html page to show as central content
     * @param privacyUrl resource id of the file containing the privacy settings or null to hide the menu
     * @param usedLibrary list of open source libraries used in the project
     * @deprecated use {@link #startActivityWithAboutPage(Context, String, URL, Intent)}
     */
    @Deprecated
    public static void startActivityWithAboutPage(Context c,
                                                  @Nullable String aboutPageUrl,
                                                  @Nullable URL privacyUrl,
                                                  @Nullable ArrayList<LibLicense> usedLibrary){
        startActivityWithAboutPage(c,aboutPageUrl,privacyUrl,usedLibrary,null);
    }

    /**
     *  display the activity that will show the about page
     * @param c context where start the activity
     * @param aboutPageUrl html page to show as central content
     * @param privacyUrl resource id of the file containing the privacy settings or null to hide the menu
     * @param usedLibraryActivity intent to show the open sources license for the library used in the project
     */
    public static void startActivityWithAboutPage(Context c,
                                                  @Nullable String aboutPageUrl,
                                                  @Nullable URL privacyUrl,
                                                  @Nullable Intent usedLibraryActivity){
        startActivityWithAboutPage(c,aboutPageUrl,privacyUrl,null,usedLibraryActivity);
    }

    public static void startActivityWithAboutPage(Context c,
                                                  @Nullable String aboutPageUrl,
                                                  @Nullable URL privacyUrl){
        startActivityWithAboutPage(c,aboutPageUrl,privacyUrl,null,null);
    }

    public static void startActivityWithAboutPage(Context c,
                                                  @Nullable String aboutPageUrl){
        startActivityWithAboutPage(c,aboutPageUrl,null,null,null);
    }

    // resource where read the privacy policy
    private URL mPrivacyResFile = null;

    private ArrayList<LibLicense> mLicenseInfos=null;

    private @Nullable Intent mOssLicensesMenuActivityClass;


    // set the text view with the app version
    private void setUpAppVersion(){
        TextView tvVersion = findViewById(R.id.about_appVersion);
        try {
            Context appContext = getApplicationContext();
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(getString(R.string.about_version,pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // set the text view with the app name
    private void setUpAppName(){
        TextView tvAppName = findViewById(R.id.about_appName);
        try {
            Context appContext = getApplicationContext();
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(getPackageName(), 0);
            tvAppName.setText(appContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // set the main content with the html page from the user
    private void setUpMainPage(@Nullable String aboutPageUrl){
        if(aboutPageUrl==null)
            return;

        WebView browser = findViewById(R.id.about_webpage);
        browser.setVerticalScrollBarEnabled(false);
        browser.loadUrl(aboutPageUrl);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Bundle extra = getIntent().getExtras();
        mPrivacyResFile = null;
        if(extra!=null) {
            if (extra.containsKey(PRIVACY_RES_ID))
                mPrivacyResFile = (URL) extra.getSerializable(PRIVACY_RES_ID);

            if (extra.containsKey(LIB_LICENSES_INFOS)) {
                mLicenseInfos = extra.getParcelableArrayList(LIB_LICENSES_INFOS);
            }
            setUpMainPage(extra.getString(ABOUT_PAGE_URL));

            mOssLicensesMenuActivityClass = extra.getParcelable(OSS_LIB_LICENSES_INFOS);
        }

        setUpAppName();
        setUpAppVersion();

        testCatalogDebugDBTextview =  findViewById(R.id.about_test_catalog_debug_db);
        testCatalogDebugDBTextview.setOnClickListener(view -> {
            if(testCatalogDebugDBTextview.getCurrentTextColor() == Color.WHITE){
                testCatalogDebugDBTextview.setTextColor(Color.BLACK);
            }else {
                Intent intent = new Intent(this, TestCatalogDebugDB.class);
                startActivity(intent);
            }
        });

        betaTextView = findViewById(R.id.about_beta_indication);

        if(getSharedPreferences(BETA_PREFERENCE, Context.MODE_PRIVATE).getBoolean(ENABLE_BETA_FUNCTIONALITIES,false)) {
            betaTextView.setVisibility(View.VISIBLE);
        } else {
            betaTextView.setVisibility(View.INVISIBLE);
        }

        TextView aboutDontUse = findViewById(R.id.about_dont_use);
        aboutDontUse.setOnClickListener(view -> {
            aboutDontUseClicks++;
            if(aboutDontUseClicks>6) {
                String buttonNegativeText;
                if(getSharedPreferences(BETA_PREFERENCE, Context.MODE_PRIVATE).getBoolean(ENABLE_BETA_FUNCTIONALITIES,false)) {
                    new AlertDialog.Builder(this)
                            .setTitle("The 101 Team")
                            .setMessage("GiovanniV\nMatteoR\nGiuseppeP\nAlbertoV\nLucaP")
                            .setPositiveButton("OK",
                                    (dialog, whichButton) ->{
                                        dialog.dismiss();
                                    }
                            )
                            .setNegativeButton("Stable",
                                    (dialog, whichButton) ->{
                                        getSharedPreferences(BETA_PREFERENCE, Context.MODE_PRIVATE).edit()
                                                .remove(ENABLE_BETA_FUNCTIONALITIES)
                                                .apply();
                                        betaTextView.setVisibility(View.INVISIBLE);
                                        ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(this);
                                        firmwareDB.readDb();
                                        dialog.dismiss();
                                    }
                            )
                            .create()
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("The 101 Team")
                            .setMessage("GiovanniV\nMatteoR\nGiuseppeP\nAlbertoV\nLucaP")
                            .setPositiveButton("OK",
                                    (dialog, whichButton) ->{
                                        dialog.dismiss();
                                    }
                            )
                            .setNegativeButton("Beta",
                                    (dialog, whichButton) ->{
                                        getSharedPreferences(BETA_PREFERENCE, Context.MODE_PRIVATE).edit()
                                                .putBoolean(ENABLE_BETA_FUNCTIONALITIES,true)
                                                .apply();
                                        betaTextView.setVisibility(View.VISIBLE);
                                        ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(this);
                                        firmwareDB.readDbBeta();
                                        dialog.dismiss();
                                    }
                            )
                            .create()
                            .show();
                }

            } else if(aboutDontUseClicks>4) {
                Toast.makeText(this, String.format("Press Again %d Times", 7-aboutDontUseClicks), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_about, menu);

        setUpPrivacyMenu(menu.findItem(R.id.menu_about_show_privacy));
        setUpLibInfoMenu(menu.findItem(R.id.menu_about_show_lib_info));

        return super.onCreateOptionsMenu(menu);
    }

    private void setUpLibInfoMenu(MenuItem item) {
        if((mLicenseInfos == null) && (mOssLicensesMenuActivityClass==null)) {
            item.setVisible(false);
        }
    }

    // hide the privacy menu if the user doesn't pass a privacy page
    private void setUpPrivacyMenu(MenuItem item) {
        if(mPrivacyResFile == null)
            item.setVisible(false);
    }

    // show the privacy policy if the view is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about_show_lib_info){
            if(mLicenseInfos!=null) {
                LibLicenseActivity.startLibLicenseActivity(this, mLicenseInfos);
            } else if(mOssLicensesMenuActivityClass!=null){
                startActivity(mOssLicensesMenuActivityClass);
            }
            return true;
        }

        if (item.getItemId() == R.id.menu_about_show_privacy){
            Intent openUrl = new Intent(Intent.ACTION_VIEW);
            openUrl.setData(Uri.parse(mPrivacyResFile.toString()));
            startActivity(openUrl);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}