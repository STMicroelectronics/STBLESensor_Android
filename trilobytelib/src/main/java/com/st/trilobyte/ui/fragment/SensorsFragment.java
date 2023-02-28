package com.st.trilobyte.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.adapter.SensorAdapter;
import com.st.trilobyte.helper.RawResHelperKt;
import com.st.trilobyte.models.Sensor;
import com.st.trilobyte.ui.SensorDetailActivity;

public class SensorsFragment extends Fragment {

    private SensorAdapter mAdapter;

    private Node.Type mBoard;

    public static SensorsFragment getInstance(Node.Type board) {
        SensorsFragment fragment = new SensorsFragment();
        fragment.setBoardType(board);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarTitle = view.findViewById(R.id.actionbar_text);
        actionbarTitle.setText(getString(R.string.sensors));

        ListView listView = view.findViewById(R.id.sensors_list);
        mAdapter = new SensorAdapter(getContext());
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                Sensor sensor = (Sensor) mAdapter.getItem(i);
                openSensorDetail(sensor);
            }
        });

        fillSensorList(mBoard);
    }

    public void setBoardType(final Node.Type board) {
        mBoard = board;
    }

    private void fillSensorList(Node.Type board) {
        mAdapter.clear();
        mAdapter.addSensors(RawResHelperKt.getSensorList(getContext(),board));
        mAdapter.notifyDataSetChanged();
    }

    private void openSensorDetail(Sensor sensor) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SensorDetailActivity.EXTRA_SENSOR_KEY, sensor);
        bundle.putSerializable(SensorDetailActivity.EXTRA_BOARD_TYPE, mBoard);
        Intent intent = new Intent(getContext(), SensorDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
