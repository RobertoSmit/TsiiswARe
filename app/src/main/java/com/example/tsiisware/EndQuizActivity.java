package com.example.tsiisware;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EndQuizActivity extends AppCompatActivity {
    Integer aantal_goed = null;
    Integer aantal_fout = null;
    String visitorsEmail = null;
    Button btnSendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz_end);

        aantal_goed = getIntent().getIntExtra("aantal_goed", 0);
        aantal_fout = getIntent().getIntExtra("aantal_fout", 0);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        visitorsEmail = findViewById(R.id.visitorsEmailAdress).toString();

        btnSendEmail.setOnClickListener(this::sendEmail);
    }

    public void sendEmail(View view) {
        Toast.makeText(this, "Email sent to " + visitorsEmail, Toast.LENGTH_SHORT).show();
        // Send email to api with the email adress of the visitor
    }

}
