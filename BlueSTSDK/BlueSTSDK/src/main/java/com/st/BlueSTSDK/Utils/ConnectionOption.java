/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentatio
 *      n
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
package com.st.BlueSTSDK.Utils;


import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.ExportedFeature;
import com.st.BlueSTSDK.Feature;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionOption  implements Parcelable{

    private final boolean resetCache;
    private final boolean enableAutoConnect;
    @Nullable
    private final
    UUIDToFeatureMap userDefineFeature;

    private ConnectionOption(Parcel in) {
        resetCache = in.readByte() != 0;
        enableAutoConnect = in.readByte() != 0;
        if(in.readByte()!=0){
            userDefineFeature = (UUIDToFeatureMap) in.readSerializable();
        }else{
            userDefineFeature = null;
        }
    }

    public static final Creator<ConnectionOption> CREATOR = new Creator<ConnectionOption>() {
        @Override
        public ConnectionOption createFromParcel(Parcel in) {
            return new ConnectionOption(in);
        }

        @Override
        public ConnectionOption[] newArray(int size) {
            return new ConnectionOption[size];
        }
    };

    public static ConnectionOptionBuilder builder(){
        return new ConnectionOptionBuilder();
    }

    public static ConnectionOption buildDefault(){
        return ConnectionOption.builder().build();
    }

    private ConnectionOption(boolean resetCache, boolean enableAutoConnect,
                             @Nullable Map<UUID, List<Class<? extends Feature>>> userDefineFeature) {
        this.resetCache = resetCache;
        this.enableAutoConnect = enableAutoConnect;

        if(userDefineFeature!=null) {
            this.userDefineFeature = new UUIDToFeatureMap();
            this.userDefineFeature.putAll(userDefineFeature);
        }else{
            this.userDefineFeature = null;
        }
    }

    public boolean enableAutoConnect() {
        return enableAutoConnect;
    }

    public boolean resetCache() {
        return resetCache;
    }

    @Nullable
    public Map<UUID, List<Class<? extends Feature>>> getUserDefineFeature() {
        return userDefineFeature;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (resetCache ? 1 : 0));
        dest.writeByte((byte) (enableAutoConnect ? 1 : 0));
        if(userDefineFeature!=null) {
            dest.writeByte((byte) 1);
            dest.writeSerializable(userDefineFeature);
        }else{
            dest.writeByte((byte) 0);
        }

    }

    public static class ConnectionOptionBuilder {

        private boolean resetCache = false;
        private boolean enableAutoConnect = false;
        private
        Map<UUID, List<Class<? extends Feature>>> userDefineFeature = null;

        public ConnectionOption build() {
            return new ConnectionOption(resetCache, enableAutoConnect,
                    userDefineFeature);
        }

        public ConnectionOptionBuilder resetCache(boolean resetCache) {
            this.resetCache = resetCache;
            return this;
        }

        public ConnectionOptionBuilder enableAutoConnect(boolean autoConnect) {
            this.enableAutoConnect = autoConnect;
            return this;
        }

        public ConnectionOptionBuilder addFeature(UUID uuid, List<Class<? extends Feature>> features) {
            if (userDefineFeature == null)
                userDefineFeature = new UUIDToClassListMap<>();
            if (userDefineFeature.containsKey(uuid)) {
                throw new IllegalArgumentException("UUID ("+uuid+") already present");
            } else {
                userDefineFeature.put(uuid, features);
            }
            return this;
        }

        public ConnectionOptionBuilder addFeature(UUID uuid, Class<? extends Feature> features) {
            return addFeature(uuid, Collections.<Class<? extends Feature>>singletonList(features));
        }

        public ConnectionOptionBuilder setFeatureMap(UUIDToClassListMap<Feature> featureMap) {
            if (userDefineFeature == null)
                userDefineFeature = new UUIDToClassListMap<>();
            userDefineFeature.putAll(featureMap);
            return this;
        }

    }
}
