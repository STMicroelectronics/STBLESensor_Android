package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectTemplate;


import android.app.Dialog;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication.ApplicationViewModel;
import com.st.blesensor.cloud.proprietary.R;

public class DeviceTemplateSelectorFragment extends AppCompatDialogFragment {

    public DeviceTemplateSelectorFragment() { }

    private DeviceTemplateViewModel mViewModel;
    private DeviceTemplateAdapter mDeviceTemplateAdapter;

    private ProgressBar mLoading;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setTitle(R.string.azure_iotCentral_deviceTemplateSelectorLoaderDesc);
        return d;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_device_template_selector, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.azure_iotCentral_deviceTemplateList);
        mDeviceTemplateAdapter = new DeviceTemplateAdapter(template -> mViewModel.selectDeviceTemplate(template));
        recyclerView.setAdapter(mDeviceTemplateAdapter);
        mLoading = root.findViewById(R.id.azure_iotCentral_deviceTemplateLoading);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(DeviceTemplateViewModel.class);
        mViewModel.getDeviceTemplates().observe(getViewLifecycleOwner(), templates -> {
            if(templates==null || templates.length==0)
                return;
            mDeviceTemplateAdapter.showTemplates(templates);
            mLoading.setVisibility(View.INVISIBLE);
            getDialog().setTitle(R.string.azure_iotCentral_deviceTemplateSelectorSelectedDesc);
        });

        ApplicationViewModel applicationViewModel = ViewModelProviders.of(requireActivity()).get(ApplicationViewModel.class);
        applicationViewModel.getSelectedApplication().observe(getViewLifecycleOwner(), application -> {
            if(application!=null)
            mViewModel.loadTemplatesForApp(application);
        });
    }

    private @Nullable DeviceTemplate findTemplateWithId(DeviceTemplate[] templates, String defaultTemplate) {
        for(DeviceTemplate template : templates){
            if(defaultTemplate.equals(template.getId())){
                return template;
            }
        }
        return  null;
    }


    private static class DeviceTemplateAdapter extends RecyclerView.Adapter<DeviceTemplateAdapter.ViewHolder>{

        interface OnTemplateSelectedListener {
            void onTemplateSelected(DeviceTemplate template);
        }

        private DeviceTemplate[] mTemplates = new DeviceTemplate[0];
        private OnTemplateSelectedListener mListener;

        public DeviceTemplateAdapter(@NonNull OnTemplateSelectedListener listener){
            mListener = listener;
        }

        void showTemplates(DeviceTemplate[] templates){
            mTemplates = templates;
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(android.R.layout.simple_list_item_2,parent,false);
            return new ViewHolder(view,mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DeviceTemplate app = mTemplates[position];
            holder.displayTemplate(app);
        }

        @Override
        public int getItemCount() {
            return mTemplates.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder{

            private final TextView mName;
            private final TextView mId;

            private DeviceTemplate mTemplate;

            public ViewHolder(View itemView, OnTemplateSelectedListener mListener) {
                super(itemView);
                mName = itemView.findViewById(android.R.id.text1);
                mId = itemView.findViewById(android.R.id.text2);
                itemView.setOnClickListener(view -> mListener.onTemplateSelected(mTemplate));
            }

            void displayTemplate(DeviceTemplate template){
                mTemplate = template;
                String name = template.getName();
                name = TextUtils.isEmpty(name) ? template.getId() : name;
                mName.setText(name);
                mId.setText(template.getId());
            }
        }
    }

}
