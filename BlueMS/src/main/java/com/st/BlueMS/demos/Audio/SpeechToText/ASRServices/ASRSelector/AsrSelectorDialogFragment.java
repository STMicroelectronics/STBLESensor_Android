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
package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRSelector;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;

import java.util.List;


public class AsrSelectorDialogFragment extends DialogFragment implements  AsrSelectorContract.View {

    private TextView mTitle;
    private RecyclerView mItemList;
    private AsrSelectorContract.Presenter mPresenter;

    public interface AsrSelectorCallback{
        void onAsrEngineSelected(String name, @ASRLanguage.Language int language);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new AsrSelectorPresenter(this);
    }

    private static void setupRecycleView(RecyclerView list){
        Context ctx = list.getContext();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ctx,
                DividerItemDecoration.VERTICAL);
        list.addItemDecoration(dividerItemDecoration);
    }

    private void setTitle(@StringRes int res){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            mTitle.setText(res);
        }else{
            getDialog().setTitle(res);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_select_ars_service,container);

        mTitle = root.findViewById(R.id.asrSelect_title);
        mItemList = root.findViewById(R.id.asrSelector_itemList);
        setupRecycleView(mItemList);
        root.findViewById(R.id.asrSelector_cancel).setOnClickListener((view)->dismiss());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onDisplay();
    }

    @Override
    public void displayAsrServicesListSelector(List<String> asrServices) {
        setTitle(R.string.asrSelect_title_engine);
        RecyclerView.Adapter adapter = new StringListAdapter(asrServices,
                (serviceName)->mPresenter.onServiceSelect(serviceName));
        mItemList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void displayLanguageSelector(@ASRLanguage.Language int[] languages) {
        setTitle(R.string.asrSelect_title_language);
        RecyclerView.Adapter adapter = new LanguageListAdapter(languages,
                (lang)->mPresenter.onLanguageSelect(lang));
        mItemList.setAdapter(adapter);
    }

    @Override
    public void notifySelection(String engineName, int language) {
        if(getActivity() instanceof AsrSelectorCallback){
            ((AsrSelectorCallback)getActivity()).onAsrEngineSelected(engineName,language);
            return;
        }
        if(getParentFragment() instanceof AsrSelectorCallback){
            ((AsrSelectorCallback)getParentFragment()).onAsrEngineSelected(engineName,language);
            return;
        }
        if(getTargetFragment() instanceof AsrSelectorCallback){
            ((AsrSelectorCallback)getParentFragment()).onAsrEngineSelected(engineName,language);
            return;
        }
    }


    private static class StringListAdapter extends RecyclerView.Adapter<StringListAdapter.ViewHolder>{

        public interface OnItemSelectedCallback{
            void onItemSelected(String s);
        }

        private List<String> value;
        private OnItemSelectedCallback callback;

        public StringListAdapter(List<String> value,OnItemSelectedCallback callback){
            this.value=value;
            this.callback = callback;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemName.setText(value.get(position));
        }

        @Override
        public int getItemCount() {
            return value.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView itemName;

            ViewHolder(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener((view)->
                        callback.onItemSelected(itemName.getText().toString()));
            }
        }
    }


    private static class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.ViewHolder>{

        public interface OnItemSelectedCallback{
            void onItemSelected(@ASRLanguage.Language int language);
        }

        private @ASRLanguage.Language int[] value;
        private OnItemSelectedCallback callback;

        public LanguageListAdapter(int[] values, OnItemSelectedCallback callback){
            this.value=values;
            this.callback = callback;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setLanguage(value[position]);
        }

        @Override
        public int getItemCount() {
            return value.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private @ASRLanguage.Language int languageId;
            private TextView itemName;

            ViewHolder(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener((view)->
                        callback.onItemSelected(languageId));
            }

            void setLanguage(@ASRLanguage.Language int lang){
                languageId = lang;
                Context ctx = itemName.getContext();
                String name = ASRLanguage.getLanguage(ctx,languageId);
                itemName.setText(name);
            }
        }
    }
}
