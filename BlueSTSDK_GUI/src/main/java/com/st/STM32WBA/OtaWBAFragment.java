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
package com.st.STM32WBA;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;
import com.st.BlueSTSDK.gui.fwUpgrade.FwVersionViewModel;
import com.st.BlueSTSDK.gui.fwUpgrade.RequestFileUtil;
import com.st.BlueSTSDK.gui.fwUpgrade.uploadFwFile.UploadOtaFileActionReceiver;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;

public class OtaWBAFragment extends DemoFragment implements UploadOtaFileActionReceiver.UploadFinishedListener, AdapterView.OnItemSelectedListener{

    public static OtaWBAFragment build(@NonNull Node node, @Nullable Uri file,
                                       @Nullable Long address){
        return build(node,file,address,true);
    }

    public static OtaWBAFragment build(@NonNull Node node, @Nullable Uri file,
                                       @Nullable Long address, boolean showAddressField ){
        return build(node,file,address,showAddressField,null,false);
    }

    public static OtaWBAFragment build(@NonNull Node node, @Nullable Uri file,
                                       @Nullable Long address, boolean showAddressField,
                                       @FirmwareType @Nullable Integer fwType,
                                       boolean showFwType){
        return build(node,file,address,showAddressField,fwType,null,showFwType);
    }

    public static OtaWBAFragment build(@NonNull Node node, @Nullable Uri file,
                                       @Nullable Long address, boolean showAddressField,
                                       @FirmwareType @Nullable Integer fwType,
                                       @Nullable Integer mWB_board,
                                       boolean showFwType){
        Bundle args = new Bundle();

        OtaWBAFragment f = new OtaWBAFragment();
        f.setArguments(args);
        return f;
    }

    public OtaWBAFragment() {
        // Required empty public constructor
    }

    private OtaWBAViewModel mOtaWBAViewModel;
    private Node mNode;
    private RequestFileUtil mRequestFile;
    private View mRootView;
    private TextView mFileNameText;
    private ProgressBar mUploadProgress;
    private TextView mUploadMessage;
    private Uri mSelectedFw;
    private TextView mAddressText;
    private TextInputLayout mAddressLayout;
    private View mProgressViewGroup;
    private RadioGroup mFirmwareTypeView;
    private TextView mNbSectorsText;
    private TextView mWaitedTimeText;
    private CheckBox mForceIt;
    private CheckBox mForceIt2;

    private FwVersionViewModel mVersionViewModel;
    private FloatingActionButton mStartUploadButton;
    private long mInitialAddress = 0x07C000;

    private byte[] mFileContent = new byte[] {};

    private static final String NODE_PARAM = OtaWBAFragment.class.getCanonicalName()+".NODE_PARAM";
    private static final String FILE_PARAM = OtaWBAFragment.class.getCanonicalName()+".FILE_PARAM";
    private static final String ADDRESS_PARAM = OtaWBAFragment.class.getCanonicalName()+".ADDRESS_PARAM";

    private static final String FW_URI_KEY = OtaWBAFragment.class.getCanonicalName()+".FW_URI_KEY";
    private static final String SHOW_ADDRESS_KEY = OtaWBAFragment.class.getCanonicalName()+".SHOW_ADDRESS_KEY";
    private static final String ADDRESS_KEY = OtaWBAFragment.class.getCanonicalName()+".ADDRESS_KEY";

    private static final String FINISH_DIALOG_TAG = OtaWBAFragment.class.getCanonicalName()+".FINISH_DIALOG_TAG";

    private static final String FW_TYPE_KEY = OtaWBAFragment.class.getCanonicalName()+".FW_TYPE_KEY";
    private static final String SHOW_FW_TYPE_KEY = OtaWBAFragment.class.getCanonicalName()+".SHOW_FW_TYPE_KEY";
    private static final String WB_TYPE_KEY = OtaWBAFragment.class.getCanonicalName()+".WB_TYPE_KEY";

    private static final String UPLOAD_PROGRESS_VISIBILITY_KEY = OtaWBAFragment.class.getCanonicalName()+".UPLOAD_PROGRESS_VISIBILITY_KEY";

