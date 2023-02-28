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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.st.BlueSTSDK.gui.R;

import java.util.ArrayList;
import java.util.List;

public class LibLicenseListFragment extends Fragment {

    public interface OnLibLicenseSelected {
        void onSelected(LibLicense lib);
    }

    private LibAdapter mAdapter;
    private RecyclerView mRecyclerView;

    public LibLicenseListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_lib_license_list, container, false);

        mRecyclerView = root.findViewById(R.id.libLicense_libsList);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.HORIZONTAL));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnLibLicenseSelected) {
            mAdapter = new LibAdapter((OnLibLicenseSelected) context);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLibLicenseSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAdapter = null;
        mRecyclerView.setAdapter(null);
    }

    public void setDisplayLibs(List<LibLicense> libs){
        mAdapter.setLibs(libs);
    }

    private static class LibAdapter extends RecyclerView.Adapter<LibAdapter.ViewHolder>{

        private OnLibLicenseSelected mOnSelect;
        private List<LibLicense> mData = new ArrayList<>();

        LibAdapter(OnLibLicenseSelected onSelectItem){
            mOnSelect = onSelectItem;
        }

        @NonNull
        @Override
        public LibAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_lib_license_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LibAdapter.ViewHolder holder, int position) {
            holder.update(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        void setLibs(List<LibLicense> newData){
            mData.clear();
            mData.addAll(newData);
            notifyDataSetChanged();
        }


        class ViewHolder extends RecyclerView.ViewHolder{

            private TextView mLibName;
            private LibLicense mLib;

            public ViewHolder(View itemView) {
                super(itemView);

                mLibName = itemView.findViewById(R.id.libLicense_itemName);
                itemView.setOnClickListener(view -> mOnSelect.onSelected(mLib));
            }

            public void update(LibLicense lib){
                mLib = lib;
                mLibName.setText(mLib.name);
            }
        }
    }


}

