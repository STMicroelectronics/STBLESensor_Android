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

package com.st.BlueSTSDK.Features;


import androidx.annotation.IntDef;

import android.util.SparseArray;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FeatureSDLogging  extends Feature{

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "SDLogging";

     public static final String FEATURE_UNIT[] = {null,null,"s"};
     public static final String FEATURE_DATA_NAME[] ={"isEnabled","loggedFeature","logInterval"} ;
     public static final Number DATA_MAX[] = {1,1L<<32-1,1L<<32-1};
     public static final Number DATA_MIN[] = {0,0,0};

    public static final int LOG_ENABLE_INDEX = 0;
    public static final int FEATURE_ENABLED_INDEX = 1;
    public static final int LOG_INTERVAL_INDEX = 2;

    /**
     * enum for choose the type of firmware to upload
     */
    @IntDef({LOGGING_STOPPED, LOGGING_STARTED,LOGGING_NO_SD, LOGGING_IO_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoggingStatus {}

    public static final int LOGGING_STOPPED=0;
    public static final int LOGGING_STARTED=1;
    public static final int LOGGING_NO_SD=2;
    public static final int LOGGING_IO_ERROR =0xFF;


    public static boolean isLogging(Sample s){
        return getLoggingStatus(s)==LOGGING_STARTED;
    }

    public static @LoggingStatus int getLoggingStatus(Sample s){
        if(hasValidIndex(s,LOG_ENABLE_INDEX))
            return s.data[LOG_ENABLE_INDEX].intValue();
        return LOGGING_IO_ERROR;
    }

    public static long getLogInterval(Sample s){
        if(hasValidIndex(s,LOG_INTERVAL_INDEX))
            return s.data[LOG_INTERVAL_INDEX].longValue();
        return -1;
    }

    public static Set<Feature> getLoggedFeature(Node node,Sample s){
        if (hasValidIndex(s,FEATURE_ENABLED_INDEX))
            return buildFeatureSet(node,s.data[FEATURE_ENABLED_INDEX].longValue());
        return Collections.emptySet();
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeatureSDLogging(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[LOG_ENABLE_INDEX],
                        FEATURE_UNIT[LOG_ENABLE_INDEX], Field.Type.UInt8,
                        DATA_MAX[LOG_ENABLE_INDEX],
                        DATA_MIN[LOG_ENABLE_INDEX]),
                new Field(FEATURE_DATA_NAME[FEATURE_ENABLED_INDEX],
                        FEATURE_UNIT[FEATURE_ENABLED_INDEX], Field.Type.UInt32,
                        DATA_MAX[FEATURE_ENABLED_INDEX],
                        DATA_MIN[FEATURE_ENABLED_INDEX]),
                new Field(FEATURE_DATA_NAME[LOG_INTERVAL_INDEX],
                        FEATURE_UNIT[LOG_INTERVAL_INDEX], Field.Type.UInt32,
                        DATA_MAX[LOG_INTERVAL_INDEX],
                        DATA_MIN[LOG_INTERVAL_INDEX])
        });
    }

    private long getFeatureMaskForClass(Class<? extends Feature> type,SparseArray<Class<? extends Feature>> featureMap){
        long mask = 0;
        for(int i = 0 ; i<featureMap.size();i++){
            if(type == featureMap.valueAt(i)){
                mask = Math.max(mask,featureMap.keyAt(i));
            }
        }
        return mask;
    }

    private long buildFeatureMask(Set<Feature> featureToLog){
        SparseArray<Class<? extends Feature>> featureMap =
                Manager.getNodeFeatures(getParentNode().getTypeId());
        long logMask =0;
        for (Feature feature : featureToLog){
            logMask |= getFeatureMaskForClass(feature.getClass(),featureMap);
        }
        return logMask;
    }

    private static Set<Feature> buildFeatureSet(Node node, long featureMask){
        Set<Feature> outList = new HashSet<>(32);
        SparseArray<Class<? extends Feature>> featureMap =
                Manager.getNodeFeatures(node.getTypeId());
        long mask= 1L<<31; //1<<31
        //we test all the 32bit of the feature mask
        for(int i=0; i<32; i++ ) {
            if ((featureMask & mask) != 0) { //if the bit is up
                Class<? extends Feature> featureClass = featureMap.get((int)mask);
                if(featureClass!=null) {
                    Feature f = node.getFeature(featureClass);
                    if(f!=null)
                        outList.add(f);
                }//if featureClass
            }//if mask
            mask = mask>>1;
        }//for
        return outList;
    }

    public void startLogging(Set<Feature> featureToLog, long interval){
        long logMask = buildFeatureMask(featureToLog);
        byte message[] = new byte[9];
        byte temp[];
        message[0]=LOGGING_STARTED;
        temp =NumberConversion.LittleEndian.uint32ToBytes(logMask);
        System.arraycopy(temp,0,message,1,temp.length);
        temp =NumberConversion.LittleEndian.uint32ToBytes(interval);
        System.arraycopy(temp,0,message,5,temp.length);
        writeData(message);
    }

    public void stopLogging(){
        byte message[] = {LOGGING_STOPPED,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        writeData(message);
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 9)
            throw new IllegalArgumentException("There are no 9 bytes available to read");


        int isEnabled = data[dataOffset];
        long featureMask = NumberConversion.LittleEndian.bytesToUInt32(data,dataOffset+1);
        long logInterval = NumberConversion.LittleEndian.bytesToUInt32(data,dataOffset+5);

        return new ExtractResult(new Sample(timestamp,new Number[]{isEnabled,featureMask,logInterval},getFieldsDesc()),9);
    }
}
