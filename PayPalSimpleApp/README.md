# PayPal Simple Checkout - Android App

A minimal Android app demonstrating PayPal Web Checkout using the PayPal Android SDK v1.8.0.

## Prerequisites

- **Android Studio** (Hedgehog or newer recommended; Otter also works)
- **JDK 17** (bundled with Android Studio)
- **Android SDK** with API level 35 installed
- The PayPal SDK source at `../PPMobileSDKv1.8.0/paypal-android/` (relative to this project)

## Getting Started

### 1. Open the Project

1. Launch Android Studio
2. Click **File > Open** (or "Open" on the Welcome screen)
3. Navigate to this folder: `PayPalSimpleApp/`
4. Click **Open**

### 2. Sync Gradle

Android Studio should automatically prompt you to sync Gradle when the project opens.

- If prompted, click **Sync Now** in the notification bar at the top
- If not prompted, go to **File > Sync Project with Gradle Files**
- Wait for the sync to complete (check the status bar at the bottom)

If you see an error about "SDK location not found", go to **File > Project Structure > SDK Location** and make sure the Android SDK path is set correctly.

### 3. Set Up an Android Emulator

If you don't already have an emulator:

1. Go to **Tools > Device Manager** (or click the phone icon in the toolbar)
2. Click **Create Device**
3. Choose a phone like **Pixel 6** and click **Next**
4. Select a system image with **API 28 or higher** (API 34 recommended). Click **Download** if needed
5. Click **Next**, then **Finish**

### 4. Build and Run

1. In the toolbar, make sure the **app** module is selected in the run configuration dropdown
2. Select your emulator from the device dropdown
3. Click the green **Run** button (or press `Shift+F10`)
4. Wait for the build to complete and the app to launch

You can also build from the command line:

```bash
./gradlew :app:assembleDebug
```

## Using the App

The app demonstrates a complete PayPal checkout flow:

1. **Tap "Pay $10.99 with PayPal"** - This creates an order on the sample merchant server and opens the PayPal web checkout in a browser
2. **Log in to PayPal** - Use a PayPal Sandbox test account to approve the payment
3. **Return to the app** - After approval, you'll see the Order ID and Payer ID
4. **Tap "Complete Payment"** - This captures the payment and shows the final order status

### PayPal Sandbox Accounts

The app uses PayPal's sample merchant server which is configured for the Sandbox environment. You need a PayPal Sandbox buyer account to test. You can create one at [developer.paypal.com](https://developer.paypal.com/dashboard/accounts).

## Project Structure

```
PayPalSimpleApp/
├── settings.gradle          # Includes SDK modules via projectDir
├── build.gradle             # Root build config with SDK properties
├── gradle.properties        # Android/POM properties + Cardinal credentials
├── gradle/
│   └── gradle-publish.gradle  # No-op stub (required by SDK modules)
├── lint.xml                 # Lint config (required by CorePayments)
└── app/
    ├── build.gradle         # App module with Compose + SDK dependencies
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/
        │   ├── values/
        │   │   ├── strings.xml
        │   │   └── themes.xml
        │   └── xml/
        │       └── network_security_config.xml
        └── java/com/example/paypal/simpleapp/
            ├── MainActivity.kt        # Single activity, sets up Compose
            ├── PaymentViewModel.kt     # Checkout logic + PayPal listener
            ├── PaymentScreen.kt        # Compose UI
            └── api/
                └── SampleServerApi.kt  # Retrofit interface for merchant server
```

### Key Files

- **`PaymentViewModel.kt`** - Contains all the PayPal integration logic:
  - Fetches a Client ID from the sample merchant server
  - Creates an order ($10.99 USD, CAPTURE intent)
  - Launches PayPal Web Checkout via `PayPalWebCheckoutClient`
  - Implements `PayPalWebCheckoutListener` for callbacks
  - Captures the order with fraud protection data via `PayPalDataCollector`

- **`PaymentScreen.kt`** - A single Compose screen that shows the payment flow states (idle, loading, approved, captured, error)

- **`SampleServerApi.kt`** - Retrofit interface for the PayPal sample merchant server at `https://sdk-sample-merchant-server.herokuapp.com/`

## How It Works

This app includes three PayPal SDK modules directly from source:

| Module | Purpose |
|--------|---------|
| `CorePayments` | Core SDK configuration (`CoreConfig`) |
| `PayPalWebPayments` | Web checkout client and listener |
| `FraudProtection` | Device data collection for fraud prevention |

The modules are included via `settings.gradle` with `projectDir` pointing to the SDK source, so no Maven artifacts or AAR files are needed.

## Using Your Own PayPal Client ID

By default, this app fetches a Client ID from PayPal's sample merchant server. To use your own:

1. Create a PayPal app at [developer.paypal.com](https://developer.paypal.com/dashboard/applications)
2. In `PaymentViewModel.kt`, replace the `fetchClientId()` call with your Client ID:
   ```kotlin
   // Replace this:
   val clientId = withContext(Dispatchers.IO) {
       api.fetchClientId().value
   }
   // With this:
   val clientId = "YOUR_CLIENT_ID_HERE"
   ```
3. You'll also need your own server to create and capture orders (the sample server won't work with a different Client ID)

## Troubleshooting

- **Build fails with "SDK location not found"**: Go to File > Project Structure > SDK Location and set the Android SDK path
- **Gradle sync fails**: Make sure the `PPMobileSDKv1.8.0` folder is at the expected relative path (`../PPMobileSDKv1.8.0/paypal-android/`)
- **App crashes on launch**: Make sure you're running on API 21+ (Android 5.0 or higher)
- **PayPal checkout doesn't return to app**: Verify the deep link scheme `com.example.paypal.simpleapp` matches in both `AndroidManifest.xml` and `PaymentViewModel.kt`
