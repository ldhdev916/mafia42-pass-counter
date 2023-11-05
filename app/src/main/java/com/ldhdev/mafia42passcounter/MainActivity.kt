package com.ldhdev.mafia42passcounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ldhdev.mafia42passcounter.ui.theme.Mafia42PassCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

            startActivity(intent)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "카운터",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        setContent {
            Mafia42PassCounterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        ElevatedButton(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = {
                                startForegroundService(
                                    Intent(
                                        this@MainActivity,
                                        CounterService::class.java
                                    )
                                )
                            }
                        ) {
                            Text(text = "시작")
                        }
                    }
                }
            }
        }
    }
}