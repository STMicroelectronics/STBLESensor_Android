package com.st.BlueSTSDK.Features.PnPL

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LazilyParsedNumber
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

abstract class PnPLCmd

abstract class PnPLGetStatusCmd(
        @SerializedName("get_status") var get_status: String
):PnPLCmd()

class PnPLGetDeviceStatusCmd : PnPLGetStatusCmd(get_status = "all")

class PnPLGetComponentStatusCmd(comp_name: String) : PnPLGetStatusCmd(get_status = comp_name)

class PnPLSendCommandField(val field_name:String, val field_value:Any)

@JsonAdapter(PnPLSendCommandCmdAdapter::class)
class PnPLSendCommand(val comp_name: String,
                      val comm_name: String,
                      val req_name: String? = null,
                      val field_list: List<PnPLSendCommandField>? = null):PnPLCmd()

class PnPLSendCommandCmdAdapter: TypeAdapter<PnPLSendCommand>() {
    override fun write(out: JsonWriter?, value: PnPLSendCommand?) {
        if (out != null) {
            out.beginObject()
            if (value != null) {
                out.name(value.comp_name +'*'+ value.comm_name)
                if (value.req_name != null && value.field_list != null) {
                    out.beginObject()
                    out.name(value.req_name)
                    out.beginObject()
                    for (f in value.field_list) {
                        out.name(f.field_name)
                        when (f.field_value) {
                            is Boolean -> out.value(f.field_value.toString().toBoolean())
                            is String -> out.value(f.field_value.toString())
                            is Int -> out.value(f.field_value.toString().toInt())
                            is Float -> out.value(f.field_value.toString().toFloat())
                        }
                    }
                    out.endObject()
                    out.endObject()
                }
                else{
                    if (value.field_list != null) {
                        out.beginObject()
                        // if value.field_list is empty --> empty out json object
                        for (f in value.field_list) {
                            out.name(f.field_name)
                            when (f.field_value) {
                                is Boolean -> out.value(f.field_value.toString().toBoolean())
                                is String -> out.value(f.field_value.toString())
                                is Int -> out.value(f.field_value.toString().toInt())
                                is Float -> out.value(f.field_value.toString().toFloat())
                            }
                        }
                        out.endObject()
                    }
                    else {
                        out.value("")
                    }
                }
                //out.endObject()
            }
            out.endObject()
        }
    }

    override fun read(`in`: JsonReader?): PnPLSendCommand {
        TODO("Not yet implemented")
    }
}

@JsonAdapter(PnPLSetCmdAdapter::class)
class PnPLSetPropertyCmd(
    val comp_name: String?,
    val properties: List<PnPLSetProperty>):PnPLCmd()

class PnPLSetCmdAdapter: TypeAdapter<PnPLSetPropertyCmd>() {
    override fun write(out: JsonWriter?, value: PnPLSetPropertyCmd?) {
        if (out != null) {
            out.beginObject()
            if (value != null) {
                out.name(value.comp_name)
                out.beginObject()
                for (p in value.properties){
                    out.name(p.prop_name)
                    val pValue = p.prop_value
                    if (pValue is PnPLSetProperty){
                        out.beginObject()
                        out.name(pValue.prop_name)
                        when (val subValue = pValue.prop_value) {
                            is String -> out.value(subValue.toString())
                            is Int -> out.value(subValue)
                            is Float -> out.value(subValue)
                            is Boolean -> out.value(subValue)
                            is LazilyParsedNumber -> out.value(if (subValue.toFloat() % 1 == 0f) subValue.toInt() else subValue.toFloat())
                        }
                        out.endObject()
                    }
                    else {
                        when (pValue) {
                            is String -> out.value(pValue.toString())
                            is Int -> out.value(pValue)
                            is Float -> out.value(pValue)
                            is Boolean -> out.value(pValue)
                            is LazilyParsedNumber -> out.value(if (pValue.toFloat() % 1 == 0f) pValue.toInt() else pValue.toFloat())
                        }
                    }
                }
                out.endObject()
                out.endObject()
            }
        }
    }

    override fun read(`in`: JsonReader?): PnPLSetPropertyCmd {
        TODO("Not yet implemented")
    }
}

class PnPLSetProperty(val prop_name:String, val prop_value:Any)