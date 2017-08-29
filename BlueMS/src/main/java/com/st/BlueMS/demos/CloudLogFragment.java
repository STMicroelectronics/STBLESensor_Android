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

package com.st.BlueMS.demos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueMS.demos.Cloud.GenericMqtt.GenericMqttConfigurationFactory;
import com.st.BlueMS.demos.Cloud.IBMWatson.IBMWatsonConfigFactory;
import com.st.BlueMS.demos.Cloud.IBMWatson.IBMWatsonQuickStartConfigFactory;
import com.st.BlueMS.demos.Cloud.util.MqttClientConfigAdapter;
import com.st.BlueMS.demos.Cloud.MqttClientConfigurationFactory;
import com.st.BlueMS.demos.util.DemoWithNetFragment;
import com.st.BlueMS.demos.util.FeatureListViewAdapter;
import com.st.BlueMS.demos.util.FeatureListViewAdapter.OnFeatureSelectChange;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sent the feature data to a cloud provider
 */
@DemoDescriptionAnnotation(name = "Cloud Logging", requareAll = {},
        iconRes = R.drawable.ic_cloud_upload_24dp)
public class CloudLogFragment extends DemoWithNetFragment {


    private static List<MqttClientConfigurationFactory> CLOUD_PROVIDER =  Arrays.asList(
            new IBMWatsonQuickStartConfigFactory(),
            new IBMWatsonConfigFactory(),
            new GenericMqttConfigurationFactory());

    /**
     * Node use for the demo
     */
    private Node mNode;
    /**
     * button for start/stop a cloud connection
     */
    private ImageButton mStartLogButton;
    /**
     * spinner used for select the cloud provider
     */
    private Spinner mCloudClientSpinner;

    /**
     * Button for show/hide the connection parameters
     */
    private Button mShowDetailsButton;

    /**
     * view that contains the feature view list
     */
    private View mFeatureListViewContainer;

    /**
     * list of avialable feature in the node
     */
    private RecyclerView mFeatureListView;
    /**
     * view where load the configuration gui for the selecte cloud provider
     */
    private FrameLayout mCloudConfig;

    /**
     * object used for build the cloud connection
     */
    private MqttClientConnectionFactory mCloudConnectionFactory;

    /**
     * mqtt connection
     */
    private @Nullable MqttAndroidClient mMqttClient;

    /**
     * object to use for send the data to the cloud
     */
    private Feature.FeatureListener mCloudLogListener;

    /**
     * snackbar used for display the page with the data
     */
    private Button mDataPageLink;

