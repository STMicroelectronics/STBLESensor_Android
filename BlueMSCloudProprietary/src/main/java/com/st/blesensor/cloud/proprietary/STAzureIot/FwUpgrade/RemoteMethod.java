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

package com.st.blesensor.cloud.proprietary.STAzureIot.FwUpgrade;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

/**
 * class describing a remote method exported by the device
 */
public class RemoteMethod {

    /**
     * method parameters type
     */
    public enum Type{
        String;


        @Override
        public String toString() {
            switch (this){
                case String:
                    return "string";
            }
            return null;
        }
    }

    /**
     * method parameter
     */
    public static final class Param{
        public final @NonNull Type type;
        public final @NonNull String name;

        public Param(@NonNull Type type,@NonNull String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Param param = (Param) o;

            if (type != param.type) return false;
            return name.equals(param.name);

        }
    }

    private @NonNull final String name;
    private @NonNull final String description;
    private @NonNull final List<Param> params;

    /**
     * build a remote method
     * @param description method description
     * @param name method name
     * @param params method parameters
     */
    public RemoteMethod(@NonNull String description,@NonNull String name,@NonNull Param... params){
        this.name = name;
        this.params = Arrays.asList(params);

        this.description = description;
    }
    public @NonNull String getName() {
        return name;
    }

    public @NonNull List<Param> getParams() {
        return params;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteMethod method = (RemoteMethod) o;

        if (!name.equals(method.name)) return false;
        if (!description.equals(method.description)) return false;
        return params.equals(method.params);

    }

}
