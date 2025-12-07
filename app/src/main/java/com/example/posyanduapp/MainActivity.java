package com.example.posyanduapp; // sesuaikan dengan package kamu

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingAnimation = findViewById(R.id.loadingAnimation);
        loadingAnimation.playAnimation();

        // Handler untuk delay splash screen sebelum pindah ke MainActivity
        new Handler().postDelayed(() -> {
            // Berhenti animasi (opsional)
            loadingAnimation.cancelAnimation();

            // Pindah ke MainActivity (ganti dengan activity utama kamu)
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // Tutup splash supaya tidak bisa back ke splash screen
        }, SPLASH_DURATION);
    }
}
