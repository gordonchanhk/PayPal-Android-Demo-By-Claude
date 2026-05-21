package com.example.paypal.simpleapp

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.android.paymentbuttons.PayPalButton
import com.paypal.android.paymentbuttons.PayPalButtonColor
import com.paypal.android.paymentbuttons.PayPalButtonLabel
import com.paypal.android.paymentbuttons.PaymentButtonShape

@Composable
fun PayPalButtonPaymentScreen(viewModel: PaymentViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    Box(modifier = Modifier.fillMaxSize()) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(text = "< Back")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Official Button Checkout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (val currentState = viewModel.state) {
            is PaymentState.Idle -> {
                AndroidView(
                    factory = { ctx ->
                        PayPalButton(ctx).apply {
                            color = PayPalButtonColor.GOLD
                            label = PayPalButtonLabel.CHECKOUT
                            shape = PaymentButtonShape.ROUNDED
                            setOnClickListener {
                                viewModel.startCheckout(activity)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is PaymentState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Processing...")
            }

            is PaymentState.OrderApproved -> {
                Text(
                    text = "Order Approved",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Order ID: ${currentState.orderId}")
                currentState.payerId?.let {
                    Text(text = "Payer ID: $it")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.captureOrder(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text(
                        text = "Complete Payment",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            is PaymentState.OrderCaptured -> {
                Text(
                    text = "Payment Complete!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Order ID: ${currentState.orderId}")
                Text(text = "Status: ${currentState.status}")
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Start New Payment")
                }
            }

            is PaymentState.Error -> {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentState.message,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Try Again")
                }
            }
        }
    }
}
