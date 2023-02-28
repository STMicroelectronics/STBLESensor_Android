/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of STMicroelectronics nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.st.BlueSTSDK.Features;

import androidx.annotation.IntDef;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Feature that contains the beamforming current direction and a list of beamforming control commands.
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureBeamforming extends Feature {

    @IntDef({Direction.TOP,Direction.TOP_RIGHT,
            Direction.RIGHT,Direction.BOTTOM_RIGHT,Direction.BOTTOM,Direction.BOTTOM_LEFT,
            Direction.LEFT,Direction.TOP_LEFT,Direction.UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
        int TOP=1;
        int TOP_RIGHT=2;
        int RIGHT=3;
        int BOTTOM_RIGHT=4;
        int BOTTOM=5;
        int BOTTOM_LEFT=6;
        int LEFT=7;
        int TOP_LEFT=8;
        int UNKNOWN=(byte)(0xFF);
    }

    private static final String FEATURE_NAME = "Beamforming";

    private static final String FEATURE_UNIT = null;
    private static final String FEATURE_DATA_NAME = "Beamforming";
    private static final float DATA_MAX = 7;
    private static final float DATA_MIN = 0;

    protected static final Field BEAMFORMING_FIELD =
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.Float, DATA_MAX, DATA_MIN);

    //NOTE BF - toggle /////////////////////////////////////////////////////////////////////////////
    /** Beamforming command Type*/
    private static final byte BF_COMMAND_TYPE_ONOFF = (byte)0xAA;
    /** Disable Beamforming command*/
    private static final byte COMMAND_DISABLE_BF[] = {0x00};
    /** Enable Beamforming command*/
    private static final byte COMMAND_ENABLE_BF[] = {0x01};
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    //NOTE BF - direction //////////////////////////////////////////////////////////////////////////
    /** Beamforming command Type*/
    private static final byte BF_COMMAND_TYPE_CHANGEDIR = (byte)0xBB;
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    //NOTE BF - toggle /////////////////////////////////////////////////////////////////////////////
    /** Beamforming command Type*/
    private static final byte BF_COMMAND_TYPE_CHANGE_TYPE = (byte)0xCC;
    /** Disable Beamforming command*/
    private static final byte COMMAND_ASR_READY_BF[] = {0x00};
    /** Enable Beamforming command*/
    private static final byte COMMAND_STRONG_BF[] = {0x01};
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * build a Feature of type FeatureBeamforming
     *
     * @param n node that will send data to this feature
     */
    public FeatureBeamforming(Node n) {
        super(FEATURE_NAME, n, new Field[]{ BEAMFORMING_FIELD });
    }//FeatureHumidity

    protected FeatureBeamforming(String name, Node n,Field data[]) {
        super(name,n,data);
        if(data[0]!=BEAMFORMING_FIELD){
            throw new IllegalArgumentException("First data[0] must be FeatureBeamforming" +
                    ".BEAMFORMING_FIELD");
        }//if
    }


    public static @Direction int getDirection(Sample s){
        if(hasValidIndex(s,0)) {
            @Direction int temp =s.data[0].byteValue();
            return temp;
        }
        return Direction.UNKNOWN;
    }

    /**
     * enable/disable beamforming
     * @param newStatus true to enable the beamforming
     * @return true if the command is correctly send
     */
    public boolean enableBeamForming(boolean newStatus){
        if(newStatus)
            return sendCommand(BF_COMMAND_TYPE_ONOFF,COMMAND_ENABLE_BF);
        else
            return sendCommand(BF_COMMAND_TYPE_ONOFF,COMMAND_DISABLE_BF);
    }

    /**
     * select beamforming direction
     * @param direction new beamforming direction
     * @return true if the command is correctly send
     */
    public boolean setBeamFormingDirection(@Direction int direction){
        byte data[] = new byte[]{(byte)direction};
        return sendCommand(BF_COMMAND_TYPE_CHANGEDIR,data);
    }

    /**
     * use the "Strong" beam forming algorithm
     * @param enable true to enable the strong algorithm, false to disable it
     * @return true if the command is correctly send
     */
    public boolean useStrongBeamformingAlgorithm(boolean enable){
        if(enable)
            return sendCommand(BF_COMMAND_TYPE_CHANGE_TYPE,COMMAND_STRONG_BF);
        else
            return sendCommand(BF_COMMAND_TYPE_CHANGE_TYPE,COMMAND_ASR_READY_BF);
    }

    /**
     * extract the beamforming direction from the node raw data, it will read a uint8 that
     * contains the bf direction value.
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the beamforming direction information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if((data.length-dataOffset)<1){
            throw new IllegalArgumentException("There are no byte available to read");
        }

        Sample beamDir = new Sample(new Number[]{data[dataOffset]},getFieldsDesc());
        return new ExtractResult(beamDir,1);

    }//update
}
