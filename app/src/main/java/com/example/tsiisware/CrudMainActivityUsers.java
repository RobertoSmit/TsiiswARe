package com.example.tsiisware;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CrudMainActivityUsers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_users);
        Button btnGoToObjects = findViewById(R.id.btnGoToObjects);
        Button btnLogout = findViewById(R.id.btnLogOutUsers);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new ViewPagerAdapterUsers(this));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(CrudMainActivityUsers.this, AdminMainActivity.class));
        });

        btnGoToObjects.setOnClickListener(v -> {
            startActivity(new Intent(CrudMainActivityUsers.this, CrudMainActivityObjects.class));
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Create User");
                    tab.setContentDescription("Create User");
                    break;
                case 1:
                    tab.setText("Delete User");
                    tab.setContentDescription("Delete User");
                    break;
            }
        }).attach();
    }
}