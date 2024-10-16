package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminMainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);


        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        db = FirebaseFirestore.getInstance();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameInput.getText().toString().trim();
                String enteredPassword = passwordInput.getText().toString().trim();

                if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                    Toast.makeText(AdminMainActivity.this, "Vul alle velden in", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users")
                        .whereEqualTo("username", enteredUsername)
                        .whereEqualTo("password", enteredPassword)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot result = task.getResult();
                                if (!result.isEmpty()) {
                                    Toast.makeText(AdminMainActivity.this, "Inloggen geslaagd", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AdminMainActivity.this, ARView.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(AdminMainActivity.this, "Onjuiste gebruikersnaam of wachtwoord", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(AdminMainActivity.this, "Fout bij het inloggen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}