    /**
     * listener that close the cloud connection when the node lost the connection
     */
    private Node.NodeStateListener mCloseConnectionOnDisconnection = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if (prevState == Node.State.Connected) {
                closeCloudConnection();
                node.removeNodeStateListener(this);
            }
        }
    };

    /**
     * Enable/disable the notification for a feature when its state change form the feature list
     */
    private OnFeatureSelectChange mSendDataToCloudWhenSelected = new OnFeatureSelectChange() {
        @Override
        public void onFeatureSelect(Feature f) {
            if (mCloudLogListener != null) {
                f.addFeatureListener(mCloudLogListener);
                mNode.enableNotification(f);
            }
        }

        @Override
        public void onFeatureDeSelect(Feature f) {
            if (mCloudLogListener != null) {
                f.removeFeatureListener(mCloudLogListener);
                mNode.disableNotification(f);
            }
        }
    };

    public CloudLogFragment() {
        // Required empty public constructor
    }

    /**
     * build an dialog for display the error during the connection
     * @param c context to use for build the dialog
     * @param message error to show
     * @return dialog to display with the error message
     */
    private static AlertDialog buildMqttErrorDialog(Context c, String message) {
        return new AlertDialog.Builder(c)
                .setIcon(R.drawable.ic_alert)
                .setTitle(R.string.cloudLog_mqttErrorTitle)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //save the fragment state when the activity is destroyed
        setRetainInstance(true);
    }


    private List<Feature> getSupportedFeatures(List<Feature> availableFeature){
        ArrayList<Feature> supprtedFeatures = new ArrayList<>(availableFeature.size());
        //fiter availabe feature with  mCloudConnectionFactory.supportFeature
        for(Feature f: availableFeature){
            if(mCloudConnectionFactory.supportFeature(f)){
                supprtedFeatures.add(f);
            }
        }

        return supprtedFeatures;


    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mNode = node;

        if (isCloudConnected()) {
            showConnectedView();
        } else
            showDisconnectedView();
    }

    /**
     * utility function that return the selected MqttClientConfigurationFactory from the spiner
     * @return current selection form the spinner
     */
    private MqttClientConfigurationFactory getSelectCloud() {
        return (MqttClientConfigurationFactory) mCloudClientSpinner.getSelectedItem();
    }

    /**
     * this will stop the  notification only the cloud connection is closed, this for permit to
     * send data also if the activity is in background (becouse we are opening the browser)
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (!isCloudConnected()) {
            stopAllNotification();
        } else {
            Toast.makeText(getActivity(), R.string.cloudLog_warningConnectionOn, Toast.LENGTH_SHORT).show();
            node.addNodeStateListener(mCloseConnectionOnDisconnection);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_cloud_log, container, false);
        mFeatureListView = (RecyclerView) root.findViewById(R.id.cloudLogFeatureList);
        mFeatureListViewContainer = root.findViewById(R.id.cloudLogFeatureListContainer);

        mStartLogButton = (ImageButton) root.findViewById(R.id.startCloudLog);
        setUpStartLogButton(mStartLogButton);

        mCloudClientSpinner = (Spinner) root.findViewById(R.id.cloudProviderSpinner);
        setUpCloudClientSpinner(mCloudClientSpinner,getNode(), savedInstanceState);
        mCloudConfig = (FrameLayout) root.findViewById(R.id.cloudProviderConfigView);

        mDataPageLink = (Button) root.findViewById(R.id.openCloudPageButton);
        setUpDataPageLink(mDataPageLink);

        mShowDetailsButton = (Button) root.findViewById(R.id.showDetailsButton);
        setUpDetailsButton(mShowDetailsButton);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected",mCloudClientSpinner.getSelectedItemPosition());

    }

    private void restoreCloudClientParamState(Bundle savedInstanceState) {
        int selected = savedInstanceState.getInt("selected");
        mCloudClientSpinner.setSelection(selected);
    }


    private void setUpDetailsButton(Button showDetailsButton) {
        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEnableState(mCloudConfig,false);
                if(mCloudConfig.getVisibility()==View.VISIBLE){
                    mCloudConfig.setVisibility(View.GONE);
                }else
                    mCloudConfig.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpDataPageLink(Button dataPageLink) {
        /*
        dataPageLink.setOnClickListener(view -> {
            Uri page = mCloudConnectionFactory.getDataPage();
            if(page!=null)
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, page));
        });
        */
        dataPageLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri page = mCloudConnectionFactory.getDataPage();
                if(page!=null)
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, page));
            }
        });
        dataPageLink.setVisibility(View.GONE);
    }


    /**
     * atach the adapter to the spinner and set the selectedListener
     * @param spinner spinner to set up
     */
    private void setUpCloudClientSpinner(Spinner spinner, @Nullable final Node node, @Nullable Bundle savedState){
        spinner.setAdapter(new MqttClientConfigAdapter(getActivity(),CLOUD_PROVIDER));

        /**
         * remove the previous view and add the new views for configure the selected service
         */
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCloudConfig.removeAllViews();
                MqttClientConfigurationFactory selectedFactory =
                        (MqttClientConfigurationFactory) adapterView.getItemAtPosition(i);
                selectedFactory.attachParameterConfiguration(getActivity(), mCloudConfig);
                selectedFactory.loadDefaultParameters(node);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mCloudConfig.removeAllViews();
            }
        });

         if(savedState!=null){
             restoreCloudClientParamState(savedState);
         }
    }


    private void setUpStartLogButton(ImageButton button) {
        //button.setOnClickListener(this::onStartStopCloudLogClick);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartStopCloudLogClick(view);
            }
        });
        if (isOnline()) {
            enableCloudButton();
        }
    }

    /**
     * true if we have a connection with the cloud service
     * @return true if the connection with the cloud service is open, false otherwise
     */
    private boolean isCloudConnected() {
        return (mMqttClient != null && mMqttClient.isConnected());
    }


    /**
     * set up the fragment views to display when the connection is open
     */
    private void showConnectedView() {
        mStartLogButton.setImageResource(R.drawable.ic_cloud_upload_stop_24dp);
        disableCloudConfiguration();
        mShowDetailsButton.setVisibility(View.VISIBLE);
        mCloudConfig.setVisibility(View.GONE);
        mFeatureListView.setVisibility(View.VISIBLE);
        mFeatureListView.setAdapter(new FeatureListViewAdapter(getSupportedFeatures(mNode.getFeatures()),
                mSendDataToCloudWhenSelected));
        if(mCloudConnectionFactory.getDataPage()!=null)
            mDataPageLink.setVisibility(View.VISIBLE);

    }

    /**
     * set up the fragment views to dipsplay when the connection is closed
     */
    private void showDisconnectedView() {
        mStartLogButton.setImageResource(R.drawable.ic_cloud_upload_24dp);
        enableCloudConfiguration();
        mFeatureListView.setVisibility(View.GONE);
        mDataPageLink.setVisibility(View.GONE);
        mShowDetailsButton.setVisibility(View.GONE);
        mCloudConfig.setVisibility(View.VISIBLE);
    }

    /**
     * open a connection to the selected cloud service
     */
    private void startCloudConnection() {
        if(isCloudConnected()) {
            return;
        }

        try {
            mCloudConnectionFactory = getSelectCloud().getConnectionFactory();
        }catch (IllegalArgumentException e){
            buildMqttErrorDialog(CloudLogFragment.this.getActivity(), e.getMessage()).show();
            Log.e(getClass().getName(),"Error: "+e.getMessage());
            return;
        }

        try {
            mMqttClient = mCloudConnectionFactory.createClient(getActivity());
            mCloudLogListener = mCloudConnectionFactory.getFeatureListener(mMqttClient);
            mCloudConnectionFactory.connect(getActivity(), mMqttClient, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    showConnectedView();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    buildMqttErrorDialog(CloudLogFragment.this.getActivity(), exception.getMessage()).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            buildMqttErrorDialog(getActivity(), e.getMessage()).show();
        }
    }

    /**
     * remove the resource used by the mqtt client
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMqttClient != null) {
            mMqttClient.close();
            mMqttClient.unregisterResources();
        }
    }

    private void stopAllNotification() {
        for (Feature f : mNode.getFeatures()) {
            if (mNode.isEnableNotification(f)) {
                f.removeFeatureListener(mCloudLogListener);
                mNode.disableNotification(f);
            }//if enabled
        }//for

        //update the gui for show our changes
        RecyclerView.Adapter a = mFeatureListView.getAdapter();
        if (a != null)
            a.notifyDataSetChanged();
    }//stopAllNotification

    /**
     * close the cloud connection
     */
    private void closeCloudConnection() {
        if (isCloudConnected()) {
            mCloudLogListener = null;
            if(mMqttClient!=null) {
                try {
                    mMqttClient.disconnect();
                } catch (MqttException e) {
                    buildMqttErrorDialog(getActivity(), e.getMessage()).show();
                }//try-catch
            }
        }//if
    }//closeCloudConnection

    /**
     * function called when the connection button is pressed
     * @param v button pressed
     */
    public void onStartStopCloudLogClick(View v) {
        if (!isCloudConnected()) {
            startCloudConnection();
        } else {
            closeCloudConnection();
            stopAllNotification();
            showDisconnectedView();
        }//if-else
    }

    /**
     * utility function for change the state of a view and its child
     * @param v view to change
     * @param enabled new state
     */
    private static void changeEnableState(View v, boolean enabled) {
        if (v instanceof ViewGroup) { //if it has child, change the child
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                changeEnableState(vg.getChildAt(i), enabled);
            }
        }
        v.setEnabled(enabled);
    }

    @Override
    protected void onSystemLostConnectivity() {
        super.onSystemLostConnectivity();
        stopAllNotification();
        showDisconnectedView();
        disableCloudButton();
    }

    @Override
    protected void onSystemHasConnectivity() {
        super.onSystemHasConnectivity();
        enableCloudButton();
    }


    private void disableCloudConfiguration() {
        mCloudClientSpinner.setEnabled(false);
        changeEnableState(mCloudConfig, false);
        mFeatureListViewContainer.setVisibility(View.VISIBLE);
    }

    private void enableCloudConfiguration() {
        mCloudClientSpinner.setEnabled(true);
        changeEnableState(mCloudConfig, true);
        mFeatureListViewContainer.setVisibility(View.GONE);

    }

    private void enableCloudButton() {
        //only the first time change the icon
        if(!mStartLogButton.isEnabled()) {
            mStartLogButton.setEnabled(true);
            mStartLogButton.setImageResource(R.drawable.ic_cloud_upload_24dp);
        }
    }

    private void disableCloudButton() {
        mStartLogButton.setEnabled(false);
        mStartLogButton.setImageResource(R.drawable.ic_cloud_offline_24dp);
    }

}