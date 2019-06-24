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
package com.st.BlueMS.demos.aiDataLog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.aiDataLog.viewModel.SelectableAnnotation;

import java.util.List;

public class AnnotationListAdapter extends RecyclerView.Adapter<AnnotationListAdapter.ViewHolder> {

    public interface AnnotationInteractionCallback {
        void onAnnotationSelected(SelectableAnnotation selected);
        void onAnnotationDeselected(SelectableAnnotation deselected);
        void onRemoved(SelectableAnnotation annotation);
    }

    private final LayoutInflater mInflater;
    private List<SelectableAnnotation> mAnnotation; // Cached copy of words
    private AnnotationInteractionCallback mCallback;

    public AnnotationListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.ai_log_annotation_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setAnnotation(mAnnotation.get(position));
    }

    public void setAnnotation(List<SelectableAnnotation> newAnnotation){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SelectableAnnotationDiffCallback(this.mAnnotation,newAnnotation));
        diffResult.dispatchUpdatesTo(this);
        mAnnotation = newAnnotation;
    }

    public void setOnAnnotationInteractionCallback(AnnotationInteractionCallback callback){
        mCallback = callback;
    }

    @Override
    public int getItemCount() {
        if(mAnnotation==null)
            return 0;
        return mAnnotation.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        SelectableAnnotation currentData;

        final CompoundButton annotationSelector;
        final TextView annotationName;

        ViewHolder(View itemView) {
            super(itemView);
            annotationName = itemView.findViewById(R.id.aiLog_annotation_name);
            annotationSelector = itemView.findViewById(R.id.aiLog_annotation_selector);
            annotationSelector.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(mCallback!=null){
                    if(isChecked){
                        mCallback.onAnnotationSelected(currentData);
                    }else{
                        mCallback.onAnnotationDeselected(currentData);
                    }
                }
            });
            itemView.findViewById(R.id.aiLog_annotation_delete).setOnClickListener(v -> {
                if(mCallback!=null){
                    mCallback.onRemoved(currentData);
                }
            });
        }

        void setAnnotation(SelectableAnnotation annotation){
            currentData = annotation;
            annotationName.setText(annotation.annotation.label);
            annotationSelector.setChecked(annotation.isSelected());

        }

    }

}
