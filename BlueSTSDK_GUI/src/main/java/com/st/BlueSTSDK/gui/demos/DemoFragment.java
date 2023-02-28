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
package com.st.BlueSTSDK.gui.demos;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.fwDataBase.db.BoardFotaType;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.DemosActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity;
import com.st.BlueSTSDK.gui.util.FragmentUtil;
import com.st.STM32WB.fwUpgrade.FwUpgradeSTM32WBActivity;
import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;

/**
 * Base class for a fragment that have to show a particular demo inside the DemoActivity activity
 * this class will call the {@link com.st.BlueSTSDK.gui.demos.DemoFragment#enableNeededNotification(com.st.BlueSTSDK.Node)}
 * when the node is connected and the fragment started (inside the onResume method).
 * And call the {@link com.st.BlueSTSDK.gui.demos.DemoFragment#disableNeedNotification(com.st.BlueSTSDK.Node)}
 * inside the onPause method for ask to the node to stop send data when they aren't used anymore
 */
public abstract class DemoFragment extends Fragment {

    public static final String SHARED_PREFS = DemoFragment.class.getCanonicalName() + "" + ".IntroductionWasShown";

    long myDownloadId = -1;

    /**
     * utility method that show an message as a toast
     *
     * @param msg resource string
     */
    protected void showActivityToast(@StringRes final int msg) {
        //run
        updateGui(() -> Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show());
    }//showActivityToast

    /**
     * Function for displaying one Introduction to each demo
     *
     * @param message Message to display
     * @param context
     */
    protected void showIntroductionMessage(final String message, Context context) {

        //Understand if the Introduction was already shown for the current demo
        final String INTRODUCTION_WAS_SHOWN = getClass().getCanonicalName() + "" + ".IntroductionWasShown";
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        boolean introductionWasShown = pref.getBoolean(INTRODUCTION_WAS_SHOWN, false);

        //If the Introduction was not yet shown
        if (!introductionWasShown) {
            updateGui(() -> {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                //Take the Title from the Demo's Title
                alertDialog.setTitle(getClass().getAnnotation(DemoDescriptionAnnotation.class).name());
                //Take the Icon from the Demo's Icon
                alertDialog.setIcon(getClass().getAnnotation(DemoDescriptionAnnotation.class).iconRes());
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            });
            //Save the flag on Shared Preference
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(INTRODUCTION_WAS_SHOWN, true);
            editor.apply();
        }
    }

    /**
     * state of the demo, if it is running or not -> if the fragment is visualized or not
     */
    private boolean mIsRunning;

    public DemoFragment() {
        if (!getClass().isAnnotationPresent(DemoDescriptionAnnotation.class)) {
            throw new RuntimeException("A DemoFragment must have an annotation of type DemoDescriptionAnnotation");
        }
    }

    /**
     * this fragment must be attached to a DemoActivity activity, this method check this condition
     *
     * @param activity activity where the fragment is attached
     * @throws java.lang.ClassCastException if the activity doesn't extend DemosActivity
     */
    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        try {
            DemosActivity temp = (DemosActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must extend DemosActivity");
        }//try
    }//onAttach


    /**
     * tell if the demo in this fragment is running or not
     *
     * @return true if the demo is running false otherwise
     */
    public boolean isRunning() {
        return mIsRunning;
    }//isRunning

    /**
     * Check that the fragment is attached to an activity, if yes run the tast on ui thread
     *
     * @param task task to run on the uithread
     */
    protected void updateGui(Runnable task) {
        FragmentUtil.runOnUiThread(this, task);
    }

    protected @Nullable
    Node getNode() {
        DemosActivity act = (DemosActivity) getActivity();
        if (act != null)
            return act.getNode();
        return null;
    }

