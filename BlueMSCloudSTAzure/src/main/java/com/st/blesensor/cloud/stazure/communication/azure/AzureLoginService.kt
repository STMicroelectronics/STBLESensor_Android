package com.st.blesensor.cloud.stazure.communication.azure

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.st.blesensor.cloud.stazure.communication.UserManager
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal data class LoginParameters(
        @Expose(serialize = false, deserialize = false)
        val name: String,
        @Expose
        @SerializedName("password")
        val password:String){

    @Expose
    @SerializedName("email")
    val email:String = "${name}@dummy.com"

    @Expose
    @SerializedName("rememberMe")
    val rememberMe:Boolean = true
}

internal data class LoginSuccessResponse(
        @Expose
        @SerializedName("token")
        val token:String,

        @Expose
        @SerializedName("expiringInterval")
        val expiringInterval:Long
): UserManager.AuthData

internal data class FieldErrors(
        @Expose
        @SerializedName("field")
        val filed:String,

        @Expose
        @SerializedName("message")
        val message:String
)

internal data class LoginErrorResponse(
        @Expose
        @SerializedName("message")
        val message:String,

        @Expose
        @SerializedName("errors")
        val errors:Array<FieldErrors>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginErrorResponse

        if (message != other.message) return false
        if (!errors.contentEquals(other.errors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + errors.contentHashCode()
        return result
    }
}

internal data class RegisterParameters(
        @Expose(serialize = false, deserialize = false)
        val name:String,
        @Expose
        @SerializedName("password")
        val password:String) {

    @Expose
    @SerializedName("email")
    val email:String = "${name}@dummy.com"

    @Expose
    @SerializedName("confirmPassword")
    val confirmPassword:String = password


}

internal interface AzureLoginService {

    @POST("user/register")
    suspend fun register(@Body param:RegisterParameters) : Response<Void>


    @POST("user/login")
    suspend fun login(@Body param:LoginParameters) : Response<LoginSuccessResponse>


}
