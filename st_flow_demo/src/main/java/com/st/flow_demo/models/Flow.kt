package com.st.flow_demo.models

import androidx.documentfile.provider.DocumentFile
import com.st.blue_sdk.board_catalog.models.Sensor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Objects
import java.util.UUID

@Serializable
data class Flow(
    @SerialName(value = "version")
    val version: Int = FLOW_VERSION,
    @SerialName(value = "id")
    var id: String,
    @SerialName(value = "ex_app")
    var ex_app: Int = FLOW_CUSTOM,
    @SerialName(value = "category")
    var category: String? = null,
    @SerialName(value = "expression")
    var expression: Flow? = null,
    @SerialName(value = "statements")
    var statements: List<Flow> = ArrayList(),
    @SerialName(value = "board_compatibility")
    var board_compatibility: ArrayList<String> = ArrayList(),
    @SerialName(value = "description")
    var description: String,
    @SerialName(value = "notes")
    var notes: String? = null,
    @SerialName(value = "sensors")
    var sensors: List<Sensor> = ArrayList(),
    @SerialName(value = "functions")
    var functions: List<Function> = ArrayList(),
    @SerialName(value = "flows")
    var flows: List<Flow> = ArrayList(),
    @SerialName(value = "outputs")
    var outputs: List<Output> = ArrayList()

) {
    @kotlinx.serialization.Transient
    //var file: File? = null
    var file: DocumentFile? = null

    companion object {
        var FLOW_VERSION = 1
        var FLOW_CUSTOM  = 0
    }

    fun generateId() {
        id = UUID.randomUUID().toString()
    }

    fun setBoard_compatibility(board: String) {
        board_compatibility.add(board)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val flow: Flow = o as Flow
        return id == flow.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    fun canBeUploaded(): Boolean {
        if (outputs.isEmpty()) {
            return false
        }
        for (output in outputs) {
            if (output.canUpload) {
                return true
            }
        }
        return false
    }

    fun canBeUsedAsInput(): Boolean {
        if (outputs.isEmpty()) {
            return false
        }
        for (output in outputs) {
            if (output.id == Output.OUTPUT_AS_INPUT_ID) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "Flow{" +
                "version=" + version +
                ", file=" + file +
                ", id='" + id + '\'' +
                ", board_compatibility=" + board_compatibility +
                ", description='" + description + '\'' +
                ", notes='" + notes + '\'' +
                ", sensors=" + sensors +
                ", functions=" + functions +
                ", flows=" + flows +
                ", outputs=" + outputs +
                ", ex_app=" + ex_app +
                '}'
    }
}

fun createNewFlow(boardName: String): Flow {
    val flow = Flow(
        version = Flow.FLOW_VERSION,
        id = "",
        category = null,
        expression = null,
        statements = ArrayList(),
        board_compatibility = arrayListOf(boardName),
        description = "New Flow",
        notes = null,
        sensors = ArrayList(),
        functions = ArrayList(),
        flows = ArrayList(),
        outputs = ArrayList()
    )

    flow.generateId()
    return flow
}
