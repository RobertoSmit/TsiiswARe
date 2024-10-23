package com.example.tsiisware;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrudMainActivityObjects extends AppCompatActivity {
    FirebaseFirestore db;
    EditText etUsername, etPassword;
    Spinner spinnerRole, spinnerUsers;
    Button btnCreate, btnDelete, btnGoToObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_objects);


        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        loadUsersIntoSpinner();

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
                            Toast.makeText(CrudMainActivityObjects.this, "Gebruiker succesvol aangemaakt", Toast.LENGTH_SHORT).show();
                            loadUsersIntoSpinner();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CrudMainActivityObjects.this, "Fout bij het aanmaken van gebruiker", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUser = spinnerUsers.getSelectedItem().toString();

                db.collection("users").document(selectedUser)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CrudMainActivityObjects.this, "Gebruiker succesvol verwijderd", Toast.LENGTH_SHORT).show();
                            loadUsersIntoSpinner();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CrudMainActivityObjects.this, "Fout bij het verwijderen van gebruiker", Toast.LENGTH_SHORT).show();
                        });
            }
        });
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
                Toast.makeText(CrudMainActivityObjects.this, "Fout bij het ophalen van gebruikers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
