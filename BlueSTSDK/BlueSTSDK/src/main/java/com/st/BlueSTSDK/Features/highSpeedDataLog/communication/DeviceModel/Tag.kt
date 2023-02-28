package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class TagConfig(
    @SerializedName("maxTagsPerAcq") var maxTagsPerAcq : Int,
    @SerializedName("swTags")
        val softwareTags:List<Tag>,
    @SerializedName("hwTags")
        val hardwareTags:List<TagHW>
)

open class Tag(
        @SerializedName("id") val id: Int,
        @SerializedName("label") var label: String, //TODO: can be null?
        @SerializedName("enabled") var isEnabled:Boolean = false
):Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(label)
        parcel.writeByte(if (isEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}