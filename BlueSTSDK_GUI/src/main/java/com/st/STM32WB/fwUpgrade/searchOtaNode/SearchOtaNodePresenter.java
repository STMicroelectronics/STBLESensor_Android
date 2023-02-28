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
package com.st.STM32WB.fwUpgrade.searchOtaNode;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;

public class SearchOtaNodePresenter implements SearchOtaNodeContract.Presenter{

    private static final int SCANNER_TIMEOUT_MS = 10*1000;


    private SearchOtaNodeContract.View mView;
    private Manager mManager;
    private @Nullable String mAddress;
    private Context  mContext;

    private Manager.ManagerListener mManagerListener = new Manager.ManagerListener() {
        @Override
        public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {
            if(!enabled){
                mManager.removeListener(this);
                mView.nodeNodeFound();
            }else{
                mView.startScan();
            }
        }

        @Override
        public void onNodeDiscovered(@NonNull Manager m,@NonNull  Node node) {
            if(STM32OTASupport.isOTANode(node,mContext)){
                if(node.getTag().equals(mAddress) || mAddress==null) {
                    mManager.removeListener(this);
                    mView.foundNode(node);
                    mManager.stopDiscovery();
                }
            }
        }
    };

    public SearchOtaNodePresenter(SearchOtaNodeContract.View view, Manager manager, Context ctx){
        mView = view;
        mManager = manager;
        mAddress = null;
        mContext = ctx;
    }

    @Override
    public void startScan(@Nullable String address) {
        mAddress = address;
        mManager.resetDiscovery();
        mManager.addListener(mManagerListener);
        mManager.startDiscovery(SCANNER_TIMEOUT_MS);
    }

    @Override
    public void stopScan() {
        mManager.stopDiscovery();
    }
}
