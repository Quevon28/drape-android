package com.drape.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.drape.app.core.designsystem.theme.DrapeTheme
import com.drape.app.feature.wardrobe.DrapeWardrobeViewModel
import com.drape.app.navigation.DrapeAppShell
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point for DRAPE.
 * Uses edge-to-edge rendering so Liquid Glass surfaces blend seamlessly behind system bars,
 * and injects `DrapeWardrobeViewModel` for local Room persistence.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val wardrobeViewModel: DrapeWardrobeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }

            DrapeTheme(darkTheme = isDarkTheme) {
                DrapeAppShell(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    wardrobeState = wardrobeViewModel.stateHolder
                )
            }
        }
    }
}
