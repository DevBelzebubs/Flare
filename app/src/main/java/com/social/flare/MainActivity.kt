package com.social.flare

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.social.flare.core.data.SettingsManager
import com.social.flare.core.ui.theme.FlareTheme
import com.social.flare.features.ai.framework.AiInteractionWorker
import com.social.flare.features.main.presentation.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = (LocalContext.current.applicationContext as FlareApp)
            var initialized by remember { mutableStateOf(app.isInitialized) }

            LaunchedEffect(Unit) {
                app.awaitInitialization()
                initialized = true
            }

            if (!initialized) {
                FlareTheme(darkTheme = true) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                    }
                }
                return@setContent
            }

            LaunchedEffect(Unit) {
                //Descomentar la linea de abajo para habilitar IA
                scheduleAiBots(applicationContext)
                //forceAiTest(applicationContext)
                //Comentar la linea de abajo para habilitar IA
                WorkManager.getInstance(applicationContext).cancelUniqueWork("AiBotInteractionWork")
            }

            val settingsManager = remember { SettingsManager(applicationContext) }
            val darkModeEnabled by settingsManager.darkModeEnabledFlow.collectAsState(initial = true)

            FlareTheme(darkTheme = darkModeEnabled) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

fun scheduleAiBots(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val aiWorkRequest = PeriodicWorkRequestBuilder<AiInteractionWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "AiBotInteractionWork",
        ExistingPeriodicWorkPolicy.KEEP,
        aiWorkRequest
    )
}

fun forceAiTest(context: Context) {
    val testRequest = OneTimeWorkRequestBuilder<AiInteractionWorker>().build()
    WorkManager.getInstance(context).enqueue(testRequest)
}