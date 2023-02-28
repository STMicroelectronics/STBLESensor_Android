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
package com.st.STM32WB.fwUpgrade.searchOtaNode;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.util.FragmentUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOtaNodeSearchCallback} interface
 * to handle interaction events.
 */
public class SearchOtaNodeFragment extends Fragment implements SearchOtaNodeContract.View {

    private OnOtaNodeSearchCallback mListener;

    private static final String SEARCH_ADDRESS_PARAM = SearchOtaNodeFragment.class.getCanonicalName()+".SEARCH_ADDRESS_PARAM";

    public static SearchOtaNodeFragment instantiate(@Nullable String searchAddress){
        SearchOtaNodeFragment f = new SearchOtaNodeFragment();

        if(searchAddress!=null) {
            Bundle args = new Bundle();
            args.putString(SEARCH_ADDRESS_PARAM, searchAddress);
            f.setArguments(args);
        }

        return f;
    }

    public SearchOtaNodeFragment() {
        // Required empty public constructor
    }

    private TextView mMessage;
    private SearchOtaNodeContract.Presenter mPresenter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_ota_node, container, false);
        mMessage = root.findViewById(R.id.otaSearch_message);
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOtaNodeSearchCallback) {
            mListener = (OnOtaNodeSearchCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOtaNodeSearchCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private @Nullable String getSearchNodeAddress(){
        Bundle args = getArguments();
        if(args==null)
            return null;
        return args.getString(SEARCH_ADDRESS_PARAM,null);
    }

    @Override
    public void onStart() {
        super.onStart();

        String address = getSearchNodeAddress();

        mPresenter = new SearchOtaNodePresenter(this, Manager.getSharedInstance(),requireContext());
        mPresenter.startScan(address);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopScan();
    }


    private void changeMessageText(@StringRes int message){
        FragmentUtil.runOnUiThread(this,()-> mMessage.setText(message));
    }

    @Override
    public void startScan() {
       changeMessageText(R.string.otaSearch_scanStart);
    }

    @Override
    public void foundNode(@NonNull Node node) {
        changeMessageText(R.string.otaSearch_nodeFound);
        mListener.onOtaNodeFound(node);
    }

    @Override
    public void nodeNodeFound() {
        changeMessageText(R.string.otaSearch_nodeNotFound);
    }


    public interface OnOtaNodeSearchCallback {
        void onOtaNodeFound(@NonNull Node node);
    }
}
