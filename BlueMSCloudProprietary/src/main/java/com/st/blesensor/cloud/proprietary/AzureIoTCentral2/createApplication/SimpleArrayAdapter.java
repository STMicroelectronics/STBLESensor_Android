package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.createApplication;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public abstract class SimpleArrayAdapter<T> extends ArrayAdapter<T> {

    public SimpleArrayAdapter(@NonNull Context context, @NonNull T[] objects) {
        super(context, android.R.layout.simple_spinner_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item,parent,false);
        }
        TextView label = convertView.findViewById(android.R.id.text1);
        T data = getItem(position);
        bindObject(label,data);
        return  convertView;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position,convertView,parent);
    }

    protected abstract void bindObject(TextView view, T obj);
}
