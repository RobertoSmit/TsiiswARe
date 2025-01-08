package com.example.tsiisware;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class InformationActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private String label;
    private String category;
    private ArrayList<String> selectedLabels;
    private int totalQuestions;
    private int questionProgress;
    private int correctQuestions;
    private int wrongQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_informationview_text_video);

        label = getIntent().getStringExtra("label");
        category = getIntent().getStringExtra("category");
        selectedLabels = getIntent().getStringArrayListExtra("selectedLabels");
        totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        questionProgress = getIntent().getIntExtra("questionProgress", 0);
        correctQuestions = getIntent().getIntExtra("correctQuestions", 0);
        wrongQuestions = getIntent().getIntExtra("wrongQuestions", 0);

        progressBar = findViewById(R.id.progressBar);
        updateProgressBar();

        setupTabLayoutAndViewPager();
    }

    private void setupTabLayoutAndViewPager() {
        TabLayout tabLayout = findViewById(R.id.tabLayoutInformation);
        ViewPager2 viewPager = findViewById(R.id.viewPagerInformation);

        ViewPagerAdapterInformation adapter = new ViewPagerAdapterInformation(this, label, category);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Past");
                    break;
                case 1:
                    tab.setText("Current");
                    break;
            }
        }).attach();
    }

    private void updateProgressBar() {
        if (totalQuestions > 0) {
            int progress = (int) ((float) questionProgress / totalQuestions * 100);
            progressBar.setProgress(progress);
        }
    }
}