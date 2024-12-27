package com.achelm.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.achelm.musicplayer.R


class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_SPEED: Long = 2500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        window.navigationBarColor = ContextCompat.getColor(this , R.color.dark_primaryColor_ofApp)

        appOpenAds()

    }

    private fun appOpenAds() {
        Thread {
            Thread.sleep(SPLASH_SPEED)
            runOnUiThread {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}