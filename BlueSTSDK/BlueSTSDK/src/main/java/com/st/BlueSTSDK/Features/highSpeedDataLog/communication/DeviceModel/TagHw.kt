package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import android.os.Parcel
import com.google.gson.annotations.SerializedName

class TagHW : Tag {

    @SerializedName("pinDesc")
    var pinDesc:String = ""

    constructor(id: Int,
                pinDesc:String,
                label: String,
                isEnabled:Boolean):super(id,label,isEnabled){
        this.pinDesc = pinDesc
    }

    constructor(parcel: Parcel) : super(parcel) {
        pinDesc = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(pinDesc)
    }
}