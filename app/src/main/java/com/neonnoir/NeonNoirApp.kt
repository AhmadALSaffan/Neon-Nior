package com.neonnoir

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Initializes Hilt's component hierarchy for the entire application
@HiltAndroidApp
class NeonNoirApp : Application()