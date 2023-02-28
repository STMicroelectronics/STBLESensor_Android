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
package com.st.STM32WB.fwUpgrade.statOtaConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeConnectionService;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.STM32WB.fwUpgrade.FwUpgradeSTM32WBActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.RequestFileUtil;
import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;


@DemoDescriptionAnnotation(name="Firmware Upgrade",
        demoCategory = {"Control"},
        requireAll = {RebootOTAModeFeature.class}
        //inside a lib the R file is not final so you can not set the icon, to do it extend this
        //this class in the main application an set a new annotation
        )
public class StartOtaRebootFragment extends DemoFragment implements StartOtaConfigContract.View, AdapterView.OnItemSelectedListener {

    private OtaConfigViewModel mOtaViewModel;
    private Node mNode;


    private static final class MemoryLayout{
        final short fistSector;
        final short nSector;
        final short sectorSize;

        private MemoryLayout(short fistSector, short nSector,short sectorSize) {
            this.fistSector = fistSector;
            this.nSector = nSector;
            this.sectorSize = sectorSize;
        }
    }

    // IMPORTANT!!!!!!
    // the code works thinking that the SectorSize of the
    // Application Memory is == to the one for BLE Memory
    // And also that the fistSector is the same for them
    private static final MemoryLayout APPLICATION_MEMORY[] = {
            new MemoryLayout((short)0x00,(short) 0x00,(short) 0x0000 /*   */), //Undef
            new MemoryLayout((short)0x07,(short) 0x7F,(short) 0x1000 /* 4k*/), //WB55
            new MemoryLayout((short)0x0E,(short) 0x24,(short) 0x800  /* 2k*/)  //WB15
    };

    private static final MemoryLayout BLE_MEMORY[] = {
            new MemoryLayout((short)0x000,(short) 0x00,(short) 0x0000 /*   */), //Undef
            new MemoryLayout((short)0x0F,(short) 0x7F,(short) 0x1000  /* 4k*/), //WB55
            new MemoryLayout((short)0x0E,(short) 0x3C,(short) 0x800   /* 2k*/)  //WB15
    };

    private StartOtaConfigContract.Presenter mPresenter;
    private RequestFileUtil mRequestFileUtil;
    private View mCustomAddressView;

    private TextView mSelectedFwName;

    private TextInputLayout mSectorTextLayout;
    private TextInputLayout mLengthTextLayout;
    private TextView mOtaReboot_file;

    private TextView mOtaReboot_description;
    private RadioGroup mOtaReboot_fwTypeGroup;
    private Button mSelectFileButton;

    private CompoundButton mApplicationMemory;
    private CompoundButton mBleMemory;
    CompoundButton mCustomMemory;


    private FloatingActionButton mOtaRebootFab;

    private TextWatcher watcherSectorTextLayout;
    private TextWatcher watcherLengthTextLayout;

