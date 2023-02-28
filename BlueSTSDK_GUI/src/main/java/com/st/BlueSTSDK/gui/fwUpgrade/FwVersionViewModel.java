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
package com.st.BlueSTSDK.gui.fwUpgrade;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;

public class FwVersionViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsWaitingFwVersion = new MutableLiveData<>();
    private MutableLiveData<Boolean> mFwUpgradeSupported = new MutableLiveData<>();
    private MutableLiveData<FwVersionBoard> mFwUpgradeRequireUpdate = new MutableLiveData<>();
    private MutableLiveData<FwVersion> mFwVersion = new MutableLiveData<>();

    public LiveData<Boolean> isWaitingFwVersion(){
        return mIsWaitingFwVersion;
    }
    public LiveData<Boolean> supportFwUpgrade(){
        return mFwUpgradeSupported;
    }
    public LiveData<FwVersionBoard> requireManualUpdateTo(){
        return mFwUpgradeRequireUpdate;
    }

    public  LiveData<FwVersion> getFwVersion(){
        return mFwVersion;
    }

    private static final FwVersionBoard MIN_COMPATIBILITY_VERSION[] = new FwVersionBoard[]{
            new FwVersionBoard("BLUEMICROSYSTEM2","",2,0,1)
    };

    private void setFwIncompatibilityFlag(FwVersionBoard version){
        for(FwVersionBoard knowBoard : MIN_COMPATIBILITY_VERSION){
            if(version.getName().equals(knowBoard.getName())){
                if(version.compareTo(knowBoard)<0){
                    mFwUpgradeRequireUpdate.postValue(knowBoard);
                }//if
            }//if
        }//for
        mFwUpgradeRequireUpdate.postValue(null);
    }

    public void loadFwVersionFromNode(Node node){
        Log.d("LoadFwVersion","loadFw");
        if(mFwVersion.getValue()!=null)
            return;
        Log.d("LoadFwVersion","loadFw2");
        FwVersionConsole console = FwVersionConsole.getFwVersionConsole(node);
        if(console != null){
            Log.d("LoadFwVersion","console!=null");
            console.setLicenseConsoleListener((console1, type, version) -> {
                Log.d("LoadFwVersion","read "+version);
                mIsWaitingFwVersion.postValue(false);
                mFwUpgradeSupported.postValue(true);

                mFwVersion.postValue(version);
                if(version instanceof FwVersionBoard){
                    setFwIncompatibilityFlag((FwVersionBoard) version);
                }
                console1.setLicenseConsoleListener(null);
            });
            mIsWaitingFwVersion.postValue(true);
            console.readVersion(FirmwareType.BOARD_FW);
        }else{
            mFwUpgradeSupported.postValue(false);
        }
        //TODO else
    }

}
