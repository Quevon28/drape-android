package com.drape.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Root Application entry point for DRAPE.
 * Initializes Hilt dependency graph and app-wide singleton configuration.
 */
@HiltAndroidApp
class DrapeApplication : Application()
