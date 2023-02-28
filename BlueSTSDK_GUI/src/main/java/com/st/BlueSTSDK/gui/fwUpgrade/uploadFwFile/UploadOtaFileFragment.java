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
package com.st.BlueSTSDK.gui.fwUpgrade.uploadFwFile;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;
import com.st.BlueSTSDK.gui.fwUpgrade.FwVersionViewModel;
import com.st.BlueSTSDK.gui.fwUpgrade.RequestFileUtil;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckHexNumber;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckMultipleOf;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;

public class UploadOtaFileFragment extends Fragment implements UploadOtaFileActionReceiver.UploadFinishedListener, AdapterView.OnItemSelectedListener{

    private static final String FINISH_DIALOG_TAG = UploadOtaFileFragment.class.getCanonicalName()+".FINISH_DIALOG_TAG";

    private static final String FW_URI_KEY = UploadOtaFileFragment.class.getCanonicalName()+".FW_URI_KEY";
    private static final String SHOW_ADDRESS_KEY = UploadOtaFileFragment.class.getCanonicalName()+".SHOW_ADDRESS_KEY";
    private static final String ADDRESS_KEY = UploadOtaFileFragment.class.getCanonicalName()+".ADDRESS_KEY";

    private static final String FW_TYPE_KEY = UploadOtaFileFragment.class.getCanonicalName()+".FW_TYPE_KEY";
    private static final String SHOW_FW_TYPE_KEY = UploadOtaFileFragment.class.getCanonicalName()+".SHOW_FW_TYPE_KEY";
    private static final String WB_TYPE_KEY = UploadOtaFileFragment.class.getCanonicalName()+".WB_TYPE_KEY";

    private static final String UPLOAD_PROGRESS_VISIBILITY_KEY = UploadOtaFileFragment.class.getCanonicalName()+".UPLOAD_PROGRESS_VISIBILITY_KEY";

    private static final String NODE_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".NODE_PARAM";
    private static final String FILE_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".FILE_PARAM";
    private static final String ADDRESS_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".ADDRESS_PARAM";



    private static final int MIN_MEMORY_ADDRESS[] = {0x00, 0x7000  ,0x7000  }; //Undef, WB, WB15
    private static final int MAX_MEMORY_ADDRESS[] = {0x00, 0x089000,0x01C000}; //Undef, WB, WB15
    private static final int WB_SECTOR_SIZE[]     = {0x00,  0x1000,   0x800}; //Undef, WB, WB15

    public static UploadOtaFileFragment build(@NonNull Node node, @Nullable Uri file,
                                              @Nullable Long address){
        return build(node,file,address,true);
    }

    public static UploadOtaFileFragment build(@NonNull Node node, @Nullable Uri file,
                                              @Nullable Long address, boolean showAddressField ){
        return build(node,file,address,showAddressField,null,false);
    }

    public static UploadOtaFileFragment build(@NonNull Node node, @Nullable Uri file,
                                              @Nullable Long address, boolean showAddressField,
                                              @FirmwareType @Nullable Integer fwType,
                                              boolean showFwType){
       return build(node,file,address,showAddressField,fwType,null,showFwType);
    }

    public static UploadOtaFileFragment build(@NonNull Node node, @Nullable Uri file,
                                              @Nullable Long address, boolean showAddressField,
                                              @FirmwareType @Nullable Integer fwType,
                                              @Nullable Integer mWB_board,
                                              boolean showFwType){
        Bundle args = new Bundle();
        args.putString(NODE_PARAM,node.getTag());
        args.putBoolean(SHOW_ADDRESS_KEY,showAddressField);
        if(file!=null)
            args.putParcelable(FILE_PARAM,file);

        if(address!=null)
            args.putLong(ADDRESS_PARAM, address);

        args.putBoolean(SHOW_FW_TYPE_KEY,showFwType);
        if(fwType!=null){
            args.putInt(FW_TYPE_KEY,fwType);
        }
        if(mWB_board!=null) {
            args.putInt(WB_TYPE_KEY,mWB_board);
        }

        UploadOtaFileFragment f = new UploadOtaFileFragment();
        f.setArguments(args);
        return f;
    }

    public UploadOtaFileFragment() {
        // Required empty public constructor
    }

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
    private Spinner mboardTypeSpinner;

