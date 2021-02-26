/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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

package com.st.BlueMS.demos.Audio.BlueVoice.fullBand;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedFeatureAudioOpusMusic;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeServer;
import com.st.BlueSTSDK.NodeServer.NodeServerListener;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
// this class is enable from the node, so it doesn't require any feature
@DemoDescriptionAnnotation(name="BlueVoice FullBand",iconRes= R.drawable.ic_bluetooth_audio)
public class BlueVoiceFullBandFragment extends BaseDemoFragment {

    private static final String TAG = BlueVoiceFullBandFragment.class.getCanonicalName();

    private static final int HIGH_BAND_BITRATE = 192000;
    private static final int LOW_BAND_BITRATE = 96000;

    /** PCM extraction and Encode/send Threads parameters */
    private static final int THREAD_POOL_SIZE = 1;
    private static final int START_DELAY = 0;
    private final TimeUnit TIME_UNIT = TimeUnit.MICROSECONDS;

    /** Audio Encoder parameters*/
    private static final int TIME_uS = 10000;
    private static final int AUD_PKT_INPUT_SIZE = 960*(TIME_uS/10000); //shorts
    private static final int AUD_PKT_INPUT_SIZE_BYTES = AUD_PKT_INPUT_SIZE *2; //bytes
    private static final int MAX_ENC_SIZE = 300;
    private static final int ENC_SAMPLING_FREQ = 48000;
    private static final short ENC_CHANNELS = 2;
    private static final int ENC_APPLICATION_TYPE = 2049;
    private static final boolean ENC_IS_VBR = false;
    private static final int ENC_COMPLEXITY = 4;

    /** Song Info*/
    private BVSong mCurrSong;
    private int mCurrSongPosition;
    private RecyclerView recyclerViewSL;
    private SongViewAdapter mSongListViewAdapter;

    private ExportedFeatureAudioOpusMusic mAudioTransmitter;
    private AudioCodecManager mAudioCodecManager;
    private PCMExtractor pcmExtractor;

    private EditText mSongsFolderText;
    private TextView mBitrateText;


