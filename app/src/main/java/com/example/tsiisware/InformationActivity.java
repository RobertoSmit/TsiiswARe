package com.example.tsiisware;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class InformationActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String label = null;
    String category = null;
    TextView quizQuestion;
    VideoView videoView;
    Button gobackButton, answer1, answer2, answer3, answer4;

    String correctAnswer;
    private static final String YOUTUBE_API_KEY = "YOUR_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        category = getIntent().getStringExtra("category");

        switch (category) {
            case "Quiz":
                setContentView(R.layout.activity_main_informationview_quiz);
                break;
            case "Text + Video":
                setContentView(R.layout.activity_main_informationview_text_video);
                break;
            case "Video":
                setContentView(R.layout.activity_main_informationview_video);
                break;
            default:
                setContentView(R.layout.ar_view);
        }

        gobackButton = findViewById(R.id.go_back);
        answer1 = findViewById(R.id.answer_1);
        answer2 = findViewById(R.id.answer_2);
        answer3 = findViewById(R.id.answer_3);
        answer4 = findViewById(R.id.answer_4);
        quizQuestion = findViewById(R.id.question);
        videoView = findViewById(R.id.videoView);

        getObjectInformation(label, category);

        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToARView();
            }
        });

        // Answer OnClickListeners
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer1.getText().toString())) {
                    // Correct answer
                    answer1.setBackgroundColor(Color.GREEN);
                } else {
                    // Wrong answer
                    answer1.setBackgroundColor(Color.RED);
                }
            }
        });

        answer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer2.getText().toString())) {
                    // Correct answer
                    answer2.setBackgroundColor(Color.GREEN);
                } else {
                    // Wrong answer
                    answer2.setBackgroundColor(Color.RED);
                }
            }
        });

        answer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer3.getText().toString())) {
                    // Correct answer
                    answer3.setBackgroundColor(Color.GREEN);
                } else {
                    // Wrong answer
                    answer3.setBackgroundColor(Color.RED);
                }
            }
        });

        answer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer4.getText().toString())) {
                    // Correct answer
                    answer4.setBackgroundColor(Color.GREEN);
                } else {
                    // Wrong answer
                    answer4.setBackgroundColor(Color.RED);
                }
            }
        });
    }

    private void getObjectInformation(String label, String category) {
        db = FirebaseFirestore.getInstance();
        db.collection("objects").document(label).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Uri videoURL = Uri.parse(document.getString("video_url"));
                    ARObject arobject = new ARObject(
                            document.getString("name"),
                            document.getString("description"),
                            videoURL,
                            document.getString("question"),
                            (List<String>) document.get("answers"),
                            document.getString("correct_answer")
                    );

                    if (category.equals("Quiz")) {
                        videoView.setVideoURI(arobject.getVideoURL());
                        quizQuestion.setText(arobject.getQuestion());
                        answer1.setText(arobject.getAnswers().get(0));
                        answer2.setText(arobject.getAnswers().get(1));
                        answer3.setText(arobject.getAnswers().get(2));
                        answer4.setText(arobject.getAnswers().get(3));

                        correctAnswer = arobject.getCorrectAnswer();
                    }
                }
            }
        });
    }

    private void goBackToARView() {
        Intent intent = new Intent(this, AR_Activity.class);
        startActivity(intent);
    }
}