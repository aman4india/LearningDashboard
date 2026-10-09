package com.aman4india.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aman4india.learningdashboard.ui.navigation.AppNavHost
import com.aman4india.learningdashboard.ui.theme.LearningDashboardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearningDashboardTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                Surface(Modifier.fillMaxSize()) {
                    when (val loggedIn = isLoggedIn) {
                        null -> Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                        else -> AppNavHost(isLoggedIn = loggedIn)
                    }
                }
            }
        }
    }
}
