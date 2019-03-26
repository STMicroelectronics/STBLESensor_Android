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

package com.st.BlueMS.demos.PredictiveMaintenance;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveAccelerationStatus;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveFrequencyDomainStatus;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveSpeedStatus;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

@DemoDescriptionAnnotation(name = "Predictive Maintenance", requareOneOf = {
        FeaturePredictiveSpeedStatus.class,
        FeaturePredictiveFrequencyDomainStatus.class,
        FeaturePredictiveAccelerationStatus.class
}, iconRes = R.drawable.predictive_demo_icon)
public class PredictiveMaintenanceFragment extends BaseDemoFragment {


    private PredictiveMaintenanceViewModel mViewModel;

    private PredictiveStatusView mSpeedStatusView;
    private PredictiveStatusView mAccelerationStatusView;
    private PredictiveStatusView mFrequencyStatusView;

    public PredictiveMaintenanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_predictive_maintenance, container, false);

        mSpeedStatusView = root.findViewById(R.id.predictive_speedStatus);
        mSpeedStatusView.setVisibility(View.GONE);
        mFrequencyStatusView = root.findViewById(R.id.predictive_frequencyDomainStatus);
        mFrequencyStatusView.setVisibility(View.GONE);
        mAccelerationStatusView = root.findViewById(R.id.predictive_accelerationStatus);
        mAccelerationStatusView.setVisibility(View.GONE);

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PredictiveMaintenanceViewModel.class);

        mViewModel.getAccStatus().observe(this, viewStatus -> {
            if(viewStatus!=null){
                mAccelerationStatusView.updateStatus(viewStatus);
            }
        });

        mViewModel.getAccStatusVisibility().observe(this, isVisible -> {
            if(isVisible!=null && isVisible){
                mAccelerationStatusView.setVisibility(View.VISIBLE);
            }else{
                mAccelerationStatusView.setVisibility(View.GONE);
            }
        });

        mViewModel.getFrequencyStatus().observe(this, viewStatus -> {
            if(viewStatus!=null){
                mFrequencyStatusView.updateStatus(viewStatus);
            }
        });

        mViewModel.getFrequencyStatusVisibility().observe(this, isVisible -> {
            if(isVisible!=null && isVisible){
                mFrequencyStatusView.setVisibility(View.VISIBLE);
            }else{
                mFrequencyStatusView.setVisibility(View.GONE);
            }
        });

        mViewModel.getSpeedStatus().observe(this, viewStatus -> {
            if(viewStatus!=null){
                mSpeedStatusView.updateStatus(viewStatus);
            }
        });

        mViewModel.getSpeedStatusVisibility().observe(this, isVisible -> {
            if(isVisible!=null && isVisible){
                mSpeedStatusView.setVisibility(View.VISIBLE);
            }else{
                mSpeedStatusView.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        if(mViewModel!=null){
            mViewModel.enableNotification(node);
        }

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mViewModel!=null){
            mViewModel.disableNotification(node);
        }
    }

}
