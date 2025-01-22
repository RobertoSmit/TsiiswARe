package com.example.tsiisware;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EndQuizActivity extends AppCompatActivity {
    Integer correctQuestions;
    Integer wrongQuestions;
    String visitorsEmail = null;
    Button btnSendEmail, btnGoToStart;

    TextView numGoedTxt, numFoutTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz_end);
        numGoedTxt = findViewById(R.id.numGoedTxt);
        numFoutTxt = findViewById(R.id.numFoutTxt);

        SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
        correctQuestions = sharedPreferences.getInt("correctQuestions", 0);
        wrongQuestions = sharedPreferences.getInt("wrongQuestions", 0);
        numGoedTxt.setText(String.valueOf(correctQuestions));
        numFoutTxt.setText(String.valueOf(wrongQuestions));


//        btnSendEmail = findViewById(R.id.btnSendEmail);
//        visitorsEmail = findViewById(R.id.visitorsEmailAdress).toString();

        btnGoToStart = findViewById(R.id.btnToStart);
        btnGoToStart.setOnClickListener(v -> {
            startActivity(new Intent(EndQuizActivity.this, UserMainActivity.class));
        });
//        btnSendEmail.setOnClickListener(this::sendEmail);
    }

    public void sendEmail(View view) {
        Toast.makeText(this, "Email sent to " + visitorsEmail, Toast.LENGTH_SHORT).show();
        // Send email to api with the email adress of the visitor
    }

}
