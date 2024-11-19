package com.example.tsiisware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity  extends AppCompatActivity {
    private final int splashDuration = 3000; //3 seconds delay. The delay indicates the time the splashscreen is visible.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_view);
        get_permission();
        // Executes delayed code without affecting the content view
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Prepare Main Activity
                Intent userMainActivity = new Intent(SplashScreenActivity.this, UserMainActivity.class);

                //Activate Main Activity after the Splashscreen
                startActivity(userMainActivity);
                finish();
            }
        }, splashDuration);
    }

    private void get_permission() {
        // Check if the app has permission to use the camera
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            // Permission denied exit the app
            get_permission();
        }
    }
}
