package com.yearbook.retro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.yearbook.retro.ui.navigation.AppNavGraph
import com.yearbook.retro.ui.navigation.Screen
import com.yearbook.retro.ui.screens.offline.OfflineScreen
import com.yearbook.retro.ui.theme.RetroYearbookTheme
import com.yearbook.retro.worker.DailyReminderWorker

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val app = application as YearbookApp
        val container = app.container

        val targetYearbookId = intent.getStringExtra(DailyReminderWorker.EXTRA_YEARBOOK_ID)

        setContent {
            RetroYearbookTheme {
                val isOnline by container.networkObserver.isOnline.collectAsState()

                if (!isOnline) {
                    OfflineScreen(
                        onRetry = {
                            container.networkObserver.checkConnectivityNow()
                        }
                    )
                } else {
                    val navController = rememberNavController()

                    val startDestination = if (targetYearbookId != null) {
                        Screen.InsideYearbook.createRoute(targetYearbookId)
                    } else if (container.authRepository.getCurrentUser() != null) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    AppNavGraph(
                        navController = navController,
                        container = container,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
