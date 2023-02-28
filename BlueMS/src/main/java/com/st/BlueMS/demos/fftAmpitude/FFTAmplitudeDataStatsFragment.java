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
package com.st.BlueMS.demos.fftAmpitude;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureMotorTimeParameter;

/**
 * dialog showing the fft statistics
 */
public class FFTAmplitudeDataStatsFragment extends DialogFragment {

    /**
     * view model containing the fft data to show
     * this fragment is shared with the plot fragment
     */
    private FFTDataViewModel mFFTViewModel;

    /**
     * view model containing the time domain data
     * this fragment is shared with the plot fragment
     */
    private TimeDomainDataViewModel mTimeDomainViewModel;

    private TextView[] mFreqStats = new TextView[3];
    private TextView mTimeStatsX;
    private TextView mTimeStatsY;
    private TextView mTimeStatsZ;

    public FFTAmplitudeDataStatsFragment(){
        super();
        setStyle(STYLE_NORMAL,R.style.DialogWithTitle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.fftDetails_title);

        View root = inflater.inflate(R.layout.fragment_fft_stat, container, false);

        mFreqStats[0] = root.findViewById(R.id.fftAmpl_xData);
        mFreqStats[1] = root.findViewById(R.id.fftAmpl_yData);
        mFreqStats[2] = root.findViewById(R.id.fftAmpl_zData);

        mTimeStatsX = root.findViewById(R.id.fftAmpl_xTimeData);
        mTimeStatsY = root.findViewById(R.id.fftAmpl_yTimeData);
        mTimeStatsZ = root.findViewById(R.id.fftAmpl_zTimeData);

        return root;
    }

    private void registerFFTUpdate(){
        mFFTViewModel.getFFTMax().observe(getViewLifecycleOwner(), fftPoints -> {
            if(fftPoints==null)
                return;
            FFTComponentsConfig.LineConf[] lines = FFTComponentsConfig.LINES;

            int nComponents = Math.min(fftPoints.size(),lines.length);
            nComponents = Math.min(nComponents,mFreqStats.length);

            for (int i = 0; i <nComponents ; i++) {
                FFTDataViewModel.FFTPoint max = fftPoints.get(i);
                mFreqStats[i].setText(getString(R.string.fftDetails_freqInfo_format,
                        lines[i].name,max.amplitude,max.frequency));
            }
        });
    }

    private void updateTimeDomainLabel(TextView label, String name, @Nullable TimeDomainDataViewModel.TimeDomainStats data){
        if(data == null){
            label.setText(R.string.fftDetails_timeInfo_not_available);
        }else{
            String xData = getString(R.string.fftDetails_timeInfo_format,name,
                    data.accPeak, FeatureMotorTimeParameter.FEATURE_ACC_UNIT,
                    data.rmsSpeed,FeatureMotorTimeParameter.FEATURE_SPEED_UNIT);
            label.setText(xData);
        }
    }

    private void registerTimeDomainUpdate(){
        mTimeDomainViewModel.getXComponentStats().observe(getViewLifecycleOwner(),
                timeDomainStats -> updateTimeDomainLabel(mTimeStatsX,"X",timeDomainStats));

        mTimeDomainViewModel.getYComponentStats().observe(getViewLifecycleOwner(),
                timeDomainStats -> updateTimeDomainLabel(mTimeStatsY,"Y",timeDomainStats));

        mTimeDomainViewModel.getZComponentStats().observe(getViewLifecycleOwner(),
                timeDomainStats -> updateTimeDomainLabel(mTimeStatsZ,"Z",timeDomainStats));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFFTViewModel = ViewModelProviders.of(requireActivity()).get(FFTDataViewModel.class);
        mTimeDomainViewModel = ViewModelProviders.of(requireActivity()).get(TimeDomainDataViewModel.class);
        registerFFTUpdate();
        registerTimeDomainUpdate();
    }
}
