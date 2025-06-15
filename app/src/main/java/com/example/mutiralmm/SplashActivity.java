package com.example.mutiralmm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000; // durasi splash 3 detik (3000 ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // gunakan layout logo splash

        new Handler().postDelayed(() -> {
            // Setelah delay, pindah ke LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish(); // tutup splash agar tidak bisa kembali ke sini
        }, SPLASH_TIME);
    }
}
