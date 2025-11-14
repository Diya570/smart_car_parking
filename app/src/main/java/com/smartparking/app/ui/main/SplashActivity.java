package com.smartparking.app.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.smartparking.app.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // A delay to show the splash screen, then navigate.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // We always go to MainActivity. MainActivity itself will decide whether to show
            // the login screen or the main content based on the user's auth state.
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // Finish SplashActivity so user can't navigate back to it.
        }, 1500); // 1.5 second delay
    }
}