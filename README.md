# Spectron Notify 1.1.0

A native Kotlin Android app for mirroring Android notification data to a Telegram bot and requesting the phone's current location through an authenticated Telegram chat.

Commands:
 /location
 /ring
 /status

Setup:
Open the project in Android Studio or use a cloud build environment with Android SDK and Gradle.
Install the APK on the device.
Enter the bot token and chat ID.
Enable Notification Access.
Enable Location permission.
Use Send Test.
Keep the app configured and the device connected to the internet.

The app only reads data exposed by Android's NotificationListenerService. It does not access private databases, passwords, OTP stores, recordings, or hidden app data.

Location is not continuously streamed. It is requested on demand through the configured Telegram chat.
