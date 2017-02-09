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

package com.st.BlueMS;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.st.BlueMS.demos.AccEventFragment;
import com.st.BlueMS.demos.CloudLogFragment;
import com.st.BlueMS.demos.EnvironmentalSensorsFragment;
import com.st.BlueMS.demos.NodeStatusFragment;
import com.st.BlueMS.demos.PedometerFragment;
import com.st.BlueMS.demos.PlotFeatureFragment;
import com.st.BlueMS.demos.SwitchFragment;
import com.st.BlueMS.demos.wesu.ActivityRecognitionFragmentWesu;
import com.st.BlueMS.demos.wesu.CarryPositionFragmentWesu;
import com.st.BlueMS.demos.wesu.MemsSensorFusionFragmentWesu;
import com.st.BlueMS.demos.wesu.preferences.SettingsWithWesuRegisters;
import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines;
import com.st.BlueSTSDK.ConfigControl;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.DemosActivity;
import com.st.BlueSTSDK.gui.demos.DemoFragment;


public class DemosActivityWesu extends DemosActivity implements ConfigControl.ConfigControlListener {
    private static final String TAG = DemosActivityWesu.class.getName();
    private static final String DIALOG_TAG = TAG+".FW_VERSION_DIALOG";

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param resetCache true if you want to reload the service and characteristics from the device
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, boolean resetCache) {
        Intent i = new Intent(c, com.st.BlueMS.DemosActivityWesu.class);
        setIntentParameters(i, node, resetCache);
        return i;
    }//getStartIntent

     /**
     * List of all the class that extend DemoFragment class, if the board match the requirement
     * for the demo it will displayed
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment> ALL_DEMOS[] = new Class[]{

            EnvironmentalSensorsFragment.class,
            MemsSensorFusionFragmentWesu.class,
            PlotFeatureFragment.class,
            ActivityRecognitionFragmentWesu.class,
            CarryPositionFragmentWesu.class,
            PedometerFragment.class,
            AccEventFragment.class,
            SwitchFragment.class,
            //MultipleLogFragment.class,
            NodeStatusFragment.class,
            CloudLogFragment.class,
            //FeatureDebugFragment.class
    };

    @Override
    protected Class<? extends DemoFragment>[] getAllDemos() {
        return ALL_DEMOS;
    }

    @Override
    protected boolean enableLicenseManager() {
        return true;
    }

    @Override
    protected boolean enableFwUploading() {
        return false;
    }


    /**
     * send a read request to have the fw version, the answer will be notify in the
     * {@link DemosActivityWesu#onRegisterReadResult(ConfigControl, Command, int)} method.
     * @param config object where read the version
     *              
     */
    private void readBoardVersion (ConfigControl config){
        if(config==null)
            return;
        config.addConfigListener(this);
        config.read(new Command(RegisterDefines.RegistersName.FW_VER.getRegister(),
                Register.Target.PERSISTENT));
    }//readBoardVersion


    /**
     * the first time and at the first connection read the wesu fw version for show the dialog
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            return;
        
        Node node = getNode();
        if(node.getState()==Node.State.Connected){
            readBoardVersion(node.getConfigRegister());
        }else {
            node.addNodeStateListener(new Node.NodeStateListener() {
                @Override
                public void onStateChange(Node node, Node.State newState, Node.State prevState) {
                    if (newState == Node.State.Connected) {
                        readBoardVersion(node.getConfigRegister());
                        node.removeNodeStateListener(this);
                        invalidateOptionsMenu();
                    }//if
                }//onStateChange
            });
        }//if-else
        
    }//onCreate
    
    
    @Override
    public void onRegisterReadResult(ConfigControl control,Command cmd, int error) {
        //if some error or the command is not a read on the fw version
        if ( error != 0 ||
                !cmd.getRegister().equals(RegisterDefines.RegistersName.FW_VER.getRegister())) {
            return;
        }//else
        
        //avoid to receive other notification
        control.removeConfigListener(this);
        FwVersion version = new RegisterDefines.FwVersionWesu(cmd);
        if(isOldFwVersion(version)){
            getFragmentManager().beginTransaction()
                    .add(new UpdateDialog(),DIALOG_TAG)
                    .commit();
        }
    }

    /**
     * load the last fw version from the resource file
     * @return last fw version konw form the app
     */
    private FwVersion loadLastKnowVersion(){
        int[] lastVersion = getResources().getIntArray(R.array.wesu_last_fw_version);
        return new FwVersion(lastVersion[0],lastVersion[1],lastVersion[2]);
    }

    /**
     * compare the fw version with the last know fw version
     * @param currentVersion board fw version
     * @return true if a more recent fw is know form the app
     */
    private boolean isOldFwVersion(FwVersion currentVersion) {
        return  currentVersion.compareTo(loadLastKnowVersion())<0;
    }


    /**
     * create a dialog Fragment that warning the user of the old fw
     */
    public static class UpdateDialog extends DialogFragment {
        private final static String OTA_APP_ID = "com.st.STBlueDFU";
        private final static String OTA_UPGRADE_ACTION = OTA_APP_ID+".OTA_UPGRADE";
        private final static String OTA_NODE_TAG = "OTA_NODE_TAG";
        private final static String OTA_NODE_NAME = "OTA_NODE_NAME";
        private final static String OTA_NODE_TYPE = "OTA_NODE_TYPE";


        /**
         * return true if there is an app that can handle the intent
         * @param updateIntent intent that will start the dfu app
         * @return true if an app for handle the intent exist
         */
        private boolean hasDfuApp(Intent updateIntent){
            PackageManager packageManager = getActivity().getPackageManager();
            return packageManager.queryIntentActivities(updateIntent,
                    PackageManager.MATCH_DEFAULT_ONLY).size()>0;
        }

        /**
         * create an intent for install the dfu app
         * @return intent with a link for the dfu app
         */
        private Intent getInstallOtaApp(){
            return new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("market://details?id="+OTA_APP_ID));
        }

        /**
         * create an intent that start the ota process.
         * if the dfu app is not installed the intent will ask to install the app.
         * @return intent used to start update the fw
         */
        private Intent getUpdateIntent(){
            Node node = ((DemosActivity)getActivity()).getNode();
            Intent otaIntent = new Intent(OTA_UPGRADE_ACTION);
            otaIntent.putExtra(OTA_NODE_TAG, node.getTag());
            otaIntent.putExtra(OTA_NODE_NAME, node.getName());
            otaIntent.putExtra(OTA_NODE_TYPE, node.getType().toString());
            if(hasDfuApp(otaIntent))
                return otaIntent;
            else
                return getInstallOtaApp();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DemosActivity activity =(DemosActivity)getActivity();
            return new AlertDialog.Builder(activity)
                    .setIcon(R.drawable.ic_warning_24dp)
                    .setTitle(R.string.wesu_warning_fw_update_title)
                    .setMessage(R.string.wesu_warning_fw_update_msg)
                    .setPositiveButton(R.string.wesu_warning_fw_update_update_button,
                            (dialog, whichButton) -> {
                                //we will exit from the app so disconnect from the node
                                activity.keepConnectionOpen(false,false);
                                startActivity(getUpdateIntent());
                                dialog.dismiss();
                                activity.finish();
                            }
                    )
                    .setNegativeButton(R.string.wesu_warning_fw_update_continue_button,
                            (dialog, whichButton) -> dialog.dismiss()
                    )
                    .setCancelable(false)
                    .create();
        }

    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_activity_wesu, menu);
        Node node = getNode();
        //hide debug stuff if not available
        if(node==null || node.getConfigRegister()==null)
            menu.findItem(R.id.openConfigRegisterSettings).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            keepConnectionOpen(true,false);
            startActivity(SettingsWithWesuRegisters.getStartIntent(this, getNode()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRegisterWriteResult(ConfigControl control, Command cmd, int error) {

    }

    @Override
    public void onRequestResult(ConfigControl control, Command cmd, boolean success) {

    }

}
