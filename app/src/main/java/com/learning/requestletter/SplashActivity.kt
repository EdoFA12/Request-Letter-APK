package com.learning.requestletter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.learning.requestletter.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.splashRoot) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        startAnimations()
    }

    private fun startAnimations() {
        // 1. Icon box — scale + fade in dari kecil
        binding.llCenter.apply {
            scaleX = 0.4f
            scaleY = 0.4f
            alpha  = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }

        // 2. Title — slide up + fade in (delay 400ms)
        binding.tvSplashTitle.apply {
            translationY = 40f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(400)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // 3. Tagline — slide up + fade in (delay 600ms)
        binding.tvSplashTagline.apply {
            translationY = 30f
            animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(600)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // 4. Lottie + versi — fade in (delay 900ms)
        binding.lottieLoading.apply {
            animate()
                .alpha(1f)
                .setStartDelay(900)
                .setDuration(400)
                .start()
        }
        binding.tvSplashVersion.apply {
            animate()
                .alpha(1f)
                .setStartDelay(1000)
                .setDuration(400)
                .start()
        }

        // 5. Pindah ke Dashboard setelah 3 detik
        binding.splashRoot.postDelayed({
            navigateToDashboard()
        }, 3000)
    }

    private fun navigateToDashboard() {
        binding.splashRoot.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                startActivity(Intent(this, MainActivityDashBoard::class.java))
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            .start()
    }
}

