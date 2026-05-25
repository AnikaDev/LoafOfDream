package com.example.loafofdream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.data.local.PreferencesManager
import com.example.loafofdream.data.local.SearchHistoryManager
import com.example.loafofdream.presentation.navigation.AppNavHost
import com.example.loafofdream.presentation.theme.LoafOfDreamTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var searchHistoryManager: SearchHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = PreferencesManager(applicationContext)
        searchHistoryManager = SearchHistoryManager(applicationContext)
        prefs.token?.let { RetrofitClient.setToken(it) }

        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }
            val navController = rememberNavController()

            LoafOfDreamTheme(darkTheme = isDarkTheme) {
                AppNavHost(
                    navController = navController,
                    prefs = prefs,
                    searchHistoryManager = searchHistoryManager,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        prefs.isDarkTheme = isDarkTheme
                    }
                )
            }
        }
    }
}
