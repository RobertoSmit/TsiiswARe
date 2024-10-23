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
    EditText etObjectName, etObjectDescription, etObjectVideoURL;
    Spinner spinnerObjects;
    Button btnCreateObjects, btnDeleteObjects, btnGoToUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crud_objects);

        db = FirebaseFirestore.getInstance();

        etObjectName = findViewById(R.id.etObjectName);
        etObjectDescription = findViewById(R.id.etObjectDescription);
        etObjectVideoURL = findViewById(R.id.etObjectVideoURL);
        spinnerObjects = findViewById(R.id.spinnerObjects);
        btnCreateObjects = findViewById(R.id.btnCreateObjects);
        btnDeleteObjects = findViewById(R.id.btnDeleteObjects);

        loadObjectsIntoSpinner();

        btnCreateObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String objectName = etObjectName.getText().toString();
                String objectDescription = etObjectDescription.getText().toString();
                String videoURL = etObjectVideoURL.getText().toString();

                if (objectName.isEmpty() || objectDescription.isEmpty() || videoURL.isEmpty()) {
                    Toast.makeText(CrudMainActivityObjects.this, "Alle velden moeten worden ingevuld", Toast.LENGTH_SHORT).show();
                } else {
                    addObjectToDatabase(objectName, objectDescription, videoURL);
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

        btnGoToUsers = findViewById(R.id.btnGoToUsers);
        btnGoToUsers.setOnClickListener(v -> {
            // Intent intent = new Intent(CrudMainActivityObjects.this, CrudMainActivityUsers.class);
            // startActivity(intent);
        });
    }

    private void addObjectToDatabase(String objectName, String objectDescription, String videoURL) {
        Map<String, Object> object = new HashMap<>();
        object.put("name", objectName);
        object.put("description", objectDescription);
        object.put("videoURL", videoURL);

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
                    adapter.add(objectName);
                }

                spinnerObjects.setAdapter(adapter);
            } else {
                Toast.makeText(CrudMainActivityObjects.this, "Fout bij het laden van objecten", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
