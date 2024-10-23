package com.example.tsiisware;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class CrudMainActivityUsers extends AppCompatActivity {
    FirebaseFirestore db;
    EditText etUsername, etPassword;
    Spinner spinnerRole, spinnerUsers;
    Button btnCreate, btnDelete, btnLogoff, btnGoToObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_users);


        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerUsers = findViewById(R.id.spinnerUsers);
        btnCreate = findViewById(R.id.btnCreateUsers);
        btnDelete = findViewById(R.id.btnDeleteUsers);
        btnLogoff = findViewById(R.id.btnLogoffUsers);
        btnGoToObjects = findViewById(R.id.btnGoToObject);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        loadUsersIntoSpinner();

        btnLogoff.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             Intent intent = new Intent(CrudMainActivityUsers.this, AdminMainActivity.class);
                                             startActivity(intent);
                                         }
                                     });
        btnGoToObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CrudMainActivityUsers.this, CrudMainActivityObjects.class);
                startActivity(intent);

            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String role = spinnerRole.getSelectedItem().toString();

                Map<String, Object> user = new HashMap<>();
                user.put("username", username);
                user.put("password", password);
                user.put("role", role);

                db.collection("users")
                        .document(username)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CrudMainActivityUsers.this, "Gebruiker succesvol aangemaakt", Toast.LENGTH_SHORT).show();
                            loadUsersIntoSpinner();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CrudMainActivityUsers.this, "Fout bij het aanmaken van gebruiker", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        View mainLayout = findViewById(R.id.crudLayoutUsers);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    if (isKeyboardOpen()) {
                        hideKeyboard(v);
                    }
                }
                return true;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUser = spinnerUsers.getSelectedItem().toString();

                db.collection("users").document(selectedUser)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CrudMainActivityUsers.this, "Gebruiker succesvol verwijderd", Toast.LENGTH_SHORT).show();
                            loadUsersIntoSpinner();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CrudMainActivityUsers.this, "Fout bij het verwijderen van gebruiker", Toast.LENGTH_SHORT).show();
                        });
            }
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


    private void loadUsersIntoSpinner() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayAdapter<String> usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                for (DocumentSnapshot document : task.getResult()) {
                    String username = document.getString("username");
                    usersAdapter.add(username);
                }

                spinnerUsers.setAdapter(usersAdapter);
            } else {
                Toast.makeText(CrudMainActivityUsers.this, "Fout bij het ophalen van gebruikers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
