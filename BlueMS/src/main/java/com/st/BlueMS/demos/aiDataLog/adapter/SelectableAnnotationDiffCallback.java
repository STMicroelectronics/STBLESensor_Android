package com.st.BlueMS.demos.aiDataLog.adapter;

import android.support.v7.util.DiffUtil;

import com.st.BlueMS.demos.aiDataLog.viewModel.SelectableAnnotation;

import java.util.List;

class SelectableAnnotationDiffCallback extends DiffUtil.Callback {
    private List<SelectableAnnotation> oldList;
    private List<SelectableAnnotation> newList;

    SelectableAnnotationDiffCallback(List<SelectableAnnotation> oldList, List<SelectableAnnotation> newList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        if(oldList == null)
            return 0;
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        if(newList == null)
            return 0;
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).annotation.equals(newList.get(newItemPosition).annotation);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

}
