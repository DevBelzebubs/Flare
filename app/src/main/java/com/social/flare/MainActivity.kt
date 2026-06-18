package com.social.flare

import android.content.Context
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.social.flare.core.data.SettingsManager
import com.social.flare.core.ui.theme.FlareDarkBackground
import com.social.flare.core.ui.theme.FlareDarkSurface
import com.social.flare.core.ui.theme.FlareLightBackground
import com.social.flare.core.ui.theme.FlareLightSurface
import com.social.flare.core.ui.theme.FlareTheme
import com.social.flare.core.ui.theme.textSizeScaleToFontScale
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
            val settingsManager = remember { SettingsManager(applicationContext) }
            val darkModeEnabled by settingsManager.darkModeEnabledFlow.collectAsState(initial = true)
            val textSizeScale by settingsManager.textSizeScaleFlow.collectAsState(initial = 0.5f)
            val currentDensity = LocalDensity.current
            val appFontScale = textSizeScaleToFontScale(textSizeScale)
            var initialized by remember { mutableStateOf(app.isInitialized) }

            LaunchedEffect(darkModeEnabled) {
                configureSystemBars(darkModeEnabled)
            }

            LaunchedEffect(Unit) {
                app.awaitInitialization()
                initialized = true
            }

            if (!initialized) {
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = currentDensity.density,
                        fontScale = currentDensity.fontScale * appFontScale
                    )
                ) {
                    FlareTheme(darkTheme = darkModeEnabled) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                }
                return@setContent
            }

            LaunchedEffect(Unit) {
                scheduleAiBots(applicationContext)
                //forceAiTest(applicationContext)
            }

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * appFontScale
                )
            ) {
                FlareTheme(darkTheme = darkModeEnabled) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        MainScreen()
                    }
                }
            }
        }
    }
}

private fun MainActivity.configureSystemBars(darkModeEnabled: Boolean) {
    if (darkModeEnabled) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(FlareDarkBackground.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(FlareDarkSurface.toArgb())
        )
    } else {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                FlareLightBackground.toArgb(),
                FlareDarkBackground.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                FlareLightSurface.toArgb(),
                FlareDarkSurface.toArgb()
            )
        )
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
