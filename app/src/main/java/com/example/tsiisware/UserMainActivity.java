package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class UserMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        Spinner spinnerCategories = findViewById(R.id.spinnerCategories);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerCategories.setAdapter(adapter);
    }

    public void onClickAdmin(View view) {
        Intent intent = new Intent(UserMainActivity.this, AdminMainActivity.class);
        startActivity(intent);
    }
}