    private FwVersionViewModel mVersionViewModel;
    private FloatingActionButton mStartUploadButton;
    private static int mWbBoardType=1; //WB55 board
    private long mInitialAddress = MIN_MEMORY_ADDRESS[mWbBoardType];
    private TextWatcher watcher1;
    private TextWatcher watcher2;
    private TextWatcher watcher3;

//   private CompoundButton mBleMemory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("FW Update","onCreateView");
        mRootView =  inflater.inflate(R.layout.fragment_upload_ota_file, container, false);

        mFileNameText = mRootView.findViewById(R.id.otaUpload_selectFileName);
        mProgressViewGroup = mRootView.findViewById(R.id.otaUpload_uploadProgressGroup);
        mUploadProgress = mRootView.findViewById(R.id.otaUpload_uploadProgress);
        mUploadMessage = mRootView.findViewById(R.id.otaUpload_uploadMessage);
        mAddressText = mRootView.findViewById(R.id.otaUpload_addressText);

        setupSelectFileButton(mRootView.findViewById(R.id.otaUpload_selectFileButton));
        mStartUploadButton  = mRootView.findViewById(R.id.otaUpload_startUploadButton);
        setupStartUploadButton(mStartUploadButton);

        // We need to find before the right WB board that we need for Max/Min Memory Address
        mboardTypeSpinner = mRootView.findViewById(R.id.otaUpload_WB_Type);
        setupWBType(savedInstanceState,getArguments());

        //Retrieve the Initial Address value
        mInitialAddress = getFlashAddress(savedInstanceState,getArguments());
        mAddressLayout = mRootView.findViewById(R.id.otaUpload_addressTextLayout);
        setupAddressText(mAddressText,mAddressLayout,mInitialAddress);

        if(!showFlashAddress(getArguments())){
            mAddressText.setVisibility(View.GONE);
        }

        setupFwTypeSelector(mRootView.findViewById(R.id.otaUpload_fwTypeSelector),
                savedInstanceState,getArguments());

