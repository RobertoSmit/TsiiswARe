package com.example.tsiisware;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CrudMainActivityObjects extends AppCompatActivity {
    FirebaseFirestore db;
    EditText etObjectName, etObjectDescription, etObjectVideoURL, etQuestion, etAnswer1, etAnswer2, etAnswer3, etAnswer4;
    Spinner spinnerObjects;
    Button btnCreateObjects, btnDeleteObjects, btnGoToUsers, btnLogoffObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_objects);

        db = FirebaseFirestore.getInstance();

        etObjectName = findViewById(R.id.etObjectName);
        etObjectDescription = findViewById(R.id.etObjectDescription);
        etObjectVideoURL = findViewById(R.id.etObjectVideoURL);
        etQuestion = findViewById(R.id.etQuestion);
        etAnswer1 = findViewById(R.id.etAnswer1);
        etAnswer2 = findViewById(R.id.etAnswer2);
        etAnswer3 = findViewById(R.id.etAnswer3);
        etAnswer4 = findViewById(R.id.etAnswer4);
        spinnerObjects = findViewById(R.id.spinnerObjects);
        btnCreateObjects = findViewById(R.id.btnCreateObjects);
        btnDeleteObjects = findViewById(R.id.btnDeleteObjects);
        btnGoToUsers = findViewById(R.id.btnGoToUsers);
        btnLogoffObjects = findViewById(R.id.btnLogoffObjects);

        loadObjectsIntoSpinner();

        btnCreateObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String objectName = etObjectName.getText().toString();
                String objectDescription = etObjectDescription.getText().toString();
                String videoURL = etObjectVideoURL.getText().toString();
                String question = etQuestion.getText().toString();
                String answer1 = etAnswer1.getText().toString();
                String answer2 = etAnswer2.getText().toString();
                String answer3 = etAnswer3.getText().toString();
                String answer4 = etAnswer4.getText().toString();

                if (objectName.isEmpty() || objectDescription.isEmpty() || videoURL.isEmpty() || question.isEmpty() ||
                        answer1.isEmpty() || answer2.isEmpty() || answer3.isEmpty() || answer4.isEmpty()) {
                    Toast.makeText(CrudMainActivityObjects.this, "Alle velden moeten worden ingevuld", Toast.LENGTH_SHORT).show();
                } else {
                    addObjectToDatabase(objectName, objectDescription, videoURL, question, new String[]{answer1, answer2, answer3, answer4});
                }
            }
        });

        btnDeleteObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedObject = spinnerObjects.getSelectedItem().toString();
                deleteObjectFromDatabase(selectedObject);
            }
        });

        btnLogoffObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CrudMainActivityObjects.this, AdminMainActivity.class);
                startActivity(intent);
            }
        });

        btnGoToUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CrudMainActivityObjects.this, CrudMainActivityUsers.class);
                startActivity(intent);
            }
        });
    }

    private void addObjectToDatabase(String objectName, String objectDescription, String videoURL, String question, String[] answers) {
        Map<String, Object> object = new HashMap<>();
        object.put("label", objectName);
        object.put("text", objectDescription);
        object.put("video_url", videoURL);
        object.put("vraag", question);
        object.put("antwoorden", Arrays.asList(answers));

        db.collection("objects").document(objectName).set(object)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CrudMainActivityObjects.this, "Object toegevoegd", Toast.LENGTH_SHORT).show();
                    loadObjectsIntoSpinner();
                })
                .addOnFailureListener(e -> Toast.makeText(CrudMainActivityObjects.this, "Fout bij toevoegen object", Toast.LENGTH_SHORT).show());
    }


    private void deleteObjectFromDatabase(String objectName) {
        db.collection("objects").document(objectName).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CrudMainActivityObjects.this, "Object verwijderd", Toast.LENGTH_SHORT).show();
                    loadObjectsIntoSpinner();
                })
                .addOnFailureListener(e -> Toast.makeText(CrudMainActivityObjects.this, "Fout bij verwijderen object", Toast.LENGTH_SHORT).show());
    }

    private void loadObjectsIntoSpinner() {
        db.collection("objects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CrudMainActivityObjects.this, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                for (DocumentSnapshot document : task.getResult()) {
                    String objectName = document.getString("name");
                    if (objectName != null) {
                        adapter.add(objectName);
                    } else {
                        Log.w("CrudMainActivityObjects", "Null object name found in Firestore document: " + document.getId());
                    }
                }

                spinnerObjects.setAdapter(adapter);
            } else {
                Toast.makeText(CrudMainActivityObjects.this, "Fout bij het laden van objecten", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
