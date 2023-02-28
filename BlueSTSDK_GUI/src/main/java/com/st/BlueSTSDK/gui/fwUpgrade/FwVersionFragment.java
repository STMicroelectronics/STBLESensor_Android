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
package com.st.BlueSTSDK.gui.fwUpgrade;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FwVersionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FwVersionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FwVersionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FwVersionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FwVersionFragment newInstance(String param1, String param2) {
        FwVersionFragment fragment = new FwVersionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private TextView mName;
    private TextView mVersion;
    private TextView mMcuType;
    private View mContentView;
    private View mLoadingView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_fw_version, container, false);

        mName = rootView.findViewById(R.id.fwVersion_nameValue);
        mVersion = rootView.findViewById(R.id.fwVersion_versionValue);
        mMcuType = rootView.findViewById(R.id.fwVersion_mcuTypeValue);
        mContentView = rootView.findViewById(R.id.fwVersion_contentView);
        mLoadingView = rootView.findViewById(R.id.fwVersion_loadingView);

        return  rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FwVersionViewModel viewModel =  ViewModelProviders.of(requireActivity()).get(FwVersionViewModel.class);
        viewModel.isWaitingFwVersion().observe(getViewLifecycleOwner(), isLoading -> {
            if(isLoading!=null && !isLoading){
                mLoadingView.setVisibility(View.GONE);
                mContentView.setVisibility(View.VISIBLE);
            }else{
                mLoadingView.setVisibility(View.VISIBLE);
                mContentView.setVisibility(View.GONE);
            }
        });

        viewModel.getFwVersion().observe(getViewLifecycleOwner(), fwVersion -> {
            if(fwVersion==null){
                mName.setText(R.string.fwVersion_unknown);
                mVersion.setText(R.string.fwVersion_unknown);
                mMcuType.setText(R.string.fwVersion_unknown);
            }else{
                mVersion.setText(getString(R.string.fwVersion_versionFormat,
                        fwVersion.getMajorVersion(),fwVersion.getMinorVersion(),fwVersion.getPatchVersion()));
                if(fwVersion instanceof FwVersionBoard) {
                    mName.setText(((FwVersionBoard) fwVersion).getName());
                    mMcuType.setText(((FwVersionBoard) fwVersion).getMcuType());
                }else{
                    mVersion.setText(R.string.fwVersion_unknown);
                    mMcuType.setText(R.string.fwVersion_unknown);
                }
            }
        });
    }
}
