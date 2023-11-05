package com.ldhdev.mafia42passcounter

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.ldhdev.mafia42passcounter.ui.theme.Mafia42PassCounterTheme

class CounterService : LifecycleService(), SavedStateRegistryOwner {

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private var view: View? = null

    private var offsetX = 0
    private var offsetY = 0

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        val view = ComposeView(this).apply {
            setViewTreeSavedStateRegistryOwner(this@CounterService)
            setViewTreeLifecycleOwner(this@CounterService)

            setContent {
                Mafia42PassCounterTheme {
                    CounterView()
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    offsetX = (event.rawX - initialTouchX).toInt()
                    offsetY = (event.rawY - initialTouchY).toInt()

                    params.x = initialX + offsetX
                    params.y = initialY + offsetY

                    windowManager.updateViewLayout(view, params)
                    true
                }

                else -> false
            }
        }


        windowManager.addView(view, params)

        this.view = view

        return super.onStartCommand(intent, flags, startId)
    }

    @Composable
    fun CounterView() {

        var count by remember { mutableStateOf(0) }

        val color = if (count >= 31) Color.Green else MaterialTheme.colorScheme.primary

        Row(
            modifier = Modifier
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$count",
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { count++ },
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    Text(text = "추가")
                }
            }
        }
    }


    override fun onDestroy() {

        stopForeground(STOP_FOREGROUND_REMOVE)

        view?.let(windowManager::removeView)

        super.onDestroy()
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}