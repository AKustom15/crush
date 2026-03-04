package com.akustom15.crushtemplate

import com.akustom15.crush.ui.splash.CrushSplashActivity

class SplashActivity : CrushSplashActivity() {

    override fun getSplashTextParts(): List<String> {
        return listOf("Crush", "Icon", "Pack")
    }

    override fun getSplashDuration(): Long {
        return 3000L
    }

    override fun getMainActivityClass(): Class<*> {
        return MainActivity::class.java
    }
}
