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

package com.st.BlueMS.demos.NodeStatus;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

import static com.st.BlueMS.demos.NodeStatus.BatteryInfoParser.Info;

/**
 * Dialog that interrogate the node to retreive the advanced battery info
 */
public class BatteryInfoDialogFragment extends DialogFragment {

    private static final String BATTERY_INFOS = BatteryInfoDialogFragment.class.getName()+".BATTERY_INFOS";

    private static final String NOTE_TAG = BatteryInfoDialogFragment.class.getName()+".NODE_TAG";
    private static final long COMMAND_TIMEOUT=1000;
    public static final String BATTERY_INFO_COMMAND = "batteryinfo";
    private View mWaitLayout;
    private RecyclerView mInfoList;


    /**
     * create the dialog that will show the battery information
     * @param node node to interrogate about the battery status
     * @return dialog that will show the information
     */
    public static BatteryInfoDialogFragment newInstance(Node node) {
        Bundle args = new Bundle();
        args.putString(NOTE_TAG,node.getTag());
        BatteryInfoDialogFragment fragment = new BatteryInfoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public BatteryInfoDialogFragment() {
        // Required empty public constructor
    }

    /**
     * battery information , null if not available
     */
    private @Nullable ArrayList<Info> mInfos;

    /**
     * Node to query
     */
    private Node mNode;

    /**
     * set up the recycler view, setting the layout manager and item decoration
     * @param c context to use for the set up
     * @param recyclerView view to configure
     */
    private static void setUpRecyclerView(Context c, RecyclerView recyclerView){
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),RecyclerView.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context c = getActivity();

        String nodeTag = getArguments().getString(NOTE_TAG);
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);

        View dialogContent = createDialogContent(c);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.batteryInfo_loadingInfo);
        builder.setPositiveButton(android.R.string.ok,null);
        builder.setView(dialogContent);
        Dialog dialog = builder.create();

        if(savedInstanceState!=null ) {
            ArrayList<Info> storeInfos = savedInstanceState.getParcelableArrayList(BATTERY_INFOS);
            if(storeInfos!=null && !storeInfos.isEmpty())
                showInfos(dialog,storeInfos);
        }
        return dialog;
    }

    @NonNull
    private View createDialogContent(Context c) {
        View dialogContent = LayoutInflater.from(c).inflate(R.layout.fragment_battery_info, null);
        mWaitLayout = dialogContent.findViewById(R.id.batteryInfo_waitView);
        mInfoList = (RecyclerView) dialogContent.findViewById(R.id.batteryInfo_infoList);
        setUpRecyclerView(getActivity(),mInfoList);
        return dialogContent;
    }

    /**
     * set up the dialog in the case the command is not available
     */
    private void setNotSupportedDialog(){
        mWaitLayout.setVisibility(View.GONE);
        getDialog().setTitle(R.string.batteryInfo_notSupported);
    }

    @Override
    public void onStart() {
        super.onStart();
        //if not info is available
        if(mInfos==null) {
            //if the node is present, connected and with the debug console
            if(mNode==null ||
                    !mNode.isConnected() ||
                    mNode.getDebug()==null){
                setNotSupportedDialog();
                return;
            }//if

            //exec the command to retrive the info
            new ConsoleCommand(mNode.getDebug(), COMMAND_TIMEOUT).exec(BATTERY_INFO_COMMAND,
                    new ConsoleCommand.Callback() {
                @Override
                public void onCommandResponds(String response) {
                    final ArrayList<Info> infos = BatteryInfoParser.parse(response);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showInfos(getDialog(), infos);
                        }
                    });
                }

                @Override
                public void onCommandError() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setNotSupportedDialog();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mInfos!=null)
            outState.putParcelableArrayList(BATTERY_INFOS,mInfos);
    }

    /**
     * show the info into the dialog
     * @param dialog dialog where show the info
     * @param infos info to show
     */
    private void showInfos(Dialog dialog, ArrayList<Info> infos){
        mInfos = infos;
        if(infos.isEmpty()){
            setNotSupportedDialog();
            return;
        }

        dialog.setTitle(R.string.batteryInfo_dialogTitile);
        mWaitLayout.setVisibility(View.GONE);
        mInfoList.setAdapter(new BatteryInfoAdapterView(infos));
        mInfoList.setVisibility(View.VISIBLE);
    }



    /**
     * Adapter to show the info in a recicleView
     */
    static class BatteryInfoAdapterView extends RecyclerView.Adapter<BatteryInfoAdapterView.ViewHolder>{

        private List<Info> mData;


        BatteryInfoAdapterView(List<Info> data){
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_battery_info_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Info info = mData.get(position);
            holder.title.setText(info.title);
            holder.value.setText(info.value);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            public final TextView title;
            public final TextView value;

            ViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.batteryInfo_title);
                value = (TextView) view.findViewById(R.id.batteryInfo_value);
            }
        }
    }

}
