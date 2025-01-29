package com.example.tsiisware;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CrudMainActivityObjects extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_objects);
        Button btnGoToUsers = findViewById(R.id.btnGoToUsers);
        Button btnLogout = findViewById(R.id.btnLogOutObjects);
        ViewPager2 viewPager = findViewById(R.id.viewPagerObjects);
        TabLayout tabLayout = findViewById(R.id.tabLayoutObjects);

        viewPager.setAdapter(new ViewPagerAdapterObjects(this));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(CrudMainActivityObjects.this, AdminMainActivity.class));
        });

        btnGoToUsers.setOnClickListener(v -> {
            startActivity(new Intent(CrudMainActivityObjects.this, CrudMainActivityUsers.class));
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Create Video Object");
                    tab.setContentDescription("Create Video Object Tab");
                    break;
                case 1:
                    tab.setText("Create Quiz Object");
                    tab.setContentDescription("Create Quiz Object Tab");
                    break;
                case 2:
                    tab.setText("Delete Object");
                    tab.setContentDescription("Delete Object Tab");
                    break;
                case 3:
                    tab.setText("Create QR Code");
                    tab.setContentDescription("Create QR Code Tab");
            }
        }).attach();
    }
}