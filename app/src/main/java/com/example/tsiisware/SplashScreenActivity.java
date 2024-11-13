package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.ContentView;
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.coroutines.Delay;

public class SplashScreenActivity  extends AppCompatActivity  {
    private final int splashDuration = 3000; //3 seconds delay. The delay indicates the time the splashscreen is visible.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_view);

        // Executes delayed code without affecting the content view
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                // Prepare Main Activity
                Intent userMainActivity = new Intent(SplashScreenActivity.this, UserMainActivity.class);

                //Activate Main Activity after the Splashscreen
                startActivity(userMainActivity);
                finish();
            }
        }, splashDuration);
    }
}
