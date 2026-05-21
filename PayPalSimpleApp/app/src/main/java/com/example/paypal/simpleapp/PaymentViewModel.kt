package com.example.paypal.simpleapp

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paypal.simpleapp.api.SampleServerApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.fraudprotection.PayPalDataCollector
import com.paypal.android.fraudprotection.PayPalDataCollectorRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class OrderApproved(
        val orderId: String,
        val payerId: String?
    ) : PaymentState()
    data class OrderCaptured(
        val orderId: String,
        val status: String
    ) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class PaymentViewModel : ViewModel(), PayPalWebCheckoutListener {

    companion object {
        private const val TAG = "PaymentViewModel"
        private const val URL_SCHEME = "com.example.paypal.simpleapp"
    }

    private val api = SampleServerApi.create()
    private var paypalClient: PayPalWebCheckoutClient? = null
    private lateinit var payPalDataCollector: PayPalDataCollector
    private var currentOrderId: String? = null

    var state by mutableStateOf<PaymentState>(PaymentState.Idle)
        private set

    fun startCheckout(activity: AppCompatActivity) {
        viewModelScope.launch {
            state = PaymentState.Loading
            try {
                // 1. Fetch Client ID
                val clientId = withContext(Dispatchers.IO) {
                    api.fetchClientId().value
                }
                Log.d(TAG, "Client ID fetched")

                // 2. Create an order
                val orderRequest = JsonObject().apply {
                    addProperty("intent", "CAPTURE")
                    add("purchase_units", JsonArray().apply {
                        add(JsonObject().apply {
                            add("amount", JsonObject().apply {
                                addProperty("currency_code", "USD")
                                addProperty("value", "10.99")
                            })
                        })
                    })
                }

                val order = withContext(Dispatchers.IO) {
                    api.createOrder(orderRequest)
                }
                currentOrderId = order.id
                Log.d(TAG, "Order created: ${order.id}")

                // 3. Configure SDK and launch PayPal Web Checkout
                val coreConfig = CoreConfig(clientId)
                payPalDataCollector = PayPalDataCollector(coreConfig)

                paypalClient = PayPalWebCheckoutClient(activity, coreConfig, URL_SCHEME)
                paypalClient?.listener = this@PaymentViewModel

                paypalClient?.start(PayPalWebCheckoutRequest(order.id))
            } catch (e: Exception) {
                Log.e(TAG, "Checkout error", e)
                state = PaymentState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun captureOrder(context: Context) {
        val orderId = currentOrderId
        if (orderId == null) {
            state = PaymentState.Error("No order to capture")
            return
        }

        viewModelScope.launch {
            state = PaymentState.Loading
            try {
                // Collect device data for fraud protection
                val dataCollectorRequest = PayPalDataCollectorRequest(hasUserLocationConsent = false)
                val cmid = payPalDataCollector.collectDeviceData(context, dataCollectorRequest)

                // Capture the order
                val response = withContext(Dispatchers.IO) {
                    api.captureOrder(orderId, cmid)
                }

                val responseJson = JSONObject(response.string())
                val status = responseJson.optString("status", "UNKNOWN")
                Log.d(TAG, "Order captured: $orderId, status: $status")
                state = PaymentState.OrderCaptured(orderId, status)
            } catch (e: Exception) {
                Log.e(TAG, "Capture error", e)
                state = PaymentState.Error(e.message ?: "Capture failed")
            }
        }
    }

    fun resetState() {
        state = PaymentState.Idle
        currentOrderId = null
    }

    // PayPalWebCheckoutListener callbacks

    override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
        Log.d(TAG, "PayPal approved: orderId=${result.orderId}, payerId=${result.payerId}")
        state = PaymentState.OrderApproved(
            orderId = result.orderId ?: currentOrderId ?: "",
            payerId = result.payerId
        )
    }

    override fun onPayPalWebFailure(error: PayPalSDKError) {
        Log.e(TAG, "PayPal error: ${error.errorDescription}")
        state = PaymentState.Error(error.errorDescription)
    }

    override fun onPayPalWebCanceled() {
        Log.d(TAG, "PayPal checkout canceled by user")
        state = PaymentState.Error("Checkout canceled by user")
    }

    override fun onCleared() {
        super.onCleared()
        paypalClient?.removeObservers()
    }
}
