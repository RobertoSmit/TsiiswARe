package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class UserMainActivity extends AppCompatActivity {

    private EditText nameInput;
    private Spinner spinnerCategories;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        Button proceedButton = findViewById(R.id.proceedButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerCategories.setAdapter(adapter);

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToFirestore();
            }
        });

        View mainLayout = findViewById(R.id.main);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isKeyboardOpen()) {
                        hideKeyboard(v);
                    }
                }
                return true;
            }
        });
    }

    private void sendDataToFirestore() {
        String name = nameInput.getText().toString();
        String category = spinnerCategories.getSelectedItem().toString();
        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> userData = new HashMap<>();
        userData.put("Name", name);
        userData.put("Category", category);
        userData.put("Created_date", createdDate);

        db.collection("bezoekers")
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    Intent intent = new Intent(UserMainActivity.this, AR_Activity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding document: " + e.getMessage());
                });
    }

    private boolean isKeyboardOpen() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm != null && imm.isAcceptingText();
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                currentFocus.clearFocus();
            }
        }
    }

    public void onClickAdmin(View view) {
        Intent intent = new Intent(UserMainActivity.this, AdminMainActivity.class);
        startActivity(intent);
    }
}
