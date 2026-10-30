package com.appzee.smartstorage.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.appzee.smartstorage.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as StorageApplication
        Handler(Looper.getMainLooper()).postDelayed({
            app.appOpenAdManager.showAdIfAvailable(this) {
                goToMain()
            }
        }, 8000)
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
