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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.st.BlueMS.demos.AccEvent.AccEventFragment;
import com.st.BlueMS.demos.ActivityRecognition.ActivityRecognitionFragment;
import com.st.BlueMS.demos.Audio.Beamforming.BeamformingFragment;
import com.st.BlueMS.demos.Audio.BlueVoice.BlueVoiceFragment;
import com.st.BlueMS.demos.Audio.BlueVoice.fullBand.BlueVoiceFullBandFragment;
import com.st.BlueMS.demos.Audio.BlueVoice.fullDuplex.BlueVoiceFullDuplexFragment;
import com.st.BlueMS.demos.Audio.SpeechToText.SpeechToTextFragment;
import com.st.BlueMS.demos.AudioClassification.AudioClassificationFragment;
//import com.st.BlueMS.demos.BinaryContentDemoFragment;
import com.st.BlueMS.demos.BinaryContentDemoFragment;
import com.st.BlueMS.demos.COSensor.COSensorDemoFragment;
import com.st.BlueMS.demos.CarryPositionFragment;
import com.st.BlueMS.demos.Cloud.CloudLogFragment;
import com.st.BlueMS.demos.ColorAmbientLight.ColorAmbientLightFragment;
import com.st.BlueMS.demos.EventCounterFragment;
import com.st.BlueMS.demos.ExtConfig.ExtConfigurationFragment;
import com.st.BlueMS.demos.GNSSFragment;
import com.st.BlueMS.demos.HighSpeedDataLog.HighSpeedDataLogFragment;
import com.st.BlueMS.demos.GestureNavigationFragment;
import com.st.BlueMS.demos.JsonNFCFragment;
import com.st.BlueMS.demos.NEAIClassClassificationFragment;
import com.st.BlueMS.demos.PnPL.PnPLConfigurationFragment;
import com.st.BlueMS.demos.HighSpeedDatalog2.HSD2MainFragment;
import com.st.BlueMS.demos.Level.LevelDemoFragment;
import com.st.BlueMS.demos.NEAIAnomalyDetectionFragment;
import com.st.BlueMS.demos.PianoDemo.PianoFragment;
import com.st.BlueMS.demos.QVARFragment;
import com.st.BlueMS.demos.SDLog.SDLogFragment;
import com.st.BlueMS.demos.Textual.GenericTextualDemoFragment;
import com.st.BlueMS.demos.TimeOfFlightMultiObject.TimeOfFlightMultiObjectFragment;
import com.st.BlueMS.demos.aiDataLog.AIDataLogDemoFragment;
import com.st.BlueMS.demos.fftAmpitude.FFTAmplitudeFragment;
import com.st.BlueMS.demos.fitnessActivity.FitnessActivityFragment;
import com.st.BlueMS.demos.machineLearningCore.FiniteStateMachineFragment;
import com.st.BlueMS.demos.machineLearningCore.MachineLearningCoreFragment;
import com.st.BlueMS.demos.machineLearningCore.STREDLFragment;
import com.st.BlueMS.demos.memsSensorFusion.CompassFragment;
import com.st.BlueMS.demos.EnvironmentalSensorsFragment;
import com.st.BlueMS.demos.HeartRateFragment;
import com.st.BlueMS.demos.MemsGestureRecognitionFragment;
import com.st.BlueMS.demos.memsSensorFusion.MemsSensorFusionFragment;
import com.st.BlueMS.demos.MotionIntensityFragment;
import com.st.BlueMS.demos.NodeStatus.NodeStatusFragment;
import com.st.BlueMS.demos.PedometerFragment;
import com.st.BlueMS.demos.ProximityGestureRecognitionFragment;
import com.st.BlueMS.demos.Audio.DirOfArrival.SourceLocFragment;
import com.st.BlueMS.demos.SwitchFragment;
import com.st.BlueMS.demos.motionAlgorithm.MotionAlgorithmFragment;
import com.st.BlueMS.demos.multiNN.MultiNeuralNetworkFragment;
import com.st.BlueMS.demos.plot.PlotFeatureFragment;
import com.st.BlueMS.preference.nucleo.SettingsWithNucleoConfiguration;
import com.st.BlueSTSDK.ExportedFeature;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedAudioOpusConf;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedFeatureAudioOpusMusic;
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeServer;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.AboutActivity;
import com.st.BlueSTSDK.gui.SettingsActivityWithNode;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.download.DownloadFwFileCompletedReceiver;
import com.st.STM32WB.fwUpgrade.feature.OTABoardWillRebootFeature;
import com.st.STM32WB.fwUpgrade.feature.OTAControlFeature;
import com.st.STM32WB.fwUpgrade.feature.OTAFileUpload;
import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;
import com.st.STM32WB.p2pDemo.feature.FeatureControlLed;
import com.st.STM32WB.p2pDemo.feature.FeatureSwitchStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activity that display all the demo available for the node
 */
