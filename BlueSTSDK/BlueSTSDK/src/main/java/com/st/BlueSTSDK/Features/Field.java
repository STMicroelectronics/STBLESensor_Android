/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
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
package com.st.BlueSTSDK.Features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class that describe a feature data field
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Field {

    /**
     * Type used for store this Field
     */
    public enum Type {
        Float,
        Int64,
        UInt32,
        Int32,
        UInt16,
        Int16,
        UInt8,
        Int8,
        ByteArray
    }

    /** field unit */
    private final String mUnit;
    /** field name */
    private final String mName;
    /** field type */
    private final Type mType;
    /** field max value */
    private final Number mMax;
    /** field min value */
    private final Number mMin;
    /** flag for plotting it*/
    private final boolean mPlotIt;

    /**
     * build a field, the field value will be set to null
     * @param name filed name
     * @param unit filed unit
     * @param type field type
     * @param max field max value
     * @param min field min value
     */
    public Field(@NonNull String name,@Nullable String unit,@NonNull Type type,@NonNull Number max,@NonNull Number min,@NonNull boolean plotIt){
        mName=name;
        mUnit=unit;
        mType=type;
        mMax=max;
        mMin=min;
        mPlotIt=plotIt;
    }

    /**
     * build a field, the field value will be set to null
     * @param name filed name
     * @param unit filed unit
     * @param type field type
     * @param max field max value
     * @param min field min value
     */
    public Field(@NonNull String name,@Nullable String unit,@NonNull Type type,@NonNull Number max,@NonNull Number min){
        this(name,unit,type,max,min,true);
    }

    /**
     * get filed unit
     * @return filed unit
     */
    public @Nullable String getUnit() {
        return mUnit;
    }

    /**
     * get filed name
     * @return filed name
     */
    public @NonNull String getName() {
        return mName;
    }

    /**
     * get field type
     * @return field type
     */
    public @NonNull Type getType() {
        return mType;
    }

    /**
     * get filed max value
     * @return field max value
     */
    public @NonNull Number getMax() {
        return mMax;
    }

    /**
     * get field min value
     * @return field min value
     */
    public @NonNull Number getMin() {
        return mMin;
    }

    /**
     * get field min value
     * @return flag for including in plot
     */
    public @NonNull boolean getPlotIt() {
        return mPlotIt;
    }

}//Field
