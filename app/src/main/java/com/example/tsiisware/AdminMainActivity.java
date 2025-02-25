package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class AdminMainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        Button goBack = findViewById(R.id.goBackBtn);

        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String enteredUsername = usernameInput.getText().toString().trim();
            String enteredPassword = passwordInput.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(AdminMainActivity.this, "Vul alle velden in", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("username", enteredUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            String storedHashedPassword = userDoc.getString("password");

                            if (storedHashedPassword != null && BCrypt.verifyer().verify(enteredPassword.toCharArray(), storedHashedPassword).verified)
 {
                                Toast.makeText(AdminMainActivity.this, "Inloggen geslaagd", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminMainActivity.this, CrudMainActivityObjects.class);
                                intent.putExtra("username", enteredUsername);
                                startActivity(intent);
                            } else {
                                Toast.makeText(AdminMainActivity.this, "Onjuiste gebruikersnaam of wachtwoord", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Gebruiker niet gevonden of fout bij het inloggen", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        goBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, UserMainActivity.class);
            startActivity(intent);
        });
    }
}