    /**
     * enable all the notification needed for running the specific demo
     *
     * @param node node where the notification will be enabled
     */
    protected abstract void enableNeededNotification(@NonNull Node node);

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == myDownloadId) {
                DownloadManager manager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = manager.getUriForDownloadedFile(myDownloadId);
                Node mNode = getNode();
                if(mNode!=null) {
                    //for the WB boards that uses the SDK V2
                    if ((mNode.getType() == Node.Type.ASTRA1) || (mNode.getType() == Node.Type.PROTEUS) || (mNode.getType() == Node.Type.STDES_CBMLORABLE)) {
                        RebootOTAModeFeature feature = mNode.getFeature(RebootOTAModeFeature.class);
                        feature.rebootToFlash((short)0x07, (short) 0x7F/* For the moment ... download all the sectors */,
                                () -> performFileUploadWB(uri));

                    } else {
                        //for the other boards
                        Intent FwIntent = FwUpgradeActivity.getStartIntent(context, getNode(), false, uri);
                        startActivity(FwIntent);
                    }
                }
            }
        }

        void performFileUploadWB(Uri uri) {
            Node n = getNode();
            if(n == null)
                return;

            NodeConnectionService.disconnect(requireContext(),getNode());
            startActivity(FwUpgradeSTM32WBActivity.getStartIntent(requireContext(),
                    STM32OTASupport.getOtaAddressForNode(getNode()),
                    uri,((long)0x07)*0x1000 /* first Sector number* sector size */,1 /* BLE fw */,1 /*mWB_board*/));
        }
    }

    MyReceiver myReceiver = new MyReceiver();

    public void showUpdateAvailable(@NonNull Node node) {

        if (node.getProtocolVersion() == 2) {
            BoardFirmware boardFwModel = node.getFwDetails();

            if(boardFwModel!=null) {
                // Don't propose a fw update for one board on WB_READY mode... so already waiting a fw
                if(boardFwModel.getFota().getType()!= BoardFotaType.wb_ready) {
                    ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(requireContext());

                    BoardFirmware fwUpgradeModel = firmwareDB.getFirstFwUpdateForFwNameDifferentFromVersion(boardFwModel.getBle_dev_id(), boardFwModel.getFw_name(), boardFwModel.getFw_version());

                    String fullCurrentFwName = boardFwModel.getFw_name() + " V" + boardFwModel.getFw_version();

                    if (fwUpgradeModel != null) {
                        if (fwUpgradeModel.getFota().getFw_url() != null) {

                            final String FW_UPDATE_WAS_SHOWN = "propose_fw_update_for" + fullCurrentFwName + "_" + node.getTag();
                            SharedPreferences pref = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                            boolean fwUpdateWasShown = pref.getBoolean(FW_UPDATE_WAS_SHOWN, false);
                            SharedPreferences.Editor editor = pref.edit();

                            if (!fwUpdateWasShown) {
                                View customLayout = getLayoutInflater().inflate(R.layout.demo_fragment_propose_fw_update, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setView(customLayout);
                                builder.setTitle("New Firmware Available");

                                TextView fwCurrentName = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_current_name);
                                TextView fwUpdateName = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_name);
                                TextView changeLog = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_text_changelog);
                                Button buttonNegative = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_command_negative);
                                Button buttonPositive = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_command_positive);

                                CheckBox checkDontAskAgain = customLayout.findViewById(R.id.demo_fragment_propose_fw_update_command_dont_ask_again);

                                Dialog dialogUpdate = builder.create();
                                dialogUpdate.show();


                                fwCurrentName.setText(fullCurrentFwName);
                                String fullUpdateFwName = fwUpgradeModel.getFw_name() + " V" + fwUpgradeModel.getFw_version();
                                fwUpdateName.setText(fullUpdateFwName);
                                if (fwUpgradeModel.getChangelog() != null) {
                                    changeLog.setText(fwUpgradeModel.getChangelog());
                                } else {
                                    changeLog.setText("Not Present");
                                }

                                buttonNegative.setOnClickListener(v -> {
                                            if (checkDontAskAgain.isChecked()) {
                                                editor.putBoolean(FW_UPDATE_WAS_SHOWN, true);
                                                editor.apply();
                                            }
                                            dialogUpdate.dismiss();
                                        }
                                );

                                buttonPositive.setOnClickListener(v -> {
                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fwUpgradeModel.getFota().getFw_url()));
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                            String nameFile = fwUpgradeModel.getFota().getFw_url().substring(fwUpgradeModel.getFota().getFw_url().lastIndexOf("/") + 1);
                                            request.setTitle(nameFile);
                                            request.setAllowedOverMetered(true);
                                            // get download service and enqueue file
                                            DownloadManager manager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                            myDownloadId = manager.enqueue(request);
                                            requireActivity().getApplicationContext().registerReceiver(myReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                            dialogUpdate.dismiss();
                                        }
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * disable all the notification used by this demo
     *
     * @param node node where disable the notification
     */
    protected abstract void disableNeedNotification(@NonNull Node node);

    //onStateChange
    /**
     * listener that will be used for enable the notification when the node is connected
     */
    private Node.NodeStateListener mNodeStatusListener = (node, newState, prevState) -> {
        if (newState == Node.State.Connected) {
            DemoFragment.this.updateGui(() -> enableNeededNotification(node));
        } else if (newState == Node.State.Lost || newState == Node.State.Dead ||
                newState == Node.State.Unreachable) {
            DemoFragment.this.updateGui(() -> disableNeedNotification(node));

        }
    };

    private void recursiveStopDemo() {
        FragmentManager fm = getChildFragmentManager();
        Class<DemoFragment> demoFragmentClass = DemoFragment.class;
        for (Fragment child : fm.getFragments()) {
            //child extends DemoFragment
            if (demoFragmentClass.isAssignableFrom(child.getClass())) {
                ((DemoFragment) child).stopDemo();
            }
        }
    }

    private void recursiveStartDemo() {
        FragmentManager fm = getChildFragmentManager();
        Class<DemoFragment> demoFragmentClass = DemoFragment.class;
        for (Fragment child : fm.getFragments()) {
            //child extends DemoFragment
            if (demoFragmentClass.isAssignableFrom(child.getClass())) {
                ((DemoFragment) child).startDemo();
            }
        }
    }

    /**
     * method called for start the demo, it will check that the node is connect and call the
     * enableNeededNotification method
     */
    public void startDemo() {
        Log.d("DemoFragment", "Start Demo");
        Node node = getNode();
        if (node == null)
            return;
        if (node.isConnected()) {
            if(node.getFirstDemoFlag()) {
                showUpdateAvailable(node);
            }
            node.setFirstDemoFlag(false);
            enableNeededNotification(node);
        }
        //we add the listener for restart restart the demo in the case of reconnection
        node.addNodeStateListener(mNodeStatusListener);
        recursiveStartDemo();
        mIsRunning = true;
    }//startDemo

    /**
     * stop the demo and disable the notification if the node is connected
     */
    public void stopDemo() {
        Log.d("DemoFragment", "Stop Demo");
        Node node = getNode();
        if (node == null)
            return;
        node.removeNodeStateListener(mNodeStatusListener);
        //if the node is already disconnected we don't care of disable the notification
        if (mIsRunning && node.isConnected()) {
            if(myDownloadId!=-1) {
                requireActivity().getApplicationContext().unregisterReceiver(myReceiver);
            }
            disableNeedNotification(node);
        }//if
        recursiveStopDemo();
        mIsRunning = false;
    }//stopDemo


    /*
    @Override
    public void setUserVisibleHint(boolean visible){
        Log.d("DemoFragment","setUserVisibleHint, isVisible: " + visible +" isResumed: " + isResumed());
        //since this fragment will be used inside a viewPager that will preload the page we have to
        //override this function for be secure to start & stop the demo when the fragment is hide
        //NOTE: when we rotate the screen the fragment is restored + setUserVisibleHint(false)
        // +setUserVisibleHit(true) -> the we start, stop and start again the demo..
        super.setUserVisibleHint(visible);
        //if the fragment is loaded
        if(isResumed()) {
            //if it became visible and the demo is not already running
            if (visible) {
                if (!isRunning())
                    startDemo();
            }else {
                stopDemo();
            }
        }//isResumed
    }
    */

    /**
     * the fragment is displayed -> start the demo
     */
    @Override
    public void onResume() {
        Log.d("DemoFragment", "OnResume:" + getUserVisibleHint() + ", isRunning:" + isRunning());
        super.onResume();
        if (!isRunning()) {
            startDemo();
        }
    }//onResume


    /**
     * the fragment is hide -> stop the demo
     */
    @Override
    public void onPause() {
        Log.d("DemoFragment", "onPause, isVisible:" + getUserVisibleHint() + ", isRunning:" + isRunning());
        if (isRunning()) {
            stopDemo();
        }
        super.onPause();
    }//stopDemo

}//DemoFragment
