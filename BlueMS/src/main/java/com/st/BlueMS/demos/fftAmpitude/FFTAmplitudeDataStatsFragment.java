package com.st.BlueMS.demos.fftAmpitude;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureMotorTimeParameter;

import java.util.List;

public class FFTAmplitudeDataStatsFragment extends DialogFragment {

    private FFTDataViewModel mFFTViewModel;
    private TimeDomainDataViewModel mTimeDomainViewModel;

    private TextView mFreqStats[] = new TextView[3];
    private TextView mTimeStatsX;
    private TextView mTimeStatsY;
    private TextView mTimeStatsZ;

    public FFTAmplitudeDataStatsFragment(){
        super();
        setStyle(STYLE_NORMAL,R.style.DialogWithTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        mFFTViewModel.getFFTMax().observe(this, new Observer<List<FFTDataViewModel.FFTPoint>>() {
            @Override
            public void onChanged(@Nullable List<FFTDataViewModel.FFTPoint> fftPoints) {
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
            }
        });
    }

    private void updateTimeDomainLable(TextView lablel, String name,@Nullable TimeDomainDataViewModel.TimeDomainStats data){
        if(data == null){
            lablel.setText(R.string.fftDetails_timeInfo_not_available);
        }else{
            String xData = getString(R.string.fftDetails_timeInfo_format,name,
                    data.accPeak, FeatureMotorTimeParameter.FEATURE_ACC_UNIT,
                    data.rmsSpeed,FeatureMotorTimeParameter.FEATURE_SPEED_UNIT);
            lablel.setText(xData);
        }
    }

    private void registerTimeDomainUpdate(){
        mTimeDomainViewModel.getXComponentStats().observe(this,
                timeDomainStats -> updateTimeDomainLable(mTimeStatsX,"X",timeDomainStats));

        mTimeDomainViewModel.getYComponentStats().observe(this,
                timeDomainStats -> updateTimeDomainLable(mTimeStatsY,"Y",timeDomainStats));

        mTimeDomainViewModel.getZComponentStats().observe(this,
                timeDomainStats -> updateTimeDomainLable(mTimeStatsZ,"Z",timeDomainStats));
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