    /** */
    private final static int PICK_FOLDER_CODE = 9999;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private static final int REQUEST_READ_ACCESS = 7;
    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private boolean checkReadExternalSDPermission(){
        Activity activity = requireActivity();
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadExternalSDPermission(final int requestCode){
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_ACCESS) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openDirectorySelector();
            }else{
                //we don't have the permission
                View rootView = requireActivity().getCurrentFocus();
                if(rootView!=null)
                    Snackbar.make(rootView, "Error in reading files from your SD", Snackbar.LENGTH_SHORT).show();
            }
            return;
        }//switch
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }//onRequestPermissionsResult


    /** Send Thread */
    private ScheduledThreadPoolExecutor pool_Send;
    private int count = 0;
    private void startSendTimer(byte[] bytes){
        count = 0;
        pool_Send.scheduleAtFixedRate(() -> {
            //120 because ST demo opus file is encoded @96kbps
            byte[] subarray = Arrays.copyOfRange(bytes, (120*count), 120*(count+1));
            mAudioTransmitter.sendEncodedAudio(subarray);
            count++;
            if((120*(count+1)) > bytes.length){
                if (pool_Send != null) {
                    stopProcess(mCurrSong, mCurrSongPosition);
                }
            }
        },START_DELAY,TIME_uS,TIME_UNIT);
    }

    private void stopSendTimer(){
        pool_Send.shutdownNow();
        pool_Send = null;
    }

    /** Encode and Send Thread */
    private ScheduledThreadPoolExecutor pool_EncSend;
    private void startEncSendTimer(){
        /* Scheduled at fixed rate, it allows to encode and send an extracted audio packet */
        pool_EncSend.scheduleAtFixedRate(() -> {
            if(pcmExtractor.isConfiguredYet() && !(pcmExtractor.isStopped())) {
                short[] subArray = pcmExtractor.getDecoded_Packet();
                byte[] encPacket;
                if (subArray != null) {
                    encPacket = mAudioCodecManager.encode(subArray);
                    mAudioTransmitter.sendEncodedAudio(encPacket);
                } else {
                    if (pool_EncSend != null) {
                        stopProcess(mCurrSong,mCurrSongPosition);
                    }
                }
            }
        }, START_DELAY, TIME_uS, TIME_UNIT);
    }

    private void stopEncSendTimer(){
        pool_EncSend.shutdownNow();
        pool_EncSend = null;
    }


    /** PCM Extraction Thread */
    private ScheduledThreadPoolExecutor pool_pcmExtractor;
    private Runnable mPCMExtractorThread = new Runnable() {
        @Override
        public void run() {
            try {
                pcmExtractor = new PCMExtractor();
                ParcelFileDescriptor fileDescriptor = requireContext()
                        .getContentResolver().openFileDescriptor(mCurrSong.getUri(),"r");
                if(fileDescriptor!=null) {
                    pcmExtractor.convert(fileDescriptor.getFileDescriptor());
                }
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    };
    private void startPCMExtractorTimer(){
        if(pool_pcmExtractor!=null){
            pool_pcmExtractor.shutdownNow();
            pool_pcmExtractor = null;
            Log.d(TAG,"pool_pcmExtractor DESTROYED!");
        }
        pool_pcmExtractor = new ScheduledThreadPoolExecutor(1);
        pool_pcmExtractor.schedule(mPCMExtractorThread,
                START_DELAY,
                TIME_UNIT);
    }
    private void forcedStopPCMExtractorTimer(){
        pcmExtractor.enableForcedStop();
    }

    /**
     * Starts the PCM Extraction procedure and the Encode/Send scheduling
     * @param s the selected song
     */
    private void startProcess(BVSong s, int songPosition){
        s.setPlaying(true);
        updateGui(() -> mSongListViewAdapter.notifyItemChanged(songPosition));
        if(!s.isDemoSong()) {
            startPCMExtractorTimer();
            String startDate = DateFormat.getDateTimeInstance().format(new Date());
            Log.d(TAG, "---> Thread STARTED @ Time: " + startDate);
            pool_EncSend = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
            startEncSendTimer();
        } else {
             String startDate = DateFormat.getDateTimeInstance().format(new Date());
            Log.d(TAG, "---> Thread STARTED @ Time: " + startDate);
            pool_Send = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
            InputStream inputStream;
            try {
                inputStream = requireContext().getAssets().open(s.getPath());
                byte[] bytes = IOUtils.toByteArray(inputStream);
                startSendTimer(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops (Forced) the PCM Extraction procedure and the Encode/Send scheduling
     * @param s the selected song
     */
    private void stopProcess(BVSong s, int songPosition){
        s.setPlaying(false);
        updateGui(() -> mSongListViewAdapter.notifyItemChanged(songPosition));
        if(pool_pcmExtractor != null && !(s.isDemoSong())){
            forcedStopPCMExtractorTimer();
        }
        if(pool_EncSend != null) {
            stopEncSendTimer();
        }
        if(pool_Send != null) {
            stopSendTimer();
        }
        String stopDate = DateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG,"---> Thread STOPPED @ Time: " + stopDate);
    }

    /**
     * Manage the song playing state when it is selected
     * @param s the selected song identifier
     */
    private void manageSongSelected(BVSong s, int songPosition){
        Log.i(TAG,": " + s.toString());
        if(mCurrSong == null){
            mCurrSong = s;
            mCurrSongPosition = songPosition;
            startProcess(mCurrSong, mCurrSongPosition);
        } else {
            if(mCurrSong == s){
                if(mCurrSong.isPlaying()){
                    stopProcess(mCurrSong, mCurrSongPosition);
                }else{
                    startProcess(mCurrSong, mCurrSongPosition);
                }
            }else{
                if(mCurrSong.isPlaying()) {
                    stopProcess(mCurrSong, mCurrSongPosition);
                }
                mCurrSong = s;
                mCurrSongPosition = songPosition;
                startProcess(mCurrSong, mCurrSongPosition);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FOLDER_CODE) {
            if(resultCode != Activity.RESULT_OK || data == null)
                return;
            Uri treeUri = data.getData();
            if(treeUri == null)
                return;

            DocumentFile documentFile = DocumentFile.fromTreeUri(requireContext(),treeUri);
            if(documentFile!=null)
                mSupportedFileViewModel.onUserSelectDirectory(documentFile);
        }else {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void updateCodecUI(int bitrate) {
        updateGui(() -> {
            int kbps = bitrate/1000;
            String bitRateText = getString(R.string.blueVoiceFB_bitrateFormat,kbps);
            mBitrateText.setText(bitRateText);
        });
    }


    private void openDirectorySelector(){
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        CharSequence chooserTitle = getString(R.string.blueVoiceFB_chooserDir);
        startActivityForResult(Intent.createChooser(i, chooserTitle), PICK_FOLDER_CODE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.fragment_bluevoice_fullband, container, false);

        pcmExtractor = new PCMExtractor();

        mSongsFolderText = mRootView.findViewById(R.id.blueVoiceFB_FolderValue);

        mBitrateText = mRootView.findViewById(R.id.blueVoiceFB_bitrateValue);

        ImageButton mBrowseSongsButton = mRootView.findViewById(R.id.blueVoiceFB_BrowseSongs);
        mBrowseSongsButton.setOnClickListener(view -> {
            if(checkReadExternalSDPermission()) {
                openDirectorySelector();
            } else {
                requestReadExternalSDPermission(REQUEST_READ_ACCESS);
            }
        });
        recyclerViewSL = mRootView.findViewById(R.id.song_list);
        return mRootView;
    }

    private SupportedFileViewModel mSupportedFileViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSupportedFileViewModel = ViewModelProviders.of(this,
                new SavedStateViewModelFactory(requireActivity().getApplication(),this))
                .get(SupportedFileViewModel.class);
        mSupportedFileViewModel.getCurrentDirectory().observe(getViewLifecycleOwner(), s -> mSongsFolderText.setText(s));

        mSupportedFileViewModel.getAvailableSongs().observe(getViewLifecycleOwner(), bvSongs -> {
                mSongListViewAdapter = new SongViewAdapter(bvSongs, this::manageSongSelected);
                recyclerViewSL.setAdapter(mSongListViewAdapter);
        });
    }

    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        menu.findItem(R.id.startLog).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        NodeServer server = node.getNodeServer();
        if(server == null)
            return;

        server.addListener(mNodeServerListener);
        mAudioTransmitter = server.getExportedFeature(ExportedFeatureAudioOpusMusic.class);

        FeatureAudioConf mAudioConf = node.getFeature(FeatureAudioConf.class);
        if(mAudioConf !=null && mAudioTransmitter!=null) {
            int encBitrate = server.isLe2MPhySupported() ? HIGH_BAND_BITRATE : LOW_BAND_BITRATE;
            updateCodecUI(encBitrate);

            mAudioConf.setEncParams(MAX_ENC_SIZE,
                    ENC_SAMPLING_FREQ,
                    ENC_CHANNELS,
                    AUD_PKT_INPUT_SIZE_BYTES / 4,
                    ENC_APPLICATION_TYPE,
                    encBitrate,
                    ENC_IS_VBR,
                    ENC_COMPLEXITY);
            mAudioCodecManager = mAudioConf.instantiateManager(false,true);
        }
    }//enableNeededNotification

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mCurrSong !=null && mCurrSong.isPlaying()) {
            stopProcess(mCurrSong, mCurrSongPosition);
        }
        NodeServer nodeServer = node.getNodeServer();
        if(nodeServer!=null)
            nodeServer.removeListener(mNodeServerListener);
    }//disableNeedNotification


    private NodeServerListener mNodeServerListener = new NodeServerListener(){

        @Override
        public void onDisconnection(@NotNull NodeServer node) { }

        @Override
        public void onConnection(@NotNull NodeServer node) { }

        @Override
        public void onPhyUpdate(@NotNull NodeServer node, boolean isUsingPhy2) {
            int encBitrate = isUsingPhy2 ? HIGH_BAND_BITRATE : LOW_BAND_BITRATE;
            updateGui(()-> updateCodecUI(encBitrate));
        }

        @Override
        public void onMtuUpdate(@NotNull NodeServer node, int newValue) { }
    };
}