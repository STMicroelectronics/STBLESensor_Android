package com.st.BlueMS.demos.fftAmpitude;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

        FFTViewModel.getFftData().observe(this, fftData -> {
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
