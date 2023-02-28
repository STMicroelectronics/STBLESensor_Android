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
package com.st.BlueNRG.fwUpgrade;

import androidx.annotation.NonNull;

import com.st.BlueNRG.fwUpgrade.feature.ImageFeature;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;

public class FwVersionConsoleBlueNRG extends FwVersionConsole {

    private static final String DEFAULT_BOARD_NAME = "BLUENRG OTA";
    private static final String DEFAULT_MCU_NAME = "BLUENRG";

    private ImageFeature mRangeMem;

    public static FwVersionConsole buildForNode(Node node){
        ImageFeature rangeMem = node.getFeature(ImageFeature.class);
        if(rangeMem!=null) {
            return new FwVersionConsoleBlueNRG(null,rangeMem);
        }
        return null;
    }

    /**
     * @param callback object where notify the command answer
     */
    private FwVersionConsoleBlueNRG(FwVersionCallback callback, ImageFeature rangeMem) {
        super(callback);
        mRangeMem = rangeMem;

    }


    @Override
    public boolean readVersion(@FirmwareType int type) {
        Feature.FeatureListener onImageFeature = new Feature.FeatureListener(){
            @Override
            public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
                if(mCallback!=null) {
                    FwVersion protocolVer = mRangeMem.getProtocolVer(sample);
                    if(protocolVer!=null) {
                        FwVersionBoard version = new FwVersionBoard(DEFAULT_BOARD_NAME, DEFAULT_MCU_NAME,
                                protocolVer.getMajorVersion(), protocolVer.getMinorVersion(), protocolVer.getPatchVersion());
                        mCallback.onVersionRead(FwVersionConsoleBlueNRG.this, FirmwareType.BOARD_FW, version);
                    }else{
                        mCallback.onVersionRead(FwVersionConsoleBlueNRG.this, FirmwareType.BOARD_FW,  null);
                    }
                }
                mRangeMem.removeFeatureListener(this);
            }
        };
        mRangeMem.addFeatureListener(onImageFeature); // remember to removeFeatureListener when it is the last
        mRangeMem.getParentNode().readFeature(mRangeMem);
        return true;
    }
}
