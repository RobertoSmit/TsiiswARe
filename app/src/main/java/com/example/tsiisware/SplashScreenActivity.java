package com.example.tsiisware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_view);

        get_permission();
    }

    private void get_permission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            proceedToMainActivity();
        }
    }

    private void proceedToMainActivity() {
        int splashDuration = 3000;
        new Handler().postDelayed(() -> {
            Intent userMainActivity = new Intent(SplashScreenActivity.this, UserMainActivity.class);
            startActivity(userMainActivity);
            finish();
        }, splashDuration);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMainActivity();
            } else {
                Toast.makeText(this, "Camera-toestemming is vereist om verder te gaan", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
