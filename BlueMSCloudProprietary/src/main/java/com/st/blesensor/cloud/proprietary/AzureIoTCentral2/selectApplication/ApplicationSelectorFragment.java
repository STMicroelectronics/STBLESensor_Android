package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.selectApplication;

import android.app.Dialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.Application;
import com.st.blesensor.cloud.proprietary.R;


public class ApplicationSelectorFragment extends AppCompatDialogFragment {

    private static final Uri CREATE_APP_URI = Uri.parse("https://apps.azureiotcentral.com/build/new/b7b05921-5b91-4b58-88d8-00ed0e95d3c2");

    public ApplicationSelectorFragment() { }

    private ApplicationViewModel mApplicationViewModel;
    private ApplicationAdapter mAppAdapter;
    private ProgressBar mLoading;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setTitle(R.string.azure_iotCentral_selectAppTitle);
        return d;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_application_selector, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.azure_iotCentral_applicationList);
        mAppAdapter = new ApplicationAdapter(app -> mApplicationViewModel.selectApplication(app));
        recyclerView.setAdapter(mAppAdapter);

        mLoading = root.findViewById(R.id.azure_iotCentral_applicationLoading);

        root.findViewById(R.id.azure_iotCentral_createApplication).setOnClickListener(view -> {
            requireActivity()
                    .startActivity(new Intent(Intent.ACTION_VIEW, CREATE_APP_URI));
            //DialogFragment createApp = new CreateApplicationFragment();
            //createApp.show(getChildFragmentManager(),"createAppTag");
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mApplicationViewModel = ViewModelProviders.of(requireActivity()).get(ApplicationViewModel.class);
        mApplicationViewModel.getApplications().observe(getViewLifecycleOwner(), applications -> {
            if(applications==null)
                return;
            mAppAdapter.showApplications(applications);
            mLoading.setVisibility(View.INVISIBLE);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mApplicationViewModel.loadApps(true);
    }

    private static class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder>{

        interface  OnApplicationSelectedListener{
            void onApplicationSelected(Application app);
        }

        private Application[] mApplications = new Application[0];
        private OnApplicationSelectedListener mListener;

        public ApplicationAdapter(OnApplicationSelectedListener listener){
            mListener = listener;
        }

        void showApplications(Application[] apps){
            mApplications = apps;
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new ViewHolder(view,mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Application app = mApplications[position];
            holder.displayApplication(app);
        }

        @Override
        public int getItemCount() {
            return mApplications.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder{

            private final TextView mName;

            private Application mApplication;

            ViewHolder(View itemView, OnApplicationSelectedListener mListener) {
                super(itemView);
                mName = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener(view -> mListener.onApplicationSelected(mApplication));
            }

            void displayApplication(Application application){
                mApplication = application;
                mName.setText(application.getName());
            }
        }
    }
}
