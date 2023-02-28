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

package com.st.BlueSTSDK.gui.thirdPartyLibLicense;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.st.BlueSTSDK.gui.R;

import java.util.ArrayList;
import java.util.List;

public class LibLicenseActivity extends AppCompatActivity implements LibLicenseContract.View,
        LibLicenseListFragment.OnLibLicenseSelected {

    private static final String LIBS = LibLicenseActivity.class.getCanonicalName()+".Libs";

    @VisibleForTesting
    public static Intent getStartLibLicenseActivityIntent(Context c, ArrayList<LibLicense> libs){
        Intent intent = new Intent(c , LibLicenseActivity.class);
        intent.putExtra(LIBS,libs);
        return intent;
    }

    public static void startLibLicenseActivity(Context c, ArrayList<LibLicense> libs){
        c.startActivity(getStartLibLicenseActivityIntent(c,libs));
    }

    private LibLicenseContract.Presenter mPresenter;
    private LibLicenseListFragment mLibListView;
    private LibLicenseDetailsFragment mDetailsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<LibLicense> libs = getIntent().getParcelableArrayListExtra(LIBS);
        mPresenter = new LibLicensePresenter(this,libs);

        setContentView(R.layout.activity_lib_license);

        if(savedInstanceState==null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            mLibListView = new LibLicenseListFragment();
            transaction.add(R.id.libLicense_fragmentListView,mLibListView);

            if(findViewById(R.id.libLicense_fragmentDetails)!=null) {
                mDetailsView = new LibLicenseDetailsFragment();
                transaction.add(R.id.libLicense_fragmentDetails, mDetailsView);
            }

            transaction.commit();
        }else{
            FragmentManager fragmentMng = getFragmentManager();
            mLibListView = (LibLicenseListFragment) fragmentMng.findFragmentById(R.id.libLicense_fragmentListView);
            mDetailsView = (LibLicenseDetailsFragment) fragmentMng.findFragmentById(R.id.libLicense_fragmentDetails);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onListViewIsDisplayed();
    }

    @Override
    public void displayLibraries(@NonNull List<LibLicense> libs) {
        mLibListView.setDisplayLibs(libs);
    }

    @Override
    public void displayDetails(@NonNull LibLicense lib) {

        if(mDetailsView==null) {
            LibLicenseDetailsActivity.startLicenseDetailActivity(this, lib);
        }else {
            mDetailsView.showDetails(lib);
        }

    }

    @Override
    public void onSelected(LibLicense lib) {
        mPresenter.onLibSelected(lib);
    }

}

