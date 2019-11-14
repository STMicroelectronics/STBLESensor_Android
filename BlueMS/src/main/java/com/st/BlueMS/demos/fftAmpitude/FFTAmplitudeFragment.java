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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.fftAmpitude.settings.FFTSettingsFragment;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.List;

@DemoDescriptionAnnotation(name = "FFTAmplitude", iconRes = R.drawable.demo_charts,
        requareAll = {FeatureFFTAmplitude.class})
public class FFTAmplitudeFragment extends BaseDemoFragment {

    private static final String FFT_PLOT_TAG = "FFTPLOT";
    private static final String FFT_LOG_FILE_EXTRA = FFTAmplitudeFragment.class.getCanonicalName()+".FFT_LOG_FILE_EXTRA";
    private FFTDataViewModel FFTViewModel;


    public FFTAmplitudeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        //displaySettings();
        FFTViewModel =
                ViewModelProviders.of(requireActivity()).get(FFTDataViewModel.class);
        TimeDomainDataViewModel timeDomainViewModel =
                ViewModelProviders.of(requireActivity()).get(TimeDomainDataViewModel.class);
        FFTViewModel.startListenDataFrom(node);
        timeDomainViewModel.startListenDataFrom(node);

        FFTViewModel.getFftData().observe(getViewLifecycleOwner(), fftData -> {
            Float frequencyStep = FFTViewModel.getFrequencyStep().getValue();
            if(frequencyStep!=null && fftData!=null) {
                logFFTData(fftData,frequencyStep);
            }
        });

    }

    private void logFFTData(List<float[]> fftData, Float frequencyStep) {
        LogFeatureActivity activity = (LogFeatureActivity)requireActivity();
        if(activity.isLogging()){
            String file = createLogFilePath(activity);
            String nodeName = getNode().getFriendlyName();
            FFTExportedService.startExport(requireContext(),file,nodeName,fftData,frequencyStep);
        }else{
            mLogFilePath = null;
        }
    }

    private String createLogFilePath(LogFeatureActivity activity) {
        if(mLogFilePath!=null)
            return  mLogFilePath;
        String path = LogFeatureActivity.getLogDirectory();
        String logPrefixName = activity.getLoggingSessionPrefix();
        mLogFilePath = String.format("%s/%s_FFT.csv", path, logPrefixName);
        return mLogFilePath;
    }

    private String mLogFilePath;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLogFilePath != null)
            outState.putString(FFT_LOG_FILE_EXTRA,mLogFilePath);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null)
            mLogFilePath = savedInstanceState.getString(FFT_LOG_FILE_EXTRA,null);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        FFTDataViewModel FFTViewModel =
                ViewModelProviders.of(requireActivity()).get(FFTDataViewModel.class);
        TimeDomainDataViewModel timeDomainViewModel =
                ViewModelProviders.of(requireActivity()).get(TimeDomainDataViewModel.class);
        FFTViewModel.stopListenDataFrom();
        timeDomainViewModel.stopListenDataFrom();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_fftamplitude, container, false);
        FragmentManager fm = getChildFragmentManager();
        if(fm.findFragmentByTag(FFT_PLOT_TAG)==null) {
            fm.beginTransaction()
                    .add(R.id.fftAmpl_rootView, FFTAmplitudePlotFragment.newInstance(), FFT_PLOT_TAG)
                    .commit();
        }
        return root;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fft_amplitude,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_fft_amplitude_settings){
            displaySettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displaySettings() {
        Node node = getNode();
        if (node != null) {
            stopDemo();
            Fragment settings = FFTSettingsFragment.newInstance(node);
            FragmentManager fm = getChildFragmentManager();
            fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if(fm.getBackStackEntryCount()==0) {
                        startDemo();
                        fm.removeOnBackStackChangedListener(this);
                    }
                }
            });
            fm.beginTransaction()
                    .replace(R.id.fftAmpl_rootView, settings,FFT_PLOT_TAG)
                    .addToBackStack(null)
                    .commit();
        }

    }

}
