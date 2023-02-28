package com.st.trilobyte.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.trilobyte.R;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class FlowsAdapter extends BaseAdapter {

    private List<Flow> mFlowList = new ArrayList<>();

    private FlowActionListener listener;

    public FlowsAdapter(final FlowActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return mFlowList.size();
    }

    @Override
    public Object getItem(final int i) {
        return mFlowList.get(i);
    }

    @Override
    public long getItemId(final int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

        Context context = viewGroup.getContext();

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.flow_adapter_cell, viewGroup, false);
        }

        final Flow flow = (Flow) getItem(i);

        ImageView outputImageview = view.findViewById(R.id.output_imageview);
        String icon = flow.getOutputs().size() == 1 ? flow.getOutputs().get(0).getIcon() : "ic_multi_output";
        int resourceId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
        outputImageview.setImageResource(resourceId);

        TextView flowName = view.findViewById(R.id.flow_name_textview);
        flowName.setText(flow.getDescription());

        view.findViewById(R.id.upload_imageview).setVisibility(flow.canBeUploaded() ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.upload_imageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    if(flow.getFile()!=null) {
                        //Inputs
                        for (int i = 0; i < flow.getSensors().size(); i++) {
                            Sensor sensor = flow.getSensors().get(i);
                        }
                        //Functions
                        for (int i = 0; i < flow.getFunctions().size(); i++) {
                            Function function = flow.getFunctions().get(i);
                        }

                        //Outputs
                        for (int i = 0; i < flow.getOutputs().size(); i++) {
                            Output output = flow.getOutputs().get(i);
                        }

                    }
                    listener.upload(flow);
                }
            }
        });

        view.findViewById(R.id.delete_imageview).setVisibility(flow.getFile() != null ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.delete_imageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.delete(flow);
                }
            }
        });

        return view;
    }

    public void addFlows(List<Flow> sensors) {
        mFlowList.addAll(sensors);
        notifyDataSetChanged();
    }

    public void clear() {
        mFlowList.clear();
    }

    // Listener

    public interface FlowActionListener {
        void upload(Flow flow);

        void delete(Flow flow);
    }
}
