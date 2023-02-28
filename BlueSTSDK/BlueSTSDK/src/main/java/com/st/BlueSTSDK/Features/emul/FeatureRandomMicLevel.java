package com.st.BlueSTSDK.Features.emul;

import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;

import java.util.Random;

/**
 * generate random data for emulate the class {@link FeatureMicLevel} it will emulate
 * {@code NUMBER_FAKE_MICROPHONE} microphones
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureRandomMicLevel extends FeatureMicLevel implements NodeEmulator.EmulableFeature {
    public static final int NUMBER_FAKE_MICROPHONE=3;

    private Random mRnd = new Random();

    public FeatureRandomMicLevel(Node parent) {
        super(parent);
    }

    @Override
    public byte[] generateFakeData() {

        byte data[] = new byte[NUMBER_FAKE_MICROPHONE];
        for(int i=0;i<NUMBER_FAKE_MICROPHONE;i++){
            data[i] =(byte) mRnd.nextInt(FeatureMicLevel.DATA_MAX);
        }//for i

        return data;
    }
}