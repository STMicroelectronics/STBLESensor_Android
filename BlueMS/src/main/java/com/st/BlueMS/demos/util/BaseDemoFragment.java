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
package com.st.BlueMS.demos.util;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.st.BlueMS.demos.PnPL.PnPLSettingsDialogFragment;
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.Arrays;

public abstract class BaseDemoFragment extends DemoFragment {


    private static final String PNPL_SETTINGS_DIALOG_TAG = BaseDemoFragment.class.getCanonicalName() + ".PNPL_SETTINGS_DIALOG_TAG";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pnpl_option_menu, menu);

        Node mNode = getNode();
        if(mNode!=null) {
            if(!mNode.getFeatures(FeaturePnPL.class).isEmpty()) {
                if(getNode().getDTDLModel()!=null) {
                    String pnplName = getClass().getAnnotation(DemoDescriptionAnnotation.class).name().toLowerCase().replace(' ','_');
                    if(!pnplName.isEmpty()) {
                        if (mNode.getDTDLModel().contains(pnplName)) {
                            menu.findItem(R.id.demo_settings).setVisible(true);
                        }
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.demo_settings) {
            Node mNode = getNode();
            String pnplName = getClass().getAnnotation(DemoDescriptionAnnotation.class).name().toLowerCase().replace(' ','_');
            if((mNode!=null) && (!pnplName.isEmpty())) {
                PnPLSettingsDialogFragment sensorConfig;
                sensorConfig = new PnPLSettingsDialogFragment(getNode(), Arrays.asList(pnplName), false);
                sensorConfig.show(getParentFragmentManager(),PNPL_SETTINGS_DIALOG_TAG);
            }
            return true;
        }//else
        return super.onOptionsItemSelected(item);
    }

}
