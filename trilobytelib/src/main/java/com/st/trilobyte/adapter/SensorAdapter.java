package com.st.trilobyte.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.trilobyte.R;
import com.st.trilobyte.helper.SensorHelper;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorAdapter extends BaseAdapter {

    private Context mContext;

    private List<Sensor> mSensors = new ArrayList<>();

    public SensorAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mSensors.size();
    }

    @Override
    public Object getItem(final int i) {
        return mSensors.get(i);
    }

    @Override
    public long getItemId(final int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.sensor_adapter_cell, viewGroup, false);
        }

        Sensor sensor = (Sensor) getItem(i);

        ImageView sensorImage = view.findViewById(R.id.sensor_image);
        int resourceId = mContext.getResources().getIdentifier(sensor.getIcon(), "drawable", mContext.getPackageName());
        sensorImage.setImageResource(resourceId);

        TextView sensorName = view.findViewById(R.id.sensor_name);
        TextView sensorModel = view.findViewById(R.id.sensor_model);

        sensorName.setText(sensor.getDescription());
        sensorModel.setText(sensor.getModel());

        return view;
    }

    public void addSensors(List<Sensor> sensors) {
        mSensors.addAll(sensors);
        notifyDataSetChanged();
    }

    public void clear() {
        mSensors.clear();
    }
}
