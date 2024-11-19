package com.example.tsiisware;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class InformationActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String label = null;
    String category = null;
    TextView quizQuestion;
    WebView webView;
    Button gobackButton, answer1, answer2, answer3, answer4;

    String correctAnswer;

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
        webView = findViewById(R.id.webView);
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
//                    String iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"%s\" frameborder=\"0\" allowfullscreen></iframe>", document.getString("video_url"));
                    ARObject arobject = new ARObject(
                            document.getString("name"),
                            document.getString("description"),
                            document.getString("video"),
                            document.getString("question"),
                            (List<String>) document.get("answers"),
                            document.getString("correct_answer")
                    );

                    if (category.equals("Quiz")) {
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                        webView.setWebViewClient(new WebViewClient());
                        webView.setWebChromeClient(new WebChromeClient());
                        webView.loadData(arobject.getVideoURL(), "text/html", "utf-8");

                        // Disable user interaction
                        webView.setOnTouchListener((v, event) -> {
                            int width = webView.getWidth();
                            int height = webView.getHeight();
                            float x = event.getX();
                            float y = event.getY();

                            // Define the middle area (e.g., 20% of the width and height)
                            float middleAreaWidth = width * 0.75f;
                            float middleAreaHeight = height * 0.75f;
                            float middleXStart = (width - middleAreaWidth) / 2;
                            float middleYStart = (height - middleAreaHeight) / 2;

                            if (x >= middleXStart && x <= (middleXStart + middleAreaWidth) &&
                                    y >= middleYStart && y <= (middleYStart + middleAreaHeight)) {
                                return false; // Allow touch event
                            } else {
                                return true; // Ignore touch event
                            }
                        });

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