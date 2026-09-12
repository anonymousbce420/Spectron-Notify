package com.spectron.notify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

class MainActivity : ComponentActivity() {
    private var commandService: BotCommandService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Screen() }
    }

    override fun onResume() {
        super.onResume()
        commandService?.stop()
        if (Prefs.token(this).isNotBlank() && Prefs.chatId(this).isNotBlank()) {
            commandService = BotCommandService(applicationContext).also { it.start() }
        }
    }

    override fun onDestroy() {
        commandService?.stop()
        super.onDestroy()
    }

    private fun listenerEnabled() =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    @Composable
    private fun Screen() {
        var token by remember { mutableStateOf(Prefs.token(this)) }
        var chatId by remember { mutableStateOf(Prefs.chatId(this)) }
        var forwarding by remember { mutableStateOf(Prefs.enabled(this)) }
        var access by remember { mutableStateOf(listenerEnabled()) }
        var status by remember { mutableStateOf("") }

        val locationLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val ok = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            status = if (ok) "Location permission enabled" else "Location permission denied"
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Spectron Notify", style = MaterialTheme.typography.headlineMedium)
                    Text("Notification mirroring and remote device location.")

                    Text(if (access) "Notification access: Enabled" else "Notification access: Disabled")

                    Button(
                        onClick = {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            access = listenerEnabled()
                        },
                        Modifier.fillMaxWidth()
                    ) { Text("Notification Access") }

                    OutlinedTextField(
                        token, { token = it },
                        label = { Text("Telegram Bot Token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    OutlinedTextField(
                        chatId, { chatId = it },
                        label = { Text("Telegram Chat ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Forward notifications")
                        Switch(forwarding, {
                            forwarding = it
                            Prefs.setEnabled(this@MainActivity, it)
                        })
                    }

                    Button(
                        onClick = {
                            Prefs.save(this@MainActivity, token, chatId)
                            status = "Settings saved"
                            commandService?.stop()
                            commandService = BotCommandService(applicationContext).also { it.start() }
                        },
                        Modifier.fillMaxWidth()
                    ) { Text("Save Settings") }

                    OutlinedButton(
                        onClick = {
                            Prefs.save(this@MainActivity, token, chatId)
                            Telegram.send(
                                this@MainActivity,
                                "SPECTRON NOTIFY\n\nTest notification sent successfully."
                            ) { _, result -> runOnUiThread { status = result } }
                        },
                        Modifier.fillMaxWidth()
                    ) { Text("Send Test") }

                    OutlinedButton(
                        onClick = {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        Modifier.fillMaxWidth()
                    ) { Text("Enable Location") }

                    Text("Telegram commands: /location, /ring, /status")
                    if (status.isNotBlank()) Text(status)
                    Text(
                        "Only Android-exposed notification data is mirrored. " +
                            "Location is requested only through the Telegram command."
                    )
                }
            }
        }
    }
}
