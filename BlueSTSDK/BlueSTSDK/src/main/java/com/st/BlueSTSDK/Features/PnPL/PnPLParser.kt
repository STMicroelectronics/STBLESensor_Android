package com.st.BlueSTSDK.Features.PnPL

import android.util.Log
import com.google.gson.*
import org.json.JSONArray
import org.json.JSONObject

class PnPLParser {

    companion object {

        private val gsonEncDec = GsonBuilder().create()

        @JvmStatic
        fun getJsonObj(rawData: ByteArray, fromFile: Boolean = false): JsonObject? {
            val commandString = if (fromFile) {
                rawData.toString(Charsets.UTF_8)
            } else {
                rawData.toString(Charsets.UTF_8).dropLast(1)
            }
            return try {
                gsonEncDec.fromJson(commandString, JsonObject::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("PnPL Commands Parser", "error parsing the response: $e")
                Log.e("PnPL Commands Parser", commandString)
                null
            }
        }

        fun getJsonFromComponents(components: List<PnPLComponent>): String{
            val deviceObject = JSONObject()
            val compArray = JSONArray()
            for(c in components){
                val compObject = JSONObject()
                val contObject = JSONObject()
                for (cn in c.cont_list){
                    if (cn.cont_info != null){
                        when (cn.cont_info) {
                            is ArrayList<*> -> {
                                if ((cn.cont_info as ArrayList<*>)[0] is PnPLEnumValue){
                                    contObject.put(cn.cont_name, cn.cont_enum_pos)
                                }
                            }
                            else -> {
                                contObject.put(cn.cont_name, cn.cont_info)
                            }
                        }
                    } else if (cn.sub_cont_list != null){
                        for (scn in c.cont_list){
                            if (scn.cont_info != null) {
                                when (scn.cont_info) {
                                    is ArrayList<*> -> {
                                        if ((scn.cont_info as ArrayList<*>)[0] is PnPLEnumValue) {
                                            contObject.put(scn.cont_name, scn.cont_enum_pos)
                                        }
                                    }
                                    else -> {
                                        contObject.put(scn.cont_name, scn.cont_info)
                                    }
                                }
                            }
                        }
                    }
                }
                compArray.put(compObject.put(c.comp_name,contObject))
            }
            deviceObject.put("components",compArray)
            return deviceObject.toString(4)
        }

        //Create JSON String from PnPLCmd Command Classes
        @JvmStatic
        fun createPnPLCommandJSON(command: PnPLCmd): String {
            return gsonEncDec.toJson(command)
        }

        //Device Status ("Instance" of the Device Template) Parsing. (only single device is supported)
        @JvmStatic
        fun extractPnPLDeviceStatus(deviceJSON: JsonObject?): PnPLDeviceStatus? {
            deviceJSON ?: return null
            if (deviceJSON.has("devices")) {
                val devicesList = deviceJSON.getAsJsonArray("devices")
                val deviceStatusJSON = devicesList.get(0).asJsonObject
                val boardId = deviceStatusJSON.get("board_id").asInt
                val fwId = deviceStatusJSON.get("fw_id").asInt
                val sn = deviceStatusJSON.get("sn").asString
                if (deviceStatusJSON.has("components")) {
                    val compList = deviceStatusJSON.getAsJsonArray("components")
                    val compStatusList = mutableListOf<PnPLComponent>()
                    for (i in 0 until compList.size()) {
                        compStatusList.add(extractPnPLComponentStatus(compList.get(i) as JsonObject))
                    }
                    return PnPLDeviceStatus(boardId, fwId, sn, compStatusList)
                }
            }
            return null
        }

        @JvmStatic
        fun extractPnPLComponentStatus(componentJSON: JsonObject): PnPLComponent {
            val compID = componentJSON.keySet().first()
            val compInfo = componentJSON.get(compID)
            val contList = mutableListOf<PnPLContent>()
            (compInfo as JsonObject).keySet().forEach { contName ->
                val contInfo = compInfo.get(contName)
                var contStatus = PnPLContent(contName, "Property")
                when (contInfo) {
                    is JsonObject -> {
                        var subContList = mutableListOf<PnPLContent>()
                        contInfo.keySet().forEach { subContName ->
                            when (val subContInfo = contInfo.get(subContName)) {
                                is JsonArray -> {
                                    Log.e("TAG", "Array") //TODO test and complete this (e.g. tags --> is it possible to have another array in status from the Device Template?)
                                }
                                is JsonObject -> {
                                    Log.e("TAG", "Object") //TODO test this
                                    //subContList.add(PnPLContent(subContName, "Property", "", subContInfo))
                                }
                                is JsonPrimitive -> {
                                    when {
                                        subContInfo.isBoolean -> {
                                            subContList.add(PnPLContent(subContName, "Property", subContInfo.asBoolean))
                                        }
                                        subContInfo.isString -> {
                                            subContList.add(PnPLContent(subContName, "Property", subContInfo.asString))
                                        }
                                        subContInfo.isNumber -> {
                                            subContList.add(PnPLContent(subContName, "Property", subContInfo.asNumber))
                                        }
                                        else -> {
                                            subContList.add(PnPLContent(subContName, "Property", subContInfo.asString))
                                        }
                                    }
                                }
                            }
                        }
                        contStatus = PnPLContent(contName, "Property", subContList)
                    }
                    is JsonPrimitive -> {
                        contStatus = when {
                            contInfo.isBoolean -> {
                                PnPLContent(contName, "Property", contInfo.asBoolean)
                            }
                            contInfo.isString -> {
                                PnPLContent(contName, "Property", contInfo.asString)
                            }
                            contInfo.isNumber -> {
                                PnPLContent(contName, "Property", contInfo.asNumber)
                            }
                            else -> {
                                PnPLContent(contName, "Property", contInfo.asString)
                            }
                        }
                    }
                }
                contList.add(contStatus)
            }
            return PnPLComponent(compID, "", contList)
        }

        //Device Template Parsing
        fun getPnPLComponentsMap(dev_template_json: String): MutableMap<String, PnPLComponent>? {
            val jsonArray = JSONArray(dev_template_json)
            if((jsonArray[0] as JSONObject).has("contents")){
                val componentList = (jsonArray[0] as JSONObject).getJSONArray("contents")
                val compMap = mutableMapOf<String, PnPLComponent>()
                (0 until componentList.length()).forEach { //DTDL Root Component
                    val component = componentList.getJSONObject(it)
                    val dName = getDisplayName(component)
                    val compSchema = component.getString("schema")
                    val compTypesSplit = compSchema.split(":")
                    when (compTypesSplit[compTypesSplit.lastIndex-1]) {
                        "sensors" -> { compMap[compSchema] = PnPLComponent(component.getString("name"), dName, comp_type = ComponentType.SENSOR) }
                        "algorithms" -> { compMap[compSchema] = PnPLComponent(component.getString("name"), dName, comp_type = ComponentType.ALGORITHM) }
                        "other" -> { compMap[compSchema] = PnPLComponent(component.getString("name"), dName, comp_type = ComponentType.OTHER) }
                        else -> { compMap[compSchema] = PnPLComponent(component.getString("name"), dName, comp_type = ComponentType.NONE) }
                    }

                }

                (1 until jsonArray.length()).forEach { it ->
                    val component = jsonArray.getJSONObject(it)
                    val contents = component.getJSONArray("contents")
                    (0 until contents.length()).forEach { iit ->
                        val content = contents.getJSONObject(iit)
                        if (content.getString("@type") == "Property") { //DTDL Property
                            val propContent = createPnPLPropertyContentFromJSONObject(content)
                            if(propContent != null) {
                                compMap[component.getString("@id")]?.cont_list?.add(propContent)
                            }
                        }
                        else if (content.getString("@type") == "Command") { //DTDL Command
                            val commContent = createPnPLCommandFromJSONObject(content)
                            if(commContent != null){
                                compMap[component.getString("@id")]?.cont_list?.add(commContent)
                            }
                        }
                        else if (content.getString("@type") == "Telemetry") { //DTDL Telemetry
                            Log.e("ERROR", "Telemetry content, not supported")
                            //TODO Telemetries support
                        } else {

                            if(content.getJSONArray("@type").get(0).toString()=="Property") {
                                //this in order to handle
//                                {
//                                    "@id": "....",
//                                    "@type": [
//                                    "Property",
//                                    "NumberValue"
//                                    ],
//                                    "displayName": {
//                                    "en": "Value Name"
//                                },
//                                    "name": "value",
//                                    "schema": "integer",
//                                    "writable": true
//                                }
                                //where there could be "NumberValue","BooleanValue" and so on..
                                val propContent = createPnPLPropertyContentFromJSONObject(content)
                                if(propContent != null) {
                                    compMap[component.getString("@id")]?.cont_list?.add(propContent)
                                }
                            } else {
                                Log.e("ERROR", "Content type not supported")
                            }
                        }
                    }
                }
                return compMap
            }
            return null
        }

        fun getPnPLSensorsComponentsMap(dev_template_json: String): MutableMap<String, PnPLComponent>?{
            val cMap = getPnPLComponentsMap(dev_template_json)
            return cMap?.filter { it.value.comp_type == ComponentType.SENSOR }?.toMutableMap()
        }

        fun getPnPLAlgorithmComponentsMap(dev_template_json: String): MutableMap<String, PnPLComponent>?{
            val cMap = getPnPLComponentsMap(dev_template_json)
            return cMap?.filter { it.value.comp_type == ComponentType.ALGORITHM }?.toMutableMap()
        }

        fun getPnPLOtherComponentsMap(dev_template_json: String): MutableMap<String, PnPLComponent>?{
            val cMap = getPnPLComponentsMap(dev_template_json)
            return cMap?.filter { it.value.comp_type == ComponentType.OTHER }?.toMutableMap()
        }

        @JvmStatic
        fun getPnPLComponentList(dev_template_json: String) : List<PnPLComponent>? {
            return getPnPLComponentsMap(dev_template_json)?.values?.toList()
        }

        fun filterContent(cont_names: List<String>, comp_list: MutableList<PnPLComponent>){

            for (comp in comp_list){
                val contList = emptyList<PnPLContent>().toMutableList()
                for (cont in comp.cont_list){
                    if (cont.cont_name in cont_names){
                        contList.add(cont)
                    }
                }
                comp.cont_list = contList
            }
        }

        fun filterComponentByName(cont_names: List<String>, comp_list: MutableList<PnPLComponent>) : MutableList<PnPLComponent>{
           //comp_list.removeIf { comp -> (comp.comp_name !in cont_names) }

            val contList = emptyList<PnPLComponent>().toMutableList()
            for(comp in comp_list) {
                if(comp.comp_name in cont_names) {
                    contList.add(comp)
                }
            }
           return contList
        }

        private fun getDisplayName(jsonObj: JSONObject): String {
            var evDName = ""
            val aDName: Any = jsonObj.get("displayName")
            if (aDName is JSONObject) {
                //displayName field JSONObject type
                if (aDName.has("en")) {
                    evDName = aDName.getString("en")
                }
            } else {
                //displayName field primitive type
                evDName = jsonObj.getString("displayName")
            }
            return evDName
        }

        private fun createPnPLEnumContent(name:String, type:String, dName:String, writable:Boolean, enumValues: JSONArray):PnPLContent?{
            val enumValueList = mutableListOf<PnPLEnumValue>()
            (0 until enumValues.length()).forEach { eit ->
                val enumValueJSON = enumValues.getJSONObject(eit)
                val evDName = getDisplayName(enumValueJSON)
                val evValue: Any = enumValueJSON.get("enumValue")
                val evName = enumValueJSON.getString("name")
                enumValueList.add(PnPLEnumValue(evDName, evValue, evName))
            }
            return PnPLContent(name, type, dName, "enum_int", writable, enumValueList, 0)
        }

        private fun createPnPLObjectContent(name:String, type:String, dName:String, writable:Boolean, requestName: String?, objectFields: JSONArray, reqSchema: String?=null):PnPLContent?{
            val fieldList = mutableListOf<PnPLContent>()
            val enumList = mutableListOf<PnPLEnumValue>()
            (0 until objectFields.length()).forEach { fit ->
                val fieldJSON = objectFields.getJSONObject(fit)
                val fDName = getDisplayName(fieldJSON)
                val fName = fieldJSON.getString("name")
                var fSchema:Any
                if (type == "Property"){
                    fSchema = fieldJSON.get("schema")
                    if (fSchema is JSONObject){
                        val fSchemaType = fSchema.getString("@type").lowercase()
                        if (fSchemaType == "object") {
                            val fObjectFieldsJSONArray = fSchema.getJSONArray("fields")
                            val objectContent = createPnPLObjectContent(fName, "CommandField", fDName, writable, null, fObjectFieldsJSONArray)
                            if (objectContent != null) {
                                fieldList.add(objectContent)
                            }
                        }
                    }
                    else{
                        fSchema = fieldJSON.getString("schema")
                        var writeConstraint = writable
                        if (fName == "min" || fName == "max"){
                            writeConstraint = false
                        }
                        fieldList.add(PnPLContent(fName, "PropertyObjectField", fDName, fSchema, writeConstraint))
                    }
                } else {
                    if (fieldJSON.has("schema")) {
                        fSchema = fieldJSON.getString("schema")
                        fieldList.add(createPnPLPrimitivePropertyFromJSONObject(fName, "CommandField", fDName, true, fSchema.toString()))
                    } else if (fieldJSON.has("dtmi:dtdl:property:schema;2")) {
                        fSchema = fieldJSON.getJSONObject("dtmi:dtdl:property:schema;2")
                        val fSchemaType = fSchema.getString("@type").lowercase()
                        if (fSchemaType == "enum") {
                            val fEnumValuesJSONArray = fSchema.getJSONArray("enumValues")
                            val enumContent = createPnPLEnumContent(fName,"CommandField",fDName,true,fEnumValuesJSONArray)
                            if (enumContent != null) {
                                fieldList.add(enumContent)
                            }
                        } else if (fSchemaType == "object") {
                            val fObjectFieldsJSONArray = fSchema.getJSONArray("fields")
                            val objectContent = createPnPLObjectContent(fName, "CommandField", fDName, true, null, fObjectFieldsJSONArray)
                            if (objectContent != null) {
                                fieldList.add(objectContent)
                            }
                        }
                    }
                    else {
                        if (fieldJSON.has("enumValue")){
                            enumList.add(PnPLEnumValue(getDisplayName(fieldJSON),fieldJSON.getInt("enumValue"),fieldJSON.getString("name")))
                        }
                    }
                }
            }
            return if (fieldList.isEmpty()) { //enum command object field
                if (requestName != null){
                    if (enumList.isNotEmpty()){
                        //enum sub-command content
                        fieldList.add(PnPLContent(requestName, type, "", "enum_int", true, enumList, 0))
                    } else if (reqSchema != null){
                        //primitive sub-command content
                        fieldList.add(PnPLContent(requestName, type, "", reqSchema, true))
                    }
                }
                PnPLContent(name, type, dName, "object", writable, null, cont_list = fieldList)
            } else {
                PnPLContent(name, type, dName, "object", writable, requestName, cont_list = fieldList)
            }
        }

        private fun createPnPLPrimitivePropertyFromJSONObject(name:String, type:String, dName:String, writable:Boolean, schema:String):PnPLContent{
            return when (schema) {
                "boolean" -> PnPLContent(name, type, dName, schema, writable)
                "integer" -> PnPLContent(name, type, dName, schema, writable)
                "double" -> PnPLContent(name, type, dName, schema, writable)
                "float" -> PnPLContent(name, type, dName, schema, writable)
                "string" -> PnPLContent(name, type, dName, schema, writable)
                else -> PnPLContent(name, type, dName, schema, writable)
            }
        }

        private fun createPnPLPropertyContentFromJSONObject(json_content:JSONObject):PnPLContent?{
            //name field
            val contName = json_content.getString("name")
            //displayName field
            val contDName = getDisplayName(json_content)
            //writable field
            var contWritable = false
            if (json_content.has("writable")) {
                contWritable = json_content.getBoolean("writable")
            }
            //schema field
            val contSchema: Any = json_content.get("schema")

            //Non-primitive Property
            if (contSchema is JSONObject) {
                //Property schema field JSONObject type
                val contSchemaType = contSchema.getString("@type")
                //if type == Enum
                if (contSchemaType == "Enum") {
                    val enumValuesJSONArray = contSchema.getJSONArray("enumValues")
                    return createPnPLEnumContent(contName, "Property", contDName, contWritable, enumValuesJSONArray)
                }
                //if type == Object
                else if (contSchemaType == "Object") {
                    val objectFieldsJSONArray = contSchema.getJSONArray("fields")
                    return createPnPLObjectContent(contName, "Property", contDName, contWritable, null, objectFieldsJSONArray)
                }
                //TODO if Other (Vector,Map,Date...) // not managed @ the moment
                else {
                    Log.e("ERROR", "DTDL Type: $contSchemaType not supported")
                    return null
                }
            }
            //Primitive Property
            else {
                return createPnPLPrimitivePropertyFromJSONObject(contName, "Property", contDName, contWritable, contSchema.toString())
            }
        }

        private fun createPnPLCommandFromJSONObject(json_content:JSONObject):PnPLContent?{
            //name field
            val commName = json_content.getString("name")
            //displayName field
            val commDName = getDisplayName(json_content)
            //request field
            val request: JSONObject
            if (json_content.has("request")) {
                //request field
                request = json_content.getJSONObject("request")
                //request name field
                val reqName = request.getString("name")

                if (request.has("schema")){
                    val reqSchema: Any = request.get("schema")
                    //Non-primitive Property
                    if (reqSchema is JSONObject) {
                        //request schema type
                        val reqSchemaType = (request.getJSONObject("schema") as JSONObject).getString("@type")
                        //Enum command req
                        if (reqSchemaType == "Enum"){
                            val enumValuesJSONArray = request.getJSONObject("schema").getJSONArray("enumValues")
                            return createPnPLObjectContent(commName, "Command", commDName, true, reqName, enumValuesJSONArray)
                        }
                        //Object command req
                        else if (reqSchemaType == "Object"){
                            //request fields
                            val reqFieldsJSONArray = request.getJSONObject("schema").getJSONArray("fields")
                            return createPnPLObjectContent(commName, "Command", commDName, true, reqName, reqFieldsJSONArray)
                        }
                    }
                    //Primitive
                    else {
                        return createPnPLObjectContent(commName, "Command", commDName, true, reqName, JSONArray(), reqSchema.toString())
                    }
                } else {
                    return createPnPLObjectContent(commName, "Command", commDName, true, reqName, JSONArray())
                }
            }
            return createPnPLObjectContent(commName, "Command", commDName, true, null, JSONArray())
        }
    }
}