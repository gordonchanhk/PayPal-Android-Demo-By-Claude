package com.example.paypal.simpleapp.api

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class ClientIdResponse(
    @SerializedName("value") val value: String
)

data class OrderResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String? = null
)

interface SampleServerApi {

    @GET("/client_id")
    suspend fun fetchClientId(): ClientIdResponse

    @POST("/orders")
    suspend fun createOrder(@Body body: JsonObject): OrderResponse

    @POST("/orders/{orderId}/capture")
    suspend fun captureOrder(
        @Path("orderId") orderId: String,
        @Header("PayPal-Client-Metadata-Id") clientMetadataId: String?
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://sdk-sample-merchant-server.herokuapp.com/"

        fun create(): SampleServerApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SampleServerApi::class.java)
        }
    }
}