public class DemosActivity extends com.st.BlueSTSDK.gui.DemosActivity {

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param options    options to use during the connection
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, ConnectionOption options) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i, node, options);
        return i;
    }//getStartIntent

    public static Intent getStartIntent(Context c, @NonNull Node node) {
        return  getStartIntent(c,node, ConnectionOption.buildDefault());
    }//getStartIntent

    @DemoDescriptionAnnotation(name="OTA Config",
            requireAll = {RebootOTAModeFeature.class},
            demoCategory = {"Control"},
            iconRes = R.drawable.ota_upload_fw )
    public static class StartOtaRebootFragment extends com.st.STM32WB.fwUpgrade.statOtaConfig.StartOtaRebootFragment{
        //empty class redefined just to set the icon res in the annotation
    }

    @DemoDescriptionAnnotation(name="Led Control",
            requireAll = {FeatureSwitchStatus.class,FeatureControlLed.class},
            demoCategory = {"Control"},
            iconRes = R.drawable.stm32wb_led_on)
    public static class LedButtonControlFragment extends com.st.STM32WB.p2pDemo.LedButtonControlFragment{
        //empty class redefined just to set the icon res in the annotation
    }

    @DemoDescriptionAnnotation(name="FUOTA",
            requireAll = {OTAControlFeature.class, OTABoardWillRebootFeature.class, OTAFileUpload.class},
            demoCategory = {"Control"},
            iconRes = R.drawable.ota_upload_fw)
    public static class OtaWBAFragment extends com.st.STM32WBA.OtaWBAFragment {
        //empty class redefined just to set the icon res in the annotation
    }

    /**
     * List of all the class that extend DemoFragment class, if the board match the requirement
     * for the demo it will displayed
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment>[] ALL_DEMOS = new Class[]{
            HighSpeedDataLogFragment.class,
            HSD2MainFragment.class,
            NEAIAnomalyDetectionFragment.class,
            NEAIClassClassificationFragment.class,
            EnvironmentalSensorsFragment.class,
            MemsSensorFusionFragment.class,
            FFTAmplitudeFragment.class,
            PlotFeatureFragment.class,
            SDLogFragment.class,
            MultiNeuralNetworkFragment.class,
            ActivityRecognitionFragment.class,
            CarryPositionFragment.class,
            ProximityGestureRecognitionFragment.class,
            MemsGestureRecognitionFragment.class,
            PedometerFragment.class,
            AccEventFragment.class,
            SwitchFragment.class,
            EventCounterFragment.class,
            BlueVoiceFragment.class,
            SpeechToTextFragment.class,
            BeamformingFragment.class,
            SourceLocFragment.class,
            AudioClassificationFragment.class,
            HeartRateFragment.class,
            MotionIntensityFragment.class,
            CompassFragment.class,
            LevelDemoFragment.class,
            COSensorDemoFragment.class,
            LedButtonControlFragment.class,
            AIDataLogDemoFragment.class,
            MotionAlgorithmFragment.class,
            FitnessActivityFragment.class,
            MachineLearningCoreFragment.class,
            FiniteStateMachineFragment.class,
            STREDLFragment.class,
            TimeOfFlightMultiObjectFragment.class,
            ColorAmbientLightFragment.class,
            QVARFragment.class,
            GNSSFragment.class,
            PianoFragment.class,
            GestureNavigationFragment.class,
            JsonNFCFragment.class,
            ExtConfigurationFragment.class,
            PnPLConfigurationFragment.class,
            StartOtaRebootFragment.class,
            CloudLogFragment.class,
            GenericTextualDemoFragment.class,
            NodeStatusFragment.class,
            //FeatureDebugFragment.class,
            OtaWBAFragment.class
    };

    @Override
    protected Class<? extends DemoFragment>[] getAllDemos() {
        ArrayList<Class<? extends DemoFragment>> demoList = new ArrayList<>(Arrays.asList(ALL_DEMOS));
        if(getNode().getType() == Node.Type.STEVAL_BCN002V1){
            demoList.remove(SpeechToTextFragment.class);
        }


        //For enabling the Demos in Beta Mode
        final SharedPreferences prefs = getSharedPreferences(AboutActivity.BETA_PREFERENCE, Context.MODE_PRIVATE);
        boolean betaFunctionalities = prefs.getBoolean(AboutActivity.ENABLE_BETA_FUNCTIONALITIES, false);
        if(betaFunctionalities) {
            demoList.add(BinaryContentDemoFragment.class);
        }

        List<Class<? extends DemoFragment>> serverDemos = getServerSizeDemos();
        if(serverDemos.contains(BlueVoiceFullBandFragment.class)){
            demoList.remove(BlueVoiceFragment.class);
            demoList.remove(SpeechToTextFragment.class);
        }
        if(serverDemos.contains(BlueVoiceFullDuplexFragment.class)){
            demoList.remove(BlueVoiceFragment.class);
        }
        if(!serverDemos.isEmpty()) {
            demoList.addAll(0,serverDemos);
        }
        return demoList.toArray(new Class[0]);
    }

    private List<Class<? extends DemoFragment>> getServerSizeDemos(){
        NodeServer server = getNode().getNodeServer();
        if(server == null){
            return Collections.emptyList();
        }
        ArrayList<Class<? extends DemoFragment>> demos = new ArrayList<>();

        ExportedFeature opusAudio = server.getExportedFeature(ExportedFeatureAudioOpusMusic.class);
        ExportedFeature opusAudioConf = server.getExportedFeature(ExportedAudioOpusConf.class);
        if(opusAudio!=null && opusAudio.isNotificationEnabled()){
            demos.add(BlueVoiceFullBandFragment.class);
        } else if(opusAudioConf!=null && opusAudioConf.isNotificationEnabled()){
            Log.e("DemosActivity", demos.toString());
            demos.add(BlueVoiceFullDuplexFragment.class);
            Log.e("DemosActivity", demos.toString());
            Log.e("DemosActivity","opusAudioConf.isNotificationEnabled()");
        }
        return demos;
    }

    @Override
    protected boolean enableFwUploading() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For Keeping the device Awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Node node = getNode();
        if(node != null){
            DownloadFwFileCompletedReceiver fwDownloadReceiver =
                    new DownloadFwFileCompletedReceiver(this, node);
            getLifecycle().addObserver(fwDownloadReceiver);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            Node node = getNode();
            keepConnectionOpen(true,false);
            if (node.getFeature(FeatureExtConfiguration.class)  == null) {
                /* Start the Activity with the Settings */
                startActivity(SettingsWithNucleoConfiguration.getStartIntent(this, getNode()));
            } else {
                /* Start the Activity without the Settings */
                startActivity(SettingsActivityWithNode.getStartIntent(this, getNode(), true));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        enableServerSideDemo();
        super.onStart();
    }

    private ExportedFeature.ExportedFeatureCallback mRefreshDemoList = new ExportedFeature.ExportedFeatureCallback() {
        @Override
        public void onNotificationDisabled(@NonNull ExportedFeature onFeature) {
            Log.d("DemoActivity","reload demos");
            runOnUiThread(()->{reloadDemoList();});

        }

        @Override
        public void onNotificationEnabled(@NonNull ExportedFeature onFeature) {
            Log.d("DemoActivity","reload demos");
            runOnUiThread(()->{reloadDemoList();});
        }
    };

    private void enableServerSideDemo() {
        Node node = getNode();
        if(node == null)
            return;
        NodeServer server = node.getNodeServer();
        if(server == null)
            return;
        ExportedFeature f = server.getExportedFeature(ExportedFeatureAudioOpusMusic.class);
        if(f!=null)
            f.addListener(mRefreshDemoList);
    }

    @Override
    protected void onStop() {
        disableServerSideDemo();
        super.onStop();
    }

    private void disableServerSideDemo() {
        Node node = getNode();
        if(node == null)
            return;
        NodeServer server = node.getNodeServer();
        if(server == null)
            return;
        ExportedFeature f = server.getExportedFeature(ExportedFeatureAudioOpusMusic.class);
        if(f!=null)
            f.removeListener(mRefreshDemoList);
    }
}
