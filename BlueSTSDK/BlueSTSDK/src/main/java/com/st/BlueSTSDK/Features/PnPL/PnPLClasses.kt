package com.st.BlueSTSDK.Features.PnPL

//TODO Enum for Components Types (Sensor, Algorithm, Other)
enum class ComponentType {
    SENSOR, ALGORITHM, OTHER, NONE
}
//TODO Enum for Sensor Types (ACC, TEMP, ...)
enum class SensorTypes {
    ACC,
}
/*enum class Day(val printableName: String) {
    ACC("Accelerometer"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday")
}*/

//TODO Enum for Contents Types (Property, Command, ...)

data class PnPLDeviceStatus(
    val board_id: Int,
    val fw_id: Int,
    val sn: String,
    val comp_list: MutableList<PnPLComponent> = mutableListOf())

data class PnPLComponent(
    var comp_name:String,//DT: hts221_temp, ST: hts221_temp
    var comp_display_name:String,//DT: HTS221_TEMP, null
    var cont_list:MutableList<PnPLContent> = mutableListOf(), //DT:[], ST:[]
    var comp_type:ComponentType? = null) //enum: Sensor,Algorithm,Other,None //DT:Sensor, ST:0

data class PnPLEnumValue(
        var displayName: String,
        var enumValue: Any,
        var name: String)

data class PnPLContent(
    var cont_name: String,//name DT:odr, ST:odr
    var cont_type: String,//Property, Command, Telemetry DT:Property, ST: null
    var cont_display_name: String?,//displayName DT:ODR, ST: null
    var cont_schema: String?, //DT: float, ST: null
    var cont_writable: Boolean?, //DT: yes, ST: null
    var cont_info: Any?, //DT: null, ST: 7
    var cont_enum_pos: Int?, //DT: null, ST:null
    var request_name: String?, //DT: null, ST:null
    var sub_cont_list: MutableList<PnPLContent>?,//DT: null, ST:null
    var file_loaded_status: Boolean?=null)
    {

    //PnPLContent From a Device Status (current properties values)
    //Used for Property Content Status initialization: only cont_name is known (and the fact that this is a Property). Then cont_info will be filled
    constructor(cont_name: String, cont_type: String):this(cont_name, cont_type, null, null, null, null, null, null, null)
    //Used for Primitive Property (or Sub-Property since a Sub-Property is still a Property itself) Content from Status creation.
    constructor(cont_name: String, cont_type: String, cont_info: Any):this(cont_name, cont_type, null, null, null, cont_info, null, null, null)
    //Used for Object Property (which contains a Sub-Property list) Content from Status creation. Each Sub-Property will use the previous constructor to create itself
    constructor(cont_name: String, cont_type: String, cont_list: MutableList<PnPLContent> = mutableListOf()):this(cont_name, cont_type, null, null, null, null, null, null, cont_list)

    //PnPLContent From a Device Template (FW+Board capability, no values are known)
    //Used to create Primitive Contents from a Device Template
    constructor(cont_name: String, cont_type: String, cont_display_name: String, cont_schema: String, cont_writable: Boolean):this(cont_name, cont_type, cont_display_name, cont_schema, cont_writable, null, null, null, null)
    //Used to create Enum Contents from a Device Template
    constructor(cont_name: String, cont_type: String, cont_display_name: String, cont_schema: String, cont_writable: Boolean, cont_info: Any, cont_enum_pos: Int):this(cont_name, cont_type, cont_display_name, cont_schema, cont_writable, cont_info, cont_enum_pos,null, null)
    //Used to create Object Contents from a Device Template
    constructor(cont_name: String, cont_type: String, cont_display_name: String, cont_schema: String, cont_writable: Boolean, request_name: String?, cont_list: MutableList<PnPLContent> = mutableListOf()):this(cont_name, cont_type, cont_display_name, cont_schema, cont_writable, null, null, request_name, cont_list)
}