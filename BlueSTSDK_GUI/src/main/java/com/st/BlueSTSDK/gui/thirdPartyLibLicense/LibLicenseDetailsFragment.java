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


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.util.LoadFileAsyncTask;

/**
 * Fragment that show the license agreement for a specific library
 */
public class LibLicenseDetailsFragment extends Fragment {

    private static final String DETAILS =LibLicenseDetailsFragment.class.getCanonicalName()+".DETAILS";

    static public LibLicenseDetailsFragment newInstance(@NonNull LibLicense details){
        Bundle args = new Bundle();
        args.putParcelable(DETAILS,details);

        LibLicenseDetailsFragment temp = new LibLicenseDetailsFragment();
        temp.setArguments(args);
        return temp;
    }

    public LibLicenseDetailsFragment() {
        // Required empty public constructor
    }

    private TextView mTitle;
    private TextView mLicense;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_lib_licese_details, container, false);
        mTitle = root.findViewById(R.id.libLicense_detailsName);
        mLicense = root.findViewById(R.id.libLicense_detailsLic);

        Bundle args = getArguments();
        if(args!=null && args.containsKey(DETAILS)){
            LibLicense lib = args.getParcelable(DETAILS);
            showDetails(lib);
        }

        return root;
    }

    public void showDetails(LibLicense lib){
        mTitle.setText(lib.name);
        new LoadFileAsyncTask(getResources(),mLicense).execute(lib.licenseFile);
    }

}
