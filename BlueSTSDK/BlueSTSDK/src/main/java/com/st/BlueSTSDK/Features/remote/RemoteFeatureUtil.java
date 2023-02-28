/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
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
 *
 ******************************************************************************/

package com.st.BlueSTSDK.Features.remote;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.Utils.UnwrapTimestamp;

import java.util.Arrays;

/**
 * Utility/common function used to manage a remote feature, data exported by a node but that
 * came from another node
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class RemoteFeatureUtil {

    /**
     * Name of the filed that will contain the source node id
     */
    public static final String REMOTE_DEVICE_DATA_NAME = "Node Id";

    /**
     * Max number of remote nodes
     */
    public static final int DATA_MAX = (1<<16)-1;

    /**
     * Min number of remote nodes
     */
    public static final  int DATA_MIN = 0;

    /**
     * Filed tha describe a remote node id
     */
    public static final Field REMOTE_DEVICE_ID = new Field(REMOTE_DEVICE_DATA_NAME,"",
            Field.Type.UInt16, DATA_MAX,DATA_MIN);


    /**
     * Extract the node id from a sample
     * @param sample sample with the data
     * @param index index where the function will find the node id data
     * @return id of the remote node
     */
    public static int getNodeId(Feature.Sample sample,int index){
        if(sample!=null)
            if(sample.data.length>index)
                if (sample.data[index] != null)
                    return sample.data[index].intValue();
        //else
        return -1;
    }//getNodeId


    /**
     * Add the remote node id field to an already decoded data sample, the remote node id will be
     * added as last element
     * @param f feature that generate the data
     * @param remoteSample decoded data sample
     * @param remoteId node that acquired the data
     * @return new sample with the remoteSample data + the remote id
     */
    public static Feature.Sample appendRemoteId(Feature f, Feature.Sample remoteSample,
                                                int remoteId){
        Number remoteData[] = Arrays.copyOf(remoteSample.data, remoteSample.data.length + 1);
        remoteData[remoteData.length-1]=remoteId;
        return new Feature.Sample(remoteSample.timestamp,remoteData,f.getFieldsDesc());
    } //appendRemoteId

    /**
     * Dummy interface used for detect if a feature is also a remote feature
     */
    public interface RemoteFeature{

    }
}
