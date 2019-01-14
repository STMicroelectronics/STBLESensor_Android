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