        //mRequestFile = new RequestFileUtil(this,mRootView);
        mRequestFile.setRootView(mRootView);
        onFileSelected(getFirmwareLocation(savedInstanceState,getArguments()));
        return  mRootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("FW Update","onViewCreated");
        mVersionViewModel = new ViewModelProvider(requireActivity()).get(FwVersionViewModel.class);
    }

    private void setupFwTypeSelector(RadioGroup selector, Bundle savedInstance, Bundle args) {
        mFirmwareTypeView = selector;
        Log.i("FW Update","setupFwTypeSelector");
        if(showFwTypeSelector(args)){
            mFirmwareTypeView.setVisibility(View.VISIBLE);
        }else{
            mFirmwareTypeView.setVisibility(View.GONE);
        }

        @IdRes int selected = getSelectedFwType(savedInstance,args) == FirmwareType.BLE_FW ? R.id.otaUpload_bleType : R.id.otaUpload_applicationType;
        mFirmwareTypeView.check(selected);
    }

    private void setupWBType(Bundle savedInstance, Bundle args) {
        Log.i("FW Update","setupWBType");
        if(showFwTypeSelector(args)) {
            mboardTypeSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.wb_board_type, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mboardTypeSpinner.setAdapter(adapter);
            mboardTypeSpinner.setOnItemSelectedListener(this);
        } else{
            mboardTypeSpinner.setVisibility(View.GONE);
        }
        if(shoWBBoardType(args)!=0){
            mWbBoardType = getWbBoardType(savedInstance,args);
        }
        mboardTypeSpinner.setSelection(mWbBoardType);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("FW Update","onSaveInstanceState");
        if(mAddressText.getText().length()!=0) {
            outState.putString(ADDRESS_KEY,mAddressText.getText().toString());
        }
        if(mFirmwareTypeView.getVisibility() == View.VISIBLE){
            outState.putInt(FW_TYPE_KEY,getSelectedFwType());
        }
        outState.putInt(UPLOAD_PROGRESS_VISIBILITY_KEY,mProgressViewGroup.getVisibility());
        outState.putParcelable(FW_URI_KEY,mSelectedFw);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.i("FW Update","onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null && savedInstanceState.containsKey(UPLOAD_PROGRESS_VISIBILITY_KEY)){
            mProgressViewGroup.setVisibility(savedInstanceState.getInt(UPLOAD_PROGRESS_VISIBILITY_KEY));
        }
    }

    private long getFlashAddress(@Nullable Bundle savedInstanceState, @Nullable Bundle arguments){
        if(savedInstanceState!=null && savedInstanceState.containsKey(ADDRESS_KEY)){
            try {
                return Long.decode(savedInstanceState.getString(ADDRESS_KEY));
            }catch (NumberFormatException e){
                return MIN_MEMORY_ADDRESS[mWbBoardType];
            }
        }
        if(arguments!=null && arguments.containsKey(ADDRESS_PARAM)){
            return arguments.getLong(ADDRESS_PARAM);
        }
        return MIN_MEMORY_ADDRESS[mWbBoardType];
    }

    private boolean showFlashAddress(@Nullable Bundle arguments){
        if(arguments!=null){
            return arguments.getBoolean(SHOW_ADDRESS_KEY,false);
        }else{
            return false;
        }
    }

    private boolean showFwTypeSelector(@Nullable Bundle arguments){
        if(arguments!=null){
            return arguments.getBoolean(SHOW_FW_TYPE_KEY,false);
        }else{
            return false;
        }
    }


    private int  shoWBBoardType(@Nullable Bundle arguments){
        if(arguments!=null){
            return arguments.getInt(WB_TYPE_KEY,0);
        } else {
            return 0;
        }
    }

    private @FirmwareType int getSelectedFwType(@Nullable Bundle savedInstanceState, @Nullable Bundle arguments){
        Log.i("FW Update","getSelectedFwType");
        if(savedInstanceState!=null && savedInstanceState.containsKey(FW_TYPE_KEY)){
                return savedInstanceState.getInt(FW_TYPE_KEY);
        }
        if(arguments!=null && arguments.containsKey(FW_TYPE_KEY)){
            return arguments.getInt(FW_TYPE_KEY);
        }
        return FirmwareType.BOARD_FW;
    }

    private int getWbBoardType(@Nullable Bundle savedInstanceState, @Nullable Bundle arguments){
        Log.i("FW Update","getWbBoardType");
        if(savedInstanceState!=null && savedInstanceState.containsKey(WB_TYPE_KEY)){
            return savedInstanceState.getInt(WB_TYPE_KEY);
        }
        if(arguments!=null && arguments.containsKey(WB_TYPE_KEY)){
            return arguments.getInt(WB_TYPE_KEY);
        }
        return 2; //WB not Identified
    }

    private @Nullable Uri getFirmwareLocation(@Nullable Bundle savedInstanceState, @Nullable Bundle arguments){
        Log.i("FW Update","getFirmwareLocation");
        if(savedInstanceState!=null && savedInstanceState.containsKey(FW_URI_KEY))
            return savedInstanceState.getParcelable(FW_URI_KEY);
        if(arguments!=null && arguments.containsKey(FILE_PARAM))
            return arguments.getParcelable(FILE_PARAM);
        return null;
    }

    private void setupAddressText(TextView addressText, TextInputLayout addressLayout, long initialValue) {
        watcher1 = new CheckMultipleOf(addressLayout,R.string.otaUpload_invalidBlockAddress,
                WB_SECTOR_SIZE[mWbBoardType]);
        addressText.addTextChangedListener(watcher1);
        watcher2 = new CheckNumberRange(addressLayout,R.string.otaUpload_invalidMemoryAddress,
                MIN_MEMORY_ADDRESS[mWbBoardType], MAX_MEMORY_ADDRESS[mWbBoardType]);
        addressText.addTextChangedListener(watcher2);
        watcher3 = new CheckHexNumber(addressLayout,R.string.otaUpload_invalidHex);
        addressText.addTextChangedListener(watcher3);

        addressText.setText("0x"+Long.toHexString(initialValue));

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("FW Update","onStart");
        Bundle args = getArguments();
        mNode = Manager.getSharedInstance().getNodeWithTag(args.getString(NODE_PARAM));

        if(mNode!=null) {
            if((mNode.getType()== Node.Type.ASTRA1) || (mNode.getType()== Node.Type.PROTEUS) || (mNode.getType() == Node.Type.STDES_CBMLORABLE)) {
                //For Polaris/Proteus
                if(mboardTypeSpinner!=null) {
                    mboardTypeSpinner.setSelection(1);
                    wBBoardSelected(1);
                    mboardTypeSpinner.setEnabled(false);
                }
            }
        }

        mNode.addNodeStateListener((node, newState, prevState) -> {
            if(newState== Node.State.Connected) {
                if((node.getType()==Node.Type.WB_BOARD) || (node.getType()==Node.Type.PROTEUS) || (node.getType()==Node.Type.ASTRA1) || (node.getType()== Node.Type.STDES_CBMLORABLE)){
                    Log.i("FW Update","requestMtu isConnected="+node.isConnected());
                    node.requestNewMtu(248+3);
                }
            }
        });
    }

    private BroadcastReceiver mMessageReceiver;

    @Override
    public void onResume() {
        super.onResume();
        Log.i("FW Update","onResume");
        mMessageReceiver = new UploadOtaFileActionReceiver(mUploadProgress,mUploadMessage,this);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver,
                FwUpgradeService.getServiceActionFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("FW Update","onPause");
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver);
    }

    private Long getFwAddress(){
        try{
            long address = Long.decode(mAddressText.getText().toString());
            //clamp
            return Math.max(MIN_MEMORY_ADDRESS[mWbBoardType],Math.min(address,MAX_MEMORY_ADDRESS[mWbBoardType]));
        }catch (NumberFormatException e){
            return null;
        }
    }

    private void setupStartUploadButton(View button) {
        button.setOnClickListener(v -> {
            Long address = getFwAddress();
            @FirmwareType int selectedType = getSelectedFwType();
            FwVersion currentVersion = mVersionViewModel.getFwVersion().getValue();
            if(mSelectedFw!=null) {
                if(address!=null) {
                    startUploadFile(mSelectedFw, selectedType,address,currentVersion);
                }else{
                    Snackbar.make(mRootView,R.string.otaUpload_invalidMemoryAddress,Snackbar.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(mRootView,R.string.otaUpload_invalidFile,Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private @FirmwareType int getSelectedFwType() {
        if( mFirmwareTypeView.getCheckedRadioButtonId() == R.id.otaUpload_bleType)
            return FirmwareType.BLE_FW;
        else
            return FirmwareType.BOARD_FW;
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
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("FW Update","onActivityResult");
        onFileSelected(mRequestFile.onActivityResult(requestCode,resultCode,data));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestFile = new RequestFileUtil(this);
    }

    //private void onFileSelected(@Nullable Uri fwFile){
    public void onFileSelected(@Nullable Uri fwFile){
        Log.i("FW Update","onFileSelected"+fwFile);
        if(fwFile==null)
            return;
        mStartUploadButton.setEnabled(true);
        mSelectedFw = fwFile;
        mFileNameText.setText(RequestFileUtil.getFileName(requireContext(),fwFile));
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        mRequestFile.onRequestPermissionsResult(requestCode,permissions,grantResults);
//    }//onRequestPermissionsResult

    @Override
    public void onUploadFinished(float time_s) {
        NodeConnectionService.disconnect(requireContext(),mNode);
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string.otaUpload_completed,
                getString(R.string.otaUpload_finished,time_s));
        dialog.setOnclickListener((dialog1, which) -> {
            //UploadOtaFileFragment.this
            NavUtils.navigateUpFromSameTask(requireActivity());
        });
        dialog.show(getParentFragmentManager(),FINISH_DIALOG_TAG);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        wBBoardSelected(id);
    }

    void wBBoardSelected(long wB_Board) {
        if(wB_Board!=0) {
            if(mSelectedFw!=null) {
                //If there is a valid file
                mStartUploadButton.setEnabled(true);
            }
            //Check if we need to change the listeners to the Address Text
            if(mWbBoardType!=((int) wB_Board)) {
                mWbBoardType = (int) wB_Board;
                //Remove the previous Text watchers...
                if(watcher1!=null) {
                    mAddressText.removeTextChangedListener(watcher1);
                    mAddressText.setText("");
                }
                if(watcher2!=null) {
                    mAddressText.removeTextChangedListener(watcher2);
                }
                if(watcher3!=null) {
                    mAddressText.removeTextChangedListener(watcher3);
                }
                mAddressText.setText("");
                //Setup the Listener to the Address text with the new layout
                setupAddressText(mAddressText, mAddressLayout, mInitialAddress);
            }
        } else {
            mStartUploadButton.setEnabled(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
