package com.example.tsiisware;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserMainActivity extends AppCompatActivity {

    private EditText nameInput;
    private Button adminButton;
    private Spinner spinnerCategories;
    private FirebaseFirestore db;
    private boolean isQuizSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        db = FirebaseFirestore.getInstance();

        adminButton = findViewById(R.id.adminRedirect);
        nameInput = findViewById(R.id.nameInput);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        Button proceedButton = findViewById(R.id.proceedButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);

        spinnerCategories.setBackgroundResource(R.color.white);

        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isQuizSelected = position != 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isQuizSelected = false;
            }
        });


        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserMainActivity.this, AdminMainActivity.class);
            startActivity(intent);
        });

        proceedButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (name.length() < 2) {
                Toast.makeText(UserMainActivity.this, "Geef een geldige naam op!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isQuizSelected) {
                Toast.makeText(UserMainActivity.this, "Selecteer een geldige categorie!", Toast.LENGTH_SHORT).show();
            } else {
                sendDataToFirestore();
            }
        });


        View mainLayout = findViewById(R.id.main);
        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (isKeyboardOpen()) {
                    hideKeyboard(v);
                }
            }
            return true;
        });
    }

    private void sendDataToFirestore() {
        String name = nameInput.getText().toString();
        String category = spinnerCategories.getSelectedItem().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("category", category);
        userData.put("created_date", System.currentTimeMillis());

        db.collection("visitors")
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    Intent intent = new Intent(UserMainActivity.this, AR_Activity.class);
                    intent.putExtra("category", category);
                    startActivity(intent);
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
}
