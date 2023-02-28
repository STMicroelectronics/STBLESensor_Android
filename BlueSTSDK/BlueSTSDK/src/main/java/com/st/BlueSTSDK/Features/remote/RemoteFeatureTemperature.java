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

import android.util.SparseArray;

import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.Utils.UnwrapTimestamp;

/**
 * Class that manage an temperature that is acquired by a node and send by another node
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class RemoteFeatureTemperature  extends FeatureTemperature
        implements RemoteFeatureUtil.RemoteFeature {

    public static final String FEATURE_NAME = "Remote Temperature";

    private SparseArray<UnwrapTimestamp> mNodeUnwrapper = new SparseArray<>();

    /**
     * build a temperature feature
     *
     * @param n node that will send data to this feature
     */
    public RemoteFeatureTemperature(Node n) {
        super(FEATURE_NAME,n,new Field[]{
                TEMPERATURE_FILED,
                RemoteFeatureUtil.REMOTE_DEVICE_ID});
    }

    /**
     * Return id of the node that acquire the temperature
     * @param s sample with the data
     * @return the remote node id or &lt;0 if the sample is not valid
     */
    public static int getNodeId(Sample s){
        return RemoteFeatureUtil.getNodeId(s,1);
    }//getNodeId

    /**
     * Extract the remote id and temperature from an array of bytes
     * @param timestamp for the remote feature is not the timestamp but the node id
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return remote Temperature sample and bytes read
     */
    @Override
    protected ExtractResult extractData(long timestamp,byte[] data, int dataOffset) {
        if (data.length - dataOffset < 3) //2 for the ts + 1 for some data
            throw new IllegalArgumentException("There are enough bytes available to read");

        //remove multiple of 2^16 since the node can unwrap the timestamp
        int remoteId = (int)timestamp %(1<<16);
        //if it is the fist time that we see the node
        if(mNodeUnwrapper.get(remoteId)==null){
            mNodeUnwrapper.append(remoteId,new UnwrapTimestamp());
        }

        int ts = NumberConversion.BigEndian.bytesToUInt16(data, dataOffset);
        timestamp = mNodeUnwrapper.get(remoteId).unwrap(ts);

        ExtractResult tempData = super.extractData(timestamp, data, dataOffset +2);

        return new ExtractResult(RemoteFeatureUtil.appendRemoteId(this,tempData.getNewSample(),remoteId),
                2+tempData.getReadBytes());
    }//update
}