    private BroadcastReceiver mMessageReceiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.fragment_fuota_wba, container, false);

        mFileNameText = mRootView.findViewById(R.id.otaUpload_selectFileName);
        mProgressViewGroup = mRootView.findViewById(R.id.otaUpload_uploadProgressGroup);
        mUploadProgress = mRootView.findViewById(R.id.otaUpload_uploadProgress);
        mUploadMessage = mRootView.findViewById(R.id.otaUpload_uploadMessage);
        mAddressText = mRootView.findViewById(R.id.otaUpload_addressText);

        setupSelectFileButton(mRootView.findViewById(R.id.otaUpload_selectFileButton));
        mStartUploadButton  = mRootView.findViewById(R.id.otaUpload_startUploadButton);
        setupStartUploadButton(mStartUploadButton);

        mAddressLayout = mRootView.findViewById(R.id.otaUpload_addressTextLayout);
        setupAddressText(mAddressText,mAddressLayout,mInitialAddress);

        setupFwTypeSelector(mRootView.findViewById(R.id.otaUpload_fwTypeSelector),
                savedInstanceState,getArguments());

        mRequestFile.setRootView(mRootView);

        //retrieveSave(savedInstanceState);

        mNbSectorsText = mRootView.findViewById(R.id.otaUpload_numberSectorsText);
        mNbSectorsText.setText("Select a file to calculate");
        mNbSectorsText.setEnabled(false);

        mWaitedTimeText = mRootView.findViewById(R.id.otaUpload_waitedTimeText);
        mWaitedTimeText.setText("8");
        mWaitedTimeText.setEnabled(false);

        mForceIt = mRootView.findViewById(R.id.forceIt);
        mForceIt.setOnCheckedChangeListener((checkbox, isSelected) -> {
            mNbSectorsText.setEnabled(isSelected);
        });

        mForceIt2 = mRootView.findViewById(R.id.forceIt2);
        mForceIt2.setOnCheckedChangeListener((checkbox, isSelected) -> {
            mWaitedTimeText.setEnabled(isSelected);
        });
        return  mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("FW Update","onViewCreated");
        mOtaWBAViewModel = new ViewModelProvider(requireActivity()).get(OtaWBAViewModel.class);
        mVersionViewModel = new ViewModelProvider(requireActivity()).get(FwVersionViewModel.class);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
            mOtaWBAViewModel.set_FirmwareType(getSelectedFwType());
            mOtaWBAViewModel.set_NbSectors(mNbSectorsText.getText());
            mOtaWBAViewModel.set_WaitedTime(mWaitedTimeText.getText());
            mOtaWBAViewModel.set_Address(mAddressText.getText());
            mOtaWBAViewModel.set_IsForceItChecked(mForceIt.isChecked());
            mOtaWBAViewModel.set_IsForceIt2Checked(mForceIt2.isChecked());
        super.onSaveInstanceState(outState);
    }

    private void setupFwTypeSelector(RadioGroup selector, Bundle savedInstance, Bundle args) {
        mFirmwareTypeView = selector;
        @IdRes int selected = getSelectedFwType() == FirmwareType.BLE_FW ? R.id.otaUpload_bleType : R.id.otaUpload_applicationType;
        mFirmwareTypeView.check(selected);
        mFirmwareTypeView.setOnCheckedChangeListener((group, idSelected) -> {
            Integer value = (idSelected == R.id.otaUpload_bleType) ? 0x0F6000 : 0x07C000;
            mAddressText.setText("0x"+Long.toHexString(value));
        });
    }

    private void setupAddressText(TextView addressText, TextInputLayout addressLayout, long initialValue) {
        addressText.setText("0x"+Long.toHexString(initialValue));
    }

    @Override
    public void onResume() {
        super.onResume();
        mSelectedFw = mOtaWBAViewModel.get_SelectedFw();
        if(mSelectedFw!=null) {
            mStartUploadButton.setEnabled(true);
            mFileNameText.setText(RequestFileUtil.getFileName(requireContext(),mSelectedFw));
        }

        mFirmwareTypeView.check((mOtaWBAViewModel.get_FirmwareType() == FirmwareType.BLE_FW)? R.id.otaUpload_bleType : R.id.otaUpload_applicationType);

        CharSequence storedAddress = mOtaWBAViewModel.get_Address();
        if(storedAddress != "") mAddressText.setText(storedAddress);

        CharSequence nbSectorsStored = mOtaWBAViewModel.get_NbSectors();
        if(nbSectorsStored != "") mNbSectorsText.setText(nbSectorsStored);

        CharSequence waitedTimeStored = mOtaWBAViewModel.get_WaitedTime();
        if(waitedTimeStored != "") mWaitedTimeText.setText(waitedTimeStored);

        mForceIt.setChecked(mOtaWBAViewModel.get_IsForceItChecked());
        mForceIt2.setChecked(mOtaWBAViewModel.get_IsForceIt2Checked());
        mNbSectorsText.setEnabled(mOtaWBAViewModel.get_IsForceItChecked());
        mWaitedTimeText.setEnabled(mOtaWBAViewModel.get_IsForceIt2Checked());

        if(mSelectedFw != null) {
            mFileContent = mOtaWBAViewModel.get_fileContent(getContext().getContentResolver());
            mNbSectorsText.setText(getNbOfSectorsToErase().toString());
        }

        mMessageReceiver = new UploadOtaFileActionReceiver(mUploadProgress,mUploadMessage,this);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver,
                FwUpgradeService.getServiceActionFilter());

        if(mNode.isConnected()) {
            mNode.requestNewMtu(240+3); // WBA doesn't support well 248 bytes packets for now
        }
    }

    private Integer getNbOfSectorsToErase() { // 1 sector = 8192 bytes
        if(mFileContent != null) {
            double sectors = mFileContent.length/8192.0;
            int floorSectors = mFileContent.length/8192;
            if(sectors == floorSectors*1.0) {
                return floorSectors;
            }
            return floorSectors + 1;
        }
        return -1;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("FW Update","onPause");
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver);
    }

    private void setupStartUploadButton(View button) {
        button.setOnClickListener(v -> {
            Long address = Long.decode(mAddressText.getText().toString());
            Long nbSectors = Long.decode(mNbSectorsText.getText().toString());
            @FirmwareType int selectedType = getSelectedFwType();
            FwVersion currentVersion = mVersionViewModel.getFwVersion().getValue();
            if(mSelectedFw!=null) {
                if(address!=null) {
                    Long addressToSend = address;
                    if(nbSectors!=null) {
                        addressToSend = Long.decode(mAddressText.getText().toString() + Integer.toHexString(Integer.parseInt("" + mNbSectorsText.getText())));
                    }
                    startUploadFile(mSelectedFw, selectedType,addressToSend,currentVersion);
                }else{
                    Snackbar.make(mRootView,R.string.otaUpload_invalidMemoryAddress,Snackbar.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(mRootView,R.string.otaUpload_invalidFile,Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private @FirmwareType int getSelectedFwType() {
        if( mFirmwareTypeView.getCheckedRadioButtonId() == R.id.otaUpload_bleType) {
            return FirmwareType.BLE_FW;
        } else
        {
            return FirmwareType.BOARD_FW;
        }

    }

    private void startUploadFile(@NonNull Uri selectedFile, @FirmwareType int type,
                                 long address,@Nullable FwVersion currentVersion) {
        FwUpgradeService.startUploadService(requireContext(),mNode,selectedFile,type,address,currentVersion);
        mProgressViewGroup.setVisibility(View.VISIBLE);
    }

    private void setupSelectFileButton(View button) {
        button.setOnClickListener(v -> mRequestFile.openFileSelector());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("FW Update","onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        //onFileSelected(mRequestFile.onActivityResult(requestCode,resultCode,data));

        //Uri mSelectedFw;
        mSelectedFw = mRequestFile.onActivityResult(requestCode,resultCode,data);
        mOtaWBAViewModel.set_SelectedFw(mSelectedFw);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestFile = new RequestFileUtil(this);
    }

    @Override
    public void onUploadFinished(float time_s) {
        NodeConnectionService.disconnect(requireContext(),mNode);
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string.otaUpload_completed,
                getString(R.string.otaUpload_finished,time_s));
        dialog.setOnclickListener((dialog1, which) -> {
            //FUOTAFragment.this
            NavUtils.navigateUpFromSameTask(requireActivity());
        });
        dialog.show(getParentFragmentManager(),FINISH_DIALOG_TAG);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mNode = node;
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {

    }
}
