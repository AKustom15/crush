package com.akustom15.crush.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.akustom15.crush.R

@SuppressLint("CustomSplashScreen")
open class CrushSplashActivity : AppCompatActivity() {

    private var splashDurationMs: Long = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CrushSplash", "onCreate START")

        setContentView(R.layout.activity_crush_splash)

        val appNamePart1 = findViewById<TextView>(R.id.appNamePart1)
        val appNamePart2 = findViewById<TextView>(R.id.appNamePart2)
        val appNamePart3 = findViewById<TextView>(R.id.appNamePart3)

        // Configure text parts from intent extras or subclass
        val parts = getSplashTextParts()
        appNamePart1.text = parts.getOrElse(0) { "" }
        appNamePart2.text = parts.getOrElse(1) { "" }
        appNamePart3.text = parts.getOrElse(2) { "" }

        // Hide unused parts
        if (parts.size < 3) appNamePart3.visibility = android.view.View.GONE
        if (parts.size < 2) appNamePart2.visibility = android.view.View.GONE
        if (parts.isEmpty()) appNamePart1.visibility = android.view.View.GONE

        // Start animations
        val anim1 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in_1)
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in_2)
        val anim3 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in_3)

        if (parts.isNotEmpty()) appNamePart1.startAnimation(anim1)
        if (parts.size >= 2) appNamePart2.startAnimation(anim2)
        if (parts.size >= 3) appNamePart3.startAnimation(anim3)

        splashDurationMs = getSplashDuration()

        // Auto-navigate after animation completes — NO button needed
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val mainActivityIntent = Intent(this, getMainActivityClass())
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(mainActivityIntent)
                finish()
            } catch (e: Exception) {
                Log.e("CrushSplash", "Error navigating to MainActivity", e)
                finish()
            }
        }, splashDurationMs)

        Log.d("CrushSplash", "onCreate END - will auto-navigate in ${splashDurationMs}ms")
    }

    open fun getSplashTextParts(): List<String> {
        return listOf("Crush", "Icon", "Pack")
    }

    open fun getSplashDuration(): Long {
        return 3000L
    }

    open fun getMainActivityClass(): Class<*> {
        throw NotImplementedError("Subclass must override getMainActivityClass()")
    }
}