    private Spinner mSelectBoardSpinner;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        wBBoardSelected((int) id);
    }

    void wBBoardSelected(int wB_board) {
        if(wB_board!=0) {
            setUpTextWatcher();
        } else {
            mOtaReboot_description.setVisibility(View.GONE);
            mOtaReboot_fwTypeGroup.setVisibility(View.GONE);
        }

        if(mOtaViewModel!=null) {
            mOtaViewModel.set_WB_board(wB_board);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_ota_reboot, container, false);

        mApplicationMemory = mRootView.findViewById(R.id.otaReboot_appMemory);
        mBleMemory = mRootView.findViewById(R.id.otaReboot_bleMemory);
        mCustomMemory = mRootView.findViewById(R.id.otaReboot_customMemory);

        mCustomAddressView = mRootView.findViewById(R.id.otaReboot_customAddrView);
        mSectorTextLayout = mRootView.findViewById(R.id.otaReboot_sectorLayout);
        mLengthTextLayout = mRootView.findViewById(R.id.otaReboot_lengthLayout);
        mSelectedFwName = mRootView.findViewById(R.id.otaReboot_fwFileName);

        mSelectBoardSpinner = mRootView.findViewById(R.id.otaReboot_boardSelection);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.wb_board_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectBoardSpinner.setAdapter(adapter);
        mSelectBoardSpinner.setOnItemSelectedListener(this);

        mOtaReboot_description = mRootView.findViewById(R.id.otaReboot_description);
        mOtaReboot_fwTypeGroup = mRootView.findViewById(R.id.otaReboot_fwTypeGroup);

        mSelectFileButton = mRootView.findViewById(R.id.otaReboot_selectFileButton);
        mSelectFileButton.setOnClickListener(v -> mPresenter.onSelectFwFilePressed());

        //mRequestFileUtil = new RequestFileUtil(this, mRootView);
        mRequestFileUtil.setRootView(mRootView);
        mOtaReboot_file = mRootView.findViewById(R.id.otaReboot_file);

        /* Listener for RadioGroup */
        mApplicationMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSelectFileButton.setVisibility(View.VISIBLE);
        });
        mBleMemory.setOnCheckedChangeListener((buttonView, isChecked) ->{
            mSelectFileButton.setVisibility(View.VISIBLE);
        });
        mCustomMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setUpUiForCustomMemory(isChecked);
        });

        mOtaRebootFab =  mRootView.findViewById(R.id.otaReboot_fab);
        mOtaRebootFab.setOnClickListener(v -> mPresenter.onRebootPressed());

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Retrieve the ViewModel
        mOtaViewModel = new ViewModelProvider(requireActivity()).get(OtaConfigViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestFileUtil = new RequestFileUtil(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("StartOtaRebootFragment","onResume Read data from ViewModel");
        /* Update UI */
        int mWB_board = mOtaViewModel.get_WB_board();
        if(mWB_board!=0) {
            mSelectBoardSpinner.setSelection(mWB_board);
            setUpTextWatcher();
        }

        int firmware_type = mOtaViewModel.get_FirmwareType();

        if(firmware_type == FirmwareType.BOARD_FW) {
            mApplicationMemory.setChecked(true);
        } else if(firmware_type == FirmwareType.BLE_FW) {
            mBleMemory.setChecked(true);
        } else {
            //Could be the Custom Memory layout
            mCustomMemory.setChecked(mOtaViewModel.get_CustomMemory());
            short firstSector = mOtaViewModel.get_firstSector();
            if(firstSector!=-1) {
                mSectorTextLayout.getEditText().setText(String.valueOf(firstSector));
            }
            short numSectors = mOtaViewModel.get_MaxSectorSelected();
            if(numSectors!=-1) {
                mLengthTextLayout.getEditText().setText(String.valueOf(numSectors));
            }
        }

        Uri mSelectedFw = mOtaViewModel.get_SelectedFw();
        if(mSelectedFw!=null){
            UpdateUiForFileSelected(mSelectedFw);
        }
    }

    private void setUpTextWatcher() {
        if(watcherSectorTextLayout!=null) {
            mSectorTextLayout .getEditText().removeTextChangedListener(watcherSectorTextLayout);
        }
        if(watcherLengthTextLayout!=null) {
            mLengthTextLayout .getEditText().removeTextChangedListener(watcherLengthTextLayout);
        }
//        mSectorTextLayout.getEditText().setText("");
//        mLengthTextLayout.getEditText().setText("");

        //Add the text watcher
        setUpSectorInputChecker(mSectorTextLayout);
        setUpLengthInputChecker(mLengthTextLayout);

        mOtaReboot_description.setVisibility(View.VISIBLE);
        mOtaReboot_fwTypeGroup.setVisibility(View.VISIBLE);
    }

    private void setUpUiForCustomMemory(boolean isChecked) {
        mCustomAddressView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mSelectFileButton.setVisibility(View.VISIBLE);
        mOtaViewModel.set_CustomMemory(isChecked);
        if(isChecked) {
            setUpSectorInputChecker(mSectorTextLayout);
            setUpLengthInputChecker(mLengthTextLayout);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i("StartOtaRebootFragment","onSaveInstanceState Save data to ViewModel");
        if(mOtaViewModel.get_WB_board()!=0) {
            //If we have selected one WB board Type
            if (mSectorTextLayout.getEditText() != null) {
                short firstSector = getSectorToDelete();
                mOtaViewModel.set_FirstSector(firstSector);
            }
            if (mLengthTextLayout.getEditText() != null) {
                short numSectors = getNSectorToSaveForUI();
                mOtaViewModel.set_MaxSectorSelected(numSectors);
            }

            if(mApplicationMemory.isChecked()) {
                mOtaViewModel.set_FirmwareType(FirmwareType.BOARD_FW);
            } else if(mBleMemory.isChecked()) {
                mOtaViewModel.set_FirmwareType(FirmwareType.BLE_FW);
            } else {
                mOtaViewModel.set_FirmwareType(-1);
            }
        }
        super.onSaveInstanceState(outState);
    }

    private void UpdateUiForFileSelected(Uri fileSelected) {
        mOtaViewModel.set_SelectedFw(fileSelected);
        String fileName = RequestFileUtil.getFileName(requireContext(),fileSelected);
        mSelectedFwName.setVisibility(View.VISIBLE);
        mOtaReboot_file.setVisibility(View.VISIBLE);
        mSelectedFwName.setText(fileName);
        mOtaRebootFab.setEnabled(true);
    }

    //Checks the Number of sectors delete
    private void setUpLengthInputChecker(TextInputLayout lengthTextLayout) {
        EditText text = lengthTextLayout.getEditText();
        if(text!=null) {
            int mWB_board = mOtaViewModel.get_WB_board();
            //we make the test taking the bigger between BLE and Application Memory
            short maxValue = (short) Math.max(BLE_MEMORY[mWB_board].nSector,APPLICATION_MEMORY[mWB_board].nSector);
            watcherSectorTextLayout = new CheckNumberRange(lengthTextLayout, R.string.otaReboot_lengthOutOfRange, 0x00, maxValue);
            text.addTextChangedListener(watcherSectorTextLayout);
        }
    }

    //Checks the first sector to delete
    private void setUpSectorInputChecker(TextInputLayout sectorTextLayout) {
        EditText text = sectorTextLayout.getEditText();
        if(text!=null) {
            int mWB_board = mOtaViewModel.get_WB_board();
            //The code works thinking that the first sector is the same for both Application and BLE memory
            short maxValue = (short) Math.max(BLE_MEMORY[mWB_board].nSector,APPLICATION_MEMORY[mWB_board].nSector);
            watcherLengthTextLayout = new CheckNumberRange(sectorTextLayout, R.string.otaReboot_sectorOutOfRange,
                    APPLICATION_MEMORY[mWB_board].fistSector,
                    APPLICATION_MEMORY[mWB_board].fistSector+maxValue);

            text.addTextChangedListener(watcherLengthTextLayout);
        }
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        RebootOTAModeFeature feature = node.getFeature(RebootOTAModeFeature.class);
        mNode = node;

        if((mNode.getType()== Node.Type.ASTRA1) || (mNode.getType()== Node.Type.PROTEUS) || (mNode.getType() == Node.Type.STDES_CBMLORABLE)) {
            //For Polaris/Proteus
            mSelectBoardSpinner.setSelection(1);
            wBBoardSelected(1);
            mSelectBoardSpinner.setEnabled(false);
        }

        mPresenter = new StartOtaRebootPresenter(this,feature);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {

    }

    @Override
    public short getSectorToDelete() {
        int mWB_board = mOtaViewModel.get_WB_board();
        if(mApplicationMemory.isChecked() || mSectorTextLayout.getEditText() == null)
            return APPLICATION_MEMORY[mWB_board].fistSector;
        if(mBleMemory.isChecked() || mSectorTextLayout.getEditText() == null){
            return BLE_MEMORY[mWB_board].fistSector;
        }
        try {
            return Short.parseShort(mSectorTextLayout.getEditText().getText().toString(), 10);
        }catch (NumberFormatException e){
            return APPLICATION_MEMORY[mWB_board].fistSector;
        }

    }

    public short getNSectorToSaveForUI() {
        short MaxSectorSelected;
        try{
            MaxSectorSelected =  Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),10);
        } catch (NumberFormatException e){
            MaxSectorSelected =  0;
        }
        return MaxSectorSelected;
    }

    @Override
    public short getNSectorToDelete() {

        //Variable Number taking into account also the File size
        //File Size in Bytes
        long FileDimension = RequestFileUtil.getFileSize(requireContext(),mOtaViewModel.get_SelectedFw());
        int mWB_board = mOtaViewModel.get_WB_board();
        //File Size in Sectors
        //IMPORTANT the code works only if the sector sze of APPLICATION_MEMORY== BLE_MEMORY
        short SectorsForFileDimension = (short) ((FileDimension+APPLICATION_MEMORY[mWB_board].sectorSize-1)/(APPLICATION_MEMORY[mWB_board].sectorSize));

        short MaxSectorSelected;

        //Return the Minimum Number
        if(mApplicationMemory.isChecked() || mLengthTextLayout.getEditText() == null) {
            MaxSectorSelected = APPLICATION_MEMORY[mWB_board].nSector;
            return (SectorsForFileDimension < MaxSectorSelected) ? SectorsForFileDimension : MaxSectorSelected;
        }
        if(mBleMemory.isChecked() || mLengthTextLayout.getEditText() == null) {
            MaxSectorSelected = BLE_MEMORY[mWB_board].nSector;
            return (SectorsForFileDimension < MaxSectorSelected) ? SectorsForFileDimension : MaxSectorSelected;
        }

        try{
            MaxSectorSelected =  Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),10);
        } catch (NumberFormatException e){
            MaxSectorSelected =  APPLICATION_MEMORY[mWB_board].nSector;
        }

        return (SectorsForFileDimension < MaxSectorSelected ) ? SectorsForFileDimension : MaxSectorSelected ;

    }

    private @FirmwareType int getSelectedFwType(){
        if(mBleMemory.isChecked()){
            return FirmwareType.BLE_FW;
        }else{
            return FirmwareType.BOARD_FW;
        }
    }

    @Override
    public void openFileSelector() {
        mRequestFileUtil.openFileSelector();
    }

    @Override
    public void performFileUpload() {
        Node n = getNode();
        if(n == null)
            return;

        NodeConnectionService.disconnect(requireContext(),getNode());
        long address = sectorToAddress(getSectorToDelete());
        int mWB_board = mOtaViewModel.get_WB_board();
        startActivity(FwUpgradeSTM32WBActivity.getStartIntent(requireContext(),
                STM32OTASupport.getOtaAddressForNode(getNode()),
                mOtaViewModel.get_SelectedFw(),address,getSelectedFwType(),mWB_board));
    }

    private long sectorToAddress(short sectorToDelete) {
        // IMPORTANT!!!!!!
        // the code works thinking that the SectorSize of the
        // Application Memory is == to the one for BLE Memory
        // so it uses the BLE_MEMORY one for both the programs
        int mWB_board = mOtaViewModel.get_WB_board();
        return sectorToDelete*BLE_MEMORY[mWB_board].sectorSize;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri mSelectedFw;
        mSelectedFw = mRequestFileUtil.onActivityResult(requestCode,resultCode,data);
        mOtaViewModel.set_SelectedFw(mSelectedFw);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        mRequestFileUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
//    }//onRequestPermissionsResult

}
