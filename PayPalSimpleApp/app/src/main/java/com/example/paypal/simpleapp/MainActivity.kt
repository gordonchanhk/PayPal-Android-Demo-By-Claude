package com.example.paypal.simpleapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

enum class Screen {
    Start,
    CustomButton,
    OfficialButton
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("D", "MainActivity::oNCreate()")
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf(Screen.Start) }
                val viewModel: PaymentViewModel = viewModel()

                when (currentScreen) {
                    Screen.Start -> {
                        StartScreen(
                            onCustomButtonClick = { currentScreen = Screen.CustomButton },
                            onOfficialButtonClick = { currentScreen = Screen.OfficialButton }
                        )
                    }
                    Screen.CustomButton -> {
                        PaymentScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.resetState()
                                currentScreen = Screen.Start
                            }
                        )
                    }
                    Screen.OfficialButton -> {
                        PayPalButtonPaymentScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.resetState()
                                currentScreen = Screen.Start
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        Log.d("D", "MainActivity::onNewIntent()")
        intent = newIntent
    }
}
